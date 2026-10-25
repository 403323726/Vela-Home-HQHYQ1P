param(
    [Parameter(Mandatory = $true)]
    [string]$TvHost,
    [string]$StockLauncher = 'none',
    [string]$JdkHome = $env:JAVA_HOME,
    [string]$AndroidHome = $env:ANDROID_HOME
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$python = Join-Path $repo 'tools\venv\Scripts\python.exe'
$launcherScript = Join-Path $repo 'launcher\build-and-install.ps1'

if (-not (Test-Path -LiteralPath $python)) {
    throw "Python environment not found. Run: py -m venv tools\venv; tools\venv\Scripts\python.exe -m pip install -r tools\requirements.txt"
}
if (-not $JdkHome) { throw 'Set JAVA_HOME or pass -JdkHome <JDK directory>.' }
if (-not $AndroidHome) { throw 'Set ANDROID_HOME or pass -AndroidHome <Android SDK directory>.' }

$env:JAVA_HOME = $JdkHome
$env:ANDROID_HOME = $AndroidHome
$env:ANDROID_SDK_ROOT = $AndroidHome
$env:TV_HOST = $TvHost

& $launcherScript -TvHost $TvHost -StockLauncher $StockLauncher
if ($LASTEXITCODE -ne 0) { throw 'Launcher build or installation failed.' }
