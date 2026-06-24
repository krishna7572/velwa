# 🎧 Velwa — Next-Gen Bluetooth Manager

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen?style=for-the-badge&logo=android"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge&logo=kotlin"/>
  <img src="https://img.shields.io/badge/Min%20SDK-26-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge"/>
</p>

> **Velwa** ek next-generation Bluetooth device manager hai Android ke liye — dark neon UI, multiple device support, volume control, auto-connect, aur bahut kuch.

---

## ✨ Features

| Feature | Details |
|---|---|
| 📡 **Bluetooth Scan** | Nearby devices dhundho real-time mein |
| 🔗 **Multi-Device Connect** | Multiple devices ek saath manage karo |
| 🔊 **Volume Control** | Har device ka awaaz kam/zyada karo seekbar se |
| 🔇 **Mute Toggle** | Ek tap mein mute/unmute |
| ⭐ **Favorites** | Apne important devices pin karo |
| 🔋 **Battery Level** | Supported devices ki battery dekhte raho |
| ✏️ **Rename Device** | Apna custom naam do device ko |
| ⚡ **Auto-Connect** | Boot pe automatically connect ho jao |
| 📱 **Paired Import** | Already paired devices ko ek tap mein import karo |
| 🗑️ **Remove Device** | Unwanted devices hatao |
| 💾 **Persistent Storage** | Room DB — data save rehta hai |
| 🌑 **Dark Neon UI** | Next-gen glassmorphism dark theme |

---

## 📱 Screenshots

> _App install karo aur dekho!_

---

## 🚀 Setup & Build

### Requirements
- Android Studio Hedgehog ya uske baad
- JDK 17+
- Android device / emulator (API 26+)

### Steps

```bash
# 1. Repo clone karo
git clone https://github.com/YOUR_USERNAME/velwa.git
cd velwa

# 2. Android Studio mein open karo
# File → Open → velwa folder select karo

# 3. Gradle sync hone do

# 4. Run karo (Shift+F10)
```

### Permissions (Auto-managed)
App khud permissions maangti hai:
- `BLUETOOTH_SCAN` — devices dhundhne ke liye
- `BLUETOOTH_CONNECT` — connect karne ke liye
- `ACCESS_FINE_LOCATION` — scan ke liye (Android 11 aur neeche)

---

## 🏗️ Project Structure

```
velwa/
├── app/src/main/
│   ├── java/com/velwa/app/
│   │   ├── data/
│   │   │   ├── bluetooth/
│   │   │   │   ├── VelwaBluetoothManager.kt   ← Core BT logic
│   │   │   │   ├── BluetoothReceiver.kt        ← BroadcastReceiver
│   │   │   │   └── BluetoothService.kt         ← Foreground service
│   │   │   └── models/
│   │   │       ├── VelwaDevice.kt              ← Data model
│   │   │       └── VelwaDatabase.kt            ← Room DB + DAO
│   │   ├── ui/
│   │   │   ├── screens/
│   │   │   │   ├── SplashActivity.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── DeviceDetailActivity.kt     ← Volume + controls
│   │   │   │   └── SettingsActivity.kt
│   │   │   └── components/
│   │   │       ├── Fragments.kt                ← 3 tab fragments
│   │   │       └── DeviceAdapter.kt            ← RecyclerView adapter
│   │   └── viewmodel/
│   │       └── MainViewModel.kt
│   └── res/
│       ├── layout/          ← All XML layouts
│       ├── drawable/        ← Vector icons
│       └── values/          ← Colors, themes, strings
```

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM
- **UI**: Material Design 3 + Custom Dark Theme
- **Database**: Room (SQLite)
- **Bluetooth**: Android Bluetooth API (Classic + BLE)
- **Async**: Kotlin Coroutines + LiveData + StateFlow
- **Navigation**: ViewPager2 + Bottom Navigation

---

## 📋 Roadmap

- [ ] EQ (Equalizer) control
- [ ] Widget for quick volume
- [ ] BLE device support (full)
- [ ] Device grouping
- [ ] Connect/disconnect shortcuts
- [ ] Material You dynamic colors

---

## 🤝 Contribute

PR welcome hai bhai! Fork karo, branch banao, PR bhejo.

---

## 📄 License

MIT License — freely use karo.

---

<p align="center">Made with ❤️ by Krishna</p>
