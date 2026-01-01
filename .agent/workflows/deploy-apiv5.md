---
description: Deploy API changes to apiv5 folder on Hostinger server via FTP
---

# Deploy APIv5 to Server

Use this workflow to upload changed API files to the development server at `https://hellohingoli.com/apiv5/`

## FTP Credentials
- **Host:** `ftp.hellohingoli.com`
- **Username:** `u122332128.a`
- **Password:** `Hostinger#293330`
- **Remote Path:** `/public_html/apiv5/`

## Quick Deploy Commands

### Upload Single File
```powershell
// turbo
curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\<FILENAME>" "ftp://ftp.hellohingoli.com/public_html/apiv5/<FILENAME>"
```

### Upload index.php (Main API File)
```powershell
// turbo
curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\index.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/index.php"
```

### Upload Config Files
```powershell
// turbo
curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\config\database.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/config/database.php"; curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\config\jwt.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/config/jwt.php"; curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\config\sms.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/config/sms.php"; curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\config\whatsapp.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/config/whatsapp.php"
```

### Upload Helper Files
```powershell
// turbo
curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\helpers\response.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/helpers/response.php"; curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\helpers\jwt.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/helpers/jwt.php"
```

### Upload All Core Files
```powershell
// turbo
curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\index.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/index.php"; curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\.htaccess" "ftp://ftp.hellohingoli.com/public_html/apiv5/.htaccess"; curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\config\database.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/config/database.php"; curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\config\jwt.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/config/jwt.php"; curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\config\sms.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/config/sms.php"; curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\config\whatsapp.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/config/whatsapp.php"; curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\helpers\response.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/helpers/response.php"; curl.exe -u "u122332128.a:Hostinger#293330" --ftp-create-dirs -T "apiv5\helpers\jwt.php" "ftp://ftp.hellohingoli.com/public_html/apiv5/helpers/jwt.php"
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
