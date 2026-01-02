<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'delete_product':
                    $productId = $_POST['product_id'];
                    $db->prepare("UPDATE old_products SET status = 'deleted' WHERE product_id = ?")->execute([$productId]);
                    header("Location: index.php?page=old_products&msg=" . urlencode("Product deleted!"));
                    exit;
                
                case 'toggle_status':
                    $productId = $_POST['product_id'];
                    $currentStatus = $_POST['current_status'];
                    $newStatus = $currentStatus === 'active' ? 'sold' : 'active';
                    $db->prepare("UPDATE old_products SET status = ? WHERE product_id = ?")->execute([$newStatus, $productId]);
                    header("Location: index.php?page=old_products&msg=" . urlencode("Status updated!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

// Fetch Products
$search = $_GET['search'] ?? '';
$filterCat = $_GET['filter_category'] ?? '';
$filterStatus = $_GET['filter_status'] ?? '';

$sql = "SELECT op.*, u.name as seller_name, u.phone as seller_phone, oc.name as category_name 
        FROM old_products op 
        LEFT JOIN users u ON op.user_id = u.phone 
        LEFT JOIN old_categories oc ON op.old_category_id = oc.id 
        WHERE op.status != 'deleted'";
$params = [];

if ($search) {
    $sql .= " AND (op.product_name LIKE ? OR op.brand LIKE ? OR op.model LIKE ?)";
    $params[] = "%$search%";
    $params[] = "%$search%";
    $params[] = "%$search%";
}
if ($filterCat) {
    $sql .= " AND op.old_category_id = ?";
    $params[] = $filterCat;
}
if ($filterStatus) {
    $sql .= " AND op.status = ?";
    $params[] = $filterStatus;
}

$sql .= " ORDER BY op.created_at DESC LIMIT 50";
$stmt = $db->prepare($sql);
$stmt->execute($params);
$products = $stmt->fetchAll();

// Fetch Categories for Filter (only parent categories)
$categories = $db->query("SELECT * FROM old_categories WHERE parent_id IS NULL ORDER BY name")->fetchAll();
?>

<div class="header">
    <div class="page-title">Manage Old Products (Used Items)</div>
    <a href="index.php?page=old_product_form" class="btn btn-primary">➕ Add Old Product</a>
</div>

<div class="card">
    <form method="GET" style="display: flex; gap: 10px; flex-wrap: wrap;">
        <input type="hidden" name="page" value="old_products">
        <input type="text" name="search" value="<?= htmlspecialchars($search) ?>" placeholder="Search product, brand, model..." style="flex: 2; padding: 0.5rem;">
        <select name="filter_category" style="padding: 0.5rem;">
            <option value="">All Categories</option>
            <?php foreach ($categories as $c): ?>
            <option value="<?= $c['id'] ?>" <?= $filterCat == $c['id'] ? 'selected' : '' ?>><?= htmlspecialchars($c['name']) ?></option>
            <?php endforeach; ?>
        </select>
        <select name="filter_status" style="padding: 0.5rem;">
            <option value="">All Status</option>
            <option value="active" <?= $filterStatus === 'active' ? 'selected' : '' ?>>Active</option>
            <option value="sold" <?= $filterStatus === 'sold' ? 'selected' : '' ?>>Sold</option>
            <option value="expired" <?= $filterStatus === 'expired' ? 'selected' : '' ?>>Expired</option>
        </select>
        <button type="submit" class="btn btn-primary">Search</button>
    </form>
</div>

<div class="card" style="overflow-x: auto;">
    <table style="min-width: 1000px;">
        <thead>
            <tr>
                <th style="width: 50px;">Image</th>
                <th>Product Name</th>
                <th>Seller</th>
                <th>Category</th>
                <th>Price</th>
                <th>Condition</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($products as $p): ?>
        <tr>
            <td>
                <?php if ($p['image_url']): ?>
                <img src="<?= $p['image_url'] ?>" style="width: 40px; height: 40px; object-fit: cover; border-radius: 4px;">
                <?php else: ?>
                <div style="width: 40px; height: 40px; background: #eee; border-radius: 4px;"></div>
                <?php endif; ?>
            </td>
            <td>
                <strong><?= htmlspecialchars($p['product_name']) ?></strong><br>
                <?php if ($p['brand'] || $p['model']): ?>
                <small style="color: #666;"><?= htmlspecialchars(trim($p['brand'] . ' ' . $p['model'])) ?></small>
                <?php endif; ?>
            </td>
            <td>
                <?= htmlspecialchars($p['seller_name'] ?? 'Unknown') ?><br>
                <small style="color: #666;"><?= $p['user_id'] ?></small>
            </td>
            <td><?= htmlspecialchars($p['category_name'] ?? '-') ?></td>
            <td>
                <?php if ($p['original_price'] && $p['original_price'] > $p['price']): ?>
                    <span style="text-decoration: line-through; color: #999;">₹<?= number_format($p['original_price']) ?></span><br>
                <?php endif; ?>
                <strong>₹<?= number_format($p['price']) ?></strong>
            </td>
            <td>
                <?php 
                $conditionColors = [
                    'like_new' => '#22c55e',
                    'good' => '#3b82f6',
                    'fair' => '#f59e0b',
                    'poor' => '#ef4444'
                ];
                $conditionLabels = [
                    'like_new' => 'Like New',
                    'good' => 'Good',
                    'fair' => 'Fair',
                    'poor' => 'Poor'
                ];
                $color = $conditionColors[$p['condition']] ?? '#666';
                $label = $conditionLabels[$p['condition']] ?? $p['condition'];
                ?>
                <span style="padding: 2px 8px; border-radius: 99px; font-size: 0.8em; background: <?= $color ?>22; color: <?= $color ?>;">
                    <?= $label ?>
                </span>
            </td>
            <td>
                <?php 
                $statusColors = ['active' => '#22c55e', 'sold' => '#3b82f6', 'expired' => '#f59e0b', 'deleted' => '#ef4444'];
                $statusColor = $statusColors[$p['status']] ?? '#666';
                ?>
                <span style="padding: 2px 8px; border-radius: 99px; font-size: 0.8em; background: <?= $statusColor ?>22; color: <?= $statusColor ?>;">
                    <?= ucfirst($p['status']) ?>
                </span>
            </td>
            <td>
                <a href="index.php?page=old_product_form&id=<?= $p['product_id'] ?>" class="btn btn-outline" style="padding: 4px 8px;">✏️</a>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="toggle_status">
                    <input type="hidden" name="product_id" value="<?= $p['product_id'] ?>">
                    <input type="hidden" name="current_status" value="<?= $p['status'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px;" title="Toggle Status">↻</button>
                </form>
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete this product?');">
                    <input type="hidden" name="action" value="delete_product">
                    <input type="hidden" name="product_id" value="<?= $p['product_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: red; border-color: #fee2e2;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
    <?php if (empty($products)): ?>
    <p style="text-align: center; color: #666; padding: 2rem;">No old products found.</p>
    <?php endif; ?>
</div>
