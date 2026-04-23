# Script para ejecutar TManager en dispositivo/emulador
# Requiere: Android SDK, ADB y un dispositivo conectado o emulador ejecutándose

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║     TManager - Device Manager Launcher                    ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Función para verificar si adb está disponible
function Test-AdbAvailable {
    try {
        adb version | Out-Null
        return $true
    }
    catch {
        return $false
    }
}

# Función para liberar procesos bloqueados
function Kill-BlockedProcesses {
    Write-Host "Limpiando procesos bloqueados..." -ForegroundColor Yellow
    Get-Process | Where-Object { $_.ProcessName -like "*java*" -or $_.ProcessName -like "*gradle*" } | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Write-Host "✓ Procesos limpiados" -ForegroundColor Green
}

# Función para limpiar la carpeta build
function Clean-BuildFolder {
    $buildPath = "app\build"
    if (Test-Path $buildPath) {
        Write-Host "Limpiando carpeta build anterior..." -ForegroundColor Yellow
        Remove-Item -Path $buildPath -Recurse -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 1
        Write-Host "✓ Carpeta build limpiada" -ForegroundColor Green
    }
}

# Verificar si ADB está disponible
if (-not (Test-AdbAvailable)) {
    Write-Host "❌ Error: ADB no está disponible." -ForegroundColor Red
    Write-Host "Solución: Configura ANDROID_HOME en el PATH del sistema" -ForegroundColor Yellow
    exit 1
}

# Mostrar dispositivos conectados
Write-Host "Buscando dispositivos conectados..." -ForegroundColor Yellow
adb devices
Write-Host ""

# Obtener número de dispositivos conectados
$deviceCount = (adb devices | Select-Object -Skip 1 | Where-Object { $_ -match '\tdevice' }).Count
if ($deviceCount -eq 0) {
    Write-Host "❌ Error: No hay dispositivos conectados o emulador ejecutándose." -ForegroundColor Red
    Write-Host ""
    Write-Host "Soluciones:" -ForegroundColor Yellow
    Write-Host "  1. Abre Android Studio > Tools > Device Manager" -ForegroundColor Cyan
    Write-Host "  2. Inicia un emulador (botón play ▶️)" -ForegroundColor Cyan
    Write-Host "  3. O conecta un dispositivo Android por USB con USB Debugging activado" -ForegroundColor Cyan
    Write-Host ""
    exit 1
}

Write-Host "✓ Dispositivos encontrados: $deviceCount" -ForegroundColor Green
Write-Host ""

# Matar procesos bloqueados
Kill-BlockedProcesses
Write-Host ""

# Limpiar build
Clean-BuildFolder
Write-Host ""

# Compilar la app en modo debug
Write-Host "Compilando aplicación..." -ForegroundColor Yellow
& ".\gradlew.bat" assembleDebug
Write-Host ""

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error durante la compilación." -ForegroundColor Red
    Write-Host "Intenta ejecutar:" -ForegroundColor Yellow
    Write-Host "  .\gradlew clean assembleDebug" -ForegroundColor Cyan
    exit 1
}

Write-Host "✓ Compilación exitosa" -ForegroundColor Green
Write-Host ""

# Verificar si el APK existe
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ Error: APK no encontrado en $apkPath" -ForegroundColor Red
    exit 1
}

$apkSize = (Get-Item $apkPath).Length / 1MB
Write-Host "✓ APK encontrado (Tamaño: $([Math]::Round($apkSize, 2)) MB)" -ForegroundColor Green
Write-Host ""

# Instalar la app
Write-Host "Instalando APK en el dispositivo..." -ForegroundColor Yellow
adb install -r $apkPath
Write-Host ""

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error durante la instalación del APK." -ForegroundColor Red
    exit 1
}

Write-Host "✓ Instalación exitosa" -ForegroundColor Green
Write-Host ""

# Iniciar la app
Write-Host "Iniciando aplicación..." -ForegroundColor Yellow
adb shell am start -n com.example.tmanager/.FirstActivity
Write-Host ""

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  ✓ ¡Aplicación iniciada correctamente!                    ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "💡 Tip: Usa 'adb logcat | findstr tmanager' para ver los logs" -ForegroundColor Yellow
Write-Host ""

