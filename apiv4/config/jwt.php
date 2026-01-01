<?php
/**
 * JWT Configuration
 * IMPORTANT: Change the JWT_SECRET to a strong random string in production!
 */

define('JWT_SECRET', 'hH1ng0l1-s3cr3t-k3y-2024-pr0duct10n-v3ry-l0ng-@nd-r@nd0m');
define('JWT_ALGORITHM', 'HS256');
define('JWT_ACCESS_EXPIRY', 3600);        // 1 hour in seconds
define('JWT_REFRESH_EXPIRY', 2592000);    // 30 days in seconds
define('JWT_ISSUER', 'hellohingoli.com');

// CORS configuration - allow requests from your domain and app
define('CORS_ORIGIN', '*');  // Use '*' for development, restrict in production
