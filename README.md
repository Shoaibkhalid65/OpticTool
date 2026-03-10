# Optical Tool 🔭
### Professional Business Management App for Optical Shops

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Kotlin-0095D5?style=flat&logo=kotlin&logoColor=white)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat&logo=android&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-brightgreen)
![Status](https://img.shields.io/badge/Status-Production%20Delivered-success)

---

## 📽️ Demo



https://github.com/user-attachments/assets/19f4c3ba-86a9-4d3d-93c7-0f68fa7eeed7



---

## 📖 Overview

**Optical Tool** is a production-ready native Android application built for optical shops and optometry professionals. It covers end-to-end business operations — from prescription calculations and invoice generation to inventory notes and shop analytics — all with full **offline-first** support.

Built entirely in **Kotlin + Jetpack Compose**, delivered as a freelance client project.

---

## ✨ Features

### 🔐 Authentication
- Google OAuth one-click sign-in
- Email & password login with OTP verification
- Secure password recovery flow
- Session persistence via Supabase Auth

### 🧮 Optical Prescription Calculator
- Input SPH, CYL, AXIS, ADD values with real-time validation
- Intelligent CYL/AXIS dependency checks
- Prescription transposition between formats
- Screenshot & share calculated results
- Archive and retrieve past prescriptions

### 🧾 Bill Book Management
- Auto-numbered invoice generation
- Attach prescription images to bills
- Payment status tracking (paid/unpaid) with visual indicators
- Sales analytics dashboard (total sales, outstanding amounts)
- PDF export, native printing, and WhatsApp sharing
- Advanced search & filter by date, status, and customer
- Pickup date scheduling

### 📋 Prescription Management
- Digital storage with image attachments
- Image compression optimized to ~250KB without quality loss
- Auto-generated sequential prescription numbers
- Quick search and browse

### 📓 Glasses Notebook
- Inventory tracking for glasses stock
- Notes and business documentation

### 🏪 Shop Profile & Branding
- Configure shop name, address, and contact details
- Custom bill footer and terms
- Currency symbol customization
- Professional branding on all generated invoices

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 100% |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture + Repository Pattern |
| DI | Dagger Hilt |
| Local DB | Room Database |
| Backend | Supabase (Auth + Cloud Storage) |
| Async | Kotlin Coroutines + Flow |
| Navigation | Jetpack Navigation Compose |
| PDF | Native Android PDF Libraries |
| Auth | Google OAuth 2.0 + Email OTP |
| Splash | Android 12+ Splash Screen API |

---

## 🏗️ Architecture

```
opticaltool/
├── data/
│   ├── auth/               # Authentication logic
│   ├── local/              # Room DAOs & Database
│   ├── models/             # Prescription, Bill, Notebook models
│   └── repository/         # Repository interfaces & implementations
├── di/                     # Hilt modules
├── navigation/             # Nav graph & route definitions
├── ui/
│   ├── auth/               # Login, Signup, Password Recovery
│   ├── calculator/         # Prescription Calculator
│   ├── prescriptioncreation/
│   ├── myprescriptions/
│   ├── billcreation/
│   ├── mybills/
│   ├── notebook/
│   ├── home/
│   ├── profile/
│   ├── shopdashboard/
│   ├── splash/
│   └── theme/              # Material Design 3 Theme
└── utils/                  # PDF, Image compression, Date utilities
```

---

## 📊 Project Stats

| Metric | Value |
|---|---|
| Kotlin Files | 61 |
| Screens | 13+ |
| Modules | Auth, Calculator, Prescriptions, Bills, Notebook, Profile |
| DB Tables | Users, Prescriptions, Bills, Notebook Entries |
| Utility Classes | 9+ |
| Project Type | Freelance / Client Delivery |
| Team Size | Solo Developer |

---

## 🔧 Key Technical Challenges Solved

**1. Custom Image Compression**
Built a compression pipeline that reduces prescription photo size to ~250KB (85% reduction) while preserving readability — critical for cloud storage costs and upload speed.

**2. Offline-First Architecture**
Designed a dual-layer data strategy: Room handles all local operations while Supabase syncs in the background. The app functions 100% without internet.

**3. Complex Prescription Validation**
Implemented business-rule validation (CYL requires AXIS, dropdown initialization at 0.00, etc.) that mirrors real optometry workflows.

**4. PDF Generation & Printing**
Custom PDF templates with shop branding using native Android APIs, including direct print support and WhatsApp sharing.

---

## 📈 Impact

- ✅ 60% reduction in prescription processing time
- ✅ Eliminated manual billing errors through automation
- ✅ 100% offline functionality — zero downtime for the business
- ✅ Production delivered to client, actively in use

---

## 📄 Legal
[Privacy Policy](./PRIVACY_POLICY.md) • [License](./LICENSE)


## 📫 Contact

**Muhammad Shoaib Khalid**
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0077B5?style=flat&logo=linkedin)](https://linkedin.com/in/muhammad-shoaib-khalid-864502297)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-181717?style=flat&logo=github)](https://github.com/shoaibkhalid65)

