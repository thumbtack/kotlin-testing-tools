#!/bin/bash

set -e

if [[ $(git status --porcelain) ]]; then
    echo "Please run this command from a clean workspace"
    exit 1
fi

if [ "$1" == "" ]
then
  echo "Usage: release.sh <version>"
  exit 1
fi

# Pull latest changes for main branch

git checkout main
git pull

# Create a local release branch

git checkout -b "release/$1"

# Update version in podspec.
# (Search podspec for `version = '1.2.3` and update with new version
# number passed in as script argument).
#
# Regex pattern adapted from
# https://semver.org/#is-there-a-suggested-regular-expression-regex-to-check-a-semver-string
# for compatibility with sed.
sed -i "" -E "s/^version *= *(["'"'"'])(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(-(0|[1-9][0-9]]*|[0-9]*[a-zA-Z-][0-9a-zA-Z-]*)(\.(0|[1-9][0-9]*|[0-9]*[a-zA-Z-][0-9a-zA-Z-]*))*)?(\+[0-9a-zA-Z-]+(\.[0-9a-zA-Z-]+)*)?["'"'"']/version = \1$1\1/g" library/build.gradle.kts

# Commit changes

git add --all
git commit -m "Release $1"

# Merge into the main branch

git checkout main
git merge "release/$1"

# Push to github

git push origin main

# Tag this release

git tag "$1"

# Push the tag to github

git push origin tag "$1"
