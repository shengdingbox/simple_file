FROM openjdk:8u252

# 安装 Git
RUN apt-get update && apt-get install -y git

# 安装 Maven
ENV MAVEN_VERSION 3.6.3
ENV MAVEN_HOME /usr/share/maven

RUN cd /tmp && \
    wget -q https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar -xf apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    mv apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} && \
    rm apache-maven-${MAVEN_VERSION}-bin.tar.gz

ENV PATH ${MAVEN_HOME}/bin:${PATH}

# 设置工作目录
WORKDIR /app

# 拷贝代码到容器
RUN git clone https://github.com/shengdingbox/simple_file.git

WORKDIR /app/simple_file

# 构建和运行应用
RUN mvn clean package -DskipTests=false

ENV TZ=Asia/Shanghai

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# ADD /app/simple_file/target/*.jar /root/app.jar

ENV JAVA_OPTS="\
-Xms512m \
-Xmx2048m \
-Xmn256m \
-XX:MetaspaceSize=1024m \
-XX:MaxMetaspaceSize=1024m"

ENV ACTIVE="pro"
ENTRYPOINT java ${JAVA_OPTS} -jar /app/simple_file/target/*.jar --spring.profiles.active=${ACTIVE}

EXPOSE 8081
