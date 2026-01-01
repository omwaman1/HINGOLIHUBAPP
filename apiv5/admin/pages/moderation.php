<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'toggle_moderation_listings':
                    $newValue = $_POST['value'] === 'true' ? 'true' : 'false';
                    $db->prepare("INSERT INTO settings (setting_key, setting_value) VALUES ('auto_moderation_listings', ?) 
                                  ON DUPLICATE KEY UPDATE setting_value = ?")->execute([$newValue, $newValue]);
                    header("Location: index.php?page=moderation&msg=" . urlencode("Listings moderation updated!"));
                    exit;
                case 'toggle_moderation_products':
                    $newValue = $_POST['value'] === 'true' ? 'true' : 'false';
                    $db->prepare("INSERT INTO settings (setting_key, setting_value) VALUES ('auto_moderation_products', ?) 
                                  ON DUPLICATE KEY UPDATE setting_value = ?")->execute([$newValue, $newValue]);
                    header("Location: index.php?page=moderation&msg=" . urlencode("Products moderation updated!"));
                    exit;
                case 'approve':
                    $id = $_POST['listing_id'];
                    
                    // Get listing details for notification
                    $stmt = $db->prepare("SELECT user_id, title FROM listings WHERE listing_id = ?");
                    $stmt->execute([$id]);
                    $listing = $stmt->fetch();
                    
                    // Update status
                    $db->prepare("UPDATE listings SET status = 'active', is_verified = 1 WHERE listing_id = ?")->execute([$id]);
                    
                    // Send push notification to user
                    if ($listing) {
                        require_once __DIR__ . '/../../helpers/firebase.php';
                        sendListingApprovedNotification((int)$listing['user_id'], $listing['title']);
                    }
                    
                    header("Location: index.php?page=moderation&msg=" . urlencode("Listing approved!"));
                    exit;
                case 'reject':
                    $id = $_POST['listing_id'];
                    $db->prepare("UPDATE listings SET status = 'rejected' WHERE listing_id = ?")->execute([$id]);
                    header("Location: index.php?page=moderation&msg=" . urlencode("Listing rejected!"));
                    exit;
                case 'approve_product':
                    $id = $_POST['product_id'];
                    
                    // Get product details for notification (shop_products link to listing to get user_id)
                    $stmt = $db->prepare("SELECT sp.product_name, l.user_id 
                                          FROM shop_products sp 
                                          LEFT JOIN listings l ON sp.listing_id = l.listing_id 
                                          WHERE sp.product_id = ?");
                    $stmt->execute([$id]);
                    $shopProduct = $stmt->fetch();
                    
                    // Update status
                    $db->prepare("UPDATE shop_products SET is_active = 1 WHERE product_id = ?")->execute([$id]);
                    
                    // Send push notification to user
                    if ($shopProduct && $shopProduct['user_id']) {
                        require_once __DIR__ . '/../../helpers/firebase.php';
                        sendListingApprovedNotification((int)$shopProduct['user_id'], $shopProduct['product_name']);
                    }
                    
                    header("Location: index.php?page=moderation&msg=" . urlencode("Product approved!"));
                    exit;;
                case 'reject_product':
                    $id = $_POST['product_id'];
                    $db->prepare("UPDATE shop_products SET is_active = 0 WHERE product_id = ?")->execute([$id]);
                    header("Location: index.php?page=moderation&msg=" . urlencode("Product rejected!"));
                    exit;
                case 'approve_old_product':
                    $id = $_POST['old_product_id'];
                    
                    // Get product details for notification
                    $stmt = $db->prepare("SELECT user_id, product_name FROM old_products WHERE product_id = ?");
                    $stmt->execute([$id]);
                    $oldProduct = $stmt->fetch();
                    
                    // Update status
                    $db->prepare("UPDATE old_products SET status = 'active' WHERE product_id = ?")->execute([$id]);
                    
                    // Send push notification to user
                    if ($oldProduct) {
                        require_once __DIR__ . '/../../helpers/firebase.php';
                        sendListingApprovedNotification((int)$oldProduct['user_id'], $oldProduct['product_name']);
                    }
                    
                    header("Location: index.php?page=moderation&msg=" . urlencode("Old product approved!"));
                    exit;
                case 'reject_old_product':
                    $id = $_POST['old_product_id'];
                    $db->prepare("UPDATE old_products SET status = 'rejected' WHERE product_id = ?")->execute([$id]);
                    header("Location: index.php?page=moderation&msg=" . urlencode("Old product rejected!"));
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

// Fetch Pending Old Products (user selling items)
$pendingOldProducts = [];
try {
    $pendingOldProducts = $db->query("
        SELECT op.*, u.username, oc.name as category_name
        FROM old_products op
        LEFT JOIN users u ON op.user_id = u.user_id
        LEFT JOIN old_categories oc ON op.old_category_id = oc.id
        WHERE op.status = 'pending'
        ORDER BY op.created_at DESC
    ")->fetchAll();
} catch (Exception $e) {
    // Ignore errors
}

// Fetch current moderation settings
$stmt = $db->prepare("SELECT setting_key, setting_value FROM settings WHERE setting_key IN ('auto_moderation_listings', 'auto_moderation_products')");
$stmt->execute();
$settingsRows = $stmt->fetchAll();
$settings = [];
foreach ($settingsRows as $row) {
    $settings[$row['setting_key']] = $row['setting_value'] === 'true';
}
$autoModListings = $settings['auto_moderation_listings'] ?? false;
$autoModProducts = $settings['auto_moderation_products'] ?? false;
?>

<div class="header">
    <div class="page-title">🛡️ Moderation Queue</div>
</div>

<!-- MODERATION SETTINGS -->
<div class="card" style="margin-bottom: 1.5rem; padding: 1.5rem;">
    <h3 style="margin: 0 0 1rem; display: flex; align-items: center; gap: 0.5rem;">
        ⚙️ Moderation Settings
    </h3>
    <div style="display: flex; gap: 2rem; flex-wrap: wrap;">
        <!-- Listings Toggle -->
        <div style="display: flex; align-items: center; gap: 0.75rem;">
            <span style="font-weight: 500;">Listings:</span>
            <form method="POST" style="display: inline;">
                <input type="hidden" name="action" value="toggle_moderation_listings">
                <input type="hidden" name="value" value="<?= $autoModListings ? 'false' : 'true' ?>">
                <button type="submit" class="btn <?= $autoModListings ? 'btn-primary' : 'btn-outline' ?>" 
                        style="padding: 6px 12px; font-size: 0.85rem;">
                    <?= $autoModListings ? '✓ Auto Approve' : '⏸ Manual Approval' ?>
                </button>
            </form>
            <small style="color: var(--text-light);">
                <?= $autoModListings ? 'New listings go live immediately' : 'Listings require admin approval' ?>
            </small>
        </div>
        
        <!-- Products Toggle -->
        <div style="display: flex; align-items: center; gap: 0.75rem;">
            <span style="font-weight: 500;">Products:</span>
            <form method="POST" style="display: inline;">
                <input type="hidden" name="action" value="toggle_moderation_products">
                <input type="hidden" name="value" value="<?= $autoModProducts ? 'false' : 'true' ?>">
                <button type="submit" class="btn <?= $autoModProducts ? 'btn-primary' : 'btn-outline' ?>" 
                        style="padding: 6px 12px; font-size: 0.85rem;">
                    <?= $autoModProducts ? '✓ Auto Approve' : '⏸ Manual Approval' ?>
                </button>
            </form>
            <small style="color: var(--text-light);">
                <?= $autoModProducts ? 'New products go live immediately' : 'Products require admin approval' ?>
            </small>
        </div>
    </div>
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

<!-- PENDING OLD PRODUCTS (Selling Items) -->
<h3 style="margin: 2rem 0 1rem;">🏷️ Pending Old Products (<?= count($pendingOldProducts) ?>)</h3>
<?php if (empty($pendingOldProducts)): ?>
<div class="card" style="text-align: center; padding: 2rem;">
    <div style="font-size: 2rem; margin-bottom: 0.5rem;">✅</div>
    <p style="color: var(--text-light); margin: 0;">No pending old products</p>
</div>
<?php else: ?>
<div class="card" style="overflow-x: auto;">
    <table style="min-width: 800px;">
        <thead>
            <tr>
                <th>Image</th>
                <th>Product</th>
                <th>Seller</th>
                <th>Price</th>
                <th>Category</th>
                <th>Date</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($pendingOldProducts as $op): ?>
        <tr>
            <td>
                <?php if (!empty($op['image_url'])): ?>
                <img src="<?= $op['image_url'] ?>" style="width: 60px; height: 60px; object-fit: cover; border-radius: 6px;">
                <?php else: ?>
                <div style="width: 60px; height: 60px; background: var(--border); border-radius: 6px; display: flex; align-items: center; justify-content: center;">🏷️</div>
                <?php endif; ?>
            </td>
            <td>
                <strong><?= htmlspecialchars($op['product_name']) ?></strong><br>
                <small style="color: var(--text-light);">Condition: <?= htmlspecialchars($op['condition'] ?? 'N/A') ?></small>
            </td>
            <td><?= htmlspecialchars($op['username'] ?? 'Unknown') ?></td>
            <td>₹<?= number_format($op['price'], 2) ?></td>
            <td><?= htmlspecialchars($op['category_name'] ?? 'N/A') ?></td>
            <td><?= date('d M Y', strtotime($op['created_at'])) ?></td>
            <td>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="approve_old_product">
                    <input type="hidden" name="old_product_id" value="<?= $op['product_id'] ?>">
                    <button type="submit" class="btn btn-primary" style="padding: 4px 10px;">✓ Approve</button>
                </form>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="reject_old_product">
                    <input type="hidden" name="old_product_id" value="<?= $op['product_id'] ?>">
                    <button type="submit" class="btn btn-danger" style="padding: 4px 10px;">✕ Reject</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
<?php endif; ?>
