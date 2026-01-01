<?php
/**
 * Standalone Reel Landing Page
 * URL: /apiv5/reel.php?id=30001
 * This bypasses the main router to avoid caching issues
 */

// Force no caching
header('Cache-Control: no-cache, no-store, must-revalidate');
header('Pragma: no-cache');
header('Expires: 0');

// Database config
require_once __DIR__ . '/config/database.php';

$reelId = $_GET['id'] ?? null;
$reel = null;
$error = null;

if ($reelId && is_numeric($reelId)) {
    try {
        $db = getDB();
        $stmt = $db->prepare("SELECT reel_id, title, thumbnail_url, likes_count FROM reels WHERE reel_id = ? AND status = 'active'");
        $stmt->execute([(int)$reelId]);
        $reel = $stmt->fetch();
        if (!$reel) {
            $error = "रील सापडले नाही";
        }
    } catch (Exception $e) {
        $error = "काहीतरी चूक झाली";
    }
} else {
    $error = "Invalid reel ID";
}

$playStoreUrl = "market://details?id=com.hingoli.hub";
$playStoreWeb = "https://play.google.com/store/apps/details?id=com.hingoli.hub";
$deepLink = "hingoliHub://reel/" . ($reelId ?? '');

// Output HTML
header('Content-Type: text/html; charset=utf-8');
?>
<!DOCTYPE html>
<html lang="mr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hingoli Hub - Reel</title>
    <meta name="description" content="बघा आपल्या हिंगोली च्या reels आता Hingoli Hub App वर">
    <meta property="og:title" content="Hingoli Hub - Reel">
    <meta property="og:description" content="बघा आपल्या हिंगोली च्या reels आता Hingoli Hub App वर 🎬">
    <meta property="og:type" content="video">
    <?php if ($reel && !empty($reel['thumbnail_url'])): ?>
    <meta property="og:image" content="<?php echo htmlspecialchars($reel['thumbnail_url']); ?>">
    <?php endif; ?>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu, sans-serif;
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: white;
            padding: 20px;
        }
        .container { text-align: center; max-width: 400px; width: 100%; }
        .logo { font-size: 3rem; margin-bottom: 20px; }
        h1 {
            font-size: 1.8rem;
            margin-bottom: 10px;
            background: linear-gradient(90deg, #e94560, #ff6b6b);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .marathi-text { font-size: 1.1rem; color: #aaa; margin-bottom: 30px; line-height: 1.6; }
        .reel-preview { background: rgba(255,255,255,0.1); border-radius: 16px; padding: 20px; margin-bottom: 30px; }
        .reel-title { font-size: 1.2rem; margin-bottom: 15px; color: #fff; }
        .reel-stats { display: flex; justify-content: center; gap: 20px; color: #aaa; font-size: 0.9rem; }
        .btn {
            display: inline-block;
            padding: 16px 32px;
            border-radius: 50px;
            font-size: 1.1rem;
            font-weight: 600;
            text-decoration: none;
            margin: 10px;
            transition: all 0.3s ease;
        }
        .btn-primary { background: linear-gradient(90deg, #e94560, #ff6b6b); color: white; }
        .btn-primary:hover { transform: scale(1.05); box-shadow: 0 10px 30px rgba(233, 69, 96, 0.4); }
        .btn-secondary { background: rgba(255,255,255,0.1); color: white; border: 1px solid rgba(255,255,255,0.3); }
        .btn-secondary:hover { background: rgba(255,255,255,0.2); }
        .footer { margin-top: 40px; color: #666; font-size: 0.85rem; }
        .error-msg { background: rgba(255,0,0,0.2); border: 1px solid rgba(255,0,0,0.3); border-radius: 8px; padding: 15px; margin-bottom: 20px; }
    </style>
</head>
<body>
<div class="container">
    <div class="logo">🎬</div>
    <h1>Hingoli Hub</h1>
    <p class="marathi-text">बघा आपल्या हिंगोली च्या reels आता Hingoli Hub App वर</p>
    
    <?php if ($error): ?>
        <div class="error-msg"><?php echo htmlspecialchars($error); ?></div>
    <?php elseif ($reel): ?>
        <div class="reel-preview">
            <div class="reel-title"><?php echo htmlspecialchars($reel['title'] ?? 'Untitled Reel'); ?></div>
            <div class="reel-stats"><span>❤️ <?php echo number_format($reel['likes_count'] ?? 0); ?> likes</span></div>
        </div>
    <?php endif; ?>
    
    <a href="<?php echo $deepLink; ?>" class="btn btn-primary" id="openApp">📱 Open in Hingoli Hub</a>
    <br>
    <a href="<?php echo $playStoreUrl; ?>" class="btn btn-secondary" id="downloadApp">⬇️ Download App</a>
    <p class="footer">© 2024 Hingoli Hub. All rights reserved.</p>
</div>
<script>
    var deepLink = "<?php echo $deepLink; ?>";
    var playStore = "<?php echo $playStoreUrl; ?>";
    var playStoreWeb = "<?php echo $playStoreWeb; ?>";
    
    // Try to open app automatically, fallback to Play Store app
    (function() {
        var clicked = false;
        
        // Try deep link via hidden iframe (works better on mobile)
        var iframe = document.createElement('iframe');
        iframe.style.display = 'none';
        iframe.src = deepLink;
        document.body.appendChild(iframe);
        
        // Also try direct location change
        window.location.href = deepLink;
        
        // After 1 second, if still on page, go to Play Store app
        setTimeout(function() {
            if (!clicked && document.hidden !== true) {
                // App didn't open, try Play Store app first
                window.location.href = playStore;
                // If that fails too, fallback to web after another second
                setTimeout(function() {
                    if (document.hidden !== true) {
                        window.location.href = playStoreWeb;
                    }
                }, 800);
            }
        }, 1000);
        
        // If page becomes hidden (app opened), don't redirect
        document.addEventListener('visibilitychange', function() {
            if (document.hidden) {
                clicked = true;
            }
        });
    })();
    
    // Download button - try market first, then web
    document.getElementById("downloadApp").addEventListener("click", function(e) {
        e.preventDefault();
        window.location.href = playStore;
        setTimeout(function() {
            window.location.href = playStoreWeb;
        }, 800);
    });
    
    // Open app button
    document.getElementById("openApp").addEventListener("click", function(e) {
        e.preventDefault();
        window.location.href = deepLink;
        setTimeout(function() {
            window.location.href = playStore;
        }, 1000);
    });
</script>
</body>
</html>
