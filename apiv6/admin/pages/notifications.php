<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'send_notification':
                    $title = $_POST['title'];
                    $message = $_POST['message'];
                    $targetType = $_POST['target_type'] ?? 'all';
                    $targetUserId = !empty($_POST['target_user_id']) ? $_POST['target_user_id'] : null;
                    
                    if ($targetType === 'all') {
                        // Send to all users
                        $users = $db->query("SELECT user_id FROM users")->fetchAll();
                        foreach ($users as $user) {
                            $stmt = $db->prepare("INSERT INTO notifications (user_id, title, message, type, is_read) VALUES (?, ?, ?, 'admin', 0)");
                            $stmt->execute([$user['user_id'], $title, $message]);
                        }
                        header("Location: index.php?page=notifications&msg=" . urlencode("Notification sent to " . count($users) . " users!"));
                    } else if ($targetUserId) {
                        // Send to specific user
                        $stmt = $db->prepare("INSERT INTO notifications (user_id, title, message, type, is_read) VALUES (?, ?, ?, 'admin', 0)");
                        $stmt->execute([$targetUserId, $title, $message]);
                        header("Location: index.php?page=notifications&msg=" . urlencode("Notification sent!"));
                    }
                    exit;
                    
                case 'delete_notification':
                    $id = $_POST['notification_id'];
                    $db->prepare("DELETE FROM notifications WHERE notification_id = ?")->execute([$id]);
                    header("Location: index.php?page=notifications&msg=" . urlencode("Notification deleted!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

// Fetch Recent Notifications
$notifications = [];
$notifError = null;
try {
    // First try to get column info
    $notifications = $db->query("SELECT * FROM notifications ORDER BY created_at DESC LIMIT 50")->fetchAll();
} catch (Exception $e) {
    $notifError = $e->getMessage();
}
?>

<div class="header">
    <div class="page-title">🔔 Notifications</div>
</div>

<!-- Send New Notification -->
<div class="card">
    <h3 style="margin-top: 0;">📤 Send New Notification</h3>
    <form method="POST">
        <input type="hidden" name="action" value="send_notification">
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1rem; margin-bottom: 1rem;">
            <label>
                <strong>Title *</strong>
                <input type="text" name="title" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            <label>
                <strong>Target</strong>
                <select name="target_type" id="targetType" onchange="toggleUserField()" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
                    <option value="all">All Users</option>
                    <option value="specific">Specific User</option>
                </select>
            </label>
            <label id="userIdField" style="display: none;">
                <strong>User ID</strong>
                <input type="number" name="target_user_id" placeholder="Enter User ID" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
        </div>
        <label>
            <strong>Message *</strong>
            <textarea name="message" required rows="3" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;"></textarea>
        </label>
        <button type="submit" class="btn btn-primary" style="margin-top: 1rem;">📤 Send Notification</button>
    </form>
</div>

<script>
function toggleUserField() {
    document.getElementById('userIdField').style.display = 
        document.getElementById('targetType').value === 'specific' ? 'block' : 'none';
}
</script>

<!-- Recent Notifications -->
<?php if ($notifError): ?>
<div class="alert alert-error">Error loading notifications: <?= htmlspecialchars($notifError) ?></div>
<?php elseif (empty($notifications)): ?>
<div class="card" style="text-align: center; padding: 3rem;">
    <div style="font-size: 3rem; margin-bottom: 1rem;">🔔</div>
    <h3>No Notifications Yet</h3>
    <p style="color: var(--text-light);">Send your first notification using the form above!</p>
</div>
<?php else: ?>
<div class="card" style="overflow-x: auto;">
    <h3 style="margin-top: 0;">📋 Recent Notifications (<?= count($notifications) ?>)</h3>
    <table style="min-width: 700px;">
        <thead>
            <tr>
                <th>Date</th>
                <th>User</th>
                <th>Title</th>
                <th>Message</th>
                <th>Read</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($notifications as $n): ?>
        <tr>
            <td><?= date('d M, h:i A', strtotime($n['created_at'] ?? $n['sent_at'] ?? 'now')) ?></td>
            <td><?= $n['user_id'] ?? $n['recipient_id'] ?? 'All' ?></td>
            <td><strong><?= htmlspecialchars($n['title'] ?? $n['subject'] ?? '-') ?></strong></td>
            <td style="max-width: 300px;"><?= htmlspecialchars($n['message'] ?? $n['body'] ?? '') ?></td>
            <td><?= ($n['is_read'] ?? $n['read']) ? '✓' : '-' ?></td>
            <td>
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete?');">
                    <input type="hidden" name="action" value="delete_notification">
                    <input type="hidden" name="notification_id" value="<?= $n['notification_id'] ?? $n['id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: red;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
<?php endif; ?>
