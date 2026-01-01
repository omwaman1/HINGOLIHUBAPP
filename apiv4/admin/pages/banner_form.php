<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'add_banner':
                    $title = $_POST['title'];
                    $link_url = $_POST['link_url'] ?? null;
                    $placement = $_POST['placement'];
                    $sort_order = $_POST['sort_order'] ?? 0;
                    $is_active = isset($_POST['is_active']) ? 1 : 0;
                    
                    $image_url = null;
                    if (isset($_FILES['banner_image']) && $_FILES['banner_image']['error'] === UPLOAD_ERR_OK) {
                        $image_url = uploadImage($_FILES['banner_image'], 'banners');
                    }
                    
                    if (!$image_url && !empty($_POST['image_url'])) {
                        $image_url = $_POST['image_url'];
                    }
                    
                    if (!$image_url) throw new Exception("Image required");
                    
                    $stmt = $db->prepare("INSERT INTO banners (title, image_url, link_url, placement, sort_order, is_active) VALUES (?, ?, ?, ?, ?, ?)");
                    $stmt->execute([$title, $image_url, $link_url, $placement, $sort_order, $is_active]);
                    
                    header("Location: index.php?page=banners&msg=" . urlencode("Banner added!"));
                    exit;

                case 'edit_banner':
                    $id = $_POST['banner_id'];
                    $title = $_POST['title'];
                    $link_url = $_POST['link_url'] ?? null;
                    $placement = $_POST['placement'];
                    $sort_order = $_POST['sort_order'] ?? 0;
                    $is_active = isset($_POST['is_active']) ? 1 : 0;
                    
                    $image_url = $_POST['existing_image'];
                    if (isset($_FILES['banner_image']) && $_FILES['banner_image']['error'] === UPLOAD_ERR_OK) {
                        $image_url = uploadImage($_FILES['banner_image'], 'banners');
                    } elseif (!empty($_POST['image_url'])) {
                        $image_url = $_POST['image_url'];
                    }
                    
                    $stmt = $db->prepare("UPDATE banners SET title = ?, image_url = ?, link_url = ?, placement = ?, sort_order = ?, is_active = ? WHERE banner_id = ?");
                    $stmt->execute([$title, $image_url, $link_url, $placement, $sort_order, $is_active, $id]);
                    
                    header("Location: index.php?page=banners&msg=" . urlencode("Banner updated!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

$editBanner = null;
if (isset($_GET['id'])) {
    $stmt = $db->prepare("SELECT * FROM banners WHERE banner_id = ?");
    $stmt->execute([$_GET['id']]);
    $editBanner = $stmt->fetch();
}
?>

<div class="header">
    <div class="page-title"><?= $editBanner ? 'Edit Banner' : 'Add New Banner' ?></div>
    <a href="index.php?page=banners" class="btn btn-outline">← Back to Banners</a>
</div>

<div class="card">
    <form method="POST" enctype="multipart/form-data">
        <input type="hidden" name="action" value="<?= $editBanner ? 'edit_banner' : 'add_banner' ?>">
        <?php if ($editBanner): ?>
        <input type="hidden" name="banner_id" value="<?= $editBanner['banner_id'] ?>">
        <input type="hidden" name="existing_image" value="<?= $editBanner['image_url'] ?>">
        <?php endif; ?>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; margin-bottom: 1.5rem;">
            <label>
                <strong>Title *</strong>
                <input type="text" name="title" value="<?= $editBanner['title'] ?? '' ?>" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            
            <label>
                <strong>Link URL</strong>
                <input type="text" name="link_url" value="<?= $editBanner['link_url'] ?? '' ?>" placeholder="https://..." style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            
            <label>
                <strong>Placement *</strong>
                <select name="placement" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="home_top" <?= ($editBanner['placement'] ?? '') === 'home_top' ? 'selected' : '' ?>>Home Top</option>
                    <option value="home_middle" <?= ($editBanner['placement'] ?? '') === 'home_middle' ? 'selected' : '' ?>>Home Middle</option>
                    <option value="shop_top" <?= ($editBanner['placement'] ?? '') === 'shop_top' ? 'selected' : '' ?>>Shop Top</option>
                </select>
            </label>
            
            <label>
                <strong>Sort Order</strong>
                <input type="number" name="sort_order" value="<?= $editBanner['sort_order'] ?? 0 ?>" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
        </div>
        
        <div style="margin-bottom: 1.5rem; background: #f8fafc; padding: 1rem; border-radius: 8px;">
            <h3 style="margin-top: 0;">📷 Banner Image</h3>
            <?php if (!empty($editBanner['image_url'])): ?>
            <img src="<?= $editBanner['image_url'] ?>" style="height: 100px; border-radius: 8px; margin: 0.5rem 0;">
            <?php endif; ?>
            <input type="file" name="banner_image">
            <div style="margin-top: 5px;">Or URL: <input type="text" name="image_url" placeholder="https://..." style="padding: 0.3rem;"></div>
        </div>
        
        <label style="display: block; margin-bottom: 1.5rem;">
            <input type="checkbox" name="is_active" <?= (!isset($editBanner) || $editBanner['is_active']) ? 'checked' : '' ?>> Active
        </label>
        
        <button type="submit" class="btn btn-primary"><?= $editBanner ? 'Update Banner' : 'Create Banner' ?></button>
    </form>
</div>
