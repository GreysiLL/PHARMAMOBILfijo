$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
Set-Location $PSScriptRoot
& .\gradlew.bat :androidApp:assembleDebug :shared:testAndroidHostTest --no-configuration-cache --console=plain '-Pkotlin.compiler.execution.strategy=in-process' '-Pkotlin.incremental=false'
if ($LASTEXITCODE -ne 0) { throw 'Fallaron la compilacion o las pruebas de S11. Comparte el primer error del registro.' }
Write-Host 'Sesion 11: APK compilado y pruebas aprobadas.'
