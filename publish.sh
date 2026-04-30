#!/bin/bash
set -e

# Fail fast if required token is missing.
if [ -z "$GH_TOKEN" ]
then
  echo "You must provide the action with a GitHub Personal Access Token secret in order to deploy."
  exit 1
fi

if [ -z "$COMMIT_EMAIL" ]
then
  COMMIT_EMAIL="${GITHUB_ACTOR}@users.noreply.github.com"
fi

if [ -z "$COMMIT_NAME" ]
then
  COMMIT_NAME="${GITHUB_ACTOR}"
fi

git config --global user.name "$GITHUB_ACTOR"
git config --global user.email "$GITHUB_ACTOR@users.noreply.github.com"

# Run the Gradle build. The `|| EXIT_STATUS=$?` intentionally suppresses
# `set -e` so a clear failure message can be printed; the exit is re-raised
# below.
EXIT_STATUS=0
./gradlew clean ${GRADLE_TASK} || EXIT_STATUS=$?

if [[ $EXIT_STATUS -ne 0 ]]; then
    echo "Project Build failed"
    exit $EXIT_STATUS
fi

git clone https://${GH_TOKEN}@github.com/${GITHUB_SLUG}.git -b ${GH_BRANCH} ${GH_BRANCH} --single-branch --depth 1 > /dev/null
cd ${GH_BRANCH}

# Mirror sync the build output into the deploy branch. Using rsync rather
# than cp -r so files removed from build/dist/ also get removed from the
# deploy branch (the previous cp -r approach accumulated stale files
# forever). --exclude='.git' preserves the deploy branch's git metadata.
rsync -a --delete --exclude='.git' ../build/dist/ ./

git add -A
# Use git status --porcelain to detect ANY change (tracked or untracked).
# The previous `git diff --quiet` missed pure-addition cases because it
# only inspected tracked files.
if [ -z "$(git status --porcelain)" ]; then
  echo "No changes in MAIN Website"
else
  git commit -m "Updating $GITHUB_SLUG ${GH_BRANCH} branch for GitHub Actions run:$GITHUB_RUN_ID"
  git push https://oauth2:${GH_TOKEN}@github.com/${GITHUB_SLUG}.git ${GH_BRANCH}
fi

cd ..
rm -rf ${GH_BRANCH}

exit 0
