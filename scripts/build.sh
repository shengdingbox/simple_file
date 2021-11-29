#!/usr/bin/env bash

mvn clean
version=`awk '/<version>[^<]+<\/version>/{gsub(/<version>|<\/version>|\n|\r/,"",$1);print $1;exit;}' pom.xml`
version=$version"."$BUILD_NUMBER"-SNAPSHOT"
echo $version
mvn versions:set -DnewVersion=$version
mvn test
