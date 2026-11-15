# UNIQUE CHAT INDIA 🇮🇳
### Complete Real Android Online Voice Chat Room, Messaging & Multiplayer Gaming Application

**UNIQUE CHAT INDIA** ek complete, modern aur real Android social voice lounge application hai jo Kotlin aur Jetpack Compose (Material 3) me bani hai. Isme Google Sign-In, Cloud Firestore backend, Live 8-Seat Voice Chat Rooms, 1-to-1 Private Chat, Multiplayer Games (Ludo, Chess, Tic Tac Toe, Quiz) aur ek secure Owner Admin Panel shamil hai.

---

## 1. Important Application Credentials & Contacts
- **App Name**: UNIQUE CHAT INDIA
- **Customer Support Hotline**: `9219240590`
- **Owner Login Email**: `arman902661@gmail.com`
- **Firestore Database ID**: `ai-studio-android-uniquech-5075769b-f611-434b-9209-69bdc52d2481`
- **Google Cloud / Firebase Project**: `gen-lang-client-0103782268`

---

## 2. Key Features Implemented

### 🎙️ 1. Live Voice Chat Rooms
- **8-Seat Stage**: Real-time microphone seats with animated sound wave halo when a member speaks.
- **Seat Management**: Join seat, leave seat, mute/unmute hardware microphone, and speakerphone switch.
- **Room Types**: Public, Private, and Password-protected rooms.
- **In-Room Live Chat**: Real-time text messages and virtual gifts.
- **Virtual Gifts & Coins**: Rose (🌹), Masala Chai (☕), Golden Crown (👑), Diya (🪔), Supercar (🏎️), India Fireworks (🎆), and Koh-i-Noor Diamond (💎).
- **Owner & Moderator Controls**: Remove user from seat, ban user from room, edit room announcements.

### 💬 2. Private 1-to-1 Messaging System
- Direct private messaging between members.
- Real-time Firestore snapshot synchronization.
- Read receipts (single check / double blue check).
- User blocking and violation reporting.

### 🎮 3. Multiplayer Gaming Arena
- **Tic Tac Toe**: Real 3x3 interactive board with turn switching, win line detection, and draw handling.
- **Desi Trivia Quiz**: Interactive multiple-choice Indian culture, Bollywood, history, and cricket quiz with instant scores.
- **Ludo Rush**: Dice roll (1..6) with step track movement and winner determination.
- **Rapid Chess Tactics**: Interactive board piece movement and captures.

### 🛡️ 4. Secure Owner Admin Dashboard
- **Owner Access Gate**: Email `arman902661@gmail.com` aur `owner`/`admin` role verification.
- **Live Metrics**: Total users, currently online members, active voice rooms, and pending reports.
- **User Management**: Search users by unique ID (e.g. `UCI-10928`), ban/unban accounts, and grant virtual coins.
- **Room Moderation**: Delete inappropriate rooms with a single tap.
- **Violation Reports**: Inspect and resolve reports filed by members.
- **Global Broadcast**: Update app-wide announcement banner and toggle maintenance mode.

---

## 3. Simple Step-by-Step Hindi Guide for Beginners

### 🌐 Step 1: Firebase Console Setup (Kaha kya click karein)
1. Browser me **https://console.firebase.google.com/** open karein.
2. Apne Google account (`arman902661@gmail.com`) se login karein.
3. Project select karein: `gen-lang-client-0103782268` (ya apna project).
4. **Authentication** tab par click karein:
   - **Sign-in method** par click karein.
   - **Google** ko enable karein aur **Save** karein.
5. **Firestore Database** tab par click karein:
   - Database ID `ai-studio-android-uniquech-5075769b-f611-434b-9209-69bdc52d2481` active hai aur security rules deploy ho chuki hain.

---

### 🎙️ Step 2: Real Voice RTC (Agora ya LiveKit) Setup
Real voice audio ke liye app me Android `AudioManager` and `VoiceRtcService` integrated hai:
1. Agar aap Agora RTC use karna chahte hain:
   - Website kholein: **https://www.agora.io/**
   - Free account banayein aur **Create Project** par click karein.
   - **App ID** aur **App Certificate** copy karein.
2. Agar aap LiveKit Cloud use karna chahte hain:
   - Website kholein: **https://livekit.io/**
   - Free project banayein aur **API Key** / **Secret** prapt karein.
3. Apni App ID ko AI Studio ke **Secrets panel** me daalein.

---

### 📱 Step 3: Android Phone me App Kaise Chalayein aur APK Kaise Banayein
1. **Android Studio** me project open karein.
2. Top menu me **Build** par click karein.
3. **Build Bundle(s) / APK(s)** -> **Build APK(s)** par click karein.
4. Kuch seconds baad bottom-right notification me **locate** par click karein.
5. Aapko `app-debug.apk` milega. Is file ko WhatsApp, Google Drive, ya USB cable ke zariye apne Android phone me bhejein aur install karein!

---

### 🚀 Step 4: Google Play Store Release Preparation
1. Android Studio me **Build** -> **Generate Signed Bundle / APK** par click karein.
2. **Android App Bundle (.aab)** select karein aur **Next** karein.
3. Apna Keystore select karein aur password daalein.
4. **release** build type chunein aur **Create** par click karein.
5. **https://play.google.com/console** par jayein, naya app banayein, `release.aab` upload karein aur publish karein.
