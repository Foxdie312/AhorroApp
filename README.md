# AhorroMeta 💰

App Android (Kotlin + Jetpack Compose) para gestionar finanzas personales y ahorrar
con un propósito concreto: el usuario crea **metas de ahorro** (p. ej. "Viaje a Japón: 2000 €"),
registra depósitos y retiradas, y ve el progreso hacia su objetivo.

## Funciones
- Crear metas de ahorro con cantidad objetivo y fecha límite opcional.
- Barra de progreso por meta y total ahorrado global.
- Depósitos y retiradas por meta, con nota y fecha.
- Movimientos generales (ingresos/gastos) fuera de metas.
- Historial editable (eliminar movimiento revierte el saldo).
- Datos guardados en el dispositivo (SharedPreferences + JSON, sin permisos).

## Cómo compilar el APK
1. Instala [Android Studio](https://developer.android.com/studio) (incluye SDK y Gradle).
2. Abre esta carpeta como proyecto (`File > Open`).
3. Deja que Gradle sincronice (descarga dependencias automáticamente).
4. APK de prueba: `Build > Build App Bundle(s) / APK(s) > Build APK(s)`.
5. APK firmado para publicar: `Build > Generate Signed App Bundle / APK`.

Desde terminal: `./gradlew assembleDebug` → APK en `app/build/outputs/apk/debug/`.

Requisitos: JDK 17, SDK 34, minSdk 24 (Android 7.0+).
