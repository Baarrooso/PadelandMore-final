@REM Script rápido de verificación de requisitos
@echo off
setlocal enabledelayedexpansion

cls
echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║   Pádel&More - Verificador de Requisitos y Setup          ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM Verificar Java
echo [*] Verificando Java...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Java instalado
    for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /r "version"') do echo   %%i
) else (
    echo ✗ Java NO encontrado - Requiere JDK 11 o superior
)
echo.

REM Verificar Android SDK
echo [*] Verificando Android SDK...
if defined ANDROID_HOME (
    echo ✓ ANDROID_HOME definido: %ANDROID_HOME%
) else (
    echo ✗ ANDROID_HOME NO está configurado
    echo   Solución: Configura ANDROID_HOME en variables de entorno
)
echo.

REM Verificar ADB
echo [*] Verificando ADB...
adb version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ ADB disponible
    adb version | findstr "Android Debug Bridge"
) else (
    echo ✗ ADB NO encontrado - Agrega %%ANDROID_HOME%%\platform-tools al PATH
)
echo.

REM Verificar dispositivos
echo [*] Buscando dispositivos/emuladores...
set DEVICE_COUNT=0
for /f "tokens=*" %%i in ('adb devices 2^>^&1 ^| findstr /r "device$"') do (
    set /a DEVICE_COUNT+=1
    echo ✓ Dispositivo encontrado: %%i
)

if %DEVICE_COUNT% equ 0 (
    echo ⚠ No hay dispositivos conectados
    echo   Soluciones:
    echo   • Inicia un emulador desde Android Studio ^> Device Manager
    echo   • O conecta un dispositivo Android por USB y activa USB Debugging
) else (
    echo ✓ Total de dispositivos: %DEVICE_COUNT%
)
echo.

REM Verificar Gradle
echo [*] Verificando Gradle...
if exist "gradlew.bat" (
    echo ✓ Gradle Wrapper encontrado
    for /f "tokens=*" %%i in ('.\gradlew.bat -v 2^>^&1 ^| findstr "Gradle"') do echo   %%i
) else (
    echo ✗ Gradle Wrapper NO encontrado
)
echo.

REM Verificar APK
echo [*] Verificando APK compilado...
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ✓ APK compilado encontrado
    for %%i in (app\build\outputs\apk\debug\app-debug.apk) do (
        echo   Tamaño: %%~zi bytes
        echo   Ruta: app\build\outputs\apk\debug\app-debug.apk
    )
) else (
    echo ⚠ APK no compilado aún
    echo   Ejecuta: .\gradlew assembleDebug
)
echo.

REM Resumen
echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║                       RESUMEN                               ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
echo Para ejecutar la app:
echo.
echo 1. Desde Android Studio (Recomendado):
echo    • Abre el proyecto
echo    • Presiona Shift + Ctrl + Q para abrir Device Manager
echo    • Inicia un emulador o conecta un dispositivo
echo    • Presiona Shift + F10 para ejecutar
echo.
echo 2. Desde línea de comandos:
echo    • .\run_on_device.ps1  (PowerShell)
echo    • run_on_device.bat    (CMD)
echo.
echo 3. Manual:
echo    • .\gradlew assembleDebug
echo    • adb install -r app\build\outputs\apk\debug\app-debug.apk
echo    • adb shell am start -n com.example.tmanager/.FirstActivity
echo.
echo ═════════════════════════════════════════════════════════════
echo.

pause
endlocal

