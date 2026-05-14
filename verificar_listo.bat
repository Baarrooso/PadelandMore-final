@echo off
REM Verificador rápido de que todo está listo para ejecutar
cls
echo.
echo ════════════════════════════════════════════════════════════
echo   VERIFICACIÓN RÁPIDA - Pádel&More
echo ════════════════════════════════════════════════════════════
echo.

REM Verificar APK
echo [1/4] Verificando APK compilado...
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ✓ APK encontrado
    for %%i in (app\build\outputs\apk\debug\app-debug.apk) do (
        set size=%%~zi
        echo   Tamaño: !size! bytes
    )
) else (
    echo ✗ APK no encontrado - necesita compilación
    echo   Ejecuta: .\run_on_device.bat
    pause
    exit /b 1
)
echo.

REM Verificar Java
echo [2/4] Verificando Java...
java -version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ Java instalado
) else (
    echo ✗ Java no encontrado
    pause
    exit /b 1
)
echo.

REM Verificar ADB
echo [3/4] Verificando ADB...
adb version >nul 2>&1
if %errorlevel% equ 0 (
    echo ✓ ADB disponible
) else (
    echo ✗ ADB no encontrado
    echo   Solución: Agrega %%ANDROID_HOME%%\platform-tools al PATH
    pause
    exit /b 1
)
echo.

REM Verificar dispositivos
echo [4/4] Verificando dispositivos/emuladores...
adb devices
echo.

setlocal enabledelayedexpansion
set DEVICE_COUNT=0
for /f "tokens=*" %%A in ('adb devices ^| find "device" ^| find /v "attached"') do set /a DEVICE_COUNT+=1

if !DEVICE_COUNT! gtr 0 (
    echo ✓ Dispositivo encontrado
) else (
    echo ⚠ No hay dispositivos conectados
    echo   Abre Android Studio ^> Device Manager para iniciar un emulador
)
endlocal
echo.

echo ════════════════════════════════════════════════════════════
echo   RESULTADO: TODO ESTÁ LISTO
echo ════════════════════════════════════════════════════════════
echo.
echo Próximo paso: Ejecuta una de estas opciones:
echo.
echo   OPCIÓN 1: Abre Android Studio y presiona Shift + F10
echo   OPCIÓN 2: .\run_on_device.bat
echo   OPCIÓN 3: Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process; .\run_on_device.ps1
echo.

pause

