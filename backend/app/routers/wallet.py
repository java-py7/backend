from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.user import User
from app.models.wallet import Wallet
from app.schemas.wallet import WalletResponse
from app.auth.dependencies import get_current_user

router = APIRouter(prefix="/api/v1/wallet", tags=["Wallet"])

@router.get("/balance", response_model=WalletResponse)
def get_user_wallet_balance(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    wallet = db.query(Wallet).filter(Wallet.user_id == current_user.id).first()
    if not wallet:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Wallet record not found for current user."
        )

    return WalletResponse(
        user_id=current_user.id,
        user_code=current_user.user_code,
        balance=wallet.balance,
        currency=wallet.currency,
        updated_at=wallet.updated_at
    )
