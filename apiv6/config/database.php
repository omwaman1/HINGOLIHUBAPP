<?php
/**
 * TiDB Cloud Database Configuration
 */

define('DB_HOST', 'gateway01.ap-southeast-1.prod.aws.tidbcloud.com');
define('DB_PORT', '4000');
define('DB_NAME', 'hellohingoli');
define('DB_USER', '39rSBGEWyaX8SaD.root');
define('DB_PASS', 'lOUBAGjTSM0SvHIt');
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
            // Ensure proper UTF-8 handling for Marathi/Devanagari characters
            $pdo->exec("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (PDOException $e) {
            error_log("Database connection failed: " . $e->getMessage());
            http_response_code(500);
            echo json_encode([
                'success' => false,
                'message' => 'Database connection failed',
                'debug_error' => $e->getMessage()
            ], JSON_UNESCAPED_UNICODE);
            exit;
        }
    }
    
    return $pdo;
}
