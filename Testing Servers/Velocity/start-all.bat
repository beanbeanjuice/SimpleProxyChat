@echo off
set "ROOT=%~dp0"
set "ROOT=%ROOT:~0,-1%"
wt new-tab --title "Proxy" --startingDirectory "%ROOT%\1 - Proxy" powershell -ExecutionPolicy Bypass -File "%ROOT%\server-wrapper.ps1" -WorkingDir "%ROOT%\1 - Proxy" -Jar "proxy.jar" -SignalFile "%ROOT%\stop-proxy.signal" -StopCommand "shutdown" ^
; new-tab --title "Paper" --startingDirectory "%ROOT%\2 - Paper" powershell -ExecutionPolicy Bypass -File "%ROOT%\server-wrapper.ps1" -WorkingDir "%ROOT%\2 - Paper" -Jar "server.jar" -SignalFile "%ROOT%\stop-paper.signal" ^
; new-tab --title "Fabric" --startingDirectory "%ROOT%\3 - Fabric" powershell -ExecutionPolicy Bypass -File "%ROOT%\server-wrapper.ps1" -WorkingDir "%ROOT%\3 - Fabric" -Jar "server.jar" -SignalFile "%ROOT%\stop-fabric.signal" ^
; new-tab --title "Folia" --startingDirectory "%ROOT%\4 - Folia" powershell -ExecutionPolicy Bypass -File "%ROOT%\server-wrapper.ps1" -WorkingDir "%ROOT%\4 - Folia" -Jar "server.jar" -SignalFile "%ROOT%\stop-folia.signal" ^
; new-tab --title "Controller" powershell -ExecutionPolicy Bypass -File "%ROOT%\stop-controller.ps1"
