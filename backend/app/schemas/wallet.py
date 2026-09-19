from datetime import datetime
from pydantic import BaseModel

class WalletResponse(BaseModel):
    user_id: int
    user_code: str
    balance: int
    currency: str
    updated_at: datetime

    class Config:
        from_attributes = True
