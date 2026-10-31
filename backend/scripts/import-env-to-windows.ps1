param([switch]$SessionOnly)

$ErrorActionPreference = "Stop"

$envFile = Join-Path $PSScriptRoot "..\.env"
if (-not (Test-Path -LiteralPath $envFile)) {
  throw "The source .env file was not found: $envFile"
}

$source = @{}
foreach ($line in Get-Content -LiteralPath $envFile) {
  $trimmed = $line.Trim()
  if (-not $trimmed -or $trimmed.StartsWith("#")) { continue }
  $separator = $trimmed.IndexOf("=")
  if ($separator -lt 1) { continue }
  $name = $trimmed.Substring(0, $separator).Trim()
  $value = $trimmed.Substring($separator + 1).Trim()
  if ($value.Length -ge 2 -and (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'")))) {
    $value = $value.Substring(1, $value.Length - 2)
  }
  $source[$name] = $value
}

$mapping = [ordered]@{
  FIREBASE_PROJECT_ID       = "EXPO_PUBLIC_FIREBASE_PROJECT_ID"
  FIREBASE_CREDENTIALS_PATH = "FIREBASE_CREDENTIALS_PATH"
  DELIVERY_ADMIN_EMAILS     = "DELIVERY_ADMIN_EMAILS"
  DATABASE_URL              = "DATABASE_URL"
  DATABASE_USERNAME         = "DATABASE_USERNAME"
  DATABASE_PASSWORD         = "DATABASE_PASSWORD"
}

$resolved = [ordered]@{}
foreach ($targetName in $mapping.Keys) {
  $sourceName = $mapping[$targetName]
  $value = $source[$targetName]
  if ([string]::IsNullOrWhiteSpace($value)) { $value = $source[$sourceName] }
  if ([string]::IsNullOrWhiteSpace($value)) {
    throw "The required value '$sourceName' is missing or empty in $envFile"
  }
  $resolved[$targetName] = $value
}

if ($resolved.DELIVERY_ADMIN_EMAILS -match "@example\.com$") {
  throw "Replace DELIVERY_ADMIN_EMAILS=admin@example.com in backend/.env with your real Firebase admin email first."
}

# Relative credential paths are relative to backend/, regardless of the launcher's working directory.
$credentialPath = $resolved.FIREBASE_CREDENTIALS_PATH
if (-not [IO.Path]::IsPathRooted($credentialPath)) {
  $credentialPath = Join-Path (Join-Path $PSScriptRoot "..") $credentialPath
}
$credentialPath = [IO.Path]::GetFullPath($credentialPath)
if (-not (Test-Path -LiteralPath $credentialPath -PathType Leaf)) {
  throw "The Firebase service-account file was not found. Check FIREBASE_CREDENTIALS_PATH in backend/.env."
}
$resolved.FIREBASE_CREDENTIALS_PATH = $credentialPath

foreach ($targetName in $resolved.Keys) {
  $value = $resolved[$targetName]
  [Environment]::SetEnvironmentVariable($targetName, $value, "Process")
  if (-not $SessionOnly) {
    [Environment]::SetEnvironmentVariable($targetName, $value, "User")
  }
  Write-Host "Configured backend setting: $targetName"
}

if ($SessionOnly) {
  Write-Host "Backend settings loaded into this session. No Windows user settings were changed."
} else {
  Write-Host "Configuration complete. This terminal is ready; fully restart your IDE before launching the backend there."
}
