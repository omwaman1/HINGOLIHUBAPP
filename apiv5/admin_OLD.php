<?php
// VERSION: 2024-12-17-v5 (Fixed Redirects)
require_once 'config/database.php';

// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

$db = getDB();
$message = '';
$error = '';

// Handle success message from redirects
if (isset($_GET['msg'])) {
    $message = htmlspecialchars($_GET['msg']);
}

// ========== FILE UPLOAD CONFIGURATION ==========
define('UPLOAD_DIR', __DIR__ . '/uploads/');
define('UPLOAD_URL', 'https://hellohingoli.com/api/uploads/');
define('MAX_FILE_SIZE', 5 * 1024 * 1024); // 5MB
define('ALLOWED_TYPES', ['image/jpeg', 'image/png', 'image/webp', 'image/gif']);

// Create upload directories if they don't exist
if (!file_exists(UPLOAD_DIR)) @mkdir(UPLOAD_DIR, 0755, true);
if (!file_exists(UPLOAD_DIR . 'listings/')) @mkdir(UPLOAD_DIR . 'listings/', 0755, true);
if (!file_exists(UPLOAD_DIR . 'categories/')) @mkdir(UPLOAD_DIR . 'categories/', 0755, true);
if (!file_exists(UPLOAD_DIR . 'banners/')) @mkdir(UPLOAD_DIR . 'banners/', 0755, true);

/**
 * Handle file upload to Cloudflare R2
 */
function uploadImage($file, $folder = 'listings') {
    if (!isset($file) || $file['error'] === UPLOAD_ERR_NO_FILE) {
        return null;
    }
    
    if ($file['error'] !== UPLOAD_ERR_OK) {
        throw new Exception("Upload error code: " . $file['error']);
    }
    
    if ($file['size'] > MAX_FILE_SIZE) {
        throw new Exception("File too large. Maximum size is 5MB.");
    }
    
    $finfo = finfo_open(FILEINFO_MIME_TYPE);
    $mimeType = finfo_file($finfo, $file['tmp_name']);
    finfo_close($finfo);
    
    if (!in_array($mimeType, ALLOWED_TYPES)) {
        throw new Exception("Invalid file type. Only JPG, PNG, WebP, GIF allowed.");
    }
    
    $ext = pathinfo($file['name'], PATHINFO_EXTENSION);
    $filename = $folder . '/' . uniqid() . '_' . time() . '.' . strtolower($ext);
    $fileContent = file_get_contents($file['tmp_name']);
    
    // R2 Configuration
    $r2Config = [
        'endpoint' => 'https://62b435cd6e08605f2c7c1aadedc6a591.r2.cloudflarestorage.com',
        'accessKeyId' => '6d12f3c5c7a0b68722e46063c8befec4',
        'secretAccessKey' => 'fd01dd18c77b8de4ccbf036b4dfafdaa062c5a32685dc8b482ac40d0a9d50d60',
        'bucket' => 'hello-hingoli-bucket',
        'publicUrl' => 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev'
    ];
    
    // Upload to R2
    $result = uploadToR2($r2Config, $filename, $fileContent, $mimeType);
    
    if (!$result['success']) {
        throw new Exception("R2 Upload failed: " . $result['error']);
    }
    
    return $r2Config['publicUrl'] . '/' . $filename;
}

/**
 * Handle multiple image upload
 */
function uploadMultipleImages($files, $folder = 'listings') {
    $urls = [];
    
    if (!isset($files['name']) || !is_array($files['name'])) {
        return $urls;
    }
    
    for ($i = 0; $i < count($files['name']); $i++) {
        if ($files['error'][$i] === UPLOAD_ERR_NO_FILE) continue;
        
        $file = [
            'name' => $files['name'][$i],
            'type' => $files['type'][$i],
            'tmp_name' => $files['tmp_name'][$i],
            'error' => $files['error'][$i],
            'size' => $files['size'][$i]
        ];
        
        $url = uploadImage($file, $folder);
        if ($url) $urls[] = $url;
    }
    
    return $urls;
}

/**
 * Upload file to Cloudflare R2 (S3-compatible)
 */
function uploadToR2($config, $key, $content, $contentType) {
    $endpoint = $config['endpoint'];
    $accessKeyId = $config['accessKeyId'];
    $secretAccessKey = $config['secretAccessKey'];
    $bucket = $config['bucket'];
    $region = 'auto'; // R2 uses 'auto' for region
    
    $host = parse_url($endpoint, PHP_URL_HOST);
    $url = "{$endpoint}/{$bucket}/{$key}";
    
    $date = gmdate('Ymd\THis\Z');
    $shortDate = gmdate('Ymd');
    $contentHash = hash('sha256', $content);
    
    // Canonical request
    $headers = [
        'host' => $host,
        'x-amz-content-sha256' => $contentHash,
        'x-amz-date' => $date,
        'content-type' => $contentType
    ];
    ksort($headers);
    
    $signedHeaders = implode(';', array_keys($headers));
    $canonicalHeaders = '';
    foreach ($headers as $k => $v) {
        $canonicalHeaders .= "{$k}:{$v}\n";
    }
    
    $canonicalRequest = "PUT\n/{$bucket}/{$key}\n\n{$canonicalHeaders}\n{$signedHeaders}\n{$contentHash}";
    $canonicalRequestHash = hash('sha256', $canonicalRequest);
    
    // String to sign
    $scope = "{$shortDate}/{$region}/s3/aws4_request";
    $stringToSign = "AWS4-HMAC-SHA256\n{$date}\n{$scope}\n{$canonicalRequestHash}";
    
    // Signing key
    $kDate = hash_hmac('sha256', $shortDate, "AWS4{$secretAccessKey}", true);
    $kRegion = hash_hmac('sha256', $region, $kDate, true);
    $kService = hash_hmac('sha256', 's3', $kRegion, true);
    $kSigning = hash_hmac('sha256', 'aws4_request', $kService, true);
    $signature = hash_hmac('sha256', $stringToSign, $kSigning);
    
    // Authorization header
    $authHeader = "AWS4-HMAC-SHA256 Credential={$accessKeyId}/{$scope}, SignedHeaders={$signedHeaders}, Signature={$signature}";
    
    // Make request
    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_CUSTOMREQUEST => 'PUT',
        CURLOPT_POSTFIELDS => $content,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_HTTPHEADER => [
            "Authorization: {$authHeader}",
            "Content-Type: {$contentType}",
            "Host: {$host}",
            "x-amz-content-sha256: {$contentHash}",
            "x-amz-date: {$date}"
        ]
    ]);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error = curl_error($ch);
    curl_close($ch);
    
    if ($httpCode >= 200 && $httpCode < 300) {
        return ['success' => true, 'url' => $url];
    }
    
    return ['success' => false, 'error' => $error ?: "HTTP {$httpCode}: {$response}"];
}

// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                // ========== CATEGORY ACTIONS ==========
                case 'add_category':
                    $name = $_POST['name'];
                    $slug = strtolower(trim(preg_replace('/[^A-Za-z0-9-]+/', '-', $name)));
                    $image_url = $_POST['image_url'];
                    $listing_type = $_POST['listing_type'];
                    $stmt = $db->prepare("INSERT INTO categories (name, slug, listing_type, parent_id, image_url) VALUES (?, ?, ?, NULL, ?)");
                    $stmt->execute([$name, $slug, $listing_type, $image_url]);
                    $message = "Category added successfully!";
                    break;

                case 'add_subcategory':
                    $name = $_POST['name'];
                    $slug = strtolower(trim(preg_replace('/[^A-Za-z0-9-]+/', '-', $name)));
                    $parent_id = $_POST['parent_id'];
                    $image_url = $_POST['image_url'];
                    
                    $stmt = $db->prepare("SELECT listing_type FROM categories WHERE category_id = ?");
                    $stmt->execute([$parent_id]);
                    $parent = $stmt->fetch();
                    $listing_type = $parent['listing_type'];

                    $stmt = $db->prepare("INSERT INTO categories (name, slug, listing_type, parent_id, image_url, depth) VALUES (?, ?, ?, ?, ?, 1)");
                    $stmt->execute([$name, $slug, $listing_type, $parent_id, $image_url]);
                    $message = "Subcategory added successfully!";
                    break;

                case 'edit_category':
                    $id = $_POST['category_id'];
                    $name = $_POST['name'];
                    $slug = strtolower(trim(preg_replace('/[^A-Za-z0-9-]+/', '-', $name)));
                    $image_url = $_POST['image_url'] ?? '';
                    $stmt = $db->prepare("UPDATE categories SET name = ?, slug = ?, image_url = ? WHERE category_id = ?");
                    $stmt->execute([$name, $slug, $image_url, $id]);
                    $message = "Category updated!";
                    break;

                case 'delete_category':
                    $id = $_POST['category_id'];
                    $stmt = $db->prepare("DELETE FROM categories WHERE category_id = ?");
                    $stmt->execute([$id]);
                    $message = "Category deleted successfully!";
                    break;

                // ========== LISTING ACTIONS ==========
                case 'add_listing':
                    $title = $_POST['title'];
                    $description = $_POST['description'];
                    $listing_type = $_POST['listing_type'];
                    $category_id = $_POST['category_id'];
                    $subcategory_id = !empty($_POST['subcategory_id']) ? $_POST['subcategory_id'] : null;
                    $price = !empty($_POST['price']) ? $_POST['price'] : null;
                    $location = $_POST['location'];
                    $city = $_POST['city'];
                    $state = $_POST['state'] ?? 'Maharashtra';
                    $user_id = $_POST['user_id'] ?? 1;
                    $status = $_POST['status'] ?? 'active';
                    $is_verified = isset($_POST['is_verified']) ? 1 : 0;
                    $is_featured = isset($_POST['is_featured']) ? 1 : 0;
                    $latitude = !empty($_POST['latitude']) ? (float)$_POST['latitude'] : null;
                    $longitude = !empty($_POST['longitude']) ? (float)$_POST['longitude'] : null;
                    
                    // Handle main image upload
                    $main_image_url = null;
                    if (!empty($_POST['main_image_url'])) {
                        $main_image_url = $_POST['main_image_url'];
                    } elseif (isset($_FILES['main_image'])) {
                        $main_image_url = uploadImage($_FILES['main_image'], 'listings');
                    }
                    
                    $stmt = $db->prepare("INSERT INTO listings 
                        (listing_type, title, description, price, category_id, subcategory_id, location, city, state, latitude, longitude, main_image_url, user_id, status, is_verified, is_featured) 
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    $stmt->execute([$listing_type, $title, $description, $price, $category_id, $subcategory_id, $location, $city, $state, $latitude, $longitude, $main_image_url, $user_id, $status, $is_verified, $is_featured]);
                    $listing_id = $db->lastInsertId();
                    
                    // Handle gallery images upload
                    if (isset($_FILES['gallery_images'])) {
                        $galleryUrls = uploadMultipleImages($_FILES['gallery_images'], 'listings');
                        foreach ($galleryUrls as $index => $url) {
                            $stmt = $db->prepare("INSERT INTO listing_images (listing_id, image_url, sort_order) VALUES (?, ?, ?)");
                            $stmt->execute([$listing_id, $url, $index]);
                        }
                    }
                    
                    // Add type-specific data
                    if ($listing_type === 'services' && !empty($_POST['experience_years'])) {
                        $stmt = $db->prepare("INSERT INTO services_listings (listing_id, experience_years) VALUES (?, ?)");
                        $stmt->execute([$listing_id, $_POST['experience_years']]);
                    } elseif ($listing_type === 'jobs') {
                        $salary_min = !empty($_POST['salary_min']) ? $_POST['salary_min'] : null;
                        $salary_max = !empty($_POST['salary_max']) ? $_POST['salary_max'] : null;
                        $salary_period = $_POST['salary_period'] ?? 'monthly';
                        $employment_type = $_POST['employment_type'] ?? 'full_time';
                        $remote_option = $_POST['remote_option'] ?? 'on_site';
                        $vacancies = !empty($_POST['vacancies']) ? $_POST['vacancies'] : 1;
                        $education = $_POST['education_required'] ?? null;
                        $experience = !empty($_POST['experience_required']) ? $_POST['experience_required'] : 0;
                        
                        $stmt = $db->prepare("INSERT INTO job_listings 
                            (listing_id, job_title, employment_type, salary_min, salary_max, salary_period, experience_required_years, education_required, remote_option, vacancies) 
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                        $stmt->execute([$listing_id, $title, $employment_type, $salary_min, $salary_max, $salary_period, $experience, $education, $remote_option, $vacancies]);
                    } elseif ($listing_type === 'business') {
                        $business_name = $_POST['business_name'] ?? $title;
                        $industry = $_POST['industry'] ?? null;
                        $established_year = !empty($_POST['established_year']) ? $_POST['established_year'] : null;
                        $employee_count = $_POST['employee_count'] ?? null;
                        
                        $stmt = $db->prepare("INSERT INTO business_listings (listing_id, business_name, industry, established_year, employee_count) VALUES (?, ?, ?, ?, ?)");
                        $stmt->execute([$listing_id, $business_name, $industry, $established_year, $employee_count]);
                    }
                    
                    $message = "Listing added successfully! (ID: $listing_id)";
                    break;

                case 'edit_listing':
                    $listing_id = $_POST['listing_id'];
                    $title = $_POST['title'];
                    $description = $_POST['description'];
                    $price = !empty($_POST['price']) ? $_POST['price'] : null;
                    $location = $_POST['location'];
                    $city = $_POST['city'];
                    $category_id = $_POST['category_id'];
                    $subcategory_id = !empty($_POST['subcategory_id']) ? $_POST['subcategory_id'] : null;
                    $status = $_POST['status'];
                    $is_verified = isset($_POST['is_verified']) ? 1 : 0;
                    $is_featured = isset($_POST['is_featured']) ? 1 : 0;
                    $latitude = !empty($_POST['latitude']) ? (float)$_POST['latitude'] : null;
                    $longitude = !empty($_POST['longitude']) ? (float)$_POST['longitude'] : null;
                    
                    // Handle main image upload
                    $main_image_url = $_POST['existing_image'] ?? null;
                    if (!empty($_POST['main_image_url'])) {
                        $main_image_url = $_POST['main_image_url'];
                    } elseif (isset($_FILES['main_image']) && $_FILES['main_image']['error'] === UPLOAD_ERR_OK) {
                        $main_image_url = uploadImage($_FILES['main_image'], 'listings');
                    }
                    
                    // Get listing type
                    $stmt = $db->prepare("SELECT listing_type FROM listings WHERE listing_id = ?");
                    $stmt->execute([$listing_id]);
                    $listing_type = $stmt->fetchColumn();
                    
                    $stmt = $db->prepare("UPDATE listings SET 
                        title = ?, description = ?, price = ?, location = ?, city = ?, 
                        category_id = ?, subcategory_id = ?, main_image_url = ?, status = ?, 
                        is_verified = ?, is_featured = ?, latitude = ?, longitude = ?, updated_at = NOW()
                        WHERE listing_id = ?");
                    $stmt->execute([$title, $description, $price, $location, $city, $category_id, $subcategory_id, $main_image_url, $status, $is_verified, $is_featured, $latitude, $longitude, $listing_id]);
                    
                    // Update type-specific data
                    if ($listing_type === 'services') {
                        $exp = !empty($_POST['experience_years']) ? $_POST['experience_years'] : 0;
                        $db->prepare("DELETE FROM services_listings WHERE listing_id = ?")->execute([$listing_id]);
                        $db->prepare("INSERT INTO services_listings (listing_id, experience_years) VALUES (?, ?)")->execute([$listing_id, $exp]);
                    } elseif ($listing_type === 'jobs') {
                        $db->prepare("DELETE FROM job_listings WHERE listing_id = ?")->execute([$listing_id]);
                        $stmt = $db->prepare("INSERT INTO job_listings (listing_id, job_title, employment_type, salary_min, salary_max, salary_period, experience_required_years, education_required, remote_option, vacancies) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                        $stmt->execute([
                            $listing_id, $title, 
                            $_POST['employment_type'] ?? 'full_time',
                            !empty($_POST['salary_min']) ? $_POST['salary_min'] : null,
                            !empty($_POST['salary_max']) ? $_POST['salary_max'] : null,
                            $_POST['salary_period'] ?? 'monthly',
                            !empty($_POST['experience_required']) ? $_POST['experience_required'] : 0,
                            $_POST['education_required'] ?? null,
                            $_POST['remote_option'] ?? 'on_site',
                            !empty($_POST['vacancies']) ? $_POST['vacancies'] : 1
                        ]);
                    } elseif ($listing_type === 'business') {
                        $db->prepare("DELETE FROM business_listings WHERE listing_id = ?")->execute([$listing_id]);
                        $stmt = $db->prepare("INSERT INTO business_listings (listing_id, business_name, industry, established_year, employee_count) VALUES (?, ?, ?, ?, ?)");
                        $stmt->execute([
                            $listing_id,
                            $_POST['business_name'] ?? $title,
                            $_POST['industry'] ?? null,
                            !empty($_POST['established_year']) ? $_POST['established_year'] : null,
                            $_POST['employee_count'] ?? null
                        ]);
                    }
                    
                    $message = "Listing updated successfully!";
                    // Redirect back to edit page to show updated values
                    header("Location: ?tab=listings&edit=$listing_id&msg=" . urlencode($message));
                    exit;
                
                case 'approve_listing':
                    $id = $_POST['listing_id'];
                    $db->prepare("UPDATE listings SET status = 'active', is_verified = 1 WHERE listing_id = ?")->execute([$id]);
                    $message = "Listing approved!";
                    break;
                
                case 'reject_listing':
                    $id = $_POST['listing_id'];
                    $reason = $_POST['rejection_reason'] ?? 'Did not meet guidelines';
                    $db->prepare("UPDATE listings SET status = 'rejected' WHERE listing_id = ?")->execute([$id]);
                    $message = "Listing rejected!";
                    break;
                
                case 'approve_product':
                    $productId = $_POST['product_id'];
                    $db->prepare("UPDATE shop_products SET is_active = 1 WHERE product_id = ?")->execute([$productId]);
                    $message = "Product approved and is now visible!";
                    break;
                
                case 'reject_product':
                    $productId = $_POST['product_id'];
                    // Delete the product as rejected
                    $db->prepare("DELETE FROM shop_products WHERE product_id = ?")->execute([$productId]);
                    $message = "Product rejected and removed!";
                    break;
                
                case 'save_auto_moderation':
                    // Save products auto-moderation setting
                    $autoModProducts = isset($_POST['auto_moderation_products']) ? 'true' : 'false';
                    $stmt = $db->prepare("INSERT INTO settings (setting_key, setting_value) VALUES ('auto_moderation_products', ?) 
                                          ON DUPLICATE KEY UPDATE setting_value = ?");
                    $stmt->execute([$autoModProducts, $autoModProducts]);
                    
                    // Save listings auto-moderation setting
                    $autoModListings = isset($_POST['auto_moderation_listings']) ? 'true' : 'false';
                    $stmt = $db->prepare("INSERT INTO settings (setting_key, setting_value) VALUES ('auto_moderation_listings', ?) 
                                          ON DUPLICATE KEY UPDATE setting_value = ?");
                    $stmt->execute([$autoModListings, $autoModListings]);
                    
                    $message = "Auto-moderation settings saved!";
                    break;
                
                case 'add_product':
                    $productName = $_POST['product_name'];
                    $description = $_POST['description'] ?? '';
                    $categoryId = (int)$_POST['category_id'];
                    $subcategoryId = !empty($_POST['subcategory_id']) ? (int)$_POST['subcategory_id'] : null;
                    $price = !empty($_POST['price']) ? (float)$_POST['price'] : 0;
                    $discountedPrice = !empty($_POST['discounted_price']) ? (float)$_POST['discounted_price'] : null;
                    $stockQty = !empty($_POST['stock_qty']) ? (int)$_POST['stock_qty'] : 1;
                    $minQty = !empty($_POST['min_qty']) ? (int)$_POST['min_qty'] : 1;
                    $condition = $_POST['condition'] ?? 'new';
                    $sellOnline = isset($_POST['sell_online']) ? 1 : 0;
                    $isActive = isset($_POST['is_active']) ? 1 : 0;
                    $listingId = (int)$_POST['listing_id']; // Business listing ID
                    
                    // Handle image upload
                    $imageUrl = null;
                    if (isset($_FILES['product_image']) && $_FILES['product_image']['error'] === UPLOAD_ERR_OK) {
                        $imageUrl = uploadImage($_FILES['product_image'], 'products');
                    } elseif (!empty($_POST['image_url'])) {
                        $imageUrl = $_POST['image_url'];
                    }
                    
                    $stmt = $db->prepare("
                        INSERT INTO shop_products (listing_id, product_name, description, category_id, subcategory_id, price, discounted_price, stock_qty, min_qty, `condition`, sell_online, is_active, image_url)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ");
                    $stmt->execute([$listingId, $productName, $description, $categoryId, $subcategoryId, $price, $discountedPrice, $stockQty, $minQty, $condition, $sellOnline, $isActive, $imageUrl]);
                    $message = "Product added successfully!";
                    break;
                
                case 'edit_product':
                    $productId = $_POST['product_id'];
                    $productName = $_POST['product_name'];
                    $description = $_POST['description'] ?? '';
                    $categoryId = (int)$_POST['category_id'];
                    $subcategoryId = !empty($_POST['subcategory_id']) ? (int)$_POST['subcategory_id'] : null;
                    $price = !empty($_POST['price']) ? (float)$_POST['price'] : 0;
                    $discountedPrice = !empty($_POST['discounted_price']) ? (float)$_POST['discounted_price'] : null;
                    $stockQty = !empty($_POST['stock_qty']) ? (int)$_POST['stock_qty'] : 1;
                    $minQty = !empty($_POST['min_qty']) ? (int)$_POST['min_qty'] : 1;
                    $condition = $_POST['condition'] ?? 'new';
                    $sellOnline = isset($_POST['sell_online']) ? 1 : 0;
                    $isActive = isset($_POST['is_active']) ? 1 : 0;
                    
                    // Handle image
                    $imageUrl = $_POST['existing_image'] ?? null;
                    if (isset($_FILES['product_image']) && $_FILES['product_image']['error'] === UPLOAD_ERR_OK) {
                        $imageUrl = uploadImage($_FILES['product_image'], 'products');
                    } elseif (!empty($_POST['image_url'])) {
                        $imageUrl = $_POST['image_url'];
                    }
                    
                    $stmt = $db->prepare("
                        UPDATE shop_products SET product_name = ?, description = ?, category_id = ?, subcategory_id = ?, 
                        price = ?, discounted_price = ?, stock_qty = ?, min_qty = ?, `condition` = ?, sell_online = ?, is_active = ?, image_url = ?
                        WHERE product_id = ?
                    ");
                    $stmt->execute([$productName, $description, $categoryId, $subcategoryId, $price, $discountedPrice, $stockQty, $minQty, $condition, $sellOnline, $isActive, $imageUrl, $productId]);
                    $message = "Product updated successfully!";
                    break;
                
                case 'delete_product':
                    $productId = $_POST['product_id'];
                    $db->prepare("DELETE FROM shop_products WHERE product_id = ?")->execute([$productId]);
                    $message = "Product deleted!";
                    break;
                
                case 'save_settings':
                    $settings = [
                        'site_name' => $_POST['site_name'] ?? 'Hello Hingoli',
                        'contact_email' => $_POST['contact_email'] ?? '',
                        'contact_phone' => $_POST['contact_phone'] ?? '',
                        'facebook_url' => $_POST['facebook_url'] ?? '',
                        'instagram_url' => $_POST['instagram_url'] ?? '',
                        'twitter_url' => $_POST['twitter_url'] ?? '',
                        'default_city' => $_POST['default_city'] ?? 'Hingoli',
                    ];
                    foreach ($settings as $key => $value) {
                        $db->prepare("INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE setting_value = ?")->execute([$key, $value, $value]);
                    }
                    $message = "Settings saved!";
                    break;

                case 'save_auto_moderation':
                    $autoModProducts = isset($_POST['auto_moderation_products']) ? 'true' : 'false';
                    $autoModListings = isset($_POST['auto_moderation_listings']) ? 'true' : 'false';
                    
                    $db->prepare("INSERT INTO settings (setting_key, setting_value) VALUES ('auto_moderation_products', ?) ON DUPLICATE KEY UPDATE setting_value = ?")
                        ->execute([$autoModProducts, $autoModProducts]);
                    $db->prepare("INSERT INTO settings (setting_key, setting_value) VALUES ('auto_moderation_listings', ?) ON DUPLICATE KEY UPDATE setting_value = ?")
                        ->execute([$autoModListings, $autoModListings]);
                    
                    $message = "Auto-moderation settings saved!";
                    header("Location: ?tab=moderation&msg=" . urlencode($message));
                    exit;

                case 'delete_listing':
                    $id = $_POST['listing_id'];
                    // Delete type-specific data first
                    $db->prepare("DELETE FROM services_listings WHERE listing_id = ?")->execute([$id]);
                    $db->prepare("DELETE FROM job_listings WHERE listing_id = ?")->execute([$id]);
                    $db->prepare("DELETE FROM business_listings WHERE listing_id = ?")->execute([$id]);
                    $db->prepare("DELETE FROM listing_images WHERE listing_id = ?")->execute([$id]);
                    // Delete main listing
                    $stmt = $db->prepare("DELETE FROM listings WHERE listing_id = ?");
                    $stmt->execute([$id]);
                    $message = "Listing deleted successfully!";
                    break;

                case 'verify_listing':
                    $id = $_POST['listing_id'];
                    $stmt = $db->prepare("UPDATE listings SET is_verified = 1, verified_at = NOW() WHERE listing_id = ?");
                    $stmt->execute([$id]);
                    $message = "Listing verified!";
                    break;

                case 'feature_listing':
                    $id = $_POST['listing_id'];
                    $stmt = $db->prepare("UPDATE listings SET is_featured = 1 WHERE listing_id = ?");
                    $stmt->execute([$id]);
                    $message = "Listing featured!";
                    break;

                case 'transfer_listing':
                    $listingId = $_POST['listing_id'];
                    $newUserId = $_POST['new_user_id'];
                    
                    // Verify user exists
                    $userCheck = $db->prepare("SELECT user_id, username FROM users WHERE user_id = ?");
                    $userCheck->execute([$newUserId]);
                    $newUser = $userCheck->fetch();
                    
                    if (!$newUser) {
                        $error = "User ID $newUserId not found!";
                        break;
                    }
                    
                    // Transfer listing
                    $stmt = $db->prepare("UPDATE listings SET user_id = ? WHERE listing_id = ?");
                    $stmt->execute([$newUserId, $listingId]);
                    $message = "Listing #$listingId transferred to " . $newUser['username'] . " (ID: $newUserId)";
                    break;

                // ========== USER ACTIONS ==========
                case 'add_user':
                    $username = $_POST['username'];
                    $phone = $_POST['phone'];
                    $email = !empty($_POST['email']) ? $_POST['email'] : null;
                    $password = !empty($_POST['password']) ? password_hash($_POST['password'], PASSWORD_DEFAULT) : null;
                    $is_verified = isset($_POST['is_verified']) ? 1 : 0;
                    
                    $stmt = $db->prepare("INSERT INTO users (username, phone, email, password_hash, is_verified) VALUES (?, ?, ?, ?, ?)");
                    $stmt->execute([$username, $phone, $email, $password, $is_verified]);
                    $message = "User added successfully!";
                    break;

                case 'delete_user':
                    $id = $_POST['user_id'];
                    $stmt = $db->prepare("DELETE FROM users WHERE user_id = ?");
                    $stmt->execute([$id]);
                    $message = "User deleted successfully!";
                    break;

                case 'edit_user':
                    $userId = $_POST['user_id'];
                    $username = $_POST['username'];
                    $phone = $_POST['phone'];
                    $email = !empty($_POST['email']) ? $_POST['email'] : null;
                    $is_verified = isset($_POST['is_verified']) ? 1 : 0;
                    
                    // Handle avatar upload
                    $avatar_url = $_POST['existing_avatar'] ?? null;
                    if (isset($_FILES['avatar']) && $_FILES['avatar']['error'] === UPLOAD_ERR_OK) {
                        $avatar_url = uploadImage($_FILES['avatar'], 'avatars');
                    }
                    
                    $sql = "UPDATE users SET username = ?, phone = ?, email = ?, is_verified = ?";
                    $params = [$username, $phone, $email, $is_verified];
                    
                    if ($avatar_url) {
                        $sql .= ", avatar_url = ?";
                        $params[] = $avatar_url;
                    }
                    
                    // Handle password change
                    if (!empty($_POST['new_password'])) {
                        $sql .= ", password_hash = ?";
                        $params[] = password_hash($_POST['new_password'], PASSWORD_DEFAULT);
                    }
                    
                    $sql .= " WHERE user_id = ?";
                    $params[] = $userId;
                    
                    $db->prepare($sql)->execute($params);
                    $message = "User updated successfully!";
                    header("Location: ?tab=user_detail&id=$userId&msg=" . urlencode($message));
                    exit;

                case 'add_user_address':
                    $userId = $_POST['user_id'];
                    $name = $_POST['name'];
                    $phone = $_POST['phone'];
                    $address_line1 = $_POST['address_line1'];
                    $address_line2 = $_POST['address_line2'] ?? null;
                    $city = $_POST['city'];
                    $state = $_POST['state'] ?? 'Maharashtra';
                    $pincode = $_POST['pincode'];
                    $is_default = isset($_POST['is_default']) ? 1 : 0;
                    
                    // If default, unset other defaults
                    if ($is_default) {
                        $db->prepare("UPDATE user_addresses SET is_default = 0 WHERE user_id = ?")->execute([$userId]);
                    }
                    
                    $stmt = $db->prepare("INSERT INTO user_addresses (user_id, name, phone, address_line1, address_line2, city, state, pincode, is_default) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    $stmt->execute([$userId, $name, $phone, $address_line1, $address_line2, $city, $state, $pincode, $is_default]);
                    $message = "Address added successfully!";
                    header("Location: ?tab=user_detail&id=$userId&msg=" . urlencode($message));
                    exit;

                case 'edit_user_address':
                    $addressId = $_POST['address_id'];
                    $userId = $_POST['user_id'];
                    $name = $_POST['name'];
                    $phone = $_POST['phone'];
                    $address_line1 = $_POST['address_line1'];
                    $address_line2 = $_POST['address_line2'] ?? null;
                    $city = $_POST['city'];
                    $state = $_POST['state'] ?? 'Maharashtra';
                    $pincode = $_POST['pincode'];
                    $is_default = isset($_POST['is_default']) ? 1 : 0;
                    
                    // If default, unset other defaults
                    if ($is_default) {
                        $db->prepare("UPDATE user_addresses SET is_default = 0 WHERE user_id = ?")->execute([$userId]);
                    }
                    
                    $stmt = $db->prepare("UPDATE user_addresses SET name = ?, phone = ?, address_line1 = ?, address_line2 = ?, city = ?, state = ?, pincode = ?, is_default = ? WHERE address_id = ?");
                    $stmt->execute([$name, $phone, $address_line1, $address_line2, $city, $state, $pincode, $is_default, $addressId]);
                    $message = "Address updated successfully!";
                    header("Location: ?tab=user_detail&id=$userId&msg=" . urlencode($message));
                    exit;

                case 'delete_user_address':
                    $addressId = $_POST['address_id'];
                    $userId = $_POST['user_id'];
                    $db->prepare("DELETE FROM user_addresses WHERE address_id = ?")->execute([$addressId]);
                    $message = "Address deleted successfully!";
                    header("Location: ?tab=user_detail&id=$userId&msg=" . urlencode($message));
                    exit;

                // ========== BANNER ACTIONS ==========
                case 'add_banner':
                    $title = $_POST['title'];
                    $link_url = $_POST['link_url'] ?? null;
                    $placement = $_POST['placement'];
                    $sort_order = $_POST['sort_order'] ?? 0;
                    $is_active = isset($_POST['is_active']) ? 1 : 0;
                    
                    // Handle image upload to Cloudflare R2
                    $image_url = $_POST['image_url'] ?? '';
                    if (isset($_FILES['banner_image']) && $_FILES['banner_image']['error'] === UPLOAD_ERR_OK) {
                        $ext = strtolower(pathinfo($_FILES['banner_image']['name'], PATHINFO_EXTENSION));
                        $allowedExts = ['jpg', 'jpeg', 'png', 'gif', 'webp'];
                        
                        if (in_array($ext, $allowedExts)) {
                            // R2 Configuration
                            $r2Config = [
                                'endpoint' => 'https://62b435cd6e08605f2c7c1aadedc6a591.r2.cloudflarestorage.com',
                                'accessKeyId' => '6d12f3c5c7a0b68722e46063c8befec4',
                                'secretAccessKey' => 'fd01dd18c77b8de4ccbf036b4dfafdaa062c5a32685dc8b482ac40d0a9d50d60',
                                'bucket' => 'hello-hingoli-bucket',
                                'publicUrl' => 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev'
                            ];
                            
                            // Compress and convert banner to WebP
                            $tmpFile = $_FILES['banner_image']['tmp_name'];
                            $maxWidth = 1200;
                            $maxHeight = 600;
                            $quality = 85;
                            
                            // Get original dimensions
                            $imageInfo = getimagesize($tmpFile);
                            $origWidth = $imageInfo[0];
                            $origHeight = $imageInfo[1];
                            $mimeType = $imageInfo['mime'];
                            
                            // Create image resource based on type
                            switch ($mimeType) {
                                case 'image/jpeg':
                                    $srcImage = imagecreatefromjpeg($tmpFile);
                                    break;
                                case 'image/png':
                                    $srcImage = imagecreatefrompng($tmpFile);
                                    break;
                                case 'image/gif':
                                    $srcImage = imagecreatefromgif($tmpFile);
                                    break;
                                case 'image/webp':
                                    $srcImage = imagecreatefromwebp($tmpFile);
                                    break;
                                default:
                                    $srcImage = imagecreatefromjpeg($tmpFile);
                            }
                            
                            if ($srcImage) {
                                // Calculate new dimensions maintaining aspect ratio
                                $ratio = min($maxWidth / $origWidth, $maxHeight / $origHeight);
                                if ($ratio < 1) {
                                    $newWidth = (int)($origWidth * $ratio);
                                    $newHeight = (int)($origHeight * $ratio);
                                } else {
                                    $newWidth = $origWidth;
                                    $newHeight = $origHeight;
                                }
                                
                                // Create resized image
                                $dstImage = imagecreatetruecolor($newWidth, $newHeight);
                                
                                // Preserve transparency for PNG
                                imagealphablending($dstImage, false);
                                imagesavealpha($dstImage, true);
                                $transparent = imagecolorallocatealpha($dstImage, 255, 255, 255, 127);
                                imagefilledrectangle($dstImage, 0, 0, $newWidth, $newHeight, $transparent);
                                
                                // Resize
                                imagecopyresampled($dstImage, $srcImage, 0, 0, 0, 0, $newWidth, $newHeight, $origWidth, $origHeight);
                                
                                // Output to buffer as WebP
                                ob_start();
                                imagewebp($dstImage, null, $quality);
                                $fileContent = ob_get_clean();
                                
                                // Cleanup
                                imagedestroy($srcImage);
                                imagedestroy($dstImage);
                                
                                $filename = 'banners/' . uniqid() . '_' . time() . '.webp';
                                $contentType = 'image/webp';
                            } else {
                                // Fallback: upload original if compression fails
                                $filename = 'banners/' . uniqid() . '_' . time() . '.' . $ext;
                                $fileContent = file_get_contents($tmpFile);
                                $contentType = $_FILES['banner_image']['type'] ?: 'image/' . $ext;
                            }
                            
                            // Upload to R2 using S3 API
                            $uploadResult = uploadToR2($r2Config, $filename, $fileContent, $contentType);
                            
                            if ($uploadResult['success']) {
                                $image_url = $r2Config['publicUrl'] . '/' . $filename;
                            } else {
                                $error = "R2 Upload failed: " . $uploadResult['error'];
                                break;
                            }
                        }
                    }
                    
                    if (empty($image_url)) {
                        $error = "Please provide an image (upload or URL)";
                        break;
                    }
                    
                    $stmt = $db->prepare("INSERT INTO banners (title, image_url, link_url, placement, sort_order, is_active) VALUES (?, ?, ?, ?, ?, ?)");
                    $stmt->execute([$title, $image_url, $link_url, $placement, $sort_order, $is_active]);
                    $message = "Banner added successfully!";
                    break;

                case 'delete_banner':
                    $id = $_POST['banner_id'];
                    $stmt = $db->prepare("DELETE FROM banners WHERE banner_id = ?");
                    $stmt->execute([$id]);
                    $message = "Banner deleted successfully!";
                    break;

                case 'toggle_banner':
                    $id = $_POST['banner_id'];
                    $stmt = $db->prepare("UPDATE banners SET is_active = NOT is_active WHERE banner_id = ?");
                    $stmt->execute([$id]);
                    $message = "Banner status updated!";
                    break;

                case 'edit_banner':
                    $id = $_POST['banner_id'];
                    $title = $_POST['title'];
                    $image_url = $_POST['image_url'];
                    $link_url = $_POST['link_url'] ?? null;
                    $placement = $_POST['placement'];
                    $sort_order = $_POST['sort_order'] ?? 0;
                    $stmt = $db->prepare("UPDATE banners SET title = ?, image_url = ?, link_url = ?, placement = ?, sort_order = ? WHERE banner_id = ?");
                    $stmt->execute([$title, $image_url, $link_url, $placement, $sort_order, $id]);
                    $message = "Banner updated!";
                    break;

                // ========== CITY ACTIONS ==========
                case 'add_city':
                    $name = $_POST['name'];
                    $slug = strtolower(trim(preg_replace('/[^A-Za-z0-9-]+/', '-', $name)));
                    $is_popular = isset($_POST['is_popular']) ? 1 : 0;
                    
                    $stmt = $db->prepare("INSERT INTO cities (state_id, name, slug, is_popular, is_active) VALUES (1, ?, ?, ?, 1)");
                    $stmt->execute([$name, $slug, $is_popular]);
                    $message = "City added successfully!";
                    break;

                case 'delete_city':
                    $id = $_POST['city_id'];
                    $stmt = $db->prepare("DELETE FROM cities WHERE city_id = ?");
                    $stmt->execute([$id]);
                    $message = "City deleted successfully!";
                    break;

                case 'edit_city':
                    $id = $_POST['city_id'];
                    $name = $_POST['name'];
                    $slug = strtolower(trim(preg_replace('/[^A-Za-z0-9-]+/', '-', $name)));
                    $is_popular = isset($_POST['is_popular']) ? 1 : 0;
                    $stmt = $db->prepare("UPDATE cities SET name = ?, slug = ?, is_popular = ? WHERE city_id = ?");
                    $stmt->execute([$name, $slug, $is_popular, $id]);
                    $message = "City updated!";
                    break;
                
                case 'approve_review':
                    $id = $_POST['review_id'];
                    $db->prepare("UPDATE reviews SET approval_status = 'approved', is_approved = 1, moderated_at = NOW() WHERE review_id = ?")->execute([$id]);
                    $message = "Review approved!";
                    break;
                
                case 'reject_review':
                    $id = $_POST['review_id'];
                    $db->prepare("UPDATE reviews SET approval_status = 'rejected', is_approved = 0, moderated_at = NOW() WHERE review_id = ?")->execute([$id]);
                    $message = "Review rejected!";
                    break;
                
                case 'delete_review':
                    $id = $_POST['review_id'];
                    $db->prepare("DELETE FROM reviews WHERE review_id = ?")->execute([$id]);
                    $message = "Review deleted!";
                    break;

                // ========== GALLERY IMAGE ACTIONS ==========
                case 'delete_gallery_image':
                    $image_id = $_POST['image_id'];
                    $db->prepare("DELETE FROM listing_images WHERE image_id = ?")->execute([$image_id]);
                    $message = "Gallery image deleted!";
                    break;

                case 'add_gallery_image':
                    $listing_id = $_POST['listing_id'];
                    $image_url = $_POST['image_url'];
                    $maxSort = $db->prepare("SELECT COALESCE(MAX(sort_order), -1) + 1 FROM listing_images WHERE listing_id = ?");
                    $maxSort->execute([$listing_id]);
                    $sortOrder = $maxSort->fetchColumn();
                    $db->prepare("INSERT INTO listing_images (listing_id, image_url, sort_order) VALUES (?, ?, ?)")->execute([$listing_id, $image_url, $sortOrder]);
                    $message = "Gallery image added!";
                    break;

                // ========== PRICELIST ACTIONS ==========
                case 'add_pricelist_item':
                    $listing_id = $_POST['listing_id'];
                    $item_name = $_POST['item_name'];
                    $item_description = $_POST['item_description'] ?? null;
                    $item_category = $_POST['item_category'] ?? null;
                    $price = $_POST['price'];
                    $discounted_price = !empty($_POST['discounted_price']) ? $_POST['discounted_price'] : null;
                    $duration = !empty($_POST['duration_minutes']) ? $_POST['duration_minutes'] : null;
                    $maxSort = $db->prepare("SELECT COALESCE(MAX(sort_order), -1) + 1 FROM listing_price_list WHERE listing_id = ?");
                    $maxSort->execute([$listing_id]);
                    $sortOrder = $maxSort->fetchColumn();
                    $db->prepare("INSERT INTO listing_price_list (listing_id, item_name, item_description, item_category, price, discounted_price, duration_minutes, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
                        ->execute([$listing_id, $item_name, $item_description, $item_category, $price, $discounted_price, $duration, $sortOrder]);
                    $message = "Pricelist item added!";
                    // Redirect back to edit page
                    header("Location: ?tab=listings&edit=$listing_id&msg=" . urlencode($message));
                    exit;

                case 'edit_pricelist_item':
                    $item_id = $_POST['item_id'];
                    $item_name = $_POST['item_name'];
                    $item_description = $_POST['item_description'] ?? null;
                    $item_category = $_POST['item_category'] ?? null;
                    $price = $_POST['price'];
                    $discounted_price = !empty($_POST['discounted_price']) ? $_POST['discounted_price'] : null;
                    $duration = !empty($_POST['duration_minutes']) ? $_POST['duration_minutes'] : null;
                    $db->prepare("UPDATE listing_price_list SET item_name = ?, item_description = ?, item_category = ?, price = ?, discounted_price = ?, duration_minutes = ? WHERE item_id = ?")
                        ->execute([$item_name, $item_description, $item_category, $price, $discounted_price, $duration, $item_id]);
                    $message = "Pricelist item updated!";
                    // Redirect back to edit page
                    $listing_id = $_POST['listing_id'] ?? null;
                    if ($listing_id) {
                        header("Location: ?tab=listings&edit=$listing_id&msg=" . urlencode($message));
                        exit;
                    }
                    break;

                case 'delete_pricelist_item':
                    $item_id = $_POST['item_id'];
                    $listing_id = $_POST['listing_id'] ?? null;
                    $db->prepare("DELETE FROM listing_price_list WHERE item_id = ?")->execute([$item_id]);
                    $message = "Pricelist item deleted!";
                    // Redirect back to edit page
                    if ($listing_id) {
                        header("Location: ?tab=listings&edit=$listing_id&msg=" . urlencode($message));
                        exit;
                    }
                    break;

                // ========== SHOP PRODUCTS ACTIONS ==========
                case 'add_shop_product':
                    $listing_id = $_POST['listing_id'];
                    $product_name = $_POST['product_name'];
                    $description = $_POST['description'] ?? null;
                    $category_id = $_POST['category_id'];
                    $subcategory_id = !empty($_POST['subcategory_id']) ? $_POST['subcategory_id'] : null;
                    $price = $_POST['price'];
                    $discounted_price = !empty($_POST['discounted_price']) ? $_POST['discounted_price'] : null;
                    $sell_online = isset($_POST['sell_online']) ? 1 : 0;
                    $stock_qty = !empty($_POST['stock_qty']) ? $_POST['stock_qty'] : null;
                    
                    // Handle image upload
                    $image_url = null;
                    if (!empty($_POST['image_url'])) {
                        $image_url = $_POST['image_url'];
                    } elseif (isset($_FILES['product_image']) && $_FILES['product_image']['error'] === UPLOAD_ERR_OK) {
                        $image_url = uploadImage($_FILES['product_image'], 'products');
                    }
                    
                    $maxSort = $db->prepare("SELECT COALESCE(MAX(sort_order), -1) + 1 FROM shop_products WHERE listing_id = ?");
                    $maxSort->execute([$listing_id]);
                    $sortOrder = $maxSort->fetchColumn();
                    
                    $db->prepare("INSERT INTO shop_products (listing_id, product_name, description, category_id, subcategory_id, price, discounted_price, image_url, sell_online, stock_qty, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                        ->execute([$listing_id, $product_name, $description, $category_id, $subcategory_id, $price, $discounted_price, $image_url, $sell_online, $stock_qty, $sortOrder]);
                    $message = "Product added!";
                    header("Location: ?tab=listings&edit=$listing_id&msg=" . urlencode($message));
                    exit;

                case 'edit_shop_product':
                    $product_id = $_POST['product_id'];
                    $listing_id = $_POST['listing_id'];
                    $product_name = $_POST['product_name'];
                    $description = $_POST['description'] ?? null;
                    $category_id = $_POST['category_id'];
                    $subcategory_id = !empty($_POST['subcategory_id']) ? $_POST['subcategory_id'] : null;
                    $price = $_POST['price'];
                    $discounted_price = !empty($_POST['discounted_price']) ? $_POST['discounted_price'] : null;
                    $sell_online = isset($_POST['sell_online']) ? 1 : 0;
                    $stock_qty = !empty($_POST['stock_qty']) ? $_POST['stock_qty'] : null;
                    
                    // Handle image upload
                    $image_url = $_POST['existing_image'] ?? null;
                    if (!empty($_POST['image_url'])) {
                        $image_url = $_POST['image_url'];
                    } elseif (isset($_FILES['product_image']) && $_FILES['product_image']['error'] === UPLOAD_ERR_OK) {
                        $image_url = uploadImage($_FILES['product_image'], 'products');
                    }
                    
                    $db->prepare("UPDATE shop_products SET product_name = ?, description = ?, category_id = ?, subcategory_id = ?, price = ?, discounted_price = ?, image_url = ?, sell_online = ?, stock_qty = ? WHERE product_id = ?")
                        ->execute([$product_name, $description, $category_id, $subcategory_id, $price, $discounted_price, $image_url, $sell_online, $stock_qty, $product_id]);
                    $message = "Product updated!";
                    header("Location: ?tab=listings&edit=$listing_id&msg=" . urlencode($message));
                    exit;

                case 'delete_shop_product':
                    $product_id = $_POST['product_id'];
                    $listing_id = $_POST['listing_id'] ?? null;
                    $db->prepare("DELETE FROM shop_products WHERE product_id = ?")->execute([$product_id]);
                    $message = "Product deleted!";
                    if ($listing_id) {
                        header("Location: ?tab=listings&edit=$listing_id&msg=" . urlencode($message));
                        exit;
                    }
                    break;

                // ========== PUSH NOTIFICATION (FCM v1 API) ==========
                case 'send_notification':
                    $title = $_POST['title'];
                    $body = $_POST['body'];
                    $targetType = $_POST['target_type'] ?? 'all';
                    
                    // Debug log file
                    $logFile = __DIR__ . '/fcm_debug.log';
                    $logMsg = function($msg) use ($logFile) {
                        file_put_contents($logFile, date('[Y-m-d H:i:s] ') . $msg . "\n", FILE_APPEND);
                    };
                    
                    $logMsg("========== SENDING NOTIFICATION ==========");
                    $logMsg("Title: $title");
                    $logMsg("Body: $body");
                    $logMsg("Target: $targetType");
                    
                    // Get user IDs and FCM tokens based on target
                    $userTokens = []; // Array of [user_id => [tokens...]]
                    if ($targetType === 'all') {
                        $stmt = $db->query("SELECT user_id, fcm_token FROM user_fcm_tokens WHERE fcm_token IS NOT NULL AND fcm_token != ''");
                        while ($row = $stmt->fetch()) {
                            if (!isset($userTokens[$row['user_id']])) {
                                $userTokens[$row['user_id']] = [];
                            }
                            $userTokens[$row['user_id']][] = $row['fcm_token'];
                        }
                    } else {
                        $stmt = $db->prepare("
                            SELECT DISTINCT uft.user_id, uft.fcm_token 
                            FROM user_fcm_tokens uft 
                            JOIN listings l ON uft.user_id = l.user_id 
                            WHERE l.listing_type = ? AND uft.fcm_token IS NOT NULL AND uft.fcm_token != ''
                        ");
                        $stmt->execute([$targetType]);
                        while ($row = $stmt->fetch()) {
                            if (!isset($userTokens[$row['user_id']])) {
                                $userTokens[$row['user_id']] = [];
                            }
                            $userTokens[$row['user_id']][] = $row['fcm_token'];
                        }
                    }
                    
                    $logMsg("Found " . count($userTokens) . " users with FCM tokens");
                    foreach ($userTokens as $userId => $tokens) {
                        $logMsg("  User $userId: " . count($tokens) . " token(s) - " . substr($tokens[0], 0, 30) . "...");
                    }
                    
                    $sentCount = 0;
                    $fcmErrors = [];
                    $projectId = 'hellohingoliapp'; // Your Firebase project ID
                    
                    // Path to service account JSON (download from Firebase Console)
                    $serviceAccountPath = __DIR__ . '/firebase-service-account.json';
                    
                    if (!empty($userTokens) && file_exists($serviceAccountPath)) {
                        $logMsg("Service account found, getting OAuth token...");
                        
                        // Get OAuth2 access token
                        $serviceAccount = json_decode(file_get_contents($serviceAccountPath), true);
                        
                        // Create JWT
                        $header = base64_encode(json_encode(['alg' => 'RS256', 'typ' => 'JWT']));
                        $now = time();
                        $claims = [
                            'iss' => $serviceAccount['client_email'],
                            'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
                            'aud' => 'https://oauth2.googleapis.com/token',
                            'iat' => $now,
                            'exp' => $now + 3600
                        ];
                        $payload = base64_encode(json_encode($claims));
                        
                        // Sign with private key
                        $signatureInput = "$header.$payload";
                        openssl_sign($signatureInput, $signature, $serviceAccount['private_key'], 'SHA256');
                        $jwt = "$header.$payload." . base64_encode($signature);
                        
                        // Exchange JWT for access token
                        $ch = curl_init('https://oauth2.googleapis.com/token');
                        curl_setopt_array($ch, [
                            CURLOPT_POST => true,
                            CURLOPT_POSTFIELDS => http_build_query([
                                'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
                                'assertion' => $jwt
                            ]),
                            CURLOPT_RETURNTRANSFER => true
                        ]);
                        $tokenResponse = json_decode(curl_exec($ch), true);
                        curl_close($ch);
                        
                        if (isset($tokenResponse['access_token'])) {
                            $logMsg("✓ OAuth token obtained successfully");
                            $accessToken = $tokenResponse['access_token'];
                            $fcmUrl = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send";
                            
                            // Send to all tokens
                            foreach ($userTokens as $userId => $tokens) {
                                foreach ($tokens as $token) {
                                    $fcmData = [
                                        'message' => [
                                            'token' => $token,
                                            'notification' => [
                                                'title' => $title,
                                                'body' => $body
                                            ],
                                            'data' => [
                                                'type' => 'admin_notification',
                                                'title' => $title,
                                                'body' => $body,
                                                'click_action' => 'FLUTTER_NOTIFICATION_CLICK'
                                            ],
                                            'android' => [
                                                'priority' => 'high',
                                                'notification' => [
                                                    'channel_id' => 'listing_notifications',
                                                    'sound' => 'default'
                                                ]
                                            ]
                                        ]
                                    ];
                                    
                                    $ch = curl_init($fcmUrl);
                                    curl_setopt_array($ch, [
                                        CURLOPT_HTTPHEADER => [
                                            'Authorization: Bearer ' . $accessToken,
                                            'Content-Type: application/json'
                                        ],
                                        CURLOPT_POST => true,
                                        CURLOPT_POSTFIELDS => json_encode($fcmData),
                                        CURLOPT_RETURNTRANSFER => true
                                    ]);
                                    $response = curl_exec($ch);
                                    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
                                    curl_close($ch);
                                    
                                    $logMsg("  User $userId: HTTP $httpCode - " . substr($response, 0, 200));
                                    
                                    if ($httpCode === 200) {
                                        $sentCount++;
                                        break; // Only count once per user
                                    } else {
                                        $fcmErrors[] = "User $userId: HTTP $httpCode - $response";
                                    }
                                }
                            }
                        } else {
                            $logMsg("✗ Failed to get OAuth token: " . json_encode($tokenResponse));
                            $error = "Failed to get OAuth token: " . ($tokenResponse['error_description'] ?? 'Unknown error');
                        }
                    } elseif (empty($userTokens)) {
                        $logMsg("✗ No users with FCM tokens found!");
                        $error = "No users with FCM tokens found to send notification to";
                    } elseif (!file_exists($serviceAccountPath)) {
                        $logMsg("✗ Missing firebase-service-account.json");
                        $error = "Missing firebase-service-account.json. Download from Firebase Console → Project Settings → Service Accounts → Generate new private key";
                    }
                    
                    $logMsg("Sent to $sentCount users");
                    if (!empty($fcmErrors)) {
                        $logMsg("Errors: " . implode("; ", $fcmErrors));
                    }
                    $logMsg("==========================================\n");
                    
                    // Log the notification to notification_logs (for admin tracking)
                    try {
                        $db->prepare("INSERT INTO notification_logs (title, body, target_type, sent_count) VALUES (?, ?, ?, ?)")
                            ->execute([$title, $body, $targetType, $sentCount]);
                    } catch (Exception $e) {
                        // Table might not exist
                    }
                    
                    // Save to user_notifications table for ALL users (for in-app notification inbox)
                    // This ensures all users see the notification in-app, even if they don't have FCM tokens
                    try {
                        // Get ALL user IDs
                        $allUsersStmt = $db->query("SELECT user_id FROM users");
                        $allUserIds = $allUsersStmt->fetchAll(PDO::FETCH_COLUMN);
                        
                        if (!empty($allUserIds)) {
                            $insertStmt = $db->prepare("
                                INSERT INTO user_notifications (user_id, title, body, type, is_read, created_at)
                                VALUES (?, ?, ?, 'admin_broadcast', 0, NOW())
                            ");
                            foreach ($allUserIds as $userId) {
                                $insertStmt->execute([$userId, $title, $body]);
                            }
                        }
                    } catch (Exception $e) {
                        // Log error but don't fail
                        error_log("Failed to save to user_notifications: " . $e->getMessage());
                    }
                    
                    if (!isset($error)) {
                        $message = "Notification sent to $sentCount users!";
                    }
                    break;

                // ========== EXPORT: LISTINGS ==========
                case 'export_listings':
                    $exportType = $_POST['export_type'] ?? '';
                    $exportStatus = $_POST['export_status'] ?? '';
                    
                    $sql = "SELECT l.listing_id, l.listing_type, l.title, l.description, 
                                   c.name as category, l.location, l.city, l.status, l.is_verified, l.is_featured,
                                   l.view_count, u.username, l.created_at
                            FROM listings l
                            LEFT JOIN categories c ON l.category_id = c.category_id
                            LEFT JOIN users u ON l.user_id = u.user_id
                            WHERE 1=1";
                    $params = [];
                    
                    if ($exportType) {
                        $sql .= " AND l.listing_type = ?";
                        $params[] = $exportType;
                    }
                    if ($exportStatus) {
                        $sql .= " AND l.status = ?";
                        $params[] = $exportStatus;
                    }
                    $sql .= " ORDER BY l.created_at DESC";
                    
                    $stmt = $db->prepare($sql);
                    $stmt->execute($params);
                    $data = $stmt->fetchAll();
                    
                    header('Content-Type: text/csv; charset=utf-8');
                    header('Content-Disposition: attachment; filename=listings_' . date('Y-m-d') . '.csv');
                    $output = fopen('php://output', 'w');
                    fputcsv($output, ['ID', 'Type', 'Title', 'Description', 'Category', 'Location', 'City', 'Status', 'Verified', 'Featured', 'Views', 'Owner', 'Created']);
                    foreach ($data as $row) {
                        fputcsv($output, $row);
                    }
                    fclose($output);
                    exit;

                // ========== EXPORT: USERS ==========
                case 'export_users':
                    $data = $db->query("SELECT user_id, username, phone, email, is_verified, listing_count, avg_rating, created_at FROM users ORDER BY created_at DESC")->fetchAll();
                    
                    header('Content-Type: text/csv; charset=utf-8');
                    header('Content-Disposition: attachment; filename=users_' . date('Y-m-d') . '.csv');
                    $output = fopen('php://output', 'w');
                    fputcsv($output, ['ID', 'Username', 'Phone', 'Email', 'Verified', 'Listings', 'Rating', 'Created']);
                    foreach ($data as $row) {
                        fputcsv($output, $row);
                    }
                    fclose($output);
                    exit;

                // ========== EXPORT: REVIEWS ==========
                case 'export_reviews':
                    $data = $db->query("
                        SELECT r.review_id, l.title as listing, u.username as reviewer, r.rating, r.title as review_title, 
                               r.content, r.approval_status, r.created_at
                        FROM reviews r
                        LEFT JOIN listings l ON r.listing_id = l.listing_id
                        LEFT JOIN users u ON r.reviewer_id = u.user_id
                        ORDER BY r.created_at DESC
                    ")->fetchAll();
                    
                    header('Content-Type: text/csv; charset=utf-8');
                    header('Content-Disposition: attachment; filename=reviews_' . date('Y-m-d') . '.csv');
                    $output = fopen('php://output', 'w');
                    fputcsv($output, ['ID', 'Listing', 'Reviewer', 'Rating', 'Title', 'Content', 'Status', 'Created']);
                    foreach ($data as $row) {
                        fputcsv($output, $row);
                    }
                    fclose($output);
                    exit;

                // ========== ENQUIRY ACTIONS ==========
                case 'update_enquiry':
                    $enquiryId = $_POST['enquiry_id'];
                    $status = $_POST['status'];
                    $db->prepare("UPDATE enquiries SET status = ? WHERE enquiry_id = ?")->execute([$status, $enquiryId]);
                    $message = "Enquiry updated!";
                    break;

                case 'delete_enquiry':
                    $enquiryId = $_POST['enquiry_id'];
                    $db->prepare("DELETE FROM enquiries WHERE enquiry_id = ?")->execute([$enquiryId]);
                    $message = "Enquiry deleted!";
                    break;

                // ========== ORDER ACTIONS ==========
                case 'update_order_status':
                    $orderId = $_POST['order_id'];
                    $newStatus = $_POST['order_status'];
                    $validStatuses = ['pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled'];
                    if (!in_array($newStatus, $validStatuses)) {
                        $error = "Invalid order status";
                        break;
                    }
                    $db->prepare("UPDATE orders SET order_status = ? WHERE order_id = ?")->execute([$newStatus, $orderId]);
                    
                    // Update all order items to match
                    $itemStatus = $newStatus === 'shipped' ? 'shipped' : ($newStatus === 'delivered' ? 'delivered' : 'confirmed');
                    if ($newStatus === 'cancelled') $itemStatus = 'cancelled';
                    $db->prepare("UPDATE order_items SET item_status = ? WHERE order_id = ?")->execute([$itemStatus, $orderId]);
                    
                    $message = "Order #$orderId status updated to $newStatus!";
                    break;

                case 'process_refund':
                    $orderId = $_POST['order_id'];
                    $refundAmount = !empty($_POST['refund_amount']) ? floatval($_POST['refund_amount']) : null;
                    
                    // Get order details
                    $stmt = $db->prepare("SELECT * FROM orders WHERE order_id = ?");
                    $stmt->execute([$orderId]);
                    $order = $stmt->fetch();
                    
                    if (!$order) {
                        $error = "Order not found";
                        break;
                    }
                    
                    if (empty($order['razorpay_payment_id'])) {
                        $error = "No payment found for this order";
                        break;
                    }
                    
                    // Razorpay refund API
                    $keyId = 'rzp_live_RrqH1rKPqejvOQ';
                    $keySecret = 'n25EPEXV9V6N5hMgb5tcgdXV';
                    
                    $refundAmountPaise = $refundAmount ? (int)($refundAmount * 100) : (int)($order['total_amount'] * 100);
                    
                    $ch = curl_init("https://api.razorpay.com/v1/payments/{$order['razorpay_payment_id']}/refund");
                    curl_setopt($ch, CURLOPT_USERPWD, "$keyId:$keySecret");
                    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
                    curl_setopt($ch, CURLOPT_POST, true);
                    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode(['amount' => $refundAmountPaise]));
                    curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
                    
                    $response = curl_exec($ch);
                    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
                    curl_close($ch);
                    
                    $result = json_decode($response, true);
                    
                    if ($httpCode === 200 && isset($result['id'])) {
                        // Update order
                        $db->prepare("UPDATE orders SET payment_status = 'refunded', order_status = 'refunded', refund_id = ?, refund_amount = ? WHERE order_id = ?")
                            ->execute([$result['id'], $refundAmountPaise / 100, $orderId]);
                        $message = "Refund of ₹" . number_format($refundAmountPaise / 100, 2) . " processed! Refund ID: " . $result['id'];
                    } else {
                        $errorMsg = $result['error']['description'] ?? 'Unknown error';
                        $error = "Refund failed: $errorMsg";
                    }
                    break;

                // ========== DELIVERY PINCODE ACTIONS ==========
                case 'add_pincode':
                    $pincode = $_POST['pincode'];
                    $cityName = $_POST['city_name'];
                    $deliveryDays = (int)($_POST['delivery_days'] ?? 2);
                    $deliveryTime = $_POST['delivery_time'] ?? '6 PM';
                    $shippingFee = floatval($_POST['shipping_fee'] ?? 50);
                    $cutoffHour = (int)($_POST['cutoff_hour'] ?? 14);
                    
                    $db->prepare("INSERT INTO service_pincodes (pincode, city_name, delivery_days, delivery_time, shipping_fee, cutoff_hour, is_serviceable) VALUES (?, ?, ?, ?, ?, ?, 1)")
                        ->execute([$pincode, $cityName, $deliveryDays, $deliveryTime, $shippingFee, $cutoffHour]);
                    $message = "Pincode $pincode added!";
                    break;

                case 'edit_pincode':
                    $pincodeId = $_POST['pincode_id'];
                    $deliveryDays = (int)$_POST['delivery_days'];
                    $deliveryTime = $_POST['delivery_time'];
                    $shippingFee = floatval($_POST['shipping_fee']);
                    $cutoffHour = (int)$_POST['cutoff_hour'];
                    $isServiceable = isset($_POST['is_serviceable']) ? 1 : 0;
                    
                    $db->prepare("UPDATE service_pincodes SET delivery_days = ?, delivery_time = ?, shipping_fee = ?, cutoff_hour = ?, is_serviceable = ? WHERE pincode = ?")
                        ->execute([$deliveryDays, $deliveryTime, $shippingFee, $cutoffHour, $isServiceable, $pincodeId]);
                    $message = "Pincode $pincodeId updated!";
                    break;

                case 'delete_pincode':
                    $pincodeId = $_POST['pincode_id'];
                    $db->prepare("DELETE FROM service_pincodes WHERE pincode = ?")->execute([$pincodeId]);
                    $message = "Pincode $pincodeId deleted!";
                    break;

                // ========== ENQUIRY ACTIONS ==========
                case 'update_enquiry_status':
                    $enquiryId = (int)$_POST['enquiry_id'];
                    $status = $_POST['status'];
                    $validStatuses = ['new', 'contacted', 'resolved', 'spam'];
                    if (in_array($status, $validStatuses)) {
                        $db->prepare("UPDATE enquiries SET status = ? WHERE enquiry_id = ?")
                            ->execute([$status, $enquiryId]);
                        $message = "Enquiry #$enquiryId status updated to $status";
                    }
                    break;

                // ========== APP CONFIG / FORCE UPDATE ==========
                case 'update_app_config':
                    $minVersion = $_POST['min_version'] ?? '1.0.0';
                    $latestVersion = $_POST['latest_version'] ?? '1.0.0';
                    $forceUpdate = isset($_POST['force_update']) ? 'true' : 'false';
                    $updateMessage = $_POST['update_message'] ?? 'Please update the app';
                    $updateMessageMr = $_POST['update_message_mr'] ?? 'कृपया अॅप अपडेट करा';
                    $playStoreUrl = $_POST['play_store_url'] ?? '';
                    
                    // Update each config value
                    $configs = [
                        'min_version' => $minVersion,
                        'latest_version' => $latestVersion,
                        'force_update' => $forceUpdate,
                        'update_message' => $updateMessage,
                        'update_message_mr' => $updateMessageMr,
                        'play_store_url' => $playStoreUrl
                    ];
                    
                    foreach ($configs as $key => $value) {
                        $db->prepare("UPDATE app_config SET config_value = ?, updated_at = NOW() WHERE config_key = ?")
                            ->execute([$value, $key]);
                    }
                    
                    $message = "App configuration updated successfully!";
                    break;

                case 'toggle_force_update':
                    // Get current value and toggle it
                    $stmt = $db->prepare("SELECT config_value FROM app_config WHERE config_key = 'force_update'");
                    $stmt->execute();
                    $currentValue = $stmt->fetchColumn();
                    $newValue = ($currentValue === 'true') ? 'false' : 'true';
                    
                    $db->prepare("UPDATE app_config SET config_value = ?, updated_at = NOW() WHERE config_key = 'force_update'")
                        ->execute([$newValue]);
                    
                    $message = "Force update " . ($newValue === 'true' ? 'ENABLED' : 'DISABLED') . "!";
                    header("Location: ?tab=app_config");
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Error: " . $e->getMessage();
    }
}

// ========== FETCH DATA ==========
// Dashboard Stats
$stats = [];
$stats['total_listings'] = $db->query("SELECT COUNT(*) FROM listings")->fetchColumn();
$stats['active_listings'] = $db->query("SELECT COUNT(*) FROM listings WHERE status = 'active'")->fetchColumn();
$stats['pending_listings'] = $db->query("SELECT COUNT(*) FROM listings WHERE status = 'pending'")->fetchColumn();
$stats['total_users'] = $db->query("SELECT COUNT(*) FROM users")->fetchColumn();
$stats['total_categories'] = $db->query("SELECT COUNT(*) FROM categories WHERE parent_id IS NULL")->fetchColumn();
$stats['services_count'] = $db->query("SELECT COUNT(*) FROM listings WHERE listing_type = 'services'")->fetchColumn();
$stats['jobs_count'] = $db->query("SELECT COUNT(*) FROM listings WHERE listing_type = 'jobs'")->fetchColumn();
$stats['selling_count'] = $db->query("SELECT COUNT(*) FROM listings WHERE listing_type = 'selling'")->fetchColumn();
$stats['business_count'] = $db->query("SELECT COUNT(*) FROM listings WHERE listing_type = 'business'")->fetchColumn();

// Order Stats
try {
    $stats['total_orders'] = $db->query("SELECT COUNT(*) FROM orders")->fetchColumn();
    $stats['pending_orders'] = $db->query("SELECT COUNT(*) FROM orders WHERE order_status = 'pending'")->fetchColumn();
    $stats['today_orders'] = $db->query("SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE()")->fetchColumn();
    $stats['total_revenue'] = $db->query("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE payment_status = 'paid'")->fetchColumn();
} catch (Exception $e) {
    $stats['total_orders'] = 0;
    $stats['pending_orders'] = 0;
    $stats['today_orders'] = 0;
    $stats['total_revenue'] = 0;
}

// Get settings
$settings = [];
try {
    $settingsRows = $db->query("SELECT setting_key, setting_value FROM settings")->fetchAll();
    foreach ($settingsRows as $row) {
        $settings[$row['setting_key']] = $row['setting_value'];
    }
} catch (Exception $e) {
    // Settings table might not exist yet
}

// Get current tab FIRST (moved up for lazy loading)
$tab = $_GET['tab'] ?? 'dashboard';

// Initialize all variables with defaults
$categories = [];
$subcategories = [];
$all_categories = [];
$listings = [];
$pendingListings = [];
$editListing = null;
$editGallery = [];
$editPricelist = [];
$users = [];
$banners = [];
$cities = [];
$reviews = [];
$listingsPerDay = [];
$usersPerDay = [];
$listingsByCategory = [];
$listingsByCity = [];
$listingsByType = [];
$enquiries = [];
$notificationLogs = [];
$otpLogs = [];
$orders = [];
$pincodes = [];
$orderFilterStatus = $_GET['order_status'] ?? '';
$orderFilterPayment = $_GET['payment_status'] ?? '';
$search = $_GET['search'] ?? '';
$filterType = $_GET['filter_type'] ?? '';
$filterStatus = $_GET['filter_status'] ?? '';
$filterCity = $_GET['filter_city'] ?? '';

// ========== LAZY LOADING - Only load data for active tab ==========

// Cities - needed for multiple tabs (forms)
if (in_array($tab, ['dashboard', 'listings', 'edit_listing', 'add_listing', 'export'])) {
    $cities = $db->query("SELECT * FROM cities ORDER BY is_popular DESC, name")->fetchAll();
}

// Categories - needed for category, listing, product, and add listing tabs
if (in_array($tab, ['dashboard', 'categories', 'listings', 'edit_listing', 'add_listing', 'products', 'add_product'])) {
    $categories = $db->query("SELECT * FROM categories WHERE parent_id IS NULL ORDER BY listing_type, sort_order, name")->fetchAll();
    $subcategories = $db->query("
        SELECT s.*, p.name as parent_name, p.listing_type 
        FROM categories s 
        JOIN categories p ON s.parent_id = p.category_id 
        WHERE s.parent_id IS NOT NULL 
        ORDER BY p.listing_type, p.name, s.name
    ")->fetchAll();
    $all_categories = $db->query("SELECT * FROM categories ORDER BY listing_type, parent_id, name")->fetchAll();
}

// Listings - only for listings tab
if (in_array($tab, ['listings', 'edit_listing'])) {
    // Build listings query with filters
    $listingsWhere = [];
    $listingsParams = [];
    
    if ($search) {
        $listingsWhere[] = "(l.title LIKE ? OR l.description LIKE ?)";
        $listingsParams[] = "%$search%";
        $listingsParams[] = "%$search%";
    }
    if ($filterType) {
        $listingsWhere[] = "l.listing_type = ?";
        $listingsParams[] = $filterType;
    }
    if ($filterStatus) {
        $listingsWhere[] = "l.status = ?";
        $listingsParams[] = $filterStatus;
    }
    if ($filterCity) {
        $listingsWhere[] = "l.city = ?";
        $listingsParams[] = $filterCity;
    }
    
    $listingsWhereClause = $listingsWhere ? 'WHERE ' . implode(' AND ', $listingsWhere) : '';
    
    $listingsQuery = "
        SELECT l.*, c.name as category_name, sc.name as subcategory_name, u.username,
               jl.salary_min, jl.salary_max, jl.employment_type, jl.salary_period, jl.remote_option, jl.vacancies, jl.education_required, jl.experience_required_years,
               sl.experience_years,
               bl.business_name, bl.industry, bl.established_year, bl.employee_count
        FROM listings l 
        LEFT JOIN categories c ON l.category_id = c.category_id 
        LEFT JOIN categories sc ON l.subcategory_id = sc.category_id 
        LEFT JOIN users u ON l.user_id = u.user_id 
        LEFT JOIN job_listings jl ON l.listing_id = jl.listing_id
        LEFT JOIN services_listings sl ON l.listing_id = sl.listing_id
        LEFT JOIN business_listings bl ON l.listing_id = bl.listing_id
        $listingsWhereClause
        ORDER BY l.created_at DESC 
        LIMIT 100
    ";
    
    $stmt = $db->prepare($listingsQuery);
    $stmt->execute($listingsParams);
    $listings = $stmt->fetchAll();
    
    // Get edit listing data if editing
    if (isset($_GET['edit'])) {
        $editId = $_GET['edit'];
        $stmt = $db->prepare("
            SELECT l.*, jl.*, sl.experience_years, bl.business_name, bl.industry, bl.established_year, bl.employee_count
            FROM listings l
            LEFT JOIN job_listings jl ON l.listing_id = jl.listing_id
            LEFT JOIN services_listings sl ON l.listing_id = sl.listing_id
            LEFT JOIN business_listings bl ON l.listing_id = bl.listing_id
            WHERE l.listing_id = ?
        ");
        $stmt->execute([$editId]);
        $editListing = $stmt->fetch();
        
        if ($editListing) {
            $stmt = $db->prepare("SELECT * FROM listing_images WHERE listing_id = ? ORDER BY sort_order");
            $stmt->execute([$editId]);
            $editGallery = $stmt->fetchAll();
            
            $stmt = $db->prepare("SELECT * FROM listing_price_list WHERE listing_id = ? ORDER BY item_category, sort_order");
            $stmt->execute([$editId]);
            $editPricelist = $stmt->fetchAll();
            
            // Fetch shop products for business listings
            $stmt = $db->prepare("SELECT sp.*, c.name as category_name, sc.name as subcategory_name FROM shop_products sp LEFT JOIN categories c ON sp.category_id = c.category_id LEFT JOIN categories sc ON sp.subcategory_id = sc.category_id WHERE sp.listing_id = ? ORDER BY sp.sort_order");
            $stmt->execute([$editId]);
            $editProducts = $stmt->fetchAll();
        }
    }
}

// Moderation - pending listings and products
if ($tab === 'moderation') {
    // Fetch auto-moderation settings
    $stmt = $db->prepare("SELECT setting_key, setting_value FROM settings WHERE setting_key IN ('auto_moderation_products', 'auto_moderation_listings')");
    $stmt->execute();
    $moderationSettings = [];
    while ($row = $stmt->fetch()) {
        $moderationSettings[$row['setting_key']] = $row['setting_value'] === 'true';
    }
    $autoModProducts = $moderationSettings['auto_moderation_products'] ?? false;
    $autoModListings = $moderationSettings['auto_moderation_listings'] ?? false;
    
    $pendingListings = $db->query("
        SELECT l.*, c.name as category_name, u.username 
        FROM listings l 
        LEFT JOIN categories c ON l.category_id = c.category_id 
        LEFT JOIN users u ON l.user_id = u.user_id 
        WHERE l.status = 'pending'
        ORDER BY l.created_at ASC
    ")->fetchAll();
    
    // Also get pending products (is_active = 0)
    $pendingProducts = $db->query("
        SELECT sp.*, c.name as category_name, l.user_id, u.username, bl.business_name as shop_name
        FROM shop_products sp
        LEFT JOIN categories c ON sp.category_id = c.category_id
        LEFT JOIN listings l ON sp.listing_id = l.listing_id
        LEFT JOIN users u ON l.user_id = u.user_id
        LEFT JOIN business_listings bl ON sp.listing_id = bl.listing_id
        WHERE sp.is_active = 0
        ORDER BY sp.created_at ASC
    ")->fetchAll();
}

// Users - for users tab and add/edit listing
if (in_array($tab, ['users', 'add_listing', 'edit_listing'])) {
    $users = $db->query("SELECT * FROM users ORDER BY username ASC")->fetchAll();
}

// Banners - only for banners tab
if ($tab === 'banners') {
    $banners = $db->query("SELECT * FROM banners ORDER BY placement, sort_order")->fetchAll();
}

// Products - for products tab
if ($tab === 'products' || $tab === 'add_product') {
    $allProducts = $db->query("
        SELECT sp.*, c.name as category_name, bl.business_name as shop_name, l.user_id, u.username
        FROM shop_products sp
        LEFT JOIN categories c ON sp.category_id = c.category_id
        LEFT JOIN listings l ON sp.listing_id = l.listing_id
        LEFT JOIN business_listings bl ON sp.listing_id = bl.listing_id
        LEFT JOIN users u ON l.user_id = u.user_id
        ORDER BY sp.created_at DESC
    ")->fetchAll();
    
    // Get business listings for dropdown
    $businessListings = $db->query("
        SELECT l.listing_id, bl.business_name, u.username
        FROM listings l
        INNER JOIN business_listings bl ON l.listing_id = bl.listing_id
        LEFT JOIN users u ON l.user_id = u.user_id
        WHERE l.status = 'active' AND l.listing_type = 'business'
        ORDER BY bl.business_name
    ")->fetchAll();
}

// Reviews - only for reviews tab
if ($tab === 'reviews') {
    $reviews = $db->query("
        SELECT r.*, l.title as listing_title, u.username as reviewer_name
        FROM reviews r
        LEFT JOIN listings l ON r.listing_id = l.listing_id
        LEFT JOIN users u ON r.reviewer_id = u.user_id
        ORDER BY r.created_at DESC
        LIMIT 100
    ")->fetchAll();
}

// Analytics - only for analytics tab
if ($tab === 'analytics') {
    $listingsPerDay = $db->query("
        SELECT DATE(created_at) as date, COUNT(*) as count 
        FROM listings 
        WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
        GROUP BY DATE(created_at) 
        ORDER BY date
    ")->fetchAll();
    
    $usersPerDay = $db->query("
        SELECT DATE(created_at) as date, COUNT(*) as count 
        FROM users 
        WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
        GROUP BY DATE(created_at) 
        ORDER BY date
    ")->fetchAll();
    
    $listingsByCategory = $db->query("
        SELECT c.name, COUNT(l.listing_id) as count 
        FROM categories c 
        LEFT JOIN listings l ON c.category_id = l.category_id 
        WHERE c.parent_id IS NULL 
        GROUP BY c.category_id, c.name 
        ORDER BY count DESC
    ")->fetchAll();
    
    $listingsByCity = $db->query("
        SELECT city, COUNT(*) as count 
        FROM listings 
        WHERE city IS NOT NULL 
        GROUP BY city 
        ORDER BY count DESC 
        LIMIT 10
    ")->fetchAll();
    
    $listingsByType = $db->query("
        SELECT listing_type, COUNT(*) as count 
        FROM listings 
        GROUP BY listing_type
    ")->fetchAll();
}

// Enquiries - only for enquiries tab
if ($tab === 'enquiries') {
    $enquiriesError = null;
    try {
        $enquiriesList = $db->query("
            SELECT e.*, l.title as listing_title, u.username 
            FROM enquiries e 
            LEFT JOIN listings l ON e.listing_id = l.listing_id 
            LEFT JOIN users u ON e.user_id = u.user_id 
            ORDER BY e.created_at DESC 
            LIMIT 100
        ")->fetchAll();
    } catch (Exception $e) {
        $enquiriesList = [];
        $enquiriesError = $e->getMessage();
    }
}

// Notification logs - only for notifications tab
if ($tab === 'notifications') {
    try {
        $notificationLogs = $db->query("
            SELECT * FROM notification_logs 
            ORDER BY created_at DESC 
            LIMIT 50
        ")->fetchAll();
    } catch (Exception $e) {
        // Table might not exist yet
    }
}

// OTP/SMS logs - only for sms_logs tab
if ($tab === 'sms_logs') {
    try {
        $otpLogs = $db->query("
            SELECT id, phone, purpose, attempts, expires_at, created_at 
            FROM otp_verifications 
            ORDER BY created_at DESC 
            LIMIT 100
        ")->fetchAll();
    } catch (Exception $e) {
        // Table might not exist
    }
}

// Orders - only for orders tab
if ($tab === 'orders' || $tab === 'order_detail') {
    try {
        $ordersWhere = [];
        $ordersParams = [];
        
        if ($orderFilterStatus) {
            $ordersWhere[] = "o.order_status = ?";
            $ordersParams[] = $orderFilterStatus;
        }
        if ($orderFilterPayment) {
            $ordersWhere[] = "o.payment_status = ?";
            $ordersParams[] = $orderFilterPayment;
        }
        
        $ordersWhereStr = $ordersWhere ? 'WHERE ' . implode(' AND ', $ordersWhere) : '';
        
        $stmt = $db->prepare("
            SELECT o.*, u.username, u.phone as user_phone,
                   (SELECT COUNT(*) FROM order_items WHERE order_id = o.order_id) as item_count
            FROM orders o 
            LEFT JOIN users u ON o.user_id = u.user_id 
            $ordersWhereStr
            ORDER BY o.created_at DESC 
            LIMIT 200
        ");
        $stmt->execute($ordersParams);
        $orders = $stmt->fetchAll();
        
        // If viewing order detail
        $orderDetail = null;
        $orderItems = [];
        if ($tab === 'order_detail' && isset($_GET['id'])) {
            $orderId = (int)$_GET['id'];
            $stmt = $db->prepare("
                SELECT o.*, u.username, u.phone as user_phone, u.email as user_email,
                       a.name as addr_name, a.phone as addr_phone, a.address_line1, a.address_line2,
                       a.city as addr_city, a.state as addr_state, a.pincode as addr_pincode
                FROM orders o 
                LEFT JOIN users u ON o.user_id = u.user_id 
                LEFT JOIN user_addresses a ON o.address_id = a.address_id
                WHERE o.order_id = ?
            ");
            $stmt->execute([$orderId]);
            $orderDetail = $stmt->fetch();
            
            if ($orderDetail) {
                $stmt = $db->prepare("
                    SELECT oi.*, l.title, l.main_image_url, us.username as seller_name
                    FROM order_items oi
                    LEFT JOIN listings l ON oi.listing_id = l.listing_id
                    LEFT JOIN users us ON oi.seller_id = us.user_id
                    WHERE oi.order_id = ?
                ");
                $stmt->execute([$orderId]);
                $orderItems = $stmt->fetchAll();
            }
        }
    } catch (Exception $e) {
        // Orders table might not exist
    }
}

// Pincodes - only for pincodes tab
if ($tab === 'pincodes') {
    try {
        $pincodes = $db->query("
            SELECT * FROM service_pincodes 
            ORDER BY is_serviceable DESC, delivery_days ASC, city_name
        ")->fetchAll();
    } catch (Exception $e) {
        // Table might not exist
    }
}

// Leads - only for leads tab
$leadsList = [];
$leadsError = null;
if ($tab === 'leads') {
    try {
        $leadsList = $db->query("
            SELECT e.*, l.title as listing_title, l.listing_type, u.username, u.phone as user_phone
            FROM enquiries e
            LEFT JOIN listings l ON e.listing_id = l.listing_id
            LEFT JOIN users u ON e.user_id = u.user_id
            ORDER BY e.created_at DESC
            LIMIT 200
        ")->fetchAll();
    } catch (Exception $e) {
        $leadsError = $e->getMessage();
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hello Hingoli Admin Panel</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@picocss/pico@1/css/pico.min.css">
    <link rel="stylesheet" href="admin.css">
    <script src="admin.js" defer></script>
</head>
<body>
    <aside class="sidebar">
        <div class="sidebar-header">
            <h2>🏠 <span>Admin Panel</span></h2>
        </div>
        <nav class="sidebar-nav">
            <a href="?tab=dashboard" class="<?= $tab === 'dashboard' ? 'active' : '' ?>"><span class="nav-icon">📊</span> <span>Dashboard</span></a>
            <a href="?tab=orders" class="<?= $tab === 'orders' || $tab === 'order_detail' ? 'active' : '' ?>"><span class="nav-icon">🛒</span> <span>Orders</span> <?= $stats['pending_orders'] > 0 ? '<span class="badge badge-warning">' . $stats['pending_orders'] . '</span>' : '' ?></a>
            <a href="?tab=listings" class="<?= $tab === 'listings' || $tab === 'edit_listing' ? 'active' : '' ?>"><span class="nav-icon">📋</span> <span>Listings</span></a>
            <a href="?tab=moderation" class="<?= $tab === 'moderation' ? 'active' : '' ?>"><span class="nav-icon">🛡️</span> <span>Moderation</span> <?= $stats['pending_listings'] > 0 ? '<span class="badge badge-danger">' . $stats['pending_listings'] . '</span>' : '' ?></a>
            <a href="?tab=add_listing" class="<?= $tab === 'add_listing' ? 'active' : '' ?>"><span class="nav-icon">➕</span> <span>Add Listing</span></a>
            <a href="?tab=products" class="<?= $tab === 'products' || $tab === 'add_product' ? 'active' : '' ?>"><span class="nav-icon">🛍️</span> <span>Products</span></a>
            <a href="?tab=categories" class="<?= $tab === 'categories' ? 'active' : '' ?>"><span class="nav-icon">📁</span> <span>Categories</span></a>
            <a href="?tab=subcategories" class="<?= $tab === 'subcategories' ? 'active' : '' ?>"><span class="nav-icon">📂</span> <span>Subcategories</span></a>
            <a href="?tab=users" class="<?= $tab === 'users' ? 'active' : '' ?>"><span class="nav-icon">👥</span> <span>Users</span></a>
            <a href="?tab=banners" class="<?= $tab === 'banners' ? 'active' : '' ?>"><span class="nav-icon">🖼️</span> <span>Banners</span></a>
            <a href="?tab=cities" class="<?= $tab === 'cities' ? 'active' : '' ?>"><span class="nav-icon">🏙️</span> <span>Cities</span></a>
            <a href="?tab=pincodes" class="<?= $tab === 'pincodes' ? 'active' : '' ?>"><span class="nav-icon">📍</span> <span>Delivery</span></a>
            <a href="?tab=reviews" class="<?= $tab === 'reviews' ? 'active' : '' ?>"><span class="nav-icon">⭐</span> <span>Reviews</span></a>
            <a href="?tab=leads" class="<?= $tab === 'leads' ? 'active' : '' ?>"><span class="nav-icon">📞</span> <span>Leads</span></a>
            <a href="?tab=analytics" class="<?= $tab === 'analytics' ? 'active' : '' ?>"><span class="nav-icon">📈</span> <span>Analytics</span></a>
            <a href="?tab=notifications" class="<?= $tab === 'notifications' ? 'active' : '' ?>"><span class="nav-icon">📲</span> <span>Notifications</span></a>
            <a href="?tab=export" class="<?= $tab === 'export' ? 'active' : '' ?>"><span class="nav-icon">📥</span> <span>Export</span></a>
            <a href="?tab=sms_logs" class="<?= $tab === 'sms_logs' ? 'active' : '' ?>"><span class="nav-icon">📱</span> <span>SMS Logs</span></a>
            <a href="?tab=app_config" class="<?= $tab === 'app_config' ? 'active' : '' ?>"><span class="nav-icon">📲</span> <span>App Version</span></a>
            <a href="?tab=settings" class="<?= $tab === 'settings' ? 'active' : '' ?>"><span class="nav-icon">⚙️</span> <span>Settings</span></a>
        </nav>
    </aside>

    <div class="main-wrapper">

    <div class="main-content">
        <?php if ($message): ?><div class="success"><?= htmlspecialchars($message) ?></div><?php endif; ?>
        <?php if ($error): ?><div class="error"><?= htmlspecialchars($error) ?></div><?php endif; ?>

        <?php if ($tab === 'dashboard'): ?>
        <!-- ========== DASHBOARD ========== -->
        <h2>📊 Dashboard</h2>
        <div class="stats-grid">
            <div class="stat-card"><h3><?= $stats['total_listings'] ?></h3><p>Total Listings</p></div>
            <div class="stat-card"><h3><?= $stats['active_listings'] ?></h3><p>Active Listings</p></div>
            <div class="stat-card"><h3><?= $stats['total_users'] ?></h3><p>Users</p></div>
            <div class="stat-card"><h3><?= $stats['total_categories'] ?></h3><p>Categories</p></div>
        </div>
        <div class="stats-grid">
            <div class="stat-card"><h3><?= $stats['services_count'] ?></h3><p>🔧 Services</p></div>
            <div class="stat-card"><h3><?= $stats['selling_count'] ?></h3><p>🛒 Selling</p></div>
            <div class="stat-card"><h3><?= $stats['business_count'] ?></h3><p>🏢 Business</p></div>
            <div class="stat-card"><h3><?= $stats['jobs_count'] ?></h3><p>💼 Jobs</p></div>
        </div>

        <div class="card">
            <div class="card-header">Recent Listings</div>
            <table>
                <thead><tr><th>ID</th><th>Title</th><th>Type</th><th>Category</th><th>Status</th><th>Created</th></tr></thead>
                <tbody>
                <?php foreach (array_slice($listings, 0, 10) as $l): ?>
                <tr>
                    <td><?= $l['listing_id'] ?></td>
                    <td><?= htmlspecialchars(substr($l['title'], 0, 40)) ?></td>
                    <td><span class="badge badge-info"><?= $l['listing_type'] ?></span></td>
                    <td><?= htmlspecialchars($l['category_name']) ?></td>
                    <td><span class="badge <?= $l['status'] === 'active' ? 'badge-success' : 'badge-warning' ?>"><?= $l['status'] ?></span></td>
                    <td><?= date('M d', strtotime($l['created_at'])) ?></td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
        </div>

        <?php elseif ($tab === 'add_listing'): ?>
        <!-- ========== ADD LISTING ========== -->
        <h2>➕ Add New Listing</h2>
        <div class="card">
            <form method="POST" action="?tab=add_listing" enctype="multipart/form-data">
                <input type="hidden" name="action" value="add_listing">
                
                <div class="form-grid">
                    <label>
                        Listing Type *
                        <select name="listing_type" id="listing_type" required onchange="toggleTypeFields(); filterCategoriesByType();">
                            <option value="services">🔧 Services</option>
                            <option value="selling">🛒 Selling</option>
                            <option value="business">🏢 Business</option>
                            <option value="jobs">💼 Jobs</option>
                        </select>
                    </label>
                    <label>
                        Category *
                        <select name="category_id" id="category_id" required onchange="filterSubcategories()">
                            <option value="">Select Category...</option>
                            <?php foreach ($categories as $cat): ?>
                            <option value="<?= $cat['category_id'] ?>" data-type="<?= $cat['listing_type'] ?>"><?= htmlspecialchars($cat['name']) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </label>
                    <label>
                        Subcategory
                        <select name="subcategory_id" id="subcategory_id">
                            <option value="">Select Subcategory...</option>
                            <?php foreach ($subcategories as $sub): ?>
                            <option value="<?= $sub['category_id'] ?>" data-parent-id="<?= $sub['parent_id'] ?>"><?= htmlspecialchars($sub['name']) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </label>
                    <label>
                        User ID
                        <select name="user_id">
                            <?php foreach ($users as $user): ?>
                            <option value="<?= $user['user_id'] ?>"><?= htmlspecialchars($user['username']) ?> (<?= $user['phone'] ?>)</option>
                            <?php endforeach; ?>
                        </select>
                    </label>
                </div>

                <label>
                    Title *
                    <input type="text" name="title" required placeholder="Enter listing title">
                </label>
                <label>
                    Description
                    <textarea name="description" rows="3" placeholder="Describe your listing..."></textarea>
                </label>

                <div class="form-grid">
                    <label id="price-field-container">
                        Price (₹)
                        <input type="number" name="price" step="0.01" placeholder="e.g. 500">
                        <small style="color: #64748b;">Not required for Business listings</small>
                    </label>
                    <label>
                        Location
                        <input type="text" name="location" placeholder="e.g. Main Market, Station Road">
                    </label>
                    <label>
                        City *
                        <select name="city" required>
                            <?php foreach ($cities as $c): ?>
                            <option value="<?= $c['name'] ?>"><?= htmlspecialchars($c['name']) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </label>
                </div>
                
                <!-- Image Upload Section -->
                <div style="margin: 20px 0; padding: 20px; background: #f8fafc; border-radius: 12px; border: 1px dashed #cbd5e1;">
                    <strong>📷 Images</strong>
                    <div class="form-grid" style="margin-top: 15px;">
                        <label>
                            Main Image (Upload)
                            <input type="file" name="main_image" accept="image/*">
                            <small style="color: #64748b;">JPG, PNG, WebP • Max 5MB</small>
                        </label>
                        <label>
                            Or Image URL
                            <input type="url" name="main_image_url" placeholder="https://...">
                        </label>
                    </div>
                    <label style="margin-top: 15px;">
                        Gallery Images (Upload Multiple)
                        <input type="file" name="gallery_images[]" accept="image/*" multiple>
                        <small style="color: #64748b;">Select multiple images for gallery</small>
                    </label>
                </div>

                <!-- Service Fields -->
                <div class="service-fields listing-type-section" id="service-fields">
                    <strong>🔧 Service Details</strong>
                    <div class="form-grid" style="margin-top: 10px;">
                        <label>Experience (Years)<input type="number" name="experience_years" min="0" placeholder="e.g. 5"></label>
                    </div>
                </div>

                <!-- Job Fields -->
                <div class="job-fields listing-type-section" id="job-fields">
                    <strong>💼 Job Details</strong>
                    <div class="form-grid" style="margin-top: 10px;">
                        <label>Min Salary (₹)<input type="number" name="salary_min" placeholder="e.g. 15000"></label>
                        <label>Max Salary (₹)<input type="number" name="salary_max" placeholder="e.g. 25000"></label>
                        <label>Salary Period
                            <select name="salary_period">
                                <option value="monthly">Monthly</option>
                                <option value="yearly">Yearly</option>
                                <option value="hourly">Hourly</option>
                                <option value="daily">Daily</option>
                            </select>
                        </label>
                        <label>Employment Type
                            <select name="employment_type">
                                <option value="full_time">Full Time</option>
                                <option value="part_time">Part Time</option>
                                <option value="contract">Contract</option>
                                <option value="internship">Internship</option>
                            </select>
                        </label>
                        <label>Work Location
                            <select name="remote_option">
                                <option value="on_site">On Site</option>
                                <option value="remote">Remote</option>
                                <option value="hybrid">Hybrid</option>
                            </select>
                        </label>
                        <label>Vacancies<input type="number" name="vacancies" min="1" value="1"></label>
                        <label>Education Required<input type="text" name="education_required" placeholder="e.g. 10th Pass"></label>
                        <label>Experience Required (Years)<input type="number" name="experience_required" min="0" placeholder="e.g. 2"></label>
                    </div>
                </div>

                <!-- Business Fields -->
                <div class="business-fields listing-type-section" id="business-fields">
                    <strong>🏢 Business Details</strong>
                    <div class="form-grid" style="margin-top: 10px;">
                        <label>Business Name<input type="text" name="business_name" placeholder="Business Name"></label>
                        <label>Industry<input type="text" name="industry" placeholder="e.g. Retail, Food"></label>
                        <label>Established Year<input type="number" name="established_year" min="1900" max="2025" placeholder="e.g. 2015"></label>
                        <label>Employee Count
                            <select name="employee_count">
                                <option value="1-10">1-10</option>
                                <option value="11-50">11-50</option>
                                <option value="51-200">51-200</option>
                                <option value="200+">200+</option>
                            </select>
                        </label>
                    </div>
                </div>

                <div class="form-grid">
                    <label>
                        Status
                        <select name="status">
                            <option value="active">Active</option>
                            <option value="pending">Pending</option>
                            <option value="draft">Draft</option>
                        </select>
                    </label>
                    <label style="display: flex; align-items: center; gap: 10px; padding-top: 25px;">
                        <input type="checkbox" name="is_verified" style="width: auto;"> Verified
                    </label>
                    <label style="display: flex; align-items: center; gap: 10px; padding-top: 25px;">
                        <input type="checkbox" name="is_featured" style="width: auto;"> Featured
                    </label>
                </div>

                <button type="submit" style="margin-top: 20px;">➕ Add Listing</button>
            </form>
        </div>

        <?php elseif ($tab === 'listings' || $tab === 'edit_listing'): ?>
        <!-- ========== LISTINGS ========== -->
        <h2>📋 All Listings</h2>
        
        <?php if ($editListing): ?>
        <!-- Edit Listing Notice - stays at top -->
        <div class="card" style="margin-bottom: 15px; background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: white;">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <strong>✏️ Editing:</strong> <?= htmlspecialchars($editListing['title']) ?>
                    <span class="badge" style="background: rgba(255,255,255,0.2); margin-left: 10px;"><?= $editListing['listing_type'] ?></span>
                </div>
                <a href="?tab=listings" style="color: white; text-decoration: underline;">Cancel Edit</a>
            </div>
        </div>
        <?php endif; ?>
        
        <?php if (!$editListing): ?>
        <!-- Search and Filter - only show when not editing -->
        <div class="card" style="margin-bottom: 15px;">
            <form method="GET" style="display: flex; gap: 10px; flex-wrap: wrap; align-items: end;">
                <input type="hidden" name="tab" value="listings">
                <label style="flex: 2; min-width: 200px;">
                    🔍 Search
                    <input type="text" name="search" value="<?= htmlspecialchars($search) ?>" placeholder="Search title or description...">
                </label>
                <label style="flex: 1; min-width: 120px;">
                    Type
                    <select name="filter_type">
                        <option value="">All Types</option>
                        <option value="services" <?= $filterType === 'services' ? 'selected' : '' ?>>Services</option>
                        <option value="selling" <?= $filterType === 'selling' ? 'selected' : '' ?>>Selling</option>
                        <option value="business" <?= $filterType === 'business' ? 'selected' : '' ?>>Business</option>
                        <option value="jobs" <?= $filterType === 'jobs' ? 'selected' : '' ?>>Jobs</option>
                    </select>
                </label>
                <label style="flex: 1; min-width: 120px;">
                    Status
                    <select name="filter_status">
                        <option value="">All Status</option>
                        <option value="active" <?= $filterStatus === 'active' ? 'selected' : '' ?>>Active</option>
                        <option value="pending" <?= $filterStatus === 'pending' ? 'selected' : '' ?>>Pending</option>
                        <option value="draft" <?= $filterStatus === 'draft' ? 'selected' : '' ?>>Draft</option>
                        <option value="rejected" <?= $filterStatus === 'rejected' ? 'selected' : '' ?>>Rejected</option>
                    </select>
                </label>
                <label style="flex: 1; min-width: 120px;">
                    City
                    <select name="filter_city">
                        <option value="">All Cities</option>
                        <?php foreach ($cities as $c): ?>
                        <option value="<?= $c['name'] ?>" <?= $filterCity === $c['name'] ? 'selected' : '' ?>><?= htmlspecialchars($c['name']) ?></option>
                        <?php endforeach; ?>
                    </select>
                </label>
                <button type="submit" style="height: 48px;">Filter</button>
                <?php if ($search || $filterType || $filterStatus || $filterCity): ?>
                <a href="?tab=listings" style="height: 48px; line-height: 48px;">Clear</a>
                <?php endif; ?>
            </form>
        </div>
        
        <div class="card">
            <div class="card-header" style="display: flex; justify-content: space-between;">
                <span>Showing <?= count($listings) ?> listings</span>
                <a href="?tab=add_listing">➕ Add New</a>
            </div>
            <div style="overflow-x: auto;">
            <table style="min-width: 1000px;">
                <thead>
                    <tr><th style="width:40px;">ID</th><th style="width:50px;">Image</th><th style="max-width:180px;">Title</th><th>Type</th><th style="max-width:120px;">Category</th><th>Price</th><th>City</th><th>Owner</th><th>Status</th><th style="width:160px;">Actions</th></tr>
                </thead>
                <tbody>
                <?php foreach ($listings as $l): ?>
                <tr>
                    <td><?= $l['listing_id'] ?></td>
                    <td><?php if($l['main_image_url']): ?><img src="<?= $l['main_image_url'] ?>" style="width:40px;height:40px;object-fit:cover;border-radius:4px;"><?php else: ?>-<?php endif; ?></td>
                    <td style="max-width:180px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;" title="<?= htmlspecialchars($l['title']) ?>">
                        <strong><?= htmlspecialchars(substr($l['title'], 0, 28)) ?></strong>
                        <?php if($l['is_verified']): ?><span class="badge badge-success">✓</span><?php endif; ?>
                        <?php if($l['is_featured']): ?><span class="badge badge-warning">★</span><?php endif; ?>
                    </td>
                    <td><span class="badge badge-info"><?= $l['listing_type'] ?></span></td>
                    <td style="max-width:120px; overflow:hidden; text-overflow:ellipsis;" title="<?= htmlspecialchars($l['category_name']) ?>"><?= htmlspecialchars(substr($l['category_name'], 0, 15)) ?></td>
                    <td style="white-space:nowrap;">
                        <?php if ($l['listing_type'] === 'jobs' && $l['salary_min']): ?>
                            ₹<?= number_format($l['salary_min']/1000) ?>k
                        <?php elseif ($l['price']): ?>
                            ₹<?= number_format($l['price']) ?>
                        <?php else: ?>
                            -
                        <?php endif; ?>
                    </td>
                    <td><?= htmlspecialchars(substr($l['city'], 0, 10)) ?></td>
                    <td>
                        <span title="User ID: <?= $l['user_id'] ?>" style="cursor:help;"><?= htmlspecialchars(substr($l['username'] ?? 'ID:'.$l['user_id'], 0, 10)) ?></span>
                    </td>
                    <td><span class="badge <?= $l['status'] === 'active' ? 'badge-success' : 'badge-warning' ?>"><?= $l['status'] ?></span></td>
                    <td style="white-space: nowrap;">
                        <a href="?tab=edit_listing&edit=<?= $l['listing_id'] ?>" class="btn-sm outline" title="Edit">✏️</a>
                        <?php if (!$l['is_verified']): ?>
                        <form method="POST" style="display:inline;">
                            <input type="hidden" name="action" value="verify_listing">
                            <input type="hidden" name="listing_id" value="<?= $l['listing_id'] ?>">
                            <button type="submit" class="btn-sm outline" title="Verify">✓</button>
                        </form>
                        <?php endif; ?>
                        <button type="button" class="btn-sm outline" title="Transfer Owner" onclick="document.getElementById('transfer-<?= $l['listing_id'] ?>').style.display='table-row'">👤</button>
                        <form method="POST" style="display:inline;" onsubmit="return confirm('Delete this listing?')">
                            <input type="hidden" name="action" value="delete_listing">
                            <input type="hidden" name="listing_id" value="<?= $l['listing_id'] ?>">
                            <button type="submit" class="btn-sm contrast outline" title="Delete">🗑</button>
                        </form>
                    </td>
                </tr>
                <!-- Transfer Row -->
                <tr id="transfer-<?= $l['listing_id'] ?>" style="display:none; background:#f0f9ff;">
                    <td colspan="10">
                        <form method="POST" style="display:flex; gap:10px; align-items:center; padding:5px;">
                            <input type="hidden" name="action" value="transfer_listing">
                            <input type="hidden" name="listing_id" value="<?= $l['listing_id'] ?>">
                            <span>Transfer "<strong><?= htmlspecialchars(substr($l['title'], 0, 20)) ?></strong>" to:</span>
                            <input type="number" name="new_user_id" placeholder="User ID" required style="width:100px;margin:0;">
                            <button type="submit" class="btn-sm">Transfer</button>
                            <button type="button" class="btn-sm outline" onclick="this.closest('tr').style.display='none'">Cancel</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            </div>
        </div>
        <?php endif; ?>
        
        <!-- Edit Listing Form -->
        <?php if ($editListing): ?>
        <div class="card" style="border: 2px solid var(--primary);">
            <div class="card-header">✏️ Edit Listing: <?= htmlspecialchars($editListing['title']) ?></div>
            <form method="POST" enctype="multipart/form-data">
                <input type="hidden" name="action" value="edit_listing">
                <input type="hidden" name="listing_id" value="<?= $editListing['listing_id'] ?>">
                <input type="hidden" name="existing_image" value="<?= $editListing['main_image_url'] ?>">
                
                <div class="form-grid">
                    <label>Title * <input type="text" name="title" value="<?= htmlspecialchars($editListing['title']) ?>" required></label>
                    <label>Category
                        <select name="category_id">
                            <?php foreach ($categories as $cat): ?>
                            <option value="<?= $cat['category_id'] ?>" <?= $editListing['category_id'] == $cat['category_id'] ? 'selected' : '' ?>><?= htmlspecialchars($cat['name']) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </label>
                    <label>Subcategory
                        <select name="subcategory_id">
                            <option value="">None</option>
                            <?php foreach ($subcategories as $sub): ?>
                            <option value="<?= $sub['category_id'] ?>" <?= $editListing['subcategory_id'] == $sub['category_id'] ? 'selected' : '' ?>><?= htmlspecialchars($sub['name']) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </label>
                    <label>Status
                        <select name="status">
                            <option value="active" <?= $editListing['status'] === 'active' ? 'selected' : '' ?>>Active</option>
                            <option value="pending" <?= $editListing['status'] === 'pending' ? 'selected' : '' ?>>Pending</option>
                            <option value="draft" <?= $editListing['status'] === 'draft' ? 'selected' : '' ?>>Draft</option>
                            <option value="rejected" <?= $editListing['status'] === 'rejected' ? 'selected' : '' ?>>Rejected</option>
                        </select>
                    </label>
                </div>
                
                <label>Description <textarea name="description" rows="3"><?= htmlspecialchars($editListing['description']) ?></textarea></label>
                
                <div class="form-grid">
                    <?php if ($editListing['listing_type'] !== 'business'): ?>
                    <label>Price (₹) <input type="number" name="price" value="<?= $editListing['price'] ?>" step="0.01"></label>
                    <?php endif; ?>
                    <label>Location <input type="text" name="location" value="<?= htmlspecialchars($editListing['location'] ?? '') ?>"></label>
                    <label>City
                        <select name="city">
                            <?php foreach ($cities as $c): ?>
                            <option value="<?= $c['name'] ?>" <?= $editListing['city'] === $c['name'] ? 'selected' : '' ?>><?= htmlspecialchars($c['name']) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </label>
                </div>
                
                <!-- Type-specific fields -->
                <?php if ($editListing['listing_type'] === 'services'): ?>
                <div style="padding: 15px; background: #f0f9ff; border-radius: 8px; margin: 15px 0;">
                    <strong>🔧 Service Details</strong>
                    <label style="margin-top: 10px;">Experience (Years) <input type="number" name="experience_years" value="<?= $editListing['experience_years'] ?? 0 ?>"></label>
                </div>
                <?php elseif ($editListing['listing_type'] === 'jobs'): ?>
                <div style="padding: 15px; background: #fef3c7; border-radius: 8px; margin: 15px 0;">
                    <strong>💼 Job Details</strong>
                    <div class="form-grid" style="margin-top: 10px;">
                        <label>Min Salary <input type="number" name="salary_min" value="<?= $editListing['salary_min'] ?>"></label>
                        <label>Max Salary <input type="number" name="salary_max" value="<?= $editListing['salary_max'] ?>"></label>
                        <label>Period <select name="salary_period">
                            <option value="monthly" <?= ($editListing['salary_period'] ?? '') === 'monthly' ? 'selected' : '' ?>>Monthly</option>
                            <option value="yearly" <?= ($editListing['salary_period'] ?? '') === 'yearly' ? 'selected' : '' ?>>Yearly</option>
                            <option value="daily" <?= ($editListing['salary_period'] ?? '') === 'daily' ? 'selected' : '' ?>>Daily</option>
                        </select></label>
                        <label>Employment <select name="employment_type">
                            <option value="full_time" <?= ($editListing['employment_type'] ?? '') === 'full_time' ? 'selected' : '' ?>>Full Time</option>
                            <option value="part_time" <?= ($editListing['employment_type'] ?? '') === 'part_time' ? 'selected' : '' ?>>Part Time</option>
                            <option value="contract" <?= ($editListing['employment_type'] ?? '') === 'contract' ? 'selected' : '' ?>>Contract</option>
                        </select></label>
                        <label>Vacancies <input type="number" name="vacancies" value="<?= $editListing['vacancies'] ?? 1 ?>"></label>
                        <label>Education <input type="text" name="education_required" value="<?= htmlspecialchars($editListing['education_required'] ?? '') ?>"></label>
                    </div>
                </div>
                <?php elseif ($editListing['listing_type'] === 'business'): ?>
                <div style="padding: 15px; background: #dcfce7; border-radius: 8px; margin: 15px 0;">
                    <strong>🏢 Business Details</strong>
                    <div class="form-grid" style="margin-top: 10px;">
                        <label>Business Name <input type="text" name="business_name" value="<?= htmlspecialchars($editListing['business_name'] ?? '') ?>"></label>
                        <label>Industry <input type="text" name="industry" value="<?= htmlspecialchars($editListing['industry'] ?? '') ?>"></label>
                        <label>Established Year <input type="number" name="established_year" value="<?= $editListing['established_year'] ?>"></label>
                        <label>Employees <select name="employee_count">
                            <option value="1-10" <?= ($editListing['employee_count'] ?? '') === '1-10' ? 'selected' : '' ?>>1-10</option>
                            <option value="11-50" <?= ($editListing['employee_count'] ?? '') === '11-50' ? 'selected' : '' ?>>11-50</option>
                            <option value="51-200" <?= ($editListing['employee_count'] ?? '') === '51-200' ? 'selected' : '' ?>>51-200</option>
                        </select></label>
                    </div>
                </div>
                <?php endif; ?>
                
                <!-- Image -->
                <div class="form-grid">
                    <label>Current Image
                        <?php if ($editListing['main_image_url']): ?>
                        <img src="<?= $editListing['main_image_url'] ?>" style="height: 80px; border-radius: 8px;">
                        <?php else: ?>
                        <span style="color: #94a3b8;">No image</span>
                        <?php endif; ?>
                    </label>
                    <label>New Image (Upload) <input type="file" name="main_image" accept="image/*"></label>
                    <label>Or Image URL <input type="url" name="main_image_url" placeholder="https://..."></label>
                </div>
                
                <div class="form-grid" style="margin-top: 15px;">
                    <label style="display: flex; align-items: center; gap: 10px;"><input type="checkbox" name="is_verified" style="width: auto;" <?= $editListing['is_verified'] ? 'checked' : '' ?>> Verified</label>
                    <label style="display: flex; align-items: center; gap: 10px;"><input type="checkbox" name="is_featured" style="width: auto;" <?= $editListing['is_featured'] ? 'checked' : '' ?>> Featured</label>
                </div>
                
                <div style="margin-top: 20px;">
                    <button type="submit">💾 Save Changes</button>
                    <a href="?tab=listings" style="margin-left: 15px;">Cancel</a>
                </div>
            </form>
        </div>
        
        <!-- Gallery Images Section -->
        <div class="card" style="margin-top: 20px;">
            <div class="card-header">🖼️ Gallery Images (<?= count($editGallery) ?>)</div>
            
            <?php if (!empty($editGallery)): ?>
            <div style="display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 15px;">
                <?php foreach ($editGallery as $img): ?>
                <div style="position: relative;">
                    <img src="<?= $img['image_url'] ?>" style="width: 100px; height: 80px; object-fit: cover; border-radius: 8px;">
                    <form method="POST" style="position: absolute; top: -5px; right: -5px;">
                        <input type="hidden" name="action" value="delete_gallery_image">
                        <input type="hidden" name="image_id" value="<?= $img['image_id'] ?>">
                        <button type="submit" onclick="return confirm('Delete?')" style="background: #dc2626; color: white; border: none; border-radius: 50%; width: 20px; height: 20px; cursor: pointer; font-size: 12px;">×</button>
                    </form>
                </div>
                <?php endforeach; ?>
            </div>
            <?php endif; ?>
            
            <form method="POST" style="display: flex; gap: 10px;">
                <input type="hidden" name="action" value="add_gallery_image">
                <input type="hidden" name="listing_id" value="<?= $editListing['listing_id'] ?>">
                <input type="url" name="image_url" placeholder="Image URL (https://...)" required style="flex: 1; margin: 0;">
                <button type="submit" class="btn-sm">➕ Add</button>
            </form>
        </div>
        
        <!-- Pricelist Section -->
        <div class="card" style="margin-top: 20px;">
            <div class="card-header">💰 Pricelist / Menu (<?= count($editPricelist) ?>)</div>
            
            <?php if (!empty($editPricelist)): ?>
            <table style="margin-bottom: 15px;">
                <thead><tr><th>Item</th><th>Category</th><th>Price</th><th>Offer</th><th>Duration</th><th>Actions</th></tr></thead>
                <tbody>
                <?php foreach ($editPricelist as $item): ?>
                <tr>
                    <form method="POST">
                    <input type="hidden" name="action" value="edit_pricelist_item">
                    <input type="hidden" name="item_id" value="<?= $item['item_id'] ?>">
                    <input type="hidden" name="listing_id" value="<?= $editListing['listing_id'] ?>">
                    <td><input type="text" name="item_name" value="<?= htmlspecialchars($item['item_name']) ?>" required style="margin:0;padding:5px;width:120px;"></td>
                    <td><input type="text" name="item_category" value="<?= htmlspecialchars($item['item_category'] ?? '') ?>" placeholder="Category" style="margin:0;padding:5px;width:80px;"></td>
                    <td><input type="number" name="price" value="<?= $item['price'] ?>" required style="margin:0;padding:5px;width:70px;"></td>
                    <td><input type="number" name="discounted_price" value="<?= $item['discounted_price'] ?>" placeholder="Offer" style="margin:0;padding:5px;width:70px;"></td>
                    <td><input type="number" name="duration_minutes" value="<?= $item['duration_minutes'] ?>" placeholder="min" style="margin:0;padding:5px;width:50px;"></td>
                    <td style="white-space:nowrap;">
                        <button type="submit" class="btn-sm outline">💾</button>
                    </form>
                        <form method="POST" style="display:inline;">
                            <input type="hidden" name="action" value="delete_pricelist_item">
                            <input type="hidden" name="item_id" value="<?= $item['item_id'] ?>">
                            <input type="hidden" name="listing_id" value="<?= $editListing['listing_id'] ?>">
                            <button type="submit" onclick="return confirm('Delete?')" class="btn-sm contrast outline">🗑</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            <?php endif; ?>
            
            <form method="POST" class="form-grid" style="background: #f8fafc; padding: 15px; border-radius: 8px;">
                <input type="hidden" name="action" value="add_pricelist_item">
                <input type="hidden" name="listing_id" value="<?= $editListing['listing_id'] ?>">
                <label>Item Name * <input type="text" name="item_name" required placeholder="e.g. Haircut"></label>
                <label>Category <input type="text" name="item_category" placeholder="e.g. Hair"></label>
                <label>Price (₹) * <input type="number" name="price" required placeholder="500"></label>
                <label>Offer Price <input type="number" name="discounted_price" placeholder="400"></label>
                <label>Duration (min) <input type="number" name="duration_minutes" placeholder="30"></label>
                <label>Description <input type="text" name="item_description" placeholder="Optional"></label>
                <button type="submit" style="grid-column: span 2;">➕ Add Pricelist Item</button>
            </form>
        </div>
        
        <?php if ($editListing['listing_type'] === 'business'): ?>
        <!-- Shop Products Section (Business Listings Only) -->
        <div class="card" style="margin-top: 20px;">
            <div class="card-header" style="background: #dcfce7;">🛒 Shop Products (<?= count($editProducts ?? []) ?>)</div>
            <p style="color: #64748b; font-size: 0.9em; margin-bottom: 15px;">Products that this business sells. Enable "Sell Online" to show in the Selling feed.</p>
            
            <?php if (!empty($editProducts)): ?>
            <table style="margin-bottom: 15px;">
                <thead><tr><th>Image</th><th>Product</th><th>Category</th><th>Price</th><th>Sell Online</th><th>Actions</th></tr></thead>
                <tbody>
                <?php foreach ($editProducts as $prod): ?>
                <tr>
                    <td>
                        <?php if ($prod['image_url']): ?>
                        <img src="<?= $prod['image_url'] ?>" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;">
                        <?php else: ?>
                        <span style="color: #94a3b8;">-</span>
                        <?php endif; ?>
                    </td>
                    <td>
                        <strong><?= htmlspecialchars($prod['product_name']) ?></strong>
                        <?php if ($prod['description']): ?>
                        <br><small style="color: #64748b;"><?= htmlspecialchars(substr($prod['description'], 0, 50)) ?>...</small>
                        <?php endif; ?>
                    </td>
                    <td><?= htmlspecialchars($prod['category_name'] ?? '-') ?></td>
                    <td>
                        ₹<?= number_format($prod['price']) ?>
                        <?php if ($prod['discounted_price']): ?>
                        <br><small style="color: #22c55e;">Offer: ₹<?= number_format($prod['discounted_price']) ?></small>
                        <?php endif; ?>
                    </td>
                    <td><?= $prod['sell_online'] ? '✅ Yes' : '❌ No' ?></td>
                    <td style="white-space: nowrap;">
                        <form method="POST" style="display:inline;">
                            <input type="hidden" name="action" value="delete_shop_product">
                            <input type="hidden" name="product_id" value="<?= $prod['product_id'] ?>">
                            <input type="hidden" name="listing_id" value="<?= $editListing['listing_id'] ?>">
                            <button type="submit" onclick="return confirm('Delete this product?')" class="btn-sm contrast outline">🗑</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            <?php endif; ?>
            
            <!-- Add Product Form -->
            <form method="POST" enctype="multipart/form-data" class="form-grid" style="background: #f0fdf4; padding: 15px; border-radius: 8px;">
                <input type="hidden" name="action" value="add_shop_product">
                <input type="hidden" name="listing_id" value="<?= $editListing['listing_id'] ?>">
                
                <label>Product Name * <input type="text" name="product_name" required placeholder="e.g. Mobile Phone"></label>
                <label>Category *
                    <select name="category_id" required>
                        <option value="">Select Category</option>
                        <?php foreach ($categories as $cat): ?>
                        <?php if ($cat['listing_type'] === 'selling' && !$cat['parent_id']): ?>
                        <option value="<?= $cat['category_id'] ?>"><?= htmlspecialchars($cat['name']) ?></option>
                        <?php endif; ?>
                        <?php endforeach; ?>
                    </select>
                </label>
                <label>Subcategory
                    <select name="subcategory_id">
                        <option value="">Select Subcategory</option>
                        <?php foreach ($subcategories as $sub): ?>
                        <option value="<?= $sub['category_id'] ?>" data-parent-id="<?= $sub['parent_id'] ?>"><?= htmlspecialchars($sub['name']) ?></option>
                        <?php endforeach; ?>
                    </select>
                </label>
                <label>Price (₹) * <input type="number" name="price" required placeholder="999"></label>
                <label>Offer Price <input type="number" name="discounted_price" placeholder="799"></label>
                <label>Stock Qty <input type="number" name="stock_qty" placeholder="Optional"></label>
                <label>Product Image <input type="file" name="product_image" accept="image/*"></label>
                <label>Or Image URL <input type="url" name="image_url" placeholder="https://..."></label>
                <label>Description <textarea name="description" placeholder="Product description..." rows="2"></textarea></label>
                <label style="display: flex; align-items: center; gap: 8px;">
                    <input type="checkbox" name="sell_online" value="1" style="width: auto;">
                    <span>Sell Online (Show in Selling feed)</span>
                </label>
                <button type="submit" style="grid-column: span 2;">➕ Add Product</button>
            </form>
        </div>
        <?php endif; ?>
        <?php endif; ?>

        <?php elseif ($tab === 'moderation'): ?>
        <!-- ========== MODERATION ========== -->
        <h2>🛡️ Moderation Queue</h2>
        <p style="color: #64748b;">Review and approve/reject pending listings and products before they go live.</p>
        
        <!-- Auto-Moderation Settings Card -->
        <div class="card" style="margin-bottom: 20px; background: linear-gradient(135deg, #f8fafc, #e2e8f0);">
            <div class="card-header" style="background: transparent; border: none;">⚙️ Auto-Moderation Settings</div>
            <form method="POST" style="padding: 0 20px 20px;">
                <input type="hidden" name="action" value="save_auto_moderation">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
                    <!-- Products Toggle -->
                    <div style="background: white; padding: 20px; border-radius: 12px; border: 1px solid #e2e8f0;">
                        <label style="display: flex; align-items: center; gap: 12px; cursor: pointer; margin: 0;">
                            <input type="checkbox" name="auto_moderation_products" value="1" <?= $autoModProducts ? 'checked' : '' ?> 
                                   style="width: 20px; height: 20px; accent-color: #16a34a;">
                            <div>
                                <strong style="display: block; margin-bottom: 4px;">🛍️ Products Auto-Approve</strong>
                                <span style="font-size: 0.85em; color: #64748b;">
                                    <?php if ($autoModProducts): ?>
                                        <span style="color: #16a34a;">🟢 ON - Products go live immediately</span>
                                    <?php else: ?>
                                        <span style="color: #dc2626;">🔴 OFF - Manual approval required</span>
                                    <?php endif; ?>
                                </span>
                            </div>
                        </label>
                    </div>
                    <!-- Listings Toggle -->
                    <div style="background: white; padding: 20px; border-radius: 12px; border: 1px solid #e2e8f0;">
                        <label style="display: flex; align-items: center; gap: 12px; cursor: pointer; margin: 0;">
                            <input type="checkbox" name="auto_moderation_listings" value="1" <?= $autoModListings ? 'checked' : '' ?> 
                                   style="width: 20px; height: 20px; accent-color: #16a34a;">
                            <div>
                                <strong style="display: block; margin-bottom: 4px;">📋 Listings Auto-Approve</strong>
                                <span style="font-size: 0.85em; color: #64748b;">
                                    <?php if ($autoModListings): ?>
                                        <span style="color: #16a34a;">🟢 ON - Services/Jobs/Business go live immediately</span>
                                    <?php else: ?>
                                        <span style="color: #dc2626;">🔴 OFF - Manual approval required</span>
                                    <?php endif; ?>
                                </span>
                            </div>
                        </label>
                    </div>
                </div>
                <button type="submit" style="margin-top: 15px; background: #3b82f6;">💾 Save Settings</button>
            </form>
        </div>
        
        <?php 
        $totalPending = count($pendingListings) + count($pendingProducts ?? []);
        ?>
        
        <?php if ($totalPending === 0): ?>
        <div class="card" style="text-align: center; padding: 60px;">
            <h3>✅ All Clear!</h3>
            <p style="color: #64748b;">No pending listings or products to review.</p>
        </div>
        <?php else: ?>
        
        <!-- Pending Counts Summary -->
        <div class="stats-grid" style="margin-bottom: 20px;">
            <div class="stat-card"><h3><?= count($pendingListings) ?></h3><p>📋 Pending Listings</p></div>
            <div class="stat-card"><h3><?= count($pendingProducts ?? []) ?></h3><p>🛍️ Pending Products</p></div>
        </div>
        
        <!-- Pending Listings Section -->
        <?php if (!empty($pendingListings)): ?>
        <div class="card">
            <div class="card-header">📋 Pending Listings (<?= count($pendingListings) ?>)</div>
            <?php foreach ($pendingListings as $pl): ?>
            <div style="display: flex; gap: 20px; align-items: start; padding: 15px 0; border-bottom: 1px solid #e2e8f0;">
                <?php if ($pl['main_image_url']): ?>
                <img src="<?= $pl['main_image_url'] ?>" style="width: 120px; height: 80px; object-fit: cover; border-radius: 8px;">
                <?php else: ?>
                <div style="width: 120px; height: 80px; background: #f1f5f9; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #94a3b8;">No image</div>
                <?php endif; ?>
                <div style="flex: 1;">
                    <h4 style="margin: 0 0 5px;"><?= htmlspecialchars($pl['title']) ?></h4>
                    <p style="margin: 0; color: #64748b; font-size: 0.9em;">
                        <span class="badge badge-info"><?= $pl['listing_type'] ?></span>
                        • <?= htmlspecialchars($pl['category_name'] ?? 'No category') ?>
                        • by <?= htmlspecialchars($pl['username'] ?? 'Unknown') ?>
                        • <?= date('M d, Y', strtotime($pl['created_at'])) ?>
                    </p>
                    <p style="margin: 10px 0 0; font-size: 0.85em; color: #475569;"><?= htmlspecialchars(substr($pl['description'] ?? '', 0, 150)) ?>...</p>
                </div>
                <div style="display: flex; gap: 8px; flex-shrink: 0;">
                    <form method="POST">
                        <input type="hidden" name="action" value="approve_listing">
                        <input type="hidden" name="listing_id" value="<?= $pl['listing_id'] ?>">
                        <button type="submit" class="btn-sm" style="background: #16a34a;">✓ Approve</button>
                    </form>
                    <form method="POST" onsubmit="return confirm('Reject this listing?')">
                        <input type="hidden" name="action" value="reject_listing">
                        <input type="hidden" name="listing_id" value="<?= $pl['listing_id'] ?>">
                        <button type="submit" class="btn-sm contrast">✕ Reject</button>
                    </form>
                    <a href="?tab=edit_listing&edit=<?= $pl['listing_id'] ?>" class="btn-sm outline">✏️</a>
                </div>
            </div>
            <?php endforeach; ?>
        </div>
        <?php endif; ?>
        
        <!-- Pending Products Section -->
        <?php if (!empty($pendingProducts)): ?>
        <div class="card">
            <div class="card-header">🛍️ Pending Products (<?= count($pendingProducts) ?>)</div>
            <?php foreach ($pendingProducts as $pp): ?>
            <div style="display: flex; gap: 20px; align-items: start; padding: 15px 0; border-bottom: 1px solid #e2e8f0;">
                <?php if ($pp['image_url']): ?>
                <img src="<?= $pp['image_url'] ?>" style="width: 120px; height: 80px; object-fit: cover; border-radius: 8px;">
                <?php else: ?>
                <div style="width: 120px; height: 80px; background: #f1f5f9; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #94a3b8;">No image</div>
                <?php endif; ?>
                <div style="flex: 1;">
                    <h4 style="margin: 0 0 5px;"><?= htmlspecialchars($pp['product_name']) ?></h4>
                    <p style="margin: 0; color: #64748b; font-size: 0.9em;">
                        <span class="badge badge-success">Product</span>
                        • ₹<?= number_format($pp['price'] ?? 0) ?>
                        • <?= htmlspecialchars($pp['category_name'] ?? 'No category') ?>
                        • Shop: <?= htmlspecialchars($pp['shop_name'] ?? 'Unknown') ?>
                        • by <?= htmlspecialchars($pp['username'] ?? 'Unknown') ?>
                    </p>
                    <p style="margin: 10px 0 0; font-size: 0.85em; color: #475569;"><?= htmlspecialchars(substr($pp['description'] ?? '', 0, 150)) ?>...</p>
                </div>
                <div style="display: flex; gap: 8px; flex-shrink: 0;">
                    <form method="POST">
                        <input type="hidden" name="action" value="approve_product">
                        <input type="hidden" name="product_id" value="<?= $pp['product_id'] ?>">
                        <button type="submit" class="btn-sm" style="background: #16a34a;">✓ Approve</button>
                    </form>
                    <form method="POST" onsubmit="return confirm('Reject and DELETE this product?')">
                        <input type="hidden" name="action" value="reject_product">
                        <input type="hidden" name="product_id" value="<?= $pp['product_id'] ?>">
                        <button type="submit" class="btn-sm contrast">✕ Reject</button>
                    </form>
                </div>
            </div>
            <?php endforeach; ?>
        </div>
        <?php endif; ?>
        
        <?php endif; ?>

        <?php elseif ($tab === 'settings'): ?>
        <!-- ========== SETTINGS ========== -->
        <h2>⚙️ Site Settings</h2>
        
        <div class="card">
            <form method="POST">
                <input type="hidden" name="action" value="save_settings">
                
                <div class="card-header">General Settings</div>
                <div class="form-grid">
                    <label>Site Name <input type="text" name="site_name" value="<?= htmlspecialchars($settings['site_name'] ?? 'Hello Hingoli') ?>"></label>
                    <label>Contact Email <input type="email" name="contact_email" value="<?= htmlspecialchars($settings['contact_email'] ?? '') ?>" placeholder="contact@example.com"></label>
                    <label>Contact Phone <input type="tel" name="contact_phone" value="<?= htmlspecialchars($settings['contact_phone'] ?? '') ?>" placeholder="+91..."></label>
                    <label>Default City
                        <select name="default_city">
                            <?php foreach ($cities as $c): ?>
                            <option value="<?= $c['name'] ?>" <?= ($settings['default_city'] ?? 'Hingoli') === $c['name'] ? 'selected' : '' ?>><?= htmlspecialchars($c['name']) ?></option>
                            <?php endforeach; ?>
                        </select>
                    </label>
                </div>
                
                <div class="card-header" style="margin-top: 30px;">Social Media Links</div>
                <div class="form-grid">
                    <label>Facebook URL <input type="url" name="facebook_url" value="<?= htmlspecialchars($settings['facebook_url'] ?? '') ?>" placeholder="https://facebook.com/..."></label>
                    <label>Instagram URL <input type="url" name="instagram_url" value="<?= htmlspecialchars($settings['instagram_url'] ?? '') ?>" placeholder="https://instagram.com/..."></label>
                    <label>Twitter URL <input type="url" name="twitter_url" value="<?= htmlspecialchars($settings['twitter_url'] ?? '') ?>" placeholder="https://twitter.com/..."></label>
                </div>
                
                <button type="submit" style="margin-top: 30px;">💾 Save Settings</button>
            </form>
        </div>
        
        <div class="card">
            <div class="card-header">Database Info</div>
            <table>
                <tr><td>Total Listings</td><td><strong><?= $stats['total_listings'] ?></strong></td></tr>
                <tr><td>Total Users</td><td><strong><?= $stats['total_users'] ?></strong></td></tr>
                <tr><td>Total Categories</td><td><strong><?= $stats['total_categories'] ?></strong></td></tr>
                <tr><td>Pending Moderation</td><td><strong><?= $stats['pending_listings'] ?></strong></td></tr>
        </div>

        <?php elseif ($tab === 'products'): ?>
        <!-- ========== PRODUCTS ========== -->
        <h2>🛍️ Products <a href="?tab=add_product" class="btn-sm" style="margin-left: 15px;">➕ Add Product</a></h2>
        <p style="color: #64748b;">Manage all products listed in the marketplace.</p>
        
        <div class="card">
            <table>
                <thead>
                    <tr>
                        <th>Image</th>
                        <th>Product Name</th>
                        <th>Price</th>
                        <th>Category</th>
                        <th>Shop</th>
                        <th>Stock</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                <?php foreach ($allProducts as $prod): ?>
                <tr>
                    <td>
                        <?php if ($prod['image_url']): ?>
                        <img src="<?= $prod['image_url'] ?>" style="width: 50px; height: 50px; object-fit: cover; border-radius: 6px;">
                        <?php else: ?>
                        <div style="width: 50px; height: 50px; background: #f1f5f9; border-radius: 6px;"></div>
                        <?php endif; ?>
                    </td>
                    <td><strong><?= htmlspecialchars($prod['product_name']) ?></strong></td>
                    <td>₹<?= number_format($prod['price'] ?? 0) ?><?= $prod['discounted_price'] ? ' <s style="color:#94a3b8;">₹' . number_format($prod['discounted_price']) . '</s>' : '' ?></td>
                    <td><?= htmlspecialchars($prod['category_name'] ?? '-') ?></td>
                    <td><?= htmlspecialchars($prod['shop_name'] ?? '-') ?></td>
                    <td><?= $prod['stock_qty'] ?? 1 ?></td>
                    <td>
                        <?php if ($prod['is_active']): ?>
                        <span class="badge badge-success">Active</span>
                        <?php else: ?>
                        <span class="badge badge-warning">Pending</span>
                        <?php endif; ?>
                    </td>
                    <td>
                        <a href="?tab=add_product&edit=<?= $prod['product_id'] ?>" class="btn-sm outline">✏️ Edit</a>
                        <form method="POST" style="display:inline;" onsubmit="return confirm('Delete this product?')">
                            <input type="hidden" name="action" value="delete_product">
                            <input type="hidden" name="product_id" value="<?= $prod['product_id'] ?>">
                            <button type="submit" class="btn-sm contrast outline">🗑️</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                <?php if (empty($allProducts)): ?>
                <tr><td colspan="8" style="text-align: center; padding: 30px; color: #64748b;">No products found</td></tr>
                <?php endif; ?>
                </tbody>
            </table>
        </div>

        <?php elseif ($tab === 'add_product'): 
        // Check if editing
        $editProduct = null;
        if (isset($_GET['edit'])) {
            $stmt = $db->prepare("SELECT * FROM shop_products WHERE product_id = ?");
            $stmt->execute([$_GET['edit']]);
            $editProduct = $stmt->fetch();
        }
        ?>
        <!-- ========== ADD/EDIT PRODUCT ========== -->
        <h2><a href="?tab=products">← Products</a> &nbsp;<?= $editProduct ? 'Edit Product' : 'Add New Product' ?></h2>
        
        <div class="card">
            <form method="POST" enctype="multipart/form-data">
                <input type="hidden" name="action" value="<?= $editProduct ? 'edit_product' : 'add_product' ?>">
                <?php if ($editProduct): ?>
                <input type="hidden" name="product_id" value="<?= $editProduct['product_id'] ?>">
                <input type="hidden" name="existing_image" value="<?= $editProduct['image_url'] ?>">
                <?php endif; ?>
                
                <div class="form-grid">
                    <label>Shop/Business * 
                        <select name="listing_id" required <?= $editProduct ? 'disabled' : '' ?>>
                            <option value="">Select Shop...</option>
                            <?php foreach ($businessListings ?? [] as $biz): ?>
                            <option value="<?= $biz['listing_id'] ?>" <?= ($editProduct && $editProduct['listing_id'] == $biz['listing_id']) ? 'selected' : '' ?>>
                                <?= htmlspecialchars($biz['business_name']) ?> (by <?= htmlspecialchars($biz['username'] ?? 'Unknown') ?>)
                            </option>
                            <?php endforeach; ?>
                        </select>
                        <?php if ($editProduct): ?>
                        <input type="hidden" name="listing_id" value="<?= $editProduct['listing_id'] ?>">
                        <?php endif; ?>
                    </label>
                    <label>Product Name * 
                        <input type="text" name="product_name" required value="<?= htmlspecialchars($editProduct['product_name'] ?? '') ?>" placeholder="Product title">
                    </label>
                </div>
                
                <label style="margin-top: 15px;">Description
                    <textarea name="description" rows="3" placeholder="Product description..."><?= htmlspecialchars($editProduct['description'] ?? '') ?></textarea>
                </label>
                
                <div class="form-grid" style="margin-top: 15px;">
                    <label>Category *
                        <select name="category_id" required>
                            <option value="">Select Category...</option>
                            <?php foreach ($categories ?? [] as $cat): ?>
                            <option value="<?= $cat['category_id'] ?>" <?= ($editProduct && $editProduct['category_id'] == $cat['category_id']) ? 'selected' : '' ?>>
                                <?= htmlspecialchars($cat['name']) ?>
                            </option>
                            <?php endforeach; ?>
                        </select>
                    </label>
                    <label>Subcategory
                        <select name="subcategory_id">
                            <option value="">Select Subcategory...</option>
                            <?php foreach ($subcategories ?? [] as $sub): ?>
                            <option value="<?= $sub['category_id'] ?>" <?= ($editProduct && $editProduct['subcategory_id'] == $sub['category_id']) ? 'selected' : '' ?>>
                                <?= htmlspecialchars($sub['name']) ?>
                            </option>
                            <?php endforeach; ?>
                        </select>
                    </label>
                </div>
                
                <div class="form-grid" style="margin-top: 15px;">
                    <label>Selling Price (₹) * <input type="number" name="price" required value="<?= $editProduct['price'] ?? '' ?>" placeholder="e.g. 499"></label>
                    <label>MRP / Original Price (₹) <input type="number" name="discounted_price" value="<?= $editProduct['discounted_price'] ?? '' ?>" placeholder="e.g. 599 (strikethrough)"></label>
                    <label>Stock Quantity <input type="number" name="stock_qty" value="<?= $editProduct['stock_qty'] ?? 1 ?>" min="0"></label>
                    <label>Min Order Qty <input type="number" name="min_qty" value="<?= $editProduct['min_qty'] ?? 1 ?>" min="1" title="Minimum quantity customer must order"></label>
                </div>
                <div class="form-grid" style="margin-top: 15px;">
                    <label>Condition
                        <select name="condition">
                            <option value="new" <?= ($editProduct && $editProduct['condition'] == 'new') ? 'selected' : '' ?>>🆕 New</option>
                            <option value="old" <?= ($editProduct && $editProduct['condition'] == 'old') ? 'selected' : '' ?>>📦 Used / Old</option>
                        </select>
                    </label>
                </div>
                
                <div style="margin: 20px 0; padding: 15px; background: #f8fafc; border-radius: 10px;">
                    <strong>📷 Product Image</strong>
                    <?php if ($editProduct && $editProduct['image_url']): ?>
                    <img src="<?= $editProduct['image_url'] ?>" style="display: block; margin: 10px 0; width: 100px; height: 100px; object-fit: cover; border-radius: 8px;">
                    <?php endif; ?>
                    <div class="form-grid" style="margin-top: 10px;">
                        <label>Upload Image <input type="file" name="product_image" accept="image/*"></label>
                        <label>Or Image URL <input type="url" name="image_url" placeholder="https://..."></label>
                    </div>
                </div>
                
                <div class="form-grid">
                    <label style="display: flex; align-items: center; gap: 10px;">
                        <input type="checkbox" name="sell_online" <?= (!$editProduct || $editProduct['sell_online']) ? 'checked' : '' ?> style="width: auto;"> Available for Online Sale
                    </label>
                    <label style="display: flex; align-items: center; gap: 10px;">
                        <input type="checkbox" name="is_active" <?= (!$editProduct || $editProduct['is_active']) ? 'checked' : '' ?> style="width: auto;"> Active (Visible to users)
                    </label>
                </div>
                
                <button type="submit" style="margin-top: 20px;"><?= $editProduct ? '💾 Update Product' : '➕ Add Product' ?></button>
            </form>
        </div>

        <?php elseif ($tab === 'categories'): ?>
        <!-- ========== CATEGORIES ========== -->
        <h2>📁 Categories</h2>
        <div class="card">
            <div class="card-header">Add New Category</div>
            <form method="POST">
                <input type="hidden" name="action" value="add_category">
                <div class="form-grid">
                    <label>Name <input type="text" name="name" required placeholder="e.g. Home Services"></label>
                    <label>Listing Type
                        <select name="listing_type" required>
                            <option value="services">Services</option>
                            <option value="selling">Selling</option>
                            <option value="business">Business</option>
                            <option value="jobs">Jobs</option>
                        </select>
                    </label>
                    <label>Image URL <input type="url" name="image_url" placeholder="https://..."></label>
                </div>
                <button type="submit">Add Category</button>
            </form>
        </div>

        <div class="card">
            <table>
                <thead><tr><th>ID</th><th>Image</th><th>Name</th><th>Type</th><th>Actions</th></tr></thead>
                <tbody>
                <?php foreach ($categories as $cat): ?>
                <tr>
                    <form method="POST">
                    <input type="hidden" name="action" value="edit_category">
                    <input type="hidden" name="category_id" value="<?= $cat['category_id'] ?>">
                    <td><?= $cat['category_id'] ?></td>
                    <td><?php if($cat['image_url']): ?><img src="<?= $cat['image_url'] ?>"><?php endif; ?></td>
                    <td><input type="text" name="name" value="<?= htmlspecialchars($cat['name']) ?>" required style="margin:0;padding:5px;"></td>
                    <td><span class="badge badge-info"><?= $cat['listing_type'] ?></span>
                        <input type="url" name="image_url" value="<?= $cat['image_url'] ?>" placeholder="Image URL" style="margin:0;padding:5px;font-size:0.8em;">
                    </td>
                    <td style="white-space:nowrap;">
                        <button type="submit" class="btn-sm outline">💾</button>
                    </form>
                        <form method="POST" style="display:inline;" onsubmit="return confirm('Delete?')">
                            <input type="hidden" name="action" value="delete_category">
                            <input type="hidden" name="category_id" value="<?= $cat['category_id'] ?>">
                            <button type="submit" class="btn-sm contrast outline">🗑</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
        </div>

        <?php elseif ($tab === 'subcategories'): ?>
        <!-- ========== SUBCATEGORIES ========== -->
        <h2>📂 Subcategories</h2>
        <div class="card">
            <div class="card-header">Add New Subcategory</div>
            <form method="POST">
                <input type="hidden" name="action" value="add_subcategory">
                <div class="form-grid">
                    <label>Parent Category
                        <select name="parent_id" required>
                            <option value="">Select Parent...</option>
                            <?php foreach ($categories as $cat): ?>
                            <option value="<?= $cat['category_id'] ?>"><?= htmlspecialchars($cat['name']) ?> (<?= $cat['listing_type'] ?>)</option>
                            <?php endforeach; ?>
                        </select>
                    </label>
                    <label>Name <input type="text" name="name" required placeholder="e.g. Electrician"></label>
                    <label>Image URL <input type="url" name="image_url" placeholder="https://..."></label>
                </div>
                <button type="submit">Add Subcategory</button>
            </form>
        </div>

        <div class="card">
            <table>
                <thead><tr><th>ID</th><th>Parent</th><th>Name</th><th>Image URL</th><th>Actions</th></tr></thead>
                <tbody>
                <?php foreach ($subcategories as $sub): ?>
                <tr>
                    <form method="POST">
                    <input type="hidden" name="action" value="edit_category">
                    <input type="hidden" name="category_id" value="<?= $sub['category_id'] ?>">
                    <td><?= $sub['category_id'] ?></td>
                    <td><?= htmlspecialchars($sub['parent_name']) ?></td>
                    <td><input type="text" name="name" value="<?= htmlspecialchars($sub['name']) ?>" required style="margin:0;padding:5px;"></td>
                    <td><input type="url" name="image_url" value="<?= $sub['image_url'] ?? '' ?>" placeholder="Image URL" style="margin:0;padding:5px;"></td>
                    <td style="white-space:nowrap;">
                        <button type="submit" class="btn-sm outline">💾</button>
                    </form>
                        <form method="POST" style="display:inline;" onsubmit="return confirm('Delete?')">
                            <input type="hidden" name="action" value="delete_category">
                            <input type="hidden" name="category_id" value="<?= $sub['category_id'] ?>">
                            <button type="submit" class="btn-sm contrast outline">🗑</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
        </div>

        <?php elseif ($tab === 'users'): ?>
        <!-- ========== USERS ========== -->
        <h2>👥 Users</h2>
        <div class="card">
            <div class="card-header">Add New User</div>
            <form method="POST">
                <input type="hidden" name="action" value="add_user">
                <div class="form-grid">
                    <label>Username <input type="text" name="username" required placeholder="Enter username"></label>
                    <label>Phone <input type="text" name="phone" required placeholder="9876543210"></label>
                    <label>Email <input type="email" name="email" placeholder="user@example.com"></label>
                    <label>Password <input type="password" name="password" placeholder="Enter password"></label>
                    <label style="display: flex; align-items: center; gap: 10px; padding-top: 25px;">
                        <input type="checkbox" name="is_verified" style="width: auto;"> Verified
                    </label>
                </div>
                <button type="submit">Add User</button>
            </form>
        </div>

        <div class="card">
            <table>
                <thead><tr><th>ID</th><th>Username</th><th>Phone</th><th>Email</th><th>Listings</th><th>Verified</th><th>Actions</th></tr></thead>
                <tbody>
                <?php foreach ($users as $user): ?>
                <tr>
                    <td><?= $user['user_id'] ?></td>
                    <td><?= htmlspecialchars($user['username']) ?></td>
                    <td><?= $user['phone'] ?></td>
                    <td><?= $user['email'] ?? '-' ?></td>
                    <td><?= $user['listing_count'] ?></td>
                    <td><?= $user['is_verified'] ? '<span class="badge badge-success">Yes</span>' : '<span class="badge badge-warning">No</span>' ?></td>
                    <td>
                        <a href="?tab=user_detail&id=<?= $user['user_id'] ?>" class="btn-sm outline">👁️ View</a>
                        <form method="POST" style="display:inline;" onsubmit="return confirm('Delete this user?')">
                            <input type="hidden" name="action" value="delete_user">
                            <input type="hidden" name="user_id" value="<?= $user['user_id'] ?>">
                            <button type="submit" class="btn-sm contrast outline">Delete</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
        </div>

        <?php elseif ($tab === 'user_detail'): 
        // Fetch user details
        $userId = $_GET['id'] ?? 0;
        $userDetail = null;
        $userAddresses = [];
        $userOrders = [];
        $userListings = [];
        
        if ($userId) {
            $stmt = $db->prepare("SELECT * FROM users WHERE user_id = ?");
            $stmt->execute([$userId]);
            $userDetail = $stmt->fetch();
            
            if ($userDetail) {
                // Get addresses
                $stmt = $db->prepare("SELECT * FROM user_addresses WHERE user_id = ? ORDER BY is_default DESC, created_at DESC");
                $stmt->execute([$userId]);
                $userAddresses = $stmt->fetchAll();
                
                // Get recent orders
                try {
                    $stmt = $db->prepare("SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC LIMIT 10");
                    $stmt->execute([$userId]);
                    $userOrders = $stmt->fetchAll();
                } catch (Exception $e) {}
                
                // Get user listings
                $stmt = $db->prepare("SELECT * FROM listings WHERE user_id = ? ORDER BY created_at DESC LIMIT 10");
                $stmt->execute([$userId]);
                $userListings = $stmt->fetchAll();
            }
        }
        ?>
        <!-- ========== USER DETAIL ========== -->
        <h2><a href="?tab=users">← Users</a> &nbsp;User Profile</h2>
        
        <?php if (!$userDetail): ?>
        <div class="card"><p>User not found.</p></div>
        <?php else: ?>
        
        <!-- User Profile Edit Form -->
        <div class="card">
            <div class="card-header">👤 Edit Profile - <?= htmlspecialchars($userDetail['username']) ?></div>
            <form method="POST" enctype="multipart/form-data">
                <input type="hidden" name="action" value="edit_user">
                <input type="hidden" name="user_id" value="<?= $userDetail['user_id'] ?>">
                <input type="hidden" name="existing_avatar" value="<?= $userDetail['avatar_url'] ?? '' ?>">
                
                <div style="display: flex; gap: 20px; align-items: flex-start; margin-bottom: 20px;">
                    <?php if ($userDetail['avatar_url']): ?>
                    <img src="<?= $userDetail['avatar_url'] ?>" alt="Avatar" style="width: 80px; height: 80px; border-radius: 50%; object-fit: cover;">
                    <?php else: ?>
                    <div style="width: 80px; height: 80px; border-radius: 50%; background: #e2e8f0; display: flex; align-items: center; justify-content: center; font-size: 32px;">👤</div>
                    <?php endif; ?>
                    <div>
                        <p><strong>User ID:</strong> <?= $userDetail['user_id'] ?></p>
                        <p><strong>Joined:</strong> <?= date('M d, Y', strtotime($userDetail['created_at'])) ?></p>
                        <p><strong>Listings:</strong> <?= $userDetail['listing_count'] ?? 0 ?> | <strong>Rating:</strong> <?= $userDetail['avg_rating'] ? number_format($userDetail['avg_rating'], 1) . '⭐' : 'N/A' ?></p>
                    </div>
                </div>
                
                <div class="form-grid">
                    <label>Username * <input type="text" name="username" required value="<?= htmlspecialchars($userDetail['username']) ?>"></label>
                    <label>Phone * <input type="text" name="phone" required value="<?= $userDetail['phone'] ?>"></label>
                    <label>Email <input type="email" name="email" value="<?= $userDetail['email'] ?? '' ?>"></label>
                    <label>Avatar <input type="file" name="avatar" accept="image/*"></label>
                    <label>New Password <input type="password" name="new_password" placeholder="Leave blank to keep current"></label>
                    <label style="display: flex; align-items: center; gap: 10px; padding-top: 25px;">
                        <input type="checkbox" name="is_verified" <?= $userDetail['is_verified'] ? 'checked' : '' ?> style="width: auto;"> Verified User
                    </label>
                </div>
                <button type="submit" style="margin-top: 15px;">💾 Save Profile</button>
            </form>
        </div>
        
        <!-- User Addresses -->
        <div class="card">
            <div class="card-header">📍 Delivery Addresses (<?= count($userAddresses) ?>)</div>
            
            <?php if (empty($userAddresses)): ?>
            <p style="color: #64748b; padding: 10px 0;">No saved addresses.</p>
            <?php else: ?>
            <div style="display: grid; gap: 15px; margin-bottom: 20px;">
                <?php foreach ($userAddresses as $addr): ?>
                <div style="border: 1px solid #e2e8f0; border-radius: 10px; padding: 15px; <?= $addr['is_default'] ? 'border-color: #10b981; background: #f0fdf4;' : '' ?>">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                        <div>
                            <strong><?= htmlspecialchars($addr['name']) ?></strong> 
                            <?= $addr['is_default'] ? '<span class="badge badge-success">Default</span>' : '' ?>
                            <p style="color: #64748b; margin: 5px 0;">📞 <?= $addr['phone'] ?></p>
                            <p style="color: #475569;"><?= htmlspecialchars($addr['address_line1']) ?><?= $addr['address_line2'] ? ', ' . htmlspecialchars($addr['address_line2']) : '' ?></p>
                            <p style="color: #475569;"><?= htmlspecialchars($addr['city']) ?>, <?= $addr['state'] ?> - <?= $addr['pincode'] ?></p>
                        </div>
                        <div style="display: flex; gap: 8px;">
                            <button type="button" class="btn-sm outline" onclick="toggleEditAddress(<?= $addr['address_id'] ?>)">✏️</button>
                            <form method="POST" style="display:inline;" onsubmit="return confirm('Delete this address?')">
                                <input type="hidden" name="action" value="delete_user_address">
                                <input type="hidden" name="address_id" value="<?= $addr['address_id'] ?>">
                                <input type="hidden" name="user_id" value="<?= $userId ?>">
                                <button type="submit" class="btn-sm contrast outline">🗑️</button>
                            </form>
                        </div>
                    </div>
                    
                    <!-- Edit Address Form (hidden) -->
                    <form method="POST" id="edit-addr-<?= $addr['address_id'] ?>" style="display: none; margin-top: 15px; padding-top: 15px; border-top: 1px solid #e2e8f0;">
                        <input type="hidden" name="action" value="edit_user_address">
                        <input type="hidden" name="address_id" value="<?= $addr['address_id'] ?>">
                        <input type="hidden" name="user_id" value="<?= $userId ?>">
                        <div class="form-grid">
                            <label>Name * <input type="text" name="name" required value="<?= htmlspecialchars($addr['name']) ?>"></label>
                            <label>Phone * <input type="text" name="phone" required value="<?= $addr['phone'] ?>"></label>
                            <label>Address Line 1 * <input type="text" name="address_line1" required value="<?= htmlspecialchars($addr['address_line1']) ?>"></label>
                            <label>Address Line 2 <input type="text" name="address_line2" value="<?= htmlspecialchars($addr['address_line2'] ?? '') ?>"></label>
                            <label>City * <input type="text" name="city" required value="<?= htmlspecialchars($addr['city']) ?>"></label>
                            <label>State <input type="text" name="state" value="<?= $addr['state'] ?>" placeholder="Maharashtra"></label>
                            <label>Pincode * <input type="text" name="pincode" required value="<?= $addr['pincode'] ?>" maxlength="6"></label>
                            <label style="display: flex; align-items: center; gap: 10px; padding-top: 25px;">
                                <input type="checkbox" name="is_default" <?= $addr['is_default'] ? 'checked' : '' ?> style="width: auto;"> Default Address
                            </label>
                        </div>
                        <button type="submit" style="margin-top: 10px;">💾 Update Address</button>
                    </form>
                </div>
                <?php endforeach; ?>
            </div>
            <?php endif; ?>
            
            <!-- Add New Address Form -->
            <details style="margin-top: 15px;">
                <summary style="cursor: pointer; color: #6366f1; font-weight: 600;">➕ Add New Address</summary>
                <form method="POST" style="margin-top: 15px;">
                    <input type="hidden" name="action" value="add_user_address">
                    <input type="hidden" name="user_id" value="<?= $userId ?>">
                    <div class="form-grid">
                        <label>Name * <input type="text" name="name" required placeholder="Full name"></label>
                        <label>Phone * <input type="text" name="phone" required placeholder="10-digit phone"></label>
                        <label>Address Line 1 * <input type="text" name="address_line1" required placeholder="House no, Building, Street"></label>
                        <label>Address Line 2 <input type="text" name="address_line2" placeholder="Area, Landmark"></label>
                        <label>City * <input type="text" name="city" required value="Hingoli"></label>
                        <label>State <input type="text" name="state" value="Maharashtra"></label>
                        <label>Pincode * <input type="text" name="pincode" required placeholder="6-digit" maxlength="6"></label>
                        <label style="display: flex; align-items: center; gap: 10px; padding-top: 25px;">
                            <input type="checkbox" name="is_default" style="width: auto;"> Set as Default
                        </label>
                    </div>
                    <button type="submit" style="margin-top: 10px;">➕ Add Address</button>
                </form>
            </details>
        </div>
        
        <!-- User Orders -->
        <?php if (!empty($userOrders)): ?>
        <div class="card">
            <div class="card-header">🛒 Recent Orders (<?= count($userOrders) ?>)</div>
            <table>
                <thead><tr><th>Order #</th><th>Date</th><th>Amount</th><th>Status</th><th>Payment</th></tr></thead>
                <tbody>
                <?php foreach ($userOrders as $order): ?>
                <tr>
                    <td><a href="?tab=order_detail&id=<?= $order['order_id'] ?>"><?= $order['order_number'] ?></a></td>
                    <td><?= date('M d, Y', strtotime($order['created_at'])) ?></td>
                    <td>₹<?= number_format($order['total_amount'], 2) ?></td>
                    <td><span class="badge badge-<?= $order['order_status'] === 'delivered' ? 'success' : ($order['order_status'] === 'cancelled' ? 'danger' : 'info') ?>"><?= $order['order_status'] ?></span></td>
                    <td><span class="badge badge-<?= $order['payment_status'] === 'paid' ? 'success' : 'warning' ?>"><?= $order['payment_status'] ?></span></td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
        </div>
        <?php endif; ?>
        
        <!-- User Listings -->
        <?php if (!empty($userListings)): ?>
        <div class="card">
            <div class="card-header">📋 User's Listings (<?= count($userListings) ?>)</div>
            <table>
                <thead><tr><th>ID</th><th>Title</th><th>Type</th><th>Status</th><th>Views</th><th>Action</th></tr></thead>
                <tbody>
                <?php foreach ($userListings as $listing): ?>
                <tr>
                    <td><?= $listing['listing_id'] ?></td>
                    <td><?= htmlspecialchars(substr($listing['title'], 0, 35)) ?></td>
                    <td><span class="badge badge-info"><?= $listing['listing_type'] ?></span></td>
                    <td><span class="badge badge-<?= $listing['status'] === 'active' ? 'success' : 'warning' ?>"><?= $listing['status'] ?></span></td>
                    <td><?= $listing['view_count'] ?? 0 ?></td>
                    <td><a href="?tab=edit_listing&edit=<?= $listing['listing_id'] ?>" class="btn-sm outline">Edit</a></td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
        </div>
        <?php endif; ?>
        
        <script>
        function toggleEditAddress(id) {
            const form = document.getElementById('edit-addr-' + id);
            form.style.display = form.style.display === 'none' ? 'block' : 'none';
        }
        </script>
        
        <?php endif; ?>

        <?php elseif ($tab === 'banners'): ?>
        <!-- ========== BANNERS ========== -->
        <h2>🖼️ Banners</h2>
        <p style="color: #64748b;">Manage promotional banners across your app.</p>
        
        <div class="card">
            <div class="card-header">➕ Add New Banner</div>
            <form method="POST" enctype="multipart/form-data">
                <input type="hidden" name="action" value="add_banner">
                <div class="form-grid">
                    <label>Title * <input type="text" name="title" required placeholder="Banner title"></label>
                    <label>Placement *
                        <select name="placement" required>
                            <optgroup label="🏠 Home">
                                <option value="home_top">Home - Top</option>
                                <option value="home_bottom">Home - Bottom</option>
                            </optgroup>
                            <optgroup label="🔧 Services">
                                <option value="services_top">Services - Top</option>
                                <option value="services_bottom">Services - Bottom</option>
                            </optgroup>
                            <optgroup label="🛒 Selling">
                                <option value="selling_top">Selling - Top</option>
                                <option value="selling_bottom">Selling - Bottom</option>
                            </optgroup>
                            <optgroup label="🏢 Business">
                                <option value="business_top">Business - Top</option>
                                <option value="business_bottom">Business - Bottom</option>
                            </optgroup>
                            <optgroup label="💼 Jobs">
                                <option value="jobs_top">Jobs - Top</option>
                                <option value="jobs_bottom">Jobs - Bottom</option>
                            </optgroup>
                            <optgroup label="📄 Other Pages">
                                <option value="listing_detail_bottom">Listing Detail</option>
                                <option value="category_bottom">Category Page</option>
                                <option value="search_bottom">Search Results</option>
                            </optgroup>
                        </select>
                    </label>
                </div>
                
                <div style="margin: 20px 0; padding: 20px; background: #f8fafc; border-radius: 12px; border: 1px dashed #cbd5e1;">
                    <strong>📷 Banner Image</strong>
                    <div class="form-grid" style="margin-top: 15px;">
                        <label>
                            Upload Image
                            <input type="file" name="banner_image" accept="image/*">
                            <small style="color: #64748b;">JPG, PNG, WebP • Recommended: 1200x400</small>
                        </label>
                        <label>
                            Or Image URL
                            <input type="url" name="image_url" placeholder="https://...">
                        </label>
                    </div>
                </div>
                
                <div class="form-grid">
                    <label>Link URL <input type="url" name="link_url" placeholder="https://... (optional)"></label>
                    <label>Sort Order <input type="number" name="sort_order" value="0" min="0"></label>
                    <label style="display: flex; align-items: center; gap: 10px; padding-top: 25px;">
                        <input type="checkbox" name="is_active" checked style="width: auto;"> Active
                    </label>
                </div>
                <button type="submit" style="margin-top: 15px;">➕ Add Banner</button>
            </form>
        </div>

        <div class="card">
            <div class="card-header">📋 All Banners</div>
            <table>
                <thead><tr><th>ID</th><th>Preview</th><th>Title</th><th>Placement</th><th>Status</th><th>Views/Clicks</th><th>Actions</th></tr></thead>
                <tbody>
                <?php foreach ($banners as $banner): ?>
                <tr>
                    <td><?= $banner['banner_id'] ?></td>
                    <td><img src="<?= $banner['image_url'] ?>" style="width:100px;height:40px;object-fit:cover;border-radius:6px;"></td>
                    <td>
                        <strong><?= htmlspecialchars($banner['title']) ?></strong>
                        <?php if ($banner['link_url']): ?>
                        <br><small><a href="<?= $banner['link_url'] ?>" target="_blank" style="color: #6366f1;">🔗 Link</a></small>
                        <?php endif; ?>
                    </td>
                    <td>
                        <span class="badge badge-info"><?= str_replace('_', ' ', ucfirst($banner['placement'])) ?></span>
                    </td>
                    <td>
                        <span class="badge <?= $banner['is_active'] ? 'badge-success' : 'badge-danger' ?>">
                            <?= $banner['is_active'] ? 'Active' : 'Inactive' ?>
                        </span>
                    </td>
                    <td>
                        👁️ <?= $banner['view_count'] ?? 0 ?> / 👆 <?= $banner['click_count'] ?? 0 ?>
                    </td>
                    <td style="white-space:nowrap;">
                        <a href="?tab=edit_banner&id=<?= $banner['banner_id'] ?>" class="btn-sm outline">✏️</a>
                        <form method="POST" style="display:inline;">
                            <input type="hidden" name="action" value="toggle_banner">
                            <input type="hidden" name="banner_id" value="<?= $banner['banner_id'] ?>">
                            <button type="submit" class="btn-sm <?= $banner['is_active'] ? '' : 'contrast' ?> outline"><?= $banner['is_active'] ? '✓' : '✗' ?></button>
                        </form>
                        <form method="POST" style="display:inline;" onsubmit="return confirm('Delete this banner?')">
                            <input type="hidden" name="action" value="delete_banner">
                            <input type="hidden" name="banner_id" value="<?= $banner['banner_id'] ?>">
                            <button type="submit" class="btn-sm contrast outline">🗑</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                <?php if (empty($banners)): ?>
                <tr><td colspan="7" style="text-align: center; padding: 40px; color: #64748b;">No banners yet. Add your first banner above!</td></tr>
                <?php endif; ?>
                </tbody>
            </table>
        </div>

        <?php elseif ($tab === 'edit_banner'): 
            $bannerId = $_GET['id'] ?? 0;
            $editBanner = $db->prepare("SELECT * FROM banners WHERE banner_id = ?");
            $editBanner->execute([$bannerId]);
            $bannerToEdit = $editBanner->fetch();
        ?>
        <!-- ========== EDIT BANNER ========== -->
        <h2>✏️ Edit Banner</h2>
        <p><a href="?tab=banners" style="color: #6366f1;">← Back to Banners</a></p>
        
        <?php if ($bannerToEdit): ?>
        <div class="card">
            <div class="card-header">Edit Banner #<?= $bannerToEdit['banner_id'] ?></div>
            <form method="POST" enctype="multipart/form-data">
                <input type="hidden" name="action" value="edit_banner">
                <input type="hidden" name="banner_id" value="<?= $bannerToEdit['banner_id'] ?>">
                
                <div style="margin-bottom: 20px;">
                    <strong>Current Image:</strong><br>
                    <img src="<?= $bannerToEdit['image_url'] ?>" style="max-width: 400px; height: 150px; object-fit: cover; border-radius: 12px; margin-top: 10px;">
                </div>
                
                <div class="form-grid">
                    <label>Title * <input type="text" name="title" value="<?= htmlspecialchars($bannerToEdit['title']) ?>" required></label>
                    <label>Placement *
                        <select name="placement" required>
                            <optgroup label="🏠 Home">
                                <option value="home_top" <?= $bannerToEdit['placement'] === 'home_top' ? 'selected' : '' ?>>Home - Top</option>
                                <option value="home_bottom" <?= $bannerToEdit['placement'] === 'home_bottom' ? 'selected' : '' ?>>Home - Bottom</option>
                            </optgroup>
                            <optgroup label="🔧 Services">
                                <option value="services_top" <?= $bannerToEdit['placement'] === 'services_top' ? 'selected' : '' ?>>Services - Top</option>
                                <option value="services_bottom" <?= $bannerToEdit['placement'] === 'services_bottom' ? 'selected' : '' ?>>Services - Bottom</option>
                            </optgroup>
                            <optgroup label="🛒 Selling">
                                <option value="selling_top" <?= $bannerToEdit['placement'] === 'selling_top' ? 'selected' : '' ?>>Selling - Top</option>
                                <option value="selling_bottom" <?= $bannerToEdit['placement'] === 'selling_bottom' ? 'selected' : '' ?>>Selling - Bottom</option>
                            </optgroup>
                            <optgroup label="🏢 Business">
                                <option value="business_top" <?= $bannerToEdit['placement'] === 'business_top' ? 'selected' : '' ?>>Business - Top</option>
                                <option value="business_bottom" <?= $bannerToEdit['placement'] === 'business_bottom' ? 'selected' : '' ?>>Business - Bottom</option>
                            </optgroup>
                            <optgroup label="💼 Jobs">
                                <option value="jobs_top" <?= $bannerToEdit['placement'] === 'jobs_top' ? 'selected' : '' ?>>Jobs - Top</option>
                                <option value="jobs_bottom" <?= $bannerToEdit['placement'] === 'jobs_bottom' ? 'selected' : '' ?>>Jobs - Bottom</option>
                            </optgroup>
                            <optgroup label="📄 Other Pages">
                                <option value="listing_detail_bottom" <?= $bannerToEdit['placement'] === 'listing_detail_bottom' ? 'selected' : '' ?>>Listing Detail</option>
                                <option value="category_bottom" <?= $bannerToEdit['placement'] === 'category_bottom' ? 'selected' : '' ?>>Category Page</option>
                                <option value="search_bottom" <?= $bannerToEdit['placement'] === 'search_bottom' ? 'selected' : '' ?>>Search Results</option>
                            </optgroup>
                        </select>
                    </label>
                </div>
                
                <div class="form-grid">
                    <label>Image URL * <input type="url" name="image_url" value="<?= htmlspecialchars($bannerToEdit['image_url']) ?>" required></label>
                    <label>Link URL <input type="url" name="link_url" value="<?= htmlspecialchars($bannerToEdit['link_url'] ?? '') ?>" placeholder="https://... (optional)"></label>
                </div>
                
                <div class="form-grid">
                    <label>Sort Order <input type="number" name="sort_order" value="<?= $bannerToEdit['sort_order'] ?>" min="0"></label>
                </div>
                
                <button type="submit" style="margin-top: 15px;">💾 Save Changes</button>
            </form>
        </div>
        <?php else: ?>
        <div class="card">
            <p style="text-align: center; padding: 40px; color: #ef4444;">Banner not found!</p>
        </div>
        <?php endif; ?>

        <?php elseif ($tab === 'cities'): ?>
        <!-- ========== CITIES ========== -->
        <h2>🏙️ Cities</h2>
        <div class="card">
            <div class="card-header">Add New City</div>
            <form method="POST">
                <input type="hidden" name="action" value="add_city">
                <div class="form-grid">
                    <label>City Name <input type="text" name="name" required placeholder="e.g. Hingoli"></label>
                    <label style="display: flex; align-items: center; gap: 10px; padding-top: 25px;">
                        <input type="checkbox" name="is_popular" style="width: auto;"> Popular City
                    </label>
                </div>
                <button type="submit">Add City</button>
            </form>
        </div>

        <div class="card">
            <table>
                <thead><tr><th>ID</th><th>Name</th><th>Popular</th><th>Listings</th><th>Actions</th></tr></thead>
                <tbody>
                <?php foreach ($cities as $city): ?>
                <tr>
                    <form method="POST">
                    <input type="hidden" name="action" value="edit_city">
                    <input type="hidden" name="city_id" value="<?= $city['city_id'] ?>">
                    <td><?= $city['city_id'] ?></td>
                    <td><input type="text" name="name" value="<?= htmlspecialchars($city['name']) ?>" required style="margin:0;padding:5px;"></td>
                    <td><input type="checkbox" name="is_popular" <?= $city['is_popular'] ? 'checked' : '' ?> style="width:auto;"></td>
                    <td><?= $city['listing_count'] ?></td>
                    <td style="white-space:nowrap;">
                        <button type="submit" class="btn-sm outline">💾</button>
                    </form>
                        <form method="POST" style="display:inline;" onsubmit="return confirm('Delete?')">
                            <input type="hidden" name="action" value="delete_city">
                            <input type="hidden" name="city_id" value="<?= $city['city_id'] ?>">
                            <button type="submit" class="btn-sm contrast outline">🗑</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
        </div>
        
        <?php elseif ($tab === 'reviews'): ?>
        <!-- ========== REVIEWS ========== -->
        <h2>⭐ Reviews Management</h2>
        <p style="color: #64748b;">Manage user reviews for listings.</p>
        
        <div class="card">
            <table>
                <thead>
                    <tr><th>ID</th><th>Listing</th><th>Reviewer</th><th>Rating</th><th>Content</th><th>Status</th><th>Date</th><th>Actions</th></tr>
                </thead>
                <tbody>
                <?php foreach ($reviews as $r): ?>
                <tr>
                    <td><?= $r['review_id'] ?></td>
                    <td><a href="?tab=edit_listing&edit=<?= $r['listing_id'] ?>"><?= htmlspecialchars(substr($r['listing_title'] ?? 'Unknown', 0, 25)) ?></a></td>
                    <td><?= htmlspecialchars($r['reviewer_name'] ?? 'Anonymous') ?></td>
                    <td>
                        <?php for ($i = 0; $i < 5; $i++): ?>
                        <span style="color: <?= $i < $r['rating'] ? '#f59e0b' : '#e2e8f0' ?>;">★</span>
                        <?php endfor; ?>
                    </td>
                    <td style="max-width: 200px;"><?= htmlspecialchars(substr($r['content'] ?? '', 0, 50)) ?>...</td>
                    <td>
                        <span class="badge <?= $r['approval_status'] === 'approved' ? 'badge-success' : ($r['approval_status'] === 'rejected' ? 'badge-danger' : 'badge-warning') ?>">
                            <?= $r['approval_status'] ?>
                        </span>
                    </td>
                    <td><?= date('M d', strtotime($r['created_at'])) ?></td>
                    <td style="white-space: nowrap;">
                        <?php if ($r['approval_status'] !== 'approved'): ?>
                        <form method="POST" style="display:inline;">
                            <input type="hidden" name="action" value="approve_review">
                            <input type="hidden" name="review_id" value="<?= $r['review_id'] ?>">
                            <button type="submit" class="btn-sm" style="background: #16a34a;">✓</button>
                        </form>
                        <?php endif; ?>
                        <?php if ($r['approval_status'] !== 'rejected'): ?>
                        <form method="POST" style="display:inline;">
                            <input type="hidden" name="action" value="reject_review">
                            <input type="hidden" name="review_id" value="<?= $r['review_id'] ?>">
                            <button type="submit" class="btn-sm" style="background: #dc2626;">✕</button>
                        </form>
                        <?php endif; ?>
                        <form method="POST" style="display:inline;" onsubmit="return confirm('Delete this review?')">
                            <input type="hidden" name="action" value="delete_review">
                            <input type="hidden" name="review_id" value="<?= $r['review_id'] ?>">
                            <button type="submit" class="btn-sm contrast outline">🗑</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                <?php if (empty($reviews)): ?>
                <tr><td colspan="8" style="text-align: center; padding: 40px; color: #64748b;">No reviews yet</td></tr>
                <?php endif; ?>
                </tbody>
            </table>
        </div>
        
        <?php elseif ($tab === 'analytics'): ?>
        <!-- ========== ANALYTICS ========== -->
        <h2>📈 Analytics Dashboard</h2>
        <p style="color: #64748b;">Track your platform growth and performance.</p>
        
        <!-- Summary Stats -->
        <div class="stats-grid">
            <div class="stat-card">
                <h3><?= $stats['total_listings'] ?></h3>
                <p>Total Listings</p>
            </div>
            <div class="stat-card">
                <h3><?= $stats['total_users'] ?></h3>
                <p>Total Users</p>
            </div>
            <div class="stat-card">
                <h3><?= count($reviews) ?></h3>
                <p>Reviews</p>
            </div>
            <div class="stat-card">
                <h3><?= $stats['pending_listings'] ?></h3>
                <p>Pending</p>
            </div>
        </div>
        
        <!-- Charts Row -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px;">
            <div class="card">
                <div class="card-header">Listings Growth (Last 30 Days)</div>
                <canvas id="listingsChart" height="200"></canvas>
            </div>
            <div class="card">
                <div class="card-header">Users Growth (Last 30 Days)</div>
                <canvas id="usersChart" height="200"></canvas>
            </div>
        </div>
        
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
            <div class="card">
                <div class="card-header">Listings by Category</div>
                <canvas id="categoryChart" height="200"></canvas>
            </div>
            <div class="card">
                <div class="card-header">Listings by City</div>
                <canvas id="cityChart" height="200"></canvas>
            </div>
        </div>
        
        <!-- Listings by Type Table -->
        <div class="card" style="margin-top: 20px;">
            <div class="card-header">Listings by Type</div>
            <table>
                <thead><tr><th>Type</th><th>Count</th><th>Percentage</th></tr></thead>
                <tbody>
                    <tr><td>🔧 Services</td><td><?= $stats['services_count'] ?></td><td><?= $stats['total_listings'] > 0 ? round(($stats['services_count'] / $stats['total_listings']) * 100, 1) : 0 ?>%</td></tr>
                    <tr><td>🛒 Selling</td><td><?= $stats['selling_count'] ?></td><td><?= $stats['total_listings'] > 0 ? round(($stats['selling_count'] / $stats['total_listings']) * 100, 1) : 0 ?>%</td></tr>
                    <tr><td>🏢 Business</td><td><?= $stats['business_count'] ?></td><td><?= $stats['total_listings'] > 0 ? round(($stats['business_count'] / $stats['total_listings']) * 100, 1) : 0 ?>%</td></tr>
                    <tr><td>💼 Jobs</td><td><?= $stats['jobs_count'] ?></td><td><?= $stats['total_listings'] > 0 ? round(($stats['jobs_count'] / $stats['total_listings']) * 100, 1) : 0 ?>%</td></tr>
                </tbody>
            </table>
        </div>
        
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <script>
        // Listings Chart
        new Chart(document.getElementById('listingsChart'), {
            type: 'line',
            data: {
                labels: <?= json_encode(array_column($listingsPerDay, 'date')) ?>,
                datasets: [{
                    label: 'New Listings',
                    data: <?= json_encode(array_column($listingsPerDay, 'count')) ?>,
                    borderColor: '#6366f1',
                    backgroundColor: 'rgba(99, 102, 241, 0.1)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options: { responsive: true, plugins: { legend: { display: false } } }
        });
        
        // Users Chart
        new Chart(document.getElementById('usersChart'), {
            type: 'line',
            data: {
                labels: <?= json_encode(array_column($usersPerDay, 'date')) ?>,
                datasets: [{
                    label: 'New Users',
                    data: <?= json_encode(array_column($usersPerDay, 'count')) ?>,
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options: { responsive: true, plugins: { legend: { display: false } } }
        });
        
        // Category Chart
        new Chart(document.getElementById('categoryChart'), {
            type: 'bar',
            data: {
                labels: <?= json_encode(array_column($listingsByCategory, 'name')) ?>,
                datasets: [{
                    data: <?= json_encode(array_column($listingsByCategory, 'count')) ?>,
                    backgroundColor: ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899']
                }]
            },
            options: { responsive: true, plugins: { legend: { display: false } }, indexAxis: 'y' }
        });
        
        // City Chart
        new Chart(document.getElementById('cityChart'), {
            type: 'doughnut',
            data: {
                labels: <?= json_encode(array_column($listingsByCity, 'city')) ?>,
                datasets: [{
                    data: <?= json_encode(array_column($listingsByCity, 'count')) ?>,
                    backgroundColor: ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316', '#84cc16', '#06b6d4']
                }]
            },
            options: { responsive: true }
        });
        </script>
        
        <?php elseif ($tab === 'notifications'): ?>
        <!-- ========== NOTIFICATIONS ========== -->
        <h2>📲 Push Notifications</h2>
        <p style="color: #64748b;">Send push notifications to app users.</p>
        
        <div class="card">
            <div class="card-header">Send New Notification</div>
            <form method="POST">
                <input type="hidden" name="action" value="send_notification">
                <div class="form-grid">
                    <label>
                        Title *
                        <input type="text" name="title" required placeholder="Notification title" maxlength="100">
                    </label>
                    <label>
                        Target Audience
                        <select name="target_type">
                            <option value="all">All Users</option>
                            <option value="services">Services Owners</option>
                            <option value="selling">Selling Users</option>
                            <option value="business">Business Owners</option>
                            <option value="jobs">Job Posters</option>
                        </select>
                    </label>
                </div>
                <label>
                    Message *
                    <textarea name="body" rows="3" required placeholder="Enter your notification message..." maxlength="500"></textarea>
                </label>
                <button type="submit" style="margin-top: 10px;">📤 Send Notification</button>
            </form>
        </div>
        
        <div class="card">
            <div class="card-header">Sent Notifications History</div>
            <?php if (empty($notificationLogs)): ?>
                <p style="text-align: center; color: #64748b; padding: 40px;">No notifications sent yet. The table will be created when you run the migration.</p>
            <?php else: ?>
            <table>
                <thead><tr><th>ID</th><th>Title</th><th>Message</th><th>Target</th><th>Sent</th><th>Date</th></tr></thead>
                <tbody>
                <?php foreach ($notificationLogs as $log): ?>
                <tr>
                    <td><?= $log['log_id'] ?></td>
                    <td><strong><?= htmlspecialchars($log['title']) ?></strong></td>
                    <td style="max-width: 200px;"><?= htmlspecialchars(substr($log['body'], 0, 50)) ?>...</td>
                    <td><span class="badge badge-info"><?= $log['target_type'] ?></span></td>
                    <td><?= $log['sent_count'] ?> users</td>
                    <td><?= date('M d, H:i', strtotime($log['created_at'])) ?></td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            <?php endif; ?>
        </div>
        
        <?php elseif ($tab === 'export'): ?>
        <!-- ========== EXPORT ========== -->
        <h2>📥 Export Data</h2>
        <p style="color: #64748b;">Download data as CSV files for reporting and analysis.</p>
        
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px;">
            <div class="card">
                <div class="card-header">📋 Export Listings</div>
                <p style="color: #64748b; font-size: 0.9em;">Download all listings with details including title, price, category, location, status, and dates.</p>
                <form method="POST" style="margin-top: 15px;">
                    <input type="hidden" name="action" value="export_listings">
                    <div class="form-grid">
                        <label>Type <select name="export_type"><option value="">All Types</option><option value="services">Services</option><option value="selling">Selling</option><option value="business">Business</option><option value="jobs">Jobs</option></select></label>
                        <label>Status <select name="export_status"><option value="">All Status</option><option value="active">Active</option><option value="pending">Pending</option><option value="draft">Draft</option></select></label>
                    </div>
                    <button type="submit" style="width: 100%; margin-top: 10px;">⬇️ Download Listings CSV</button>
                </form>
            </div>
            
            <div class="card">
                <div class="card-header">👥 Export Users</div>
                <p style="color: #64748b; font-size: 0.9em;">Download all registered users with username, phone, email, listing count, and registration date.</p>
                <form method="POST" style="margin-top: 15px;">
                    <input type="hidden" name="action" value="export_users">
                    <button type="submit" style="width: 100%; margin-top: 54px;">⬇️ Download Users CSV</button>
                </form>
            </div>
            
            <div class="card">
                <div class="card-header">⭐ Export Reviews</div>
                <p style="color: #64748b; font-size: 0.9em;">Download all reviews with listing info, rating, content, and approval status.</p>
                <form method="POST" style="margin-top: 15px;">
                    <input type="hidden" name="action" value="export_reviews">
                    <button type="submit" style="width: 100%; margin-top: 54px;">⬇️ Download Reviews CSV</button>
                </form>
            </div>
        </div>
        
        <?php elseif ($tab === 'enquiries'): ?>
        <!-- ========== ENQUIRIES ========== -->
        <h2>📩 Enquiries Management</h2>
        <p style="color: #64748b;">View and manage contact requests from app users.</p>
        
        <div class="card">
            <?php if (empty($enquiries)): ?>
                <p style="text-align: center; color: #64748b; padding: 40px;">
                    No enquiries yet. Run the database migration to create the enquiries table.<br><br>
                    <code style="background: #f1f5f9; padding: 10px; border-radius: 6px; display: inline-block;">
                        Source: api/admin_dashboard_migration.sql
                    </code>
                </p>
            <?php else: ?>
            <table>
                <thead><tr><th>ID</th><th>Name</th><th>Phone</th><th>Listing</th><th>Type</th><th>Message</th><th>Status</th><th>Date</th><th>Actions</th></tr></thead>
                <tbody>
                <?php foreach ($enquiries as $e): ?>
                <tr>
                    <td><?= $e['enquiry_id'] ?></td>
                    <td><strong><?= htmlspecialchars($e['name']) ?></strong></td>
                    <td><a href="tel:<?= $e['phone'] ?>"><?= $e['phone'] ?></a></td>
                    <td><?= htmlspecialchars(substr($e['listing_title'] ?? '-', 0, 20)) ?></td>
                    <td><span class="badge badge-info"><?= $e['enquiry_type'] ?></span></td>
                    <td style="max-width: 150px;"><?= htmlspecialchars(substr($e['message'] ?? '', 0, 30)) ?>...</td>
                    <td>
                        <span class="badge <?= $e['status'] === 'new' ? 'badge-warning' : ($e['status'] === 'resolved' ? 'badge-success' : 'badge-info') ?>">
                            <?= $e['status'] ?>
                        </span>
                    </td>
                    <td><?= date('M d', strtotime($e['created_at'])) ?></td>
                    <td style="white-space: nowrap;">
                        <form method="POST" style="display:inline;">
                            <input type="hidden" name="action" value="update_enquiry">
                            <input type="hidden" name="enquiry_id" value="<?= $e['enquiry_id'] ?>">
                            <input type="hidden" name="status" value="contacted">
                            <button type="submit" class="btn-sm outline" title="Mark Contacted">📞</button>
                        </form>
                        <form method="POST" style="display:inline;">
                            <input type="hidden" name="action" value="update_enquiry">
                            <input type="hidden" name="enquiry_id" value="<?= $e['enquiry_id'] ?>">
                            <input type="hidden" name="status" value="resolved">
                            <button type="submit" class="btn-sm" style="background: #16a34a;" title="Mark Resolved">✓</button>
                        </form>
                        <form method="POST" style="display:inline;" onsubmit="return confirm('Delete?')">
                            <input type="hidden" name="action" value="delete_enquiry">
                            <input type="hidden" name="enquiry_id" value="<?= $e['enquiry_id'] ?>">
                            <button type="submit" class="btn-sm contrast outline">🗑</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            <?php endif; ?>
        </div>
        
        <?php elseif ($tab === 'sms_logs'): ?>
        <!-- ========== SMS LOGS ========== -->
        <h2>📱 SMS / OTP Logs</h2>
        <p style="color: #64748b;">View OTP verification attempts and SMS delivery logs.</p>
        
        <div class="card">
            <div class="card-header">Recent OTP Verifications</div>
            <?php if (empty($otpLogs)): ?>
                <p style="text-align: center; color: #64748b; padding: 40px;">No OTP logs found.</p>
            <?php else: ?>
            <table>
                <thead><tr><th>ID</th><th>Phone</th><th>Purpose</th><th>Attempts</th><th>Expires</th><th>Created</th><th>Status</th></tr></thead>
                <tbody>
                <?php foreach ($otpLogs as $log): ?>
                <tr>
                    <td><?= $log['id'] ?></td>
                    <td><?= substr($log['phone'], 0, 3) . '****' . substr($log['phone'], -3) ?></td>
                    <td>
                        <span class="badge <?= $log['purpose'] === 'signup' ? 'badge-success' : ($log['purpose'] === 'login' ? 'badge-info' : 'badge-warning') ?>">
                            <?= $log['purpose'] ?>
                        </span>
                    </td>
                    <td><?= $log['attempts'] ?></td>
                    <td><?= date('M d, H:i', strtotime($log['expires_at'])) ?></td>
                    <td><?= date('M d, H:i', strtotime($log['created_at'])) ?></td>
                    <td>
                        <?php 
                        $expired = strtotime($log['expires_at']) < time();
                        $used = $log['attempts'] >= 3;
                        ?>
                        <span class="badge <?= $expired ? 'badge-danger' : ($used ? 'badge-warning' : 'badge-success') ?>">
                            <?= $expired ? 'Expired' : ($used ? 'Max Attempts' : 'Active') ?>
                        </span>
                    </td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            <?php endif; ?>
        </div>
        
        <?php elseif ($tab === 'app_config'): 
            // Fetch current app config
            $appConfigs = [];
            try {
                $stmt = $db->query("SELECT config_key, config_value, updated_at FROM app_config");
                while ($row = $stmt->fetch()) {
                    $appConfigs[$row['config_key']] = $row;
                }
            } catch (Exception $e) {
                $error = "app_config table not found. Run the migration first.";
            }
        ?>
        <!-- ========== APP VERSION / FORCE UPDATE ========== -->
        <h2>📲 App Version & Force Update</h2>
        <p style="color: #64748b;">Control app version requirements and force users to update from Play Store.</p>
        
        <?php if (empty($appConfigs)): ?>
        <div class="card">
            <p style="text-align: center; color: #ef4444; padding: 40px;">
                app_config table not found or empty.<br><br>
                The table should have been created automatically. Check the database connection.
            </p>
        </div>
        <?php else: ?>
        
        <!-- Current Config -->
        <div class="stats-grid" style="margin-bottom: 20px;">
            <div class="stat-card">
                <h3><?= htmlspecialchars($appConfigs['min_version']['config_value'] ?? '1.0.0') ?></h3>
                <p>Minimum Version</p>
            </div>
            <div class="stat-card">
                <h3><?= htmlspecialchars($appConfigs['latest_version']['config_value'] ?? '1.0.0') ?></h3>
                <p>Latest Version</p>
            </div>
            <div class="stat-card" style="position: relative;">
                <?php $isForceOn = ($appConfigs['force_update']['config_value'] ?? 'false') === 'true'; ?>
                <form method="POST" style="margin: 0;">
                    <input type="hidden" name="action" value="toggle_force_update">
                    <button type="submit" style="
                        background: <?= $isForceOn ? '#ef4444' : '#22c55e' ?>;
                        color: white;
                        border: none;
                        padding: 12px 24px;
                        border-radius: 30px;
                        font-size: 1.1em;
                        font-weight: bold;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        gap: 8px;
                        margin: 0 auto;
                        transition: all 0.3s ease;
                    ">
                        <?= $isForceOn ? '🔴 ON' : '🟢 OFF' ?>
                    </button>
                </form>
                <p style="margin-top: 10px;">Force Update</p>
                <small style="color: #64748b;">Click to <?= $isForceOn ? 'disable' : 'enable' ?></small>
            </div>
            <div class="stat-card">
                <h3><?= date('M d, H:i', strtotime($appConfigs['min_version']['updated_at'] ?? 'now')) ?></h3>
                <p>Last Updated</p>
            </div>
        </div>
        
        <div class="card">
            <div class="card-header">⚙️ Update Configuration</div>
            <form method="POST">
                <input type="hidden" name="action" value="update_app_config">
                
                <div class="form-grid">
                    <label>
                        Minimum Version *
                        <input type="text" name="min_version" value="<?= htmlspecialchars($appConfigs['min_version']['config_value'] ?? '1.0.0') ?>" required placeholder="e.g. 1.0.0" pattern="[0-9]+\.[0-9]+\.[0-9]+">
                        <small style="color: #64748b;">Users below this version will be forced to update</small>
                    </label>
                    <label>
                        Latest Version *
                        <input type="text" name="latest_version" value="<?= htmlspecialchars($appConfigs['latest_version']['config_value'] ?? '1.0.0') ?>" required placeholder="e.g. 1.0.0" pattern="[0-9]+\.[0-9]+\.[0-9]+">
                        <small style="color: #64748b;">Current version on Play Store</small>
                    </label>
                </div>
                
                <div class="form-grid">
                    <label>
                        Play Store URL
                        <input type="url" name="play_store_url" value="<?= htmlspecialchars($appConfigs['play_store_url']['config_value'] ?? '') ?>" placeholder="https://play.google.com/store/apps/details?id=com.hingoli.hub">
                    </label>
                    <label style="display: flex; align-items: center; gap: 10px; padding-top: 25px;">
                        <input type="checkbox" name="force_update" style="width: auto;" <?= ($appConfigs['force_update']['config_value'] ?? 'false') === 'true' ? 'checked' : '' ?>>
                        <span style="color: #ef4444; font-weight: bold;">🚨 Force ALL Users to Update</span>
                    </label>
                </div>
                
                <label style="margin-top: 15px;">
                    Update Message (English)
                    <textarea name="update_message" rows="2" placeholder="Message shown to users"><?= htmlspecialchars($appConfigs['update_message']['config_value'] ?? 'A new version is available. Please update to continue.') ?></textarea>
                </label>
                
                <label>
                    Update Message (Marathi)
                    <textarea name="update_message_mr" rows="2" placeholder="मराठी संदेश"><?= htmlspecialchars($appConfigs['update_message_mr']['config_value'] ?? 'नवीन आवृत्ती उपलब्ध आहे. कृपया सुरू ठेवण्यासाठी अपडेट करा.') ?></textarea>
                </label>
                
                <button type="submit" style="margin-top: 15px;">💾 Save Configuration</button>
            </form>
        </div>
        
        <div class="card" style="margin-top: 20px;">
            <div class="card-header">📖 How It Works</div>
            <div style="padding: 15px; color: #64748b;">
                <ul style="margin: 0; padding-left: 20px;">
                    <li><strong>Minimum Version:</strong> Users with app version lower than this will see a force update dialog and cannot use the app until they update.</li>
                    <li><strong>Latest Version:</strong> The current version available on Play Store. Used for informational purposes.</li>
                    <li><strong>Force ALL Users:</strong> When enabled, ALL users will be forced to update regardless of their current version. Use this for critical updates.</li>
                    <li><strong>Update Message:</strong> Custom message shown in the update dialog.</li>
                </ul>
                <hr style="margin: 15px 0;">
                <p><strong>Quick Actions:</strong></p>
                <ul style="padding-left: 20px;">
                    <li>To force update: Set minimum version higher than current app version OR enable "Force ALL Users"</li>
                    <li>To disable force update: Disable "Force ALL Users" checkbox AND ensure minimum version is not higher than current app version</li>
                </ul>
            </div>
        </div>
        <?php endif; ?>
        
        <?php elseif ($tab === 'orders'): ?>
        <!-- ========== ORDERS ========== -->
        <h2>🛒 Orders Management</h2>
        <p style="color: #64748b;">Manage customer orders, update status, and process refunds.</p>
        
        <!-- Order Stats -->
        <div class="stats-grid" style="margin-bottom: 20px;">
            <div class="stat-card"><h3><?= $stats['total_orders'] ?></h3><p>Total Orders</p></div>
            <div class="stat-card"><h3><?= $stats['today_orders'] ?></h3><p>Today's Orders</p></div>
            <div class="stat-card"><h3><?= $stats['pending_orders'] ?></h3><p>Pending</p></div>
            <div class="stat-card"><h3>₹<?= number_format($stats['total_revenue'], 0) ?></h3><p>Revenue</p></div>
        </div>
        
        <!-- Filters -->
        <div class="card" style="margin-bottom: 15px; padding: 15px;">
            <form method="GET" style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center;">
                <input type="hidden" name="tab" value="orders">
                <select name="order_status" onchange="this.form.submit()">
                    <option value="">All Statuses</option>
                    <option value="pending" <?= $orderFilterStatus === 'pending' ? 'selected' : '' ?>>Pending</option>
                    <option value="confirmed" <?= $orderFilterStatus === 'confirmed' ? 'selected' : '' ?>>Confirmed</option>
                    <option value="processing" <?= $orderFilterStatus === 'processing' ? 'selected' : '' ?>>Processing</option>
                    <option value="shipped" <?= $orderFilterStatus === 'shipped' ? 'selected' : '' ?>>Shipped</option>
                    <option value="delivered" <?= $orderFilterStatus === 'delivered' ? 'selected' : '' ?>>Delivered</option>
                    <option value="cancelled" <?= $orderFilterStatus === 'cancelled' ? 'selected' : '' ?>>Cancelled</option>
                </select>
                <select name="payment_status" onchange="this.form.submit()">
                    <option value="">All Payments</option>
                    <option value="pending" <?= $orderFilterPayment === 'pending' ? 'selected' : '' ?>>Payment Pending</option>
                    <option value="paid" <?= $orderFilterPayment === 'paid' ? 'selected' : '' ?>>Paid</option>
                    <option value="failed" <?= $orderFilterPayment === 'failed' ? 'selected' : '' ?>>Failed</option>
                    <option value="refunded" <?= $orderFilterPayment === 'refunded' ? 'selected' : '' ?>>Refunded</option>
                </select>
                <?php if ($orderFilterStatus || $orderFilterPayment): ?>
                <a href="?tab=orders" class="btn btn-outline">Clear Filters</a>
                <?php endif; ?>
            </form>
        </div>
        
        <div class="card">
            <?php if (empty($orders)): ?>
                <p style="text-align: center; color: #64748b; padding: 40px;">No orders found.</p>
            <?php else: ?>
            <table>
                <thead><tr><th>Order #</th><th>Customer</th><th>Items</th><th>Amount</th><th>Payment</th><th>Status</th><th>Date</th><th>Actions</th></tr></thead>
                <tbody>
                <?php foreach ($orders as $o): ?>
                <tr>
                    <td><strong><a href="?tab=order_detail&id=<?= $o['order_id'] ?>"><?= $o['order_number'] ?></a></strong></td>
                    <td>
                        <?= htmlspecialchars($o['username'] ?? 'Guest') ?><br>
                        <small style="color:#64748b;"><?= $o['user_phone'] ?></small>
                    </td>
                    <td><?= $o['item_count'] ?> item(s)</td>
                    <td><strong>₹<?= number_format($o['total_amount'], 0) ?></strong></td>
                    <td>
                        <span class="badge <?= $o['payment_status'] === 'paid' ? 'badge-success' : ($o['payment_status'] === 'pending' ? 'badge-warning' : 'badge-danger') ?>">
                            <?= ucfirst($o['payment_status']) ?>
                        </span>
                        <?php if ($o['payment_method']): ?>
                        <br><small><?= strtoupper($o['payment_method']) ?></small>
                        <?php endif; ?>
                    </td>
                    <td>
                        <form method="POST" style="display:inline;">
                            <input type="hidden" name="action" value="update_order_status">
                            <input type="hidden" name="order_id" value="<?= $o['order_id'] ?>">
                            <select name="order_status" onchange="this.form.submit()" style="font-size: 0.85em; padding: 4px 8px;">
                                <option value="pending" <?= $o['order_status'] === 'pending' ? 'selected' : '' ?>>⏳ Pending</option>
                                <option value="confirmed" <?= $o['order_status'] === 'confirmed' ? 'selected' : '' ?>>✅ Confirmed</option>
                                <option value="processing" <?= $o['order_status'] === 'processing' ? 'selected' : '' ?>>🔄 Processing</option>
                                <option value="shipped" <?= $o['order_status'] === 'shipped' ? 'selected' : '' ?>>📦 Shipped</option>
                                <option value="delivered" <?= $o['order_status'] === 'delivered' ? 'selected' : '' ?>>🎉 Delivered</option>
                                <option value="cancelled" <?= $o['order_status'] === 'cancelled' ? 'selected' : '' ?>>❌ Cancelled</option>
                            </select>
                        </form>
                    </td>
                    <td><?= date('M d, H:i', strtotime($o['created_at'])) ?></td>
                    <td style="white-space: nowrap;">
                        <a href="?tab=order_detail&id=<?= $o['order_id'] ?>" class="btn btn-small">👁️ View</a>
                        <?php if ($o['payment_status'] === 'paid' && !in_array($o['order_status'], ['refunded', 'cancelled'])): ?>
                        <form method="POST" style="display:inline;" onsubmit="return confirm('Process refund of ₹<?= $o['total_amount'] ?>?');">
                            <input type="hidden" name="action" value="process_refund">
                            <input type="hidden" name="order_id" value="<?= $o['order_id'] ?>">
                            <button type="submit" class="btn btn-small btn-danger">💸 Refund</button>
                        </form>
                        <?php endif; ?>
                    </td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            <?php endif; ?>
        </div>
        
        <?php elseif ($tab === 'order_detail' && isset($orderDetail) && $orderDetail): ?>
        <!-- ========== ORDER DETAIL ========== -->
        <h2><a href="?tab=orders">← Orders</a> &nbsp;Order #<?= $orderDetail['order_number'] ?></h2>
        
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px;">
            <!-- Order Info -->
            <div class="card">
                <div class="card-header">Order Information</div>
                <table style="width: 100%;">
                    <tr><td><strong>Order ID:</strong></td><td><?= $orderDetail['order_id'] ?></td></tr>
                    <tr><td><strong>Order Number:</strong></td><td><?= $orderDetail['order_number'] ?></td></tr>
                    <tr><td><strong>Order Status:</strong></td><td><span class="badge badge-info"><?= ucfirst($orderDetail['order_status']) ?></span></td></tr>
                    <tr><td><strong>Payment Status:</strong></td><td><span class="badge <?= $orderDetail['payment_status'] === 'paid' ? 'badge-success' : 'badge-warning' ?>"><?= ucfirst($orderDetail['payment_status']) ?></span></td></tr>
                    <tr><td><strong>Payment Method:</strong></td><td><?= strtoupper($orderDetail['payment_method'] ?? '-') ?></td></tr>
                    <tr><td><strong>Razorpay Order ID:</strong></td><td><small><?= $orderDetail['razorpay_order_id'] ?? '-' ?></small></td></tr>
                    <tr><td><strong>Razorpay Payment ID:</strong></td><td><small><?= $orderDetail['razorpay_payment_id'] ?? '-' ?></small></td></tr>
                    <tr><td><strong>Created:</strong></td><td><?= date('M d, Y H:i', strtotime($orderDetail['created_at'])) ?></td></tr>
                </table>
            </div>
            
            <!-- Customer & Delivery -->
            <div class="card">
                <div class="card-header">Customer & Delivery</div>
                <table style="width: 100%;">
                    <tr><td><strong>Customer:</strong></td><td><?= htmlspecialchars($orderDetail['username'] ?? 'Guest') ?></td></tr>
                    <tr><td><strong>Phone:</strong></td><td><a href="tel:<?= $orderDetail['user_phone'] ?>"><?= $orderDetail['user_phone'] ?></a></td></tr>
                    <tr><td><strong>Email:</strong></td><td><?= $orderDetail['user_email'] ?? '-' ?></td></tr>
                    <tr><td colspan="2"><hr style="margin: 10px 0;"></td></tr>
                    <tr><td><strong>Delivery Name:</strong></td><td><?= htmlspecialchars($orderDetail['addr_name'] ?? '-') ?></td></tr>
                    <tr><td><strong>Address:</strong></td><td><?= htmlspecialchars($orderDetail['address_line1'] ?? '') ?> <?= $orderDetail['address_line2'] ?></td></tr>
                    <tr><td><strong>City:</strong></td><td><?= $orderDetail['addr_city'] ?>, <?= $orderDetail['addr_state'] ?> - <?= $orderDetail['addr_pincode'] ?></td></tr>
                    <tr><td><strong>Delivery Phone:</strong></td><td><?= $orderDetail['addr_phone'] ?></td></tr>
                </table>
            </div>
        </div>
        
        <!-- Order Items -->
        <div class="card">
            <div class="card-header">Order Items</div>
            <table>
                <thead><tr><th>Image</th><th>Product</th><th>Seller</th><th>Qty</th><th>Price</th><th>Subtotal</th><th>Status</th></tr></thead>
                <tbody>
                <?php foreach ($orderItems as $item): ?>
                <tr>
                    <td><img src="<?= $item['main_image_url'] ?>" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;"></td>
                    <td><strong><?= htmlspecialchars($item['title']) ?></strong></td>
                    <td><?= htmlspecialchars($item['seller_name'] ?? '-') ?></td>
                    <td><?= $item['quantity'] ?></td>
                    <td>₹<?= number_format($item['price'], 0) ?></td>
                    <td><strong>₹<?= number_format($item['price'] * $item['quantity'], 0) ?></strong></td>
                    <td><span class="badge badge-info"><?= ucfirst($item['item_status']) ?></span></td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            
            <div style="text-align: right; padding: 15px; border-top: 1px solid #e2e8f0;">
                <p>Subtotal: ₹<?= number_format($orderDetail['subtotal'], 0) ?></p>
                <p>Shipping: ₹<?= number_format($orderDetail['shipping_fee'], 0) ?></p>
                <p style="font-size: 1.2em;"><strong>Total: ₹<?= number_format($orderDetail['total_amount'], 0) ?></strong></p>
            </div>
        </div>
        
        <!-- Actions -->
        <div class="card" style="margin-top: 20px;">
            <div class="card-header">Actions</div>
            <div style="display: flex; gap: 10px; padding: 15px;">
                <form method="POST">
                    <input type="hidden" name="action" value="update_order_status">
                    <input type="hidden" name="order_id" value="<?= $orderDetail['order_id'] ?>">
                    <select name="order_status" style="padding: 10px;">
                        <option value="pending" <?= $orderDetail['order_status'] === 'pending' ? 'selected' : '' ?>>Pending</option>
                        <option value="confirmed" <?= $orderDetail['order_status'] === 'confirmed' ? 'selected' : '' ?>>Confirmed</option>
                        <option value="processing" <?= $orderDetail['order_status'] === 'processing' ? 'selected' : '' ?>>Processing</option>
                        <option value="shipped" <?= $orderDetail['order_status'] === 'shipped' ? 'selected' : '' ?>>Shipped</option>
                        <option value="delivered" <?= $orderDetail['order_status'] === 'delivered' ? 'selected' : '' ?>>Delivered</option>
                        <option value="cancelled" <?= $orderDetail['order_status'] === 'cancelled' ? 'selected' : '' ?>>Cancelled</option>
                    </select>
                    <button type="submit">Update Status</button>
                </form>
                
                <?php if ($orderDetail['payment_status'] === 'paid' && !in_array($orderDetail['order_status'], ['refunded', 'cancelled'])): ?>
                <form method="POST" onsubmit="return confirm('Process full refund of ₹<?= $orderDetail['total_amount'] ?>?');">
                    <input type="hidden" name="action" value="process_refund">
                    <input type="hidden" name="order_id" value="<?= $orderDetail['order_id'] ?>">
                    <button type="submit" class="btn btn-danger">💸 Process Full Refund</button>
                </form>
                <?php endif; ?>
            </div>
        </div>
        
        <?php elseif ($tab === 'pincodes'): ?>
        <!-- ========== DELIVERY PINCODES ========== -->
        <h2>📍 Delivery Zones Management</h2>
        <p style="color: #64748b;">Manage serviceable pincodes, delivery times, and shipping fees.</p>
        
        <!-- Add Pincode Form -->
        <div class="card" style="margin-bottom: 20px;">
            <div class="card-header">Add Serviceable Pincode</div>
            <form method="POST" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 10px; padding: 15px;">
                <input type="hidden" name="action" value="add_pincode">
                <input type="text" name="pincode" placeholder="Pincode (6 digits)" pattern="[0-9]{6}" required>
                <input type="text" name="city_name" placeholder="City Name" required>
                <input type="number" name="delivery_days" placeholder="Delivery Days" value="2" min="0" required>
                <input type="text" name="delivery_time" placeholder="Delivery Time" value="6 PM">
                <input type="number" name="shipping_fee" placeholder="Shipping Fee" value="50" step="0.01" required>
                <input type="number" name="cutoff_hour" placeholder="Cutoff Hour (24h)" value="14" min="0" max="23">
                <button type="submit">➕ Add Pincode</button>
            </form>
        </div>
        
        <div class="card">
            <div class="card-header">Serviceable Pincodes</div>
            <?php if (empty($pincodes)): ?>
                <p style="text-align: center; color: #64748b; padding: 40px;">No pincodes configured. Add pincodes above to enable delivery.</p>
            <?php else: ?>
            <table>
                <thead><tr><th>Pincode</th><th>City</th><th>Delivery Days</th><th>Delivery Time</th><th>Shipping Fee</th><th>Cutoff</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>
                <?php foreach ($pincodes as $p): ?>
                <tr>
                    <td><strong><?= htmlspecialchars($p['pincode']) ?></strong></td>
                    <td><?= htmlspecialchars($p['city_name'] ?? '') ?></td>
                    <td><?= $p['delivery_days'] ?? 1 ?> day(s)</td>
                    <td><?= htmlspecialchars($p['delivery_time'] ?? '5 PM') ?></td>
                    <td>₹<?= number_format(floatval($p['shipping_fee'] ?? 0), 0) ?></td>
                    <td><?= $p['cutoff_hour'] ?? 14 ?>:00</td>
                    <td>
                        <span class="badge <?= ($p['is_serviceable'] ?? 1) ? 'badge-success' : 'badge-danger' ?>">
                            <?= ($p['is_serviceable'] ?? 1) ? 'Active' : 'Inactive' ?>
                        </span>
                    </td>
                    <td style="white-space: nowrap;">
                        <button type="button" class="btn btn-small" onclick="document.getElementById('edit-<?= $p['pincode'] ?>').style.display='table-row'">✏️ Edit</button>
                        <form method="POST" style="display:inline;" onsubmit="return confirm('Delete pincode <?= $p['pincode'] ?>?');">
                            <input type="hidden" name="action" value="delete_pincode">
                            <input type="hidden" name="pincode_id" value="<?= $p['pincode'] ?>">
                            <button type="submit" class="btn btn-small btn-danger">🗑️</button>
                        </form>
                    </td>
                </tr>
                <!-- Inline Edit Row -->
                <tr id="edit-<?= $p['pincode'] ?>" style="display: none; background: #f8fafc;">
                    <td colspan="8">
                        <form method="POST" style="display: flex; gap: 10px; align-items: center; flex-wrap: wrap; padding: 10px;">
                            <input type="hidden" name="action" value="edit_pincode">
                            <input type="hidden" name="pincode_id" value="<?= $p['pincode'] ?>">
                            <label>Days: <input type="number" name="delivery_days" value="<?= $p['delivery_days'] ?? 1 ?>" min="0" style="width: 60px;"></label>
                            <label>Time: <input type="text" name="delivery_time" value="<?= htmlspecialchars($p['delivery_time'] ?? '5 PM') ?>" style="width: 80px;"></label>
                            <label>Fee: ₹<input type="number" name="shipping_fee" value="<?= $p['shipping_fee'] ?? 0 ?>" step="0.01" style="width: 80px;"></label>
                            <label>Cutoff: <input type="number" name="cutoff_hour" value="<?= $p['cutoff_hour'] ?? 14 ?>" min="0" max="23" style="width: 60px;">:00</label>
                            <label><input type="checkbox" name="is_serviceable" <?= ($p['is_serviceable'] ?? 1) ? 'checked' : '' ?>> Active</label>
                            <button type="submit" class="btn btn-small">💾 Save</button>
                            <button type="button" class="btn btn-small btn-outline" onclick="this.closest('tr').style.display='none'">Cancel</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            <?php endif; ?>
        </div>

<?php elseif ($tab === 'leads'): ?>
        <!-- ========== LEADS ========== -->
        <h2>📞 Leads Management</h2>
        <p style="color: #64748b; margin-bottom: 20px;">View contact requests (calls & chats) from app users.</p>
        
        <?php if (!empty($leadsError)): ?>
        <div class="alert alert-danger" style="background: #fee2e2; border: 1px solid #dc2626; color: #991b1b; padding: 15px; border-radius: 8px; margin-bottom: 20px;">
            <strong>❌ Database Error:</strong> <?= htmlspecialchars($leadsError) ?>
        </div>
        <?php endif; ?>
        
        <div class="card">
            <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                <span><strong>Total: <?= count($leadsList) ?> leads</strong></span>
                <span style="color: #64748b; font-size: 14px;">Contact requests from app users</span>
            </div>
            
            <?php if (empty($leadsList)): ?>
                <p style="text-align: center; color: #64748b; padding: 60px 20px;">
                    <span style="font-size: 48px; display: block; margin-bottom: 15px;">📭</span>
                    No leads yet. Leads are logged when users tap Call or Chat on listings.
                </p>
            <?php else: ?>
            <div style="overflow-x: auto;">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Date & Time</th>
                        <th>Type</th>
                        <th>Customer</th>
                        <th>Phone</th>
                        <th>Listing</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                <?php foreach ($leadsList as $lead): ?>
                <tr>
                    <td><strong>#<?= $lead['enquiry_id'] ?></strong></td>
                    <td style="white-space: nowrap; font-size: 13px;">
                        <?= date('d M Y', strtotime($lead['created_at'])) ?><br>
                        <span style="color: #64748b;"><?= date('h:i A', strtotime($lead['created_at'])) ?></span>
                    </td>
                    <td>
                        <?php if ($lead['enquiry_type'] === 'call'): ?>
                            <span class="badge badge-info" style="background: #3b82f6;">📞 Call</span>
                        <?php elseif ($lead['enquiry_type'] === 'chat'): ?>
                            <span class="badge badge-success" style="background: #22c55e;">💬 Chat</span>
                        <?php elseif ($lead['enquiry_type'] === 'whatsapp'): ?>
                            <span class="badge badge-warning" style="background: #25d366;">📱 WhatsApp</span>
                        <?php else: ?>
                            <span class="badge badge-secondary"><?= ucfirst($lead['enquiry_type'] ?? 'Unknown') ?></span>
                        <?php endif; ?>
                    </td>
                    <td>
                        <strong><?= htmlspecialchars($lead['name'] ?? $lead['username'] ?? 'Guest') ?></strong>
                        <?php if (!empty($lead['user_id'])): ?>
                            <br><span style="color: #64748b; font-size: 12px;">User #<?= $lead['user_id'] ?></span>
                        <?php endif; ?>
                    </td>
                    <td>
                        <?php if (!empty($lead['phone'])): ?>
                            <a href="tel:<?= htmlspecialchars($lead['phone']) ?>" style="color: #3b82f6; text-decoration: none;">
                                <?= htmlspecialchars($lead['phone']) ?>
                            </a>
                        <?php elseif (!empty($lead['user_phone'])): ?>
                            <a href="tel:<?= htmlspecialchars($lead['user_phone']) ?>" style="color: #3b82f6; text-decoration: none;">
                                <?= htmlspecialchars($lead['user_phone']) ?>
                            </a>
                        <?php else: ?>
                            <span style="color: #94a3b8;">Not provided</span>
                        <?php endif; ?>
                    </td>
                    <td style="max-width: 200px;">
                        <?php if ($lead['listing_id']): ?>
                            <a href="?tab=edit_listing&edit=<?= $lead['listing_id'] ?>" style="color: #3b82f6; text-decoration: none;" title="<?= htmlspecialchars($lead['listing_title'] ?? '') ?>">
                                <?= htmlspecialchars(mb_substr($lead['listing_title'] ?? 'Listing #'.$lead['listing_id'], 0, 30)) ?>
                                <?= strlen($lead['listing_title'] ?? '') > 30 ? '...' : '' ?>
                            </a>
                            <?php if (!empty($lead['listing_type'])): ?>
                                <br><span class="badge badge-info" style="font-size: 10px;"><?= ucfirst($lead['listing_type']) ?></span>
                            <?php endif; ?>
                        <?php else: ?>
                            <span style="color: #94a3b8;">—</span>
                        <?php endif; ?>
                    </td>
                    <td>
                        <span class="badge <?= 
                            $lead['status'] === 'new' ? 'badge-warning' : 
                            ($lead['status'] === 'contacted' ? 'badge-info' : 
                            ($lead['status'] === 'resolved' ? 'badge-success' : 'badge-danger')) 
                        ?>">
                            <?= ucfirst($lead['status'] ?? 'new') ?>
                        </span>
                    </td>
                    <td style="white-space: nowrap;">
                        <form method="POST" style="display: inline;">
                            <input type="hidden" name="action" value="update_enquiry_status">
                            <input type="hidden" name="enquiry_id" value="<?= $lead['enquiry_id'] ?>">
                            <select name="status" onchange="this.form.submit()" style="padding: 6px 10px; font-size: 13px; border-radius: 6px; border: 1px solid #e2e8f0;">
                                <option value="new" <?= ($lead['status'] ?? '') === 'new' ? 'selected' : '' ?>>🆕 New</option>
                                <option value="contacted" <?= ($lead['status'] ?? '') === 'contacted' ? 'selected' : '' ?>>📞 Contacted</option>
                                <option value="resolved" <?= ($lead['status'] ?? '') === 'resolved' ? 'selected' : '' ?>>✅ Resolved</option>
                                <option value="spam" <?= ($lead['status'] ?? '') === 'spam' ? 'selected' : '' ?>>🚫 Spam</option>
                            </select>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
            </div>
            <?php endif; ?>
        </div>

        <?php endif; ?>

    </div>
    </div>
    <div style="text-align: center; padding: 10px; color: #999; font-size: 12px;">
        Version: 2024-12-17-v4
    </div>
</body>
</html>
