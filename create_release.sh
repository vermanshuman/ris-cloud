#!/bin/bash

set -e

mvn clean

mvn org.codehaus.mojo:build-helper-maven-plugin:3.3.0:remove-project-artifact
mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.nextMajorVersion}
NEW_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:3.4.0:evaluate -Dexpression=project.version -q -DforceStdout)

mvn install

git add pom.xml
git add src/main/resources/effective-pom.xml
git commit -m "Build new version $NEW_VERSION"

echo "  -> $(pwd)/target/RIS.war"