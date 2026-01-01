<?php
// Handle Save
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'save_version') {
    try {
        $android_version = $_POST['android_version'];
        $android_min = $_POST['android_min_version'];
        $android_url = $_POST['android_url'];
        $force_update = isset($_POST['force_update']) ? 1 : 0;
        $update_message = $_POST['update_message'];
        
        $stmt = $db->prepare("INSERT INTO app_config (config_key, config_value) VALUES 
            ('android_version', ?), ('android_min_version', ?), ('android_url', ?), ('force_update', ?), ('update_message', ?)
            ON DUPLICATE KEY UPDATE config_value = VALUES(config_value)");
        $stmt->execute([$android_version, $android_min, $android_url, $force_update, $update_message]);
        
        header("Location: index.php?page=app_version&msg=" . urlencode("Version settings saved!"));
        exit;
    } catch (Exception $e) {
        $error = "Failed to save: " . $e->getMessage();
    }
}

// Fetch Config
$config = [];
try {
    $rows = $db->query("SELECT config_key, config_value FROM app_config")->fetchAll();
    foreach ($rows as $row) $config[$row['config_key']] = $row['config_value'];
} catch (Exception $e) {}
?>

<div class="header">
    <div class="page-title">📲 App Version Control</div>
</div>

<form method="POST">
    <input type="hidden" name="action" value="save_version">
    
    <div class="card">
        <h3 style="margin-top: 0;">Android App</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1.5rem;">
            <label>
                <strong>Current Version</strong>
                <input type="text" name="android_version" value="<?= $config['android_version'] ?? '1.0.0' ?>" placeholder="1.0.0" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            <label>
                <strong>Minimum Required Version</strong>
                <input type="text" name="android_min_version" value="<?= $config['android_min_version'] ?? '1.0.0' ?>" placeholder="1.0.0" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            <label>
                <strong>Play Store URL</strong>
                <input type="url" name="android_url" value="<?= $config['android_url'] ?? '' ?>" placeholder="https://play.google.com/store/apps/..." style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
        </div>
        
        <label style="display: block; margin-top: 1.5rem;">
            <strong>Update Message</strong>
            <textarea name="update_message" rows="3" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;"><?= htmlspecialchars($config['update_message'] ?? 'A new version of the app is available. Please update for the best experience.') ?></textarea>
        </label>
        
        <label style="display: block; margin-top: 1rem;">
            <input type="checkbox" name="force_update" <?= ($config['force_update'] ?? 0) ? 'checked' : '' ?>> 
            <strong>Force Update</strong> <span style="color: var(--text-light);">(Users must update to continue using the app)</span>
        </label>
    </div>
    
    <button type="submit" class="btn btn-primary">💾 Save Version Settings</button>
</form>
