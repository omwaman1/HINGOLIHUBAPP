<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'add_user':
                    $username = $_POST['username'];
                    $phone = $_POST['phone'];
                    $email = !empty($_POST['email']) ? $_POST['email'] : null;
                    $password = !empty($_POST['password']) ? password_hash($_POST['password'], PASSWORD_DEFAULT) : null;
                    $is_verified = isset($_POST['is_verified']) ? 1 : 0;
                    
                    // Handle avatar upload
                    $avatar_url = null;
                    if (isset($_FILES['avatar']) && $_FILES['avatar']['error'] === UPLOAD_ERR_OK) {
                        $avatar_url = uploadImage($_FILES['avatar'], 'avatars');
                    }
                    
                    $stmt = $db->prepare("INSERT INTO users (username, phone, email, password_hash, is_verified, avatar_url) VALUES (?, ?, ?, ?, ?, ?)");
                    $stmt->execute([$username, $phone, $email, $password, $is_verified, $avatar_url]);
                    $newId = $db->lastInsertId();
                    
                    header("Location: index.php?page=user_form&id=$newId&msg=" . urlencode("User added successfully!"));
                    exit;

                case 'edit_user':
                    $userId = $_POST['user_id'];
                    $username = $_POST['username'];
                    $phone = $_POST['phone'];
                    $email = !empty($_POST['email']) ? $_POST['email'] : null;
                    $is_verified = isset($_POST['is_verified']) ? 1 : 0;
                    
                    // Handle avatar upload
                    $avatar_url = $_POST['existing_avatar'] ?? null;
                    if (isset($_FILES['avatar']) && $_FILES['avatar']['error'] === UPLOAD_ERR_OK) {
                        $avatar_url = uploadImage($_FILES['avatar'], 'avatars');
                    }
                    
                    $sql = "UPDATE users SET username = ?, phone = ?, email = ?, is_verified = ?, avatar_url = ?";
                    $params = [$username, $phone, $email, $is_verified, $avatar_url];
                    
                    if (!empty($_POST['new_password'])) {
                        $sql .= ", password_hash = ?";
                        $params[] = password_hash($_POST['new_password'], PASSWORD_DEFAULT);
                    }
                    
                    $sql .= " WHERE user_id = ?";
                    $params[] = $userId;
                    
                    $db->prepare($sql)->execute($params);
                    header("Location: index.php?page=user_form&id=$userId&msg=" . urlencode("User updated successfully!"));
                    exit;
                    
                case 'delete_address':
                    $addressId = $_POST['address_id'];
                    $userId = $_POST['user_id'];
                    $db->prepare("DELETE FROM user_addresses WHERE address_id = ?")->execute([$addressId]);
                    header("Location: index.php?page=user_form&id=$userId&msg=" . urlencode("Address deleted!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

$editUser = null;
$addresses = [];

if (isset($_GET['id'])) {
    $id = $_GET['id'];
    $stmt = $db->prepare("SELECT * FROM users WHERE user_id = ?");
    $stmt->execute([$id]);
    $editUser = $stmt->fetch();
    
    if ($editUser) {
        $addrStmt = $db->prepare("SELECT * FROM user_addresses WHERE user_id = ? ORDER BY is_default DESC");
        $addrStmt->execute([$id]);
        $addresses = $addrStmt->fetchAll();
    }
}
?>

<div class="header">
    <div class="page-title"><?= $editUser ? 'Edit User' : 'Add New User' ?></div>
    <a href="index.php?page=users" class="btn btn-outline">← Back to Users</a>
</div>

<div class="card">
    <form method="POST" enctype="multipart/form-data">
        <input type="hidden" name="action" value="<?= $editUser ? 'edit_user' : 'add_user' ?>">
        <?php if ($editUser): ?>
        <input type="hidden" name="user_id" value="<?= $editUser['user_id'] ?>">
        <input type="hidden" name="existing_avatar" value="<?= $editUser['avatar_url'] ?>">
        <?php endif; ?>

        <div style="display: flex; gap: 2rem; align-items: flex-start; flex-wrap: wrap;">
            <!-- Avatar Section -->
            <div style="text-align: center;">
                <?php if (!empty($editUser['avatar_url'])): ?>
                <img src="<?= $editUser['avatar_url'] ?>" style="width: 100px; height: 100px; border-radius: 50%; object-fit: cover; margin-bottom: 0.5rem; display: block;">
                <?php else: ?>
                <div style="width: 100px; height: 100px; border-radius: 50%; background: #eee; display: flex; align-items: center; justify-content: center; margin-bottom: 0.5rem;">No Img</div>
                <?php endif; ?>
                <input type="file" name="avatar" style="width: 200px;">
            </div>
            
            <!-- Details Section -->
            <div style="flex: 1; min-width: 300px;">
                <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 1rem; margin-bottom: 1rem;">
                    <label>
                        <strong>Username *</strong>
                        <input type="text" name="username" value="<?= $editUser['username'] ?? '' ?>" required style="width: 100%; padding: 0.5rem;">
                    </label>
                    
                    <label>
                        <strong>Phone *</strong>
                        <input type="text" name="phone" value="<?= $editUser['phone'] ?? '' ?>" required style="width: 100%; padding: 0.5rem;">
                    </label>
                    
                    <label>
                        <strong>Email</strong>
                        <input type="email" name="email" value="<?= $editUser['email'] ?? '' ?>" style="width: 100%; padding: 0.5rem;">
                    </label>
                    
                    <label>
                        <strong><?= $editUser ? 'New Password (leave blank to keep)' : 'Password' ?></strong>
                        <input type="password" name="<?= $editUser ? 'new_password' : 'password' ?>" style="width: 100%; padding: 0.5rem;">
                    </label>
                </div>
                
                <label style="display: block; margin-top: 1rem;">
                    <input type="checkbox" name="is_verified" <?= (!isset($editUser) || $editUser['is_verified']) ? 'checked' : '' ?>> Verified User
                </label>
                
                <button type="submit" class="btn btn-primary" style="margin-top: 1.5rem;"><?= $editUser ? 'Update User' : 'Create User' ?></button>
            </div>
        </div>
    </form>
</div>

<?php if ($editUser): ?>
<div class="card">
    <h3 style="margin-top: 0;">📍 Addresses</h3>
    <?php if (empty($addresses)): ?>
        <p style="color: #666;">No addresses found for this user.</p>
    <?php else: ?>
        <table style="width: 100%;">
            <thead>
                <tr>
                    <th>Type/Name</th>
                    <th>Address</th>
                    <th>City/State</th>
                    <th>Default</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <?php foreach ($addresses as $addr): ?>
                <tr>
                    <td><?= htmlspecialchars($addr['name']) ?></td>
                    <td><?= htmlspecialchars($addr['address_line1']) ?><br><small><?= htmlspecialchars($addr['address_line2']) ?></small></td>
                    <td><?= htmlspecialchars($addr['city']) ?>, <?= htmlspecialchars($addr['state']) ?> - <?= htmlspecialchars($addr['pincode']) ?></td>
                    <td><?= $addr['is_default'] ? '⭐' : '' ?></td>
                    <td>
                        <form method="POST" onsubmit="return confirm('Delete address?');">
                            <input type="hidden" name="action" value="delete_address">
                            <input type="hidden" name="address_id" value="<?= $addr['address_id'] ?>">
                            <input type="hidden" name="user_id" value="<?= $editUser['user_id'] ?>">
                            <button type="submit" class="btn btn-outline" style="color: red; padding: 2px 6px;">×</button>
                        </form>
                    </td>
                </tr>
                <?php endforeach; ?>
            </tbody>
        </table>
    <?php endif; ?>
</div>
<?php endif; ?>
