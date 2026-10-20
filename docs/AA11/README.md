# Actividad autónoma 11

Grease Duran Castro. Rama `feature/autonoma11-duran`.
Entrega: https://github.com/GreysiLL/AP11/tree/feature/autonoma11-duran

## Documento de entrega

[S11_ActividadAutonoma_DuranCastro.pdf](S11_ActividadAutonoma_DuranCastro.pdf) reúne los cuatro productos de la guía: suite de seis pruebas, cinco escenarios, antigüedad de los datos y guion de ocho minutos. Incluye el código de la suite y su salida completa como anexos.

## Pruebas

`Autonoma11SuiteTest` está en commonTest y utiliza una base por caso y MockEngine. Los seis métodos T01 a T06 pasan. T04 aísla la fase de descarga usando la API simulada y la transacción local; no invierte el orden de sincronizar para provocar una bajada con pendientes. También pasa la prueba de migración 1 a 2, conservando productos existentes. Total: 102 pruebas aprobadas, cero fallos, errores u omisiones.

Para ejecutar todo: `powershell -NoProfile -ExecutionPolicy Bypass -File .\verificar-autonoma11.ps1`.
Solo los seis casos: agregar `-SoloSuite`.
`salida-completa-suite.log` y `suite-seis-pruebas.xml` contienen la ejecución específica; `compilacion-y-todas-las-pruebas.log` y `pruebas-completas.json`, la regresión completa.

## Escenarios y fixtures

Las pruebas de borrado se hicieron únicamente en el paquete de depuración aislado `pe.edu.upeu.pharmamobil.aa11`, construido con `-Paa11Pruebas=true`. La instalación habitual conserva sus datos. La herramienta debug solo permite las fixtures en ese paquete y no se incluye en release.

`escenarios.json` registra fecha de Lima, plataforma, pasos, resultado, estado local, captura y conclusión. `bitacora-android.json` conserva las observaciones de ejecución. El reloj visible del emulador usa otra zona horaria.

El escenario E04 se comprueba entrando a Ver catálogo sin conexión desde el login tras borrar datos. No se ofrece un catálogo ajeno a una cuenta no autenticada: se muestra el estado vacío y Nunca sincronizado. Antes del primer login, SQLite aún no había abierto el archivo por su inicialización diferida.

En E05 se usa un JWT de prueba firmado con la clave de desarrollo local y vencido una hora antes. `fixture-token-vencido.json` registra su rechazo 401; no contiene el token ni la clave. La sesión previamente validada abre sin red en modo lectura. Al reconectar y pulsar Sincronizar, el servidor responde 401 y vuelve al login conservando SQLite. Después se inició sesión correctamente.

Para evidenciar más de un día de antigüedad se aplicó una marca de dos días mediante una fixture explícita de debug en la base aislada. La captura proviene de la app real; no se editó la imagen ni se afirma que se esperó dos días. Después de un login y sincronización correctos la marca vuelve a ser actual.

## Implementación y límites

`Sincronizacion.sq` guarda el instante por usuario. La migración `1.sqm` incorpora la tabla sin borrar productos de la versión anterior. La marca se escribe dentro de la transacción de descarga solo después de completar la sincronización. El ViewModel la observa y la pantalla la expresa en minutos, horas o días, con el caso Nunca sincronizado. Una solicitud fallida no modifica la marca.

iOS tiene driver y código común preparados, pero no se ejecutó por falta de una Mac con Xcode. El tramo de evidencia multiplataforma del guion señala esta limitación y no equivale a una validación iOS. El guion está preparado para ocho minutos; el ensayo oral corresponde al estudiante.

Se conserva el límite de la sincronización básica: el backend no ofrece idempotencia de POST ni resolución de conflictos entre dispositivos.
