<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'delete_city':
                    $id = $_POST['city_id'];
                    $db->prepare("DELETE FROM cities WHERE city_id = ?")->execute([$id]);
                    header("Location: index.php?page=cities&msg=" . urlencode("City deleted!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

$cities = $db->query("SELECT * FROM cities ORDER BY name")->fetchAll();
?>

<div class="header">
    <div class="page-title">Manage Cities</div>
    <a href="index.php?page=city_form" class="btn btn-primary">➕ Add City</a>
</div>

<div class="card" style="overflow-x: auto;">
    <table style="min-width: 600px;">
        <thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Slug</th>
                <th>Popular</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($cities as $c): ?>
        <tr>
            <td><?= $c['city_id'] ?></td>
            <td><strong><?= htmlspecialchars($c['name']) ?></strong></td>
            <td><?= $c['slug'] ?></td>
            <td><?= $c['is_popular'] ? '⭐' : '-' ?></td>
            <td>
                <a href="index.php?page=city_form&id=<?= $c['city_id'] ?>" class="btn btn-outline" style="padding: 4px 8px;">✏️</a>
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete city?');">
                    <input type="hidden" name="action" value="delete_city">
                    <input type="hidden" name="city_id" value="<?= $c['city_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: red; border-color: #fee2e2;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
