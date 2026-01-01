<?php
// Update reel title with proper UTF-8 encoding
require_once __DIR__ . '/config/database.php';

$title = 'तुम्हालाही हा प्रश्न आहे का की हिंगोलीमध्ये सर्वात बेस्ट मॅगी कुठे मिळते? बघा हा व्हिडिओ!';
$reelId = 30001;

try {
    $db = getDB();
    $stmt = $db->prepare("UPDATE reels SET title = ? WHERE reel_id = ?");
    $result = $stmt->execute([$title, $reelId]);
    
    if ($result) {
        echo "SUCCESS: Title updated for reel $reelId\n";
        
        // Verify
        $stmt = $db->prepare("SELECT reel_id, title FROM reels WHERE reel_id = ?");
        $stmt->execute([$reelId]);
        $row = $stmt->fetch();
        echo "New title: " . $row['title'] . "\n";
    } else {
        echo "FAILED: Could not update title\n";
    }
} catch (Exception $e) {
    echo "ERROR: " . $e->getMessage() . "\n";
}
