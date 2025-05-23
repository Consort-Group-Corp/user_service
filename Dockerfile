# Используем официальный образ OpenJDK 21
FROM openjdk:21-jdk-slim

# Устанавливаем рабочую директорию в контейнере
WORKDIR /app

# Копируем JAR файл из директории сборки в контейнер
COPY build/libs/*.jar app.jar

# Открываем порт для приложения (если приложение использует порт 8081)
EXPOSE 8081

# Указываем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
