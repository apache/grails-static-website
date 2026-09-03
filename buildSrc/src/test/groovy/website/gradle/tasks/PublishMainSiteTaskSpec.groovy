/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package website.gradle.tasks

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification
import spock.lang.TempDir

class PublishMainSiteTaskSpec extends Specification {

    @TempDir
    File tempDir

    private static String previousSkipRetryBackoff

    def setupSpec() {
        previousSkipRetryBackoff = System.getProperty('website.publish.skipRetryBackoff')
        System.setProperty('website.publish.skipRetryBackoff', 'true')
    }

    def cleanupSpec() {
        if (previousSkipRetryBackoff == null) {
            System.clearProperty('website.publish.skipRetryBackoff')
        } else {
            System.setProperty('website.publish.skipRetryBackoff', previousSkipRetryBackoff)
        }
    }

    void 'retryablePushRejection recognizes porcelain non-fast-forward lines'() {
        expect:
        PublishMainSiteTask.retryablePushRejection(
                '!\tHEAD:refs/heads/asf-site-production\t[rejected] (non-fast-forward)')
        PublishMainSiteTask.retryablePushRejection(
                '!\tHEAD:refs/heads/asf-site-production\t[rejected] (fetch first)')
        !PublishMainSiteTask.retryablePushRejection('error: failed to push some refs')
        !PublishMainSiteTask.retryablePushRejection('')
    }

    void 'redactAuthenticatedUrls masks credentials in git output'() {
        expect:
        PublishMainSiteTask.redactAuthenticatedUrls(
                'remote: https://oauth2:secret-token@github.com/apache/grails-website.git') ==
                'remote: https://***@github.com/apache/grails-website.git'
    }

    void 'first push succeeds when the destination has not moved'() {
        given:
        GitFixture git = new GitFixture(tempDir)
        git.seedRemote()
        File publisher = git.clonePublisher('publisher')
        String clonedTip = git.revParse(publisher)
        new File(publisher, 'index.html').text = 'site-a'
        git.commitAll(publisher, 'publish site a')
        PublishMainSiteTask task = newTask()

        when:
        task.pushWithRetry(publisher, git.remoteUrl, 'asf-site-production', clonedTip)

        then:
        git.fileOnRemote('index.html') == 'site-a'
        git.fileOnRemote('docs/keep.txt') == 'core-docs'
    }

    void 'disjoint concurrent commit is rebased and both changes are kept'() {
        given:
        GitFixture git = new GitFixture(tempDir)
        git.seedRemote()
        File publisher = git.clonePublisher('publisher')
        String clonedTip = git.revParse(publisher)
        new File(publisher, 'index.html').text = 'site-a'
        git.commitAll(publisher, 'publish site a')

        File other = git.clonePublisher('other')
        new File(other, 'docs/keep.txt').text = 'core-docs-updated'
        git.commitAll(other, 'core docs')
        git.push(other)

        PublishMainSiteTask task = newTask()

        when:
        task.pushWithRetry(publisher, git.remoteUrl, 'asf-site-production', clonedTip)

        then:
        git.fileOnRemote('index.html') == 'site-a'
        git.fileOnRemote('docs/keep.txt') == 'core-docs-updated'
    }

    void 'conflicting rebase aborts without replacing the remote winner'() {
        given:
        GitFixture git = new GitFixture(tempDir)
        git.seedRemote()
        File publisher = git.clonePublisher('publisher')
        String clonedTip = git.revParse(publisher)
        new File(publisher, 'index.html').text = 'site-a'
        git.commitAll(publisher, 'publish site a')

        File other = git.clonePublisher('other')
        new File(other, 'index.html').text = 'site-b'
        git.commitAll(other, 'publish site b')
        git.push(other)

        PublishMainSiteTask task = newTask()

        when:
        task.pushWithRetry(publisher, git.remoteUrl, 'asf-site-production', clonedTip)

        then:
        def e = thrown(GradleException)
        e.message.contains('Rebase failed')
        git.fileOnRemote('index.html') == 'site-b'
    }

    void 'unrelated push failures are not retried'() {
        given:
        GitFixture git = new GitFixture(tempDir)
        git.seedRemote()
        File publisher = git.clonePublisher('publisher')
        String clonedTip = git.revParse(publisher)
        new File(publisher, 'index.html').text = 'site-a'
        git.commitAll(publisher, 'publish site a')
        PublishMainSiteTask task = newTask()

        when:
        task.pushWithRetry(publisher, git.remoteUrl + '-missing', 'asf-site-production', clonedTip)

        then:
        def e = thrown(GradleException)
        e.message.contains('without a retryable non-fast-forward rejection')
    }

    void 'rewritten remote history fails closed'() {
        given:
        GitFixture git = new GitFixture(tempDir)
        git.seedRemote()
        File publisher = git.clonePublisher('publisher')
        String clonedTip = git.revParse(publisher)
        new File(publisher, 'index.html').text = 'site-a'
        git.commitAll(publisher, 'publish site a')

        git.replaceRemoteWithUnrelatedHistory()

        PublishMainSiteTask task = newTask()

        when:
        task.pushWithRetry(publisher, git.remoteUrl, 'asf-site-production', clonedTip)

        then:
        def e = thrown(GradleException)
        e.message.contains('not a descendant') || e.message.contains('without a retryable')
        git.fileOnRemote('index.html') == 'unrelated'
    }

    private PublishMainSiteTask newTask() {
        def project = ProjectBuilder.builder().withProjectDir(new File(tempDir, 'proj')).build()
        PublishMainSiteTask.register(project)
        return project.tasks.getByName(PublishMainSiteTask.NAME) as PublishMainSiteTask
    }

    private static final class GitFixture {
        final File root
        final File remote
        final String remoteUrl

        GitFixture(File tempDir) {
            root = tempDir
            remote = new File(tempDir, 'remote.git')
            remoteUrl = remote.absolutePath
        }

        void seedRemote() {
            File seed = new File(root, 'seed')
            seed.mkdirs()
            git(seed, 'init', '-b', 'asf-site-production')
            configureIdentity(seed)
            new File(seed, 'index.html').text = 'original'
            new File(seed, 'docs').mkdirs()
            new File(seed, 'docs/keep.txt').text = 'core-docs'
            commitAll(seed, 'seed')
            git(root, 'clone', '--bare', seed.absolutePath, remote.absolutePath)
        }

        File clonePublisher(String name) {
            File dir = new File(root, name)
            git(root, 'clone', '--branch', 'asf-site-production', '--single-branch', remoteUrl, dir.absolutePath)
            configureIdentity(dir)
            return dir
        }

        void commitAll(File repo, String message) {
            git(repo, 'add', '-A')
            git(repo, 'commit', '--no-verify', '-m', message)
        }

        void push(File repo) {
            git(repo, 'push', 'origin', 'asf-site-production')
        }

        String revParse(File repo) {
            git(repo, 'rev-parse', 'HEAD').trim()
        }

        String fileOnRemote(String path) {
            File inspect = new File(root, "inspect-${UUID.randomUUID()}")
            git(root, 'clone', '--branch', 'asf-site-production', '--single-branch', '--depth', '1',
                    remoteUrl, inspect.absolutePath)
            return new File(inspect, path).text
        }

        void replaceRemoteWithUnrelatedHistory() {
            File other = new File(root, 'unrelated')
            other.mkdirs()
            git(other, 'init', '-b', 'asf-site-production')
            configureIdentity(other)
            new File(other, 'index.html').text = 'unrelated'
            commitAll(other, 'orphan')
            git(other, 'remote', 'add', 'origin', remoteUrl)
            git(other, 'push', '--force', 'origin', 'HEAD:asf-site-production')
        }

        private static void configureIdentity(File repo) {
            git(repo, 'config', 'user.email', 'ci@example.com')
            git(repo, 'config', 'user.name', 'ci')
        }

        private static String git(File dir, String... args) {
            List<String> cmd = ['git', '-c', 'core.hooksPath=']
            cmd.addAll(args.toList())
            Process proc = new ProcessBuilder(cmd)
                    .directory(dir)
                    .redirectErrorStream(true)
                    .start()
            String out = proc.inputStream.text
            int code = proc.waitFor()
            if (code != 0) {
                throw new IllegalStateException("git ${args.join(' ')} failed (${code}): ${out}")
            }
            return out
        }
    }
}
