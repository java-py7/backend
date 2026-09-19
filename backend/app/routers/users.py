from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import or_
from app.database import get_db
from app.models.user import User
from app.schemas.user import UserResponse, UserProfileUpdate, UserPublicProfile
from app.auth.dependencies import get_current_user

router = APIRouter(prefix="/api/v1/users", tags=["Users"])

@router.get("/me", response_model=UserResponse)
def get_current_user_profile(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    db.refresh(current_user)
    balance = current_user.wallet.balance if current_user.wallet else 0
    return UserResponse(
        id=current_user.id,
        user_code=current_user.user_code,
        full_name=current_user.full_name,
        username=current_user.username,
        phone=current_user.phone,
        is_active=current_user.is_active,
        created_at=current_user.created_at,
        balance=balance
    )

@router.put("/me", response_model=UserResponse)
def update_current_user_profile(req: UserProfileUpdate, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    if req.full_name and len(req.full_name.strip()) >= 2:
        current_user.full_name = req.full_name.strip()
    if req.phone and len(req.phone.strip()) >= 7:
        existing = db.query(User).filter(User.phone == req.phone.strip(), User.id != current_user.id).first()
        if existing:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Phone number is already in use by another account.")
        current_user.phone = req.phone.strip()

    db.commit()
    db.refresh(current_user)
    balance = current_user.wallet.balance if current_user.wallet else 0
    return UserResponse(
        id=current_user.id,
        user_code=current_user.user_code,
        full_name=current_user.full_name,
        username=current_user.username,
        phone=current_user.phone,
        is_active=current_user.is_active,
        created_at=current_user.created_at,
        balance=balance
    )

@router.get("/resolve/{identifier}", response_model=UserPublicProfile)
def resolve_user_public_profile(identifier: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Resolve receiver details before paying by user_code, username, or phone."""
    clean_id = identifier.strip()
    if clean_id.startswith("vendorcoinpay://pay/"):
        clean_id = clean_id.replace("vendorcoinpay://pay/", "").strip()

    target = db.query(User).filter(
        or_(
            User.user_code == clean_id,
            User.username == clean_id.lower(),
            User.phone == clean_id
        )
    ).first()

    if not target or not target.is_active:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"User with identifier '{clean_id}' was not found."
        )

    return UserPublicProfile(
        id=target.id,
        user_code=target.user_code,
        full_name=target.full_name,
        username=target.username
    )
