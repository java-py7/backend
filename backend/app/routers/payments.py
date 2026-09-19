from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.user import User
from app.schemas.payment import PaymentRequest, PaymentResponse
from app.auth.dependencies import get_current_user
from app.services.payment_service import execute_payment

router = APIRouter(prefix="/api/v1/payments", tags=["Payments"])

@router.post("/send", response_model=PaymentResponse, status_code=status.HTTP_200_OK)
def send_coins(req: PaymentRequest, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    result = execute_payment(
        db=db,
        sender=current_user,
        receiver_identifier=req.receiver_identifier,
        amount=req.amount,
        note=req.note
    )
    return PaymentResponse(**result)
