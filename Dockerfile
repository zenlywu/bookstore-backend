# 使用 OpenJDK 17 作為基礎映像
FROM openjdk:17-jdk-slim

# 設定 ARG 變數來傳入 JAR 檔案名稱
ARG JAR_FILE=target/*.jar

# 設定工作目錄
WORKDIR /app

# 複製 JAR 檔案到容器內
COPY ${JAR_FILE} app.jar

# 設定容器啟動指令
ENTRYPOINT ["java", "-jar", "app.jar"]

# 開放 8080 端口（Spring Boot 預設埠）
EXPOSE 8080
