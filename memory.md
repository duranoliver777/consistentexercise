# Memory.md

Contexto importante del proyecto, para recordar la idea y las decisiones a lo largo del tiempo.

## Idea del proyecto
- Nombre: `consistent exercise`.
- Objetivo: app Android de ejercicio para fomentar la **constancia** (consistencia) al entrenar.
- Estado actual: dos pantallas. `BienvenidaScreen` (mensaje "Bienvenido" + botón "Continuar") y `TemporizadorScreen` (reloj temporizador con tiempo configurable y botones de inicio/detener y reiniciar). "Continuar" navega al temporizador.

## Stack y configuración
- Kotlin + Jetpack Compose (Material 3), un solo módulo `:app`.
- AGP 9.4.1, Kotlin 2.2.10, Gradle wrapper 9.6.0, Java 11, `minSdk 28`, `compileSdk`/`targetSdk 37`.
- Paquete base: `com.example.consistentexercise`.
- Git: repositorio local inicializado con commit inicial. Conexión a remoto de GitHub en proceso.

## Decisiones y notas
- Estilo de borde reutilizable extraído a `ui/PantallaConBorde.kt` (composables `PantallaConBorde` y `BordeDifuminado`, más los colores `AzulClaro` y `AzulBoton`). Ambas pantallas usan `PantallaConBorde` y por tanto comparten fondo blanco y borde.
- `MainActivity.kt` contiene la Activity y `AppConsistentExercise`, host de navegación (enum `Pantalla`, sin librería de navegación). También aloja el estado del temporizador, sus efectos (conteo y alarma) y el diálogo "Tiempo agotado", para que sigan activos fuera de la pantalla del temporizador.
- `BienvenidaScreen.kt` y `TemporizadorScreen.kt` viven en el paquete raíz `com.example.consistentexercise`.
- `ConfiguracionScreen.kt` (también en el paquete raíz) es una pantalla placeholder ("Configuración" + "Próximamente") con flecha de volver arriba a la izquierda; su contenido se definirá más adelante.
- Navegación: `MainActivity.kt` usa un enum `Pantalla { BIENVENIDA, TEMPORIZADOR, CONFIGURACION }` con `rememberSaveable`, un `BackHandler` (Configuración → pantalla previa; otras → Bienvenida/salir) y un `rememberSaveableStateHolder` con `SaveableStateProvider(pantalla)`.
- Estado del temporizador en `TemporizadorEstado.kt` (paquete raíz): clase con `mutableStateOf` (`minutos`, `segundos`, `restante`, `corriendo`, `tiempoAgotado`, `conteo`, `finEnMs`), un `listSaver` y `recordarTemporizadorEstado()`. Se crea en `AppConsistentExercise` (fuera del `SaveableStateProvider`) y se pasa a `TemporizadorScreen`, que queda como UI casi sin estado.
- Botón de configuración: `IconButton` con `Icons.Filled.Settings` en la parte superior derecha de `BienvenidaScreen` y `TemporizadorScreen` (`align(Alignment.TopEnd)` + `padding(top = 20.dp, end = 20.dp)` + `size(56.dp)`, ícono de `28.dp`), para no solaparse con el borde. Los íconos requirieron añadir `androidx.compose.material:material-icons-core` (gestionado por el BOM) en `gradle/libs.versions.toml` y `app/build.gradle.kts`, porque Material 3 no lo trae de forma transitiva.
- Botones de configuración y volver: tamaño 15% mayor que el `IconButton` por defecto (`IconButton` de 56dp e ícono de 28dp, frente a 48dp/24dp originales).
- El color azul claro del borde es `AzulClaro = Color(0xFF81D4FA)`; el efecto difuminado se logra con `Modifier.blur(6.dp)` sobre una capa de borde de 2.dp (el blur solo se aplica en API 31+).
- Fondo blanco (`Color.White`) en la pantalla de bienvenida.
- El botón "Continuar" usa `AzulBoton = Color(0xFF4FC3F7)` (un azul algo más oscuro que el borde, sin gran contraste) y mide 50% del ancho y 10% del alto de la pantalla (`fillMaxWidth(0.5f)` + `fillMaxHeight(0.1f)`); su texto va a 22sp (ajustado por el usuario) y en cursiva (`FontStyle.Italic`).
- Texto "Bienvenido" en negro (`Color.Black`), negrita (`FontWeight.Bold`, 700) y con fuente caligráfica: `Dancing Script` (Google Fonts, licencia OFL) en `app/src/main/res/font/dancing_script.ttf`, aplicada con `FontFamily(Font(R.font.dancing_script, weight = FontWeight.Bold))`. Se eligió esta fuente como alternativa a "Gentle Calligraphy" (no disponible como fuente estándar).
- Borde exterior: además del borde difuminado, `BordeDifuminado` dibuja un marco sólido de 6.dp de `AzulClaro` en el perímetro exterior (`border(6.dp, ...)`), cubriendo todo el alrededor externo del borde actual.
- `TemporizadorScreen`: muestra el conteo atrás en `mm:ss`; el tiempo se ajusta con botones `-`/`+` para minutos y segundos (máx. 99 y 59); botón que alterna "Iniciar"/"Detener" y botón "Reiniciar" que vuelve al tiempo configurado. El conteo se calcula por hora de reloj: al iniciar se fija `finEnMs = SystemClock.elapsedRealtime() + total*1000` y un `LaunchedEffect` en `AppConsistentExercise` recalcula `restante = ceil((finEnMs - now)/1000)` cada 200 ms. Así el tiempo no se detiene al salir de la pantalla (por ejemplo, a Configuración).
- Al llegar a 0: `tiempoAgotado` pone en marcha un sonido tipo despertador y muestra un `AlertDialog` "Tiempo agotado" con botón "Aceptar", ambos en `AppConsistentExercise` (`EfectosTemporizador`), por lo que suenan/aparecen aunque no estés en la pantalla del temporizador. El sonido se sintetiza con `AudioTrack` (PCM 16-bit mono a 44100 Hz, `USAGE_ALARM`): `generarSonidoDespertador()` crea un buffer monótono a **1300 Hz** con patrón de 3 bips (50 ms cada uno) separados por 50 ms y una inactividad de 900 ms (ciclo 1100 ms), con envolvente para evitar clics; se reproduce en bucle (`MODE_STATIC` + `setLoopPoints`) dentro de un `LaunchedEffect`. Al pulsar "Aceptar" (o descartar) el efecto se cancela, se hace `stop()` y `release()` del track.
- Sección de conteo en la parte inferior de `TemporizadorScreen` (`SeccionTally`): muestra tally marks (grupos de 5: 4 líneas + diagonal) dibujados con `Canvas` en `GrupoTally`, con botones `-`/`+` y texto "Conteo: N". Layout del temporizador con `Column` que reserva `weight(1f)` para el reloj y deja el conteo abajo; usa `FlowRow` (API experimental) para que los grupos salten de línea.
- Paleta unificada en ambas pantallas: textos en negro (`Color.Black`), botones rellenos `AzulBoton` con texto blanco, `OutlinedButton` "Reiniciar" con borde `AzulClaro` y texto negro.

## Pendientes / próximos pasos
- (pendiente) Definir funcionalidades de dominio (registro de entrenamientos, rachas, recordatorios, etc.).
- (pendiente) Apartado de configuración, con estos grupos:
  - Temporizador/ejercicio: duración por defecto, intervalos (duración/pausa), sonido de alarma on/off y tono, vibración al terminar, auto-iniciar al entrar.
  - Constancia: meta diaria/semanal, día de inicio de semana para rachas, recordatorio de objetivo al abrir.
  - Notificaciones: recordatorios on/off, hora del recordatorio diario, días activos.
  - Apariencia: tema claro/oscuro/sistema, color dinámico on/off, color de acento (azul actual vs. otros).
  - Datos: exportar/importar historial (JSON/CSV), borrar datos/estadísticas con confirmación.
  - Otros: "Acerca de" (versión, licencias), idioma.
