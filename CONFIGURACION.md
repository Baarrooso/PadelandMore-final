# Guía de Configuración - PadelManager Android Studio

## Cambios Realizados ✅

Se han realizados los siguientes cambios para que el proyecto se abra correctamente en Android Studio y en Device Manager:

### 1. **Actualización de build.gradle.kts (app)**
   - ✅ Actualizado `targetSdk` de 34 a 35 (coincide con `compileSdk`)
   - ✅ Actualizado Firebase BOM de 33.1.2 a 34.11.0
   - ✅ Actualizado Google Play Services Auth de 21.2.0 a 21.5.1

### 2. **Optimización de gradle.properties**
   - ✅ Habilitado compilación paralela (`org.gradle.parallel=true`)
   - ✅ Habilitado compilación incremental (`android.incremental=true`)
   - ✅ Habilitado daemon de Gradle (`org.gradle.daemon=true`)

### 3. **Configuración de Android Studio**
   - ✅ Creado archivo `.idea/runConfigurations.xml`
   - ✅ SDK configurado en `local.properties`: `C:\Users\adri_\AppData\Local\Android\Sdk`

## Cómo abrir el proyecto en Android Studio

1. **Abre Android Studio**
2. **Selecciona**: File → Open
3. **Navega a**: `C:\Users\adri_\Downloads\TManager-main\PadelManager_Entrega\PadelManager`
4. **Sincroniza Gradle**: Android Studio sincronizará automáticamente

## Ejecutar en Device Manager

1. **Abre el Device Manager** (Tools → Device Manager)
2. **Crea un emulador** o **conecta un dispositivo físico**
3. **Ejecuta la app**: 
   - Presiona `Shift + F10` o
   - Click en "Run 'app'" en el botón verde de ejecución

## Requisitos Mínimos

- ✅ Android SDK API Level 35 instalado
- ✅ Java JDK 11 o superior (preferentemente jbr-21)
- ✅ Gradle 8.5+ (se descarga automáticamente)
- ✅ Firebase configurado (google-services.json presente)

## Troubleshooting

### Si aparece error de SDK:
- Verifica que `local.properties` tenga: `sdk.dir=C:\\Users\\adri_\\AppData\\Local\\Android\\Sdk`
- Si no existe la carpeta, descarga el SDK desde Android Studio

### Si aparece error de Gradle:
- Click en "Sync Now" en la notificación azul
- O ejecuta: `./gradlew clean build` en PowerShell

### Si el emulador no aparece en Device Manager:
- Abre Android Studio
- Ve a: Tools → Device Manager
- Crea un nuevo virtual device con API Level 24 o superior

## Próximos Pasos

El proyecto está listo para:
- ✅ Compilar exitosamente
- ✅ Ejecutarse en emulador
- ✅ Ejecutarse en dispositivo físico
- ✅ Debuggear con logcat

¡El proyecto debería compilar y ejecutarse sin problemas ahora! 🎉

