# Guía: Ejecutar TManager en Device Manager

## Pasos para ejecutar la aplicación en un dispositivo/emulador

### 1. **Opción A: Usar el script automático (Recomendado)**

#### En PowerShell:
```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\run_on_device.ps1
```

#### En CMD:
```cmd
run_on_device.bat
```

**El script hará automáticamente:**
- ✓ Verificar si hay dispositivos conectados
- ✓ Compilar la aplicación
- ✓ Instalar el APK
- ✓ Iniciar la app

---

### 2. **Opción B: Manual (Paso a paso)**

#### Paso 1: Compilar la app
```bash
.\gradlew assembleDebug
```

#### Paso 2: Verificar dispositivos conectados
```bash
adb devices
```

Deberías ver algo como:
```
List of attached devices
emulator-5554          device
```

#### Paso 3: Instalar el APK
```bash
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

#### Paso 4: Iniciar la aplicación
```bash
adb shell am start -n com.example.tmanager/.FirstActivity
```

---

### 3. **Usar Android Studio (Opción más fácil)**

1. Abre el proyecto en **Android Studio**
2. Selecciona **Run > Select Device** (Ctrl+Alt+Shift+D)
3. En la ventana del **Device Manager**:
   - Si quieres usar un emulador: Click en ▶️ (botón play) de un emulador
   - Si quieres usar un dispositivo físico: Conecta el USB y aparecerá en la lista
4. Una vez seleccione el dispositivo, presiona **Run** (Shift+F10)

---

## Requisitos Previos

Asegúrate de tener instalado:

1. **Android SDK Platform 35** (o superior)
2. **Android SDK Build Tools 35.x**
3. **Android Emulator** (o un dispositivo físico con USB debugging activado)
4. **Java 11 o superior** (el proyecto requiere JDK 11)

### Verificar instalación:
```bash
# Verificar Android SDK
echo %ANDROID_HOME%

# Verificar ADB
adb --version

# Verificar Java
java -version
```

---

## Solución de problemas

### "Error: ADB no está disponible"
**Solución:**
1. Asegúrate de que Android SDK esté instalado
2. Añade `%ANDROID_HOME%\platform-tools` al PATH del sistema
3. Reinicia la terminal/PowerShell

### "No hay dispositivos conectados"
**Solución:**
1. **Para emulador:**
   - Abre Android Studio > Device Manager
   - Haz click en ▶️ para iniciar un emulador
   
2. **Para dispositivo físico:**
   - Conecta el dispositivo por USB
   - Activa "USB Debugging" en Developer Options
   - Ejecuta: `adb devices` (debe aparecer tu dispositivo)

### "Error durante la compilación"
**Solución:**
1. Ejecuta: `.\gradlew clean`
2. Vuelve a intentar: `.\gradlew assembleDebug`

### "Package com.example.tmanager no se encuentra"
**Solución:**
- Ejecuta: `adb shell pm list packages | findstr tmanager`
- Si no aparece, instala manualmente con el script

---

## Información útil

### Debugging
```bash
# Ver logs en tiempo real
adb logcat

# Ver logs de solo la app
adb logcat | grep "tmanager\|ActivityManager"

# Limpiar logcat
adb logcat -c
```

### Desinstalar la app
```bash
adb uninstall com.example.tmanager
```

### Ver información del dispositivo
```bash
adb shell getprop ro.build.version.release    # Versión Android
adb shell wm size                             # Resolución de pantalla
```

---

## Próximos pasos

Una vez que la app esté en ejecución:

1. **En FirstActivity:**
   - Presiona "Iniciar sesión" o "Registrarse"

2. **Authentication con Firebase:**
   - La app usa Firebase para autenticación
   - Asegúrate de tener internet en el dispositivo

3. **Debugging en Android Studio:**
   - Abre Logcat para ver los logs
   - Usa breakpoints para debuggear

¡Éxito! 🚀

