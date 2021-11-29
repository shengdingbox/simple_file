#!/usr/bin/env bash

projectName=demo-service
target=$1
port=$2
config_uri=$3

health_url="http://127.0.0.1:$port/actuator/health"

echo "target = "$target

pid=`ps -ef |grep 'java' |grep $projectName |grep $port |awk '{print $2}'`
if [ -n "$pid" ]; then
    kill -9 $pid
    sleep 3
fi

case $target in
    dev)
        JAVA_OPTS="-Xms512m -Xmx512m -Xss256k -Xmn256m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=384m -XX:SurvivorRatio=8"
        JAVA_OPTS="$JAVA_OPTS -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:../logs/$projectName/gc.log"
        JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=../logs/$projectName/dump"
        JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$target -Dserver.port=$port -Dspring.cloud.nacos.config.server-addr=$config_uri "
        nohup java -jar $JAVA_OPTS $projectName*.jar > /dev/null &
    ;;
    sit | tag)
        JAVA_OPTS="-Xms512m -Xmx512m -Xss256k -Xmn256m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=384m -XX:SurvivorRatio=8"
        JAVA_OPTS="$JAVA_OPTS -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:../logs/$projectName/gc.log"
        JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=../logs/$projectName/dump"
        JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$target -Dserver.port=$port -Dspring.cloud.nacos.config.server-addr=$config_uri"
        nohup java -jar $JAVA_OPTS $projectName*.jar > /dev/null &
    ;;
    uat)
        JAVA_OPTS="-Xms1024m -Xmx1024m -Xss256k -Xmn256m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=384m -XX:SurvivorRatio=8"
        JAVA_OPTS="$JAVA_OPTS -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/home/logs/$projectName/gc.log"
        JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=..logs/$projectName/dump"
        JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=uat -Dserver.port=$port -Dspring.cloud.nacos.config.server-addr=$config_uri"
        nohup java -jar $JAVA_OPTS $projectName*.jar > /dev/null &
    ;;
    prod)
        JAVA_OPTS="-Xms2048m -Xmx2048m -Xss256k -Xmn256m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=384m -XX:SurvivorRatio=8"
        JAVA_OPTS="$JAVA_OPTS -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:../logs/$projectName/gc.log"
        JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=../logs/$projectName/dump"
        JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$target -Dserver.port=$port -Dspring.cloud.nacos.config.server-addr=$config_uri"
        nohup java -jar $JAVA_OPTS $projectName*.jar > /dev/null &
    ;;
    *)
        echo "No this ENV:$target."
        exit 1
    ;;
esac


sleep 20
for ((i=0;i<20;i++))
do
  curl -s ${health_url} > health
  state=`cat health |grep status |grep UP`
  if [ ${#state} -gt 10 ]; then
    break
  else
    echo "Waiting for start ..."
    sleep 5
  fi
done
if [ ${#state} -gt 10 ]; then
  echo "Deploy Success"
  exit 0
else
  echo "Deploy Fail"
  exit 1
fi

