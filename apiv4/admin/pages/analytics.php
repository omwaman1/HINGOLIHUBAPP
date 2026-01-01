<?php
// Analytics Data
$totalListings = $db->query("SELECT COUNT(*) FROM listings")->fetchColumn();
$totalUsers = $db->query("SELECT COUNT(*) FROM users")->fetchColumn();
$totalOrders = $db->query("SELECT COUNT(*) FROM orders")->fetchColumn();
$pendingOrders = $db->query("SELECT COUNT(*) FROM orders WHERE order_status = 'pending'")->fetchColumn();

// Listings by type
$listingsByType = $db->query("SELECT listing_type, COUNT(*) as count FROM listings GROUP BY listing_type")->fetchAll();

// Recent signups
$recentUsers = $db->query("SELECT DATE(created_at) as date, COUNT(*) as count FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) GROUP BY DATE(created_at) ORDER BY date")->fetchAll();

// Top cities
$topCities = $db->query("SELECT city, COUNT(*) as count FROM listings GROUP BY city ORDER BY count DESC LIMIT 5")->fetchAll();

// Category distribution
$categories = $db->query("SELECT c.name, COUNT(l.listing_id) as count FROM categories c LEFT JOIN listings l ON c.category_id = l.category_id WHERE c.parent_id IS NULL GROUP BY c.category_id ORDER BY count DESC LIMIT 10")->fetchAll();
?>

<div class="header">
    <div class="page-title">📈 Analytics</div>
</div>

<!-- Stats Cards -->
<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; margin-bottom: 1.5rem;">
    <div class="card" style="text-align: center;">
        <div style="font-size: 2rem; font-weight: bold; color: var(--primary);"><?= number_format($totalListings) ?></div>
        <div style="color: var(--text-light);">Total Listings</div>
    </div>
    <div class="card" style="text-align: center;">
        <div style="font-size: 2rem; font-weight: bold; color: var(--success);"><?= number_format($totalUsers) ?></div>
        <div style="color: var(--text-light);">Registered Users</div>
    </div>
    <div class="card" style="text-align: center;">
        <div style="font-size: 2rem; font-weight: bold; color: var(--warning);"><?= number_format($totalOrders) ?></div>
        <div style="color: var(--text-light);">Total Orders</div>
    </div>
    <div class="card" style="text-align: center;">
        <div style="font-size: 2rem; font-weight: bold; color: var(--danger);"><?= number_format($pendingOrders) ?></div>
        <div style="color: var(--text-light);">Pending Orders</div>
    </div>
</div>

<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1.5rem;">
    <!-- Listings by Type -->
    <div class="card">
        <h3 style="margin-top: 0;">📋 Listings by Type</h3>
        <?php foreach ($listingsByType as $lt): ?>
        <div style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid var(--border);">
            <span><?= ucfirst($lt['listing_type']) ?></span>
            <strong><?= $lt['count'] ?></strong>
        </div>
        <?php endforeach; ?>
    </div>
    
    <!-- Top Cities -->
    <div class="card">
        <h3 style="margin-top: 0;">🏙️ Top Cities</h3>
        <?php foreach ($topCities as $city): ?>
        <div style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid var(--border);">
            <span><?= htmlspecialchars($city['city']) ?></span>
            <strong><?= $city['count'] ?></strong>
        </div>
        <?php endforeach; ?>
    </div>
    
    <!-- Top Categories -->
    <div class="card">
        <h3 style="margin-top: 0;">📁 Top Categories</h3>
        <?php foreach ($categories as $cat): ?>
        <div style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid var(--border);">
            <span><?= htmlspecialchars($cat['name']) ?></span>
            <strong><?= $cat['count'] ?></strong>
        </div>
        <?php endforeach; ?>
    </div>
</div>
