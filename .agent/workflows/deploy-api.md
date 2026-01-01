---
description: How to deploy PHP API changes to the server
---

## Steps

1. Open FileZilla or your preferred FTP client
2. Connect to your Hostinger server
3. Navigate to `public_html/api/`
4. Upload the modified files from `c:\Users\Meeting\Desktop\MH\api\`
   - Most common: `index.php`
5. Test the endpoint in browser:
   - `https://hellohingoli.com/api/` - should return version info
   - `https://hellohingoli.com/api/prefetch` - returns all prefetch data

## Common Files to Deploy
- `index.php` - Main API router and handlers
- `config/database.php` - Database configuration
- `helpers/*.php` - Utility functions
