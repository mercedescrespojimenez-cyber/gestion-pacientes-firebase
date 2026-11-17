# Gestión de pacientes con Firebase Realtime Database

Ampliación del proyecto de gestión de pacientes del Tema 5 de *Aplicaciones para Sistemas de Información Sanitaria y Dispositivos Móviles* (Máster en Ingeniería Biomédica y Salud Digital, Universidad de Sevilla).

El proyecto original, de J. A. Álvarez García y A. Jiménez Ramírez, permitía crear un paciente aleatorio y buscarlo por NUHSA. Es decir, solo cubría el alta y la lectura del CRUD. Esta versión completa el CRUD y añade las mejoras que proponen las propias diapositivas (5.4.4): listado con `RecyclerView` y `ChildEventListener`, modificación y borrado.

Autora de la ampliación: Mercedes Crespo.

## Funcionalidades añadidas

1. **Alta mediante formulario.**
   - Campos: NUHSA, nombre, apellidos, grupo sanguíneo (en un `Spinner`) y fecha de nacimiento (con `DatePickerDialog`, como en el apartado 5.3).
   - Validación de cada campo con el mensaje de error junto al campo.
   - Comprueba que el NUHSA no exista ya antes de guardar.
2. **Listado en tiempo real.** `RecyclerView` alimentado por un `ChildEventListener`: la lista se actualiza sola cuando se añade, modifica o borra un paciente, también desde otro dispositivo. Muestra un contador de pacientes.
3. **Filtro por grupo sanguíneo** en el listado.
4. **Modificación de pacientes (Update)** desde la ficha, con el formulario relleno. El NUHSA no se puede cambiar, porque es la clave en Firebase.
5. **Borrado de pacientes (Delete)** desde la ficha, con diálogo de confirmación.
6. **Ficha ampliada.**
   - Muestra también el NUHSA y la fecha de nacimiento.
   - Se actualiza en tiempo real.
   - Avisa si el paciente se borra desde otro dispositivo.
7. **Correcciones del proyecto original.**
   - El botón «Paciente aleatorio» ya no sobrescribe a otro paciente si el número aleatorio se repite.
   - La búsqueda valida el NUHSA antes de abrir la ficha.
   - La ficha deja de escuchar a Firebase cuando no está visible (`onStart`/`onStop`).

## Modelo de datos

Cada paciente se guarda en dos rutas, como en el original:

```
Pacientes/{nuhsa}                    → ficha completa
PacientesPorGrupo/{grupo}/{nuhsa}    → copia para filtrar por grupo sanguíneo
```

Las dos copias se escriben y se borran a la vez con `updateChildren`, que aplica todos los cambios de forma atómica, en la clase `PacienteRepositorio`. Si se cambia el grupo sanguíneo de un paciente, se borra la copia del grupo antiguo.

## Cómo ejecutarlo

1. **Abrir el proyecto.** Abre la carpeta en Android Studio (*File → Open*) y deja que sincronice Gradle.
2. **Crear el proyecto de Firebase.** En la [consola de Firebase](https://console.firebase.google.com), crea un proyecto y añade una app Android con el paquete `com.example.conexionrealtimedatabase`.
3. **Añadir la configuración.** Descarga `google-services.json` y cópialo en la carpeta `app/`.
4. **Crear la base de datos.** En *Realtime Database*, crea la base de datos y pega en la pestaña *Reglas* el contenido de `database.rules.json`.
5. **Ejecutar.** Lanza la app en un emulador (AVD) o en un móvil.

## Cambios técnicos respecto al original

- **Versiones:** Gradle 8.9, Android Gradle Plugin 8.7.3, `compileSdk 35` y `targetSdk 34`, para que el proyecto abra en el Android Studio actual. El original usaba Gradle 7.0.2, que no funciona con el JDK 17/21 que trae Android Studio.
- **Firebase:** se gestiona con BoM (`firebase-bom`), y se eliminan `jcenter()` y Jetifier.
- **Espacio de nombres:** se declara con `namespace` en `app/build.gradle`, en lugar de con el atributo `package` del manifest.
- **Clase `Paciente`:** incluye setters, para que Firebase pueda reconstruir el objeto.
- **Validaciones:** están en la clase `Validador`, sin dependencias de Android, con pruebas unitarias en `ValidadorTest` (*Run 'ValidadorTest'*).

## Seguridad y datos

Los datos de la app son **ficticios**. Las reglas de `database.rules.json` validan la estructura de cada paciente, pero siguen permitiendo leer y escribir sin autenticación. Con datos reales de salud haría falta añadir Firebase Authentication y restringir el acceso por usuario. El RGPD trata los datos de salud como categoría especial.
