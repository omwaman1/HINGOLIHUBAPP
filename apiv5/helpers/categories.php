<?php
/**
 * Category Helpers
 * Unified functions for handling shop_categories and old_categories
 * These tables have identical structure, so we use a single implementation
 */

/**
 * Get categories from a table (works for shop_categories and old_categories)
 * @param string $table Table name (shop_categories or old_categories)
 */
function getCategoriesFromTable(string $table): void {
    $level = getQueryParam('level');
    $parentId = getQueryParam('parent_id');
    $withSubcategories = getQueryParam('with_subcategories') === '1';
    
    $db = getDB();
    
    $sql = "SELECT id, parent_id, level, name, name_mr, slug, icon, color, 
                   image_url, sort_order, product_count
            FROM $table WHERE is_active = 1";
    $params = [];
    
    if ($level !== null) {
        $sql .= " AND level = ?";
        $params[] = (int)$level;
    }
    
    if ($parentId !== null) {
        if ($parentId === '0' || $parentId === '') {
            $sql .= " AND parent_id IS NULL";
        } else {
            $sql .= " AND parent_id = ?";
            $params[] = (int)$parentId;
        }
    } elseif ($level === null) {
        $sql .= " AND level = 1";
    }
    
    $sql .= " ORDER BY sort_order, name";
    
    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    $categories = $stmt->fetchAll();
    
    // Transform categories
    $categories = array_map('transformShopCategory', $categories);
    
    // Load subcategories if requested
    if ($withSubcategories && !empty($categories)) {
        $categoryIds = array_column($categories, 'id');
        $placeholders = str_repeat('?,', count($categoryIds) - 1) . '?';
        
        $stmt = $db->prepare("
            SELECT id, parent_id, level, name, name_mr, slug, icon, color, 
                   image_url, sort_order, product_count
            FROM $table WHERE parent_id IN ($placeholders) AND is_active = 1
            ORDER BY sort_order, name
        ");
        $stmt->execute($categoryIds);
        $subcategories = $stmt->fetchAll();
        
        // Group subcategories by parent
        $subcatByParent = [];
        foreach ($subcategories as $subcat) {
            $pid = (int)$subcat['parent_id'];
            if (!isset($subcatByParent[$pid])) $subcatByParent[$pid] = [];
            $subcatByParent[$pid][] = transformShopCategory($subcat);
        }
        
        // Attach subcategories to parents
        foreach ($categories as &$cat) {
            $cat['subcategories'] = $subcatByParent[$cat['id']] ?? [];
        }
    }
    
    successResponse($categories);
}

/**
 * Get category by ID from a table
 * @param string $table Table name
 * @param int $categoryId Category ID
 */
function getCategoryByIdFromTable(string $table, int $categoryId): void {
    $db = getDB();
    $stmt = $db->prepare("
        SELECT id, parent_id, level, name, name_mr, slug, icon, color, 
               image_url, sort_order, product_count
        FROM $table 
        WHERE id = ? AND is_active = 1
    ");
    $stmt->execute([$categoryId]);
    $category = $stmt->fetch();
    
    if (!$category) {
        errorResponse('Category not found', 404);
    }
    
    $result = transformShopCategory($category);
    
    // Get subcategories
    $stmt = $db->prepare("
        SELECT id, parent_id, level, name, name_mr, slug, icon, color, 
               image_url, sort_order, product_count
        FROM $table 
        WHERE parent_id = ? AND is_active = 1 
        ORDER BY sort_order, name
    ");
    $stmt->execute([$categoryId]);
    $subcategories = $stmt->fetchAll();
    
    $result['subcategories'] = array_map('transformShopCategory', $subcategories);
    
    successResponse($result);
}

/**
 * Get subcategories from a table
 * @param string $table Table name
 * @param int $parentId Parent category ID
 */
function getSubcategoriesFromTable(string $table, int $parentId): void {
    $db = getDB();
    $stmt = $db->prepare("
        SELECT id, parent_id, level, name, name_mr, slug, icon, color, 
               image_url, sort_order, product_count
        FROM $table 
        WHERE parent_id = ? AND is_active = 1 
        ORDER BY sort_order, name
    ");
    $stmt->execute([$parentId]);
    $subcategories = $stmt->fetchAll();
    
    successResponse(array_map('transformShopCategory', $subcategories));
}

/**
 * Handle category routes for a specific table
 * @param string $method HTTP method
 * @param array $segments URL segments
 * @param string $table Table name
 * @param callable|null $productsHandler Optional handler for getting products in category
 */
function handleCategoriesForTable(string $method, array $segments, string $table, ?callable $productsHandler = null): void {
    if ($method !== 'GET') {
        errorResponse('Method not allowed', 405);
    }
    
    $categoryId = $segments[0] ?? null;
    $subResource = $segments[1] ?? null;
    
    if ($categoryId === null) {
        getCategoriesFromTable($table);
    } elseif ($subResource === 'subcategories') {
        getSubcategoriesFromTable($table, (int)$categoryId);
    } elseif ($subResource === 'products' && $productsHandler !== null) {
        $productsHandler((int)$categoryId);
    } else {
        getCategoryByIdFromTable($table, (int)$categoryId);
    }
}
