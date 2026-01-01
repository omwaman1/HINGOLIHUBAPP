<?php
$orderId = $_GET['id'] ?? null;

if (!$orderId) {
    header("Location: index.php?page=orders&error=" . urlencode("No order ID specified"));
    exit;
}

// Fetch Order
$stmt = $db->prepare("SELECT o.*, u.username, u.phone, u.email, 
                      a.name as address_name, a.phone as address_phone, a.address_line1, a.address_line2, a.city, a.state, a.pincode
                      FROM orders o 
                      LEFT JOIN users u ON o.user_id = u.user_id
                      LEFT JOIN user_addresses a ON o.address_id = a.address_id
                      WHERE o.order_id = ?");
$stmt->execute([$orderId]);
$order = $stmt->fetch();

if (!$order) {
    header("Location: index.php?page=orders&error=" . urlencode("Order not found"));
    exit;
}

// Fetch Order Items
$itemStmt = $db->prepare("SELECT oi.*, sp.product_name, sp.image_url 
                          FROM order_items oi 
                          LEFT JOIN shop_products sp ON oi.product_id = sp.product_id
                          WHERE oi.order_id = ?");
$itemStmt->execute([$orderId]);
$items = $itemStmt->fetchAll();
?>

<div class="header">
    <div class="page-title">Order #<?= $orderId ?></div>
    <a href="index.php?page=orders" class="btn btn-outline">← Back to Orders</a>
</div>

<div style="display: grid; grid-template-columns: 2fr 1fr; gap: 1.5rem;">
    <!-- Order Items -->
    <div class="card">
        <h3 style="margin-top: 0;">📦 Order Items</h3>
        <table style="width: 100%;">
            <thead>
                <tr>
                    <th style="width: 60px;"></th>
                    <th>Product</th>
                    <th>Price</th>
                    <th>Qty</th>
                    <th>Total</th>
                </tr>
            </thead>
            <tbody>
            <?php foreach ($items as $item): ?>
            <tr>
                <td>
                    <?php if ($item['image_url']): ?>
                    <img src="<?= $item['image_url'] ?>" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;">
                    <?php endif; ?>
                </td>
                <td><?= htmlspecialchars($item['product_name']) ?></td>
                <td>₹<?= number_format($item['price']) ?></td>
                <td><?= $item['quantity'] ?></td>
                <td><strong>₹<?= number_format($item['price'] * $item['quantity']) ?></strong></td>
            </tr>
            <?php endforeach; ?>
            </tbody>
            <tfoot>
                <tr>
                    <td colspan="4" style="text-align: right;"><strong>Total:</strong></td>
                    <td><strong style="font-size: 1.2rem;">₹<?= number_format($order['total_amount']) ?></strong></td>
                </tr>
            </tfoot>
        </table>
    </div>

    <!-- Order Info -->
    <div>
        <div class="card">
            <h3 style="margin-top: 0;">📋 Order Info</h3>
            <p><strong>Status:</strong> 
                <span style="padding: 2px 8px; border-radius: 99px; background: #e0f2fe; color: #0284c7;">
                    <?= ucfirst($order['order_status'] ?? 'pending') ?>
                </span>
            </p>
            <p><strong>Payment:</strong> <?= ucfirst($order['payment_method'] ?? 'COD') ?> - <?= ucfirst($order['payment_status'] ?? 'pending') ?></p>
            <p><strong>Date:</strong> <?= date('d M Y, h:i A', strtotime($order['created_at'])) ?></p>
            <?php if ($order['notes']): ?>
            <p><strong>Notes:</strong> <?= htmlspecialchars($order['notes']) ?></p>
            <?php endif; ?>
        </div>

        <div class="card">
            <h3 style="margin-top: 0;">👤 Customer</h3>
            <p><strong><?= htmlspecialchars($order['username']) ?></strong></p>
            <p>📞 <?= $order['phone'] ?></p>
            <?php if ($order['email']): ?>
            <p>📧 <?= $order['email'] ?></p>
            <?php endif; ?>
        </div>

        <div class="card">
            <h3 style="margin-top: 0;">📍 Delivery Address</h3>
            <?php if ($order['address_line1']): ?>
            <p><strong><?= htmlspecialchars($order['address_name']) ?></strong> (<?= $order['address_phone'] ?>)</p>
            <p><?= htmlspecialchars($order['address_line1']) ?></p>
            <?php if ($order['address_line2']): ?>
            <p><?= htmlspecialchars($order['address_line2']) ?></p>
            <?php endif; ?>
            <p><?= htmlspecialchars($order['city']) ?>, <?= $order['state'] ?> - <?= $order['pincode'] ?></p>
            <?php else: ?>
            <p style="color: #999;">No address on file</p>
            <?php endif; ?>
        </div>
    </div>
</div>
