#!/usr/bin/env bash

[[ -d target ]] || mkdir target
java -jar ../checkstyle-8.12-all.jar -c scripts/checkstyle/checkstyle.xml src/main/java > target/checkstyle-result.log
cat target/checkstyle-result.log 2>&1
wc=`cat target/checkstyle-result.log |grep -E 'WARN|ERROR' |wc -l`
if [ $wc != 0 ]; then
  exit 1
fi

