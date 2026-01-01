<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'update_status':
                    $orderId = $_POST['order_id'];
                    $status = $_POST['status'];
                    $stmt = $db->prepare("UPDATE orders SET order_status = ?, updated_at = NOW() WHERE order_id = ?");
                    $stmt->execute([$status, $orderId]);
                    header("Location: index.php?page=orders&msg=" . urlencode("Order status updated to $status!"));
                    exit;
                    
                case 'delete_order':
                    $orderId = $_POST['order_id'];
                    $db->prepare("DELETE FROM order_items WHERE order_id = ?")->execute([$orderId]);
                    $db->prepare("DELETE FROM orders WHERE order_id = ?")->execute([$orderId]);
                    header("Location: index.php?page=orders&msg=" . urlencode("Order deleted!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

// Filters
$filterStatus = $_GET['status'] ?? '';
$search = $_GET['search'] ?? '';

// Fetch Orders
$sql = "SELECT o.*, u.username, u.phone,
        (SELECT SUM(quantity) FROM order_items WHERE order_id = o.order_id) as item_count
        FROM orders o 
        LEFT JOIN users u ON o.user_id = u.user_id
        WHERE 1=1";
$params = [];

if ($filterStatus) {
    $sql .= " AND o.order_status = ?";
    $params[] = $filterStatus;
}
if ($search) {
    $sql .= " AND (o.order_id LIKE ? OR u.username LIKE ? OR u.phone LIKE ?)";
    $params[] = "%$search%";
    $params[] = "%$search%";
    $params[] = "%$search%";
}

$sql .= " ORDER BY o.created_at DESC LIMIT 100";
$stmt = $db->prepare($sql);
$stmt->execute($params);
$orders = $stmt->fetchAll();
?>

<div class="header">
    <div class="page-title">Manage Orders</div>
</div>

<!-- Filters -->
<div class="card">
    <form method="GET" style="display: flex; gap: 10px; flex-wrap: wrap;">
        <input type="hidden" name="page" value="orders">
        <input type="text" name="search" value="<?= htmlspecialchars($search) ?>" placeholder="Order ID, Username, Phone..." style="flex: 2; min-width: 200px; padding: 0.5rem;">
        <select name="status" style="padding: 0.5rem;">
            <option value="">All Status</option>
            <option value="pending" <?= $filterStatus === 'pending' ? 'selected' : '' ?>>Pending</option>
            <option value="confirmed" <?= $filterStatus === 'confirmed' ? 'selected' : '' ?>>Confirmed</option>
            <option value="processing" <?= $filterStatus === 'processing' ? 'selected' : '' ?>>Processing</option>
            <option value="shipped" <?= $filterStatus === 'shipped' ? 'selected' : '' ?>>Shipped</option>
            <option value="delivered" <?= $filterStatus === 'delivered' ? 'selected' : '' ?>>Delivered</option>
            <option value="cancelled" <?= $filterStatus === 'cancelled' ? 'selected' : '' ?>>Cancelled</option>
        </select>
        <button type="submit" class="btn btn-primary">Filter</button>
    </form>
</div>

<!-- Orders Table -->
<div class="card" style="overflow-x: auto;">
    <div style="margin-bottom: 1rem; color: var(--text-light);">
        Showing <?= count($orders) ?> orders
    </div>
    <table style="min-width: 900px;">
        <thead>
            <tr>
                <th>Order #</th>
                <th>Customer</th>
                <th>Items</th>
                <th>Total</th>
                <th>Payment</th>
                <th>Status</th>
                <th>Date</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($orders as $o): ?>
        <tr>
            <td><strong>#<?= $o['order_id'] ?></strong></td>
            <td>
                <?= htmlspecialchars($o['username']) ?><br>
                <small style="color: #666;"><?= $o['phone'] ?></small>
            </td>
            <td><?= $o['item_count'] ?? 0 ?> items</td>
            <td><strong>₹<?= number_format($o['total_amount']) ?></strong></td>
            <td>
                <?php
                $payColor = match($o['payment_status'] ?? 'pending') {
                    'paid' => '#dcfce7; color: #166534',
                    'pending' => '#fef3c7; color: #d97706',
                    'failed' => '#fee2e2; color: #dc2626',
                    default => '#f1f5f9; color: #475569'
                };
                ?>
                <span style="padding: 2px 8px; border-radius: 99px; font-size: 0.8em; background: <?= $payColor ?>;">
                    <?= ucfirst($o['payment_status'] ?? 'pending') ?>
                </span>
            </td>
            <td>
                <form method="POST" style="display: inline;">
                    <input type="hidden" name="action" value="update_status">
                    <input type="hidden" name="order_id" value="<?= $o['order_id'] ?>">
                    <select name="status" onchange="this.form.submit()" style="padding: 4px; font-size: 0.85em;">
                        <?php foreach (['pending', 'confirmed', 'processing', 'shipped', 'delivered', 'cancelled'] as $s): ?>
                        <option value="<?= $s ?>" <?= ($o['order_status'] ?? '') === $s ? 'selected' : '' ?>><?= ucfirst($s) ?></option>
                        <?php endforeach; ?>
                    </select>
                </form>
            </td>
            <td><?= date('d M Y, h:i A', strtotime($o['created_at'])) ?></td>
            <td>
                <a href="index.php?page=order_detail&id=<?= $o['order_id'] ?>" class="btn btn-outline" style="padding: 4px 8px;">👁</a>
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete this order?');">
                    <input type="hidden" name="action" value="delete_order">
                    <input type="hidden" name="order_id" value="<?= $o['order_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: red; border-color: #fee2e2;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
