from datetime import datetime
from fastapi import HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import or_
from app.models.user import User
from app.models.wallet import Wallet
from app.models.transaction import Transaction
from app.utils.code_generator import generate_transaction_ref

def execute_payment(db: Session, sender: User, receiver_identifier: str, amount: int, note: str = "") -> dict:
    """
    Execute an atomic transfer of coins from sender to receiver.
    Uses row-level locking (with_for_update) to prevent race conditions.
    """
    # 1. Basic validation
    if amount <= 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Transfer amount must be greater than zero."
        )

    # Clean identifier
    identifier = receiver_identifier.strip()
    if identifier.startswith("vendorcoinpay://pay/"):
        identifier = identifier.replace("vendorcoinpay://pay/", "").strip()

    # 2. Find receiver
    receiver = db.query(User).filter(
        or_(
            User.user_code == identifier,
            User.username == identifier,
            User.phone == identifier
        )
    ).first()

    if not receiver:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Receiver '{identifier}' not found. Please check the user code, phone, or username."
        )

    if not receiver.is_active:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Receiver account is inactive."
        )

    # 3. Prevent self-transfer
    if sender.id == receiver.id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="You cannot send coins to yourself."
        )

    # 4. Atomic transaction with row locking
    # Determine locking order by user_id to prevent database deadlocks
    first_id = min(sender.id, receiver.id)
    second_id = max(sender.id, receiver.id)

    # Lock wallets in strict ascending order of user_id
    wallet_first = db.query(Wallet).filter(Wallet.user_id == first_id).with_for_update().first()
    wallet_second = db.query(Wallet).filter(Wallet.user_id == second_id).with_for_update().first()

    sender_wallet = wallet_first if sender.id == first_id else wallet_second
    receiver_wallet = wallet_second if sender.id == first_id else wallet_first

    if not sender_wallet:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Sender wallet not found.")
    if not receiver_wallet:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Receiver wallet not found.")

    # 5. Check sufficient balance
    if sender_wallet.balance < amount:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Insufficient coins. Available balance: {sender_wallet.balance} Coins, required: {amount} Coins."
        )

    # 6. Execute atomic transfer
    sender_wallet.balance -= amount
    sender_wallet.updated_at = datetime.utcnow()

    receiver_wallet.balance += amount
    receiver_wallet.updated_at = datetime.utcnow()

    # 7. Create ledger transaction entry
    ref_id = generate_transaction_ref()
    txn = Transaction(
        reference_id=ref_id,
        sender_id=sender.id,
        receiver_id=receiver.id,
        amount=amount,
        status="COMPLETED",
        note=note or "",
        created_at=datetime.utcnow()
    )
    db.add(txn)

    # 8. Commit atomically
    try:
        db.commit()
    except Exception as e:
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Payment transaction failed to commit: {str(e)}"
        )

    return {
        "success": True,
        "message": f"Successfully sent {amount} Coins to {receiver.full_name}",
        "reference_id": ref_id,
        "amount": amount,
        "sender_new_balance": sender_wallet.balance,
        "receiver_name": receiver.full_name,
        "receiver_code": receiver.user_code,
        "timestamp": txn.created_at.strftime("%Y-%m-%d %H:%M:%S")
    }
