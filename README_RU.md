# DuoChat — Android мессенджер

Это стартовый проект Android-приложения на Kotlin + Jetpack Compose + Firebase.
В нём есть регистрация/вход и текстовый чат в реальном времени.

## 1. Что понадобится
- Android Studio
- Интернет
- Аккаунт Google для Firebase

## 2. Создай Firebase-проект
1. Открой https://console.firebase.google.com/
2. Создай проект DuoChat.
3. Добавь Android-приложение с package name: `com.example.duochat`
4. Скачай `google-services.json`.
5. Положи файл в папку `DuoChat/app/` (рядом с `build.gradle.kts`).

## 3. Включи сервисы Firebase
1. Authentication → Sign-in method → включи Email/Password.
2. Firestore Database → Create database.
3. В Rules вставь содержимое файла `firestore.rules`, но сначала замени
   `PASTE_FIRST_USER_UID_HERE` и `PASTE_SECOND_USER_UID_HERE` на UID двух аккаунтов.

## 4. Как получить UID двух пользователей
1. Запусти приложение и зарегистрируй первый аккаунт.
2. В Firebase Console → Authentication → Users скопируй его User UID.
3. Повтори со вторым аккаунтом на втором телефоне.
4. Вставь оба UID в `firestore.rules` и опубликуй правила.

Важно: пока не добавлены оба UID, чат не будет доступен. Не оставляй правила открытыми для всех пользователей.

## 5. Открой и запусти
1. Распакуй архив.
2. Открой папку `DuoChat` в Android Studio.
3. Дождись Gradle Sync.
4. Подключи Android-телефон с включённой USB-отладкой или запусти эмулятор.
5. Нажми Run.

## Что пока не добавлено
Это первая рабочая основа: текстовые сообщения и вход. Фото, голосовые сообщения,
push-уведомления, статусы «в сети» и шифрование end-to-end пока не реализованы.
Для публикации приложения и реального использования нужно отдельно настроить
Firebase и проверить правила доступа.


## Сборка APK без Android Studio (через GitHub)
1. Создай репозиторий на https://github.com/ и загрузи в него все файлы проекта.
2. В Firebase Console зарегистрируй Android-приложение с package name `com.example.duochat`.
3. Скачай `google-services.json` и добавь его в папку `app/` в GitHub.
4. В Firebase включи Email/Password Authentication и создай Firestore Database.
5. Открой вкладку Actions в репозитории. Если GitHub попросит разрешить запуск workflows — подтверди.
6. Выбери workflow **Build DuoChat APK** и нажми **Run workflow** (или просто отправь изменения в ветку `main`).
7. Когда сборка закончится успешно, открой её запуск, найди раздел **Artifacts** и скачай `DuoChat-debug-apk`.
8. Распакуй скачанный архив на телефоне и установи `app-debug.apk`.

Сборка выполняется на серверах GitHub. Android Studio на телефоне или компьютере не нужна.
Для публикации приложения и защиты чата всё равно нужно корректно настроить Firebase и правила Firestore.
