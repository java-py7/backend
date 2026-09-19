import random
import uuid

def generate_user_code() -> str:
    """Generate a clean, readable 6-digit user code prefixed with VC-."""
    num = random.randint(100000, 999999)
    return f"VC-{num}"

def generate_transaction_ref() -> str:
    """Generate a unique transaction reference."""
    return f"TXN-{uuid.uuid4().hex[:12].upper()}"
