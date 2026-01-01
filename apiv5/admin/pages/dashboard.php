<div class="header">
    <div class="page-title">Dashboard</div>
</div>

<div class="card">
    <h2>👋 Welcome Back, Admin!</h2>
    <p>Select a module from the sidebar to start managing your application.</p>
</div>

<!-- Stats Overview -->
<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.5rem;">
    <?php
    // Quick Stats
    try {
        $stats = [
            'Listings' => $db->query("SELECT COUNT(*) FROM listings")->fetchColumn(),
            'Users' => $db->query("SELECT COUNT(*) FROM users")->fetchColumn(),
            'Orders' => $db->query("SELECT COUNT(*) FROM orders")->fetchColumn(),
            'Products' => $db->query("SELECT COUNT(*) FROM shop_products")->fetchColumn(),
        ];
    } catch (Exception $e) {
        $stats = [];
        echo "<p>Error loading stats: " . $e->getMessage() . "</p>";
    }
    ?>
    
    <?php foreach ($stats as $label => $count): ?>
    <div class="card" style="text-align: center; margin: 0;">
        <div style="font-size: 2rem; font-weight: bold; color: var(--primary);"><?= number_format($count) ?></div>
        <div style="color: var(--text-light);"><?= $label ?></div>
    </div>
    <?php endforeach; ?>
</div>
