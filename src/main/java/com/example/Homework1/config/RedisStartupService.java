package com.example.Homework1.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class RedisStartupService {

    private static final String CONTAINER_NAME = "redis";

    @PostConstruct
    public void startRedisOnBoot() {
        try {
            // 🔍 檢查 Docker 是否已運行
            if (!isDockerRunning()) {
                System.out.println("⚠️ Docker 未啟動，嘗試啟動...");
                startDocker();
                Thread.sleep(10000); // 等待 10 秒讓 Docker 完全啟動
            }

            // 🔍 檢查 Redis 容器是否已經存在
            if (!isRedisContainerExist()) {
                System.out.println("🚀 Redis 容器不存在，正在創建...");
                createRedisContainer();
            }

            // 🔍 檢查 Redis 是否在運行
            if (!isRedisRunning()) {
                System.out.println("🔄 Redis 未運行，正在啟動...");
                startRedisContainer();
            } else {
                System.out.println("✅ Redis 已在運行！");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ 啟動 Redis 失敗：" + e.getMessage());
        }
    }

    // ✅ 檢查 Docker 是否運行
    private boolean isDockerRunning() throws IOException, InterruptedException {
        Process checkDocker = Runtime.getRuntime().exec("docker info");
        return checkDocker.waitFor() == 0;
    }

    // ✅ 嘗試啟動 Docker（適用於 Windows Docker Desktop）
    private void startDocker() throws IOException, InterruptedException {
        Process startDocker = Runtime.getRuntime().exec("cmd /c start docker");
        startDocker.waitFor();
        System.out.println("✅ Docker Desktop 已啟動！");
    }

    // ✅ 檢查 Redis 容器是否已存在
    private boolean isRedisContainerExist() throws IOException, InterruptedException {
        Process checkRedis = Runtime.getRuntime().exec("docker ps -a --filter \"name=" + CONTAINER_NAME + "\" --format \"{{.Names}}\"");
        BufferedReader reader = new BufferedReader(new InputStreamReader(checkRedis.getInputStream()));
        String output = reader.readLine();
        checkRedis.waitFor();
        return output != null && output.equals(CONTAINER_NAME);
    }

    // ✅ 檢查 Redis 是否在運行
    private boolean isRedisRunning() throws IOException, InterruptedException {
        Process checkRedis = Runtime.getRuntime().exec("docker ps --filter \"name=" + CONTAINER_NAME + "\" --format \"{{.Names}}\"");
        BufferedReader reader = new BufferedReader(new InputStreamReader(checkRedis.getInputStream()));
        String output = reader.readLine();
        checkRedis.waitFor();
        return output != null && output.equals(CONTAINER_NAME);
    }

    // ✅ 創建 Redis 容器
    private void createRedisContainer() throws IOException, InterruptedException {
        Process createRedis = Runtime.getRuntime().exec("docker run --name " + CONTAINER_NAME + " -d -p 6379:6379 redis");
        createRedis.waitFor();
        System.out.println("✅ Redis 容器已創建！");
    }

    // ✅ 啟動 Redis 容器
    private void startRedisContainer() throws IOException, InterruptedException {
        Process startRedis = Runtime.getRuntime().exec("docker start " + CONTAINER_NAME);
        startRedis.waitFor();
        System.out.println("✅ Redis 容器已啟動！");
    }
}
