# Builds the release APK and sends it through Firebase App Distribution to the testers in local.properties
# (appDistributionTesters=...). Needs app/google-services.json and a `firebase login` on this PC (see SYNC.md).
# .\tools\publish.ps1 "What changed in this build"
param([string]$notes = "")
# no $ErrorActionPreference = "Stop": PowerShell 5.1 would take Gradle's stderr warnings for errors; exit codes are checked
$env:JAVA_HOME = "$env:USERPROFILE\.gradle\jdks\eclipse_adoptium-25-amd64-windows.2"
Set-Location (Split-Path $PSScriptRoot)
if (-not (Test-Path "app\google-services.json")) { throw "app\google-services.json is missing: see SYNC.md, step 2." }
& .\gradlew.bat :app:testDebugUnitTest :app:assembleRelease
if ($LASTEXITCODE -ne 0) { throw "The build or its tests failed; nothing was sent." }
& .\gradlew.bat :app:appDistributionUploadRelease "--releaseNotes=$notes"
if ($LASTEXITCODE -ne 0) { throw "The upload failed." }
