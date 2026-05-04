# ✅ APLICACIÓN LISTA PARA ACCEDER

## 📊 Resumen del Trabajo Realizado

### ✅ Estado Actual
- **Compilación**: BUILD SUCCESSFUL ✓
- **Repositorio Git**: Sincronizado con `main` ✓
- **Estructura**: MainActivity con 3 botones ✓
- **Autenticación**: FirstActivity → LoginActivity/MainActivity ✓
- **Permisos**: Notificaciones y almacenamiento configurados ✓

---

## 🚀 CÓMO INICIAR LA APLICACIÓN

### Opción 1: Desde Android Studio
1. Abre el proyecto en Android Studio
2. Click en **Run** o presiona **Shift + F10**
3. Selecciona el dispositivo/emulador
4. ¡Listo! La app inicia en FirstActivity

### Opción 2: Desde terminal
```bash
cd C:\Users\sergi\Escritorio\PADELYMORE\TManager-main\TManager-main
.\gradlew installDebug
adb shell am start -n com.example.tmanager/.FirstActivity
```

### Opción 3: Usando el script
```bash
.\run_and_open.bat
```

---

## 🎯 FLUJO DE ACCESO

### 1️⃣ FirstActivity (Pantalla Inicial)
- Verifica si hay usuario autenticado
- Si **SÍ** → Va a MainActivity
- Si **NO** → Va a LoginActivity

### 2️⃣ LoginActivity (Inicio de Sesión)
- Ingresa tus credenciales
- O ve a RegisterActivity si no tienes cuenta

### 3️⃣ MainActivity (Pantalla Principal)
Con 3 opciones principales:

| Opción | Acceso |
|--------|--------|
| **🏠 Inicio** | Reservas, clases, torneos (todos) |
| **👥 Comunidad** | Solo administradores |
| **👤 Perfil** | Tu perfil + Cerrar sesión |

---

## 🔐 ROLES Y PERMISOS

### 👤 Usuario Jugador
```
✅ Reservar pistas
✅ Apuntarse a clases
✅ Participar en torneos/sorteos
❌ NO acceso a Comunidad
❌ Interfaz restringida
```

### 🏢 Usuario Administrador
```
✅ Acceso a TODO
✅ Crear/editar pistas
✅ Gestionar clases
✅ Crear torneos
✅ Ver comunidad
```

---

## 📁 Estructura del Proyecto

```
TManager-main/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/tmanager/
│   │   │   ├── FirstActivity.java ← Punto de entrada
│   │   │   ├── MainActivity.java ← Pantalla principal
│   │   │   ├── LoginActivity.java ← Login
│   │   │   ├── RegisterActivity.java ← Registro
│   │   │   └── ... (fragmentos y utilidades)
│   │   └── res/layout/
│   │       ├── activity_first.xml
│   │       ├── activity_main.xml
│   │       └── ... (otros layouts)
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── ACCESO_APLICACION.md ← Este documento
```

---

## 🛠️ Cambios Realizados Hoy

✅ **Git Pull**: Sincronizado con `main` branch  
✅ **Compilación**: Arreglada y sin errores  
✅ **FirstActivity**: Configurada como LAUNCHER  
✅ **MainActivity**: Con 3 botones (Inicio/Comunidad/Perfil)  
✅ **Autenticación**: Automática al iniciar  
✅ **Guías**: Documentación completa agregada  

---

## 🔗 URLs Útiles

- **Repositorio**: https://github.com/Baarrooso/TFG.git
- **Rama Actual**: `feature/restrict-player-ui`
- **Rama Principal**: `main`

---

## ❓ PREGUNTAS FRECUENTES

**P: ¿Qué hago si me sale error de "Cannot resolve symbol"?**  
R: Ejecuta:
```bash
./gradlew clean build
```

**P: ¿Cómo cambio entre Jugador y Administrador?**  
R: Se detecta automáticamente desde la base de datos. Usa credenciales diferentes.

**P: ¿Puedo acceder sin internet?**  
R: No, necesita conexión a Firebase/MySQL.

**P: ¿Dónde cierro sesión?**  
R: En la pestaña "👤 Perfil" hay un botón de cerrar sesión.

---

## 📞 Próximos Pasos

1. Conectar a base de datos MySQL con AWS
2. Implementar notificaciones FCM
3. Unificar partes duplicadas del código
4. Agregar Device Manager

---

**Estado**: ✅ LISTA PARA USAR  
**Fecha**: 2026-05-04  
**Rama**: feature/restrict-player-ui  
**Compilación**: BUILD SUCCESSFUL

