# Práctica 11 — Persistencia offline-first

Estudiante: Grease Duran Castro. Rama: `feature/offline-first-duran`.
La rama continúa el CRUD autenticado y el diseño de la sesión 10 (`e922aad`).
No se reemplazó por el `develop` anterior para no perder esas funciones.

## Implementación

- SQLDelight 2.1.0 genera `PharmaDb` desde `Producto.sq` y `SesionGuardada.sq`.
- Android utiliza `AndroidSqliteDriver`, iOS `NativeSqliteDriver`; ambos abren `pharma.db`.
- Las pruebas comunes usan una fábrica expect/actual: JDBC en el host Android y driver nativo en iOS. JDBC no se importa desde commonTest porque no es multiplataforma.
- `ProductoLocal` convierte las filas al dominio sin importar Ktor ni DTO.
- `ProductoRepositorioOffline.observar()` ofrece exclusivamente el Flow local. Obtener detalles y listar también leen SQLite.
- El ViewModel recoge el Flow una vez. Crear, editar y eliminar no recargan manualmente el listado.
- Los registros locales nuevos reciben identificadores negativos y `pendiente = 1`.
- Sincronizar primero envía pendientes, confirma cada respuesta y luego descarga todas las páginas del catálogo. Las escrituras de reemplazo se realizan en una transacción y preservan las filas pendientes.
- Cada subida confirmada reemplaza inmediatamente el ID negativo por el ID positivo del servidor. Si después falla GET, no se repite ese POST en el siguiente intento.
- Un mutex serializa las sincronizaciones y escrituras locales de este repositorio.
- Ediciones locales se envían mediante PUT. Las eliminaciones de filas positivas se conservan como pendientes inactivas hasta enviar DELETE; eliminar una fila negativa cancela su creación sin enviar un ID negativo al backend.
- Los productos están separados por usuario mediante una clave primaria compuesta. Cerrar sesión no borra trabajo pendiente de ese usuario ni lo muestra a otra cuenta.

## Autenticación al abrir sin red

El JWT continúa en el almacén cifrado de la sesión 10. La base guarda solamente usuario, nombre y permisos que llegaron en una respuesta válida del servidor. Si `auth/me` no responde por conexión o timeout, puede restaurarse esa sesión previa mientras el JWT no esté vencido. Leer `sub` y `exp` localmente no verifica la firma ni reemplaza la autorización del servidor. Una respuesta 401 nunca se oculta con la caché; borra la sesión según el flujo anterior.

La primera entrada requiere login con red. No se habilita login offline con contraseña. Si el token de una hora expiró, se requiere iniciar sesión nuevamente con el backend disponible. Clientes y reportes mantienen su funcionamiento de la sesión anterior; la persistencia de esta práctica corresponde a productos.

## Comprobación en Android

1. Encender Oracle Docker y PharmaSoft; iniciar sesión con `admin / Admin123`.
2. Abrir Productos y pulsar **Sincronizar**. Cerrar por completo la app.
3. Activar modo avión y desactivar Wi-Fi (algunos emuladores conservan Wi-Fi en modo avión). Volver a abrir: deben verse los datos guardados y el aviso sin conexión.
4. Registrar un producto de prueba. Debe aparecer desde el Flow local con **Pendiente de sincronización** y un ID negativo en SQLite.
5. Desactivar modo avión, recuperar Wi-Fi y pulsar **Sincronizar**.
6. Consultar ese producto en Swagger con autorización: debe existir con ID positivo. La marca pendiente debe desaparecer en la app.
7. Comprobar adicionalmente que se puede reabrir con PharmaSoft detenido, después de un login reciente, sin eliminar la base del emulador.

Ejecutar `verificar-sesion11.ps1` para compilar y correr las pruebas. La salida real y las capturas se incluyen a continuación.

## Resultados reales del 20 de octubre de 2026

El APK compiló con `BUILD SUCCESSFUL`. Pasaron 90 pruebas, sin fallos, errores ni omisiones; 13 son nuevas de persistencia y sesión offline. `pruebas.json` conserva el resumen y los nombres de esos casos; `compilacion-y-pruebas.log` contiene la salida real. `verificacion.json` identifica el APK mediante SHA-256.

Se creó `S11Offline-1020143628` en modo avión con Wi-Fi y datos desactivados. SQLite lo guardó con `id = -1` y `pendiente = 1`. Después de cerrar por completo y reabrir la app, el producto seguía pendiente. Al recuperar la conexión y pulsar Sincronizar, el mismo registro pasó a `id = 82` y `pendiente = 0`, sin duplicarse. GET `/api/v1/productos/82` devolvió 200 y Swagger mostró el nombre, precio 5.70 y stock 12. El producto de demostración se conserva activo en el servidor.

Después se detuvo PharmaSoft. Una consulta desde el host confirmó que el backend no respondía; se cerró y reabrió PharmaMobil con Wi-Fi y datos habilitados. La sesión previa se recuperó y el catálogo local mostró los cuatro productos con el aviso sin conexión. La captura 06 y la bitácora registran esa prueba por separado del modo avión.

La bitácora usa hora de Lima. El reloj visible del emulador tiene otra zona horaria; no se modificó para las capturas.

### Evidencias

- [01 Catálogo con red](capturas/01-con-red.png).
- [02 Arranque en modo avión](capturas/02-arranque-modo-avion.png): catálogo local y aviso sin conexión.
- [03 Creación pendiente](capturas/03-producto-pendiente.png): registro visible antes de existir en PharmaSoft.
- [04 Persistencia tras reabrir](capturas/04-pendiente-tras-reabrir.png).
- [05 Sincronización al reconectar](capturas/05-reconexion-sincronizada.png): marca pendiente eliminada.
- [06 Arranque con PharmaSoft apagado](capturas/06-backend-apagado.png): lectura local con la red del emulador habilitada.
- [07 Comprobación en Swagger](capturas/07-swagger-producto-confirmado.png): respuesta real 200 con ID 82.
- [Bitácora y filas de SQLite](bitacora-android.json).
- [Producto confirmado por la API](producto-confirmado-servidor.json).

La captura de Swagger encuadra URL y respuesta para no mostrar el JWT de la sección curl. No se guardaron tokens en estas evidencias.

[Informe de la práctica con las siete evidencias](Informe_Practica11_Grease_Duran_Castro.pdf).

## Límites

iOS queda pendiente de compilar y ejecutar en Mac/Xcode. No se atribuyen capturas Android a iOS. El backend no ofrece una clave de idempotencia: si una respuesta POST se pierde después de que el servidor haya creado el producto, el cliente no puede garantizar que un reintento no lo duplique. La confirmación por fila resuelve fallos posteriores de descarga, pero no esa ambigüedad de red. Los conflictos entre cambios de varios dispositivos no se resuelven automáticamente en esta sincronización básica.

Documentación de referencia: [SQLDelight 2.1.0](https://sqldelight.github.io/sqldelight/2.1.0/multiplatform_sqlite/) y [Flow de consultas](https://sqldelight.github.io/sqldelight/2.1.0/multiplatform_sqlite/coroutines/).
