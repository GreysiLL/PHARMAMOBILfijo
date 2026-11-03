# Firebase — sesión 13

La variante Android con configuración incluye Crashlytics y Messaging 2.7.0 de GitLive. La sincronización registra errores no fatales mediante el puerto común Telemetria. El adaptador usa un mensaje fijo y el código HTTP; no registra el token de sesión ni el contenido de productos.

El archivo androidApp/google-services.json se excluye de Git. Con él se activan Google Services y Crashlytics; sin él se compila con un adaptador local sin envío de datos.

La compilación con GitLive está en gitlive-build.log. La recepción de un reporte en consola y la entrega de una notificación FCM son verificaciones independientes de la compilación.
