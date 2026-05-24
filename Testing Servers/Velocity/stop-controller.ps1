$root = $PSScriptRoot

Write-Host "=== Server Stop Controller ===" -ForegroundColor Cyan
Write-Host "Type 'stop' and press Enter to safely shut down all servers."
Write-Host ""

while ($true) {
    $cmd = Read-Host ">"
    if ($cmd.Trim().ToLower() -eq "stop") {
        Write-Host "Sending stop signals..." -ForegroundColor Yellow
        New-Item "$root\stop-proxy.signal"  -ItemType File -Force | Out-Null
        New-Item "$root\stop-paper.signal"  -ItemType File -Force | Out-Null
        New-Item "$root\stop-fabric.signal" -ItemType File -Force | Out-Null
        New-Item "$root\stop-folia.signal"  -ItemType File -Force | Out-Null
        Write-Host "Done. Servers are shutting down." -ForegroundColor Green
        break
    } else {
        Write-Host "Unknown command. Type 'stop' to stop all servers." -ForegroundColor DarkGray
    }
}

Write-Host "Press Enter to close."
$null = Read-Host
