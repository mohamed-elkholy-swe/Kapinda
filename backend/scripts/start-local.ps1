$ErrorActionPreference = "Stop"

# Import into this process so Maven inherits the current values without an IDE restart.
& (Join-Path $PSScriptRoot "import-env-to-windows.ps1") -SessionOnly
Push-Location (Join-Path $PSScriptRoot "..")
try {
  Write-Host "Starting backend with the local database and Firebase settings."
  & mvn.cmd spring-boot:run
  $backendExitCode = $LASTEXITCODE
} finally {
  Pop-Location
}
if ($backendExitCode -ne 0) { throw "Backend stopped with exit code $backendExitCode. See the backend log above." }
