param([Parameter(Mandatory=$true)][string]$Repositorio)
$ErrorActionPreference = 'Stop'
$gitPractica = 'C:\Program Files\Git\cmd\git.exe'
Set-Location $PSScriptRoot
if ($Repositorio -notmatch '^https://github\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+(?:\.git)?$') { throw 'Indica el enlace HTTPS del repositorio de GitHub.' }
$ramaPractica = & $gitPractica branch --show-current
if ($ramaPractica -ne 'feature/offline-first-duran') { throw 'Debes estar en la rama feature/offline-first-duran.' }
$estadoPractica = & $gitPractica status --porcelain
if ($estadoPractica) { throw 'Hay cambios sin registrar. Completa el commit antes de subir.' }
$remotosPractica = & $gitPractica remote
if ($remotosPractica -contains 'practica11') {
    & $gitPractica remote set-url practica11 $Repositorio
} else {
    & $gitPractica remote add practica11 $Repositorio
}
if ($LASTEXITCODE -ne 0) { throw 'No se pudo configurar el repositorio de la practica 11.' }
& $gitPractica -c http.sslBackend=openssl push -u practica11 feature/offline-first-duran
if ($LASTEXITCODE -ne 0) { throw 'No se pudo subir la practica 11.' }
Write-Host 'Practica 11 subida. Revisa la rama feature/offline-first-duran en tu repositorio.'
