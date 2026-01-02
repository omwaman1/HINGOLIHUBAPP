<?php
// Fetch Favorites with details
$favorites = $db->query("SELECT f.*, u.username, l.title as listing_title, l.listing_type, l.main_image_url
                         FROM favorites f
                         LEFT JOIN users u ON f.user_id = u.user_id
                         LEFT JOIN listings l ON f.listing_id = l.listing_id
                         ORDER BY f.created_at DESC
                         LIMIT 100")->fetchAll();

// Group by listing to see popular items
$popular = $db->query("SELECT l.listing_id, l.title, l.listing_type, COUNT(f.favorite_id) as fav_count
                       FROM favorites f
                       JOIN listings l ON f.listing_id = l.listing_id
                       GROUP BY f.listing_id
                       ORDER BY fav_count DESC
                       LIMIT 10")->fetchAll();
?>

<div class="header">
    <div class="page-title">❤️ User Favorites</div>
</div>

<!-- Popular Listings -->
<div class="card">
    <h3 style="margin-top: 0;">🔥 Most Favorited Listings</h3>
    <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
        <?php foreach ($popular as $p): ?>
        <div style="background: #f8fafc; padding: 0.75rem 1rem; border-radius: 8px; display: flex; gap: 0.5rem; align-items: center;">
            <span style="font-size: 1.5rem; font-weight: bold; color: var(--primary);"><?= $p['fav_count'] ?></span>
            <div>
                <div style="font-weight: 500;"><?= htmlspecialchars($p['title']) ?></div>
                <small style="color: #666;"><?= $p['listing_type'] ?></small>
            </div>
        </div>
        <?php endforeach; ?>
    </div>
</div>

<!-- All Favorites -->
<div class="card" style="overflow-x: auto;">
    <h3 style="margin-top: 0;">📋 Recent Favorites</h3>
    <table style="min-width: 700px;">
        <thead>
            <tr>
                <th>Date</th>
                <th>User</th>
                <th>Listing</th>
                <th>Type</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($favorites as $f): ?>
        <tr>
            <td><?= date('d M Y, h:i A', strtotime($f['created_at'])) ?></td>
            <td><?= htmlspecialchars($f['username'] ?? 'User #' . $f['user_id']) ?></td>
            <td>
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <?php if ($f['main_image_url']): ?>
                    <img src="<?= $f['main_image_url'] ?>" style="width: 40px; height: 40px; object-fit: cover; border-radius: 4px;">
                    <?php endif; ?>
                    <?= htmlspecialchars($f['listing_title']) ?>
                </div>
            </td>
            <td><span class="badge"><?= $f['listing_type'] ?></span></td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
