# Resumen de unificación completada

## ✅ Cambios realizados

### 1. Flujo único de acceso
- **Eliminada** la parada en `WelcomeActivity` tras registro
- `RegisterActivity` → directamente a `MainActivity`
- Solo **un punto de entrada**: `FirstActivity` (detecta sesión existente)
- Flujo uniforme: `LoginActivity` → `RegisterActivity` → `MainActivity` (según rol)

### 2. Centralización de navegación (nuevo `SessionNavigator.java`)
Todos estos métodos **repetidos** en diferentes actividades fueron consolidados:
- `cerrarSesion()` (estaba en `MiPerfilFragment`, `MiInformacionActivity`)
- `irALoginLimpio()` (estaba en `MiInformacionActivity`)
- `finishAffinity()` + Intent flags (estaban en `ExpulsadoActivity`, `LoginActivity`)

**Métodos centralizados en `SessionNavigator`:**
- `goToLoginClean()` → Redirige a login limpiando pila
- `signOutToLogin()` → Logout + redirige a login
- `signOutGoogleAndGoTo()` → Logout de Google + redirige donde sea necesario
- `goToClean()` → Redirige limpio a cualquier actividad

**Usados en:**
- `MiPerfilFragment.cerrarSesion()` ✅
- `MiInformacionActivity.cerrarSesion()` ✅
- `MiInformacionActivity.confirmarEliminarCuenta()` ✅
- `ExpulsadoActivity` ✅
- `RegisterActivity.irALogin()` ✅
- `LoginActivity` (Google no registrado) ✅

### 3. Normalización de esquema (`AuthUtil.java`)
- **Antes**: `collection("users")` 
- **Ahora**: `collection("usuarios")`
- Ahora **consistente** con `RegisterActivity`, `LoginActivity` y resto del app
- Único vocabulario de colecciones en Firestore

### 4. Capa de sesión preparada para migración (`UserSession.java`)
Fachada que abstrae:
- `getCurrentUid()` → UID del usuario (cambiará a JWT en AWS/MySQL)
- `getCurrentName()` → Nombre del usuario
- `getCurrentEmail()` → Email del usuario
- `isUserJugador()` → Consulta rol (cambiará a API MySQL)
- `isLoggedIn()` → Verifica si hay sesión

**Ventaja**: No hay que tocar UI/Fragments en migración a MySQL, solo estos métodos.

### 5. Flujo de control de acceso según rol
**Sigue igual (sin cambios)**, pero ahora más consistente:
- `AuthUtil.isJugador()` → Determina permisos en runtime
- `MainActivity` restringe `btnComunidad` si `isJugador == true`
- `TorneosFragment` oculta botón crear si `isJugador == true`

---

## 📊 Duplicación eliminada

| Antes | Después | Lugar |
|-------|---------|-------|
| 6 formas distintas de logout | 1 método unificado | `SessionNavigator` |
| Colecciones `users` y `usuarios` | Solo `usuarios` | `AuthUtil` + Rest of app |
| `irALoginLimpio()` en 2 archivos | 1 método reutilizable | `SessionNavigator.goToLoginClean()` |
| Intent flags repetidas en 4+ sitios | 1 helper | `SessionNavigator.goToClean()` |

---

## 🔧 Cómo se ve el flujo ahora

```
FirstActivity (splash/check sesión)
    ↓
    ├─→ Si hay sesión: MainActivity (ver rol → cargar fragments)
    └─→ Si no hay sesión: mostrar LoginActivity
        ├─ Login manual → comprobarEquipoYRedirigir() → MainActivity
        ├─ Login Google ok → MainActivity
        ├─ Login Google sin registro → SessionNavigator.signOutGoogleAndGoTo(..., RegisterActivity)
        └─ "Ir a registro" → SessionNavigator.goToLoginClean()

    En MainActivity:
        ├─ Si isJugador:
        │   ├─ ReservasFragment (OK)
        │   ├─ TorneosFragment (OK, sin botón crear)
        │   ├─ SorteosFragment (OK)
        │   ├─ ClasesParticularesFragment (OK)
        │   └─ btnComunidad (BLOQUEADO)
        └─ Si admin/entrenador:
            └─ Todas las opciones disponibles

    En MiPerfilFragment:
        └─ Botón "Cerrar sesión" → SessionNavigator.signOutToLogin() → LoginActivity
```

---

## 🚀 Preparado para migración a MySQL/AWS

Los siguientes archivos están listos para adaptarse **SIN cambiar UI**:
- `SessionNavigator.java` → Mantener igual
- `UserSession.java` → Cambiar métodos que lean de JWT/API
- `AuthUtil.java` → Cambiar `isJugador()` a HTTP GET

Ver `MIGRATION_AWS_MYSQL.md` para pasos detallados.

---

## ⚠️ Advertencias de lint que siguen (no bloquean)

- `LoginActivity`: Google Sign-In deprecated (es de Google, no nuestro)
- Métodos sin usar en `UserSession` (están preparados para migración)

Son lint warnings, **no errores de compilación**. Se pueden silenciar con `@Deprecated` o `@SuppressWarnings` en AWS/MySQL si deseamos.


