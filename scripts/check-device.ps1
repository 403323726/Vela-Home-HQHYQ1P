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

function Show-Tv([string]$Label, [string]$Command) {
    Write-Host ''
    Write-Host "== $Label =="
    & $python -I $tv $Command
    if ($LASTEXITCODE -ne 0) { throw "ADB command failed: $Command" }
}

Show-Tv 'Model' 'getprop ro.product.model'
Show-Tv 'Android' 'getprop ro.build.version.release'
Show-Tv 'SDK' 'getprop ro.build.version.sdk'
Show-Tv 'Display size' 'wm size'
Show-Tv 'Display density' 'wm density'
Show-Tv 'Default Home' 'cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME'
Show-Tv 'Vela Home package' 'pm list packages -e com.gathin.tvhome'
Show-Tv 'ADB controller' 'pm list packages -e jiujiutv.jiujiu.adb'
Show-Tv 'Dangbei launcher package' 'pm list packages -e com.dangbei.leard.leradlauncher'
Show-Tv 'Dangbei RUN_IN_BACKGROUND' 'cmd appops get com.dangbei.leard.leradlauncher RUN_IN_BACKGROUND'
Show-Tv 'Dangbei RUN_ANY_IN_BACKGROUND' 'cmd appops get com.dangbei.leard.leradlauncher RUN_ANY_IN_BACKGROUND'
Show-Tv 'Disabled packages' 'pm list packages -d'
Show-Tv 'Foreground activity' "dumpsys activity activities | grep -E 'mResumedActivity|topResumedActivity' | head -n 3"
