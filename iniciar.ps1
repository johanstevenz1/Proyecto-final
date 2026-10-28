$ErrorActionPreference = 'Stop'
$proyectoJovan = Join-Path $PSScriptRoot 'backend'
Push-Location -LiteralPath $proyectoJovan
try {
    if (-not (Test-Path -LiteralPath 'target/jovan-pets-1.0.0.jar')) {
        & './mvnw.cmd' clean package
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo compilar el proyecto.' }
    }
    Write-Host 'Abre http://localhost:8080 cuando el servidor termine de iniciar.'
    & java -jar 'target/jovan-pets-1.0.0.jar'
} finally { Pop-Location }
