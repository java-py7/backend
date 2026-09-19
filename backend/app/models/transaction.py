from datetime import datetime
from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, CheckConstraint
from sqlalchemy.orm import relationship
from app.database import Base

class Transaction(Base):
    __tablename__ = "transactions"

    id = Column(Integer, primary_key=True, index=True)
    reference_id = Column(String(40), unique=True, index=True, nullable=False) # e.g. TXN-a1b2c3d4e5f6
    sender_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    receiver_id = Column(Integer, ForeignKey("users.id"), nullable=False, index=True)
    amount = Column(Integer, CheckConstraint("amount > 0", name="positive_amount"), nullable=False)
    status = Column(String(20), default="COMPLETED", nullable=False) # COMPLETED, FAILED
    note = Column(String(255), default="")
    created_at = Column(DateTime, default=datetime.utcnow, index=True)

    # Relationships
    sender = relationship("User", foreign_keys=[sender_id], back_populates="sent_transactions")
    receiver = relationship("User", foreign_keys=[receiver_id], back_populates="received_transactions")
