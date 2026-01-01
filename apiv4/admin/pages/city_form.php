<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'add_city':
                    $name = $_POST['name'];
                    $slug = strtolower(trim(preg_replace('/[^A-Za-z0-9-]+/', '-', $name)));
                    $is_popular = isset($_POST['is_popular']) ? 1 : 0;
                    
                    $stmt = $db->prepare("INSERT INTO cities (state_id, name, slug, is_popular, is_active) VALUES (1, ?, ?, ?, 1)");
                    $stmt->execute([$name, $slug, $is_popular]);
                    
                    header("Location: index.php?page=cities&msg=" . urlencode("City added!"));
                    exit;

                case 'edit_city':
                    $id = $_POST['city_id'];
                    $name = $_POST['name'];
                    $slug = strtolower(trim(preg_replace('/[^A-Za-z0-9-]+/', '-', $name)));
                    $is_popular = isset($_POST['is_popular']) ? 1 : 0;
                    
                    $stmt = $db->prepare("UPDATE cities SET name = ?, slug = ?, is_popular = ? WHERE city_id = ?");
                    $stmt->execute([$name, $slug, $is_popular, $id]);
                    
                    header("Location: index.php?page=cities&msg=" . urlencode("City updated!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

$editCity = null;
if (isset($_GET['id'])) {
    $stmt = $db->prepare("SELECT * FROM cities WHERE city_id = ?");
    $stmt->execute([$_GET['id']]);
    $editCity = $stmt->fetch();
}
?>

<div class="header">
    <div class="page-title"><?= $editCity ? 'Edit City' : 'Add New City' ?></div>
    <a href="index.php?page=cities" class="btn btn-outline">← Back to Cities</a>
</div>

<div class="card">
    <form method="POST">
        <input type="hidden" name="action" value="<?= $editCity ? 'edit_city' : 'add_city' ?>">
        <?php if ($editCity): ?>
        <input type="hidden" name="city_id" value="<?= $editCity['city_id'] ?>">
        <?php endif; ?>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; margin-bottom: 1.5rem;">
            <label>
                <strong>City Name *</strong>
                <input type="text" name="name" value="<?= $editCity['name'] ?? '' ?>" required style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
        </div>
        
        <label style="display: block; margin-bottom: 1.5rem;">
            <input type="checkbox" name="is_popular" <?= (!isset($editCity) || $editCity['is_popular']) ? 'checked' : '' ?>> Popular City
        </label>
        
        <button type="submit" class="btn btn-primary"><?= $editCity ? 'Update City' : 'Create City' ?></button>
    </form>
</div>
