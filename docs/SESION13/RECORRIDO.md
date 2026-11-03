# Sesión 13 — Integración
Tabla preliminar basada en revisión del código. El recorrido completo en el dispositivo aún requiere verificación.

| Momento | Hallazgo o comprobación pendiente | Gravedad | Decisión |
|---|---|---|---|
| Primera apertura | Existe pantalla de inicio de sesión; comprobar instalación limpia. | Alta | Verificar hoy |
| Credenciales incorrectas | Comprobar que se distingan de una caída de red. | Alta | Verificar hoy |
| Inventario vacío | Existe estado vacío; comprobar con una cuenta sin productos. | Media | Verificar hoy |
| Crear sin conexión | Existe cola local; comprobar persistencia al reiniciar. | Alta | Verificar hoy |
| Recuperar conexión | Verificar un solo envío y los contadores. | Alta | Verificar hoy |
| Validación del servidor | Confirmar que el error llega al campo. | Alta | Verificar hoy |
| Token vencido | Comprobar si se conserva el borrador. | Alta | Revisar en sesión 14 si queda pendiente |
| Cerrar sesión | Comprobar eliminación del token y aislamiento entre cuentas. | Alta | Verificar hoy |

## Evidencias verificadas en el Pixel

- [Sesión anterior vencida](sesion13-login.png): la app mostró el aviso y permitió volver a iniciar sesión.
- [Inventario local vacío](sesion13-inventario.png): sin sesión guardada, mostró un estado vacío con la indicación de conectarse.
- Notificaciones: [denegadas](s13-permiso-denegado.png), [bloqueadas con Ajustes](s13-permiso-bloqueado.png) y [concedidas](s13-permiso-concedido.png).
- [Modo avión](s13-pendientes.png): se creó S13-Evidencia-1609; el contador mostró un pendiente.
- [Reconexión](s13-reconexion.png): tras sincronizar hubo cero pendientes y el inventario conservó siete productos.
- [Pruebas automatizadas](pruebas.log): 102 pruebas aprobadas.

- [Crashlytics](crashlytics-reporte.png): la consola recibió el error recuperable «Fallo de sincronización de productos».

Aún no se documentaron las comprobaciones manuales de validación del servidor, cierre de sesión ni recepción de una notificación FCM.
