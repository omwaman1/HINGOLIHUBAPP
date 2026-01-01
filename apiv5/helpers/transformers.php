<?php
/**
 * Model Transformers
 * Centralized type casting and response formatting for all models
 */

/**
 * Transform category row to API response format
 */
function transformCategory(array $cat): array {
    return [
        'category_id' => (int)$cat['category_id'],
        'parent_id' => $cat['parent_id'] ? (int)$cat['parent_id'] : null,
        'name' => $cat['name'],
        'name_mr' => $cat['name_mr'] ?? null,
        'slug' => $cat['slug'] ?? null,
        'listing_type' => $cat['listing_type'] ?? null,
        'depth' => isset($cat['depth']) ? (int)$cat['depth'] : null,
        'icon_url' => $cat['icon_url'] ?? null,
        'image_url' => $cat['image_url'] ?? null,
        'description' => $cat['description'] ?? null,
        'listing_count' => isset($cat['listing_count']) ? (int)$cat['listing_count'] : 0
    ];
}

/**
 * Transform shop/old category row to API response format
 * Used for shop_categories and old_categories tables
 */
function transformShopCategory(array $cat): array {
    return [
        'id' => (int)$cat['id'],
        'parent_id' => $cat['parent_id'] ? (int)$cat['parent_id'] : null,
        'level' => isset($cat['level']) ? (int)$cat['level'] : 1,
        'name' => $cat['name'],
        'name_mr' => $cat['name_mr'] ?? null,
        'slug' => $cat['slug'] ?? null,
        'icon' => $cat['icon'] ?? null,
        'color' => $cat['color'] ?? null,
        'image_url' => $cat['image_url'] ?? null,
        'sort_order' => isset($cat['sort_order']) ? (int)$cat['sort_order'] : 0,
        'product_count' => isset($cat['product_count']) ? (int)$cat['product_count'] : 0
    ];
}

/**
 * Transform shop product row to API response format
 */
function transformShopProduct(array $p): array {
    return [
        'product_id' => (int)$p['product_id'],
        'listing_id' => isset($p['listing_id']) ? (int)$p['listing_id'] : null,
        'product_name' => $p['product_name'],
        'description' => $p['description'] ?? null,
        'category_id' => isset($p['category_id']) ? (int)$p['category_id'] : null,
        'shop_category_id' => isset($p['shop_category_id']) ? (int)$p['shop_category_id'] : null,
        'category_name' => $p['category_name'] ?? null,
        'category_name_mr' => $p['category_name_mr'] ?? null,
        'subcategory_id' => $p['subcategory_id'] ? (int)$p['subcategory_id'] : null,
        'subcategory_name' => $p['subcategory_name'] ?? null,
        'price' => (float)$p['price'],
        'discounted_price' => $p['discounted_price'] ? (float)$p['discounted_price'] : null,
        'image_url' => $p['image_url'] ?? null,
        'stock_qty' => $p['stock_qty'] ? (int)$p['stock_qty'] : null,
        'min_qty' => isset($p['min_qty']) ? (int)$p['min_qty'] : 1,
        'sell_online' => (bool)($p['sell_online'] ?? false),
        'condition' => $p['condition'] ?? 'new',
        'business_name' => $p['business_name'] ?? null,
        'business_phone' => $p['business_phone'] ?? null,
        'city' => $p['city'] ?? null,
        'user_id' => isset($p['user_id']) ? (int)$p['user_id'] : null,
        'created_at' => $p['created_at'] ?? null
    ];
}

/**
 * Transform old product row to API response format
 */
function transformOldProduct(array $p): array {
    return [
        'product_id' => (int)$p['product_id'],
        'product_name' => $p['product_name'],
        'description' => $p['description'] ?? null,
        'price' => (float)$p['price'],
        'original_price' => $p['original_price'] ? (float)$p['original_price'] : null,
        'image_url' => $p['image_url'] ?? null,
        'condition' => $p['condition'] ?? 'used',
        'age_months' => $p['age_months'] ? (int)$p['age_months'] : null,
        'has_warranty' => (bool)($p['has_warranty'] ?? false),
        'has_bill' => (bool)($p['has_bill'] ?? false),
        'brand' => $p['brand'] ?? null,
        'model' => $p['model'] ?? null,
        'city' => $p['city'] ?? null,
        'accept_offers' => (bool)($p['accept_offers'] ?? false),
        'view_count' => isset($p['view_count']) ? (int)$p['view_count'] : 0,
        'category_name' => $p['category_name'] ?? null,
        'category_name_mr' => $p['category_name_mr'] ?? null,
        'seller_name' => $p['seller_name'] ?? null,
        'seller_phone' => $p['seller_phone'] ?? null,
        'show_phone' => (bool)($p['show_phone'] ?? false),
        'user_id' => isset($p['user_id']) ? (int)$p['user_id'] : null,
        'created_at' => $p['created_at'] ?? null
    ];
}

/**
 * Transform listing row to API response format
 */
function transformListing(array $l): array {
    return [
        'listing_id' => (int)$l['listing_id'],
        'user_id' => (int)$l['user_id'],
        'title' => $l['title'],
        'description' => $l['description'] ?? null,
        'listing_type' => $l['listing_type'],
        'category_id' => $l['category_id'] ? (int)$l['category_id'] : null,
        'category_name' => $l['category_name'] ?? null,
        'category_name_mr' => $l['category_name_mr'] ?? null,
        'subcategory_id' => $l['subcategory_id'] ? (int)$l['subcategory_id'] : null,
        'subcategory_name' => $l['subcategory_name'] ?? null,
        'city' => $l['city'] ?? null,
        'address' => $l['address'] ?? null,
        'phone' => $l['phone'] ?? null,
        'whatsapp' => $l['whatsapp'] ?? null,
        'email' => $l['email'] ?? null,
        'website' => $l['website'] ?? null,
        'main_image_url' => $l['main_image_url'] ?? null,
        'status' => $l['status'] ?? 'active',
        'is_featured' => (bool)($l['is_featured'] ?? false),
        'is_verified' => (bool)($l['is_verified'] ?? false),
        'view_count' => isset($l['view_count']) ? (int)$l['view_count'] : 0,
        'contact_count' => isset($l['contact_count']) ? (int)$l['contact_count'] : 0,
        'rating' => isset($l['rating']) ? (float)$l['rating'] : null,
        'review_count' => isset($l['review_count']) ? (int)$l['review_count'] : 0,
        'created_at' => $l['created_at'] ?? null,
        'updated_at' => $l['updated_at'] ?? null
    ];
}

/**
 * Transform order row to API response format
 */
function transformOrder(array $o): array {
    return [
        'order_id' => (int)$o['order_id'],
        'order_number' => $o['order_number'],
        'user_id' => (int)$o['user_id'],
        'total_amount' => (float)$o['total_amount'],
        'delivery_fee' => isset($o['delivery_fee']) ? (float)$o['delivery_fee'] : 0,
        'status' => $o['status'],
        'payment_status' => $o['payment_status'] ?? 'pending',
        'payment_method' => $o['payment_method'] ?? null,
        'razorpay_order_id' => $o['razorpay_order_id'] ?? null,
        'razorpay_payment_id' => $o['razorpay_payment_id'] ?? null,
        'delivery_address' => $o['delivery_address'] ?? null,
        'delivery_pincode' => $o['delivery_pincode'] ?? null,
        'delivery_city' => $o['delivery_city'] ?? null,
        'estimated_delivery' => $o['estimated_delivery'] ?? null,
        'created_at' => $o['created_at'] ?? null,
        'updated_at' => $o['updated_at'] ?? null
    ];
}

/**
 * Transform cart item row to API response format
 */
function transformCartItem(array $c): array {
    return [
        'cart_item_id' => (int)$c['cart_item_id'],
        'product_id' => (int)$c['product_id'],
        'quantity' => (int)$c['quantity'],
        'product_name' => $c['product_name'] ?? null,
        'price' => isset($c['price']) ? (float)$c['price'] : 0,
        'discounted_price' => $c['discounted_price'] ? (float)$c['discounted_price'] : null,
        'image_url' => $c['image_url'] ?? null,
        'stock_qty' => $c['stock_qty'] ? (int)$c['stock_qty'] : null,
        'seller_id' => isset($c['seller_id']) ? (int)$c['seller_id'] : null,
        'seller_name' => $c['seller_name'] ?? null
    ];
}

/**
 * Transform user row to API response format
 */
function transformUser(array $u): array {
    return [
        'user_id' => (int)$u['user_id'],
        'username' => $u['username'] ?? null,
        'phone' => $u['phone'],
        'email' => $u['email'] ?? null,
        'avatar_url' => $u['avatar_url'] ?? null,
        'city' => $u['city'] ?? null,
        'is_verified' => (bool)($u['is_verified'] ?? false),
        'created_at' => $u['created_at'] ?? null
    ];
}

/**
 * Transform address row to API response format
 */
function transformAddress(array $a): array {
    return [
        'address_id' => (int)$a['address_id'],
        'full_name' => $a['full_name'],
        'phone' => $a['phone'],
        'address_line1' => $a['address_line1'],
        'address_line2' => $a['address_line2'] ?? null,
        'city' => $a['city'],
        'state' => $a['state'] ?? 'Maharashtra',
        'pincode' => $a['pincode'],
        'is_default' => (bool)($a['is_default'] ?? false)
    ];
}

/**
 * Transform banner row to API response format
 */
function transformBanner(array $b): array {
    return [
        'banner_id' => (int)$b['banner_id'],
        'title' => $b['title'] ?? null,
        'image_url' => $b['image_url'],
        'link_type' => $b['link_type'] ?? null,
        'link_value' => $b['link_value'] ?? null,
        'placement' => $b['placement'] ?? 'home_top',
        'sort_order' => isset($b['sort_order']) ? (int)$b['sort_order'] : 0
    ];
}

/**
 * Transform review row to API response format
 */
function transformReview(array $r): array {
    return [
        'review_id' => (int)$r['review_id'],
        'rating' => (int)$r['rating'],
        'comment' => $r['comment'] ?? null,
        'image_url' => $r['image_url'] ?? null,
        'user_id' => (int)$r['user_id'],
        'username' => $r['username'] ?? 'Anonymous',
        'avatar_url' => $r['avatar_url'] ?? null,
        'created_at' => $r['created_at'] ?? null
    ];
}
