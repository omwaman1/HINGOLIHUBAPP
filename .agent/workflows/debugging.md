---
description: Common debugging steps for Hingoli Hub app issues
---

## API Issues

### Check API Response
```powershell
curl https://hellohingoli.com/api/prefetch
```

### View API Logs
Check Hostinger error logs in cPanel → Error Log

## Android App Issues

### Clear App Data
Settings → Apps → Hingoli Hub → Clear Data

### View All Logs
```powershell
adb logcat | Select-String "hingoli.hub"
```

### View Specific Logs
```powershell
# API calls
adb logcat -s "okhttp.OkHttpClient"

# SharedDataRepository
adb logcat -s "SharedDataRepository"

# Crashes
adb logcat "*:E" | Select-String "hingoli.hub"
```

## Build Issues

### Clean Gradle Cache
```powershell
cd c:\Users\Meeting\Desktop\MH\app
.\gradlew clean
```

### Sync Gradle
In Android Studio: File → Sync Project with Gradle Files

## Database Issues

### Check TiDB Connection
Test in PHP script or via MySQL client using config from `config/database.php`
