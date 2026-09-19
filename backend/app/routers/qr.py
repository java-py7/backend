from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import or_
from app.database import get_db
from app.models.user import User
from app.schemas.payment import QRResolveResponse
from app.auth.dependencies import get_current_user

router = APIRouter(prefix="/api/v1/qr", tags=["QR Code"])

@router.get("/my/payload", response_model=QRResolveResponse)
def get_my_qr_payload(current_user: User = Depends(get_current_user)):
    return QRResolveResponse(
        user_id=current_user.id,
        user_code=current_user.user_code,
        full_name=current_user.full_name,
        username=current_user.username,
        phone=current_user.phone,
        qr_payload=f"vendorcoinpay://pay/{current_user.user_code}"
    )

@router.get("/resolve/{identifier}", response_model=QRResolveResponse)
def resolve_qr_code(identifier: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    clean_id = identifier.strip()
    if clean_id.startswith("vendorcoinpay://pay/"):
        clean_id = clean_id.replace("vendorcoinpay://pay/", "").strip()

    user = db.query(User).filter(
        or_(
            User.user_code == clean_id,
            User.username == clean_id.lower(),
            User.phone == clean_id
        )
    ).first()

    if not user or not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"QR target '{clean_id}' does not belong to any active user."
        )

    return QRResolveResponse(
        user_id=user.id,
        user_code=user.user_code,
        full_name=user.full_name,
        username=user.username,
        phone=user.phone,
        qr_payload=f"vendorcoinpay://pay/{user.user_code}"
    )
