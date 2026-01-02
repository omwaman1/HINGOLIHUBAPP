<?php
ob_start(); // Enable output buffering for header redirects
require_once 'auth_check.php';
require_once '../config/database.php'; // Path from apiv4/admin/ to apiv4/config/

// Get Database Connection
$db = getDB();

// Include Helper Functions
require_once 'includes/functions.php';

// Determine Page
$page = $_GET['page'] ?? 'dashboard';
$tempPage = $page; // For active nav highlighting

// Validate Allowed Pages
$allowedPages = ['dashboard', 'listings', 'listing_form', 'products', 'product_form', 'old_products', 'old_product_form', 'users', 'user_form', 'categories', 'category_form', 'banners', 'banner_form', 'reels', 'reel_form', 'cities', 'city_form', 'reviews', 'orders', 'order_detail', 'settings', 'enquiries', 'notifications', 'favorites', 'pincodes', 'moderation', 'subcategories', 'analytics', 'export', 'sms_logs', 'app_version'];
if (!in_array($page, $allowedPages)) {
    $page = 'dashboard';
}

// Handle AJAX requests BEFORE including header (to avoid HTML in JSON response)
$isAjax = !empty($_SERVER['HTTP_X_REQUESTED_WITH']) && strtolower($_SERVER['HTTP_X_REQUESTED_WITH']) === 'xmlhttprequest';
$isAjaxUpload = isset($_POST['ajax_upload']); // For file uploads that don't set X-Requested-With
if (($isAjax || $isAjaxUpload) && $_SERVER['REQUEST_METHOD'] === 'POST') {
    require_once "pages/{$page}.php";
    exit;
}

// Page Title mapping
$titles = [
    'dashboard' => 'Dashboard',
    'listings' => 'Manage Listings',
    'products' => 'Manage Shop Products',
    'old_products' => 'Manage Old Products',
    'users' => 'Manage Users',
    'categories' => 'Manage Categories',
    'banners' => 'Manage Banners'
];

$pageTitle = $titles[$page] ?? ucwords(str_replace('_', ' ', $page));

// Include Header
require_once 'includes/header.php';

// Include Page Content
$pageFile = "pages/{$page}.php";
if (file_exists($pageFile)) {
    require_once $pageFile;
} else {
    echo "<div class='card'><h2>Page not found</h2><p>The page <strong>{$page}</strong> does not exist yet.</p></div>";
}

// Include Footer
require_once 'includes/footer.php';
?>
