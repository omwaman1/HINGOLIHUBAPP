---
description: Deploy API changes to apiv6 folder on Hostinger server via FTP
---

# Deploy APIv6 to Server

Use this workflow to upload changed API files to the development server at `https://hellohingoli.com/apiv6/`

## FTP Credentials
- **Host:** `ftp.hellohingoli.com`
- **Username:** `u122332128.apiv6`
- **Password:** `Hostinger#293330`
- **Remote Path:** `/` (FTP root is already apiv6 folder)

## Quick Deploy Commands

### Upload Single File
```powershell
// turbo
curl.exe -u "u122332128.apiv6:Hostinger#293330" -T "apiv6\<FILENAME>" "ftp://ftp.hellohingoli.com/<FILENAME>"
```

### Upload index.php (Main API File)
```powershell
// turbo
curl.exe -u "u122332128.apiv6:Hostinger#293330" -T "apiv6\index.php" "ftp://ftp.hellohingoli.com/"
```

### Upload Helper Files (jwt.php, response.php)
```powershell
// turbo
curl.exe -u "u122332128.apiv6:Hostinger#293330" -T "apiv6\helpers\jwt.php" "ftp://ftp.hellohingoli.com/helpers/jwt.php"; curl.exe -u "u122332128.apiv6:Hostinger#293330" -T "apiv6\helpers\response.php" "ftp://ftp.hellohingoli.com/helpers/response.php"
```

### Upload Config Files
```powershell
// turbo
curl.exe -u "u122332128.apiv6:Hostinger#293330" -T "apiv6\config\database.php" "ftp://ftp.hellohingoli.com/config/database.php"; curl.exe -u "u122332128.apiv6:Hostinger#293330" -T "apiv6\config\jwt.php" "ftp://ftp.hellohingoli.com/config/jwt.php"; curl.exe -u "u122332128.apiv6:Hostinger#293330" -T "apiv6\config\sms.php" "ftp://ftp.hellohingoli.com/config/sms.php"; curl.exe -u "u122332128.apiv6:Hostinger#293330" -T "apiv6\config\whatsapp.php" "ftp://ftp.hellohingoli.com/config/whatsapp.php"
```

### Upload All Core Files (index.php + helpers + .htaccess)
```powershell
// turbo
curl.exe -u "u122332128.apiv6:Hostinger#293330" -T "apiv6\index.php" "ftp://ftp.hellohingoli.com/index.php"; curl.exe -u "u122332128.apiv6:Hostinger#293330" -T "apiv6\.htaccess" "ftp://ftp.hellohingoli.com/.htaccess"; curl.exe -u "u122332128.apiv6:Hostinger#293330" -T "apiv6\helpers\jwt.php" "ftp://ftp.hellohingoli.com/helpers/jwt.php"; curl.exe -u "u122332128.apiv6:Hostinger#293330" -T "apiv6\helpers\response.php" "ftp://ftp.hellohingoli.com/helpers/response.php"
```

## Verify Deployment
After uploading, test the API:
```powershell
// turbo
curl.exe "https://hellohingoli.com/apiv6/"
```

Expected response:
```json
{"success":true,"message":"Success","data":{"version":"1.0.0","name":"Hello Hingoli API"}}
```

## File Locations
- **Local:** `c:\Users\Meeting\Desktop\MH\apiv6\`
- **Remote:** `https://hellohingoli.com/apiv6/`
