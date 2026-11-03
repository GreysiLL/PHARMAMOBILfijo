$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
function G { & git -c "safe.directory=$($PSScriptRoot.Replace('\','/'))" @args; if ($LASTEXITCODE -ne 0) { throw 'Git falló. Revisa el mensaje anterior.' } }
if ((G branch --show-current) -ne 'feature/integrador-13') { throw 'Abre feature/integrador-13.' }
if (G diff --cached --name-only) { throw 'Hay cambios ya preparados; revísalos primero.' }
function Guardar($mensaje, $rutas) { G add -- @rutas; if (G diff --cached --name-only) { G commit -m $mensaje } }
$src='shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil'
$db='shared/src/commonMain/sqldelight/pe/edu/upeu/pharmamobil/db'
Guardar 'docs(s13): registra recorrido y evidencias de Android' @('docs/SESION13','iniciar-sesion13.ps1','registrar-sesion13.ps1')
Guardar 'feat(s13): amplía la base de datos de productos pendientes' @("$db/Producto.sq","$db/2.sqm")
Guardar 'feat(s13): refina la sincronización y muestra el estado de la cola' @("$src/data/local/ProductoLocal.kt","$src/data/repository/ProductoRepositorioOffline.kt","$src/domain/repository/ProductoRepository.kt","$src/domain/usecase/ListarProductosUseCase.kt","$src/presentation/producto/ProductoViewModel.kt","$src/presentation/producto/ProductoUiState.kt","$src/presentation/producto/ProductoScreen.kt")
Guardar 'feat(s13): gestiona el permiso de notificaciones en Android' @('androidApp/src/main/AndroidManifest.xml','shared/build.gradle.kts',"$src/domain/permiso","$src/presentation/producto/PermisoNotificaciones.kt",'shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobil/presentation/producto/PermisoNotificaciones.android.kt','shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobil/presentation/producto/PermisoNotificaciones.ios.kt')
Guardar 'test(s13): verifica migración y fallos de sincronización' @('shared/src/commonTest/kotlin/pe/edu/upeu/pharmamobil/data/repository/ProductoRepositorioOfflineTest.kt')
G status -sb
