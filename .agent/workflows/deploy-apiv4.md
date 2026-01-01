---
description: Deploy API changes to apiv4 folder on Hostinger server via FTP
---

# Deploy APIv4 to Server

Use this workflow to upload changed API files to the development server at `https://hellohingoli.com/apiv4/`

## FTP Credentials
- **Host:** `ftp.hellohingoli.com`
- **Username:** `u122332128.api`
- **Password:** `Hostinger#293330`
- **Remote Path:** `/` (root of FTP account = `/public_html/apiv4/`)

## Quick Deploy Commands

### Upload Single File
```powershell
// turbo
curl.exe -u "u122332128.api:Hostinger#293330" -T "apiv4\<FILENAME>" "ftp://ftp.hellohingoli.com/<FILENAME>"
```

### Upload index.php (Main API File)
```powershell
// turbo
curl.exe -u "u122332128.api:Hostinger#293330" -T "apiv4\index.php" "ftp://ftp.hellohingoli.com/index.php"
```

### Upload Config Files
```powershell
// turbo
curl.exe -u "u122332128.api:Hostinger#293330" --ftp-create-dirs -T "apiv4\config\database.php" "ftp://ftp.hellohingoli.com/config/database.php"
curl.exe -u "u122332128.api:Hostinger#293330" -T "apiv4\config\jwt.php" "ftp://ftp.hellohingoli.com/config/jwt.php"
curl.exe -u "u122332128.api:Hostinger#293330" -T "apiv4\config\sms.php" "ftp://ftp.hellohingoli.com/config/sms.php"
curl.exe -u "u122332128.api:Hostinger#293330" -T "apiv4\config\whatsapp.php" "ftp://ftp.hellohingoli.com/config/whatsapp.php"
```

### Upload Helper Files
```powershell
// turbo
curl.exe -u "u122332128.api:Hostinger#293330" --ftp-create-dirs -T "apiv4\helpers\response.php" "ftp://ftp.hellohingoli.com/helpers/response.php"
curl.exe -u "u122332128.api:Hostinger#293330" -T "apiv4\helpers\jwt.php" "ftp://ftp.hellohingoli.com/helpers/jwt.php"
```

### Upload All Core Files
```powershell
// turbo
curl.exe -u "u122332128.api:Hostinger#293330" -T "apiv4\index.php" "ftp://ftp.hellohingoli.com/index.php"
curl.exe -u "u122332128.api:Hostinger#293330" -T "apiv4\.htaccess" "ftp://ftp.hellohingoli.com/.htaccess"
```

## Verify Deployment
After uploading, test the API:
```powershell
// turbo
curl.exe "https://hellohingoli.com/apiv4/"
```

Expected response:
```json
{"success":true,"message":"Success","data":{"version":"1.0.0","name":"Hello Hingoli API"}}
```

## File Locations
- **Local:** `c:\Users\Meeting\Desktop\MH\apiv4\`
- **Remote:** `https://hellohingoli.com/apiv4/`

## Common Files to Deploy
| Local Path | Remote Path |
|------------|-------------|
| `apiv4\index.php` | `/index.php` |
| `apiv4\.htaccess` | `/.htaccess` |
| `apiv4\config\database.php` | `/config/database.php` |
| `apiv4\config\jwt.php` | `/config/jwt.php` |
| `apiv4\config\sms.php` | `/config/sms.php` |
| `apiv4\helpers\response.php` | `/helpers/response.php` |
| `apiv4\helpers\jwt.php` | `/helpers/jwt.php` |
