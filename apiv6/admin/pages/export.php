<?php
if (isset($_GET['download'])) {
    $type = $_GET['download'];
    header('Content-Type: text/csv; charset=utf-8');
    header("Content-Disposition: attachment; filename={$type}_export_" . date('Y-m-d') . ".csv");
    $output = fopen('php://output', 'w');
    
    if ($type === 'listings') {
        fputcsv($output, ['ID', 'Title', 'Type', 'Category', 'City', 'Status', 'Created']);
        $rows = $db->query("SELECT l.listing_id, l.title, l.listing_type, c.name, l.city, l.status, l.created_at FROM listings l LEFT JOIN categories c ON l.category_id = c.category_id")->fetchAll();
        foreach ($rows as $row) fputcsv($output, $row);
    } elseif ($type === 'users') {
        fputcsv($output, ['ID', 'Username', 'Phone', 'Email', 'Verified', 'Created']);
        $rows = $db->query("SELECT user_id, username, phone, email, is_verified, created_at FROM users")->fetchAll();
        foreach ($rows as $row) fputcsv($output, $row);
    } elseif ($type === 'orders') {
        fputcsv($output, ['ID', 'User', 'Total', 'Status', 'Payment', 'Created']);
        $rows = $db->query("SELECT o.order_id, u.username, o.total_amount, o.order_status, o.payment_status, o.created_at FROM orders o LEFT JOIN users u ON o.user_id = u.user_id")->fetchAll();
        foreach ($rows as $row) fputcsv($output, $row);
    }
    fclose($output);
    exit;
}
?>

<div class="header">
    <div class="page-title">📥 Export Data</div>
</div>

<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1.5rem;">
    <div class="card" style="text-align: center;">
        <div style="font-size: 3rem; margin-bottom: 1rem;">📋</div>
        <h3>Listings</h3>
        <p style="color: var(--text-light);">Export all listings to CSV</p>
        <a href="index.php?page=export&download=listings" class="btn btn-primary">Download CSV</a>
    </div>
    
    <div class="card" style="text-align: center;">
        <div style="font-size: 3rem; margin-bottom: 1rem;">👥</div>
        <h3>Users</h3>
        <p style="color: var(--text-light);">Export all users to CSV</p>
        <a href="index.php?page=export&download=users" class="btn btn-primary">Download CSV</a>
    </div>
    
    <div class="card" style="text-align: center;">
        <div style="font-size: 3rem; margin-bottom: 1rem;">📦</div>
        <h3>Orders</h3>
        <p style="color: var(--text-light);">Export all orders to CSV</p>
        <a href="index.php?page=export&download=orders" class="btn btn-primary">Download CSV</a>
    </div>
</div>
