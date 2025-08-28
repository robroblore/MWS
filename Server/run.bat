@echo off
cls
title Server Console
:StartServer
start "" /b /wait java -Xmx8G -jar server.jar nogui -o true
echo (%time%) Server closed/crashed... restarting!
goto StartServer