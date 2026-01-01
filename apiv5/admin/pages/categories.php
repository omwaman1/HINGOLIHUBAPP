<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $action = $_POST['action'] ?? '';
        
        switch ($action) {
            case 'add_category':
                $name = trim($_POST['name']);
                $slug = strtolower(trim(preg_replace('/[^A-Za-z0-9-]+/', '-', $name)));
                $parent_id = !empty($_POST['parent_id']) ? $_POST['parent_id'] : null;
                $listing_type = !empty($_POST['listing_type']) ? $_POST['listing_type'] : null;
                $is_shop = isset($_POST['is_shop']) ? 1 : 0;
                
                // If subcategory, inherit type from parent
                if ($parent_id && !$listing_type) {
                    $parentType = $db->query("SELECT listing_type FROM categories WHERE category_id = $parent_id")->fetchColumn();
                    $listing_type = $parentType;
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
                $name = trim($_POST['name']);
                $slug = strtolower(trim(preg_replace('/[^A-Za-z0-9-]+/', '-', $name)));
                $parent_id = !empty($_POST['parent_id']) ? $_POST['parent_id'] : null;
                $listing_type = !empty($_POST['listing_type']) ? $_POST['listing_type'] : null;
                
                $image_url = $_POST['existing_image'] ?? null;
                if (isset($_FILES['image']) && $_FILES['image']['error'] === UPLOAD_ERR_OK) {
                    $image_url = uploadImage($_FILES['image'], 'categories');
                } elseif (!empty($_POST['image_url'])) {
                    $image_url = $_POST['image_url'];
                }
                
                $stmt = $db->prepare("UPDATE categories SET name = ?, slug = ?, parent_id = ?, listing_type = ?, image_url = ? WHERE category_id = ?");
                $stmt->execute([$name, $slug, $parent_id, $listing_type, $image_url, $id]);
                header("Location: index.php?page=categories&msg=" . urlencode("Category updated!"));
                exit;
                
            case 'delete':
                $id = $_POST['category_id'];
                // Check for children
                $childCount = $db->query("SELECT COUNT(*) FROM categories WHERE parent_id = $id")->fetchColumn();
                if ($childCount > 0) {
                    header("Location: index.php?page=categories&error=" . urlencode("Cannot delete: has $childCount subcategories"));
                } else {
                    $db->prepare("DELETE FROM categories WHERE category_id = ?")->execute([$id]);
                    header("Location: index.php?page=categories&msg=" . urlencode("Deleted!"));
                }
                exit;
                
            case 'delete_shop_cat':
                $id = $_POST['shop_cat_id'];
                $db->prepare("DELETE FROM shop_categories WHERE category_id = ? OR id = ?")->execute([$id, $id]);
                header("Location: index.php?page=categories&msg=" . urlencode("Shop category deleted!"));
                exit;
                
            case 'delete_old_cat':
                $id = $_POST['old_cat_id'];
                $db->prepare("DELETE FROM old_categories WHERE category_id = ? OR id = ?")->execute([$id, $id]);
                header("Location: index.php?page=categories&msg=" . urlencode("Old category deleted!"));
                exit;
        }
    } catch (Exception $e) {
        $error = "Error: " . $e->getMessage();
    }
}

// Get Filters
$filterType = $_GET['type'] ?? '';
$filterLevel = $_GET['level'] ?? ''; // 'parent' or 'sub'
$filterParent = $_GET['parent'] ?? '';
$search = $_GET['search'] ?? '';

// Build Query
$sql = "SELECT c.*, p.name as parent_name, 
        (SELECT COUNT(*) FROM categories WHERE parent_id = c.category_id) as child_count,
        (SELECT COUNT(*) FROM listings WHERE category_id = c.category_id) as listing_count
        FROM categories c 
        LEFT JOIN categories p ON c.parent_id = p.category_id 
        WHERE 1=1";
$params = [];

if ($filterType) {
    $sql .= " AND c.listing_type = ?";
    $params[] = $filterType;
}
if ($filterLevel === 'parent') {
    $sql .= " AND c.parent_id IS NULL";
} elseif ($filterLevel === 'sub') {
    $sql .= " AND c.parent_id IS NOT NULL";
}
if ($filterParent) {
    $sql .= " AND c.parent_id = ?";
    $params[] = $filterParent;
}
if ($search) {
    $sql .= " AND c.name LIKE ?";
    $params[] = "%$search%";
}

$sql .= " ORDER BY CASE WHEN c.parent_id IS NULL THEN 0 ELSE 1 END, p.name, c.name";
$stmt = $db->prepare($sql);
$stmt->execute($params);
$categories = $stmt->fetchAll();

// Get parent categories for filter dropdown
$parentCats = $db->query("SELECT category_id, name, listing_type FROM categories WHERE parent_id IS NULL ORDER BY name")->fetchAll();

// Get listing types for filter
$types = $db->query("SELECT DISTINCT listing_type FROM categories WHERE listing_type IS NOT NULL")->fetchAll(PDO::FETCH_COLUMN);

// Edit mode
$editCat = null;
if (isset($_GET['edit'])) {
    $editCat = $db->query("SELECT * FROM categories WHERE category_id = " . intval($_GET['edit']))->fetch();
}
?>

<div class="header">
    <div class="page-title">📁 Categories Management</div>
</div>

<!-- Filters -->
<div class="card">
    <form method="GET" style="display: flex; gap: 10px; flex-wrap: wrap; align-items: end;">
        <input type="hidden" name="page" value="categories">
        
        <label style="min-width: 150px;">
            <small>Search</small>
            <input type="text" name="search" value="<?= htmlspecialchars($search) ?>" placeholder="Category name..." style="width: 100%; padding: 0.4rem;">
        </label>
        
        <label style="min-width: 120px;">
            <small>Type</small>
            <select name="type" style="width: 100%; padding: 0.4rem;">
                <option value="">All Types</option>
                <?php foreach ($types as $t): ?>
                <option value="<?= $t ?>" <?= $filterType === $t ? 'selected' : '' ?>><?= ucfirst($t) ?></option>
                <?php endforeach; ?>
            </select>
        </label>
        
        <label style="min-width: 120px;">
            <small>Level</small>
            <select name="level" style="width: 100%; padding: 0.4rem;">
                <option value="">All Levels</option>
                <option value="parent" <?= $filterLevel === 'parent' ? 'selected' : '' ?>>Parent Only</option>
                <option value="sub" <?= $filterLevel === 'sub' ? 'selected' : '' ?>>Subcategories</option>
            </select>
        </label>
        
        <label style="min-width: 150px;">
            <small>Parent Category</small>
            <select name="parent" style="width: 100%; padding: 0.4rem;">
                <option value="">All Parents</option>
                <?php foreach ($parentCats as $pc): ?>
                <option value="<?= $pc['category_id'] ?>" <?= $filterParent == $pc['category_id'] ? 'selected' : '' ?>><?= htmlspecialchars($pc['name']) ?></option>
                <?php endforeach; ?>
            </select>
        </label>
        
        <button type="submit" class="btn btn-primary" style="padding: 0.5rem 1rem;">🔍 Filter</button>
        <a href="index.php?page=categories" class="btn btn-outline" style="padding: 0.5rem 1rem;">Clear</a>
    </form>
</div>

<!-- Add/Edit Form -->
<div class="card">
    <h3 style="margin-top: 0;"><?= $editCat ? '✏️ Edit Category' : '➕ Add New Category' ?></h3>
    <form method="POST" enctype="multipart/form-data" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; align-items: end;">
        <input type="hidden" name="action" value="<?= $editCat ? 'edit_category' : 'add_category' ?>">
        <?php if ($editCat): ?>
        <input type="hidden" name="category_id" value="<?= $editCat['category_id'] ?>">
        <input type="hidden" name="existing_image" value="<?= $editCat['image_url'] ?>">
        <?php endif; ?>
        
        <label>
            <small>Name *</small>
            <input type="text" name="name" value="<?= htmlspecialchars($editCat['name'] ?? '') ?>" required style="width: 100%; padding: 0.5rem;">
        </label>
        
        <label>
            <small>Parent Category</small>
            <select name="parent_id" style="width: 100%; padding: 0.5rem;">
                <option value="">None (Root)</option>
                <?php foreach ($parentCats as $pc): ?>
                <?php if (($editCat['category_id'] ?? 0) != $pc['category_id']): ?>
                <option value="<?= $pc['category_id'] ?>" <?= ($editCat['parent_id'] ?? '') == $pc['category_id'] ? 'selected' : '' ?>>
                    <?= htmlspecialchars($pc['name']) ?> (<?= $pc['listing_type'] ?>)
                </option>
                <?php endif; ?>
                <?php endforeach; ?>
            </select>
        </label>
        
        <label>
            <small>Listing Type</small>
            <select name="listing_type" style="width: 100%; padding: 0.5rem;">
                <option value="">Inherit from Parent</option>
                <option value="services" <?= ($editCat['listing_type'] ?? '') === 'services' ? 'selected' : '' ?>>Services</option>
                <option value="selling" <?= ($editCat['listing_type'] ?? '') === 'selling' ? 'selected' : '' ?>>Selling</option>
                <option value="business" <?= ($editCat['listing_type'] ?? '') === 'business' ? 'selected' : '' ?>>Business</option>
                <option value="jobs" <?= ($editCat['listing_type'] ?? '') === 'jobs' ? 'selected' : '' ?>>Jobs</option>
            </select>
        </label>
        
        <label>
            <small>Image</small>
            <?php if (!empty($editCat['image_url'])): ?>
            <img src="<?= $editCat['image_url'] ?>" style="height: 30px; vertical-align: middle; margin-right: 5px; border-radius: 4px;">
            <?php endif; ?>
            <input type="file" name="image" style="width: 100%;">
        </label>
        
        <div>
            <button type="submit" class="btn btn-primary"><?= $editCat ? '💾 Update' : '➕ Add' ?></button>
            <?php if ($editCat): ?>
            <a href="index.php?page=categories" class="btn btn-outline">Cancel</a>
            <?php endif; ?>
        </div>
    </form>
</div>

<!-- Categories Table -->
<div class="card" style="overflow-x: auto;">
    <div style="margin-bottom: 1rem; color: var(--text-light);">
        Showing <?= count($categories) ?> categories
    </div>
    <table style="min-width: 700px;">
        <thead>
            <tr>
                <th style="width: 40px;"></th>
                <th>Category</th>
                <th>Type</th>
                <th>Parent</th>
                <th>Subs</th>
                <th>Listings</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($categories as $c): ?>
        <tr style="<?= $c['parent_id'] ? 'background: rgba(99,102,241,0.03);' : '' ?>">
            <td>
                <?php if ($c['image_url']): ?>
                <img src="<?= $c['image_url'] ?>" style="width: 32px; height: 32px; object-fit: cover; border-radius: 4px;">
                <?php else: ?>
                <span style="color: var(--text-light);">📁</span>
                <?php endif; ?>
            </td>
            <td>
                <?php if ($c['parent_id']): ?>
                <span style="color: var(--text-light);">└ </span>
                <?php endif; ?>
                <strong><?= htmlspecialchars($c['name']) ?></strong>
                <br><small style="color: var(--text-light);"><?= $c['slug'] ?></small>
            </td>
            <td>
                <?php if ($c['listing_type']): ?>
                <span class="badge"><?= ucfirst($c['listing_type']) ?></span>
                <?php else: ?>
                <span style="color: var(--text-light);">-</span>
                <?php endif; ?>
            </td>
            <td><?= $c['parent_name'] ? htmlspecialchars($c['parent_name']) : '<span style="color:var(--text-light)">Root</span>' ?></td>
            <td><?= $c['child_count'] ?: '-' ?></td>
            <td><?= $c['listing_count'] ?: '-' ?></td>
            <td>
                <a href="index.php?page=categories&edit=<?= $c['category_id'] ?>" class="btn btn-outline" style="padding: 3px 8px;">✏️</a>
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete this category?');">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="category_id" value="<?= $c['category_id'] ?>">
                    <button type="submit" class="btn btn-danger" style="padding: 3px 8px;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>

<!-- Shop Categories Section -->
<?php
try {
    $shopCats = $db->query("SELECT * FROM shop_categories ORDER BY name")->fetchAll();
?>
<div class="card">
    <h3 style="margin-top: 0;">🛍️ Shop Categories</h3>
    <?php if (empty($shopCats)): ?>
    <p style="color: var(--text-light);">No shop categories found.</p>
    <?php else: ?>
    <table>
        <thead><tr><th>ID</th><th>Name</th><th>Slug</th><th>Image</th><th>Actions</th></tr></thead>
        <tbody>
        <?php foreach ($shopCats as $sc): ?>
        <tr>
            <td><?= $sc['category_id'] ?? $sc['id'] ?? '-' ?></td>
            <td><strong><?= htmlspecialchars($sc['name']) ?></strong></td>
            <td style="color: var(--text-light);"><?= $sc['slug'] ?? '-' ?></td>
            <td>
                <?php if (!empty($sc['image_url'])): ?>
                <img src="<?= $sc['image_url'] ?>" style="height: 30px; border-radius: 4px;">
                <?php else: ?>-<?php endif; ?>
            </td>
            <td>
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete shop category?');">
                    <input type="hidden" name="action" value="delete_shop_cat">
                    <input type="hidden" name="shop_cat_id" value="<?= $sc['category_id'] ?? $sc['id'] ?>">
                    <button type="submit" class="btn btn-danger" style="padding: 3px 8px;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
    <?php endif; ?>
</div>
<?php } catch (Exception $e) { /* shop_categories table doesn't exist */ } ?>

<!-- Old Categories Section -->
<?php
try {
    $oldCats = $db->query("SELECT * FROM old_categories ORDER BY name")->fetchAll();
?>
<div class="card">
    <h3 style="margin-top: 0;">📦 Old Categories (Legacy)</h3>
    <?php if (empty($oldCats)): ?>
    <p style="color: var(--text-light);">No old categories found.</p>
    <?php else: ?>
    <table>
        <thead><tr><th>ID</th><th>Name</th><th>Slug</th><th>Actions</th></tr></thead>
        <tbody>
        <?php foreach ($oldCats as $oc): ?>
        <tr>
            <td><?= $oc['category_id'] ?? $oc['id'] ?? '-' ?></td>
            <td><strong><?= htmlspecialchars($oc['name']) ?></strong></td>
            <td style="color: var(--text-light);"><?= $oc['slug'] ?? '-' ?></td>
            <td>
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete old category?');">
                    <input type="hidden" name="action" value="delete_old_cat">
                    <input type="hidden" name="old_cat_id" value="<?= $oc['category_id'] ?? $oc['id'] ?>">
                    <button type="submit" class="btn btn-danger" style="padding: 3px 8px;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
    <?php endif; ?>
</div>
<?php } catch (Exception $e) { /* old_categories table doesn't exist */ } ?>
