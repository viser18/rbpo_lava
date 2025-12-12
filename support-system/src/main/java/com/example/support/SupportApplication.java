package com.example.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SupportApplication {
    public static void main(String[] args) {
        // Простая загрузка .env без дополнительных библиотек
        loadEnvVariables();
        SpringApplication.run(SupportApplication.class, args);
    }

    private static void loadEnvVariables() {
        try {
            // Читаем .env файл вручную
            java.nio.file.Path envPath = java.nio.file.Paths.get(".env");
            if (java.nio.file.Files.exists(envPath)) {
                java.util.List<String> lines = java.nio.file.Files.readAllLines(envPath);
                for (String line : lines) {
                    if (!line.trim().isEmpty() && !line.startsWith("#")) {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            System.setProperty(key, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки .env: " + e.getMessage());
        }
    }
}