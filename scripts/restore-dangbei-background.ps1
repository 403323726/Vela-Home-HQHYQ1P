param(
    [Parameter(Mandatory = $true)]
    [string]$TvHost,
    [switch]$MakeDefault
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
$component = 'com.dangbei.leard.leradlauncher/com.dangbei.launcher.home.LauncherActivity'

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

Write-Host 'Enabling the Dangbei OS launcher package...'
Invoke-Tv "pm enable --user 0 $package" | Out-Null

Write-Host 'Restoring Dangbei OS background execution...'
Invoke-Tv "cmd appops set $package RUN_IN_BACKGROUND allow" | Out-Null
Invoke-Tv "cmd appops set $package RUN_ANY_IN_BACKGROUND allow" | Out-Null
Invoke-Tv "am set-standby-bucket --user 0 $package active" | Out-Null

if ($MakeDefault) {
    Write-Host 'Setting the Dangbei OS launcher as default Home...'
    Invoke-Tv "cmd package set-home-activity --user 0 $component" | Out-Null
}

Write-Host ''
Write-Host 'Verification:'
Invoke-Tv "pm list packages -e $package" | Out-Null
Invoke-Tv 'cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME' | Out-Null
Invoke-Tv "cmd appops get $package RUN_IN_BACKGROUND" | Out-Null
Invoke-Tv "cmd appops get $package RUN_ANY_IN_BACKGROUND" | Out-Null
