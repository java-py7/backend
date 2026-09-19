import os
import sys
import time
import requests

# Ensure backend root is in sys.path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

BASE_URL = "http://127.0.0.1:8000"

def test_full_flow():
    print("\n--- [STARTING END-TO-END VERIFICATION] ---")
    
    # 1. Health check
    res = requests.get(f"{BASE_URL}/health")
    assert res.status_code == 200, f"Health check failed: {res.text}"
    print("[1/10] Backend health check PASSED")

    # Unique usernames based on timestamp
    ts = int(time.time())
    user_a_uname = f"alice_{ts}"
    user_b_uname = f"bob_{ts}"
    user_a_phone = f"98{str(ts)[-8:]}"
    user_b_phone = f"97{str(ts)[-8:]}"

    # 2. Register User A
    payload_a = {
        "full_name": "Alice Sharma",
        "username": user_a_uname,
        "phone": user_a_phone,
        "password": "Password123",
        "confirm_password": "Password123"
    }
    res_a = requests.post(f"{BASE_URL}/api/v1/auth/register", json=payload_a)
    assert res_a.status_code == 201, f"User A registration failed: {res_a.text}"
    data_a = res_a.json()
    token_a = data_a["access_token"]
    user_a = data_a["user"]
    print(f"[2/10] User A registered: {user_a['full_name']} ({user_a['user_code']}), Balance: {user_a['balance']}")
    assert user_a["balance"] == 1000, "Initial balance should be 1000"

    # 3. Register User B
    payload_b = {
        "full_name": "Bob Verma",
        "username": user_b_uname,
        "phone": user_b_phone,
        "password": "Password123",
        "confirm_password": "Password123"
    }
    res_b = requests.post(f"{BASE_URL}/api/v1/auth/register", json=payload_b)
    assert res_b.status_code == 201, f"User B registration failed: {res_b.text}"
    data_b = res_b.json()
    token_b = data_b["access_token"]
    user_b = data_b["user"]
    print(f"[3/10] User B registered: {user_b['full_name']} ({user_b['user_code']}), Balance: {user_b['balance']}")
    assert user_b["balance"] == 1000, "Initial balance should be 1000"

    # 4. Login User A
    login_payload = {
        "username_or_phone": user_a_uname,
        "password": "Password123"
    }
    res_login = requests.post(f"{BASE_URL}/api/v1/auth/login", json=login_payload)
    assert res_login.status_code == 200, f"Login failed: {res_login.text}"
    print("[4/10] User A login PASSED")

    headers_a = {"Authorization": f"Bearer {token_a}"}
    headers_b = {"Authorization": f"Bearer {token_b}"}

    # 5. Resolve User B by user_code (simulating QR scan or manual input)
    res_res = requests.get(f"{BASE_URL}/api/v1/users/resolve/{user_b['user_code']}", headers=headers_a)
    assert res_res.status_code == 200, f"Resolve receiver failed: {res_res.text}"
    resolved_b = res_res.json()
    assert resolved_b["user_code"] == user_b["user_code"]
    print(f"[5/10] Resolved receiver by code {user_b['user_code']}: {resolved_b['full_name']} PASSED")

    # 6. User A sends 150 coins to User B
    pay_payload = {
        "receiver_identifier": user_b["user_code"],
        "amount": 150,
        "note": "Payment for fresh chai & snacks"
    }
    res_pay = requests.post(f"{BASE_URL}/api/v1/payments/send", json=pay_payload, headers=headers_a)
    assert res_pay.status_code == 200, f"Payment transfer failed: {res_pay.text}"
    pay_data = res_pay.json()
    assert pay_data["success"] is True
    assert pay_data["sender_new_balance"] == 850
    print(f"[6/10] Payment 150 Coins sent successfully! Ref: {pay_data['reference_id']}, Sender new balance: {pay_data['sender_new_balance']}")

    # 7. Check User B balance directly from database via /wallet/balance
    res_bal_b = requests.get(f"{BASE_URL}/api/v1/wallet/balance", headers=headers_b)
    assert res_bal_b.status_code == 200, f"Fetch balance B failed: {res_bal_b.text}"
    bal_b = res_bal_b.json()["balance"]
    assert bal_b == 1150, f"Expected 1150, got {bal_b}"
    print(f"[7/10] User B verified balance from PostgreSQL: {bal_b} Coins (1000 + 150) PASSED")

    # 8. Test constraint: Try sending more coins than available (e.g. 5,000)
    res_over = requests.post(f"{BASE_URL}/api/v1/payments/send", json={"receiver_identifier": user_b["user_code"], "amount": 5000}, headers=headers_a)
    assert res_over.status_code == 400, "Should reject overdraft payment"
    print(f"[8/10] Overdraft rejected as expected: {res_over.json()['detail']}")

    # 9. Test constraint: Try sending to oneself
    res_self = requests.post(f"{BASE_URL}/api/v1/payments/send", json={"receiver_identifier": user_a["user_code"], "amount": 50}, headers=headers_a)
    assert res_self.status_code == 400, "Should reject self transfer"
    print(f"[9/10] Self-payment rejected as expected: {res_self.json()['detail']}")

    # 10. Check transaction history for User A and User B
    res_hist_a = requests.get(f"{BASE_URL}/api/v1/transactions", headers=headers_a)
    assert res_hist_a.status_code == 200
    txns_a = res_hist_a.json()
    assert len(txns_a) >= 1
    assert txns_a[0]["type"] == "SENT"
    assert txns_a[0]["amount"] == 150

    res_hist_b = requests.get(f"{BASE_URL}/api/v1/transactions", headers=headers_b)
    assert res_hist_b.status_code == 200
    txns_b = res_hist_b.json()
    assert len(txns_b) >= 1
    assert txns_b[0]["type"] == "RECEIVED"
    assert txns_b[0]["amount"] == 150
    # 11. Test receipt lookup by ID and reference ID
    txn_ref = pay_data['reference_id']
    res_receipt_ref = requests.get(f"{BASE_URL}/api/v1/transactions/{txn_ref}", headers=headers_a)
    assert res_receipt_ref.status_code == 200
    receipt_data = res_receipt_ref.json()
    assert receipt_data["reference_id"] == txn_ref
    assert receipt_data["amount"] == 150
    assert receipt_data["type"] == "SENT"
    print(f"[11/11] Digital receipt lookup by reference ID ({txn_ref}) PASSED")

    print("\n>>> ALL 11 END-TO-END TESTS PASSED SUCCESSFULLY! <<<\n")


if __name__ == "__main__":
    test_full_flow()
