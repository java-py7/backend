import os
import sys
import time
import re
import subprocess
import threading
import uvicorn

def start_tunnel():
    """Starts cloudflared tunnel in the background and prints the public URL."""
    tunnel_exe = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "cloudflared.exe")
    if not os.path.exists(tunnel_exe):
        print(f"[!] Warning: {tunnel_exe} not found. Running local-only mode.")
        return None

    print("\n[*] Initializing Cloudflare secure HTTPS tunnel...")
    process = subprocess.Popen(
        [tunnel_exe, "tunnel", "--url", "http://127.0.0.1:8000"],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        encoding="utf-8",
        errors="replace"
    )

    public_url = None
    url_pattern = re.compile(r"https://[a-zA-Z0-9-]+\.trycloudflare\.com")

    # Monitor output for the tunnel URL
    def monitor_tunnel():
        nonlocal public_url
        for line in iter(process.stdout.readline, ''):
            if not line:
                break
            match = url_pattern.search(line)
            if match and not public_url:
                public_url = match.group(0)
                print("==================================================================")
                print(f"[*] PUBLIC HTTPS CLOUD TUNNEL ACTIVE: {public_url}")
                print(f"[*] External devices can connect directly to: {public_url}/")
                print("==================================================================")

    thread = threading.Thread(target=monitor_tunnel, daemon=True)
    thread.start()

    # Wait up to 10 seconds for URL
    for _ in range(20):
        if public_url:
            break
        time.sleep(0.5)

    return process

if __name__ == "__main__":
    current_dir = os.path.dirname(os.path.abspath(__file__))
    if current_dir not in sys.path:
        sys.path.insert(0, current_dir)

    # Initialize Database
    try:
        from app.database import init_db
        init_db()
        print("[*] PostgreSQL database verified and active.")
    except Exception as e:
        print(f"[!] Warning during DB check: {e}")

    # Start Cloudflare Tunnel
    tunnel_proc = start_tunnel()

    print("==================================================")
    print("Starting Vendor Coin Pay FastAPI Backend...")
    print("Accessible locally on: http://127.0.0.1:8000")
    print("API Documentation available at: http://127.0.0.1:8000/docs")
    print("==================================================")

    try:
        uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=False)
    finally:
        if tunnel_proc:
            tunnel_proc.terminate()
