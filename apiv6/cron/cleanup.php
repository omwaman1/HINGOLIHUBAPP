<?php
/**
 * Database Cleanup Cron Job
 * 
 * Cleans up expired data from the database:
 * - Expired OTP verifications (older than 1 day)
 * - OTP send logs (older than 30 days)
 * - Old notification logs (older than 90 days)
 * 
 * Setup on Linux server:
 * crontab -e
 * Add: 0 3 * * * php /var/www/html/api/cron/cleanup.php >> /var/log/hellohingoli_cleanup.log 2>&1
 * 
 * Or call via URL with secret key:
 * https://hellohingoli.com/api/cron/cleanup.php?key=YOUR_SECRET_KEY
 */

// Security: Only allow CLI or with secret key
$isCliRequest = php_sapi_name() === 'cli';
$secretKey = 'hh_cron_secret_2024'; // Change this to a secure random string

if (!$isCliRequest) {
    // Web request - require secret key
    if (!isset($_GET['key']) || $_GET['key'] !== $secretKey) {
        http_response_code(403);
        die('Unauthorized');
    }
    header('Content-Type: application/json');
}

require_once __DIR__ . '/../config/database.php';

$results = [
    'success' => true,
    'timestamp' => date('Y-m-d H:i:s'),
    'cleaned' => []
];

try {
    $pdo = getDB();
    
    // 1. Delete expired OTP verifications (older than 1 day)
    $stmt = $pdo->prepare("DELETE FROM otp_verifications WHERE expires_at < NOW() - INTERVAL 1 DAY");
    $stmt->execute();
    $results['cleaned']['otp_verifications'] = $stmt->rowCount();
    
    // 2. Delete old OTP send logs (older than 30 days)
    $stmt = $pdo->prepare("DELETE FROM otp_send_logs WHERE sent_at < NOW() - INTERVAL 30 DAY");
    $stmt->execute();
    $results['cleaned']['otp_send_logs'] = $stmt->rowCount();
    
    // 3. Delete old notification logs (older than 90 days)
    $stmt = $pdo->prepare("DELETE FROM notification_logs WHERE created_at < NOW() - INTERVAL 90 DAY");
    $stmt->execute();
    $results['cleaned']['notification_logs'] = $stmt->rowCount();
    
    // 4. Delete old user notifications that are read (older than 60 days)
    $stmt = $pdo->prepare("DELETE FROM user_notifications WHERE is_read = 1 AND created_at < NOW() - INTERVAL 60 DAY");
    $stmt->execute();
    $results['cleaned']['user_notifications_read'] = $stmt->rowCount();
    
    // 5. Clean up abandoned cart items (older than 30 days, for non-logged-in or inactive users)
    $stmt = $pdo->prepare("DELETE FROM cart_items WHERE created_at < NOW() - INTERVAL 30 DAY");
    $stmt->execute();
    $results['cleaned']['old_cart_items'] = $stmt->rowCount();
    
    // 6. Delete expired user sessions
    $stmt = $pdo->prepare("DELETE FROM user_sessions WHERE expires_at < NOW()");
    $stmt->execute();
    $results['cleaned']['expired_sessions'] = $stmt->rowCount();
    
    $totalCleaned = array_sum($results['cleaned']);
    $results['total_rows_deleted'] = $totalCleaned;
    
    $message = sprintf(
        "[%s] Cleanup completed: %d rows deleted (OTP: %d, OTP Logs: %d, Notifications: %d, Read Notifs: %d, Carts: %d, Sessions: %d)",
        $results['timestamp'],
        $totalCleaned,
        $results['cleaned']['otp_verifications'],
        $results['cleaned']['otp_send_logs'],
        $results['cleaned']['notification_logs'],
        $results['cleaned']['user_notifications_read'],
        $results['cleaned']['old_cart_items'],
        $results['cleaned']['expired_sessions']
    );
    
    if ($isCliRequest) {
        echo $message . "\n";
    } else {
        echo json_encode($results, JSON_PRETTY_PRINT);
    }
    
    // Log to file
    error_log($message);
    
} catch (Exception $e) {
    $results['success'] = false;
    $results['error'] = $e->getMessage();
    
    error_log("Cleanup Error: " . $e->getMessage());
    
    if ($isCliRequest) {
        echo "ERROR: " . $e->getMessage() . "\n";
        exit(1);
    } else {
        http_response_code(500);
        echo json_encode($results);
    }
}
