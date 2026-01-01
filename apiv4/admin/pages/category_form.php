<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'add_category':
                    $name = $_POST['name'];
                    $slug = strtolower(trim(preg_replace('/[^A-Za-z0-9-]+/', '-', $name)));
                    $parent_id = !empty($_POST['parent_id']) ? $_POST['parent_id'] : null;
                    $listing_type = $_POST['listing_type'] ?? null;
                    
                    // If subcategory, inherit type from parent if not set
                    if ($parent_id && !$listing_type) {
                        $p = $db->query("SELECT listing_type FROM categories WHERE category_id = $parent_id")->fetch();
                        $listing_type = $p['listing_type'];
                    }
                    
                    $image_url = null;
                    if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
                        $image_url = uploadImage($_FILES['image'], 'categories');
                    } elseif (!empty($_POST['image_url'])) {
                        $image_url = $_POST['image_url'];
                    }
                    
                    $stmt = $db->prepare("INSERT INTO categories (name, slug, parent_id, listing_type, image_url) VALUES (?, ?, ?, ?, ?)");
                    $stmt->execute([$name, $slug, $parent_id, $listing_type, $image_url]);
                    
                    header("Location: index.php?page=categories&msg=" . urlencode("Category added!"));
                    exit;

                case 'edit_category':
                    $id = $_POST['category_id'];
                    $name = $_POST['name'];
                    $slug = strtolower(trim(preg_replace('/[^A-Za-z0-9-]+/', '-', $name)));
                    $parent_id = !empty($_POST['parent_id']) ? $_POST['parent_id'] : null;
                    $listing_type = $_POST['listing_type'] ?? null;
                    
                    $image_url = $_POST['existing_image'];
                    if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
                        $image_url = uploadImage($_FILES['image'], 'categories');
                    } elseif (!empty($_POST['image_url'])) {
                        $image_url = $_POST['image_url'];
                    }
                    
                    $stmt = $db->prepare("UPDATE categories SET name = ?, slug = ?, parent_id = ?, listing_type = ?, image_url = ? WHERE category_id = ?");
                    $stmt->execute([$name, $slug, $parent_id, $listing_type, $image_url, $id]);
                    
                    header("Location: index.php?page=categories&msg=" . urlencode("Category updated!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

$editCat = null;
if (isset($_GET['id'])) {
    $stmt = $db->prepare("SELECT * FROM categories WHERE category_id = ?");
    $stmt->execute([$_GET['id']]);
    $editCat = $stmt->fetch();
}

// Fetch potential parents (Roots only)
$parents = $db->query("SELECT * FROM categories WHERE parent_id IS NULL ORDER BY name")->fetchAll();
?>

<div class="header">
    <div class="page-title"><?= $editCat ? 'Edit Category' : 'Add New Category' ?></div>
    <a href="index.php?page=categories" class="btn btn-outline">← Back to Categories</a>
</div>

<div class="card">
    <form method="POST" enctype="multipart/form-data">
        <input type="hidden" name="action" value="<?= $editCat ? 'edit_category' : 'add_category' ?>">
        <?php if ($editCat): ?>
        <input type="hidden" name="category_id" value="<?= $editCat['category_id'] ?>">
        <input type="hidden" name="existing_image" value="<?= $editCat['image_url'] ?>">
        <?php endif; ?>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; margin-bottom: 1.5rem;">
            <label>
                <strong>Name *</strong>
                <input type="text" name="name" value="<?= $editCat['name'] ?? '' ?>" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            
            <label>
                <strong>Parent Category</strong>
                <select name="parent_id" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="">None (Root Category)</option>
                    <?php foreach ($parents as $p): ?>
                    <?php if (($editCat['category_id'] ?? 0) != $p['category_id']): ?>
                    <option value="<?= $p['category_id'] ?>" <?= ($editCat['parent_id'] ?? '') == $p['category_id'] ? 'selected' : '' ?>>
                        <?= htmlspecialchars($p['name']) ?>
                    </option>
                    <?php endif; ?>
                    <?php endforeach; ?>
                </select>
            </label>
            
            <label>
                <strong>Listing Type</strong>
                <select name="listing_type" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="">None/Inherit</option>
                    <option value="services" <?= ($editCat['listing_type'] ?? '') === 'services' ? 'selected' : '' ?>>Services</option>
                    <option value="selling" <?= ($editCat['listing_type'] ?? '') === 'selling' ? 'selected' : '' ?>>Selling</option>
                    <option value="business" <?= ($editCat['listing_type'] ?? '') === 'business' ? 'selected' : '' ?>>Business</option>
                    <option value="jobs" <?= ($editCat['listing_type'] ?? '') === 'jobs' ? 'selected' : '' ?>>Jobs</option>
                </select>
            </label>
        </div>
        
        <div style="margin-bottom: 1.5rem; background: #f8fafc; padding: 1rem; border-radius: 8px;">
            <h3 style="margin-top: 0;">📷 Category Icon/Image</h3>
            <?php if (!empty($editCat['image_url'])): ?>
            <img src="<?= $editCat['image_url'] ?>" style="height: 60px; border-radius: 8px; margin: 0.5rem 0;">
            <?php endif; ?>
            <input type="file" name="image">
            <div style="margin-top: 5px;">Or URL: <input type="text" name="image_url" placeholder="https://..." style="padding: 0.3rem;"></div>
        </div>
        
        <button type="submit" class="btn btn-primary"><?= $editCat ? 'Update Category' : 'Create Category' ?></button>
    </form>
</div>
