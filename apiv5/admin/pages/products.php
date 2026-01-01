<?php
// Handle AJAX Actions
$isAjax = !empty($_SERVER['HTTP_X_REQUESTED_WITH']) && strtolower($_SERVER['HTTP_X_REQUESTED_WITH']) === 'xmlhttprequest';

if ($isAjax && $_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'delete_product':
                    $productId = $_POST['product_id'];
                    $db->prepare("DELETE FROM shop_products WHERE product_id = ?")->execute([$productId]);
                    echo json_encode(['success' => true, 'message' => 'Product deleted successfully!']);
                    exit;
                
                case 'toggle_active':
                    $productId = $_POST['product_id'];
                    $db->prepare("UPDATE shop_products SET is_active = NOT is_active WHERE product_id = ?")->execute([$productId]);
                    $stmt = $db->prepare("SELECT is_active FROM shop_products WHERE product_id = ?");
                    $stmt->execute([$productId]);
                    $newStatus = $stmt->fetchColumn();
                    echo json_encode(['success' => true, 'message' => 'Status updated!', 'is_active' => (bool)$newStatus]);
                    exit;
            }
        }
        echo json_encode(['success' => false, 'message' => 'Invalid action']);
        exit;
    } catch (Exception $e) {
        echo json_encode(['success' => false, 'message' => 'Error: ' . $e->getMessage()]);
        exit;
    }
}

// Handle Regular POST Actions (fallback)
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'delete_product':
                    $productId = $_POST['product_id'];
                    $db->prepare("DELETE FROM shop_products WHERE product_id = ?")->execute([$productId]);
                    header("Location: index.php?page=products&msg=" . urlencode("Product deleted!"));
                    exit;
                
                case 'toggle_active':
                    $productId = $_POST['product_id'];
                    $stmt = $db->prepare("UPDATE shop_products SET is_active = NOT is_active WHERE product_id = ?");
                    $stmt->execute([$productId]);
                    header("Location: index.php?page=products&msg=" . urlencode("Status updated!"));
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

$sql = "SELECT sp.*, l.title as listing_title, sc.name as category_name 
        FROM shop_products sp 
        LEFT JOIN listings l ON sp.listing_id = l.listing_id 
        LEFT JOIN shop_categories sc ON sp.shop_category_id = sc.id 
        WHERE 1=1";
$params = [];

if ($search) {
    $sql .= " AND (sp.product_name LIKE ? OR l.title LIKE ?)";
    $params[] = "%$search%";
    $params[] = "%$search%";
}
if ($filterCat) {
    $sql .= " AND sp.shop_category_id = ?";
    $params[] = $filterCat;
}

$sql .= " ORDER BY sp.created_at DESC LIMIT 50";
$stmt = $db->prepare($sql);
$stmt->execute($params);
$products = $stmt->fetchAll();

// Fetch Categories for Filter (from shop_categories table)
$categories = $db->query("SELECT * FROM shop_categories WHERE parent_id IS NULL AND is_active = 1 ORDER BY sort_order, name")->fetchAll();
?>

<!-- Toast Notification -->
<div id="toast" style="display: none; position: fixed; top: 20px; right: 20px; padding: 12px 24px; border-radius: 8px; color: white; font-weight: 500; z-index: 9999; box-shadow: 0 4px 12px rgba(0,0,0,0.3); transition: all 0.3s ease;"></div>

<div class="header">
    <div class="page-title">Manage Shop Products</div>
    <a href="index.php?page=product_form" class="btn btn-primary">➕ Add Product</a>
</div>

<div class="card">
    <form method="GET" style="display: flex; gap: 10px; flex-wrap: wrap;">
        <input type="hidden" name="page" value="products">
        <input type="text" name="search" value="<?= htmlspecialchars($search) ?>" placeholder="Search product or shop..." style="flex: 2; padding: 0.5rem;">
        <select name="filter_category" style="padding: 0.5rem;">
            <option value="">All Categories</option>
            <?php foreach ($categories as $c): ?>
            <option value="<?= $c['id'] ?>" <?= $filterCat == $c['id'] ? 'selected' : '' ?>><?= htmlspecialchars($c['name']) ?></option>
            <?php endforeach; ?>
        </select>
        <button type="submit" class="btn btn-primary">Search</button>
    </form>
</div>

<div class="card" style="overflow-x: auto;">
    <table style="min-width: 900px;">
        <thead>
            <tr>
                <th style="width: 50px;">Image</th>
                <th>Product Name</th>
                <th>Shop (Listing)</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($products as $p): ?>
        <tr id="product-row-<?= $p['product_id'] ?>">
            <td>
                <?php if ($p['image_url']): ?>
                <img src="<?= $p['image_url'] ?>" style="width: 40px; height: 40px; object-fit: cover; border-radius: 4px;">
                <?php else: ?>
                <div style="width: 40px; height: 40px; background: #333; border-radius: 4px;"></div>
                <?php endif; ?>
            </td>
            <td>
                <strong><?= htmlspecialchars($p['product_name']) ?></strong><br>
                <small style="color: #666;"><?= htmlspecialchars($p['category_name'] ?? '') ?></small>
            </td>
            <td><?= htmlspecialchars($p['listing_title'] ?? '') ?></td>
            <td>
                <?php if ($p['discounted_price']): ?>
                    <span style="text-decoration: line-through; color: #999;">₹<?= number_format($p['price']) ?></span><br>
                    <strong>₹<?= number_format($p['discounted_price']) ?></strong>
                <?php else: ?>
                    ₹<?= number_format($p['price']) ?>
                <?php endif; ?>
            </td>
            <td><?= $p['stock_qty'] ?? '-' ?></td>
            <td>
                <!-- Toggle Switch -->
                <label class="toggle-switch" style="position: relative; display: inline-block; width: 50px; height: 26px; cursor: pointer;">
                    <input type="checkbox" 
                           id="toggle-<?= $p['product_id'] ?>" 
                           <?= $p['is_active'] ? 'checked' : '' ?> 
                           onchange="toggleStatus(<?= $p['product_id'] ?>, this)"
                           style="opacity: 0; width: 0; height: 0;">
                    <span class="slider" style="position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: <?= $p['is_active'] ? '#22c55e' : '#444' ?>; transition: 0.3s; border-radius: 26px;">
                        <span style="position: absolute; content: ''; height: 20px; width: 20px; left: <?= $p['is_active'] ? '26px' : '3px' ?>; bottom: 3px; background-color: white; transition: 0.3s; border-radius: 50%;"></span>
                    </span>
                </label>
            </td>
            <td>
                <a href="index.php?page=product_form&id=<?= $p['product_id'] ?>" class="btn btn-outline" style="padding: 4px 8px;" title="Edit">✏️</a>
                <button type="button" class="btn btn-outline" style="padding: 4px 8px; color: red; border-color: #fee2e2;" 
                        onclick="deleteProduct(<?= $p['product_id'] ?>)" title="Delete">🗑</button>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
    <?php if (empty($products)): ?>
    <p style="text-align: center; color: #666; padding: 2rem;">No products found.</p>
    <?php endif; ?>
</div>

<script>
// Show toast notification
function showToast(message, isSuccess = true) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.style.backgroundColor = isSuccess ? '#22c55e' : '#ef4444';
    toast.style.display = 'block';
    toast.style.opacity = '1';
    
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => {
            toast.style.display = 'none';
        }, 300);
    }, 3000);
}

// Toggle product status via AJAX
function toggleStatus(productId, checkbox) {
    const slider = checkbox.nextElementSibling;
    const knob = slider.querySelector('span');
    
    fetch('index.php?page=products', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: `action=toggle_active&product_id=${productId}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Update toggle appearance
            if (data.is_active) {
                slider.style.backgroundColor = '#22c55e';
                knob.style.left = '26px';
                checkbox.checked = true;
            } else {
                slider.style.backgroundColor = '#444';
                knob.style.left = '3px';
                checkbox.checked = false;
            }
            showToast(data.message, true);
        } else {
            // Revert checkbox
            checkbox.checked = !checkbox.checked;
            showToast(data.message, false);
        }
    })
    .catch(error => {
        // Revert checkbox on error
        checkbox.checked = !checkbox.checked;
        showToast('Network error. Please try again.', false);
    });
}

// Delete product via AJAX
function deleteProduct(productId) {
    if (!confirm('Are you sure you want to delete this product?')) {
        return;
    }
    
    fetch('index.php?page=products', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: `action=delete_product&product_id=${productId}`
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Remove row from table with animation
            const row = document.getElementById(`product-row-${productId}`);
            if (row) {
                row.style.transition = 'opacity 0.3s ease';
                row.style.opacity = '0';
                setTimeout(() => row.remove(), 300);
            }
            showToast(data.message, true);
        } else {
            showToast(data.message, false);
        }
    })
    .catch(error => {
        showToast('Network error. Please try again.', false);
    });
}
</script>

<style>
.toggle-switch input:focus + .slider {
    box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.3);
}
</style>
