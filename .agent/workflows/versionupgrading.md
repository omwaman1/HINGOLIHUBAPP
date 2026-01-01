---
description: Complete version upgrade process for API and Android app
---

# Version Upgrading Workflow

Use this workflow when upgrading to a new API version (e.g., apiv4 → apiv5, apiv5 → apiv6).

## Overview
This process involves:
1. Copying API files to new version folder
2. Creating deployment workflow for new API version
3. Updating Android app API base URL
4. Updating Android app version numbers
5. Deploying new API to server

---

## Step 1: Copy API Folder to New Version

Copy all files from current API version to new version folder:

```powershell
# Example: apiv5 → apiv6
Copy-Item -Path "apiv5" -Destination "apiv6" -Recurse
```

**Verify:** Check new folder exists with all files:
```powershell
Get-ChildItem -Path "apiv6" -Recurse | Measure-Object
```

---

## Step 2: Create Deploy Workflow for New API Version

Create new workflow file at `.agent/workflows/deploy-apiv<N>.md`

**File to create:** `.agent/workflows/deploy-apiv<N>.md`

Update these items in the workflow:
- Description: `Deploy API changes to apiv<N> folder on Hostinger server via FTP`
- All FTP remote paths: `/apiv<N>/...`
- All local paths: `apiv<N>\...`
- Verification URL: `https://hellohingoli.com/apiv<N>/`
- File locations table

---

## Step 3: Update Android App API Base URL

**File:** `app/app/build.gradle.kts`

**Location:** Lines ~41-56 (inside `buildTypes` block)

Update BOTH debug and release URLs:

```kotlin
buildTypes {
    debug {
        // Change this line:
        buildConfigField("String", "API_BASE_URL", "\"https://hellohingoli.com/apiv<N>/\"")
    }
    release {
        // Change this line:
        buildConfigField("String", "API_BASE_URL", "\"https://hellohingoli.com/apiv<N>/\"")
    }
}
```

---

## Step 4: Update Android App Version Numbers

**File:** `app/app/build.gradle.kts`

**Location:** Lines ~27-28 (inside `defaultConfig` block)

```kotlin
defaultConfig {
    // Update these:
    versionCode = <N>        // Increment by 1 (e.g., 5 → 6)
    versionName = "1.0.<N>"  // Match version code (e.g., "1.0.6")
}
```

---

## Step 5: Deploy New API to Server

Use the new deploy workflow to upload all files:

```powershell
// turbo
# Upload main API file
curl.exe -u "u122332128.api:Hostinger#293330" --ftp-create-dirs -T "apiv<N>\index.php" "ftp://ftp.hellohingoli.com/apiv<N>/index.php"

# Upload .htaccess
curl.exe -u "u122332128.api:Hostinger#293330" --ftp-create-dirs -T "apiv<N>\.htaccess" "ftp://ftp.hellohingoli.com/apiv<N>/.htaccess"

# Upload config files
curl.exe -u "u122332128.api:Hostinger#293330" --ftp-create-dirs -T "apiv<N>\config\database.php" "ftp://ftp.hellohingoli.com/apiv<N>/config/database.php"
curl.exe -u "u122332128.api:Hostinger#293330" --ftp-create-dirs -T "apiv<N>\config\jwt.php" "ftp://ftp.hellohingoli.com/apiv<N>/config/jwt.php"
curl.exe -u "u122332128.api:Hostinger#293330" --ftp-create-dirs -T "apiv<N>\config\sms.php" "ftp://ftp.hellohingoli.com/apiv<N>/config/sms.php"
curl.exe -u "u122332128.api:Hostinger#293330" --ftp-create-dirs -T "apiv<N>\config\whatsapp.php" "ftp://ftp.hellohingoli.com/apiv<N>/config/whatsapp.php"

# Upload helper files
curl.exe -u "u122332128.api:Hostinger#293330" --ftp-create-dirs -T "apiv<N>\helpers\response.php" "ftp://ftp.hellohingoli.com/apiv<N>/helpers/response.php"
curl.exe -u "u122332128.api:Hostinger#293330" --ftp-create-dirs -T "apiv<N>\helpers\jwt.php" "ftp://ftp.hellohingoli.com/apiv<N>/helpers/jwt.php"
```

---

## Step 6: Verify API Deployment

Test the new API endpoint:

```powershell
// turbo
curl.exe "https://hellohingoli.com/apiv<N>/"
```

**Expected response:**
```json
{"success":true,"message":"Success","data":{"version":"1.0.0","name":"Hello Hingoli API"}}
```

---

## Step 7: Rebuild Android App

Open Android Studio and build the app with new version:
- Debug build for testing
- Release build for Play Store

---

## Quick Reference: Files to Modify

| File | What to Change |
|------|----------------|
| `apiv<N>/` folder | Create by copying previous version |
| `.agent/workflows/deploy-apiv<N>.md` | Create new deploy workflow |
| `app/app/build.gradle.kts` | `API_BASE_URL` (debug & release) |
| `app/app/build.gradle.kts` | `versionCode` and `versionName` |

---

## Example: Upgrading from v5 to v6

1. `Copy-Item -Path "apiv5" -Destination "apiv6" -Recurse`
2. Create `.agent/workflows/deploy-apiv6.md`
3. Change `apiv5` → `apiv6` in `build.gradle.kts` API URLs
4. Change `versionCode = 5` → `versionCode = 6`
5. Change `versionName = "1.0.5"` → `versionName = "1.0.6"`
6. Deploy apiv6 to server using `/deploy-apiv6`
7. Rebuild Android app
