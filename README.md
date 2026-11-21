# Playlist Maker

Учебное Android-приложение на Kotlin и Jetpack Compose.
Текущий этап — **спринт 11**. В приложении можно сохранять обложки плейлистов. Финальное действие выбирается при сборке. Плейлисты и избранное сохраняются в Room, история поиска — в DataStore. Поиск обращается к iTunes Search API и требует интернета.

## Структура

- [docs/README.md](docs/README.md) — собранные задания спринтов 3–11.
- [figma/README.md](figma/README.md) — каталог эталонов и SVG.
- `app/src/main/` — Kotlin/Compose и Android-ресурсы текущего этапа.
- `app/src/test/` — тесты навигации, UI и рендер двух тем на Robolectric.
- [docs/verification.md](docs/verification.md) — результаты проверок и ограничения.
- [docs/git-workflow.md](docs/git-workflow.md) — цепочка веток спринтов.

## Инструменты

Android Studio Meerkat 2024.3.1 или совместимая версия, **JDK 17**,
Android SDK Platform **35**, Build Tools **35.0.0**.
Gradle **8.11.1** (Wrapper включён), AGP **8.9.1**, Kotlin **2.1.10**.
minSdk **26**, targetSdk **35**. [Совместимость AGP и Gradle](https://developer.android.com/build/releases/agp-8-9-0-release-notes).

Откройте корневую папку в Android Studio, установите Gradle JDK 17 и SDK 35,
выполните Gradle Sync. Выберите `app` и устройство/эмулятор Android 8+.
Интернет нужен для загрузки зависимостей, поиска и сетевых обложек.

Сборка из PowerShell:

```powershell
$env:JAVA_HOME = 'C:/path/to/jdk-17'
$env:ANDROID_HOME = 'C:/path/to/Android/Sdk'
./gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

Путь к SDK можно вместо переменной указать в `local.properties`:
`sdk.dir=C:/Users/USERNAME/AppData/Local/Android/Sdk`. Файл не отслеживается Git.
APK: `app/build/outputs/apk/debug/app-debug.apk`.

На Windows используйте путь к Gradle-кэшу с латинскими символами, если тестовый
процесс завершается с `GradleWorkerMain: ClassNotFoundException`:

```powershell
$env:GRADLE_USER_HOME = Join-Path $env:TEMP 'playlistmaker-gradle'
```

## Выбор финального варианта

| Значение | Действие в приложении |
|---|---|
| 1 | Удаление плейлиста корзиной в деталях или долгим нажатием на плейлист в списке |
| 2 | Удаление трека долгим нажатием на него внутри плейлиста |
| 3 | Объединение плейлистов. Удерживайте исходный плейлист и выберите целевой в нижней панели |

**Android Studio.** Откройте корневой gradle.properties, укажите одно значение (например, finalVariant=2), выполните **Sync Project with Gradle Files** и нажмите **Run** для модуля app. Чтобы установить APK вручную, выполните assembleDebug в окне Gradle или терминале. Для переключения измените значение, синхронизируйте и соберите приложение снова. Значение в репозитории по умолчанию — 1.

**PowerShell.** Из корневой папки выполните одну команду. Параметр -P переопределяет gradle.properties для текущего запуска.

```powershell
./gradlew.bat assembleDebug -PfinalVariant=1
./gradlew.bat assembleDebug -PfinalVariant=2
./gradlew.bat assembleDebug -PfinalVariant=3
```

APK записывается в app/build/outputs/apk/debug/app-debug.apk. Скопируйте его под отдельным именем, если хотите сохранить несколько сборок. Установка другой сборки поверх текущей сохраняет данные, если APK подписаны тем же ключом. Обложки и избранное доступны во всех трёх сборках.

## uv и проверки

Android собирается Gradle; `pyproject.toml` и uv управляют вспомогательными инструментами.

```powershell
uv sync
uv run android-project doctor
uv run android-project check
uv run android-project build
uv run android-project test
uv run android-project lint
uv run python tools/compare_figma.py
```

`check` проверяет ресурсы, объявления Activity и границы текущего спринта;
он не заменяет компиляцию. Android-тесты запускаются локально на Robolectric,
создают PNG в `app/build/reports/screenshots/`. Сравнение с Figma выполняется после тестов.
Используется системный шрифт. Для сравнения нужны локальные эталоны Figma, описанные в каталоге макетов.

## Адрес поддержки

Для локальной сборки с предзаполненным получателем передайте параметр:

```powershell
./gradlew.bat assembleDebug '-PplaylistMakerSupportEmail=student@example.org'
```

Укажите свой адрес вместо примера. Без параметра получатель остаётся пустым.
