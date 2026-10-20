$ErrorActionPreference = 'Stop'
$gitAA11 = 'C:\Program Files\Git\cmd\git.exe'
Set-Location $PSScriptRoot
if ((& $gitAA11 branch --show-current) -ne 'feature/autonoma11-duran') { throw 'Cambia a feature/autonoma11-duran antes de subir la autonoma.' }
if ((& $gitAA11 remote get-url practica11) -ne 'https://github.com/GreysiLL/AP11.git') { throw 'El repositorio no corresponde a GreysiLL/AP11.' }
if (& $gitAA11 status --porcelain) { throw 'Hay cambios sin registrar. Completa el commit antes de subir.' }
& $gitAA11 -c http.sslBackend=openssl push -u practica11 feature/autonoma11-duran
if ($LASTEXITCODE -ne 0) { throw 'No se pudo subir la actividad autonoma 11.' }
Write-Host 'Autonoma subida a https://github.com/GreysiLL/AP11/tree/feature/autonoma11-duran'
