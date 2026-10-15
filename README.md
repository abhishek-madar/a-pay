# A pay (a-pay)

A modern, high-security international payment processing and financial intelligence Android application built with **Jetpack Compose** and **Material Design 3**.

---

## 🌟 Key Features

- **Rupee-First Global Currency Engine**:
  - Benchmark figures formatted in Indian Rupees (₹).
  - Multi-currency support across **USD ($)**, **EUR (€)**, **GBP (£)**, **AED (د.إ)**, **JPY (¥)**, **SGD (S$)**, and **CAD (C$)** with live exchange rates.
  - Interactive multi-currency converter and dynamic currency switcher.

- **Real-Time CameraX QR Code Scanner**:
  - Integrated with **Android CameraX** (`camera2`, `lifecycle`, `view`, `core`) and ZXing.
  - Viewfinder with animated laser sweep, flashlight/torch toggle, and lens switcher.
  - Supports UPI QR codes (`upi://pay`), JSON digital invoices, and merchant codes.

- **Automated Regional Tax Engine**:
  - **India GST**: 18% (CGST 9% + SGST 9% or IGST), 12%, and 28% slabs.
  - **US State Sales Tax**: California (7.25%), New York (8.875%).
  - **European Union VAT**: Germany (19%), France (20%).
  - **United Kingdom**: HMRC VAT (20%).
  - **United Arab Emirates**: FTA VAT (5%).
  - **Japan**: Consumption Tax (10%) and **Singapore**: GST (9%).
  - Cross-border export exemption support (0%).

- **Payment Gateways & Banking Integrations**:
  - **Stripe Connect**: Credit/Debit Cards, Apple Pay, SEPA, ACH.
  - **PayPal Express**: Global wallets, Venmo, Pay Later.
  - **Razorpay International**: UPI, NetBanking, and RuPay card processing.
  - **Direct Bank Wire / SWIFT**: RTGS, NEFT, and international SWIFT transfers.
  - **Connected Accounts**: Corporate checking & treasury accounts with real-time sync.

- **End-to-End Cryptography & Security**:
  - **Hardware-backed AES-256-GCM** encryption for transaction payloads.
  - **SHA-256 Cryptographic Hash** generated for every ledger transaction.
  - **Two-Factor Authentication (2FA)** with live TOTP code generation (RFC 6238).
  - **Biometric Authentication** prompt support (Fingerprint / Face Unlock).
  - **Role-Based Access Control (RBAC)**: Admin, Finance Manager, Auditor, and Cashier roles.

- **Financial Analytics & Reporting**:
  - Interactive **Monthly Spending & Cash Flow Curve** (smooth Canvas bezier area chart).
  - **Expense Category Distribution** (custom Donut Chart).
  - **Budget Calculator & Limit Alert System** (visual progress bars with 80% & 100% threshold alarms).
  - **Custom Data Export**: Bookkeeping CSV, Monthly Tax Reports, and Executive PDF summaries.

- **Dark Mode & Eye-Strain Reduction**:
  - Sleek obsidian-emerald dark theme with instant light mode toggle.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose (BOM 2024.09.00) with Material Design 3 (M3)
- **Architecture**: Clean MVVM (Model-View-ViewModel) with Kotlin Coroutines & StateFlow
- **Camera & Scanning**: AndroidX CameraX (1.5.0) + ZXing Core (3.5.3)
- **Design System**: Centralized `Theme.kt`, dynamic color scheme, responsive WindowInsets
- **Testing**: Robolectric + JUnit4 unit test suite

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug / Meerkat or later
- JDK 17 or higher
- Android SDK 36 (minSdk: 24, targetSdk: 36)

### Steps
```bash
# Clone the repository
git clone https://github.com/abhishek-madar/a-pay.git
cd a-pay

# Build debug APK
./gradlew assembleDebug

# Run unit and Robolectric tests
./gradlew testDebugUnitTest
```

---

## 📄 License
This project is licensed under the Apache 2.0 License.
