@echo off
title Vendor Coin Pay - Public Backend Server
cd /d "%~dp0backend"
echo ==========================================================
echo  VENDOR COIN PAY - BACKEND & CLOUDFLARE HTTPS TUNNEL
echo ==========================================================
echo.
python run_server_with_tunnel.py
pause
