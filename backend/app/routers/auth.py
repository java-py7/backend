from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import or_
from app.database import get_db
from app.models.user import User
from app.models.wallet import Wallet
from app.schemas.user import UserRegister, UserLogin, UserResponse
from app.schemas.auth import TokenResponse
from app.auth.security import get_password_hash, verify_password, create_access_token
from app.utils.code_generator import generate_user_code

router = APIRouter(prefix="/api/v1/auth", tags=["Authentication"])

@router.post("/register", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
def register_user(req: UserRegister, db: Session = Depends(get_db)):
    # 1. Validation checks
    if req.confirm_password and req.password != req.confirm_password:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Passwords do not match.")

    # Check if username or phone already exists
    existing = db.query(User).filter(
        or_(User.username == req.username.strip().lower(), User.phone == req.phone.strip())
    ).first()
    if existing:
        if existing.username == req.username.strip().lower():
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Username is already taken.")
        if existing.phone == req.phone.strip():
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Phone number is already registered.")

    # Generate unique user_code
    for _ in range(10):
        code = generate_user_code()
        if not db.query(User).filter(User.user_code == code).first():
            break
    else:
        code = f"VC-{int(UserRegister.model_fields.get('username', '0') or 100000)}"

    # 2. Create user
    new_user = User(
        user_code=code,
        full_name=req.full_name.strip(),
        username=req.username.strip().lower(),
        phone=req.phone.strip(),
        hashed_password=get_password_hash(req.password),
        is_active=True
    )
    db.add(new_user)
    db.flush() # get new_user.id

    # 3. Create wallet with initial welcome balance of 1,000 coins
    wallet = Wallet(
        user_id=new_user.id,
        balance=1000,
        currency="COIN"
    )
    db.add(wallet)
    db.commit()
    db.refresh(new_user)

    # 4. Generate JWT token
    token = create_access_token({"sub": str(new_user.id)})

    return TokenResponse(
        access_token=token,
        token_type="bearer",
        user=UserResponse(
            id=new_user.id,
            user_code=new_user.user_code,
            full_name=new_user.full_name,
            username=new_user.username,
            phone=new_user.phone,
            is_active=new_user.is_active,
            created_at=new_user.created_at,
            balance=wallet.balance
        )
    )

@router.post("/login", response_model=TokenResponse)
def login_user(req: UserLogin, db: Session = Depends(get_db)):
    ident = req.username_or_phone.strip()
    user = db.query(User).filter(
        or_(User.username == ident.lower(), User.phone == ident)
    ).first()

    if not user or not verify_password(req.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username/phone or password."
        )

    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Account is deactivated. Please contact support."
        )

    token = create_access_token({"sub": str(user.id)})
    balance = user.wallet.balance if user.wallet else 0

    return TokenResponse(
        access_token=token,
        token_type="bearer",
        user=UserResponse(
            id=user.id,
            user_code=user.user_code,
            full_name=user.full_name,
            username=user.username,
            phone=user.phone,
            is_active=user.is_active,
            created_at=user.created_at,
            balance=balance
        )
    )
