@echo off
title Chạy Unit Test Backend
echo ==============================================
echo   DANG KHOI CHAY TOAN BO UNIT TEST (JAVA)
echo ==============================================
echo.

cd /d "%~dp0"
call mvnw.cmd test

echo.
echo ==============================================
echo   DA CHAY XONG! Kiem tra ket qua o tren.
echo ==============================================
pause
