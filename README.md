# Digital Mindfulness Platform (Android + Kotlin Backend)

Клиент-серверная система формирования цифровой осознанности: Android-клиент перехватывает момент импульсивного запуска выбранных приложений, применяет поведенческую интервенцию и формирует агрегированные метрики, а backend на Ktor хранит и синхронизирует данные.

## Что реализовано в репозитории

### 1) Core mechanics (`:core`)
Чистый Kotlin-модуль с доменной логикой:
- доменные модели: `TriggerRule`, `LaunchAttempt`, `MindfulSession`, `ProgressSnapshot`;
- Rules Engine с адаптивной эскалацией soft→strict при признаках импульсивности;
- расчёт прогресса: очки дисциплины, streak, mindful launch rate, impulse cancel rate;
- whitelist-поддержка для критичных приложений.

### 2) Backend (`:backend`, Ktor)
Рабочий серверный контур:
- `GET /health`;
- `POST /api/v1/users/{userId}/rules`;
- `POST /api/v1/users/{userId}/sessions`;
- `POST /api/v1/users/{userId}/decide`;
- `GET /api/v1/users/{userId}/progress`;
- JSON serialization и тесты через `testApplication`.

### 3) Android client scaffold (`android-client/`)
Клиент на Kotlin + Jetpack Compose:
- `MainActivity` и Compose-экран барьера;
- `MindfulViewModel`, использующий общий `MindfulnessEngine`;
- базовый сценарий интервенции (cancel / continue);
- контракт `LaunchMonitor` для интеграции platform-specific foreground detection.

> В CI этого окружения автоматически собираются и тестируются JVM-модули `:core` и `:backend`.
> Android-клиент дан как готовая архитектурная и кодовая база для открытия в Android Studio и подключения AGP/SDK-пайплайна.

## Архитектура

- **Core mechanics** — независимое доменное ядро (правила, решения, сессии, метрики).
- **Android client** — UI/UX слой и platform adapters (детект foreground app + показ барьера).
- **Backend** — хранение, синхронизация и API выдачи прогресса.
- **Analytics/Social (расширение)** — групповые сценарии, лидерборды, отчёты по периодам.

## API (текущая реализация)

### POST `/api/v1/users/{userId}/rules`
```json
{
  "packageName": "com.social.app",
  "mode": "SOFT",
  "bypassLimitPerDay": 2
}
```

### POST `/api/v1/users/{userId}/sessions`
```json
{
  "packageName": "com.social.app",
  "mode": "SOFT",
  "outcome": "CANCELLED"
}
```


### POST `/api/v1/users/{userId}/decide`
```json
{
  "packageName": "com.social.app",
  "unlockedRecently": true
}
```

Response:
```json
{
  "shouldIntervene": true,
  "effectiveMode": "STRICT",
  "reason": "adaptive_escalation"
}
```

### GET `/api/v1/users/{userId}/progress`
```json
{
  "disciplinePoints": 5,
  "streak": 1,
  "mindfulLaunchRate": 1.0,
  "impulseCancelRate": 1.0
}
```

## Запуск и тесты

### Требования
- JDK 17 (рекомендовано для этой сборки).

### Тесты
```bash
./gradlew test
```

### Запуск backend
```bash
./gradlew :backend:run
```
Сервер стартует на `0.0.0.0:8080`.


### Важно про Gradle Wrapper в этом репозитории
В репозитории **не хранится** `gradle/wrapper/gradle-wrapper.jar`, потому что в целевом PR-пайплайне бинарные файлы отклоняются.

Если нужно запустить wrapper локально, восстановите jar командой:
```bash
gradle wrapper
```
После этого можно использовать привычные команды:
```bash
./gradlew test
```

## Дальнейшие шаги

1. Подключить полноценный Android Gradle проект (AGP) с runtime permissions (Usage Stats / overlay strategy).
2. Добавить persistent storage backend (PostgreSQL) и репозитории вместо in-memory.
3. Реализовать sync протокол дневных/недельных агрегатов.
4. Добавить social контур: группы, weekly challenges, leaderboard по стабильности.
5. Вынести API contracts в shared DTO-модуль для строгой типизации client↔server.
