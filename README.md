# Getting Started

### 删除文件
* 删除idea文件demo-service.iml
* 删除根目录下的 .git
* 删除根目录下的 .idea

### 修改pom

    <groupId>com.icitic.rp</groupId>--修改组名
    <artifactId>demo-service</artifactId>--修改工程名
    <version>0.0.1-SNAPSHOT</version>
    <name>demo-service</name>--与工程名一致即可
    <description>模版工程</description>--修改工程描述

### sonar-project.properties
修改为相应的服务层包地址
sonar.sources=src/main/java/com/icitic/rp/demo/service

###重命名包名

使用idea或者eclipse重命名com.icitic.rp.demo

###修改配置
application-local.properties
修改bootstrap.yml的服务名和端口
修改swagger2.java的检测包名
###修改脚本
修改scripts目录下的go.sh脚本:
projectName=demo-service