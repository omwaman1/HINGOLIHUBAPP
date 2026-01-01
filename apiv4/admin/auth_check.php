<?php
session_start();

// Valid Credentials (Hardcoded for now as requested)
define('ADMIN_USER', 'admin');
define('ADMIN_PASS', 'admin123');

// Check if logged in
if (!isset($_SESSION['admin_logged_in']) || $_SESSION['admin_logged_in'] !== true) {
    // Redirect to login page
    header("Location: login.php");
    exit;
}
?>
