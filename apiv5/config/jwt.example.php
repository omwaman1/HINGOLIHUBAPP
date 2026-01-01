<?php
/**
 * JWT Configuration
 * 
 * INSTRUCTIONS:
 * 1. Copy this file to jwt.php
 * 2. Replace the placeholder values with your actual secret key
 * 3. NEVER commit jwt.php to GitHub!
 */

define('JWT_SECRET', 'your-super-secret-jwt-key-here-make-it-long-and-random');
define('JWT_EXPIRY', 86400 * 30); // 30 days
