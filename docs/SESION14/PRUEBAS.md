# Pruebas de la sesión 14

APK y AAB Android release generados con R8 activo. Primera compilación: cae8834. Segunda compilación (red Wi-Fi): 02f928e; consultar la constancia en Entrega_Sesion14. Versión 1.0.0 (1); paquete pe.edu.upeu.pharmamobil. Titular: Grease Duran Castro. SHA-256 del certificado: 5640fa6d6e96a45ce83e23e4ee3385d727877e7ebf0f7eeadfd1441e5320beff. APK verificado con apksigner; AAB verificado con jarsigner.

| Equipo | Instalación | Recorrido |
|---|---|---|
| Pixel 9 emulado, Android 16 | Android rechazó reemplazar debug con release por certificado distinto | No ejecutado; se conservó la app anterior y sus datos |
| Samsung Galaxy Z Flip (SM-F731B), Android 17 | APK firmado instalado por USB; arranque correcto | Login correcto, 9 productos sincronizados, detalle con precio y stock |
| Teléfono físico 2 | No conectado | Pendiente |
| iOS | No generado | Requiere Mac, Xcode y equipo de firma |

S14-01 (bloquea el recorrido): instalar el APK firmado sobre la instalación debug del mismo paquete produce INSTALL_FAILED_UPDATE_INCOMPATIBLE. En un teléfono sin PharmaMobil instalada se puede probar una instalación limpia. No se borraron los datos del emulador.

S14-02: el release se configuró para PharmaSoft en 10.40.29.75:8080. Desde el PC y el emulador el endpoint respondió HTTP 401 sin credenciales, lo que confirma acceso de red. La conexión desde un teléfono real sigue sin verificarse. Si la IP cambia, actualizar pharmaHost y el dominio de network_security_config.xml del release, y volver a compilar.

R8 completó la compilación con avisos sobre metadatos Kotlin. En el Galaxy Z Flip se verificaron el arranque, la autenticación, la descarga del inventario y el detalle con R8 activo. El recorrido observado se detalla abajo. La instalación autónoma sin USB no se verificó. No se creó la etiqueta final de entrega.

## Recorrido del Galaxy Z Flip — Android 17

| Paso | Resultado observado | Evidencia |
|---|---|---|
| Instalación y arranque | APK firmado instalado por ADB desde el archivo; abrió PharmaMobil. | [Captura](capturas/s14-flip-instalacion.png) |
| Inicio de sesión | Cuenta de prueba: 9 productos al entrar. Contraseña incorrecta: la app mostró el mensaje correspondiente. | [Inventario](capturas/s14-flip-inventario.png), [rechazo](capturas/s14-flip-login-invalido.png) |
| CRUD y validación | Alta de S14-Flip-Prueba, lectura, edición del ID 122 y eliminación del registro de prueba. Precio 0.001 rechazado con mensaje bajo Precio. | [Detalle](capturas/s14-flip-detalle.png), [edición](capturas/s14-flip-edicion.png), [eliminación](capturas/s14-flip-eliminacion.png), [validación](capturas/s14-flip-validacion.png) |
| Privilegios | VENDEDOR consultó el catálogo sin controles de creación, edición o eliminación. | [Captura](capturas/s14-flip-vendedor.png) |
| Sin red | Se creó un producto en modo avión con Wi-Fi apagada; la cola mostró 1 pendiente. | [Captura](capturas/s14-flip-offline.png) |
| Reconexión | Se restauró la conexión y se sincronizó: 0 pendientes, 10 productos y el registro recibió ID 122. | [Captura](capturas/s14-flip-reconexion.png) |
| Notificaciones | Concesión en el diálogo real. Denegado y bloqueado preparados por ADB; la app ofreció volver a solicitar o abrir Ajustes, respectivamente. Se restauró la concesión. | [Concedido](capturas/s14-flip-permiso.png), [denegado](capturas/s14-flip-denegado.png), [bloqueado](capturas/s14-flip-bloqueado.png) |
| Cierre y reapertura | Tras cerrar sesión y reiniciar el proceso, volvió al formulario de acceso. | [Captura](capturas/s14-flip-cierre.png) |
