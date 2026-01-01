---
description: Deploy API changes to apiv5 folder on Hostinger server via FTP
---

# Deploy APIv5 to Server

Use this workflow to upload changed API files to the development server at `https://hellohingoli.com/apiv5/`

## FTP Credentials
- **Host:** `ftp.hellohingoli.com`
- **Username:** `u122332128.apiv5`
- **Password:** `Hostinger#293330`
- **Remote Path:** `/` (FTP root is already apiv5 folder)

## Quick Deploy Commands

### Upload Single File
```powershell
// turbo
curl.exe -u "u122332128.apiv5:Hostinger#293330" -T "apiv5\<FILENAME>" "ftp://ftp.hellohingoli.com/<FILENAME>"
```

### Upload index.php (Main API File)
```powershell
// turbo
curl.exe -u "u122332128.apiv5:Hostinger#293330" -T "apiv5\index.php" "ftp://ftp.hellohingoli.com/index.php"
```

### Upload Helper Files (jwt.php, response.php)
```powershell
// turbo
curl.exe -u "u122332128.apiv5:Hostinger#293330" -T "apiv5\helpers\jwt.php" "ftp://ftp.hellohingoli.com/helpers/jwt.php"; curl.exe -u "u122332128.apiv5:Hostinger#293330" -T "apiv5\helpers\response.php" "ftp://ftp.hellohingoli.com/helpers/response.php"
```

### Upload Config Files
```powershell
// turbo
curl.exe -u "u122332128.apiv5:Hostinger#293330" -T "apiv5\config\database.php" "ftp://ftp.hellohingoli.com/config/database.php"; curl.exe -u "u122332128.apiv5:Hostinger#293330" -T "apiv5\config\jwt.php" "ftp://ftp.hellohingoli.com/config/jwt.php"; curl.exe -u "u122332128.apiv5:Hostinger#293330" -T "apiv5\config\sms.php" "ftp://ftp.hellohingoli.com/config/sms.php"; curl.exe -u "u122332128.apiv5:Hostinger#293330" -T "apiv5\config\whatsapp.php" "ftp://ftp.hellohingoli.com/config/whatsapp.php"
```

### Upload All Core Files (index.php + helpers + .htaccess)
```powershell
// turbo
curl.exe -u "u122332128.apiv5:Hostinger#293330" -T "apiv5\index.php" "ftp://ftp.hellohingoli.com/index.php"; curl.exe -u "u122332128.apiv5:Hostinger#293330" -T "apiv5\.htaccess" "ftp://ftp.hellohingoli.com/.htaccess"; curl.exe -u "u122332128.apiv5:Hostinger#293330" -T "apiv5\helpers\jwt.php" "ftp://ftp.hellohingoli.com/helpers/jwt.php"; curl.exe -u "u122332128.apiv5:Hostinger#293330" -T "apiv5\helpers\response.php" "ftp://ftp.hellohingoli.com/helpers/response.php"
```

## Verify Deployment
After uploading, test the API:
```powershell
// turbo
curl.exe "https://hellohingoli.com/apiv5/"
```

Expected response:
```json
{"success":true,"message":"Success","data":{"version":"1.0.0","name":"Hello Hingoli API"}}
```

## File Locations
- **Local:** `c:\Users\Meeting\Desktop\MH\apiv5\`
- **Remote:** `https://hellohingoli.com/apiv5/`
