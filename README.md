# Pádel&More (Android)

Proyecto Android Studio basado en el antiguo flujo de futbol, adaptado a padel con un MVP funcional.

## Funciones incluidas

- Reserva de pistas (`ReservasFragment`)
- Publicar y ver torneos (`TorneosFragment`)
- Inscripcion basica a torneos
- Perfil de jugador de padel con nivel y ciudad (`PadelNivelActivity`)
- Busqueda de jugadores de nivel similar (`MatchNivelFragment`)
- Navegacion principal actualizada en `MainActivity`

## Estructura nueva (MVP)

- `app/src/main/java/com/example/tmanager/ReservasFragment.java`
- `app/src/main/java/com/example/tmanager/TorneosFragment.java`
- `app/src/main/java/com/example/tmanager/MatchNivelFragment.java`
- `app/src/main/java/com/example/tmanager/PadelNivelActivity.java`
- `app/src/main/res/layout/fragment_reservas.xml`
- `app/src/main/res/layout/fragment_torneos.xml`
- `app/src/main/res/layout/fragment_match_nivel.xml`
- `app/src/main/res/layout/activity_padel_nivel.xml`
- `app/src/main/res/layout/dialog_reserva_pista.xml`
- `app/src/main/res/layout/dialog_torneo_padel.xml`

## Colecciones Firestore usadas

- `usuarios`
  - campos nuevos: `nivelPadel` (number), `ciudadPadel` (string)
- `reservas_padel`
  - `userUid`, `club`, `pista`, `fecha`, `hora`
- `torneos_padel`
  - `nombre`, `ciudad`, `fecha`, `nivel`, `inscritos`
- `inscripciones_torneo_padel`
  - `userUid`, `torneoResumen`

## Como abrir y ejecutar

1. Abre la carpeta raiz del proyecto en Android Studio.
2. Revisa `local.properties` para que `sdk.dir` apunte a tu Android SDK.
3. Sincroniza Gradle.
4. Ejecuta en emulador/dispositivo.

## Nota importante

Desde terminal local no se pudo completar el build por configuracion de SDK (`sdk.dir` invalido en `local.properties`). En Android Studio, corrigiendo ese path, deberia compilar con normalidad.

