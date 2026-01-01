<?php
// Fetch Common Data
$categories = $db->query("SELECT * FROM categories WHERE parent_id IS NULL ORDER BY name")->fetchAll();
$subcategories = $db->query("SELECT * FROM categories WHERE parent_id IS NOT NULL ORDER BY name")->fetchAll(); // Simplified
try {
    $cities = $db->query("SELECT DISTINCT name FROM cities ORDER BY name")->fetchAll();
} catch (Exception $e) { $cities = []; }
try {
    $users = $db->query("SELECT user_id, username, phone FROM users ORDER BY user_id")->fetchAll();
} catch (Exception $e) { $users = []; }

$editListing = null;
$editGallery = [];
$editPricelist = [];
$editProducts = [];

// Handle Actions
$error = null;
$success = null;
$debug_log = [];
$isAjax = !empty($_SERVER['HTTP_X_REQUESTED_WITH']) && strtolower($_SERVER['HTTP_X_REQUESTED_WITH']) === 'xmlhttprequest';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $debug_log[] = "POST received: " . date('Y-m-d H:i:s');
    $debug_log[] = "Action: " . ($_POST['action'] ?? 'not set');
    $debug_log[] = "AJAX: " . ($isAjax ? 'yes' : 'no');
    
    // Log to file for debugging
    error_log("[Listing Form] " . implode(" | ", $debug_log));
    
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'add_listing':
                    $title = $_POST['title'];
                    $description = $_POST['description'];
                    $listing_type = $_POST['listing_type'];
                    $category_id = $_POST['category_id'];
                    $subcategory_id = !empty($_POST['subcategory_id']) ? $_POST['subcategory_id'] : null;
                    $location = $_POST['location'];
                    $city = $_POST['city'];
                    $state = $_POST['state'] ?? 'Maharashtra';
                    $user_id = $_POST['user_id'] ?? 1; // Default to admin/system user if not specified
                    $status = $_POST['status'] ?? 'active';
                    $is_verified = isset($_POST['is_verified']) ? 1 : 0;
                    $is_featured = isset($_POST['is_featured']) ? 1 : 0;
                    $latitude = !empty($_POST['latitude']) ? (float)$_POST['latitude'] : null;
                    $longitude = !empty($_POST['longitude']) ? (float)$_POST['longitude'] : null;
                    
                    // Main Image
                    $main_image_url = null;
                    if (!empty($_POST['main_image_url'])) {
                        $main_image_url = $_POST['main_image_url'];
                    } elseif (isset($_FILES['main_image'])) {
                        // Use listing title for filename
                        $main_image_url = uploadImage($_FILES['main_image'], 'listings', $title);
                    }
                    
                    // Get next sequential ID (avoiding TiDB auto_increment gaps)
                    $nextIdStmt = $db->query("SELECT COALESCE(MAX(listing_id), 0) + 1 as next_id FROM listings");
                    $next_listing_id = $nextIdStmt->fetch()['next_id'];
                    
                    $stmt = $db->prepare("INSERT INTO listings 
                        (listing_id, listing_type, title, description, category_id, subcategory_id, location, city, state, latitude, longitude, main_image_url, user_id, status, is_verified, is_featured) 
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    $stmt->execute([$next_listing_id, $listing_type, $title, $description, $category_id, $subcategory_id, $location, $city, $state, $latitude, $longitude, $main_image_url, $user_id, $status, $is_verified, $is_featured]);
                    $listing_id = $next_listing_id;
                    
                    // Gallery - use title for naming
                    if (isset($_FILES['gallery_images'])) {
                        $galleryUrls = uploadMultipleImages($_FILES['gallery_images'], 'listings', $title . 'gallery');
                        foreach ($galleryUrls as $index => $url) {
                            $stmt = $db->prepare("INSERT INTO listing_images (listing_id, image_url, sort_order) VALUES (?, ?, ?)");
                            $stmt->execute([$listing_id, $url, $index]);
                        }
                    }
                    
                    // Type Specific Data
                    if ($listing_type === 'services' && !empty($_POST['experience_years'])) {
                        $stmt = $db->prepare("INSERT INTO services_listings (listing_id, experience_years) VALUES (?, ?)");
                        $stmt->execute([$listing_id, $_POST['experience_years']]);
                    } elseif ($listing_type === 'jobs') {
                        $stmt = $db->prepare("INSERT INTO job_listings 
                            (listing_id, job_title, employment_type, salary_min, salary_max, salary_period, experience_required_years, education_required, remote_option, vacancies) 
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
                        $stmt = $db->prepare("INSERT INTO business_listings (listing_id, business_name, industry, established_year, employee_count) VALUES (?, ?, ?, ?, ?)");
                        $stmt->execute([
                            $listing_id,
                            $_POST['business_name'] ?? $title,
                            $_POST['industry'] ?? null,
                            !empty($_POST['established_year']) ? $_POST['established_year'] : null,
                            $_POST['employee_count'] ?? null
                        ]);
                    }
                    
                    $success = "Listing created successfully! (ID: $listing_id)";
                    if ($isAjax) {
                        header('Content-Type: application/json');
                        echo json_encode(['success' => true, 'message' => $success, 'listing_id' => $listing_id]);
                        exit;
                    }
                    header("Location: index.php?page=listing_form&id=$listing_id&msg=" . urlencode($success));
                    exit;

                case 'edit_listing':
                    $listing_id = $_POST['listing_id'];
                    $title = $_POST['title'];
                    $description = $_POST['description'];
                    $location = $_POST['location'];
                    $city = $_POST['city'];
                    $category_id = $_POST['category_id'];
                    $subcategory_id = !empty($_POST['subcategory_id']) ? $_POST['subcategory_id'] : null;
                    $status = $_POST['status'];
                    $is_verified = isset($_POST['is_verified']) ? 1 : 0;
                    $is_featured = isset($_POST['is_featured']) ? 1 : 0;
                    $latitude = !empty($_POST['latitude']) ? (float)$_POST['latitude'] : null;
                    $longitude = !empty($_POST['longitude']) ? (float)$_POST['longitude'] : null;
                    
                    $main_image_url = $_POST['existing_image'] ?? null;
                    if (!empty($_POST['main_image_url'])) {
                        $main_image_url = $_POST['main_image_url'];
                    } elseif (isset($_FILES['main_image']) && $_FILES['main_image']['error'] === UPLOAD_ERR_OK) {
                        // Use listing title for filename
                        $main_image_url = uploadImage($_FILES['main_image'], 'listings', $title);
                    }
                    
                    // Get type
                    $stmt = $db->prepare("SELECT listing_type FROM listings WHERE listing_id = ?");
                    $stmt->execute([$listing_id]);
                    $listing_type = $stmt->fetchColumn();
                    
                    $stmt = $db->prepare("UPDATE listings SET 
                        title = ?, description = ?, location = ?, city = ?, 
                        category_id = ?, subcategory_id = ?, main_image_url = ?, status = ?, 
                        is_verified = ?, is_featured = ?, latitude = ?, longitude = ?, updated_at = NOW()
                        WHERE listing_id = ?");
                    $stmt->execute([$title, $description, $location, $city, $category_id, $subcategory_id, $main_image_url, $status, $is_verified, $is_featured, $latitude, $longitude, $listing_id]);
                    
                    // Update type-specific
                    if ($listing_type === 'services') {
                        $db->prepare("DELETE FROM services_listings WHERE listing_id = ?")->execute([$listing_id]);
                        $db->prepare("INSERT INTO services_listings (listing_id, experience_years) VALUES (?, ?)")->execute([$listing_id, $_POST['experience_years'] ?? 0]);
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
                    
                    $success = "Listing updated successfully!";
                    if ($isAjax) {
                        header('Content-Type: application/json');
                        echo json_encode(['success' => true, 'message' => $success, 'listing_id' => $listing_id]);
                        exit;
                    }
                    header("Location: index.php?page=listing_form&id=$listing_id&msg=" . urlencode($success));
                    exit;

                case 'add_gallery_image':
                    $listing_id = $_POST['listing_id'];
                    $added_count = 0;
                    
                    // Get listing title for naming
                    $titleStmt = $db->prepare("SELECT title FROM listings WHERE listing_id = ?");
                    $titleStmt->execute([$listing_id]);
                    $listingTitle = $titleStmt->fetchColumn() ?: 'gallery';
                    
                    // Handle file uploads (with compression)
                    if (isset($_FILES['gallery_files']) && !empty($_FILES['gallery_files']['name'][0])) {
                        $galleryUrls = uploadMultipleImages($_FILES['gallery_files'], 'listings', $listingTitle . 'gallery');
                        foreach ($galleryUrls as $index => $url) {
                            $db->prepare("INSERT INTO listing_images (listing_id, image_url, sort_order) VALUES (?, ?, 0)")->execute([$listing_id, $url]);
                            $added_count++;
                        }
                    }
                    
                    // Handle URL input
                    if (!empty($_POST['image_url'])) {
                        $image_url = $_POST['image_url'];
                        $db->prepare("INSERT INTO listing_images (listing_id, image_url, sort_order) VALUES (?, ?, 0)")->execute([$listing_id, $image_url]);
                        $added_count++;
                    }
                    
                    $success = $added_count > 0 ? "$added_count image(s) added successfully!" : "No images added.";
                    if ($isAjax) {
                        header('Content-Type: application/json');
                        echo json_encode(['success' => $added_count > 0, 'message' => $success, 'added_count' => $added_count]);
                        exit;
                    }
                    header("Location: index.php?page=listing_form&id=$listing_id&msg=" . urlencode($success));
                    exit;
                    
                case 'delete_gallery_image':
                    $image_id = $_POST['image_id'];
                    $listing_id = $_POST['listing_id'];
                    $db->prepare("DELETE FROM listing_images WHERE image_id = ?")->execute([$image_id]);
                    
                    $success = "Image deleted successfully!";
                    if ($isAjax) {
                        header('Content-Type: application/json');
                        echo json_encode(['success' => true, 'message' => $success]);
                        exit;
                    }
                    header("Location: index.php?page=listing_form&id=$listing_id&msg=" . urlencode($success));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
        error_log("[Listing Form ERROR] " . $e->getMessage() . " | Trace: " . $e->getTraceAsString());
        if ($isAjax) {
            header('Content-Type: application/json');
            echo json_encode(['success' => false, 'error' => $error]);
            exit;
        }
    } catch (PDOException $e) {
        $error = "Database error: " . $e->getMessage();
        error_log("[Listing Form DB ERROR] " . $e->getMessage() . " | Code: " . $e->getCode());
        if ($isAjax) {
            header('Content-Type: application/json');
            echo json_encode(['success' => false, 'error' => $error]);
            exit;
        }
    }
}

// Fetch Existing Data if Editing
if (isset($_GET['id'])) {
    $id = $_GET['id'];
    $stmt = $db->prepare("SELECT l.*, c.name as category_name,
        jl.salary_min, jl.salary_max, jl.salary_period, jl.employment_type, jl.education_required, jl.experience_required_years as experience_required, jl.remote_option, jl.vacancies,
        service.experience_years, 
        bl.business_name, bl.industry, bl.established_year, bl.employee_count
        FROM listings l
        LEFT JOIN categories c ON l.category_id = c.category_id
        LEFT JOIN job_listings jl ON l.listing_id = jl.listing_id
        LEFT JOIN services_listings service ON l.listing_id = service.listing_id
        LEFT JOIN business_listings bl ON l.listing_id = bl.listing_id
        WHERE l.listing_id = ?");
    $stmt->execute([$id]);
    $editListing = $stmt->fetch();
    
    if ($editListing) {
        $editGallery = $db->prepare("SELECT * FROM listing_images WHERE listing_id = ? ORDER BY sort_order")->execute([$id]) ? $stmt->fetchAll() : []; // Fix logic, stmt already used
        
        $galleryStmt = $db->prepare("SELECT * FROM listing_images WHERE listing_id = ? ORDER BY sort_order");
        $galleryStmt->execute([$id]);
        $editGallery = $galleryStmt->fetchAll();
        
        $priceStmt = $db->prepare("SELECT * FROM listing_price_list WHERE listing_id = ? ORDER BY sort_order");
        $priceStmt->execute([$id]);
        $editPricelist = $priceStmt->fetchAll();
        
        if ($editListing['listing_type'] === 'business') {
            $prodStmt = $db->prepare("SELECT sp.*, c.name as category_name FROM shop_products sp LEFT JOIN categories c ON sp.category_id = c.category_id WHERE listing_id = ? ORDER BY sort_order");
            $prodStmt->execute([$id]);
            $editProducts = $prodStmt->fetchAll();
        }
    }
}
?>

<?php if (!empty($error)): ?>
<div style="background: #fee2e2; border: 1px solid #ef4444; color: #991b1b; padding: 1rem; border-radius: 8px; margin-bottom: 1rem;">
    <strong>⚠️ Error:</strong> <?= htmlspecialchars($error) ?>
</div>
<?php endif; ?>

<?php if (!empty($_GET['msg'])): ?>
<div style="background: #dcfce7; border: 1px solid #22c55e; color: #166534; padding: 1rem; border-radius: 8px; margin-bottom: 1rem;">
    ✅ <?= htmlspecialchars($_GET['msg']) ?>
</div>
<?php endif; ?>

<div class="header">
    <div class="page-title"><?= $editListing ? 'Edit Listing' : 'Add New Listing' ?></div>
    <a href="index.php?page=listings" class="btn btn-outline">← Back to Listings</a>
</div>

<div class="card">
    <form method="POST" enctype="multipart/form-data">
        <input type="hidden" name="action" value="<?= $editListing ? 'edit_listing' : 'add_listing' ?>">
        <?php if ($editListing): ?>
        <input type="hidden" name="listing_id" value="<?= $editListing['listing_id'] ?>">
        <input type="hidden" name="existing_image" value="<?= $editListing['main_image_url'] ?>">
        <?php endif; ?>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; margin-bottom: 1.5rem;">
            <?= $editListing ? '' : '
            <label>
                <strong>Listing Type *</strong>
                <select name="listing_type" required onchange="toggleFields(this.value)" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="">Select Type</option>
                    <option value="services">Services</option>
                    <option value="selling">Selling</option>
                    <option value="business">Business</option>
                    <option value="jobs">Jobs</option>
                </select>
            </label>
            ' ?>
            
            <label>
                <strong>Title *</strong>
                <input type="text" name="title" value="<?= $editListing['title'] ?? '' ?>" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            
            <label>
                <strong>Category *</strong>
                <select name="category_id" id="category-select" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="">Select Category (type to search)</option>
                    <?php foreach ($categories as $cat): ?>
                    <option value="<?= $cat['category_id'] ?>" <?= ($editListing['category_id'] ?? '') == $cat['category_id'] ? 'selected' : '' ?>>
                        <?= htmlspecialchars($cat['name']) ?> (<?= $cat['listing_type'] ?>)
                    </option>
                    <?php endforeach; ?>
                </select>
            </label>
            
            <label>
                <strong>Subcategory</strong>
                <select name="subcategory_id" id="subcategory-select" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="">None (type to search)</option>
                    <?php foreach ($subcategories as $sub): ?>
                    <option value="<?= $sub['category_id'] ?>" <?= ($editListing['subcategory_id'] ?? '') == $sub['category_id'] ? 'selected' : '' ?>>
                        <?= htmlspecialchars($sub['name']) ?>
                    </option>
                    <?php endforeach; ?>
                </select>
            </label>
            
            <label>
                <strong>Status</strong>
                <select name="status" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="active" <?= ($editListing['status'] ?? '') === 'active' ? 'selected' : '' ?>>Active</option>
                    <option value="pending" <?= ($editListing['status'] ?? '') === 'pending' ? 'selected' : '' ?>>Pending</option>
                    <option value="draft" <?= ($editListing['status'] ?? '') === 'draft' ? 'selected' : '' ?>>Draft</option>
                    <option value="rejected" <?= ($editListing['status'] ?? '') === 'rejected' ? 'selected' : '' ?>>Rejected</option>
                </select>
            </label>
            
            <label>
                <strong>Owner/User *</strong>
                <select name="user_id" id="user-select" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <?php foreach ($users as $u): ?>
                    <option value="<?= $u['user_id'] ?>" <?= (($editListing['user_id'] ?? 1) == $u['user_id']) ? 'selected' : '' ?>>
                        <?= htmlspecialchars($u['username']) ?> (<?= $u['phone'] ?>)
                    </option>
                    <?php endforeach; ?>
                </select>
                <small style="color: #666;">Default: Admin (9999999999)</small>
            </label>
            
            <label>
                <strong>City *</strong>
                <select name="city" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <?php foreach ($cities as $c): ?>
                    <option value="<?= $c['name'] ?>" <?= ($editListing['city'] ?? 'Hingoli') === $c['name'] ? 'selected' : '' ?>>
                        <?= htmlspecialchars($c['name']) ?>
                    </option>
                    <?php endforeach; ?>
                </select>
            </label>
            
            <label>
                <strong>Location/Address</strong>
                <input type="text" name="location" value="<?= $editListing['location'] ?? '' ?>" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
        </div>
        
        <label style="display: block; margin-bottom: 1.5rem;">
            <strong>Description</strong>
            <textarea name="description" rows="4" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;"><?= $editListing['description'] ?? '' ?></textarea>
        </label>
        
        <!-- Type Specific Fields (Shown based on JS or Edit Mode) -->
        <?php 
        $type = $editListing['listing_type'] ?? ''; 
        $displayService = ($type === 'services') ? 'block' : 'none';
        $displayJob = ($type === 'jobs') ? 'block' : 'none';
        $displayBusiness = ($type === 'business') ? 'block' : 'none';
        $displayPrice = ($type !== 'business') ? 'block' : 'none';
        ?>
        
        <div id="price-field" style="display: <?= $displayPrice ?>; margin-bottom: 1.5rem;">
            <label><strong>Price (₹)</strong> <input type="number" name="price" value="<?= $editListing['price'] ?? '' ?>" step="0.01" style="padding: 0.5rem;"></label>
        </div>
        
        <div id="service-fields" style="display: <?= $displayService ?>; background: rgba(59, 130, 246, 0.1); border: 1px solid rgba(59, 130, 246, 0.3); padding: 1rem; border-radius: 8px; margin-bottom: 1.5rem;">
            <h3 style="margin-top:0; color: #3b82f6;">🔧 Service Details</h3>
            <label>Experience (Years) <input type="number" name="experience_years" value="<?= $editListing['experience_years'] ?? '' ?>" style="padding: 0.5rem;"></label>
        </div>
        
        <div id="job-fields" style="display: <?= $displayJob ?>; background: rgba(234, 179, 8, 0.1); border: 1px solid rgba(234, 179, 8, 0.3); padding: 1rem; border-radius: 8px; margin-bottom: 1.5rem;">
            <h3 style="margin-top:0; color: #ca8a04;">💼 Job Details</h3>
            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem;">
                <label>Min Salary <input type="number" name="salary_min" value="<?= $editListing['salary_min'] ?? '' ?>" style="width:100%; padding:0.5rem;"></label>
                <label>Max Salary <input type="number" name="salary_max" value="<?= $editListing['salary_max'] ?? '' ?>" style="width:100%; padding:0.5rem;"></label>
                <label>Vacancies <input type="number" name="vacancies" value="<?= $editListing['vacancies'] ?? 1 ?>" style="width:100%; padding:0.5rem;"></label>
                <label>Education <input type="text" name="education_required" value="<?= $editListing['education_required'] ?? '' ?>" style="width:100%; padding:0.5rem;"></label>
            </div>
        </div>
        
        <div id="business-fields" style="display: <?= $displayBusiness ?>; background: rgba(34, 197, 94, 0.1); border: 1px solid rgba(34, 197, 94, 0.3); padding: 1rem; border-radius: 8px; margin-bottom: 1.5rem;">
            <h3 style="margin-top:0; color: #16a34a;">🏢 Business Details</h3>
            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem;">
                <label>Business Name <input type="text" name="business_name" value="<?= $editListing['business_name'] ?? '' ?>" style="width:100%; padding:0.5rem;"></label>
                <label>Industry <input type="text" name="industry" value="<?= $editListing['industry'] ?? '' ?>" style="width:100%; padding:0.5rem;"></label>
            </div>
        </div>

        <div style="margin-bottom: 1.5rem; background: rgba(59, 130, 246, 0.1); border: 1px solid rgba(59, 130, 246, 0.2); padding: 1rem; border-radius: 8px;">
            <h3 style="margin-top: 0; color: #1e40af;">📷 Images</h3>
            <div style="display: flex; gap: 2rem; align-items: start; flex-wrap: wrap;">
                <div>
                    <strong>Main Image</strong><br>
                    <?php if (!empty($editListing['main_image_url'])): ?>
                    <img src="<?= $editListing['main_image_url'] ?>" style="height: 100px; border-radius: 8px; margin: 0.5rem 0;">
                    <?php endif; ?>
                    <input type="file" name="main_image">
                    <div style="margin-top: 5px;">Or URL: <input type="text" name="main_image_url" placeholder="https://..." style="padding: 0.3rem;"></div>
                </div>
                
                <?php if (!$editListing): ?>
                <div>
                    <strong>Gallery Images</strong><br>
                    <input type="file" name="gallery_images[]" multiple style="margin-top: 0.5rem;">
                </div>
                <?php endif; ?>
            </div>
        </div>
        
        <div style="margin-bottom: 1.5rem; display: flex; gap: 2rem;">
            <label><input type="checkbox" name="is_verified" <?= !empty($editListing['is_verified']) ? 'checked' : '' ?>> Verified</label>
            <label><input type="checkbox" name="is_featured" <?= !empty($editListing['is_featured']) ? 'checked' : '' ?>> Featured</label>
        </div>
        
        <button type="submit" class="btn btn-primary"><?= $editListing ? 'Update Listing' : 'Create Listing' ?></button>
    </form>
</div>

<?php if ($editListing): ?>
<div class="card" style="margin-top: 1rem;">
    <?php if (!empty($_GET['msg'])): ?>
    <div style="background: #dcfce7; border: 1px solid #22c55e; color: #166534; padding: 0.75rem 1rem; border-radius: 8px; margin-bottom: 1rem;">
        ✅ <?= htmlspecialchars($_GET['msg']) ?>
    </div>
    <?php endif; ?>
    
    <h3 style="margin-top: 0;">🖼️ Gallery Images (<?= count($editGallery) ?>)</h3>
    
    <!-- Existing Gallery Images -->
    <?php if (count($editGallery) > 0): ?>
    <div style="display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 1.5rem; padding: 1rem; background: rgba(59, 130, 246, 0.1); border-radius: 8px;">
        <?php foreach ($editGallery as $img): ?>
        <div style="position: relative; border: 2px solid rgba(59, 130, 246, 0.3); border-radius: 8px; overflow: hidden;">
            <img src="<?= $img['image_url'] ?>" style="height: 100px; display: block;">
            <form method="POST" style="position: absolute; top: 2px; right: 2px;">
                <input type="hidden" name="action" value="delete_gallery_image">
                <input type="hidden" name="listing_id" value="<?= $editListing['listing_id'] ?>">
                <input type="hidden" name="image_id" value="<?= $img['image_id'] ?>">
                <button type="submit" style="background: #ef4444; color: white; border: none; cursor: pointer; width: 24px; height: 24px; border-radius: 50%; font-weight: bold;">×</button>
            </form>
        </div>
        <?php endforeach; ?>
    </div>
    <?php else: ?>
    <p style="color: #888; margin-bottom: 1rem;">No gallery images yet.</p>
    <?php endif; ?>
    
    <!-- Add New Images -->
    <form method="POST" enctype="multipart/form-data" style="background: rgba(34, 197, 94, 0.1); border: 1px solid rgba(34, 197, 94, 0.3); padding: 1rem; border-radius: 8px;">
        <input type="hidden" name="action" value="add_gallery_image">
        <input type="hidden" name="listing_id" value="<?= $editListing['listing_id'] ?>">
        
        <div style="margin-bottom: 1rem;">
            <strong style="color: #16a34a;">📤 Upload Images (Auto-compressed to WebP)</strong><br>
            <input type="file" name="gallery_files[]" multiple accept="image/*" style="margin-top: 0.5rem;">
            <small style="color: #666; display: block; margin-top: 0.25rem;">Select multiple images. Max 10MB each.</small>
        </div>
        
        <div style="margin-bottom: 1rem;">
            <strong>Or Add by URL:</strong><br>
            <input type="text" name="image_url" placeholder="https://example.com/image.jpg" style="padding: 0.5rem; width: 100%; max-width: 400px; margin-top: 0.5rem;">
        </div>
        
        <button type="submit" class="btn btn-primary">➕ Add Images</button>
    </form>
</div>
<?php endif; ?>

<script>
// Category data from PHP
const allCategories = <?= json_encode(array_map(function($cat) {
    return [
        'id' => $cat['category_id'],
        'name' => $cat['name'],
        'listing_type' => $cat['listing_type']
    ];
}, $categories)) ?>;

const allSubcategories = <?= json_encode(array_map(function($sub) {
    return [
        'id' => $sub['category_id'],
        'name' => $sub['name'],
        'parent_id' => $sub['parent_id']
    ];
}, $subcategories)) ?>;

// Current edit values (for restoring on page load)
const editCategoryId = <?= json_encode($editListing['category_id'] ?? null) ?>;
const editSubcategoryId = <?= json_encode($editListing['subcategory_id'] ?? null) ?>;
const editListingType = <?= json_encode($editListing['listing_type'] ?? null) ?>;

function toggleFields(type) {
    document.getElementById('service-fields').style.display = type === 'services' ? 'block' : 'none';
    document.getElementById('job-fields').style.display = type === 'jobs' ? 'block' : 'none';
    document.getElementById('business-fields').style.display = type === 'business' ? 'block' : 'none';
    document.getElementById('price-field').style.display = type === 'business' ? 'none' : 'block';
    
    // Filter categories by listing type
    filterCategoriesByType(type);
}

function filterCategoriesByType(listingType) {
    const categorySelect = document.getElementById('category-select');
    const categorySearch = document.getElementById('category-search');
    
    if (!categorySelect) return;
    
    // Clear search input
    if (categorySearch) categorySearch.value = '';
    
    // Clear and repopulate categories
    categorySelect.innerHTML = '<option value="">Select Category</option>';
    
    const filteredCategories = listingType 
        ? allCategories.filter(cat => cat.listing_type === listingType)
        : allCategories;
    
    filteredCategories.forEach(cat => {
        const option = document.createElement('option');
        option.value = cat.id;
        option.text = cat.name;
        if (cat.id == editCategoryId) {
            option.selected = true;
        }
        categorySelect.appendChild(option);
    });
    
    // Reset subcategories
    filterSubcategoriesByParent(categorySelect.value);
    
    // Update the searchable select's stored options
    updateSearchableOptions('category-search', 'category-select');
}

function filterSubcategoriesByParent(parentId) {
    const subcategorySelect = document.getElementById('subcategory-select');
    const subcategorySearch = document.getElementById('subcategory-search');
    
    if (!subcategorySelect) return;
    
    // Clear search input
    if (subcategorySearch) subcategorySearch.value = '';
    
    // Clear and repopulate subcategories
    subcategorySelect.innerHTML = '<option value="">None</option>';
    
    if (!parentId) return;
    
    const filteredSubs = allSubcategories.filter(sub => sub.parent_id == parentId);
    
    filteredSubs.forEach(sub => {
        const option = document.createElement('option');
        option.value = sub.id;
        option.text = sub.name;
        if (sub.id == editSubcategoryId) {
            option.selected = true;
        }
        subcategorySelect.appendChild(option);
    });
    
    // Update the searchable select's stored options
    updateSearchableOptions('subcategory-search', 'subcategory-select');
}

// Store for searchable selects
const searchableStores = {};

function updateSearchableOptions(searchInputId, selectId) {
    const select = document.getElementById(selectId);
    if (!select) return;
    
    searchableStores[selectId] = Array.from(select.options).map(opt => ({
        value: opt.value,
        text: opt.text,
        selected: opt.selected
    }));
}

function setupSearchableSelect(searchInputId, selectId) {
    const searchInput = document.getElementById(searchInputId);
    const select = document.getElementById(selectId);
    
    if (!searchInput || !select) return;
    
    // Store initial options
    updateSearchableOptions(searchInputId, selectId);
    
    searchInput.addEventListener('input', function() {
        const filter = this.value.toLowerCase().trim();
        const currentValue = select.value;
        const originalOptions = searchableStores[selectId] || [];
        
        // Clear select
        select.innerHTML = '';
        
        // Filter and re-add matching options
        originalOptions.forEach(opt => {
            if (opt.text.toLowerCase().includes(filter) || opt.value === '') {
                const option = document.createElement('option');
                option.value = opt.value;
                option.text = opt.text;
                if (opt.value === currentValue) {
                    option.selected = true;
                }
                select.appendChild(option);
            }
        });
        
        // If only one non-empty option matches, auto-select it
        const nonEmptyOptions = Array.from(select.options).filter(o => o.value !== '');
        if (nonEmptyOptions.length === 1 && filter.length > 0) {
            select.value = nonEmptyOptions[0].value;
        }
    });
    
    // Show selected item in search and trigger subcategory filter for category
    select.addEventListener('change', function() {
        const selectedOption = select.options[select.selectedIndex];
        if (selectedOption && selectedOption.value) {
            searchInput.value = selectedOption.text;
        }
        
        // If this is the category select, filter subcategories
        if (selectId === 'category-select') {
            filterSubcategoriesByParent(this.value);
        }
    });
    
    // Select text on focus
    searchInput.addEventListener('focus', function() {
        if (this.value === select.options[select.selectedIndex]?.text) {
            this.select();
        }
    });
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Add change listener to category dropdown to filter subcategories
    const categorySelect = document.getElementById('category-select');
    if (categorySelect) {
        categorySelect.addEventListener('change', function() {
            filterSubcategoriesByParent(this.value);
        });
    }
    
    // If editing, set initial filters based on listing type
    if (editListingType) {
        filterCategoriesByType(editListingType);
        if (editCategoryId) {
            document.getElementById('category-select').value = editCategoryId;
            filterSubcategoriesByParent(editCategoryId);
            if (editSubcategoryId) {
                document.getElementById('subcategory-select').value = editSubcategoryId;
            }
        }
    }
    
    // Add listener to listing type dropdown (for new listings)
    const listingTypeSelect = document.querySelector('select[name="listing_type"]');
    if (listingTypeSelect) {
        listingTypeSelect.addEventListener('change', function() {
            filterCategoriesByType(this.value);
        });
    }
    
    // ========== AJAX FORM SUBMISSION ==========
    initAjaxForms();
});

// Message display helper
function showMessage(container, message, isSuccess) {
    // Remove existing messages
    const existing = container.querySelectorAll('.ajax-message');
    existing.forEach(el => el.remove());
    
    const div = document.createElement('div');
    div.className = 'ajax-message';
    div.style.cssText = isSuccess 
        ? 'background: #dcfce7; border: 1px solid #22c55e; color: #166534; padding: 1rem; border-radius: 8px; margin-bottom: 1rem;'
        : 'background: #fee2e2; border: 1px solid #ef4444; color: #991b1b; padding: 1rem; border-radius: 8px; margin-bottom: 1rem;';
    div.innerHTML = (isSuccess ? '✅ ' : '⚠️ ') + message;
    
    // Insert at top of container
    container.insertBefore(div, container.firstChild);
    
    // Auto-hide after 5 seconds
    setTimeout(() => div.remove(), 5000);
}

// Initialize AJAX for all forms
function initAjaxForms() {
    // Main listing form
    const mainForm = document.querySelector('form[enctype="multipart/form-data"]');
    if (mainForm && mainForm.querySelector('input[name="action"]')) {
        mainForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const submitBtn = this.querySelector('button[type="submit"]');
            const originalText = submitBtn.textContent;
            submitBtn.disabled = true;
            submitBtn.textContent = 'Saving...';
            
            try {
                const formData = new FormData(this);
                const response = await fetch(window.location.href, {
                    method: 'POST',
                    body: formData,
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                });
                
                const result = await response.json();
                const container = this.closest('.card');
                
                if (result.success) {
                    showMessage(container, result.message, true);
                    // If new listing, redirect to edit page
                    if (result.listing_id && !document.querySelector('input[name="listing_id"]')) {
                        setTimeout(() => {
                            window.location.href = 'index.php?page=listing_form&id=' + result.listing_id + '&msg=' + encodeURIComponent(result.message);
                        }, 1000);
                    }
                } else {
                    showMessage(container, result.error || 'An error occurred', false);
                }
            } catch (err) {
                showMessage(mainForm.closest('.card'), 'Network error: ' + err.message, false);
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = originalText;
            }
        });
    }
    
    // Gallery add form (with file uploads)
    const galleryAddForm = document.querySelector('form[enctype="multipart/form-data"] input[name="action"][value="add_gallery_image"]');
    if (galleryAddForm) {
        const form = galleryAddForm.closest('form');
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const submitBtn = this.querySelector('button[type="submit"]');
            const originalText = submitBtn.textContent;
            submitBtn.disabled = true;
            submitBtn.textContent = 'Uploading...';
            
            try {
                const formData = new FormData(this);
                const response = await fetch(window.location.href, {
                    method: 'POST',
                    body: formData,
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                });
                
                const result = await response.json();
                const container = this.closest('.card');
                
                if (result.success) {
                    showMessage(container, result.message, true);
                    // Reload page to show new images
                    setTimeout(() => window.location.reload(), 1500);
                } else {
                    showMessage(container, result.error || 'Upload failed', false);
                }
            } catch (err) {
                showMessage(this.closest('.card'), 'Network error: ' + err.message, false);
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = originalText;
            }
        });
    }
    
    // Gallery delete forms
    document.querySelectorAll('form input[name="action"][value="delete_gallery_image"]').forEach(input => {
        const form = input.closest('form');
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            if (!confirm('Delete this image?')) return;
            
            const btn = this.querySelector('button[type="submit"]');
            btn.disabled = true;
            
            try {
                const formData = new FormData(this);
                const response = await fetch(window.location.href, {
                    method: 'POST',
                    body: formData,
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                });
                
                const result = await response.json();
                
                if (result.success) {
                    // Remove the image element
                    this.closest('div[style*="position: relative"]').remove();
                    // Update count in header
                    const header = document.querySelector('h3');
                    if (header && header.textContent.includes('Gallery')) {
                        const count = document.querySelectorAll('form input[name="action"][value="delete_gallery_image"]').length;
                        header.textContent = '🖼️ Gallery Images (' + count + ')';
                    }
                } else {
                    alert(result.error || 'Delete failed');
                }
            } catch (err) {
                alert('Network error: ' + err.message);
            } finally {
                btn.disabled = false;
            }
        });
    });
}
</script>
