/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package website.gradle.tasks

import groovy.transform.CompileStatic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.process.ExecOperations

import javax.inject.Inject
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes

/**
 * Mirrors {@code build/dist/} into a deploy branch on a separate repo and
 * pushes the resulting commit if anything changed. Replaces the legacy
 * {@code publish.sh} bash script with a Gradle-native task so all
 * operational entry points are uniform: {@code ./gradlew <task>}.
 *
 * Inputs (all read from environment variables to preserve compatibility
 * with the existing CI workflow secrets):
 * <ul>
 *   <li>{@code GH_TOKEN} (required) - PAT used to clone and push to the
 *       deploy repo.</li>
 *   <li>{@code GITHUB_SLUG} (required, defaulted to {@code apache/grails-website}) -
 *       owner/name of the deploy repo.</li>
 *   <li>{@code GH_BRANCH} (required, defaulted to {@code asf-site-production}) -
 *       branch on the deploy repo that receives the mirror push.</li>
 *   <li>{@code GITHUB_ACTOR} (optional) - committer name. Defaults to
 *       {@code github-actions[bot]}.</li>
 *   <li>{@code GITHUB_RUN_ID} (optional) - included in the commit message.
 *       Defaults to {@code local}.</li>
 * </ul>
 *
 * Behavior:
 * <ol>
 *   <li>The task depends on {@code build}, so the dist directory is
 *       produced automatically in the same Gradle invocation. Callers
 *       typically invoke {@code ./gradlew clean publishMainSite}.</li>
 *   <li>Clones the deploy branch into {@code build/publish/main-site/}
 *       (single-branch, depth 1).</li>
 *   <li>Mirror-syncs {@code build/dist/} into that clone (deletes any
 *       file that does not exist in the source, preserves the cloned
 *       {@code .git/} metadata).</li>
 *   <li>Stages all changes; if {@code git status --porcelain} is empty,
 *       reports "no changes" and exits 0 without pushing.</li>
 *   <li>Otherwise commits and pushes back to {@code GH_BRANCH}.</li>
 * </ol>
 *
 * The task uses {@link ExecOperations} (configuration-cache compatible)
 * to invoke the {@code git} executable. Java NIO does the file mirror,
 * so there is no {@code rsync} runtime dependency on Windows local dev
 * boxes.
 */
@CompileStatic
abstract class PublishMainSiteTask extends DefaultTask {

    public static final String NAME = 'publishMainSite'
    public static final String GROUP = 'migration'
    public static final String DEFAULT_GITHUB_SLUG = 'apache/grails-website'
    public static final String DEFAULT_DEPLOY_BRANCH = 'asf-site-production'

    @Internal
    final String description = 'Mirror the built site into the deploy repo and push if changed'

    String group = GROUP

    @Inject
    abstract ExecOperations getExec()

    /**
     * The {@code owner/name} slug of the deploy repository. Sourced from
     * {@code GITHUB_SLUG}, defaulted to {@link #DEFAULT_GITHUB_SLUG}.
     */
    @Input
    abstract Property<String> getGitHubSlug()

    /**
     * The deploy branch to receive the mirror push. Sourced from
     * {@code GH_BRANCH}, defaulted to {@link #DEFAULT_DEPLOY_BRANCH}.
     */
    @Input
    abstract Property<String> getDeployBranch()

    /**
     * The committer name. Sourced from {@code GITHUB_ACTOR}.
     */
    @Internal
    abstract Property<String> getCommitAuthor()

    /**
     * Optional CI run id used in the commit message body.
     */
    @Internal
    abstract Property<String> getRunId()

    /**
     * The PAT used to clone and push. Sensitive, so it is {@code @Internal}
     * (not part of the up-to-date check input fingerprint).
     */
    @Internal
    abstract Property<String> getGhToken()

    /**
     * The build output to publish (typically {@code build/dist/}). Marked
     * {@code @Internal} (rather than {@code @InputDirectory}) because:
     * <ul>
     *   <li>The task always wants to inspect whatever is on disk at
     *       execution time. The {@code git status --porcelain} check that
     *       happens after the mirror sync is the real "anything to push?"
     *       gate; up-to-date caching is not desirable here.</li>
     *   <li>Many other tasks ({@code renderBlog}, {@code renderMinutes},
     *       {@code genPlugins}, {@code genHtaccess},
     *       {@code genBskyAtProtoDid}, {@code genSitemap}, ...) write
     *       into this directory. {@code @InputDirectory} would force a
     *       direct {@code dependsOn} declaration on every one of them; the
     *       {@code dependsOn 'build'} edge already orders them correctly
     *       in the task graph.</li>
     * </ul>
     */
    @Internal
    abstract DirectoryProperty getDistDir()

    /**
     * The working directory used to host the deploy-branch clone. Cleared
     * between runs.
     */
    @Internal
    abstract DirectoryProperty getDeployWorkDir()

    static TaskProvider<PublishMainSiteTask> register(Project project) {
        project.tasks.register(NAME, PublishMainSiteTask) { PublishMainSiteTask task ->
            task.dependsOn('build')
            // Guides landing/tags/categories.
            task.dependsOn('buildGuides')
            // Per-version vendored guide corpus.
            task.dependsOn('buildAllGuides')
            task.gitHubSlug.convention(
                    project.providers.environmentVariable('GITHUB_SLUG')
                            .orElse(DEFAULT_GITHUB_SLUG))
            task.deployBranch.convention(
                    project.providers.environmentVariable('GH_BRANCH')
                            .orElse(DEFAULT_DEPLOY_BRANCH))
            task.commitAuthor.convention(
                    project.providers.environmentVariable('GITHUB_ACTOR')
                            .orElse('github-actions[bot]'))
            task.runId.convention(
                    project.providers.environmentVariable('GITHUB_RUN_ID')
                            .orElse('local'))
            task.ghToken.convention(
                    project.providers.environmentVariable('GH_TOKEN'))
            task.distDir.convention(project.layout.buildDirectory.dir('dist'))
            task.deployWorkDir.convention(project.layout.buildDirectory.dir('publish/main-site'))
        }
    }

    @TaskAction
    void publish() {
        String token = ghToken.getOrNull()
        if (!token) {
            throw new GradleException(
                    'GH_TOKEN environment variable is required to publish. ' +
                            'Set it (or run from a CI job that injects it as a secret) and retry.')
        }
        String slug = gitHubSlug.get()
        String branch = deployBranch.get()
        String author = commitAuthor.get()
        String run = runId.get()
        File distRoot = distDir.get().asFile
        File deployRoot = deployWorkDir.get().asFile

        if (!distRoot.directory) {
            throw new GradleException("distDir does not exist: ${distRoot.absolutePath}. Did the build task run?")
        }

        // 1. Wipe and recreate the deploy work dir.
        if (deployRoot.exists() && !deployRoot.deleteDir()) {
            throw new GradleException("Failed to clean previous deploy directory: ${deployRoot.absolutePath}")
        }
        deployRoot.parentFile.mkdirs()

        // 2. Shallow-clone the deploy branch.
        String cloneUrl = "https://${token}@github.com/${slug}.git"
        runGit(deployRoot.parentFile,
                'clone', '--branch', branch, '--single-branch', '--depth', '1',
                cloneUrl, deployRoot.absolutePath)

        // 3. Configure committer for this clone (no global state mutation).
        runGit(deployRoot, 'config', 'user.name', author)
        runGit(deployRoot, 'config', 'user.email', "${author}@users.noreply.github.com".toString())

        // 4. Layer the dist tree onto the deploy clone (additive overwrite).
        // Files only present on the deploy branch (e.g. ASF .asf.yaml) are
        // preserved; explicit removals must go through a separate cleanup.
        Path deployPath = deployRoot.toPath()
        Path distPath = distRoot.toPath()
        copyTree(distPath, deployPath)

        // 5. Stage changes and detect any actual diff.
        runGit(deployRoot, 'add', '-A')
        String porcelain = captureGit(deployRoot, 'status', '--porcelain')
        if (porcelain.empty) {
            logger.lifecycle('No changes in MAIN Website')
            return
        }

        // 6. Commit + push.
        String commitMessage = "Updating ${slug} ${branch} branch for GitHub Actions run:${run}"
        runGit(deployRoot, 'commit', '-m', commitMessage)
        // Re-use the credential-bearing URL so this push does not depend on
        // any global git credential helper state.
        String pushUrl = "https://oauth2:${token}@github.com/${slug}.git"
        runGit(deployRoot, 'push', pushUrl, branch)
    }

    private void runGit(File workingDir, String... args) {
        List<String> cmd = ['git']
        cmd.addAll(args.toList())
        exec.exec { spec ->
            spec.workingDir = workingDir
            spec.commandLine = cmd
        }
    }

    private String captureGit(File workingDir, String... args) {
        List<String> cmd = ['git']
        cmd.addAll(args.toList())
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        exec.exec { spec ->
            spec.workingDir = workingDir
            spec.commandLine = cmd
            spec.standardOutput = out
        }
        return new String(out.toByteArray(), 'UTF-8').trim()
    }

    /**
     * Copy every file and subdirectory under {@code source} into
     * {@code target}, overwriting existing files. Files only present
     * on {@code target} are left in place.
     */
    private static void copyTree(Path source, Path target) {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                Path dest = target.resolve(source.relativize(dir).toString())
                Files.createDirectories(dest)
                return FileVisitResult.CONTINUE
            }

            @Override
            FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                Path dest = target.resolve(source.relativize(file).toString())
                Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
                return FileVisitResult.CONTINUE
            }
        })
    }
}
