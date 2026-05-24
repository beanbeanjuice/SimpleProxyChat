@echo off
set ROOT=G:\Minecraft Servers\Bungeecord
wt new-tab --title "Proxy" --startingDirectory "%ROOT%\1 - Proxy" powershell -ExecutionPolicy Bypass -File "%ROOT%\server-wrapper.ps1" -WorkingDir "%ROOT%\1 - Proxy" -Jar "proxy.jar" -SignalFile "%ROOT%\stop-proxy.signal" -StopCommand "end" ^
; new-tab --title "Paper" --startingDirectory "%ROOT%\2 - Paper" powershell -ExecutionPolicy Bypass -File "%ROOT%\server-wrapper.ps1" -WorkingDir "%ROOT%\2 - Paper" -Jar "server.jar" -SignalFile "%ROOT%\stop-paper.signal" ^
; new-tab --title "Spigot" --startingDirectory "%ROOT%\3 - Spigot" powershell -ExecutionPolicy Bypass -File "%ROOT%\server-wrapper.ps1" -WorkingDir "%ROOT%\3 - Spigot" -Jar "server.jar" -SignalFile "%ROOT%\stop-spigot.signal" ^
; new-tab --title "Controller" powershell -ExecutionPolicy Bypass -File "%ROOT%\stop-controller.ps1"
