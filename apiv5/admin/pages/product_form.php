<?php
// Fetch Common Data
// Shops (Business Listings)
$shops = $db->query("SELECT listing_id, title, business_listings.business_name 
                     FROM listings 
                     LEFT JOIN business_listings USING(listing_id) 
                     WHERE listing_type = 'business' 
                     ORDER BY title")->fetchAll();

// Shop Product Categories (from shop_categories table)
$categories = $db->query("SELECT * FROM shop_categories WHERE parent_id IS NULL AND is_active = 1 ORDER BY sort_order, name")->fetchAll();
$allSubcategories = $db->query("SELECT * FROM shop_categories WHERE parent_id IS NOT NULL AND is_active = 1 ORDER BY name")->fetchAll();

$editProduct = null;

// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'add_product':
                    $product_name = $_POST['product_name'];
                    $description = $_POST['description'] ?? null;
                    $listing_id = $_POST['listing_id'];
                    $category_id = $_POST['category_id'];
                    $subcategory_id = !empty($_POST['subcategory_id']) ? $_POST['subcategory_id'] : null;
                    $price = $_POST['price'];
                    $discounted_price = !empty($_POST['discounted_price']) ? $_POST['discounted_price'] : null;
                    $stock_qty = !empty($_POST['stock_qty']) ? $_POST['stock_qty'] : 1;
                    $sell_online = isset($_POST['sell_online']) ? 1 : 0;
                    $is_active = isset($_POST['is_active']) ? 1 : 0;
                    
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
                    
                    $stmt = $db->prepare("INSERT INTO shop_products (listing_id, product_name, description, shop_category_id, subcategory_id, price, discounted_price, image_url, sell_online, stock_qty, is_active, sort_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    $stmt->execute([$listing_id, $product_name, $description, $category_id, $subcategory_id, $price, $discounted_price, $image_url, $sell_online, $stock_qty, $is_active, $sortOrder]);
                    
                    $newId = $db->lastInsertId();
                    header("Location: index.php?page=product_form&id=$newId&msg=" . urlencode("Product added successfully!"));
                    exit;

                case 'edit_product':
                    $product_id = $_POST['product_id'];
                    $product_name = $_POST['product_name'];
                    $description = $_POST['description'] ?? null;
                    $listing_id = $_POST['listing_id'];
                    $category_id = $_POST['category_id'];
                    $subcategory_id = !empty($_POST['subcategory_id']) ? $_POST['subcategory_id'] : null;
                    $price = $_POST['price'];
                    $discounted_price = !empty($_POST['discounted_price']) ? $_POST['discounted_price'] : null;
                    $stock_qty = !empty($_POST['stock_qty']) ? $_POST['stock_qty'] : 1;
                    $sell_online = isset($_POST['sell_online']) ? 1 : 0;
                    $is_active = isset($_POST['is_active']) ? 1 : 0;
                    
                    // Handle image upload
                    $image_url = $_POST['existing_image'] ?? null;
                    if (!empty($_POST['image_url'])) {
                        $image_url = $_POST['image_url'];
                    } elseif (isset($_FILES['product_image']) && $_FILES['product_image']['error'] === UPLOAD_ERR_OK) {
                        $image_url = uploadImage($_FILES['product_image'], 'products');
                    }
                    
                    $stmt = $db->prepare("UPDATE shop_products SET listing_id = ?, product_name = ?, description = ?, shop_category_id = ?, subcategory_id = ?, price = ?, discounted_price = ?, stock_qty = ?, sell_online = ?, is_active = ?, image_url = ? WHERE product_id = ?");
                    $stmt->execute([$listing_id, $product_name, $description, $category_id, $subcategory_id, $price, $discounted_price, $stock_qty, $sell_online, $is_active, $image_url, $product_id]);
                    
                    header("Location: index.php?page=product_form&id=$product_id&msg=" . urlencode("Product updated successfully!"));
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
    $stmt = $db->prepare("SELECT * FROM shop_products WHERE product_id = ?");
    $stmt->execute([$id]);
    $editProduct = $stmt->fetch();
}
?>

<div class="header">
    <div class="page-title"><?= $editProduct ? 'Edit Product' : 'Add New Product' ?></div>
    <a href="index.php?page=products" class="btn btn-outline">← Back to Products</a>
</div>

<div class="card">
    <form method="POST" enctype="multipart/form-data">
        <input type="hidden" name="action" value="<?= $editProduct ? 'edit_product' : 'add_product' ?>">
        <?php if ($editProduct): ?>
        <input type="hidden" name="product_id" value="<?= $editProduct['product_id'] ?>">
        <input type="hidden" name="existing_image" value="<?= $editProduct['image_url'] ?>">
        <?php endif; ?>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; margin-bottom: 1.5rem;">
            <label>
                <strong>Select Shop (Listing) *</strong>
                <select name="listing_id" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="">Select Shop</option>
                    <?php foreach ($shops as $s): ?>
                    <option value="<?= $s['listing_id'] ?>" <?= ($editProduct['listing_id'] ?? '') == $s['listing_id'] ? 'selected' : '' ?>>
                        <?= htmlspecialchars($s['business_name'] ?: $s['title']) ?> (ID: <?= $s['listing_id'] ?>)
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
                <select name="category_id" id="categorySelect" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;" onchange="updateSubcategories()">
                    <option value="">Select Category</option>
                    <?php foreach ($categories as $cat): ?>
                    <option value="<?= $cat['id'] ?>" <?= ($editProduct['shop_category_id'] ?? '') == $cat['id'] ? 'selected' : '' ?>>
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
                <strong>Discounted Price (₹)</strong>
                <input type="number" name="discounted_price" value="<?= $editProduct['discounted_price'] ?? '' ?>" step="0.01" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>

            <label>
                <strong>Stock Quantity</strong>
                <input type="number" name="stock_qty" value="<?= $editProduct['stock_qty'] ?? 1 ?>" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
        </div>
        
        <label style="display: block; margin-bottom: 1.5rem;">
            <strong>Description</strong>
            <textarea name="description" rows="4" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;"><?= $editProduct['description'] ?? '' ?></textarea>
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
            <label><input type="checkbox" name="sell_online" <?= !empty($editProduct['sell_online']) ? 'checked' : '' ?>> Sell Online</label>
            <label><input type="checkbox" name="is_active" <?= (!isset($editProduct) || $editProduct['is_active']) ? 'checked' : '' ?>> Active</label>
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
