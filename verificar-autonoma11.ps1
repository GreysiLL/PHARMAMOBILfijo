param([switch]$SoloSuite,[switch]$InstalacionAislada)
$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
Set-Location $PSScriptRoot
$tareasAA11 = @(':androidApp:assembleDebug', ':shared:testAndroidHostTest')
if ($SoloSuite) { $tareasAA11 += @('--tests', '*Autonoma11SuiteTest') }
if ($InstalacionAislada) { $tareasAA11 += '-Paa11Pruebas=true' }
& .\gradlew.bat @tareasAA11 --no-configuration-cache --console=plain '-Pkotlin.compiler.execution.strategy=in-process' '-Pkotlin.incremental=false'
if ($LASTEXITCODE -ne 0) { throw 'Fallaron la compilacion o las pruebas de la actividad autonoma 11.' }
Write-Host 'Actividad autonoma 11 verificada.'
