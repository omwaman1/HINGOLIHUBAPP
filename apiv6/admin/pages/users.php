<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'delete_user':
                    $userId = $_POST['user_id'];
                    // Optional: Check if user has listings or critical data before deleting?
                    // For now, simple delete (database FKs might restrict this)
                    $stmt = $db->prepare("DELETE FROM users WHERE user_id = ?");
                    $stmt->execute([$userId]);
                    header("Location: index.php?page=users&msg=" . urlencode("User deleted successfully!"));
                    exit;
                
                case 'toggle_verified':
                    $userId = $_POST['user_id'];
                    $stmt = $db->prepare("UPDATE users SET is_verified = NOT is_verified WHERE user_id = ?");
                    $stmt->execute([$userId]);
                    header("Location: index.php?page=users&msg=" . urlencode("Verification status updated!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

// Fetch Users
$search = $_GET['search'] ?? '';
$sql = "SELECT * FROM users WHERE 1=1";
$params = [];

if ($search) {
    $sql .= " AND (username LIKE ? OR phone LIKE ? OR email LIKE ?)";
    $params[] = "%$search%";
    $params[] = "%$search%";
    $params[] = "%$search%";
}

$sql .= " ORDER BY created_at DESC LIMIT 50";
$stmt = $db->prepare($sql);
$stmt->execute($params);
$users = $stmt->fetchAll();
?>

<div class="header">
    <div class="page-title">Manage Users</div>
    <a href="index.php?page=user_form" class="btn btn-primary">➕ Add User</a>
</div>

<div class="card">
    <form method="GET">
        <input type="hidden" name="page" value="users">
        <div style="display: flex; gap: 10px;">
            <input type="text" name="search" value="<?= htmlspecialchars($search) ?>" placeholder="Search by name, phone or email..." style="flex: 1; padding: 0.5rem;">
            <button type="submit" class="btn btn-primary">Search</button>
        </div>
    </form>
</div>

<div class="card" style="overflow-x: auto;">
    <table style="min-width: 800px;">
        <thead>
            <tr>
                <th style="width: 50px;">ID</th>
                <th style="width: 50px;">Avatar</th>
                <th>Username</th>
                <th>Phone</th>
                <th>Email</th>
                <th>Verified</th>
                <th>Joined</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($users as $u): ?>
        <tr>
            <td><?= $u['user_id'] ?></td>
            <td>
                <?php if ($u['avatar_url']): ?>
                <img src="<?= $u['avatar_url'] ?>" style="width: 32px; height: 32px; border-radius: 50%; object-fit: cover;">
                <?php else: ?>
                <div style="width: 32px; height: 32px; border-radius: 50%; background: #ddd; display: flex; align-items: center; justify-content: center; font-size: 10px;">NA</div>
                <?php endif; ?>
            </td>
            <td>
                <strong><?= htmlspecialchars($u['username']) ?></strong>
            </td>
            <td><?= htmlspecialchars($u['phone']) ?></td>
            <td><?= htmlspecialchars($u['email'] ?? '-') ?></td>
            <td>
                <?php if($u['is_verified']): ?>
                <span style="color: green;">✓ Verified</span>
                <?php else: ?>
                <span style="color: #999;">-</span>
                <?php endif; ?>
            </td>
            <td><?= date('d M Y', strtotime($u['created_at'])) ?></td>
            <td>
                <a href="index.php?page=user_form&id=<?= $u['user_id'] ?>" class="btn btn-outline" style="padding: 4px 8px;">✏️</a>
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete this user?');">
                    <input type="hidden" name="action" value="delete_user">
                    <input type="hidden" name="user_id" value="<?= $u['user_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: red; border-color: #fee2e2;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
