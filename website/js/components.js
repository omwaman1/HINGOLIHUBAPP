// =====================================================
// Hingoli Hub Marketplace - UI Components
// =====================================================

const components = {
    // Listing Card
    listingCard(listing) {
        const rating = listing.avg_rating > 0 ? `
            <div class="listing-rating">
                <span>★</span>
                <span>${parseFloat(listing.avg_rating).toFixed(1)}</span>
            </div>
        ` : '';

        const price = listing.price ? `
            <div class="listing-price">₹${this.formatPrice(listing.price)}</div>
        ` : '';

        return `
            <article class="listing-card" onclick="app.viewListing(${listing.listing_id})">
                <img 
                    src="${listing.main_image_url || 'https://via.placeholder.com/300x200?text=No+Image'}" 
                    alt="${listing.title}"
                    class="listing-image"
                    loading="lazy"
                    onerror="this.src='https://via.placeholder.com/300x200?text=No+Image'"
                >
                <div class="listing-content">
                    <span class="listing-category">${listing.category_name || listing.listing_type}</span>
                    <h3 class="listing-title">${listing.title}</h3>
                    <div class="listing-location">
                        <span>📍</span>
                        <span>${listing.city || 'Hingoli'}</span>
                    </div>
                    <div class="listing-meta">
                        ${rating}
                        ${price}
                    </div>
                </div>
            </article>
        `;
    },

    // Category Card
    categoryCard(category, onClick) {
        const icons = {
            'services': '🔧',
            'selling': '🛒',
            'business': '🏪',
            'jobs': '💼'
        };

        const icon = icons[category.listing_type] || '📦';

        return `
            <div class="category-card" onclick="${onClick}">
                <div class="category-icon">${icon}</div>
                <h3 class="category-title">${category.name}</h3>
                <p class="category-count">${category.listing_count || 0} listings</p>
            </div>
        `;
    },

    // Main Category Card (for homepage)
    mainCategoryCard(type, title, description, icon) {
        return `
            <div class="category-card" onclick="app.goToListings('${type}')">
                <div class="category-icon">${icon}</div>
                <h3 class="category-title">${title}</h3>
                <p class="category-count">${description}</p>
            </div>
        `;
    },

    // Skeleton Loader for listing
    listingCardSkeleton() {
        return `
            <div class="listing-card">
                <div class="skeleton skeleton-image"></div>
                <div class="listing-content">
                    <div class="skeleton skeleton-text" style="width: 40%"></div>
                    <div class="skeleton skeleton-text"></div>
                    <div class="skeleton skeleton-text-sm"></div>
                </div>
            </div>
        `;
    },

    // Banner Slide
    bannerSlide(banner) {
        return `
            <div class="banner-slide">
                <img src="${banner.image_url}" alt="${banner.title || 'Banner'}" loading="lazy">
            </div>
        `;
    },

    // Format price
    formatPrice(price) {
        if (!price) return '';
        return new Intl.NumberFormat('en-IN').format(price);
    },

    // Empty state
    emptyState(message, icon = '📭') {
        return `
            <div class="empty-state" style="text-align: center; padding: 3rem; color: var(--gray-500);">
                <div style="font-size: 3rem; margin-bottom: 1rem;">${icon}</div>
                <p>${message}</p>
            </div>
        `;
    },

    // Error state
    errorState(message) {
        return `
            <div class="error-state" style="text-align: center; padding: 3rem; color: var(--error);">
                <div style="font-size: 3rem; margin-bottom: 1rem;">❌</div>
                <p>${message}</p>
                <button class="btn btn-outline mt-4" onclick="location.reload()">Try Again</button>
            </div>
        `;
    },

    // Product Card (for Buy & Sell / Shop Products)
    productCard(product) {
        const hasDiscount = product.discounted_price && product.discounted_price < product.price;
        const displayPrice = hasDiscount ? product.discounted_price : product.price;

        const priceHtml = hasDiscount ? `
            <div class="listing-price">
                <span style="text-decoration: line-through; color: var(--gray-400); font-size: 0.75rem;">₹${this.formatPrice(product.price)}</span>
                <span style="color: var(--success); font-weight: 600;">₹${this.formatPrice(displayPrice)}</span>
            </div>
        ` : `<div class="listing-price" style="font-weight: 600; color: var(--primary-600);">₹${this.formatPrice(product.price)}</div>`;

        return `
            <article class="listing-card" onclick="app.viewProduct(${product.product_id})">
                <img 
                    src="${product.image_url || 'https://via.placeholder.com/300x200?text=No+Image'}" 
                    alt="${product.product_name}"
                    class="listing-image"
                    loading="lazy"
                    onerror="this.src='https://via.placeholder.com/300x200?text=No+Image'"
                >
                <div class="listing-content">
                    <span class="listing-category">${product.category_name || 'Product'}</span>
                    <h3 class="listing-title">${product.product_name}</h3>
                    <div class="listing-location">
                        <span>🏪</span>
                        <span>${product.business_name || product.city || 'Hingoli'}</span>
                    </div>
                    <div class="listing-meta">
                        ${priceHtml}
                    </div>
                </div>
            </article>
        `;
    }
};

// Export for use
window.components = components;
