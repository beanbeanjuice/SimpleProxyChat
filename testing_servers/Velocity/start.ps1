Set-Location $PSScriptRoot
docker compose up -d

$services = "proxy", "paper", "paper2", "fabric", "neoforge"

foreach ($s in $services) {
  wt -w 0 new-tab --title $s -d "$PSScriptRoot" pwsh -NoExit -Command "docker compose logs --tail 100 $s \; docker compose attach $s"
  Start-Sleep -Milliseconds 300
}

