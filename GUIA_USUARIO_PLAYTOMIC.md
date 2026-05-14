# Guía de Usuario - Pádel&More

## 1. Qué hace la aplicación

Pádel&More es una app para gestionar la actividad de un usuario de pádel: acceso a la cuenta, perfil, comunidad, reservas, resultados, notificaciones, mensajes y otras pantallas de apoyo.

La aplicación ya trabaja con un backend propio y una base de datos MySQL. El inicio de sesión, el registro y la mayoría de acciones del usuario se guardan en el servidor, no en Firebase.

## 2. Inicio de sesión y registro

Al abrir la app, el acceso se controla con la sesión guardada localmente.

Puedes entrar de dos formas:

- Con email y contraseña.
- Con Google, si quieres usar esa opción.

Si cierras sesión, la app vuelve a la pantalla de acceso y borra la sesión local del dispositivo.

## 3. Pantalla principal

La pantalla principal organiza la app en varias secciones:

- Inicio: acceso general a la actividad y reservas.
- Comunidad: búsqueda de jugadores por nivel, ciudad y nombre.
- Perfil: datos personales y estadísticas del usuario.

En la parte superior también puedes abrir notificaciones o el menú de ajustes.

## 4. Perfil personal

Desde el perfil puedes consultar y editar tus datos básicos:

- Nombre.
- Alias, si eres jugador.
- Foto de perfil.
- Datos de pádel como nivel o ciudad.

También puedes cerrar sesión o eliminar tu cuenta.

## 5. Comunidad

La sección de comunidad permite encontrar otros usuarios y seguirlos.

Puedes filtrar por:

- Nombre.
- Ciudad.
- Nivel de pádel.

Desde cada tarjeta puedes:

- Ver el perfil público del usuario.
- Seguir o dejar de seguir.

## 6. Perfil público

El perfil público muestra la información visible de otro usuario:

- Nombre y alias.
- Nivel.
- Seguidores y seguidos.
- Partidos relacionados.
- Publicaciones asociadas.

Si no lo sigues, puedes empezar a seguirlo desde esa misma pantalla.

## 7. Reservas

La zona de reservas permite elegir:

- Club.
- Pista.
- Día.
- Hora.
- Duración.

La reserva se calcula en función de la pista y la franja horaria. Antes de confirmar, la app muestra un resumen con el precio aproximado.

## 8. Clases particulares

En clases particulares se muestra el listado disponible cargado desde el backend. Cada fila resume entrenador, horario y precio.

## 9. Torneos y sorteos

La app permite consultar torneos y sorteos disponibles.

- Los torneos se pueden listar y crear según el rol del usuario.
- Los sorteos se pueden visualizar y el usuario puede inscribirse.

## 10. Resultados y partidos

La pantalla de resultados muestra el historial de partidos y los datos básicos del enfrentamiento.

Si tienes permisos de entrenador, puedes modificar el resultado de un partido desde su detalle.

## 11. Mensajes y notificaciones

La mensajería permite consultar y enviar mensajes asociados al equipo.

Las notificaciones muestran avisos de la actividad de la app y se marcan como leídas cuando sales de la pantalla.

## 12. Datos y sincronización

Los datos de la app se guardan en el servidor mediante MySQL y la API backend.

Eso significa que:

- Los cambios se sincronizan con el servidor.
- No dependes de una base de datos local para la información principal.
- Si cambias de dispositivo, la cuenta y los datos siguen disponibles al iniciar sesión.

## 13. Notas sobre notificaciones push

La app todavía usa Firebase Messaging solo para el envío de notificaciones push.

Eso no afecta al login, al perfil ni al almacenamiento de datos principales. La información de usuario y el contenido funcional ya viven en el backend propio.

## 14. Problemas habituales

Si algo no carga correctamente:

- Comprueba que tienes sesión iniciada.
- Verifica tu conexión a internet.
- Cierra la app y vuelve a abrirla.
- Si una pantalla sigue vacía, puede ser que no haya datos creados todavía en el servidor.

## 15. Resumen rápido

- El acceso se hace con backend propio.
- Los datos principales se guardan en MySQL.
- La comunidad, el perfil, los resultados, las reservas y las notificaciones ya siguen esa arquitectura.
- Firebase solo permanece para el canal push.
