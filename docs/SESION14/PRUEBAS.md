# Pruebas de la sesión 14

APK y AAB Android release generados con R8 activo. Primera compilación: cae8834. Segunda compilación (red Wi-Fi): 02f928e; consultar la constancia en Entrega_Sesion14. Versión 1.0.0 (1); paquete pe.edu.upeu.pharmamobil. Titular: Grease Duran Castro. SHA-256 del certificado: 5640fa6d6e96a45ce83e23e4ee3385d727877e7ebf0f7eeadfd1441e5320beff. APK verificado con apksigner; AAB verificado con jarsigner.

| Equipo | Instalación | Recorrido |
|---|---|---|
| Pixel 9 emulado, Android 16 | Android rechazó reemplazar debug con release por certificado distinto | No ejecutado; se conservó la app anterior y sus datos |
| Teléfono físico 1 | No conectado | Pendiente |
| Teléfono físico 2 | No conectado | Pendiente |
| iOS | No generado | Requiere Mac, Xcode y equipo de firma |

S14-01 (bloquea el recorrido): instalar el APK firmado sobre la instalación debug del mismo paquete produce INSTALL_FAILED_UPDATE_INCOMPATIBLE. En un teléfono sin PharmaMobil instalada se puede probar una instalación limpia. No se borraron los datos del emulador.

S14-02: el release se configuró para PharmaSoft en 10.40.29.75:8080. Desde el PC y el emulador el endpoint respondió HTTP 401 sin credenciales, lo que confirma acceso de red. La conexión desde un teléfono real sigue sin verificarse. Si la IP cambia, actualizar pharmaHost y el dominio de network_security_config.xml del release, y volver a compilar.

R8 completó la compilación con avisos sobre metadatos Kotlin. No se ha comprobado aún el comportamiento en ejecución del release. No se creó la etiqueta final de entrega.
