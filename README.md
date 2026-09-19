# Vendor Coin Pay

> **Simple, Instant Virtual Coin Payments for Vendors and Everyday Users**  
> *A complete, full-stack Android & Python FastAPI project built with Native Java, XML, and PostgreSQL.*

---

## 1. Project Overview

**Vendor Coin Pay** is a virtual coin payment application designed to make digital transactions effortlessly accessible for local shop vendors, students, and everyday users who find conventional UPI applications complicated.

### Key Philosophy
- **High Visibility**: High-contrast Dark Navy (`#0B132B`) and Gold (`#FFB703`) theme with large typography.
- **Zero Clutter**: Essential actions only — **Pay**, **Scan QR**, **My QR**, and **Recent Transactions**.
- **Real Database Source of Truth**: Absolutely no hardcoded mock data. Every user, balance, and transaction is authoritatively managed inside a real PostgreSQL database with atomic transfers (`SELECT ... FOR UPDATE`).
- **Integrated QR Ecosystem**: Instant camera QR code scanning and dynamic QR code generation (`vendorcoinpay://pay/VC-XXXXXX`).

---

## 2. Technology Stack

### Android Frontend
- **Language**: Java 17
- **UI & Layouts**: Native Android XML, Material Design Components 3
- **Networking**: Retrofit 2.11.0 + OkHttp 4.12.0 + Gson
- **QR Engine**: ZXing Android Embedded 4.3.0 + ZXing Core 3.5.3
- **Session & Caching**: SharedPreferences (stores JWT token & user ID; backend remains source of truth)
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14/15 (API 34/35)

### Backend API
- **Framework**: Python 3.13 + FastAPI
- **ASGI Server**: Uvicorn
- **ORM / Database Layer**: SQLAlchemy 2.0
- **Validation**: Pydantic v2
- **Security**: Passlib (Bcrypt hashing), PyJWT / Python-Jose (JWT Bearer Tokens)

### Database
- **Engine**: PostgreSQL 18 (Dedicated local cluster)
- **Database Name**: `vendorcoinpay`
- **Transactions**: Atomic transfers with row-level locks and database integrity constraints (`CHECK (balance >= 0)`).

---

## 3. Project Structure

```
c:\VCP\
├── VendorCoinPay.apk                <-- Pre-built, ready-to-install Android APK
├── README.md                        <-- Comprehensive documentation
│
├── backend/                         <-- Python FastAPI & PostgreSQL Backend
│   ├── app/
│   │   ├── __init__.py
│   │   ├── main.py                  <-- FastAPI app & middleware configuration
│   │   ├── database.py              <-- PostgreSQL engine & session setup
│   │   ├── models/                  <-- SQLAlchemy models (User, Wallet, Transaction)
│   │   ├── schemas/                 <-- Pydantic validation models
│   │   ├── routers/                 <-- Auth, Users, Wallet, Payments, Transactions, QR
│   │   ├── services/                <-- Atomic transfer payment service (row locking)
│   │   ├── auth/                    <-- Bcrypt password hashing & JWT handlers
│   │   └── utils/                   <-- Code generator (VC-XXXXXX, TXN-XXXXXXXX)
│   ├── requirements.txt
│   ├── .env                         <-- Database credentials & secrets
│   ├── run_server.py                <-- Server launcher script
│   └── test_end_to_end.py           <-- Automated full-flow test suite
│
└── android/                         <-- Native Android Studio Project
    ├── build.gradle                 <-- Root build script
    ├── settings.gradle
    ├── gradle.properties
    ├── gradlew.bat / gradlew
    └── app/
        ├── build.gradle             <-- App module dependencies & SDK config
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/vendorcoinpay/
            │   ├── AppConfig.java   <-- Server IP/URL configuration
            │   ├── MainActivity.java
            │   ├── api/             <-- ApiClient & ApiService (Retrofit)
            │   ├── models/          <-- Data classes
            │   ├── storage/         <-- SessionManager (SharedPreferences)
            │   ├── adapters/        <-- TransactionAdapter (RecyclerView)
            │   └── screens/         <-- Splash, Login, Register, Home, Pay, History, Profile, My QR, Scan QR
            └── res/
                ├── values/          <-- colors.xml, strings.xml, themes.xml
                ├── drawable/        <-- Vector icons & card drawables
                ├── layout/          <-- XML user interface layouts
                ├── menu/            <-- Bottom navigation menu
                └── xml/             <-- network_security_config.xml
```

---

## 4. Database Schema

### Users Table (`users`)
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | Integer | Primary Key, Auto-increment | Unique internal user ID |
| `user_code` | Varchar(32) | Unique, Indexed | Clean user identifier (e.g. `VC-764609`) |
| `full_name` | Varchar(120) | Not Null | User's real name |
| `username` | Varchar(64) | Unique, Indexed | Login username |
| `phone` | Varchar(20) | Unique, Indexed | Contact phone number |
| `hashed_password` | Varchar(255) | Not Null | Bcrypt hashed password |
| `is_active` | Boolean | Default True | Account status |
| `created_at` | Timestamp | Default UTC now | Registration date |

### Wallets Table (`wallets`)
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | Integer | Primary Key | Wallet ID |
| `user_id` | Integer | Foreign Key (`users.id`), Unique | 1-to-1 relationship with User |
| `balance` | Integer | Not Null, `CHECK (balance >= 0)` | Authoritative coin balance |
| `currency` | Varchar(16) | Default 'COIN' | Coin symbol |
| `updated_at` | Timestamp | Default UTC now | Last balance update |

### Transactions Table (`transactions`)
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | Integer | Primary Key | Transaction ID |
| `reference_id` | Varchar(40) | Unique, Indexed | Unique transaction ref (e.g. `TXN-3D2197D0B93E`) |
| `sender_id` | Integer | Foreign Key (`users.id`) | Wallet sending the coins |
| `receiver_id` | Integer | Foreign Key (`users.id`) | Wallet receiving the coins |
| `amount` | Integer | Not Null, `CHECK (amount > 0)` | Quantity of coins transferred |
| `status` | Varchar(20) | Default 'COMPLETED' | Status of transfer |
| `note` | Varchar(255) | Nullable | Optional transfer note |
| `created_at` | Timestamp | Indexed | Timestamp of transaction |

---

## 5. API Endpoints

### Authentication
- `POST /api/v1/auth/register` — Create a new user, initialize wallet with 1,000 welcome coins, return JWT token.
- `POST /api/v1/auth/login` — Authenticate using username/phone and password, return JWT token.

### User & Profile
- `GET /api/v1/users/me` — Get current logged-in user profile & live coin balance.
- `PUT /api/v1/users/me` — Update full name or phone number.
- `GET /api/v1/users/resolve/{identifier}` — Resolve receiver details by user code, username, or phone.

### Wallet & Payments
- `GET /api/v1/wallet/balance` — Get real-time authoritative balance from PostgreSQL.
- `POST /api/v1/payments/send` — Execute atomic transfer between sender and receiver.

### Transactions & QR
- `GET /api/v1/transactions` — Query transaction history with filter (`ALL`, `SENT`, `RECEIVED`).
- `GET /api/v1/transactions/{id}` — Query specific transaction details.
- `GET /api/v1/qr/my/payload` — Get current user's QR payload string (`vendorcoinpay://pay/{user_code}`).
- `GET /api/v1/qr/resolve/{identifier}` — Resolve QR code target to user profile.

---

## 6. How to Run the Backend

### Prerequisites
- Python 3.10+
- PostgreSQL 18

### Step 1: Install Python Dependencies
```bash
cd c:\VCP\backend
pip install -r requirements.txt
```

### Step 2: Start the Server
```bash
python run_server.py
```
*The script automatically ensures PostgreSQL is active, verifies the database, creates all tables, and starts the FastAPI server at `http://0.0.0.0:8000`.*

### Step 3: Run Automated Verification Tests
```bash
python test_end_to_end.py
```
This script tests registration, login, balance verification, atomic coin transfer, overdraft prevention, self-payment prevention, and transaction history.

---

## 7. Connecting Android to the Backend

### 1. Global Multi-Device Mode (Any Phone on 4G/5G/Wi-Fi)
The app is configured to connect through a secure, public Cloudflare HTTPS Tunnel:
```java
// In c:\VCP\android\app\src\main\java\com\vendorcoinpay\AppConfig.java
public static final String BASE_URL = "https://marsh-grid-chemical-conditional.trycloudflare.com/";
```
- **How to start the public backend**: Double-click `start_backend_public.bat` or run:
  ```bash
  cd c:\VCP\backend
  python run_server_with_tunnel.py
  ```
- **How to share the APK**: Send `c:\VCP\VendorCoinPay.apk` to any Android phone via WhatsApp, Google Drive, or USB. It will immediately connect to your PostgreSQL database from anywhere in the world!

### 2. Local Emulator Mode
To route directly inside an Android Studio emulator on the same PC:
```java
public static final String BASE_URL = "http://10.0.2.2:8000/";
```

### 3. Local Wi-Fi Mode
If both your PC and phone are on the exact same Wi-Fi router:
1. Find your PC's local IPv4 address by running `ipconfig` (e.g. `192.168.1.50`).
2. Set `BASE_URL = "http://192.168.1.50:8000/";` in `AppConfig.java`.
3. Rebuild the APK.

---

## 8. How to Build the Android APK

### Option A: Command Line (Gradle)
```bash
cd c:\VCP\android
.\gradlew assembleDebug
```
The output APK is generated at:
`c:\VCP\android\app\build\outputs\apk\debug\app-debug.apk`
A copy is also kept at:
`c:\VCP\VendorCoinPay.apk`

### Option B: Android Studio
1. Open Android Studio.
2. Select **Open an existing Android Studio project** and choose `c:\VCP\android`.
3. Wait for Gradle sync to complete.
4. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)** or click the **Run (Green Triangle)** button to run on an attached device or emulator.

---

## 9. Testing & Flow Walkthrough

1. **Launch App**: The Splash screen checks for an existing session. If none exists, it opens the Login/Register screen.
2. **Register User 1 (e.g. Ramesh)**:
   - Full Name: `Ramesh Sharma`
   - Username: `ramesh`
   - Phone: `9876543210`
   - Password: `Password123`
   - Immediately receives **1,000 Welcome Coins**!
3. **Open My QR**:
   - Displays Ramesh's crisp QR code containing `vendorcoinpay://pay/VC-XXXXXX`.
4. **Register User 2 (e.g. Amit)** on another device or by logging out and registering:
   - Full Name: `Amit Kumar`
   - Username: `amit`
   - Phone: `9876543211`
   - Password: `Password123`
5. **Send Payment**:
   - Go to the **Pay** tab.
   - Enter Ramesh's user code or scan his QR.
   - The verified receiver badge immediately shows "Ramesh Sharma ✓ Verified".
   - Enter `150` Coins (or tap the `+100` and `+50` quick chips).
   - Tap **PAY COINS**.
   - Review the confirmation dialog: "Pay 150 Coins to Ramesh Sharma (VC-XXXXXX)".
   - Tap **CONFIRM PAYMENT**.
   - Payment Successful! Amit's balance drops from 1,000 to 850 Coins.
6. **Check History**:
   - Amit's History tab displays: `-150 Coins | Paid to Ramesh Sharma`.
   - Logging in as Ramesh displays: `+150 Coins | Received from Amit Kumar` with a live balance of **1,150 Coins**.
