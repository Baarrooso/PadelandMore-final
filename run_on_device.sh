#!/bin/bash
# Script para ejecutar Pádel&More en dispositivo/emulador (Linux/Mac)

set -e

echo ""
echo "========================================"
echo " Pádel&More - Device Manager Launcher"
echo "========================================"
echo ""

# Verificar si adb está disponible
if ! command -v adb &> /dev/null; then
    echo "Error: ADB no está disponible."
    echo "Asegúrate de que el Android SDK esté instalado y ADB esté en el PATH."
    exit 1
fi

# Mostrar dispositivos conectados
echo "Dispositivos conectados:"
adb devices
echo ""

# Obtener número de dispositivos conectados
DEVICE_COUNT=$(adb devices | grep -c device || echo 0)
if [ $DEVICE_COUNT -lt 2 ]; then
    echo "Error: No hay dispositivos conectados o emulador ejecutándose."
    echo "Por favor:"
    echo "  1. Conecta un dispositivo Android, o"
    echo "  2. Inicia un emulador desde Android Studio > Device Manager"
    exit 1
fi

# Compilar la app en modo debug
echo "Compilando aplicación..."
./gradlew assembleDebug

echo ""
echo "✓ Compilación exitosa"
echo ""

# Verificar si el APK existe
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "Error: APK no encontrado en $APK_PATH"
    exit 1
fi

# Instalar la app
echo "Instalando APK en el dispositivo..."
adb install -r "$APK_PATH"

echo ""
echo "✓ Instalación exitosa"
echo ""

# Iniciar la app
echo "Iniciando aplicación..."
adb shell am start -n com.example.tmanager/.FirstActivity

echo ""
echo "========================================"
echo " ✓ Aplicación iniciada correctamente!"
echo "========================================"
echo ""

