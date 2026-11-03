$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
function G { & git -c "safe.directory=$($PSScriptRoot.Replace('\','/'))" @args; if($LASTEXITCODE -ne 0){throw 'Git falló.'} }
if((G branch --show-current) -ne 'feature/integrador-13'){throw 'Selecciona feature/integrador-13.'}
if(G diff --cached --name-only){throw 'Revisa primero los cambios que ya están preparados.'}
$shared='shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil'
G add -- .gitignore androidApp/build.gradle.kts androidApp/src/main/kotlin/pe/edu/upeu/pharmamobil/MainApplication.kt androidApp/src/conFirebase androidApp/src/sinFirebase "$shared/domain/port" "$shared/di/AppModule.kt" "$shared/data/repository/ProductoRepositorioOffline.kt" docs/SESION13 registrar-firebase13.ps1
G commit -m 'feat(s13): integra Crashlytics y Messaging con GitLive'
G status -sb
