<?php
// Handle Save
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['action']) && $_POST['action'] === 'save_settings') {
    try {
        $settings = [
            'site_name' => $_POST['site_name'] ?? 'Hello Hingoli',
            'contact_email' => $_POST['contact_email'] ?? '',
            'contact_phone' => $_POST['contact_phone'] ?? '',
            'whatsapp_number' => $_POST['whatsapp_number'] ?? '',
            'facebook_url' => $_POST['facebook_url'] ?? '',
            'instagram_url' => $_POST['instagram_url'] ?? '',
            'twitter_url' => $_POST['twitter_url'] ?? '',
            'default_city' => $_POST['default_city'] ?? 'Hingoli',
            'min_order_amount' => $_POST['min_order_amount'] ?? '0',
            'delivery_charge' => $_POST['delivery_charge'] ?? '0',
            'free_delivery_above' => $_POST['free_delivery_above'] ?? '500',
        ];
        
        foreach ($settings as $key => $value) {
            $stmt = $db->prepare("INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) ON DUPLICATE KEY UPDATE setting_value = ?");
            $stmt->execute([$key, $value, $value]);
        }
        
        header("Location: index.php?page=settings&msg=" . urlencode("Settings saved successfully!"));
        exit;
    } catch (Exception $e) {
        $error = "Failed to save: " . $e->getMessage();
    }
}

// Fetch Current Settings
$settingsData = [];
try {
    $result = $db->query("SELECT setting_key, setting_value FROM settings");
    while ($row = $result->fetch()) {
        $settingsData[$row['setting_key']] = $row['setting_value'];
    }
} catch (Exception $e) {
    // Table might not exist
}

// Helper function
function getSetting($key, $default = '') {
    global $settingsData;
    return $settingsData[$key] ?? $default;
}
?>

<div class="header">
    <div class="page-title">⚙️ App Settings</div>
</div>

<form method="POST">
    <input type="hidden" name="action" value="save_settings">
    
    <div class="card">
        <h3 style="margin-top: 0;">🏢 General Settings</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem;">
            <label>
                <strong>Site Name</strong>
                <input type="text" name="site_name" value="<?= htmlspecialchars(getSetting('site_name', 'Hello Hingoli')) ?>" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            <label>
                <strong>Default City</strong>
                <input type="text" name="default_city" value="<?= htmlspecialchars(getSetting('default_city', 'Hingoli')) ?>" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
        </div>
    </div>
    
    <div class="card">
        <h3 style="margin-top: 0;">📞 Contact Information</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem;">
            <label>
                <strong>Contact Email</strong>
                <input type="email" name="contact_email" value="<?= htmlspecialchars(getSetting('contact_email')) ?>" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            <label>
                <strong>Contact Phone</strong>
                <input type="text" name="contact_phone" value="<?= htmlspecialchars(getSetting('contact_phone')) ?>" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            <label>
                <strong>WhatsApp Number</strong>
                <input type="text" name="whatsapp_number" value="<?= htmlspecialchars(getSetting('whatsapp_number')) ?>" placeholder="919876543210" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
        </div>
    </div>
    
    <div class="card">
        <h3 style="margin-top: 0;">🔗 Social Media</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem;">
            <label>
                <strong>Facebook URL</strong>
                <input type="url" name="facebook_url" value="<?= htmlspecialchars(getSetting('facebook_url')) ?>" placeholder="https://facebook.com/..." style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            <label>
                <strong>Instagram URL</strong>
                <input type="url" name="instagram_url" value="<?= htmlspecialchars(getSetting('instagram_url')) ?>" placeholder="https://instagram.com/..." style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            <label>
                <strong>Twitter/X URL</strong>
                <input type="url" name="twitter_url" value="<?= htmlspecialchars(getSetting('twitter_url')) ?>" placeholder="https://x.com/..." style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
        </div>
    </div>
    
    <div class="card">
        <h3 style="margin-top: 0;">🛒 E-Commerce Settings</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1.5rem;">
            <label>
                <strong>Min Order Amount (₹)</strong>
                <input type="number" name="min_order_amount" value="<?= getSetting('min_order_amount', '0') ?>" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            <label>
                <strong>Delivery Charge (₹)</strong>
                <input type="number" name="delivery_charge" value="<?= getSetting('delivery_charge', '0') ?>" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
            <label>
                <strong>Free Delivery Above (₹)</strong>
                <input type="number" name="free_delivery_above" value="<?= getSetting('free_delivery_above', '500') ?>" style="width: 100%; padding: 0.5rem; margin-top: 0.5rem;">
            </label>
        </div>
    </div>
    
    <button type="submit" class="btn btn-primary" style="margin-top: 1rem;">💾 Save Settings</button>
</form>
