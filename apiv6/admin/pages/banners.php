<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'delete_banner':
                    $id = $_POST['banner_id'];
                    $db->prepare("DELETE FROM banners WHERE banner_id = ?")->execute([$id]);
                    header("Location: index.php?page=banners&msg=" . urlencode("Banner deleted!"));
                    exit;
                    
                case 'toggle_active':
                    $id = $_POST['banner_id'];
                    $db->prepare("UPDATE banners SET is_active = NOT is_active WHERE banner_id = ?")->execute([$id]);
                    header("Location: index.php?page=banners&msg=" . urlencode("Status updated!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

$banners = $db->query("SELECT * FROM banners ORDER BY placement, sort_order")->fetchAll();
?>

<div class="header">
    <div class="page-title">Manage Banners</div>
    <a href="index.php?page=banner_form" class="btn btn-primary">➕ Add Banner</a>
</div>

<div class="card" style="overflow-x: auto;">
    <table style="min-width: 800px;">
        <thead>
            <tr>
                <th style="width: 100px;">Image</th>
                <th>Title & Link</th>
                <th>Placement</th>
                <th>Order</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($banners as $b): ?>
        <tr>
            <td>
                <img src="<?= $b['image_url'] ?>" style="width: 120px; height: 60px; object-fit: cover; border-radius: 4px;">
            </td>
            <td>
                <strong><?= htmlspecialchars($b['title']) ?></strong><br>
                <small style="color: #666;"><?= htmlspecialchars($b['link_url'] ?? '') ?></small>
            </td>
            <td><span class="badge"><?= $b['placement'] ?></span></td>
            <td><?= $b['sort_order'] ?></td>
            <td>
                <span style="padding: 2px 8px; border-radius: 99px; font-size: 0.8em; background: <?= $b['is_active'] ? '#dcfce7' : '#fee2e2' ?>; color: <?= $b['is_active'] ? '#166534' : '#dc2626' ?>;">
                    <?= $b['is_active'] ? 'Active' : 'Inactive' ?>
                </span>
            </td>
            <td>
                <a href="index.php?page=banner_form&id=<?= $b['banner_id'] ?>" class="btn btn-outline" style="padding: 4px 8px;">✏️</a>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="toggle_active">
                    <input type="hidden" name="banner_id" value="<?= $b['banner_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px;" title="Toggle Status">↻</button>
                </form>
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete banner?');">
                    <input type="hidden" name="action" value="delete_banner">
                    <input type="hidden" name="banner_id" value="<?= $b['banner_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: red; border-color: #fee2e2;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
