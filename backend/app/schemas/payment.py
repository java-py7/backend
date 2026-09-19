from typing import Optional
from pydantic import BaseModel, Field

class PaymentRequest(BaseModel):
    receiver_identifier: str = Field(..., description="Receiver user_code (e.g. VC-108247), username, or phone")
    amount: int = Field(..., gt=0, description="Number of virtual coins to send (must be > 0)")
    note: Optional[str] = Field(default="", max_length=100)

class PaymentResponse(BaseModel):
    success: bool
    message: str
    reference_id: str
    amount: int
    sender_new_balance: int
    receiver_name: str
    receiver_code: str
    timestamp: str

class QRResolveResponse(BaseModel):
    user_id: int
    user_code: str
    full_name: str
    username: str
    phone: str
    qr_payload: str
