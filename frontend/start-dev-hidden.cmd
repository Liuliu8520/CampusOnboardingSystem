@echo off
cd /d "%~dp0"
npm.cmd --cache .\.npm-cache run dev -- --host 0.0.0.0 > "%~dp0dev-server.log" 2> "%~dp0dev-server.err.log"
