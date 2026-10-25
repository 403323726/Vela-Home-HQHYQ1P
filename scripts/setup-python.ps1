$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
Set-Location $repo

if (-not (Test-Path -LiteralPath 'tools\requirements.txt')) {
    throw 'Run this script from the repository scripts directory.'
}

if (-not (Test-Path -LiteralPath 'tools\venv\Scripts\python.exe')) {
    py -m venv tools\venv
    if ($LASTEXITCODE -ne 0) { throw 'Could not create the Python virtual environment.' }
}

tools\venv\Scripts\python.exe -m pip install --upgrade pip
tools\venv\Scripts\python.exe -m pip install -r tools\requirements.txt
Write-Host 'Python environment ready: tools\venv'
