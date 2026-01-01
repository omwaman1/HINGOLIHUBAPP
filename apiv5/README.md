# Hingoli Hub API

PHP REST API for the Hingoli Hub (हिंगोली हब) marketplace app.

**Website:** https://hellohingoli.com  
**API Base URL:** https://hellohingoli.com/api

## 📁 Folder Structure

```
api/
├── config/
│   ├── database.php    # TiDB Cloud connection (pre-configured)
│   └── jwt.php         # JWT settings
├── helpers/
│   ├── jwt.php         # JWT token functions
│   └── response.php    # Response helpers
├── .htaccess           # URL rewriting rules
├── index.php           # Main router
├── test_users.sql      # Test users for development
└── README.md           # This file
```

## 🚀 Installation on Hostinger

### Step 1: Upload API Files

1. Login to Hostinger hPanel
2. Go to **File Manager**
3. Navigate to `public_html`
4. Create a folder called `api`
5. Upload all files from this `api` folder to `public_html/api`

### Step 2: Create Test Users in Database

Run this SQL in your TiDB Cloud console to create test users:

```sql
INSERT INTO users (user_id, username, email, phone, password_hash, is_verified, is_active, created_at)
VALUES (
    1,
    'testuser',
    'test@hellohingoli.com',
    '9876543210',
    '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    1,
    1,
    NOW()
)
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash);

INSERT INTO users (user_id, username, email, phone, password_hash, is_verified, is_active, created_at)
VALUES (
    2,
    'demo',
    'demo@hellohingoli.com',
    '1234567890',
    '$2y$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B77UdFm',
    1,
    1,
    NOW()
)
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash);
```

## 🔑 Test Credentials

| Username    | Phone       | Password  |
|-------------|-------------|-----------|
| testuser    | 9876543210  | password  |
| demo        | 1234567890  | secret    |

## 📡 API Endpoints

### Base URL
```
https://hellohingoli.com/api
```

### Authentication

#### POST /auth/login
Login with phone and password.

**Request:**
```json
{
    "phone": "9876543210",
    "password": "password"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Login successful",
    "data": {
        "access_token": "eyJ...",
        "refresh_token": "eyJ...",
        "expires_in": 3600,
        "token_type": "Bearer",
        "user": {
            "user_id": 1,
            "username": "testuser",
            "phone": "9876543210"
        }
    }
}
```

#### POST /auth/refresh
Refresh access token using refresh token.

**Headers:**
```
Authorization: Bearer <refresh_token>
```

---

### Categories

#### GET /categories
Get all categories.

**Query Parameters:**
- `type` - Filter by listing type: `services`, `business`, `selling`, `jobs`
- `parent_id` - Filter by parent category (use `0` for top-level)

**Example:**
```
GET https://hellohingoli.com/api/categories?type=services
```

#### GET /categories/{id}
Get category by ID.

#### GET /categories/{id}/subcategories
Get subcategories of a category.

---

### Listings

#### GET /listings
Get listings with filters.

**Query Parameters:**
- `type` - Filter by listing type
- `category_id` - Filter by category
- `subcategory_id` - Filter by subcategory
- `city` - Filter by city
- `search` - Search in title/description
- `page` - Page number (default: 1)
- `per_page` - Items per page (default: 20, max: 50)

**Example:**
```
GET https://hellohingoli.com/api/listings?type=services&city=Hingoli&page=1
```

#### GET /listings/{id}
Get listing details by ID.

#### GET /listings/{id}/price-list
Get price list for a listing.

#### GET /listings/{id}/reviews
Get reviews for a listing.

---

## 📱 Android Configuration

Update your Android app's base URL in `NetworkModule.kt`:

```kotlin
private const val BASE_URL = "https://hellohingoli.com/api/"
```

## 🔒 Database Configuration (Pre-configured)

The API is already configured to connect to TiDB Cloud:
- **Host:** gateway01.ap-southeast-1.prod.aws.tidbcloud.com
- **Port:** 4000
- **Database:** hellohingoli
- **SSL:** Enabled (required for TiDB Serverless)

## 🐛 Troubleshooting

### 500 Internal Server Error
- Check Hostinger error logs in hPanel
- Verify TiDB Cloud is accessible from Hostinger

### 404 Not Found
- Ensure `.htaccess` is uploaded
- Check if mod_rewrite is enabled

### CORS Errors
- The API allows all origins by default
- Update `CORS_ORIGIN` in `config/jwt.php` if needed

### Database Connection Issues
- TiDB Cloud requires SSL - this is already configured
- Check if your IP is whitelisted in TiDB Cloud (usually not needed for serverless)
