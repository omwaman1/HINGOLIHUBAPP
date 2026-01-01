// =====================================================
// Hingoli Hub™ Marketplace - Main Application
// =====================================================

// Configuration
const CONFIG = {
    SUPPORT_PHONE: '9096632830',
    WHATSAPP_MESSAGE: (title) => `Hi, I'm interested in "${title}" on Hingoli Hub™.`,
    LIVE_CHAT_ENABLED: true
};

const app = {
    // Initialize the app
    async init() {
        console.log('Hingoli Hub™ Marketplace initialized');

        // Initialize auth state
        this.initAuth();

        // Load data based on current page
        const page = this.getCurrentPage();

        switch (page) {
            case 'index':
            case '':
                await this.loadHomePage();
                break;
            case 'listings':
                await this.loadListingsPage();
                break;
            case 'listing-detail':
                await this.loadListingDetail();
                break;
        }

        // Initialize search
        this.initSearch();

        // Initialize live chat
        this.initLiveChat();
    },

    // Get current page name
    getCurrentPage() {
        const path = window.location.pathname;
        const page = path.split('/').pop().replace('.html', '');
        return page || 'index';
    },

    // Initialize search functionality
    initSearch() {
        const searchForm = document.getElementById('searchForm');
        if (searchForm) {
            searchForm.addEventListener('submit', (e) => {
                e.preventDefault();
                const query = document.getElementById('searchInput').value;
                const category = document.getElementById('categorySelect')?.value || '';
                this.performSearch(query, category);
            });
        }
    },

    // Initialize Tawk.to Live Chat
    initLiveChat() {
        if (!CONFIG.LIVE_CHAT_ENABLED) return;

        // Tawk.to Live Chat Widget
        var Tawk_API = Tawk_API || {}, Tawk_LoadStart = new Date();
        (function () {
            var s1 = document.createElement("script"), s0 = document.getElementsByTagName("script")[0];
            s1.async = true;
            s1.src = 'https://embed.tawk.to/676ccef2af5bfec1dbe12c48/1ifvcrjd7';
            s1.charset = 'UTF-8';
            s1.setAttribute('crossorigin', '*');
            s0.parentNode.insertBefore(s1, s0);
        })();

        window.Tawk_API = Tawk_API;
    },

    // Open live chat
    openLiveChat() {
        if (window.Tawk_API && window.Tawk_API.maximize) {
            window.Tawk_API.maximize();
        } else {
            // Fallback - redirect to WhatsApp
            this.openWhatsApp('General Inquiry');
        }
    },

    // Call support
    callSupport() {
        window.location.href = `tel:+91${CONFIG.SUPPORT_PHONE}`;
    },

    // Open WhatsApp with message
    openWhatsApp(listingTitle = '') {
        const message = CONFIG.WHATSAPP_MESSAGE(listingTitle || 'your services');
        const url = `https://wa.me/91${CONFIG.SUPPORT_PHONE}?text=${encodeURIComponent(message)}`;
        window.open(url, '_blank');
    },

    // Perform search
    performSearch(query, category = '') {
        const params = new URLSearchParams();
        if (query) params.set('search', query);
        if (category) params.set('type', category);
        window.location.href = `listings.html?${params.toString()}`;
    },

    // Navigate to listings page
    goToListings(type = '') {
        const params = type ? `?type=${type}` : '';
        window.location.href = `listings.html${params}`;
    },

    // View a single listing
    viewListing(id) {
        window.location.href = `listing-detail.html?id=${id}`;
    },

    // View a product (Buy & Sell item)
    viewProduct(id) {
        window.location.href = `product-detail.html?id=${id}`;
    },

    // =====================================================
    // Homepage
    // =====================================================
    async loadHomePage() {
        // Load featured listings
        await this.loadFeaturedListings();

        // Load recent listings by type
        await this.loadRecentByType();
    },

    async loadBanners() {
        const container = document.getElementById('bannerCarousel');
        if (!container) return;

        try {
            const response = await api.getBanners('home_top');

            if (response.success && response.data?.length > 0) {
                const banners = response.data;
                container.innerHTML = `
                    <div class="banner-slides">
                        ${banners.map((b, i) => `
                            <div class="banner-slide ${i === 0 ? 'active' : ''}" style="background-image: url('${b.image_url}')">
                                ${b.title ? `<div class="banner-overlay"><h3>${b.title}</h3></div>` : ''}
                            </div>
                        `).join('')}
                    </div>
                    ${banners.length > 1 ? `
                        <div class="banner-dots">
                            ${banners.map((_, i) => `<span class="dot ${i === 0 ? 'active' : ''}" onclick="app.goToSlide(${i})"></span>`).join('')}
                        </div>
                    ` : ''}
                `;

                // Auto-rotate
                if (banners.length > 1) {
                    this.startBannerRotation(banners.length);
                }
            }
        } catch (error) {
            console.log('Banners not available');
        }
    },

    currentSlide: 0,
    bannerInterval: null,

    startBannerRotation(totalSlides) {
        this.bannerInterval = setInterval(() => {
            this.currentSlide = (this.currentSlide + 1) % totalSlides;
            this.goToSlide(this.currentSlide);
        }, 4000);
    },

    goToSlide(index) {
        this.currentSlide = index;
        const slides = document.querySelectorAll('.banner-slide');
        const dots = document.querySelectorAll('.banner-dots .dot');

        slides.forEach((slide, i) => {
            slide.classList.toggle('active', i === index);
        });
        dots.forEach((dot, i) => {
            dot.classList.toggle('active', i === index);
        });
    },

    async loadFeaturedListings() {
        const container = document.getElementById('featuredListings');
        if (!container) return;

        container.innerHTML = Array(4).fill(components.listingCardSkeleton()).join('');

        try {
            const response = await api.getListings({
                is_featured: 1,
                per_page: 8,
                status: 'active'
            });

            const listings = Array.isArray(response.data) ? response.data : response.data?.listings || [];

            if (response.success && listings.length > 0) {
                container.innerHTML = listings
                    .map(listing => components.listingCard(listing))
                    .join('');
            } else {
                container.innerHTML = components.emptyState('No featured listings yet');
            }
        } catch (error) {
            container.innerHTML = components.errorState('Failed to load listings');
        }
    },

    async loadRecentByType() {
        // Load regular listings (services, business, jobs)
        const listingTypes = ['services', 'business', 'jobs'];

        for (const type of listingTypes) {
            const container = document.getElementById(`${type}Listings`);
            if (!container) continue;

            container.innerHTML = Array(4).fill(components.listingCardSkeleton()).join('');

            try {
                const response = await api.getListings({
                    type,
                    per_page: 4,
                    status: 'active'
                });

                const listings = Array.isArray(response.data) ? response.data : response.data?.listings || [];

                if (response.success && listings.length > 0) {
                    container.innerHTML = listings
                        .map(listing => components.listingCard(listing))
                        .join('');
                } else {
                    container.innerHTML = components.emptyState(`No ${type} listings yet`);
                }
            } catch (error) {
                container.innerHTML = components.errorState('Failed to load');
            }
        }

        // Load products for Buy & Sell section (uses /products endpoint)
        const sellingContainer = document.getElementById('sellingListings');
        if (sellingContainer) {
            sellingContainer.innerHTML = Array(4).fill(components.listingCardSkeleton()).join('');

            try {
                const response = await api.getProducts({ per_page: 8 });

                // Products come in response.data.products array
                const products = response.data?.products || response.data || [];

                if (response.success && products.length > 0) {
                    sellingContainer.innerHTML = products
                        .slice(0, 8)
                        .map(product => components.productCard(product))
                        .join('');
                } else {
                    sellingContainer.innerHTML = components.emptyState('No products for sale yet');
                }
            } catch (error) {
                console.error('Products error:', error);
                sellingContainer.innerHTML = components.emptyState('No products for sale yet');
            }
        }
    },

    // =====================================================
    // Listings Page
    // =====================================================
    async loadListingsPage() {
        const params = new URLSearchParams(window.location.search);
        const type = params.get('type') || '';
        const category = params.get('category') || '';
        const search = params.get('search') || '';
        const page = params.get('page') || 1;

        // Update page title
        if (type) {
            const titles = {
                'services': 'Services',
                'selling': 'Buy & Sell',
                'business': 'Local Businesses',
                'jobs': 'Jobs'
            };
            const titleEl = document.getElementById('pageTitle');
            if (titleEl) titleEl.textContent = titles[type] || 'All Listings';
        }

        // Load categories for filter
        await this.loadCategoryFilters(type);

        // Load listings
        await this.loadListings({ type, category_id: category, search, page });
    },

    async loadCategoryFilters(type) {
        const container = document.getElementById('categoryFilters');
        if (!container) return;

        try {
            const params = type ? { type } : {};
            const response = await api.getCategories(params);

            if (response.success && response.data?.length > 0) {
                const categories = response.data.filter(c => !c.parent_id);
                container.innerHTML = categories.map(cat => `
                    <label class="filter-option">
                        <input type="radio" name="category" value="${cat.category_id}" 
                            onchange="app.filterByCategory(${cat.category_id})">
                        <span>${cat.name}</span>
                    </label>
                `).join('');
            }
        } catch (error) {
            console.error('Failed to load categories:', error);
        }
    },

    async loadListings(filters = {}) {
        const container = document.getElementById('listingsGrid');
        if (!container) return;

        container.innerHTML = Array(8).fill(components.listingCardSkeleton()).join('');

        try {
            const response = await api.getListings({
                ...filters,
                per_page: 20,
                status: 'active'
            });

            const listings = Array.isArray(response.data) ? response.data : response.data?.listings || [];

            if (response.success && listings.length > 0) {
                container.innerHTML = listings
                    .map(listing => components.listingCard(listing))
                    .join('');

                const countEl = document.getElementById('listingsCount');
                if (countEl) {
                    const total = response.pagination?.total || listings.length;
                    countEl.textContent = `${total} listings found`;
                }
            } else {
                container.innerHTML = components.emptyState('No listings found matching your criteria');
            }
        } catch (error) {
            container.innerHTML = components.errorState('Failed to load listings');
        }
    },

    filterByCategory(categoryId) {
        const params = new URLSearchParams(window.location.search);
        params.set('category', categoryId);
        window.location.search = params.toString();
    },

    filterByType(type) {
        const params = new URLSearchParams(window.location.search);
        if (type) {
            params.set('type', type);
        } else {
            params.delete('type');
        }
        params.delete('category');
        window.location.search = params.toString();
    },

    // =====================================================
    // Listing Detail Page
    // =====================================================
    currentListing: null,

    async loadListingDetail() {
        const params = new URLSearchParams(window.location.search);
        const id = params.get('id');

        if (!id) {
            document.getElementById('listingDetail').innerHTML =
                components.errorState('Listing not found');
            return;
        }

        try {
            const response = await api.getListingById(id);

            if (response.success && response.data) {
                this.currentListing = response.data;
                this.renderListingDetail(response.data);

                // Load reviews
                await this.loadReviews(id);

                // Load price list for service listings
                if (response.data.listing_type === 'services') {
                    await this.loadPriceList(id);
                }
            } else {
                document.getElementById('listingDetail').innerHTML =
                    components.errorState('Listing not found');
            }
        } catch (error) {
            document.getElementById('listingDetail').innerHTML =
                components.errorState('Failed to load listing details');
        }
    },

    renderListingDetail(listing) {
        document.title = `${listing.title} - Hingoli Hub™`;

        // Update hero image
        const heroImage = document.getElementById('heroImage');
        if (heroImage) {
            heroImage.src = listing.main_image_url || 'https://via.placeholder.com/800x400?text=No+Image';
            heroImage.onerror = () => heroImage.src = 'https://via.placeholder.com/800x400?text=No+Image';
        }

        // Update title
        const titleEl = document.getElementById('listingTitle');
        if (titleEl) titleEl.textContent = listing.title;

        // Update category badge
        const categoryEl = document.getElementById('listingCategory');
        if (categoryEl) categoryEl.textContent = listing.category_name || listing.listing_type;

        // Update location
        const locationEl = document.getElementById('listingLocation');
        if (locationEl) locationEl.textContent = listing.location || listing.city || 'Hingoli';

        // Update description
        const descEl = document.getElementById('listingDescription');
        if (descEl) descEl.textContent = listing.description || 'No description provided.';

        // Update rating
        if (listing.avg_rating > 0) {
            const ratingEl = document.getElementById('listingRating');
            if (ratingEl) ratingEl.textContent = `★ ${parseFloat(listing.avg_rating).toFixed(1)}`;
        }

        // Update views
        const viewsEl = document.getElementById('listingViews');
        if (viewsEl) viewsEl.textContent = `${listing.view_count || 0} views`;

        // Set up contact buttons
        this.setupContactButtons(listing);
    },

    async loadReviews(listingId) {
        const container = document.getElementById('reviewsContainer');
        if (!container) return;

        try {
            const response = await api.getListingReviews(listingId);

            if (response.success && response.data?.length > 0) {
                container.innerHTML = response.data.map(review => `
                    <div class="review-card">
                        <div class="review-header">
                            <span class="review-rating">${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}</span>
                            <span class="review-date">${new Date(review.created_at).toLocaleDateString()}</span>
                        </div>
                        <p class="review-text">${review.comment || ''}</p>
                        <p class="review-author">- ${review.reviewer_name || 'Anonymous'}</p>
                    </div>
                `).join('');
            } else {
                container.innerHTML = '<p class="no-reviews">No reviews yet</p>';
            }
        } catch (error) {
            console.log('Reviews not available');
        }
    },

    async loadPriceList(listingId) {
        const container = document.getElementById('priceListContainer');
        if (!container) return;

        try {
            const response = await api.getListingPriceList(listingId);

            if (response.success && response.data?.length > 0) {
                // Show the price list section
                const section = document.getElementById('priceListSection');
                if (section) section.style.display = 'block';

                container.innerHTML = `
                    <table class="price-table">
                        <thead>
                            <tr><th>Service</th><th>Price</th></tr>
                        </thead>
                        <tbody>
                            ${response.data.map(item => `
                                <tr>
                                    <td>${item.name}</td>
                                    <td>₹${item.price}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                `;
            }
        } catch (error) {
            console.log('Price list not available');
        }
    },

    setupContactButtons(listing) {
        const callBtn = document.getElementById('callButton');
        const whatsappBtn = document.getElementById('whatsappButton');
        const chatBtn = document.getElementById('chatButton');

        // Call button - uses support phone
        if (callBtn) {
            callBtn.onclick = () => this.callSupport();
        }

        // WhatsApp button - opens WhatsApp with pre-filled message
        if (whatsappBtn) {
            whatsappBtn.onclick = () => this.openWhatsApp(listing.title);
        }

        // Chat button - opens live chat
        if (chatBtn) {
            chatBtn.onclick = () => this.openLiveChat();
        }
    },

    // Share listing
    shareListing(listing) {
        const url = window.location.href;
        const text = `Check out "${listing?.title || 'this listing'}" on Hingoli Hub™!`;

        if (navigator.share) {
            navigator.share({ title: listing?.title, text, url });
        } else {
            // Fallback - copy to clipboard
            navigator.clipboard.writeText(`${text}\n${url}`);
            alert('Link copied to clipboard!');
        }
    },

    // =====================================================
    // Authentication
    // =====================================================

    // Initialize auth state in navigation
    initAuth() {
        const authNav = document.getElementById('authNav');
        if (!authNav) return;

        if (this.isLoggedIn()) {
            const user = this.getUser();
            authNav.innerHTML = `
                <div class="user-menu" style="display: flex; align-items: center; gap: 8px;">
                    <a href="dashboard.html" style="color: white; text-decoration: none; font-size: 13px;">👤 ${user?.username || 'User'}</a>
                    <button onclick="app.logout()" class="btn btn-white" style="padding: 4px 12px; font-size: 11px;">Logout</button>
                </div>
            `;
        } else {
            authNav.innerHTML = `<a href="login.html" class="btn btn-white">👤 Login</a>`;
        }
    },

    // Check if user is logged in
    isLoggedIn() {
        return !!localStorage.getItem('auth_token');
    },

    // Get current user
    getUser() {
        try {
            return JSON.parse(localStorage.getItem('user'));
        } catch {
            return null;
        }
    },

    // Get auth token
    getToken() {
        return localStorage.getItem('auth_token');
    },

    // Logout
    logout() {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('user');
        window.location.href = 'index.html';
    }
};

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => app.init());

// Export for use
window.app = app;
