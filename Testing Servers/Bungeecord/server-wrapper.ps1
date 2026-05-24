param(
    [string]$WorkingDir,
    [string]$Jar,
    [string]$SignalFile,
    [string]$StopCommand = "stop"
)

Set-Location $WorkingDir

# Clean up any stale signal from a previous run
if (Test-Path $SignalFile) { Remove-Item $SignalFile -Force }

$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = "java"
$psi.Arguments = "-Xms512M -Xmx1024M -jar `"$Jar`" nogui"
$psi.WorkingDirectory = $WorkingDir
$psi.UseShellExecute = $false
$psi.RedirectStandardInput = $true
# stdout/stderr are intentionally NOT redirected so server logs appear in the tab directly

$proc = [System.Diagnostics.Process]::Start($psi)
$proc.StandardInput.AutoFlush = $true

$buf = ""
while (-not $proc.HasExited) {
    # Check for stop signal from the controller tab
    if (Test-Path $SignalFile) {
        Remove-Item $SignalFile -Force
        $proc.StandardInput.WriteLine($StopCommand)
    }

    # Forward keyboard input to the server so you can still type commands manually
    if ([Console]::KeyAvailable) {
        $key = [Console]::ReadKey($true)
        if ($key.Key -eq [ConsoleKey]::Enter) {
            [Console]::WriteLine()
            if ($buf.Length -gt 0) {
                $proc.StandardInput.WriteLine($buf)
                $buf = ""
            }
        } elseif ($key.Key -eq [ConsoleKey]::Backspace) {
            if ($buf.Length -gt 0) {
                $buf = $buf.Substring(0, $buf.Length - 1)
                [Console]::Write("`b `b")
            }
        } else {
            $buf += $key.KeyChar
            [Console]::Write($key.KeyChar)
        }
    }

    Start-Sleep -Milliseconds 50
}

Write-Host "Server stopped. Press Enter to close."
$null = Read-Host
