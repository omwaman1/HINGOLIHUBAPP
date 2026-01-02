<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= $pageTitle ?? 'Admin' ?> - Hingoli Hub</title>
    <style>
        :root {
            --primary: #6366f1;
            --primary-hover: #818cf8;
            --bg: #0f0f0f;
            --card-bg: #1a1a1a;
            --sidebar-bg: #141414;
            --text: #f1f5f9;
            --text-light: #94a3b8;
            --border: #2a2a2a;
            --success: #22c55e;
            --warning: #f59e0b;
            --danger: #ef4444;
            --sidebar-width: 220px;
            --sidebar-collapsed: 60px;
        }
        
        * { box-sizing: border-box; }
        
        body { 
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; 
            background: var(--bg); 
            color: var(--text); 
            margin: 0; 
            display: flex; 
            min-height: 100vh; 
        }
        
        /* Sidebar */
        .sidebar { 
            width: var(--sidebar-width); 
            background: var(--sidebar-bg); 
            border-right: 1px solid var(--border); 
            display: flex; 
            flex-direction: column; 
            position: fixed; 
            height: 100vh; 
            z-index: 100; 
            transition: width 0.3s ease;
            overflow: hidden;
        }
        
        .sidebar.collapsed { width: var(--sidebar-collapsed); }
        .sidebar.collapsed .nav-text, .sidebar.collapsed .nav-badge { display: none; }
        .sidebar.collapsed .sidebar-title { display: none; }
        .sidebar.collapsed .sidebar-header { justify-content: center; padding: 1rem 0.5rem; }
        
        .sidebar-header { 
            padding: 1rem 1rem;
            font-weight: bold; 
            font-size: 1rem; 
            color: var(--text); 
            border-bottom: 1px solid var(--border); 
            display: flex; 
            align-items: center; 
            gap: 10px;
            white-space: nowrap;
        }
        
        .toggle-btn {
            background: transparent;
            border: none;
            color: var(--text-light);
            width: 28px;
            height: 28px;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1rem;
            transition: color 0.2s;
        }
        .toggle-btn:hover { color: var(--text); }
        
        .nav { padding: 0.5rem; flex: 1; overflow-y: auto; overflow-x: hidden; }
        
        .nav-item { 
            display: flex; 
            align-items: center;
            gap: 10px;
            padding: 0.6rem 0.75rem; 
            color: var(--text-light); 
            text-decoration: none; 
            border-radius: 6px; 
            margin-bottom: 2px; 
            transition: all 0.15s;
            white-space: nowrap;
            font-size: 0.9rem;
        }
        .nav-item:hover { background: rgba(255,255,255,0.05); color: var(--text); }
        .nav-item.active { background: rgba(99, 102, 241, 0.15); color: var(--primary); }
        .nav-item .icon { font-size: 1rem; min-width: 20px; text-align: center; }
        
        .nav-badge {
            margin-left: auto;
            background: var(--primary);
            color: white;
            font-size: 0.7rem;
            padding: 2px 6px;
            border-radius: 99px;
            font-weight: 600;
        }
        .nav-badge.warning { background: var(--warning); }
        .nav-badge.danger { background: var(--danger); }
        
        .nav-divider { border: 0; border-top: 1px solid var(--border); margin: 0.5rem 0; }
        .nav-label { font-size: 0.7rem; color: var(--text-light); padding: 0.5rem 0.75rem; text-transform: uppercase; letter-spacing: 0.5px; }
        
        /* Main Content */
        .main { 
            flex: 1; 
            margin-left: var(--sidebar-width); 
            padding: 1.25rem 1.5rem; 
            transition: margin-left 0.3s ease;
            min-height: 100vh;
        }
        .main.expanded { margin-left: var(--sidebar-collapsed); }
        
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; flex-wrap: wrap; gap: 1rem; }
        .page-title { font-size: 1.3rem; font-weight: 600; }
        
        /* Cards */
        .card { background: var(--card-bg); padding: 1.25rem; border-radius: 10px; border: 1px solid var(--border); margin-bottom: 1.25rem; }
        
        /* Buttons */
        .btn { padding: 0.5rem 1rem; border-radius: 6px; border: 1px solid transparent; cursor: pointer; font-size: 0.85rem; text-decoration: none; display: inline-flex; align-items: center; gap: 0.5rem; transition: all 0.2s; }
        .btn-primary { background: var(--primary); color: white; }
        .btn-primary:hover { background: var(--primary-hover); }
        .btn-outline { background: transparent; border-color: var(--border); color: var(--text); }
        .btn-outline:hover { background: var(--border); }
        .btn-danger { background: rgba(239, 68, 68, 0.1); color: var(--danger); border-color: rgba(239, 68, 68, 0.2); }
        
        /* Form Elements */
        input, select, textarea { background: var(--bg); border: 1px solid var(--border); color: var(--text); border-radius: 6px; padding: 0.5rem 0.75rem; }
        input:focus, select:focus, textarea:focus { outline: none; border-color: var(--primary); }
        
        /* Tables */
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 0.65rem; text-align: left; border-bottom: 1px solid var(--border); }
        th { font-weight: 600; color: var(--text-light); font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.5px; }
        tr:hover { background: rgba(255,255,255,0.02); }
        
        .badge { padding: 3px 10px; border-radius: 99px; font-size: 0.75rem; font-weight: 500; background: rgba(99, 102, 241, 0.1); color: var(--primary); }
        
        .alert { padding: 0.85rem; border-radius: 8px; margin-bottom: 1rem; font-size: 0.9rem; }
        .alert-success { background: rgba(34, 197, 94, 0.1); color: var(--success); border: 1px solid rgba(34, 197, 94, 0.2); }
        .alert-error { background: rgba(239, 68, 68, 0.1); color: var(--danger); border: 1px solid rgba(239, 68, 68, 0.2); }
        
        .mobile-toggle { display: none; }
        
        @media (max-width: 768px) {
            .sidebar { transform: translateX(-100%); width: var(--sidebar-width) !important; }
            .sidebar.open { transform: translateX(0); }
            .main { margin-left: 0 !important; padding: 1rem; }
            .mobile-toggle { display: block; }
            .sidebar-overlay { display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 99; }
            .sidebar-overlay.active { display: block; }
        }
        
        ::-webkit-scrollbar { width: 5px; }
        ::-webkit-scrollbar-track { background: var(--sidebar-bg); }
        ::-webkit-scrollbar-thumb { background: var(--border); border-radius: 3px; }
    </style>
</head>
<body>
    <div class="sidebar-overlay" id="overlay" onclick="closeMobileSidebar()"></div>
    
    <aside class="sidebar" id="sidebar">
        <div class="sidebar-header">
            <button class="toggle-btn" onclick="toggleSidebar()" title="Toggle Menu">☰</button>
            <span class="sidebar-title">Panel</span>
        </div>
        <nav class="nav">
            <a href="index.php?page=dashboard" class="nav-item <?= ($tempPage ?? '') === 'dashboard' ? 'active' : '' ?>">
                <span class="icon">📊</span><span class="nav-text">Dashboard</span>
            </a>
            <a href="index.php?page=orders" class="nav-item <?= ($tempPage ?? '') === 'orders' ? 'active' : '' ?>">
                <span class="icon">🛒</span><span class="nav-text">Orders</span>
                <?php 
                try { 
                    $orderCount = $db->query("SELECT COUNT(*) FROM orders WHERE order_status = 'pending'")->fetchColumn(); 
                    if ($orderCount > 0) echo "<span class='nav-badge'>$orderCount</span>";
                } catch(Exception $e) {} 
                ?>
            </a>
            <a href="index.php?page=listings" class="nav-item <?= ($tempPage ?? '') === 'listings' ? 'active' : '' ?>">
                <span class="icon">📋</span><span class="nav-text">Listings</span>
            </a>
            <a href="index.php?page=moderation" class="nav-item <?= ($tempPage ?? '') === 'moderation' ? 'active' : '' ?>">
                <span class="icon">🛡️</span><span class="nav-text">Moderation</span>
                <?php 
                try { 
                    $pendingCount = $db->query("SELECT COUNT(*) FROM listings WHERE status = 'pending'")->fetchColumn(); 
                    if ($pendingCount > 0) echo "<span class='nav-badge warning'>$pendingCount</span>";
                } catch(Exception $e) {} 
                ?>
            </a>
            <a href="index.php?page=listing_form" class="nav-item <?= ($tempPage ?? '') === 'listing_form' ? 'active' : '' ?>">
                <span class="icon">➕</span><span class="nav-text">Add Listing</span>
            </a>
            <a href="index.php?page=products" class="nav-item <?= ($tempPage ?? '') === 'products' ? 'active' : '' ?>">
                <span class="icon">🛍️</span><span class="nav-text">Shop Products</span>
            </a>
            <a href="index.php?page=old_products" class="nav-item <?= ($tempPage ?? '') === 'old_products' ? 'active' : '' ?>">
                <span class="icon">📦</span><span class="nav-text">Old Products</span>
            </a>
            <a href="index.php?page=categories" class="nav-item <?= ($tempPage ?? '') === 'categories' ? 'active' : '' ?>">
                <span class="icon">📁</span><span class="nav-text">Categories</span>
            </a>
            <a href="index.php?page=users" class="nav-item <?= ($tempPage ?? '') === 'users' ? 'active' : '' ?>">
                <span class="icon">👥</span><span class="nav-text">Users</span>
            </a>
            <a href="index.php?page=banners" class="nav-item <?= ($tempPage ?? '') === 'banners' ? 'active' : '' ?>">
                <span class="icon">🖼️</span><span class="nav-text">Banners</span>
            </a>
            <a href="index.php?page=reels" class="nav-item <?= ($tempPage ?? '') === 'reels' ? 'active' : '' ?>">
                <span class="icon">🎬</span><span class="nav-text">Reels</span>
            </a>
            <a href="index.php?page=cities" class="nav-item <?= ($tempPage ?? '') === 'cities' ? 'active' : '' ?>">
                <span class="icon">🏙️</span><span class="nav-text">Cities</span>
            </a>
            <a href="index.php?page=pincodes" class="nav-item <?= ($tempPage ?? '') === 'pincodes' ? 'active' : '' ?>">
                <span class="icon">📍</span><span class="nav-text">Delivery</span>
            </a>
            <a href="index.php?page=reviews" class="nav-item <?= ($tempPage ?? '') === 'reviews' ? 'active' : '' ?>">
                <span class="icon">⭐</span><span class="nav-text">Reviews</span>
            </a>
            <a href="index.php?page=enquiries" class="nav-item <?= ($tempPage ?? '') === 'enquiries' ? 'active' : '' ?>">
                <span class="icon">📞</span><span class="nav-text">Leads</span>
            </a>
            <a href="index.php?page=analytics" class="nav-item <?= ($tempPage ?? '') === 'analytics' ? 'active' : '' ?>">
                <span class="icon">📈</span><span class="nav-text">Analytics</span>
            </a>
            <a href="index.php?page=notifications" class="nav-item <?= ($tempPage ?? '') === 'notifications' ? 'active' : '' ?>">
                <span class="icon">📲</span><span class="nav-text">Notifications</span>
            </a>
            <a href="index.php?page=export" class="nav-item <?= ($tempPage ?? '') === 'export' ? 'active' : '' ?>">
                <span class="icon">📥</span><span class="nav-text">Export</span>
            </a>
            <a href="index.php?page=sms_logs" class="nav-item <?= ($tempPage ?? '') === 'sms_logs' ? 'active' : '' ?>">
                <span class="icon">📱</span><span class="nav-text">SMS Logs</span>
            </a>
            <a href="index.php?page=app_version" class="nav-item <?= ($tempPage ?? '') === 'app_version' ? 'active' : '' ?>">
                <span class="icon">📲</span><span class="nav-text">App Version</span>
            </a>
            
            <hr class="nav-divider">
            
            <a href="index.php?page=settings" class="nav-item <?= ($tempPage ?? '') === 'settings' ? 'active' : '' ?>">
                <span class="icon">⚙️</span><span class="nav-text">Settings</span>
            </a>
            <a href="logout.php" class="nav-item" style="color: var(--danger);">
                <span class="icon">🚪</span><span class="nav-text">Logout</span>
            </a>
        </nav>
    </aside>
    
    <main class="main" id="main">
        <button class="mobile-toggle btn btn-outline" onclick="openMobileSidebar()" style="margin-bottom: 1rem;">☰ Menu</button>
        
        <?php if (isset($_GET['msg'])): ?>
        <div class="alert alert-success">✅ <?= htmlspecialchars($_GET['msg']) ?></div>
        <?php endif; ?>
        <?php if (isset($_GET['error'])): ?>
        <div class="alert alert-error">⚠️ <?= htmlspecialchars($_GET['error']) ?></div>
        <?php endif; ?>

<script>
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const main = document.getElementById('main');
    sidebar.classList.toggle('collapsed');
    main.classList.toggle('expanded');
    localStorage.setItem('sidebarCollapsed', sidebar.classList.contains('collapsed'));
}
function openMobileSidebar() {
    document.getElementById('sidebar').classList.add('open');
    document.getElementById('overlay').classList.add('active');
}
function closeMobileSidebar() {
    document.getElementById('sidebar').classList.remove('open');
    document.getElementById('overlay').classList.remove('active');
}
if (localStorage.getItem('sidebarCollapsed') === 'true') {
    document.getElementById('sidebar').classList.add('collapsed');
    document.getElementById('main').classList.add('expanded');
}
</script>
