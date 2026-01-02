<?php
/**
 * TiDB Cloud Database Configuration
 * 
 * INSTRUCTIONS:
 * 1. Copy this file to database.php
 * 2. Replace the placeholder values with your actual credentials
 * 3. NEVER commit database.php to GitHub!
 */

define('DB_HOST', 'your-database-host.com');
define('DB_PORT', '4000');
define('DB_NAME', 'your_database_name');
define('DB_USER', 'your_username');
define('DB_PASS', 'your_password');
define('DB_CHARSET', 'utf8mb4');

/**
 * Get PDO database connection with SSL for TiDB Cloud
 */
function getDB(): PDO {
    static $pdo = null;
    
    if ($pdo === null) {
        $dsn = "mysql:host=" . DB_HOST . ";port=" . DB_PORT . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
        
        $options = [
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES   => false,
            PDO::ATTR_TIMEOUT            => 10,
            // TiDB Cloud SSL settings
            PDO::MYSQL_ATTR_SSL_CA       => true,
            PDO::MYSQL_ATTR_SSL_VERIFY_SERVER_CERT => false,
        ];
        
        try {
            $pdo = new PDO($dsn, DB_USER, DB_PASS, $options);
        } catch (PDOException $e) {
            error_log("Database connection failed: " . $e->getMessage());
            http_response_code(500);
            echo json_encode([
                'success' => false,
                'message' => 'Database connection failed',
                'debug_error' => $e->getMessage()
            ]);
            exit;
        }
    }
    
    return $pdo;
}
