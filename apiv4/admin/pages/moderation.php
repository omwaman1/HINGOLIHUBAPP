<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'approve':
                    $id = $_POST['listing_id'];
                    $db->prepare("UPDATE listings SET status = 'active', is_verified = 1 WHERE listing_id = ?")->execute([$id]);
                    header("Location: index.php?page=moderation&msg=" . urlencode("Listing approved!"));
                    exit;
                case 'reject':
                    $id = $_POST['listing_id'];
                    $db->prepare("UPDATE listings SET status = 'rejected' WHERE listing_id = ?")->execute([$id]);
                    header("Location: index.php?page=moderation&msg=" . urlencode("Listing rejected!"));
                    exit;
                case 'approve_product':
                    $id = $_POST['product_id'];
                    $db->prepare("UPDATE shop_products SET is_active = 1 WHERE product_id = ?")->execute([$id]);
                    header("Location: index.php?page=moderation&msg=" . urlencode("Product approved!"));
                    exit;
                case 'reject_product':
                    $id = $_POST['product_id'];
                    $db->prepare("UPDATE shop_products SET is_active = 0 WHERE product_id = ?")->execute([$id]);
                    header("Location: index.php?page=moderation&msg=" . urlencode("Product rejected!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

// Fetch Pending Listings
$listings = $db->query("SELECT l.*, u.username, c.name as category_name 
                        FROM listings l 
                        LEFT JOIN users u ON l.user_id = u.user_id 
                        LEFT JOIN categories c ON l.category_id = c.category_id 
                        WHERE l.status = 'pending' 
                        ORDER BY l.created_at DESC")->fetchAll();

// Fetch Pending Products (from shop_products)
$pendingProducts = [];
try {
    $pendingProducts = $db->query("
        SELECT sp.*, l.title as listing_title, u.username, oc.name as category_name
        FROM shop_products sp
        LEFT JOIN listings l ON sp.listing_id = l.listing_id
        LEFT JOIN users u ON l.user_id = u.user_id
        LEFT JOIN old_categories oc ON sp.shop_category_id = oc.id
        WHERE sp.is_active = 0
        ORDER BY sp.created_at DESC
    ")->fetchAll();
} catch (Exception $e) {
    // moderation_status column may not exist, try simpler query
    try {
        $pendingProducts = $db->query("
            SELECT sp.*, l.title as listing_title, u.username, oc.name as category_name
            FROM shop_products sp
            LEFT JOIN listings l ON sp.listing_id = l.listing_id
            LEFT JOIN users u ON l.user_id = u.user_id
            LEFT JOIN old_categories oc ON sp.shop_category_id = oc.id
            WHERE sp.is_active = 0
            ORDER BY sp.created_at DESC
        ")->fetchAll();
    } catch (Exception $e2) {
        // Ignore errors
    }
}
?>

<div class="header">
    <div class="page-title">🛡️ Moderation Queue</div>
</div>

<!-- PENDING LISTINGS -->
<h3 style="margin: 1.5rem 0 1rem;">📋 Pending Listings (<?= count($listings) ?>)</h3>
<?php if (empty($listings)): ?>
<div class="card" style="text-align: center; padding: 2rem;">
    <div style="font-size: 2rem; margin-bottom: 0.5rem;">✅</div>
    <p style="color: var(--text-light); margin: 0;">No pending listings</p>
</div>
<?php else: ?>
<div class="card" style="overflow-x: auto;">
    <table style="min-width: 800px;">
        <thead>
            <tr>
                <th>Image</th>
                <th>Listing</th>
                <th>Submitted By</th>
                <th>Category</th>
                <th>Date</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($listings as $l): ?>
        <tr>
            <td>
                <?php if ($l['main_image_url']): ?>
                <img src="<?= $l['main_image_url'] ?>" style="width: 60px; height: 60px; object-fit: cover; border-radius: 6px;">
                <?php endif; ?>
            </td>
            <td>
                <strong><?= htmlspecialchars($l['title']) ?></strong><br>
                <small style="color: var(--text-light);"><?= $l['listing_type'] ?></small>
            </td>
            <td><?= htmlspecialchars($l['username'] ?? 'Unknown') ?></td>
            <td><?= htmlspecialchars($l['category_name']) ?></td>
            <td><?= date('d M Y', strtotime($l['created_at'])) ?></td>
            <td>
                <a href="index.php?page=listing_form&id=<?= $l['listing_id'] ?>" class="btn btn-outline" style="padding: 4px 8px;">👁 View</a>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="approve">
                    <input type="hidden" name="listing_id" value="<?= $l['listing_id'] ?>">
                    <button type="submit" class="btn btn-primary" style="padding: 4px 10px;">✓ Approve</button>
                </form>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="reject">
                    <input type="hidden" name="listing_id" value="<?= $l['listing_id'] ?>">
                    <button type="submit" class="btn btn-danger" style="padding: 4px 10px;">✕ Reject</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
<?php endif; ?>

<!-- PENDING PRODUCTS -->
<h3 style="margin: 2rem 0 1rem;">🛒 Pending Products (<?= count($pendingProducts) ?>)</h3>
<?php if (empty($pendingProducts)): ?>
<div class="card" style="text-align: center; padding: 2rem;">
    <div style="font-size: 2rem; margin-bottom: 0.5rem;">✅</div>
    <p style="color: var(--text-light); margin: 0;">No pending products</p>
</div>
<?php else: ?>
<div class="card" style="overflow-x: auto;">
    <table style="min-width: 800px;">
        <thead>
            <tr>
                <th>Image</th>
                <th>Product</th>
                <th>Business</th>
                <th>Price</th>
                <th>Category</th>
                <th>Date</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($pendingProducts as $p): ?>
        <tr>
            <td>
                <?php if (!empty($p['image_url'])): ?>
                <img src="<?= $p['image_url'] ?>" style="width: 60px; height: 60px; object-fit: cover; border-radius: 6px;">
                <?php else: ?>
                <div style="width: 60px; height: 60px; background: var(--border); border-radius: 6px; display: flex; align-items: center; justify-content: center;">📦</div>
                <?php endif; ?>
            </td>
            <td>
                <strong><?= htmlspecialchars($p['product_name']) ?></strong><br>
                <small style="color: var(--text-light);">ID: <?= $p['product_id'] ?></small>
            </td>
            <td><?= htmlspecialchars($p['listing_title'] ?? 'Unknown') ?><br>
                <small style="color: var(--text-light);">by <?= htmlspecialchars($p['username'] ?? 'Unknown') ?></small>
            </td>
            <td>₹<?= number_format($p['price'], 2) ?></td>
            <td><?= htmlspecialchars($p['category_name'] ?? 'N/A') ?></td>
            <td><?= date('d M Y', strtotime($p['created_at'])) ?></td>
            <td>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="approve_product">
                    <input type="hidden" name="product_id" value="<?= $p['product_id'] ?>">
                    <button type="submit" class="btn btn-primary" style="padding: 4px 10px;">✓ Approve</button>
                </form>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="reject_product">
                    <input type="hidden" name="product_id" value="<?= $p['product_id'] ?>">
                    <button type="submit" class="btn btn-danger" style="padding: 4px 10px;">✕ Reject</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
<?php endif; ?>

