---
description: How to build and test the Android app in Android Studio
---

## Development Environment

- **IDE**: Android Studio (build and run directly from IDE)
- **Testing**: Run on connected device/emulator via Android Studio

## Build & Run (Android Studio)

1. Open project: `c:\Users\Meeting\Desktop\MH\app` in Android Studio
2. Wait for Gradle sync to complete
3. Click **Run** (green play button) or press `Shift+F10`

## Debug Logging

The app uses specific log tags for easy filtering in Logcat:

### Cache & Data Flow
```
CacheDebug          // Cache expiry, refresh, and data loading
SharedDataRepository // All repository operations
```

### Filter in Logcat
```
tag:CacheDebug
```
Or combine multiple:
```
tag:CacheDebug | tag:SharedDataRepository
```

### API Calls
```
tag:okhttp.OkHttpClient
```

### Delete Operations
```
tag:DebugDelete
```

## Full Rebuild (if needed)

In Android Studio: **Build → Clean Project**, then **Build → Rebuild Project**

Or via terminal:
```powershell
cd c:\Users\Meeting\Desktop\MH\app
.\gradlew clean assembleDebug
```
