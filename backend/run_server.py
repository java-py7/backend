import os
import sys
import uvicorn

if __name__ == "__main__":
    # Ensure current directory is in sys.path
    current_dir = os.path.dirname(os.path.abspath(__file__))
    if current_dir not in sys.path:
        sys.path.insert(0, current_dir)

    print("==================================================")
    print("Starting Vendor Coin Pay FastAPI Backend...")
    print("Accessible locally on: http://127.0.0.1:8000")
    print("Accessible from Android Emulator on: http://10.0.2.2:8000")
    print("Accessible from Local Network on: http://0.0.0.0:8000")
    print("API Documentation available at: http://127.0.0.1:8000/docs")
    print("==================================================")

    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=False)
