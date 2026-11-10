# Sesión 14 — cierre de alcance

Base probada: sesión 13, commit 7363593. No se agregan funciones ni se cambia el esquema local.

| Punto | Clasificación | Evidencia o límite |
|---|---|---|
| Sincronización y permiso de notificaciones | Cerrado | Capturas y pruebas en docs/SESION13. |
| Rechazo de validación y privilegios | Cerrado | HTTP 400/403 y pantallas en el PDF de evidencias. |
| Cierre de sesión | Cerrado | Se comprobó ausencia del token persistido y conservación del inventario. |
| Entrega de notificaciones FCM | Declarado | SDK instalado; entrega de notificación no verificada. |
| Firma y ejecución iOS | Declarado | No se dispone de dispositivo iOS; no se generó ni se validó el build iOS. |
| Pruebas del release en dos teléfonos | Declarado | Galaxy Z Flip probado. No se dispone de un segundo Android físico; el emulador no se contabiliza. |

Rama local preparada desde la versión publicada de la sesión 13; la integración en develop todavía debe verificarse.
