<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'add_subcategory':
                    $name = $_POST['name'];
                    $parent_id = $_POST['parent_id'];
                    $slug = strtolower(trim(preg_replace('/[^A-Za-z0-9-]+/', '-', $name)));
                    $parentType = $db->query("SELECT listing_type FROM categories WHERE category_id = $parent_id")->fetchColumn();
                    $stmt = $db->prepare("INSERT INTO categories (name, slug, parent_id, listing_type) VALUES (?, ?, ?, ?)");
                    $stmt->execute([$name, $slug, $parent_id, $parentType]);
                    header("Location: index.php?page=subcategories&msg=" . urlencode("Subcategory added!"));
                    exit;
                case 'delete':
                    $id = $_POST['category_id'];
                    $db->prepare("DELETE FROM categories WHERE category_id = ?")->execute([$id]);
                    header("Location: index.php?page=subcategories&msg=" . urlencode("Deleted!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

$parents = $db->query("SELECT * FROM categories WHERE parent_id IS NULL ORDER BY name")->fetchAll();
$subcategories = $db->query("SELECT c.*, p.name as parent_name FROM categories c JOIN categories p ON c.parent_id = p.category_id ORDER BY p.name, c.name")->fetchAll();
?>

<div class="header">
    <div class="page-title">📂 Subcategories</div>
</div>

<div class="card">
    <h3 style="margin-top: 0;">➕ Add Subcategory</h3>
    <form method="POST" style="display: flex; gap: 10px; flex-wrap: wrap; align-items: end;">
        <input type="hidden" name="action" value="add_subcategory">
        <label>Parent Category *<br>
            <select name="parent_id" required style="padding: 0.5rem; min-width: 200px;">
                <?php foreach ($parents as $p): ?>
                <option value="<?= $p['category_id'] ?>"><?= htmlspecialchars($p['name']) ?> (<?= $p['listing_type'] ?>)</option>
                <?php endforeach; ?>
            </select>
        </label>
        <label>Subcategory Name *<br><input type="text" name="name" required style="padding: 0.5rem; min-width: 200px;"></label>
        <button type="submit" class="btn btn-primary" style="height: 42px;">Add</button>
    </form>
</div>

<div class="card" style="overflow-x: auto;">
    <table>
        <thead><tr><th>Subcategory</th><th>Parent</th><th>Type</th><th>Action</th></tr></thead>
        <tbody>
        <?php foreach ($subcategories as $s): ?>
        <tr>
            <td><strong><?= htmlspecialchars($s['name']) ?></strong></td>
            <td><?= htmlspecialchars($s['parent_name']) ?></td>
            <td><?= $s['listing_type'] ?></td>
            <td>
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete?');">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="category_id" value="<?= $s['category_id'] ?>">
                    <button type="submit" class="btn btn-danger" style="padding: 4px 8px;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
