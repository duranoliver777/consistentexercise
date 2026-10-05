# AGENTS.md

App Android recién generada con "Empty Activity" (`consistent exercise`). Un solo módulo `:app`, Kotlin + Jetpack Compose (Material 3). Sin repo git, sin README, sin CI.

## Idioma
- Escribe siempre las instrucciones, comentarios de archivos de configuración y demás documentación en **español**.

## Memoria del proyecto
- Guarda en `memory.md` los datos importantes del contexto (idea del proyecto, decisiones, pendientes); sirve para recordar la idea del proyecto más adelante.

## Toolchain
- Gradle wrapper 9.6.0; usa siempre `./gradlew` (no asumas un `gradle` del sistema).
- AGP 9.4.1, Kotlin 2.2.10, Java 11 source/target, `minSdk 28`, `compileSdk`/`targetSdk 37`.
- Las versiones viven en `gradle/libs.versions.toml`; añade dependencias ahí, no inline.

## Rarezas del DSL de AGP 9
Los archivos de build usan sintaxis nueva de AGP 9. No los "corrijas" a formas antiguas:
- `compileSdk { version = release(37) }` (no `compileSdk = 37`).
- El shrink de release usa `optimization { enable = true; packageScope = setOf(...) }`, no `isMinifyEnabled`/`proguardFiles`.
- Las reglas keep de R8 van en `app/src/main/keepRules/*.keep` (no `proguard-rules.pro`).

## Comandos
- Build: `./gradlew assembleDebug`
- Tests unitarios (host): `./gradlew test`
- Un solo test unitario: `./gradlew :app:testDebugUnitTest --tests "com.example.consistentexercise.ExampleUnitTest"`
- Tests instrumentados (requieren emulador/dispositivo): `./gradlew connectedAndroidTest`
- Lint: `./gradlew lint`
- No hay formateador configurado. El estilo Kotlin es `kotlin.code.style=official` (definido en `gradle.properties`).

## Estructura
- Punto de entrada: `app/src/main/java/com/example/consistentexercise/MainActivity.kt`. Toda la UI es Compose; `ui/theme/` contiene el tema Material 3 (color dinámico activado en API 31+).
- `local.properties` contiene la ruta del SDK específica de la máquina (`sdk.dir`); está en gitignore -> nunca lo commitees.

## Convenciones
- No añadas comentarios de código a menos que se pidan (los archivos existentes solo llevan los comentarios de plantilla generados).
- La app sigue siendo código de plantilla (composable `Greeting`); todavía no hay lógica de dominio.
