<?php
// Debug the actual POST /reels/like route
error_reporting(E_ALL);
ini_set('display_errors', 1);

require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/helpers/jwt.php';

header('Content-Type: text/plain');

echo "=== DEBUG POST /reels/like ROUTE ===\n\n";

// 1. Check getJsonInput function
echo "1. Checking getJsonInput... ";
if (function_exists('getJsonInput')) {
    echo "EXISTS\n";
} else {
    echo "MISSING - checking if defined in index.php\n";
    $indexContent = file_get_contents(__DIR__ . '/index.php');
    if (strpos($indexContent, 'function getJsonInput') !== false) {
        echo "   Found in index.php but not loaded\n";
    } else {
        echo "   NOT FOUND ANYWHERE!\n";
    }
}

// 2. Check global keyword issue
echo "2. Checking handleReels structure for global usage... ";
$indexContent = file_get_contents(__DIR__ . '/index.php');

// Find handleReels POST case
if (preg_match('/case \'POST\':\s+global \$currentUserId;/', $indexContent)) {
    echo "FOUND global in case block ✓\n";
} else {
    echo "NOT FOUND - might be syntax error in global placement\n";
}

// 3. Check errorResponse function
echo "3. Checking errorResponse function... ";
if (strpos($indexContent, 'function errorResponse') !== false) {
    echo "EXISTS\n";
} else {
    echo "MISSING\n";
}

// 4. Check successResponse function
echo "4. Checking successResponse function... ";
if (strpos($indexContent, 'function successResponse') !== false) {
    echo "EXISTS\n";
} else {
    echo "MISSING\n";
}

// 5. Check helpers/response.php
echo "5. Checking helpers/response.php exists... ";
if (file_exists(__DIR__ . '/helpers/response.php')) {
    echo "EXISTS\n";
    // Check if it's included
    if (strpos($indexContent, 'helpers/response.php') !== false || strpos($indexContent, "helpers/response.php'") !== false) {
        echo "   And is included in index.php ✓\n";
    } else {
        echo "   But NOT included in index.php!\n";
    }
} else {
    echo "MISSING\n";
}

// 6. Simulate actual API call manually
echo "\n6. Simulating toggleReelLike manually...\n";

// Set up globals like the router would
$currentUserId = 9595340263;
$reelId = 30001;

$db = getDB();

try {
    $stmt = $db->prepare("SELECT id FROM reel_likes WHERE user_id = ? AND reel_id = ?");
    $stmt->execute([$currentUserId, $reelId]);
    $existing = $stmt->fetch();
    echo "   Existing like: " . ($existing ? "YES (id=" . $existing['id'] . ")" : "NO") . "\n";
    
    if ($existing) {
        $db->prepare("DELETE FROM reel_likes WHERE user_id = ? AND reel_id = ?")->execute([$currentUserId, $reelId]);
        $db->prepare("UPDATE reels SET likes_count = GREATEST(0, likes_count - 1) WHERE reel_id = ?")->execute([$reelId]);
        $isLiked = false;
    } else {
        $db->prepare("INSERT INTO reel_likes (user_id, reel_id) VALUES (?, ?)")->execute([$currentUserId, $reelId]);
        $db->prepare("UPDATE reels SET likes_count = likes_count + 1 WHERE reel_id = ?")->execute([$reelId]); 
        $isLiked = true;
    }
    
    $countStmt = $db->prepare("SELECT likes_count FROM reels WHERE reel_id = ?");
    $countStmt->execute([$reelId]);
    $likesCount = (int)$countStmt->fetchColumn();
    
    echo "   Action: " . ($isLiked ? "LIKED" : "UNLIKED") . "\n";
    echo "   New likes_count: $likesCount\n";
    echo "   SUCCESS ✓\n";
    
} catch (Exception $e) {
    echo "   ERROR: " . $e->getMessage() . "\n";
}

echo "\n=== DONE ===\n";
