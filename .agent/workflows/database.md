---
description: Connect and run queries on TiDB Cloud database (hellohingoli)
---

# Database Connection Workflow

This workflow connects to the **TiDB Cloud** database for the HelloHingoli API.

## Database Details
- **Host:** gateway01.ap-southeast-1.prod.aws.tidbcloud.com
- **Port:** 4000
- **Database:** hellohingoli
- **User:** 39rSBGEWyaX8SaD.root

## Connection Command

// turbo
1. To connect interactively:
```powershell
C:\xampp\mysql\bin\mysql.exe -h gateway01.ap-southeast-1.prod.aws.tidbcloud.com -P 4000 -u "39rSBGEWyaX8SaD.root" -p"lOUBAGjTSM0SvHIt" --ssl hellohingoli
```

// turbo
2. To run a single query:
```powershell
C:\xampp\mysql\bin\mysql.exe -h gateway01.ap-southeast-1.prod.aws.tidbcloud.com -P 4000 -u "39rSBGEWyaX8SaD.root" -p"lOUBAGjTSM0SvHIt" --ssl hellohingoli -e "YOUR_SQL_QUERY_HERE"
```

// turbo
3. To run a SQL file:
```powershell
C:\xampp\mysql\bin\mysql.exe -h gateway01.ap-southeast-1.prod.aws.tidbcloud.com -P 4000 -u "39rSBGEWyaX8SaD.root" -p"lOUBAGjTSM0SvHIt" --ssl hellohingoli < "path\to\your\file.sql"
```

## Common Queries

// turbo
4. Show all tables:
```powershell
C:\xampp\mysql\bin\mysql.exe -h gateway01.ap-southeast-1.prod.aws.tidbcloud.com -P 4000 -u "39rSBGEWyaX8SaD.root" -p"lOUBAGjTSM0SvHIt" --ssl hellohingoli -e "SHOW TABLES;"
```

// turbo
5. Describe a table structure:
```powershell
C:\xampp\mysql\bin\mysql.exe -h gateway01.ap-southeast-1.prod.aws.tidbcloud.com -P 4000 -u "39rSBGEWyaX8SaD.root" -p"lOUBAGjTSM0SvHIt" --ssl hellohingoli -e "DESCRIBE table_name;"
```

// turbo
6. Count rows in a table:
```powershell
C:\xampp\mysql\bin\mysql.exe -h gateway01.ap-southeast-1.prod.aws.tidbcloud.com -P 4000 -u "39rSBGEWyaX8SaD.root" -p"lOUBAGjTSM0SvHIt" --ssl hellohingoli -e "SELECT COUNT(*) FROM table_name;"
```

## Important Notes
- Always use `--ssl` flag as TiDB Cloud requires SSL connections
- For complex queries, save them in a `.sql` file and run using method #3
- Database credentials are stored in `c:\Users\Meeting\Desktop\MH\apiv4\config\database.php`
