from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.database import Base, engine, init_db
from app.models import User, Wallet, Transaction # Ensures all models are registered
from app.routers import auth, users, wallet, payments, transactions, qr

# Automatically ensure database tables exist (works both locally and in cloud)
init_db()


app = FastAPI(
    title="Vendor Coin Pay API",
    description="Backend API for Vendor Coin Pay mobile virtual coin payment system.",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router)
app.include_router(users.router)
app.include_router(wallet.router)
app.include_router(payments.router)
app.include_router(transactions.router)
app.include_router(qr.router)

@app.get("/")
def root():
    return {
        "app": "Vendor Coin Pay API",
        "status": "online",
        "version": "1.0.0",
        "message": "Welcome to Vendor Coin Pay API"
    }

@app.get("/health")
def health():
    return {"status": "healthy"}
