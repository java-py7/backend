from datetime import datetime
from typing import Optional
from pydantic import BaseModel

class TransactionResponse(BaseModel):
    id: int
    reference_id: str
    sender_id: int
    sender_name: str
    sender_code: str
    receiver_id: int
    receiver_name: str
    receiver_code: str
    amount: int
    status: str
    note: Optional[str] = ""
    timestamp: datetime
    type: str # 'SENT' or 'RECEIVED' relative to current user

    class Config:
        from_attributes = True

class TransactionDetail(BaseModel):
    id: int
    reference_id: str
    amount: int
    sender_name: str
    sender_code: str
    receiver_name: str
    receiver_code: str
    status: str
    note: Optional[str] = ""
    created_at: datetime
    type: str # 'SENT' or 'RECEIVED' relative to current user

