<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'approve_review':
                    $id = $_POST['review_id'];
                    $db->prepare("UPDATE reviews SET approval_status = 'approved', is_approved = 1, moderated_at = NOW() WHERE review_id = ?")->execute([$id]);
                    header("Location: index.php?page=reviews&msg=" . urlencode("Review approved!"));
                    exit;
                
                case 'reject_review':
                    $id = $_POST['review_id'];
                    $db->prepare("UPDATE reviews SET approval_status = 'rejected', is_approved = 0, moderated_at = NOW() WHERE review_id = ?")->execute([$id]);
                    header("Location: index.php?page=reviews&msg=" . urlencode("Review rejected!"));
                    exit;
                
                case 'delete_review':
                    $id = $_POST['review_id'];
                    $db->prepare("DELETE FROM reviews WHERE review_id = ?")->execute([$id]);
                    header("Location: index.php?page=reviews&msg=" . urlencode("Review deleted!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

$reviews = [];
$reviewError = null;
try {
    $reviews = $db->query("SELECT r.*, l.title as listing_title, u.username as reviewer_name 
                           FROM reviews r 
                           LEFT JOIN listings l ON r.listing_id = l.listing_id 
                           LEFT JOIN users u ON r.user_id = u.user_id 
                           ORDER BY r.created_at DESC")->fetchAll();
} catch (Exception $e) {
    $reviewError = $e->getMessage();
}
?>

<div class="header">
    <div class="page-title">⭐ Manage Reviews</div>
</div>

<?php if ($reviewError): ?>
<div class="alert alert-error">Error loading reviews: <?= htmlspecialchars($reviewError) ?></div>
<?php elseif (empty($reviews)): ?>
<div class="card" style="text-align: center; padding: 3rem;">
    <div style="font-size: 3rem; margin-bottom: 1rem;">⭐</div>
    <h3>No Reviews Yet</h3>
    <p style="color: var(--text-light);">Reviews will appear here when users leave them on listings.</p>
</div>
<?php else: ?>
<div class="card" style="overflow-x: auto;">
    <div style="margin-bottom: 1rem; color: var(--text-light);">Total: <?= count($reviews) ?> reviews</div>
    <table style="min-width: 800px;">
        <thead>
            <tr>
                <th>Date</th>
                <th>Listing</th>
                <th>Reviewer</th>
                <th>Rating</th>
                <th>Comment</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($reviews as $r): ?>
        <tr>
            <td><?= date('d M Y', strtotime($r['created_at'])) ?></td>
            <td><?= htmlspecialchars($r['listing_title'] ?? 'Unknown') ?></td>
            <td><?= htmlspecialchars($r['reviewer_name'] ?? 'Unknown') ?></td>
            <td><span style="color: gold;"><?= str_repeat('★', $r['rating'] ?? 0) ?></span> (<?= $r['rating'] ?? 0 ?>)</td>
            <td style="max-width: 300px;">
                <strong><?= htmlspecialchars($r['title'] ?? '') ?></strong><br>
                <?= htmlspecialchars($r['comment'] ?? $r['review_text'] ?? '') ?>
            </td>
            <td>
                <?php 
                $status = $r['approval_status'] ?? ($r['is_approved'] ? 'approved' : 'pending');
                if ($status === 'approved'): ?>
                <span style="color: green;">✓ Approved</span>
                <?php elseif ($status === 'rejected'): ?>
                <span style="color: red;">✕ Rejected</span>
                <?php else: ?>
                <span style="color: orange;">⏳ Pending</span>
                <?php endif; ?>
            </td>
            <td>
                <?php if ($status !== 'approved'): ?>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="approve_review">
                    <input type="hidden" name="review_id" value="<?= $r['review_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: green;" title="Approve">✓</button>
                </form>
                <?php endif; ?>
                
                <?php if ($status !== 'rejected'): ?>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="reject_review">
                    <input type="hidden" name="review_id" value="<?= $r['review_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: orange;" title="Reject">✕</button>
                </form>
                <?php endif; ?>
                
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete review?');">
                    <input type="hidden" name="action" value="delete_review">
                    <input type="hidden" name="review_id" value="<?= $r['review_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: red; border-color: #fee2e2;" title="Delete">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
<?php endif; ?>
