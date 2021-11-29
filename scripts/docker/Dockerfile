FROM 12.99.110.170/java:8

RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/shanghai" > /etc/timezone;

VOLUME /tmp

ADD target/storey-service-*.jar /work/app.jar
ADD run.sh /

ENTRYPOINT ["/run.sh"]
