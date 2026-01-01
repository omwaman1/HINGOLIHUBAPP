---
description: Project file index for quick reference and navigation
---

# Hingoli Hub Project File Index

## API (apiv5/)
- `index.php` - Main API router and endpoints
- `reel.php` - Standalone reel landing page
- `.htaccess` - URL rewriting rules
- `config/database.php` - Database connection configuration
- `routes/` - Modular route handlers
- `helpers/` - Utility functions
- `admin/` - Admin panel pages
- `admin/pages/reels.php` - Reels management
- `admin/pages/reel_form.php` - Reel add/edit form

## Android App (app/)
- `app/src/main/java/com/hingoli/hub/`
  - `data/model/Reel.kt` - Reel data model
  - `ui/reels/ReelsScreen.kt` - Reels UI screen
  - `ui/reels/ReelsViewModel.kt` - Reels business logic
  - `data/api/ApiService.kt` - API interface
  - `data/repository/` - Data repositories

## Scripts (scripts/)
- Various utility and maintenance scripts

## Website (website/)
- Static website files

## Delivery App (delivery-app/)
- Delivery partner application

## Firebase Functions (firebase-functions/)
- Cloud functions for notifications, etc.

## Root Files
- `bulk_upload_reels.php` - Bulk reel upload utility
- `upload_reel.php` - Single reel upload
- `reels_urls.txt` - Reel video URLs list
