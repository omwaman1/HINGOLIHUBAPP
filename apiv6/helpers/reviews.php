<?php
/**
 * Review Helper Functions
 * Unified review handling for listings and products
 */

/**
 * Transform review row to API response format
 */
function transformReviewRow(array $r): array {
    return [
        'review_id' => (int)$r['review_id'],
        'rating' => (int)$r['rating'],
        'title' => $r['title'] ?? null,
        'content' => $r['content'] ?? null,
        'images' => isset($r['images']) && $r['images'] ? json_decode($r['images'], true) : [],
        'seller_response' => $r['seller_response'] ?? null,
        'seller_response_at' => $r['seller_response_at'] ?? null,
        'helpful_count' => isset($r['helpful_count']) ? (int)$r['helpful_count'] : 0,
        'is_verified_purchase' => (bool)($r['is_verified_purchase'] ?? false),
        'reviewer' => [
            'user_id' => isset($r['user_id']) ? (int)$r['user_id'] : null,
            'username' => $r['username'] ?? 'Anonymous',
            'avatar_url' => $r['avatar_url'] ?? null
        ],
        'created_at' => $r['created_at'] ?? null
    ];
}

/**
 * Get reviews for a listing, product, or old product
 * @param string $type 'listing', 'product', or 'old_product'
 * @param int $entityId Listing ID, Product ID, or Old Product ID
 */
function getReviewsFor(string $type, int $entityId): void {
    list($page, $perPage) = getPaginationParams(10, 50);
    
    $db = getDB();
    // Determine column and exclusion condition based on type
    // This ensures we only get reviews for the specific type
    // avoiding ID collisions (e.g., product_id=1 vs listing_id=1)
    switch ($type) {
        case 'product':
            $column = 'product_id';
            // Only get reviews that have product_id set AND listing_id/old_product_id are NULL
            $exclusionCondition = 'AND r.listing_id IS NULL AND r.old_product_id IS NULL';
            break;
        case 'old_product':
            $column = 'old_product_id';
            // Only get reviews that have old_product_id set AND listing_id/product_id are NULL
            $exclusionCondition = 'AND r.listing_id IS NULL AND r.product_id IS NULL';
            break;
        default:
            $column = 'listing_id';
            // Only get reviews that have listing_id set AND product_id/old_product_id are NULL
            $exclusionCondition = 'AND r.product_id IS NULL AND r.old_product_id IS NULL';
    }
    
    // Get total count
    $stmt = $db->prepare("SELECT COUNT(*) FROM reviews r WHERE r.$column = ? AND r.is_approved = 1 $exclusionCondition");
    $stmt->execute([$entityId]);
    $total = (int)$stmt->fetchColumn();
    
    // Get reviews with pagination
    $offset = ($page - 1) * $perPage;
    $stmt = $db->prepare("
        SELECT r.review_id, r.rating, r.title, r.content, r.images,
               r.seller_response, r.seller_response_at, r.helpful_count,
               r.is_verified_purchase, r.created_at,
               u.user_id, u.username, u.avatar_url
        FROM reviews r
        LEFT JOIN users u ON r.reviewer_id = u.user_id
        WHERE r.$column = ? AND r.is_approved = 1 $exclusionCondition
        ORDER BY r.created_at DESC
        LIMIT $perPage OFFSET $offset
    ");
    $stmt->execute([$entityId]);
    $reviews = $stmt->fetchAll();
    
    // Transform reviews
    $reviews = array_map('transformReviewRow', $reviews);
    
    paginatedResponse($reviews, $page, $perPage, $total);
}

/**
 * Add a review to a listing, product, or old product
 * @param string $type 'listing', 'product', or 'old_product'
 * @param int $entityId Listing ID, Product ID, or Old Product ID
 */
function addReviewFor(string $type, int $entityId): void {
    // Require authentication
    $user = requireAuth();
    $userId = (int)$user['user_id'];
    
    $db = getDB();
    
    // Determine column, table, and exclusion condition based on type
    // Exclusion condition ensures we don't have ID collisions between different review types
    switch ($type) {
        case 'product':
            $column = 'product_id';
            $table = 'shop_products';
            $idColumn = 'product_id';
            $exclusionCondition = 'AND listing_id IS NULL AND old_product_id IS NULL';
            break;
        case 'old_product':
            $column = 'old_product_id';
            $table = 'old_products';
            $idColumn = 'product_id';
            $exclusionCondition = 'AND listing_id IS NULL AND product_id IS NULL';
            break;
        default:
            $column = 'listing_id';
            $table = 'listings';
            $idColumn = 'listing_id';
            $exclusionCondition = 'AND product_id IS NULL AND old_product_id IS NULL';
    }
    
    // Check if entity exists and get owner
    if ($type === 'product') {
        $stmt = $db->prepare("
            SELECT sp.product_id, l.user_id 
            FROM shop_products sp 
            JOIN listings l ON sp.listing_id = l.listing_id 
            WHERE sp.product_id = ?
        ");
    } elseif ($type === 'old_product') {
        $stmt = $db->prepare("
            SELECT product_id, user_id 
            FROM old_products 
            WHERE product_id = ? AND status = 'active'
        ");
    } else {
        $stmt = $db->prepare("SELECT listing_id, user_id FROM listings WHERE listing_id = ? AND status = 'active'");
    }
    $stmt->execute([$entityId]);
    $entity = $stmt->fetch();
    
    if (!$entity) {
        $typeName = $type === 'old_product' ? 'Product' : ucfirst($type);
        errorResponse($typeName . ' not found', 404);
    }
    
    // Check if user is trying to review their own entity
    if ((int)$entity['user_id'] === $userId) {
        $typeName = $type === 'old_product' ? 'product' : $type;
        errorResponse('You cannot review your own ' . $typeName, 400);
    }
    
    // Check if already reviewed (with exclusion to avoid ID collision)
    $stmt = $db->prepare("SELECT review_id FROM reviews WHERE $column = ? AND reviewer_id = ? $exclusionCondition");
    $stmt->execute([$entityId, $userId]);
    if ($stmt->fetch()) {
        $typeName = $type === 'old_product' ? 'product' : $type;
        errorResponse('You have already reviewed this ' . $typeName, 400);
    }
    
    // Get review data
    $data = getJsonBody();
    $rating = isset($data['rating']) ? (int)$data['rating'] : 0;
    $content = $data['content'] ?? $data['comment'] ?? null;
    $title = $data['title'] ?? null;
    
    if ($rating < 1 || $rating > 5) {
        errorResponse('Rating must be between 1 and 5', 400);
    }
    
    // Check for verified purchase (for products with orders)
    // Note: old_products don't have order tracking, so verified purchase only for shop_products
    $isVerifiedPurchase = false;
    if ($type === 'product') {
        $stmt = $db->prepare("
            SELECT oi.order_item_id 
            FROM order_items oi 
            JOIN orders o ON oi.order_id = o.order_id 
            WHERE oi.product_id = ? AND o.user_id = ? AND o.status IN ('delivered', 'completed')
        ");
        $stmt->execute([$entityId, $userId]);
        $isVerifiedPurchase = (bool)$stmt->fetch();
    }
    // Old products are C2C marketplace, no verified purchase tracking
    
    try {
        $stmt = $db->prepare("
            INSERT INTO reviews ($column, reviewer_id, rating, title, content, is_verified_purchase, is_approved, created_at)
            VALUES (?, ?, ?, ?, ?, ?, 1, NOW())
        ");
        $stmt->execute([$entityId, $userId, $rating, $title, $content, $isVerifiedPurchase ? 1 : 0]);
        $reviewId = (int)$db->lastInsertId();
        
        // Update entity rating (with exclusion conditions to count only correct review type)
        $ratingColumn = $type === 'product' ? 'avg_rating' : 'rating';
        $countColumn = 'review_count';
        
        $stmt = $db->prepare("
            UPDATE $table SET 
                $ratingColumn = (SELECT AVG(rating) FROM reviews WHERE $column = ? AND is_approved = 1 $exclusionCondition),
                $countColumn = (SELECT COUNT(*) FROM reviews WHERE $column = ? AND is_approved = 1 $exclusionCondition)
            WHERE $idColumn = ?
        ");
        $stmt->execute([$entityId, $entityId, $entityId]);
        
        successResponse([
            'review_id' => $reviewId,
            'message' => 'Review submitted successfully'
        ], 'Review added');
        
    } catch (Exception $e) {
        error_log("Add review error: " . $e->getMessage());
        errorResponse('Failed to submit review', 500);
    }
}
