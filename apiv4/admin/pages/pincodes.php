<?php
// Handle Actions
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if (isset($_POST['action'])) {
            switch ($_POST['action']) {
                case 'add_pincode':
                    $pincode = $_POST['pincode'];
                    $area_name = $_POST['area_name'];
                    $city = $_POST['city'] ?? 'Hingoli';
                    $is_active = isset($_POST['is_active']) ? 1 : 0;
                    $delivery_charge = !empty($_POST['delivery_charge']) ? $_POST['delivery_charge'] : 0;
                    
                    $stmt = $db->prepare("INSERT INTO service_pincodes (pincode, area_name, city, is_active, delivery_charge) VALUES (?, ?, ?, ?, ?)");
                    $stmt->execute([$pincode, $area_name, $city, $is_active, $delivery_charge]);
                    header("Location: index.php?page=pincodes&msg=" . urlencode("Pincode added!"));
                    exit;
                    
                case 'toggle_active':
                    $id = $_POST['pincode_id'];
                    $db->prepare("UPDATE service_pincodes SET is_active = NOT is_active WHERE pincode_id = ?")->execute([$id]);
                    header("Location: index.php?page=pincodes&msg=" . urlencode("Status updated!"));
                    exit;
                    
                case 'delete_pincode':
                    $id = $_POST['pincode_id'];
                    $db->prepare("DELETE FROM service_pincodes WHERE pincode_id = ?")->execute([$id]);
                    header("Location: index.php?page=pincodes&msg=" . urlencode("Pincode deleted!"));
                    exit;
            }
        }
    } catch (Exception $e) {
        $error = "Action failed: " . $e->getMessage();
    }
}

// Fetch Pincodes
$pincodes = $db->query("SELECT * FROM service_pincodes ORDER BY pincode")->fetchAll();
?>

<div class="header">
    <div class="page-title">📍 Service Pincodes (Delivery Areas)</div>
</div>

<!-- Add New Pincode -->
<div class="card">
    <h3 style="margin-top: 0;">➕ Add New Pincode</h3>
    <form method="POST" style="display: flex; gap: 10px; flex-wrap: wrap; align-items: end;">
        <input type="hidden" name="action" value="add_pincode">
        <label>
            Pincode *
            <input type="text" name="pincode" required maxlength="6" pattern="[0-9]{6}" placeholder="431513" style="width: 100px; padding: 0.5rem;">
        </label>
        <label>
            Area Name *
            <input type="text" name="area_name" required placeholder="Area/Locality" style="width: 200px; padding: 0.5rem;">
        </label>
        <label>
            City
            <input type="text" name="city" value="Hingoli" style="width: 120px; padding: 0.5rem;">
        </label>
        <label>
            Delivery Charge (₹)
            <input type="number" name="delivery_charge" value="0" style="width: 100px; padding: 0.5rem;">
        </label>
        <label style="display: flex; align-items: center; gap: 5px; padding-top: 20px;">
            <input type="checkbox" name="is_active" checked> Active
        </label>
        <button type="submit" class="btn btn-primary" style="height: 42px;">Add</button>
    </form>
</div>

<!-- Pincodes List -->
<div class="card" style="overflow-x: auto;">
    <table style="min-width: 600px;">
        <thead>
            <tr>
                <th>Pincode</th>
                <th>Area Name</th>
                <th>City</th>
                <th>Delivery Charge</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($pincodes as $p): ?>
        <tr>
            <td><strong><?= $p['pincode'] ?></strong></td>
            <td><?= htmlspecialchars($p['area_name']) ?></td>
            <td><?= htmlspecialchars($p['city']) ?></td>
            <td>₹<?= number_format($p['delivery_charge']) ?></td>
            <td>
                <span style="padding: 2px 8px; border-radius: 99px; font-size: 0.8em; background: <?= $p['is_active'] ? '#dcfce7' : '#fee2e2' ?>; color: <?= $p['is_active'] ? '#166534' : '#dc2626' ?>;">
                    <?= $p['is_active'] ? 'Active' : 'Inactive' ?>
                </span>
            </td>
            <td>
                <form method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="toggle_active">
                    <input type="hidden" name="pincode_id" value="<?= $p['pincode_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px;">↻</button>
                </form>
                <form method="POST" style="display:inline;" onsubmit="return confirm('Delete?');">
                    <input type="hidden" name="action" value="delete_pincode">
                    <input type="hidden" name="pincode_id" value="<?= $p['pincode_id'] ?>">
                    <button type="submit" class="btn btn-outline" style="padding: 4px 8px; color: red;">🗑</button>
                </form>
            </td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
</div>
