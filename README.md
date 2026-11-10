This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## Capacidades nativas

Práctica 09: [verificación actual, capturas y límites](docs/SESION09/VERIFICACION.md). Rama `feature/expect-actual-duran`.

PharmaMobil usa `expect/actual` para formatear soles y una interfaz del dominio inyectada por Koin para compartir. Android se verificó con un producto real de PharmaSoft; iOS sigue pendiente de Mac/Xcode.

| Archivo común | Android | iOS |
|---|---|---|
| [Formato.kt](shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/platform/Formato.kt) | [NumberFormat](shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobil/platform/Formato.android.kt) | [NSNumberFormatter](shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobil/platform/Formato.ios.kt) |
| [Compartidor](shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/domain/platform/Compartidor.kt) | [Intent ACTION_SEND](shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobil/platform/CompartidorAndroid.kt) | [UIActivityViewController](shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobil/platform/CompartidorIos.kt) |
| expect platformModule | [Módulo Android](shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobil/di/PlatformModule.android.kt) | [Módulo iOS](shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobil/di/PlatformModule.ios.kt) |

El [mapper de presentación](shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/presentation/producto/ProductoUi.kt) convierte el precio antes de mostrarlo. El [texto compartido](shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/domain/usecase/TextoParaCompartir.kt) incluye nombre, precio y stock. Detalle usa GET por ID del backend real.

Para probar: abrir Productos, tocar Paracetamol 500 mg y pulsar Compartir. El selector muestra `Paracetamol 500 mg — S/ 4.50 — Stock: 120`. Al cancelar se conserva el detalle.

Capturas: [listado](docs/SESION09/capturas/01-listado-actual.png), [selector Android](docs/SESION09/capturas/03-selector-actual.png) y [error original sin actual](docs/SESION09/capturas/05-error-sin-actual.png). El error es un registro histórico auténtico; la versión completa actual compila.

Interop revisada: Swift importa Shared, inicializa Koin con KoinIosKt.doInitKoinIos() y crea Compose mediante MainViewControllerKt.MainViewController(). No se acredita ejecución iOS.

Repositorio de entrega: https://github.com/GreysiLL/Activi-09-pract/tree/feature/expect-actual-duran

## Sesión 7: consumo GET con Ktor (registro histórico)

Ktor 3.6.0 y kotlinx.serialization consumen PharmaSoft con un HttpClient único inyectado por Koin.

| Plataforma | Motor | URL base |
|---|---|---|
| Emulador Android | OkHttp | http://10.0.2.2:8080/api/v1/ |
| Simulador iOS en Mac | Darwin | http://localhost:8080/api/v1/ |

Endpoint: `GET productos?pagina=0&tamanio=20`. La sesión carga la primera página (20 productos).
La respuesta se deserializa como `PaginaResponseDto<ProductoResponseDto>`: contenido, pagina, tamanio, totalElementos, totalPaginas y ultima.
ProductoResponseDto: id, nombre, precio, stock, estado, categoriaId y categoriaNombre. Se ignoran campos adicionales, como las fechas.
El mapper entrega Producto al dominio sin dependencias de Ktor ni anotaciones de serialización.

Mantener Docker (Oracle healthy) y PharmaSoft en el puerto 8080 abiertos. Ejecutar Android, entrar a Productos y comprobar los tres productos de prueba.
En Logcat filtrar `Ktor:` para ver GET y respuesta 200. Crear otro producto en Swagger y volver a entrar a Productos.
Desactivar la conexión del emulador y pulsar Reintentar para verificar el mensaje de error.
El registro desde la app sigue guardando en memoria y no aparece en el listado REST: el POST corresponde a la sesión 8.
HTTP se autoriza únicamente para los hosts locales indicados. En producción usar HTTPS.
La verificación de iOS requiere Mac y Xcode; preparar el código en Windows no acredita ejecución en iOS.

Verificación en Windows: powershell -NoProfile -ExecutionPolicy Bypass -File .\verificar-sesion07.ps1
OkHttp se fija en 5.3.2 con su BOM para mantener compileSdk 36: OkHttp 5.5 requiere SDK 37.



## Conectividad REST

Actividad autónoma S07: [catálogo, DTO y bitácora](docs/S07_AUTONOMA/actividad-autonoma.md).
Los cinco endpoints de productos se ejecutaron contra PharmaSoft; [resultados HTTP completos](docs/S07_AUTONOMA/endpoints-ejecutados.json).
Base Android: http://10.0.2.2:8080/api/v1/. Base iOS: http://localhost:8080/api/v1/ en la Mac del backend.
GET /productos devuelve página; POST devuelve 201; PUT devuelve 200; DELETE devuelve 204 y aplica baja lógica.
La Activity debug PruebasConexionActivity permite reproducir éxito, 404, desconexión, timeout de 1 ms y JSON estricto/tolerante.
Abrir después de instalar debug: `adb shell am start -n pe.edu.upeu.pharmamobil/.PruebasConexionActivity`.
Para PC-03 activar modo avión y desactivar Wi-Fi antes de ejecutar. Guardar Logcat filtrado por `Ktor:` y las capturas.
Los registros deben distinguir pruebas HTTP del backend, pruebas Ktor automatizadas y ejecución visual del emulador.
La evidencia iOS requiere una ejecución real en Mac/Xcode.

Repositorio de entrega S07 autónoma: https://github.com/GreysiLL/AA

Las cinco pruebas de conexión pasaron en Android (Pixel 9, API 36). [Bitácora real](docs/S07_AUTONOMA/bitacora-android.json), [registro Ktor](docs/S07_AUTONOMA/ktor-android.log) e [informe PDF](docs/S07_AUTONOMA/S07_ActividadAutonoma_Duran_Castro.pdf).

iOS: no ejecutado por falta de Mac/Xcode. Esta evidencia queda pendiente.


## Sesión 08 - CRUD REST

Rama `feature/crud-productos-duran`. [Implementación y verificación](docs/SESION08/VERIFICACION.md).

En Productos, Registrar guarda en PharmaSoft; Editar carga por ID y permite Guardar cambios; Eliminar pide confirmación y aplica la baja lógica del backend. El inventario muestra los productos activos de todas las páginas. Los errores del servidor se muestran bajo el campo correspondiente o en un mensaje de operación sin borrar el listado.

Oracle y PharmaSoft deben estar funcionando. Categoría por defecto: 1 (Analgésicos); editar conserva la categoría original.

Verificar en PowerShell: `powershell -NoProfile -ExecutionPolicy Bypass -File .\verificar-sesion08.ps1`.

iOS no ejecutado por falta de Mac/Xcode; no se acredita esa evidencia.


Entrega práctica S08: https://github.com/GreysiLL/act.prac-2/tree/feature/crud-productos-duran

Verificación final: 63 pruebas Android host sin fallos; CRUD real y error400 ejecutados en el emulador. [Capturas, bitácora y logs](docs/SESION08/VERIFICACION.md). Tres productos activos antes/después; producto de prueba eliminado lógicamente. Publicar la rama: `powershell -NoProfile -ExecutionPolicy Bypass -File .\subir-practica08.ps1`.


## Código específico de plataforma

Actividad autónoma 09: [inventario completo](docs/S09_AUTONOMA/inventario.md) y [comparación de 677 palabras](docs/S09_AUTONOMA/informe-comparativo.md).

- Moneda: `expect fun formatearSoles`; Android NumberFormat e iOS NSNumberFormatter.
- Información del dispositivo: `expect class InfoDispositivo`; Android Build.VERSION.RELEASE e iOS UIDevice. Visible en el menú **Acerca de**.
- Inyección y conectividad: `expect val platformModule`; Koin configura OkHttp/Context y Darwin sin Context Android.
- Navegación atrás: `expect fun AtrasDetalle`; Android BackHandler, iOS botón Volver de la pantalla común.
- Compartir: interfaz común `Compartidor` con implementaciones Intent.ACTION_SEND y UIActivityViewController, seleccionadas mediante Koin.

Todas las rutas exactas y APIs están en el inventario. commonMain no importa android. ni platform.
APK compilado y ejecutado en Pixel 9 API36: precio literal `S/ 4.50` (espacio U+00A0), selector y Android versión 16. 69 pruebas aprobadas.
Se retiró temporalmente el actual Android del formato y se registró el fallo real; después se restauró y se compiló correctamente.
iOS: código implementado, sin compilación ni capturas por falta de Mac/Xcode. Resultado literal del formato Foundation pendiente; no se afirma equivalencia verificada.

Repositorio de esta actividad: https://github.com/GreysiLL/SE3AA/tree/feature/autonoma09-duran


## Autenticación

Sesión 10: login real `POST /api/v1/auth/login` y recuperación con `GET /api/v1/auth/me`. Rama `feature/auth-jwt-duran`.

| Usuario de prueba | Contraseña | Alcance |
|---|---|---|
| admin | Admin123 | Todos los permisos |
| vendedor | Vendedor123 | Catálogo de lectura, clientes y ventas |
| almacen | Almacen123 | Lectura y escritura del catálogo |

Para ejecutar: mantener Oracle en Docker y PharmaSoft actualizado (seguridad, commit profesor `fff44f99c06d4ca0e71809b591c64435f25978d2`). Ejecutar `iniciar-pharmasoft.ps1` en la carpeta pharmaSoft; el script ahora utiliza `target/PharmaBackend-sesion10.jar`. `-Compilar` recompila desde el proyecto antes de actualizar ese JAR.
Android apunta a `http://10.0.2.2:8080/api/v1/`; simulador iOS a `http://localhost:8080/api/v1/` y requiere el backend en la Mac o ajustar la dirección.

Ktor Auth agrega Bearer automáticamente en la primera petición protegida y omite el login. Se limpia su caché al iniciar, restaurar y cerrar la sesión. El token se persiste mediante AlmacenSeguro: Android EncryptedSharedPreferences con MasterKey; iOS Keychain. No se guarda la contraseña. Las copias de seguridad Android están deshabilitadas.
Logging usa HEADERS en debug y NONE en release; Authorization aparece como `***`. El cuerpo del login no se registra, para no revelar contraseña ni token.

401 en login: credenciales incorrectas. 401 en una ruta protegida: limpia el token y vuelve al login con aviso. 403: mantiene la sesión y muestra falta de permiso. La interfaz usa los permisos del usuario actual; vendedor no tiene botones de escritura y almacén no ve Clientes. Al cerrar sesión se descartan los ViewModels y formularios del usuario anterior.
Clientes ahora consulta y registra por REST; su formulario incorpora DNI y apellidos requeridos por PharmaSoft. Pedidos conserva su pantalla pendiente de sesiones anteriores: no se implementó una interfaz de ventas en esta práctica.

Verificación automática: `powershell -NoProfile -ExecutionPolicy Bypass -File .\verificar-sesion10.ps1`.
77 pruebas aprobadas. [Evidencias de sesión 10](docs/SESION10/README.md): matriz de 23 comprobaciones de seguridad, bitácora Android, capturas y registros sin tokens completos.

La Activity de comprobaciones de seguridad existe solo en debug. Permite provocar el 403 con almacén y el cierre por 401 con un token deliberadamente inválido; esta última comprobación borra la sesión de prueba.
EncryptedSharedPreferences está deprecado en AndroidX Security 1.1.0; se conserva para cumplir la implementación indicada en la guía, sin migrar otros mecanismos durante esta práctica.
iOS: implementación de Keychain preparada; compilación y ejecución pendientes por falta de Mac/Xcode. No se presentan capturas iOS ni se afirma que esa plataforma está verificada.

Repositorio de la práctica 10: https://github.com/GreysiLL/AP10/tree/feature/auth-jwt-duran

### Comprobaciones de la API con Postman

La carpeta [postman](postman/README.md) permite importar la colección y el entorno local para comprobar PharmaSoft. Incluye los 28 endpoints, 53 solicitudes, login automático de los tres roles y un CRUD de registros exclusivos de prueba. Verificada con Newman: 50 solicitudes ejecutadas y 68 comprobaciones aprobadas, sin fallos. Ventas y creación de usuarios son opcionales y están desactivadas por defecto. Los archivos no contienen tokens reales.

## Persistencia offline-first

La sesión 11 usa SQLDelight 2.1.0 y una base local `pharma.db` como fuente de verdad del inventario. La pantalla observa un Flow; crear, editar y eliminar escriben en SQLite. Los productos nuevos reciben IDs negativos y la marca pendiente hasta sincronizar. **Sincronizar** envía primero los pendientes y después descarga el catálogo paginado. Una confirmación del servidor reemplaza el ID local por uno positivo.

La sesión previamente autenticada puede recuperarse sin red. Si el JWT está vencido, el catálogo guardado queda disponible solo para lectura; al reconectar se solicita iniciar sesión nuevamente. Los datos locales se separan por usuario. La primera autenticación y la renovación requieren conexión. Los drivers Android e iOS están registrados; iOS sigue pendiente de ejecutar en Mac/Xcode.

Ver [implementación, pruebas y evidencias de la sesión 11](docs/SESION11/README.md). Ejecutar `verificar-sesion11.ps1` para generar el APK y correr las pruebas.

## Actividad autónoma 11

La rama `feature/autonoma11-duran` agrega seis casos de prueba, cinco escenarios documentados y la fecha de la última sincronización correcta. El inventario informa si los datos son recientes, antiguos o nunca se sincronizaron. La migración de la base conserva los productos existentes.

Ver el [informe sin referencias y las evidencias](docs/AA11/README.md). Resultado verificado: 102 pruebas aprobadas, incluidas las seis de la suite autónoma. Ejecutar `verificar-autonoma11.ps1`; agregar `-SoloSuite` para ejecutar únicamente esos seis casos.

Las pruebas destructivas y las fixtures se ejecutaron en una instalación debug aislada (`-InstalacionAislada`), conservando los datos de la aplicación habitual. La ejecución y captura en iOS quedan pendientes por falta de Mac/Xcode.

## Firma Android — sesión 14

Crea un archivo local keystore.properties en la raíz con storeFile, storePassword, keyAlias y keyPassword. Guarda la clave JKS fuera del repositorio. Git excluye ese archivo y las extensiones JKS y keystore. Para compilar, ejecuta gradlew.bat :androidApp:assembleRelease :androidApp:bundleRelease. La guía de verificación se encuentra en docs/SESION14/PRUEBAS.md.
