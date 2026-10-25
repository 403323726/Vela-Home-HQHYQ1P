param(
    [Parameter(Mandatory = $true)]
    [string]$TvHost
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$python = Join-Path $repo 'tools\venv\Scripts\python.exe'
$tv = Join-Path $repo 'tools\tv.py'

if (-not (Test-Path -LiteralPath $python)) {
    throw "Python environment not found. Run: py -m venv tools\venv; tools\venv\Scripts\python.exe -m pip install -r tools\requirements.txt"
}

$env:TV_HOST = $TvHost
$package = 'com.dangbei.leard.leradlauncher'
$velaHome = 'com.gathin.tvhome/.MainActivity'

function Invoke-Tv([string]$Command) {
    $output = (& $python -I $tv $Command 2>&1 | Out-String).Trim()
    if ($LASTEXITCODE -ne 0) {
        throw "ADB command failed: $Command`n$output"
    }
    if ($output -match '(?i)security exception|permission denial|exception|denied|error') {
        throw "Device rejected: $Command`n$output"
    }
    if ($output) { Write-Host $output }
    return $output
}

Write-Host 'Setting Vela Home as the default Home...'
Invoke-Tv "cmd package set-home-activity --user 0 $velaHome" | Out-Null

Write-Host 'Stopping the Dangbei OS launcher process...'
Invoke-Tv "am force-stop $package" | Out-Null

Write-Host 'Restricting Dangbei OS background execution...'
Invoke-Tv "cmd appops set $package RUN_IN_BACKGROUND ignore" | Out-Null
Invoke-Tv "cmd appops set $package RUN_ANY_IN_BACKGROUND ignore" | Out-Null
Invoke-Tv "am set-standby-bucket --user 0 $package rare" | Out-Null

Write-Host ''
Write-Host 'Verification:'
Invoke-Tv 'cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME' | Out-Null
Invoke-Tv "pm list packages -e $package" | Out-Null
Invoke-Tv "cmd appops get $package RUN_IN_BACKGROUND" | Out-Null
Invoke-Tv "cmd appops get $package RUN_ANY_IN_BACKGROUND" | Out-Null

Write-Host ''
Write-Host 'Done. Reboot once and verify Home, Wi-Fi, and ADB before making more changes.'
Write-Host 'Undo with: powershell -ExecutionPolicy Bypass -File scripts\restore-dangbei-background.ps1 -TvHost <IP>'
