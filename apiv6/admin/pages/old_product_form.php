<?php
// Fetch Common Data
// Users (for seller selection)
$users = $db->query("SELECT phone, name FROM users ORDER BY name")->fetchAll();

// Old Product Categories (parent categories only)
$categories = $db->query("SELECT * FROM old_categories WHERE parent_id IS NULL ORDER BY sort_order, name")->fetchAll();

// All subcategories for dynamic filtering
$allSubcategories = $db->query("SELECT * FROM old_categories WHERE parent_id IS NOT NULL ORDER BY name")->fetchAll();

$editProduct = null;

// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'add_product':
                    $user_id = $_POST['user_id'];
                    $product_name = $_POST['product_name'];
                    $description = $_POST['description'] ?? null;
                    $old_category_id = $_POST['old_category_id'];
                    $subcategory_id = !empty($_POST['subcategory_id']) ? $_POST['subcategory_id'] : null;
                    $price = $_POST['price'];
                    $original_price = !empty($_POST['original_price']) ? $_POST['original_price'] : null;
                    $condition = $_POST['condition'] ?? 'good';
                    $brand = !empty($_POST['brand']) ? $_POST['brand'] : null;
                    $model = !empty($_POST['model']) ? $_POST['model'] : null;
                    $status = $_POST['status'] ?? 'active';
                    $show_phone = isset($_POST['show_phone']) ? 1 : 0;
                    $accept_offers = isset($_POST['accept_offers']) ? 1 : 0;
                    
                    // Handle image upload
                    $image_url = null;
                    if (!empty($_POST['image_url'])) {
                        $image_url = $_POST['image_url'];
                    } elseif (isset($_FILES['product_image']) && $_FILES['product_image']['error'] === UPLOAD_ERR_OK) {
                        $image_url = uploadImage($_FILES['product_image'], 'old_products');
                    }
                    
                    $stmt = $db->prepare("INSERT INTO old_products (user_id, product_name, description, old_category_id, subcategory_id, price, original_price, `condition`, brand, model, image_url, status, show_phone, accept_offers, city) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Hingoli')");
                    $stmt->execute([$user_id, $product_name, $description, $old_category_id, $subcategory_id, $price, $original_price, $condition, $brand, $model, $image_url, $status, $show_phone, $accept_offers]);
                    
                    $newId = $db->lastInsertId();
                    
                    // Update category product count
                    $db->prepare("UPDATE old_categories SET product_count = product_count + 1 WHERE id = ?")->execute([$old_category_id]);
                    
                    header("Location: index.php?page=old_product_form&id=$newId&msg=" . urlencode("Product added successfully!"));
                    exit;

                case 'edit_product':
                    $product_id = $_POST['product_id'];
                    $user_id = $_POST['user_id'];
                    $product_name = $_POST['product_name'];
                    $description = $_POST['description'] ?? null;
                    $old_category_id = $_POST['old_category_id'];
                    $subcategory_id = !empty($_POST['subcategory_id']) ? $_POST['subcategory_id'] : null;
                    $price = $_POST['price'];
                    $original_price = !empty($_POST['original_price']) ? $_POST['original_price'] : null;
                    $condition = $_POST['condition'] ?? 'good';
                    $brand = !empty($_POST['brand']) ? $_POST['brand'] : null;
                    $model = !empty($_POST['model']) ? $_POST['model'] : null;
                    $status = $_POST['status'] ?? 'active';
                    $show_phone = isset($_POST['show_phone']) ? 1 : 0;
                    $accept_offers = isset($_POST['accept_offers']) ? 1 : 0;
                    
                    // Handle image upload
                    $image_url = $_POST['existing_image'] ?? null;
                    if (!empty($_POST['image_url'])) {
                        $image_url = $_POST['image_url'];
                    } elseif (isset($_FILES['product_image']) && $_FILES['product_image']['error'] === UPLOAD_ERR_OK) {
                        $image_url = uploadImage($_FILES['product_image'], 'old_products');
                    }
                    
                    $stmt = $db->prepare("UPDATE old_products SET user_id = ?, product_name = ?, description = ?, old_category_id = ?, subcategory_id = ?, price = ?, original_price = ?, `condition` = ?, brand = ?, model = ?, image_url = ?, status = ?, show_phone = ?, accept_offers = ? WHERE product_id = ?");
                    $stmt->execute([$user_id, $product_name, $description, $old_category_id, $subcategory_id, $price, $original_price, $condition, $brand, $model, $image_url, $status, $show_phone, $accept_offers, $product_id]);
                    
                    header("Location: index.php?page=old_product_form&id=$product_id&msg=" . urlencode("Product updated successfully!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

// Fetch Existing Data if Editing
if (isset($_GET['id'])) {
    $id = $_GET['id'];
    $stmt = $db->prepare("SELECT * FROM old_products WHERE product_id = ?");
    $stmt->execute([$id]);
    $editProduct = $stmt->fetch();
}
?>

<div class="header">
    <div class="page-title"><?= $editProduct ? 'Edit Old Product' : 'Add New Old Product' ?></div>
    <a href="index.php?page=old_products" class="btn btn-outline">← Back to Old Products</a>
</div>

<?php if (isset($error)): ?>
<div class="alert alert-error">⚠️ <?= htmlspecialchars($error) ?></div>
<?php endif; ?>

<div class="card">
    <form method="POST" enctype="multipart/form-data">
        <input type="hidden" name="action" value="<?= $editProduct ? 'edit_product' : 'add_product' ?>">
        <?php if ($editProduct): ?>
        <input type="hidden" name="product_id" value="<?= $editProduct['product_id'] ?>">
        <input type="hidden" name="existing_image" value="<?= $editProduct['image_url'] ?>">
        <?php endif; ?>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; margin-bottom: 1.5rem;">
            <label>
                <strong>Seller (User) *</strong>
                <select name="user_id" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="">Select Seller</option>
                    <?php foreach ($users as $u): ?>
                    <option value="<?= $u['phone'] ?>" <?= ($editProduct['user_id'] ?? '') == $u['phone'] ? 'selected' : '' ?>>
                        <?= htmlspecialchars($u['name'] ?: 'User') ?> (<?= $u['phone'] ?>)
                    </option>
                    <?php endforeach; ?>
                </select>
            </label>
            
            <label>
                <strong>Product Name *</strong>
                <input type="text" name="product_name" value="<?= $editProduct['product_name'] ?? '' ?>" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            
            <label>
                <strong>Category *</strong>
                <select name="old_category_id" id="categorySelect" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;" onchange="updateSubcategories()">
                    <option value="">Select Category</option>
                    <?php foreach ($categories as $cat): ?>
                    <option value="<?= $cat['id'] ?>" <?= ($editProduct['old_category_id'] ?? '') == $cat['id'] ? 'selected' : '' ?>>
                        <?= htmlspecialchars($cat['name']) ?>
                    </option>
                    <?php endforeach; ?>
                </select>
            </label>
            
            <label>
                <strong>Subcategory</strong>
                <select name="subcategory_id" id="subcategorySelect" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="">None</option>
                    <?php foreach ($allSubcategories as $sub): ?>
                    <option value="<?= $sub['id'] ?>" data-parent="<?= $sub['parent_id'] ?>" <?= ($editProduct['subcategory_id'] ?? '') == $sub['id'] ? 'selected' : '' ?> style="display: none;">
                        <?= htmlspecialchars($sub['name']) ?>
                    </option>
                    <?php endforeach; ?>
                </select>
            </label>
            
            <label>
                <strong>Price (₹) *</strong>
                <input type="number" name="price" value="<?= $editProduct['price'] ?? '' ?>" required step="0.01" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            
            <label>
                <strong>Original Price (₹)</strong>
                <input type="number" name="original_price" value="<?= $editProduct['original_price'] ?? '' ?>" step="0.01" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;" placeholder="For showing discount">
            </label>

            <label>
                <strong>Condition *</strong>
                <select name="condition" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="like_new" <?= ($editProduct['condition'] ?? 'good') === 'like_new' ? 'selected' : '' ?>>Like New</option>
                    <option value="good" <?= ($editProduct['condition'] ?? 'good') === 'good' ? 'selected' : '' ?>>Good</option>
                    <option value="fair" <?= ($editProduct['condition'] ?? '') === 'fair' ? 'selected' : '' ?>>Fair</option>
                    <option value="poor" <?= ($editProduct['condition'] ?? '') === 'poor' ? 'selected' : '' ?>>Poor</option>
                </select>
            </label>

            <label>
                <strong>Status</strong>
                <select name="status" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="active" <?= ($editProduct['status'] ?? 'active') === 'active' ? 'selected' : '' ?>>Active</option>
                    <option value="sold" <?= ($editProduct['status'] ?? '') === 'sold' ? 'selected' : '' ?>>Sold</option>
                    <option value="expired" <?= ($editProduct['status'] ?? '') === 'expired' ? 'selected' : '' ?>>Expired</option>
                </select>
            </label>

            <label>
                <strong>Brand</strong>
                <input type="text" name="brand" value="<?= $editProduct['brand'] ?? '' ?>" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;" placeholder="e.g., Samsung, Apple">
            </label>

            <label>
                <strong>Model</strong>
                <input type="text" name="model" value="<?= $editProduct['model'] ?? '' ?>" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;" placeholder="e.g., Galaxy S21, iPhone 12">
            </label>
        </div>
        
        <label style="display: block; margin-bottom: 1.5rem;">
            <strong>Description</strong>
            <textarea name="description" rows="4" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;" placeholder="Describe the product condition, features, reason for selling..."><?= $editProduct['description'] ?? '' ?></textarea>
        </label>
        
        <div style="margin-bottom: 1.5rem; background: #f8fafc; padding: 1rem; border-radius: 8px;">
            <h3 style="margin-top: 0;">📷 Product Image</h3>
            <div style="display: flex; gap: 2rem; align-items: start;">
                <div>
                    <?php if (!empty($editProduct['image_url'])): ?>
                    <img src="<?= $editProduct['image_url'] ?>" style="height: 100px; border-radius: 8px; margin: 0.5rem 0;">
                    <?php endif; ?>
                    <input type="file" name="product_image">
                    <div style="margin-top: 5px;">Or URL: <input type="text" name="image_url" placeholder="https://..." style="padding: 0.3rem;"></div>
                </div>
            </div>
        </div>
        
        <div style="margin-bottom: 1.5rem; display: flex; gap: 2rem;">
            <label><input type="checkbox" name="show_phone" <?= (!isset($editProduct) || $editProduct['show_phone']) ? 'checked' : '' ?>> Show Phone Number</label>
            <label><input type="checkbox" name="accept_offers" <?= (!isset($editProduct) || $editProduct['accept_offers']) ? 'checked' : '' ?>> Accept Offers</label>
        </div>
        
        <button type="submit" class="btn btn-primary"><?= $editProduct ? 'Update Product' : 'Create Product' ?></button>
    </form>
</div>

<script>
function updateSubcategories() {
    const categoryId = document.getElementById('categorySelect').value;
    const subcategorySelect = document.getElementById('subcategorySelect');
    const options = subcategorySelect.querySelectorAll('option[data-parent]');
    
    // Hide all subcategory options
    options.forEach(opt => {
        opt.style.display = 'none';
        if (opt.getAttribute('data-parent') === categoryId) {
            opt.style.display = 'block';
        }
    });
    
    // Reset selection if current selection is hidden
    const currentOption = subcategorySelect.options[subcategorySelect.selectedIndex];
    if (currentOption && currentOption.style.display === 'none') {
        subcategorySelect.value = '';
    }
}

// Initialize subcategories on page load
document.addEventListener('DOMContentLoaded', updateSubcategories);
</script>
