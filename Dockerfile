FROM openjdk:8u252

ENV TZ=Asia/Shanghai

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone


ADD target/*.jar /root/app.jar

ENV JAVA_OPTS="\
-Xms512m \
-Xmx2048m \
-Xmn256m \
-XX:MetaspaceSize=1024m \
-XX:MaxMetaspaceSize=1024m"

ENV ACTIVE="k8s"
ENTRYPOINT java ${JAVA_OPTS} -jar /root/app.jar --spring.profiles.active=${ACTIVE}

EXPOSE 8081
