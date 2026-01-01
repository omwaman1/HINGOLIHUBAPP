// =====================================================
// Hingoli Hub Marketplace - API Client
// =====================================================

const API_BASE_URL = 'https://hellohingoli.com/api';

const api = {
    // Get auth headers if logged in
    getAuthHeaders() {
        const token = localStorage.getItem('auth_token');
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    },

    // Make API request
    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;

        try {
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...this.getAuthHeaders(),
                    ...options.headers
                },
                ...options
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'API request failed');
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },

    // =====================================================
    // Categories
    // =====================================================
    async getCategories(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/categories${queryString ? `?${queryString}` : ''}`);
    },

    async getCategoryById(id) {
        return this.request(`/categories/${id}`);
    },

    async getSubcategories(categoryId) {
        return this.request(`/categories/${categoryId}/subcategories`);
    },

    // =====================================================
    // Listings
    // =====================================================
    async getListings(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/listings${queryString ? `?${queryString}` : ''}`);
    },

    async getListingById(id) {
        return this.request(`/listings/${id}`);
    },

    async getListingPriceList(id) {
        return this.request(`/listings/${id}/price-list`);
    },

    async getListingReviews(id) {
        return this.request(`/listings/${id}/reviews`);
    },

    // =====================================================
    // Products (Buy & Sell)
    // =====================================================
    async getProducts(params = {}) {
        const queryString = new URLSearchParams(params).toString();
        return this.request(`/products${queryString ? `?${queryString}` : ''}`);
    },

    async getProductById(id) {
        return this.request(`/products/${id}`);
    },

    // =====================================================
    // Banners
    // =====================================================
    async getBanners(placement = 'home_top') {
        return this.request(`/banners?placement=${placement}`);
    },

    // =====================================================
    // Search
    // =====================================================
    async search(query, params = {}) {
        const searchParams = new URLSearchParams({ search: query, ...params }).toString();
        return this.request(`/listings?${searchParams}`);
    },

    // =====================================================
    // Cart (requires auth)
    // =====================================================
    async getCart() {
        return this.request('/cart');
    },

    async addToCart(productId, quantity = 1) {
        return this.request('/cart', {
            method: 'POST',
            body: JSON.stringify({ product_id: productId, quantity })
        });
    },

    async updateCartItem(cartItemId, quantity) {
        return this.request(`/cart/${cartItemId}`, {
            method: 'PUT',
            body: JSON.stringify({ quantity })
        });
    },

    async removeCartItem(cartItemId) {
        return this.request(`/cart/${cartItemId}`, {
            method: 'DELETE'
        });
    },

    async clearCart() {
        return this.request('/cart', {
            method: 'DELETE'
        });
    },

    // =====================================================
    // Addresses (requires auth)
    // =====================================================
    async getAddresses() {
        return this.request('/addresses');
    },

    async addAddress(addressData) {
        return this.request('/addresses', {
            method: 'POST',
            body: JSON.stringify(addressData)
        });
    },

    async deleteAddress(addressId) {
        return this.request(`/addresses/${addressId}`, {
            method: 'DELETE'
        });
    },

    // =====================================================
    // Delivery
    // =====================================================
    async checkDelivery(pincode) {
        return this.request(`/delivery/check?pincode=${pincode}`);
    },

    // =====================================================
    // Orders (requires auth)
    // =====================================================
    async createOrder(addressId, paymentMethod = 'razorpay') {
        return this.request('/orders', {
            method: 'POST',
            body: JSON.stringify({ address_id: addressId, payment_method: paymentMethod })
        });
    },

    async verifyPayment(orderId, razorpayPaymentId, razorpaySignature) {
        return this.request(`/orders/${orderId}/verify-payment`, {
            method: 'POST',
            body: JSON.stringify({
                razorpay_payment_id: razorpayPaymentId,
                razorpay_signature: razorpaySignature
            })
        });
    },

    async getOrders() {
        return this.request('/orders');
    },

    async getOrderById(orderId) {
        return this.request(`/orders/${orderId}`);
    },

    // =====================================================
    // User Profile (requires auth)
    // =====================================================
    async getUserProfile() {
        return this.request('/user/profile');
    },

    async updateUserProfile(profileData) {
        return this.request('/user/profile', {
            method: 'PUT',
            body: JSON.stringify(profileData)
        });
    },

    async getUserListings(type = 'all') {
        return this.request(`/user/listings?type=${type}`);
    },

    // =====================================================
    // Listings Management (requires auth)
    // =====================================================
    async createListing(formData) {
        // Uses FormData for file upload support
        const token = localStorage.getItem('auth_token');
        return fetch(`${API_BASE_URL}/listings`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        }).then(r => r.json());
    },

    async updateListing(listingId, formData) {
        const token = localStorage.getItem('auth_token');
        return fetch(`${API_BASE_URL}/listings/${listingId}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        }).then(r => r.json());
    },

    async deleteListing(listingId) {
        return this.request(`/listings/${listingId}`, {
            method: 'DELETE'
        });
    }
};

// Export for use
window.api = api;

