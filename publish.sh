#!/bin/bash
set -e

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

./gradlew clean ${GRADLE_TASK} || EXIT_STATUS=$?

if [[ $EXIT_STATUS -ne 0 ]]; then
    echo "Project Build failed"
    exit $EXIT_STATUS
fi

git clone https://${GH_TOKEN}@github.com/${GITHUB_SLUG}.git -b ${GH_BRANCH} ${GH_BRANCH} --single-branch --depth 1 > /dev/null
cd ${GH_BRANCH}
cp -rv ../build/dist/. .
if git diff --quiet; then
  echo "No changes in MAIN Website"
else
  git add -A
  git commit -a -m "Updating $GITHUB_SLUG ${GH_BRANCH} branch for Github Actions run:$GITHUB_RUN_ID"
  git push https://oauth2:${GH_TOKEN}@github.com/${GITHUB_SLUG}.git ${GH_BRANCH}
fi

cd ..
rm -rf ${GH_BRANCH}

exit $EXIT_STATUS
