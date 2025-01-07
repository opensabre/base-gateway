# 使用 openjdk:23-slim 作为基础镜像
FROM openjdk:23-alpine

# 添加参数 JAR_FILE，用于指定要添加到镜像中的 JAR 文件名称
ARG JAR_FILE

# 将 target/${JAR_FILE} 文件添加到镜像中，并命名为 app.jar
ADD target/${JAR_FILE} app.jar

# 设置容器启动时执行的命令，使用 java 命令运行 app.jar，并设置随机数生成器
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]