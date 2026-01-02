<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'verify_listing':
                    $id = $_POST['listing_id'];
                    $stmt = $db->prepare("UPDATE listings SET is_verified = 1, verified_at = NOW() WHERE listing_id = ?");
                    $stmt->execute([$id]);
                    header("Location: index.php?page=listings&msg=" . urlencode("Listing verified!"));
                    exit;

                case 'delete_listing':
                    $id = $_POST['listing_id'];
                    // Delete type-specific data first
                    $db->prepare("DELETE FROM services_listings WHERE listing_id = ?")->execute([$id]);
                    $db->prepare("DELETE FROM job_listings WHERE listing_id = ?")->execute([$id]);
                    $db->prepare("DELETE FROM business_listings WHERE listing_id = ?")->execute([$id]);
                    $db->prepare("DELETE FROM listing_images WHERE listing_id = ?")->execute([$id]);
                    // Delete main listing
                    $stmt = $db->prepare("DELETE FROM listings WHERE listing_id = ?");
                    $stmt->execute([$id]);
                    header("Location: index.php?page=listings&msg=" . urlencode("Listing deleted successfully!"));
                    exit;

                case 'transfer_listing':
                    $listingId = $_POST['listing_id'];
                    $newUserId = $_POST['new_user_id'];
                    
                    // Verify user exists
                    $userCheck = $db->prepare("SELECT user_id, username FROM users WHERE user_id = ?");
                    $userCheck->execute([$newUserId]);
                    $newUser = $userCheck->fetch();
                    
                    if (!$newUser) {
                        header("Location: index.php?page=listings&error=" . urlencode("User ID $newUserId not found!"));
                        exit;
                    }
                    
                    // Transfer listing
                    $stmt = $db->prepare("UPDATE listings SET user_id = ? WHERE listing_id = ?");
                    $stmt->execute([$newUserId, $listingId]);
                    header("Location: index.php?page=listings&msg=" . urlencode("Listing transferred to {$newUser['username']}"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

// Fetch Cities for Filter
try {
    $cities = $db->query("SELECT DISTINCT name FROM cities ORDER BY name")->fetchAll();
} catch (Exception $e) {
    $cities = [];
}

// Search & Filters
$search = $_GET['search'] ?? '';
$filterType = $_GET['filter_type'] ?? '';
$filterStatus = $_GET['filter_status'] ?? '';
$filterCity = $_GET['filter_city'] ?? '';

// Build Query
$sql = "SELECT l.*, c.name as category_name, u.username,
        jl.salary_min, service.experience_years, bl.business_name
        FROM listings l
        LEFT JOIN categories c ON l.category_id = c.category_id
        LEFT JOIN users u ON l.user_id = u.user_id
        LEFT JOIN job_listings jl ON l.listing_id = jl.listing_id
        LEFT JOIN services_listings service ON l.listing_id = service.listing_id
        LEFT JOIN business_listings bl ON l.listing_id = bl.listing_id
        WHERE 1=1";
$params = [];

if ($search) {
    $sql .= " AND (l.title LIKE ? OR l.description LIKE ?)";
    $params[] = "%$search%";
    $params[] = "%$search%";
}
if ($filterType) {
    $sql .= " AND l.listing_type = ?";
    $params[] = $filterType;
}
if ($filterStatus) {
    $sql .= " AND l.status = ?";
    $params[] = $filterStatus;
}
if ($filterCity) {
    $sql .= " AND l.city = ?";
    $params[] = $filterCity;
}

$sql .= " ORDER BY l.created_at DESC LIMIT 100";
$stmt = $db->prepare($sql);
$stmt->execute($params);
$listings = $stmt->fetchAll();
?>

<div class="header">
    <div class="page-title">Manage Listings</div>
    <a href="index.php?page=listing_form" class="btn btn-primary">➕ Add New Listing</a>
</div>

<!-- Search Filter -->
<div class="card">
    <form method="GET" style="display: flex; gap: 10px; flex-wrap: wrap; align-items: end;">
        <input type="hidden" name="page" value="listings">
        <label style="flex: 2; min-width: 200px;">
            Search
            <input type="text" name="search" value="<?= htmlspecialchars($search) ?>" placeholder="Search title or description...">
        </label>
        <label style="flex: 1; min-width: 120px;">
            Type
            <select name="filter_type">
                <option value="">All Types</option>
                <option value="services" <?= $filterType === 'services' ? 'selected' : '' ?>>Services</option>
                <option value="selling" <?= $filterType === 'selling' ? 'selected' : '' ?>>Selling</option>
                <option value="business" <?= $filterType === 'business' ? 'selected' : '' ?>>Business</option>
                <option value="jobs" <?= $filterType === 'jobs' ? 'selected' : '' ?>>Jobs</option>
            </select>
        </label>
        <label style="flex: 1; min-width: 120px;">
            Status
            <select name="filter_status">
                <option value="">All Status</option>
                <option value="active" <?= $filterStatus === 'active' ? 'selected' : '' ?>>Active</option>
                <option value="pending" <?= $filterStatus === 'pending' ? 'selected' : '' ?>>Pending</option>
                <option value="draft" <?= $filterStatus === 'draft' ? 'selected' : '' ?>>Draft</option>
                <option value="rejected" <?= $filterStatus === 'rejected' ? 'selected' : '' ?>>Rejected</option>
            </select>
        </label>
        <label style="flex: 1; min-width: 120px;">
            City
            <select name="filter_city">
                <option value="">All Cities</option>
                <?php foreach ($cities as $c): ?>
                <option value="<?= $c['name'] ?>" <?= $filterCity === $c['name'] ? 'selected' : '' ?>><?= htmlspecialchars($c['name']) ?></option>
                <?php endforeach; ?>
            </select>
        </label>
        <button type="submit" class="btn btn-primary" style="height: 42px;">Filter</button>
        <?php if ($search || $filterType || $filterStatus || $filterCity): ?>
        <a href="index.php?page=listings" class="btn btn-outline" style="height: 42px;">Clear</a>
        <?php endif; ?>
    </form>
</div>

<!-- Listings Table -->
<div class="card" style="overflow-x: auto;">
    <div style="margin-bottom: 1rem; color: var(--text-light);">
        Showing <?= count($listings) ?> listings
    </div>
    <table style="min-width: 1000px;">
        <thead>
            <tr>
                <th style="width:40px;">ID</th>
                <th style="width:50px;">Image</th>
                <th style="max-width:200px;">Title</th>
                <th>Type</th>
                <th>Category</th>
                <th>Price/Salary</th>
                <th>City</th>
                <th>Owner</th>
                <th>Status</th>
                <th style="width:140px;">Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($listings as $l): ?>
        <tr>
            <td><?= $l['listing_id'] ?></td>
            <td>
                <?php if($l['main_image_url']): ?>
                <img src="<?= $l['main_image_url'] ?>" style="width:40px;height:40px;object-fit:cover;border-radius:4px;">
                <?php else: ?>
                <span style="display:block;width:40px;height:40px;background:#f1f5f9;border-radius:4px;"></span>
                <?php endif; ?>
            </td>
            <td title="<?= htmlspecialchars($l['title']) ?>">
                <div style="font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 200px;">
                    <?= htmlspecialchars($l['title']) ?>
                </div>
                <div>
                    <?php if($l['is_verified']): ?><span style="color: green; font-size: 0.8em;">✓ Verified</span><?php endif; ?>
                    <?php if($l['is_featured']): ?><span style="color: orange; font-size: 0.8em;">★ Featured</span><?php endif; ?>
                </div>
            </td>
            <td><span style="background: #e0f2fe; color: #0284c7; padding: 2px 6px; border-radius: 4px; font-size: 0.8em;"><?= $l['listing_type'] ?></span></td>
            <td><?= htmlspecialchars($l['category_name']) ?></td>
            <td>
                <?php if ($l['listing_type'] === 'jobs' && $l['salary_min']): ?>
                    ₹<?= number_format($l['salary_min']/1000) ?>k
                <?php elseif ($l['price']): ?>
                    ₹<?= number_format($l['price']) ?>
                <?php else: ?>
                    -
                <?php endif; ?>
            </td>
            <td><?= htmlspecialchars($l['city']) ?></td>
            <td>
                <span title="User ID: <?= $l['user_id'] ?>"><?= htmlspecialchars($l['username'] ?? 'ID:'.$l['user_id']) ?></span>
            </td>
            <td>
                <?php
                $statusColor = match($l['status']) {
                    'active' => '#dcfce7; color: #166534',
                    'pending' => '#fef3c7; color: #d97706',
                    'rejected' => '#fee2e2; color: #dc2626',
                    default => '#f1f5f9; color: #475569'
                };
                ?>
                <span style="background: <?= $statusColor ?>; padding: 2px 8px; border-radius: 99px; font-size: 0.85em; font-weight: 500; text-transform: capitalize;">
                    <?= $l['status'] ?>
                </span>
            </td>
            <td style="display: flex; gap: 5px;">
                <a href="index.php?page=listing_form&id=<?= $l['listing_id'] ?>" class="btn btn-outline" style="padding: 4px 8px;" title="Edit">✏️</a>
                
                <?php if (!$l['is_verified']): ?>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="verify_listing">
                    <input type="hidden" name="listing_id" value="<?= $l['listing_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: green;" title="Verify">✓</button>
                </form>
                <?php endif; ?>
               
                <form method="POST" style="display:inline;" onsubmit="return confirm('Are you sure you want to delete this listing?');">
                    <input type="hidden" name="action" value="delete_listing">
                    <input type="hidden" name="listing_id" value="<?= $l['listing_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: red; border-color: #fee2e2;" title="Delete">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
