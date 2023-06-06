#!/usr/bin/env bash

set -eo pipefail

# Trap not-normal exit signals: 1/HUP, 2/INT, 3/QUIT, 15/TERM
trap catch_sig 1 2 3 15
# Trap errors (simple commands exiting with a non-zero status)
trap 'catch_err ${LINENO}' ERR

if [ $# -ne 2 ]; then
  echo "Usage: release.sh <release_version> <new_version>"
  exit 1
fi

release_version=$1
new_version=$2

mvn versions:set "-DnewVersion=$release_version" -DgenerateBackupPoms=false

git commit -a -m "$release_version release"
git tag -a "$release_version" -a -m "$release_version release"

mvn deploy -DskipTests -Prelease

mvn versions:set "-DnewVersion=$new_version" -DgenerateBackupPoms=false
git commit -a -m "Preparing $new_version iteration"

git push
git push origin "$release_version"
