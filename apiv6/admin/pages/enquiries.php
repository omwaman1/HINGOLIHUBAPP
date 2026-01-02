<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'mark_read':
                    $id = $_POST['enquiry_id'];
                    $db->prepare("UPDATE enquiries SET status = 'read' WHERE enquiry_id = ?")->execute([$id]);
                    header("Location: index.php?page=enquiries&msg=" . urlencode("Marked as read!"));
                    exit;
                    
                case 'mark_responded':
                    $id = $_POST['enquiry_id'];
                    $db->prepare("UPDATE enquiries SET status = 'responded', responded_at = NOW() WHERE enquiry_id = ?")->execute([$id]);
                    header("Location: index.php?page=enquiries&msg=" . urlencode("Marked as responded!"));
                    exit;
                    
                case 'delete_enquiry':
                    $id = $_POST['enquiry_id'];
                    $db->prepare("DELETE FROM enquiries WHERE enquiry_id = ?")->execute([$id]);
                    header("Location: index.php?page=enquiries&msg=" . urlencode("Enquiry deleted!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

// Fetch Enquiries
$filterStatus = $_GET['status'] ?? '';
$sql = "SELECT e.*, l.title as listing_title, u.username as sender_name, u.phone as sender_phone
        FROM enquiries e
        LEFT JOIN listings l ON e.listing_id = l.listing_id
        LEFT JOIN users u ON e.user_id = u.user_id
        WHERE 1=1";
$params = [];

if ($filterStatus) {
    $sql .= " AND e.status = ?";
    $params[] = $filterStatus;
}

$sql .= " ORDER BY e.created_at DESC LIMIT 100";
$stmt = $db->prepare($sql);
$stmt->execute($params);
$enquiries = $stmt->fetchAll();
?>

<div class="header">
    <div class="page-title">📩 Business Enquiries</div>
</div>

<div class="card">
    <form method="GET" style="display: flex; gap: 10px;">
        <input type="hidden" name="page" value="enquiries">
        <select name="status" style="padding: 0.5rem;">
            <option value="">All Status</option>
            <option value="new" <?= $filterStatus === 'new' ? 'selected' : '' ?>>New</option>
            <option value="read" <?= $filterStatus === 'read' ? 'selected' : '' ?>>Read</option>
            <option value="responded" <?= $filterStatus === 'responded' ? 'selected' : '' ?>>Responded</option>
        </select>
        <button type="submit" class="btn btn-primary">Filter</button>
    </form>
</div>

<div class="card" style="overflow-x: auto;">
    <table style="min-width: 800px;">
        <thead>
            <tr>
                <th>Date</th>
                <th>From</th>
                <th>For Listing</th>
                <th>Message</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($enquiries as $e): ?>
        <tr style="<?= ($e['status'] ?? 'new') === 'new' ? 'background: #fefce8;' : '' ?>">
            <td><?= date('d M Y', strtotime($e['created_at'])) ?><br><small><?= date('h:i A', strtotime($e['created_at'])) ?></small></td>
            <td>
                <strong><?= htmlspecialchars($e['sender_name'] ?? $e['name'] ?? 'Anonymous') ?></strong><br>
                <small>📞 <?= $e['sender_phone'] ?? $e['phone'] ?? '-' ?></small>
            </td>
            <td><?= htmlspecialchars($e['listing_title'] ?? 'N/A') ?></td>
            <td style="max-width: 300px;">
                <?= nl2br(htmlspecialchars($e['message'])) ?>
            </td>
            <td>
                <?php
                $statusColor = match($e['status'] ?? 'new') {
                    'new' => '#fef3c7; color: #d97706',
                    'read' => '#e0f2fe; color: #0284c7',
                    'responded' => '#dcfce7; color: #166534',
                    default => '#f1f5f9; color: #475569'
                };
                ?>
                <span style="padding: 2px 8px; border-radius: 99px; font-size: 0.8em; background: <?= $statusColor ?>;">
                    <?= ucfirst($e['status'] ?? 'new') ?>
                </span>
            </td>
            <td>
                <?php if (($e['status'] ?? 'new') === 'new'): ?>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="mark_read">
                    <input type="hidden" name="enquiry_id" value="<?= $e['enquiry_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px;" title="Mark Read">👁</button>
                </form>
                <?php endif; ?>
                
                <?php if (($e['status'] ?? 'new') !== 'responded'): ?>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="mark_responded">
                    <input type="hidden" name="enquiry_id" value="<?= $e['enquiry_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: green;" title="Mark Responded">✓</button>
                </form>
                <?php endif; ?>
                
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete this enquiry?');">
                    <input type="hidden" name="action" value="delete_enquiry">
                    <input type="hidden" name="enquiry_id" value="<?= $e['enquiry_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: red; border-color: #fee2e2;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
