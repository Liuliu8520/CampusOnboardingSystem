@echo off
cd /d "%~dp0"
npm.cmd --cache .\.npm-cache run dev -- --host 0.0.0.0
