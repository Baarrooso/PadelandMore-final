@echo off
REM Script para ejecutar Pádel&More en dispositivo/emulador
REM Limpia procesos bloqueados, compila e instala la app

setlocal enabledelayedexpansion

cls
echo.
echo ════════════════════════════════════════════════════════════
echo   Pádel&More - Device Manager Launcher
echo ════════════════════════════════════════════════════════════
echo.

REM Matar procesos Java/Gradle bloqueados
echo Limpiando procesos bloqueados...
taskkill /F /IM java.exe >nul 2>&1
taskkill /F /IM javaw.exe >nul 2>&1
timeout /t 2 /nobreak >nul
echo [OK] Procesos limpiados
echo.

REM Verificar si ADB está disponible
adb version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] ADB no disponible
    echo Solución: Agrega %%ANDROID_HOME%%\platform-tools al PATH
    pause
    exit /b 1
)

REM Verificar si hay dispositivos conectados
echo Buscando dispositivos conectados...
adb devices
echo.

for /f "tokens=*" %%A in ('adb devices ^| find "device" ^| find /v "attached"') do set DEVICE_COUNT=%%A
if "!DEVICE_COUNT!"=="" (
    echo [ERROR] No hay dispositivos conectados
    echo.
    echo Soluciones:
    echo - Abre Android Studio ^> Tools ^> Device Manager
    echo - Inicia un emulador (botón play)
    echo - O conecta un dispositivo Android con USB Debugging activado
    echo.
    pause
    exit /b 1
)

echo [OK] Dispositivo encontrado
echo.

REM Limpiar build anterior
echo Limpiando carpeta build anterior...
if exist "app\build\" (
    rmdir /s /q "app\build" >nul 2>&1
    timeout /t 1 /nobreak >nul
)
echo [OK] Build limpiado
echo.

REM Compilar la app en modo debug
echo Compilando aplicación...
call gradlew.bat assembleDebug

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Error durante la compilación
    echo Intenta: .\gradlew clean assembleDebug
    echo.
    pause
    exit /b 1
)

echo.
echo [OK] Compilación exitosa
echo.

REM Verificar si el APK existe
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk

if not exist "!APK_PATH!" (
    echo [ERROR] APK no encontrado en !APK_PATH!
    pause
    exit /b 1
)

echo [OK] APK encontrado
echo.

REM Instalar la app
echo Instalando APK en el dispositivo...
adb install -r "!APK_PATH!"

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Error durante la instalación del APK
    pause
    exit /b 1
)

echo.
echo [OK] Instalación exitosa
echo.

REM Iniciar la app
echo Iniciando aplicación...
adb shell am start -n com.example.tmanager/.FirstActivity

echo.
echo ════════════════════════════════════════════════════════════
echo   [OK] ¡Aplicación iniciada correctamente!
echo ════════════════════════════════════════════════════════════
echo.
echo Tip: Usa 'adb logcat | findstr tmanager' para ver los logs
echo.

pause
endlocal

