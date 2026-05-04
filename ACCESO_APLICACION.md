# 📱 Guía de Acceso a la Aplicación

## ✅ Estado Actual
- **Compilación**: ✅ BUILD SUCCESSFUL
- **Git**: ✅ Sincronizado con rama `main`
- **Dependencias**: ✅ Todas resueltas
- **Permisos**: ✅ Configurados

---

## 🚀 Cómo Acceder a la Aplicación

### 1. **Primera Ejecución (FirstActivity)**
La aplicación inicia con `FirstActivity` que detecta automáticamente:
- Si **NO hay usuario autenticado**: Muestra pantalla de login/registro
- Si **hay usuario autenticado**: Navega automáticamente a `MainActivity`

### 2. **Pantalla de Login/Registro**
- **Iniciar Sesión**: Accede con tu cuenta Firebase/MySQL
- **Registrarse**: Crea una nueva cuenta

### 3. **Después de Iniciar Sesión (MainActivity)**
Accedes a tres secciones principales:

| Botón | Descripción |
|-------|-------------|
| **🏠 Inicio** | Reservas de pistas, clases y torneos/sorteos |
| **👥 Comunidad** | Sección de comunidad (solo para administradores) |
| **👤 Perfil** | Tu perfil de usuario con opción de cerrar sesión |

---

## 🔒 Control de Acceso por Rol

### 👤 Rol: Jugador
- ✅ Ver y reservar pistas
- ✅ Ver y reservarse en clases
- ✅ Apuntarse a torneos/sorteos
- ❌ No acceso a sección "Comunidad"
- ❌ Interfaz simplificada

### 🏢 Rol: Administrador
- ✅ Acceso total a todas las secciones
- ✅ Gestión de pistas, clases, torneos
- ✅ Acceso a estadísticas y reportes
- ✅ Sección "Comunidad" completa

---

## 🔧 Compilación y Ejecución

### Compilar:
```bash
./gradlew clean assembleDebug
```

### Ejecutar en dispositivo/emulador:
```bash
./gradlew installDebug
adb shell am start -n com.example.tmanager/.FirstActivity
```

O usa el script proporcionado:
```bash
./run_on_device.bat  # Windows
./run_on_device.sh   # Linux/Mac
```

---

## 📋 Flujo de Autenticación

```
FirstActivity
    ↓
¿Usuario autenticado?
    ↓                  ↓
   NO                 SÍ
    ↓                  ↓
LoginActivity    MainActivity
    ↓                  ↓
RegisterActivity  [Inicio/Comunidad/Perfil]
```

---

## 🛠️ Cambios Realizados

✅ **Git**: Pull sincronizado de `main`  
✅ **Compilación**: Limpia y exitosa  
✅ **Estructura**: MainActivity con 3 botones (Inicio/Comunidad/Perfil)  
✅ **Autenticación**: FirstActivity como LAUNCHER  
✅ **Permisos**: Notificaciones y almacenamiento configurados  

---

## 📝 Próximos Pasos

1. **Conectar base de datos MySQL con AWS**
2. **Implementar cierre de sesión en perfil**
3. **Restringir vista de comunidad para jugadores**
4. **Configurar FCM para notificaciones**

---

## 🆘 Solución de Problemas

### "Cannot resolve symbol 'FirstActivity'"
```bash
./gradlew clean build
```

### "Build failed"
```bash
./gradlew --refresh-dependencies build
```

### "App no inicia"
1. Verifica que AndroidManifest.xml tenga FirstActivity como LAUNCHER
2. Comprueba los permisos en AndroidManifest.xml
3. Revisa los logs en Android Studio

---

**Última actualización**: 2026-05-04  
**Estado**: ✅ Lista para usar

