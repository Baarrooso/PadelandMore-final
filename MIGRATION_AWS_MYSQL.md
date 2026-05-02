# Guía de Migración: Firebase → MySQL/AWS

## Estado actual (después de unificación)

### Flujo de acceso único
- **FirstActivity**: Pantalla inicial que detecta si hay sesión activa
- **LoginActivity**: Manejo centralizado con `SessionNavigator`
- **RegisterActivity**: Registro → directo a MainActivity (sin WelcomeActivity)
- **MainActivity**: Shell principal que carga fragments según rol
- **Rol**: Campo `usuarios.rol` en Firestore → valores: `jugador`, `entrenador`, `admin`, `none`

### Utilidades centralizadas
- **SessionNavigator.java**: Centraliza toda navegación de logout/cambio de pantalla
- **UserSession.java**: Abstrae obtención de usuario actual (preparada para migración)
- **AuthUtil.java**: Lee rol de Firestore (usa colección `usuarios`)

## Pasos para migración a MySQL/AWS

### 1. Crear backend REST API (AWS)
```
POST   /api/auth/login          → Autentica usuario, devuelve token JWT
POST   /api/auth/register       → Registra nuevo usuario
POST   /api/auth/logout         → Invalida sesión
GET    /api/users/:uid          → Obtiene datos del usuario
GET    /api/users/:uid/role     → Obtiene rol del usuario
PUT    /api/users/:uid          → Actualiza datos
DELETE /api/users/:uid          → Elimina cuenta
```

### 2. Crear cliente HTTP (Retrofit o similar)
- Añadir dependencia Retrofit en `build.gradle.kts`
- Crear interfaz ApiClient con los endpoints anteriores
- Implementar manejo de JWT tokens (guardar en SharedPreferences)

### 3. Migrar LoginActivity
```java
// Cambiar de:
auth.signInWithEmailAndPassword(mail, pass)

// A:
apiClient.login(mail, pass).enqueue((response, token) -> {
    SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
    prefs.edit().putString("jwt_token", token).apply();
    startActivity(new Intent(this, MainActivity.class));
});
```

### 4. Migrar RegisterActivity
```java
// Cambiar de:
auth.createUserWithEmailAndPassword(correo, pass)

// A:
apiClient.register(nombre, correo, pass).enqueue((response) -> {
    startActivity(new Intent(this, MainActivity.class));
});
```

### 5. Actualizar UserSession.java
```java
// Cambiar de:
public static String getCurrentUid() {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    return user != null ? user.getUid() : null;
}

// A:
public static String getCurrentUid() {
    SharedPreferences prefs = context.getSharedPreferences("user_data", MODE_PRIVATE);
    return prefs.getString("uid", null);
}
```

### 6. Actualizar AuthUtil.java para consultar rol desde MySQL
```java
// Cambiar de:
FirebaseFirestore.getInstance()
    .collection("usuarios")
    .document(user.getUid())
    .get()

// A:
apiClient.getUserRole(uid).enqueue((response) -> {
    String role = response.getRole(); // De MySQL
    callback.onResult("jugador".equalsIgnoreCase(role));
});
```

### 7. Eliminar dependencias Firebase
En `app/build.gradle.kts`, remover:
```kotlin
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")
implementation("com.google.firebase:firebase-storage")
implementation("com.google.firebase:firebase-analytics")
implementation("com.google.android.gms:play-services-auth:21.5.1")
```

Mantener solo si hay push notifications:
```kotlin
implementation("com.google.firebase:firebase-messaging")
```

### 8. Añadir Retrofit para comunicación con MySQL/AWS
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
```

## Estructura de datos esperada en MySQL

### Tabla `usuarios`
```sql
CREATE TABLE usuarios (
  uid VARCHAR(255) PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  rol ENUM('jugador', 'entrenador', 'admin', 'none') DEFAULT 'none',
  equipoId VARCHAR(255),
  fotoUrl VARCHAR(255),
  edad INT,
  nivel VARCHAR(50),
  mano VARCHAR(50),
  creado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  actualizado TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## Archivos clave por área

### Autenticación
- `LoginActivity.java` → Cambiar a HTTP login
- `RegisterActivity.java` → Cambiar a HTTP register
- `SessionNavigator.java` → Mantener igual
- `UserSession.java` → Adaptar para JWT/API

### Lectura de datos de usuario
- `AuthUtil.java` → Cambiar `isJugador()` a HTTP GET
- `MiPerfilFragment.java` → Usa AuthUtil (no requiere cambios directos)
- `MiInformacionActivity.java` → Usa AuthUtil (no requiere cambios directos)

### Logout
- `SessionNavigator.java` → Mantener igual (limpia token local)
- `MiPerfilFragment.java` → Llamará a SessionNavigator.signOutToLogin() sin cambios

## Pasos de testing tras migración

1. Verificar login con usuario nuevo desde AWS/MySQL
2. Verificar rol "jugador" restringe acceso a "Comunidad"
3. Verificar rol "admin" permite ver todas las opciones
4. Verificar logout limpia JWT token y vuelve a login
5. Verificar cierre de sesión por inactividad (timeout del JWT)

## Notas importantes

- **JWT Token**: Guardar en SharedPreferences, incluir en header `Authorization: Bearer <token>`
- **Refresh Token**: Implementar rotación automática de tokens expirados
- **Caché local**: Mantener AuthUtil.isJugador() con caché local para offline (si aplica)
- **Manejo de errores**: Implementar reintentos y manejo de conexión perdida
- **Logs**: Desactivar logs de Retrofit en producción con `HttpLoggingInterceptor.Level.NONE`

