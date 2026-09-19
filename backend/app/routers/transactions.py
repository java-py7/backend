from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session
from sqlalchemy import or_, desc
from app.database import get_db
from app.models.user import User
from app.models.transaction import Transaction
from app.schemas.transaction import TransactionResponse, TransactionDetail
from app.auth.dependencies import get_current_user

router = APIRouter(prefix="/api/v1/transactions", tags=["Transactions"])

@router.get("", response_model=List[TransactionResponse])
def get_user_transactions(
    filter_type: str = Query("ALL", pattern="^(ALL|SENT|RECEIVED)$"),
    limit: int = Query(20, ge=1, le=100),
    offset: int = Query(0, ge=0),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    query = db.query(Transaction)

    if filter_type == "SENT":
        query = query.filter(Transaction.sender_id == current_user.id)
    elif filter_type == "RECEIVED":
        query = query.filter(Transaction.receiver_id == current_user.id)
    else:
        query = query.filter(
            or_(Transaction.sender_id == current_user.id, Transaction.receiver_id == current_user.id)
        )

    txns = query.order_by(desc(Transaction.created_at)).offset(offset).limit(limit).all()

    result = []
    for t in txns:
        is_sent = (t.sender_id == current_user.id)
        result.append(
            TransactionResponse(
                id=t.id,
                reference_id=t.reference_id,
                sender_id=t.sender_id,
                sender_name=t.sender.full_name if t.sender else "Unknown",
                sender_code=t.sender.user_code if t.sender else "",
                receiver_id=t.receiver_id,
                receiver_name=t.receiver.full_name if t.receiver else "Unknown",
                receiver_code=t.receiver.user_code if t.receiver else "",
                amount=t.amount,
                status=t.status,
                note=t.note or "",
                timestamp=t.created_at,
                type="SENT" if is_sent else "RECEIVED"
            )
        )
    return result

@router.get("/{identifier}", response_model=TransactionDetail)
def get_transaction_by_id(identifier: str, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    # Check if identifier is numeric or reference string
    if identifier.isdigit():
        query_filter = (Transaction.id == int(identifier))
    else:
        query_filter = (Transaction.reference_id == identifier)

    t = db.query(Transaction).filter(
        query_filter,
        or_(Transaction.sender_id == current_user.id, Transaction.receiver_id == current_user.id)
    ).first()

    if not t:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Transaction not found or unauthorized access."
        )

    is_sent = (t.sender_id == current_user.id)

    return TransactionDetail(
        id=t.id,
        reference_id=t.reference_id,
        amount=t.amount,
        sender_name=t.sender.full_name if t.sender else "Unknown",
        sender_code=t.sender.user_code if t.sender else "",
        receiver_name=t.receiver.full_name if t.receiver else "Unknown",
        receiver_code=t.receiver.user_code if t.receiver else "",
        status=t.status,
        note=t.note or "",
        created_at=t.created_at,
        type="SENT" if is_sent else "RECEIVED"
    )

