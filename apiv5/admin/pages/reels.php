<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    ob_start(); // Prevent blank page on redirect
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'delete_reel':
                    $id = $_POST['reel_id'];
                    $db->prepare("DELETE FROM reels WHERE reel_id = ?")->execute([$id]);
                    ob_end_clean();
                    header("Location: index.php?page=reels&msg=" . urlencode("Reel deleted!"));
                    exit;
                    
                case 'toggle_active':
                    $id = $_POST['reel_id'];
                    $db->prepare("UPDATE reels SET status = IF(status = 'active', 'inactive', 'active') WHERE reel_id = ?")->execute([$id]);
                    ob_end_clean();
                    header("Location: index.php?page=reels&msg=" . urlencode("Status updated!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        ob_end_clean();
        $error = "Action failed: " . $e->getMessage();
    }
}

$reels = $db->query("SELECT * FROM reels ORDER BY sort_order ASC, created_at DESC")->fetchAll();
?>

<div class="header">
    <div class="page-title">Manage Reels</div>
    <a href="index.php?page=reel_form" class="btn btn-primary">➕ Add Reel</a>
</div>

<div class="card" style="overflow-x: auto;">
    <table style="min-width: 900px;">
        <thead>
            <tr>
                <th style="width: 120px;">Video</th>
                <th>Title</th>
                <th>Video URL</th>
                <th>Order</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php if (empty($reels)): ?>
        <tr>
            <td colspan="6" style="text-align: center; color: var(--text-light); padding: 2rem;">
                No reels found. <a href="index.php?page=reel_form">Add your first reel</a>
            </td>
        </tr>
        <?php else: ?>
        <?php foreach ($reels as $r): ?>
        <tr>
            <td>
                <?php if ($r['video_url']): ?>
                <video src="<?= htmlspecialchars($r['video_url']) ?>" style="width: 100px; height: 150px; object-fit: cover; border-radius: 8px; background: #000;" muted></video>
                <?php elseif ($r['thumbnail_url']): ?>
                <img src="<?= htmlspecialchars($r['thumbnail_url']) ?>" style="width: 100px; height: 150px; object-fit: cover; border-radius: 8px;">
                <?php else: ?>
                <div style="width: 100px; height: 150px; background: var(--border); border-radius: 8px; display: flex; align-items: center; justify-content: center;">🎬</div>
                <?php endif; ?>
            </td>
            <td>
                <strong><?= htmlspecialchars($r['title'] ?? 'Untitled') ?></strong><br>
                <small style="color: #666;">ID: <?= $r['reel_id'] ?></small>
            </td>
            <td>
                <?php if ($r['video_url']): ?>
                <a href="<?= htmlspecialchars($r['video_url']) ?>" target="_blank" style="color: var(--primary); word-break: break-all; font-size: 0.8rem;">
                    <?= strlen($r['video_url']) > 50 ? substr($r['video_url'], 0, 50) . '...' : $r['video_url'] ?>
                </a>
                <?php elseif ($r['instagram_url']): ?>
                <a href="<?= htmlspecialchars($r['instagram_url']) ?>" target="_blank" style="color: var(--warning); word-break: break-all; font-size: 0.8rem;">
                    (Instagram) <?= strlen($r['instagram_url']) > 40 ? substr($r['instagram_url'], 0, 40) . '...' : $r['instagram_url'] ?>
                </a>
                <?php else: ?>
                <span style="color: var(--text-light);">No URL</span>
                <?php endif; ?>
            </td>
            <td><?= $r['sort_order'] ?></td>
            <td>
                <span style="padding: 2px 8px; border-radius: 99px; font-size: 0.8em; background: <?= $r['status'] === 'active' ? '#dcfce7' : '#fee2e2' ?>; color: <?= $r['status'] === 'active' ? '#166534' : '#dc2626' ?>;">
                    <?= ucfirst($r['status']) ?>
                </span>
            </td>
            <td>
                <a href="index.php?page=reel_form&id=<?= $r['reel_id'] ?>" class="btn btn-outline" style="padding: 4px 8px;">✏️</a>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="toggle_active">
                    <input type="hidden" name="reel_id" value="<?= $r['reel_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px;" title="Toggle Status">↻</button>
                </form>
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete this reel?');">
                    <input type="hidden" name="action" value="delete_reel">
                    <input type="hidden" name="reel_id" value="<?= $r['reel_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: red; border-color: #fee2e2;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        <?php endif; ?>
        </tbody>
    </table>
</div>

<style>
video:hover {
    cursor: pointer;
}
video:focus {
    outline: 2px solid var(--primary);
}
</style>

<script>
// Play video on hover
document.querySelectorAll('video').forEach(v => {
    v.addEventListener('mouseenter', () => v.play());
    v.addEventListener('mouseleave', () => { v.pause(); v.currentTime = 0; });
});
</script>
