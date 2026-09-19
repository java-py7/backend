from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field

class UserBase(BaseModel):
    full_name: str = Field(..., min_length=2, max_length=100)
    username: str = Field(..., min_length=3, max_length=30)
    phone: str = Field(..., min_length=7, max_length=20)

class UserRegister(UserBase):
    password: str = Field(..., min_length=4)
    confirm_password: Optional[str] = None

class UserLogin(BaseModel):
    username_or_phone: str
    password: str

class UserProfileUpdate(BaseModel):
    full_name: Optional[str] = None
    phone: Optional[str] = None

class UserPublicProfile(BaseModel):
    id: int
    user_code: str
    full_name: str
    username: str

    class Config:
        from_attributes = True

class UserResponse(BaseModel):
    id: int
    user_code: str
    full_name: str
    username: str
    phone: str
    is_active: bool
    created_at: datetime
    balance: Optional[int] = 0

    class Config:
        from_attributes = True
