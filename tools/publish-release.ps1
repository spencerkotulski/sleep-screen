param(
    [Parameter(Mandatory = $true)][string]$Version,
    [Parameter(Mandatory = $true)][string]$Apk,
    [Parameter(Mandatory = $true)][string]$NotesFile
)
$ErrorActionPreference = 'Stop'
$repo = 'spencerkotulski/sleep-screen'
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) { throw 'Install GitHub CLI and run gh auth login first.' }
if (-not (Test-Path -LiteralPath $Apk -PathType Leaf)) { throw 'APK file not found.' }
if (-not (Test-Path -LiteralPath $NotesFile -PathType Leaf)) { throw 'Release notes file not found.' }
if ($Version -notmatch '^v[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9.-]+)?$') { throw 'Version must look like v0.3.0-beta.' }
& gh auth status
if ($LASTEXITCODE -ne 0) { throw 'Run gh auth login first.' }
# Never overwrite an existing release or silently replace its APK.
& gh release view $Version --repo $repo 2>$null | Out-Null
if ($LASTEXITCODE -eq 0) { throw 'This release already exists. Choose a new version.' }
$staging = Join-Path ([System.IO.Path]::GetTempPath()) ('sleep-screen-release-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $staging | Out-Null
try {
    $asset = Join-Path $staging 'Sleep-Screen.apk'
    Copy-Item -LiteralPath $Apk -Destination $asset
    & gh release create $Version $asset --repo $repo --title "Sleep Screen $Version" --notes-file $NotesFile --target main --latest
    if ($LASTEXITCODE -ne 0) { throw 'Release creation failed. Check GitHub before retrying.' }
    Write-Host "Published $Version. Downloader code 4109594 follows the latest release."
} finally {
    Remove-Item -LiteralPath $staging -Recurse -Force
}
