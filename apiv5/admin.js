/**
 * Hello Hingoli Admin Panel JavaScript
 * External JS for faster caching
 */

// Toggle listing type-specific fields
function toggleTypeFields() {
    const typeSelect = document.getElementById('listing_type');
    if (!typeSelect) return;

    const type = typeSelect.value;
    const serviceFields = document.getElementById('service-fields');
    const jobFields = document.getElementById('job-fields');
    const businessFields = document.getElementById('business-fields');
    const priceField = document.getElementById('price-field-container');

    if (serviceFields) serviceFields.style.display = type === 'services' ? 'block' : 'none';
    if (jobFields) jobFields.style.display = type === 'jobs' ? 'block' : 'none';
    if (businessFields) businessFields.style.display = type === 'business' ? 'block' : 'none';

    // Hide price field for business listings
    if (priceField) priceField.style.display = type === 'business' ? 'none' : 'block';
}

// Filter categories by listing type
function filterCategoriesByType() {
    const typeSelect = document.getElementById('listing_type');
    const categorySelect = document.getElementById('category_id');

    if (!typeSelect || !categorySelect) return;

    const type = typeSelect.value;
    const options = categorySelect.querySelectorAll('option');

    options.forEach(opt => {
        if (opt.value === '') {
            opt.style.display = 'block';
        } else {
            opt.style.display = opt.dataset.type === type ? 'block' : 'none';
        }
    });

    categorySelect.value = '';
    filterSubcategories();
}

// Filter subcategories by selected parent category
function filterSubcategories() {
    const categorySelect = document.getElementById('category_id');
    const subcategorySelect = document.getElementById('subcategory_id');

    if (!categorySelect || !subcategorySelect) return;

    const categoryId = categorySelect.value;
    const options = subcategorySelect.querySelectorAll('option');

    options.forEach(opt => {
        if (opt.value === '') {
            opt.style.display = 'block';
        } else {
            opt.style.display = opt.dataset.parentId === categoryId ? 'block' : 'none';
        }
    });

    subcategorySelect.value = '';
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function () {
    // Initialize type fields toggle
    toggleTypeFields();

    // Initialize category filters
    filterCategoriesByType();

    // Add event listeners
    const typeSelect = document.getElementById('listing_type');
    if (typeSelect) {
        typeSelect.addEventListener('change', function () {
            toggleTypeFields();
            filterCategoriesByType();
        });
    }

    const categorySelect = document.getElementById('category_id');
    if (categorySelect) {
        categorySelect.addEventListener('change', filterSubcategories);
    }
});
