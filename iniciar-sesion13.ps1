$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
function G { & git -c "safe.directory=$($PSScriptRoot.Replace('\','/'))" @args; if ($LASTEXITCODE -ne 0) { throw 'Git falló.' } }
if (G branch --list feature/integrador-13) { G switch feature/integrador-13; exit }
if ((G branch --show-current) -ne 'feature/autonoma11-duran') { throw 'Ejecuta desde la rama base feature/autonoma11-duran.' }
G merge-base --is-ancestor develop HEAD
G branch -f develop HEAD
G switch -c feature/integrador-13 develop
