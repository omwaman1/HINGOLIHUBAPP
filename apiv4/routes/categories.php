<?php
/**
 * Categories Routes Handler
 * GET /categories, /shop-categories, /old-categories
 */

/**
 * Categories routes
 */
function handleCategories(string $method, array $segments): void {
    if ($method !== 'GET') {
        errorResponse('Method not allowed', 405);
    }
    
    $categoryId = $segments[0] ?? null;
    $subResource = $segments[1] ?? null;
    
    if ($categoryId === null) {
        // GET /categories
        getCategories();
    } elseif ($subResource === 'subcategories') {
        // GET /categories/{id}/subcategories
        getSubcategories((int)$categoryId);
    } else {
        // GET /categories/{id}
        getCategoryById((int)$categoryId);
    }
}

/**
 * Get categories list
 * Now uses transformCategory from transformers.php
 */
function getCategories(): void {
    $type = getQueryParam('type');
    $parentId = getQueryParam('parent_id');
    
    $db = getDB();
    $sql = "SELECT category_id, parent_id, name, name_mr, slug, listing_type, depth, 
                   icon_url, image_url, description, listing_count
            FROM categories 
            WHERE is_active = 1";
    $params = [];
    
    if ($type) {
        $sql .= " AND listing_type = ?";
        $params[] = $type;
    }
    
    if ($parentId !== null) {
        if ($parentId === '0' || $parentId === '') {
            $sql .= " AND parent_id IS NULL";
        } else {
            $sql .= " AND parent_id = ?";
            $params[] = (int)$parentId;
        }
    } else {
        // By default, return only top-level categories
        $sql .= " AND parent_id IS NULL";
    }
    
    $sql .= " ORDER BY sort_order, name";
    
    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    $categories = $stmt->fetchAll();
    
    // Use transformer for consistent response format
    successResponse(array_map('transformCategory', $categories));
}

/**
 * Get category by ID
 * Now uses transformCategory from transformers.php
 */
function getCategoryById(int $categoryId): void {
    $db = getDB();
    $stmt = $db->prepare("
        SELECT category_id, parent_id, name, name_mr, slug, listing_type, depth,
               icon_url, image_url, description, listing_count
        FROM categories 
        WHERE category_id = ? AND is_active = 1
    ");
    $stmt->execute([$categoryId]);
    $category = $stmt->fetch();
    
    if (!$category) {
        errorResponse('Category not found', 404);
    }
    
    successResponse(transformCategory($category));
}

/**
 * Get subcategories
 * Now uses transformCategory from transformers.php
 */
function getSubcategories(int $parentId): void {
    $db = getDB();
    $stmt = $db->prepare("
        SELECT category_id, parent_id, name, name_mr, slug, listing_type, depth,
               icon_url, image_url, description, listing_count
        FROM categories 
        WHERE parent_id = ? AND is_active = 1
        ORDER BY sort_order, name
    ");
    $stmt->execute([$parentId]);
    $categories = $stmt->fetchAll();
    
    successResponse(array_map('transformCategory', $categories));
}

/**
 * Shop Categories routes: GET /shop-categories
 * Dedicated category system for e-commerce/shop section
 * Now uses unified category helpers
 */
function handleShopCategories(string $method, array $segments): void {
    handleCategoriesForTable($method, $segments, 'shop_categories', 'getShopCategoryProducts');
}

/**
 * Get products in a shop category
 */
function getShopCategoryProducts(int $categoryId): void {
    $db = getDB();
    $page = max(1, (int)(getQueryParam('page') ?? 1));
    $limit = min(50, max(10, (int)(getQueryParam('limit') ?? 20)));
    $offset = ($page - 1) * $limit;
    
    $stmt = $db->prepare("SELECT id FROM shop_categories WHERE (id = ? OR parent_id = ?) AND is_active = 1");
    $stmt->execute([$categoryId, $categoryId]);
    $categoryIds = array_column($stmt->fetchAll(), 'id');
    
    if (empty($categoryIds)) {
        errorResponse('Category not found', 404);
    }
    
    $placeholders = str_repeat('?,', count($categoryIds) - 1) . '?';
    $stmt = $db->prepare("
        SELECT sp.product_id, sp.product_name, sp.description, 
               sp.price, sp.discounted_price, sp.image_url,
               sp.stock_qty, sp.sell_online, sp.condition,
               sc.name as category_name, l.title as shop_name
        FROM shop_products sp
        LEFT JOIN shop_categories sc ON sp.shop_category_id = sc.id
        LEFT JOIN listings l ON sp.listing_id = l.listing_id
        WHERE sp.shop_category_id IN ($placeholders) AND sp.is_active = 1
        ORDER BY sp.sort_order, sp.product_name
        LIMIT ? OFFSET ?
    ");
    $params = array_merge($categoryIds, [$limit, $offset]);
    $stmt->execute($params);
    $products = $stmt->fetchAll();
    
    $stmt = $db->prepare("SELECT COUNT(*) as total FROM shop_products WHERE shop_category_id IN ($placeholders) AND is_active = 1");
    $stmt->execute($categoryIds);
    $total = (int)$stmt->fetch()['total'];
    
    $products = array_map(function($p) {
        return [
            'product_id' => (int)$p['product_id'],
            'product_name' => $p['product_name'],
            'description' => $p['description'],
            'price' => (float)$p['price'],
            'discounted_price' => $p['discounted_price'] ? (float)$p['discounted_price'] : null,
            'image_url' => $p['image_url'],
            'stock_qty' => $p['stock_qty'] ? (int)$p['stock_qty'] : null,
            'sell_online' => (bool)$p['sell_online'],
            'condition' => $p['condition'],
            'category_name' => $p['category_name'],
            'shop_name' => $p['shop_name']
        ];
    }, $products);
    
    successResponse([
        'products' => $products,
        'pagination' => ['page' => $page, 'limit' => $limit, 'total' => $total, 'total_pages' => ceil($total / $limit)]
    ]);
}

/**
 * Old Categories routes: GET /old-categories
 * Now uses unified category helpers
 */
function handleOldCategories(string $method, array $segments): void {
    handleCategoriesForTable($method, $segments, 'old_categories');
}
