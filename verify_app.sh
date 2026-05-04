#!/bin/bash

# Script de verificación rápida de la aplicación
# Verifica que todo esté listo para ejecutar

echo "=========================================="
echo "  VERIFICACIÓN DE LA APLICACIÓN"
echo "=========================================="
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Verificar Git
echo -n "[1/5] Verificando Git... "
if cd "$(dirname "$0")" && git status > /dev/null 2>&1; then
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${RED}✗ ERROR${NC}"
fi

# 2. Verificar si gradle existe
echo -n "[2/5] Verificando Gradle... "
if [ -f "gradlew" ]; then
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${RED}✗ ERROR - gradlew no encontrado${NC}"
fi

# 3. Verificar Android SDK
echo -n "[3/5] Verificando Android SDK... "
if [ -n "$ANDROID_SDK_ROOT" ]; then
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${YELLOW}⚠ ADVERTENCIA - ANDROID_SDK_ROOT no configurado${NC}"
fi

# 4. Verificar build.gradle.kts
echo -n "[4/5] Verificando build.gradle.kts... "
if grep -q "minSdk" "app/build.gradle.kts"; then
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${RED}✗ ERROR${NC}"
fi

# 5. Verificar AndroidManifest.xml
echo -n "[5/5] Verificando AndroidManifest.xml... "
if grep -q "FirstActivity" "app/src/main/AndroidManifest.xml"; then
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${RED}✗ ERROR - FirstActivity no encontrada${NC}"
fi

echo ""
echo "=========================================="
echo "  COMANDOS ÚTILES"
echo "=========================================="
echo ""
echo "Compilar:"
echo "  ./gradlew clean build"
echo ""
echo "Ejecutar en dispositivo:"
echo "  ./gradlew installDebug"
echo "  adb shell am start -n com.example.tmanager/.FirstActivity"
echo ""
echo "Ver logs:"
echo "  adb logcat"
echo ""
echo "=========================================="

