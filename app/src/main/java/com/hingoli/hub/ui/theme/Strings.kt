package com.hingoli.hub.ui.theme

/**
 * Localized strings helper for English/Marathi translations.
 * Use: Strings.getLocalizedText(en, mr, isMarathi)
 */
object Strings {
    
    // Helper function to get localized text
    fun get(en: String, mr: String, isMarathi: Boolean): String = if (isMarathi) mr else en
    
    // ===== COMMON =====
    fun cancel(isMarathi: Boolean) = get("Cancel", "रद्द करा", isMarathi)
    fun save(isMarathi: Boolean) = get("Save", "सेव्ह करा", isMarathi)
    fun add(isMarathi: Boolean) = get("Add", "जोडा", isMarathi)
    fun delete(isMarathi: Boolean) = get("Delete", "हटवा", isMarathi)
    fun retry(isMarathi: Boolean) = get("Retry", "पुन्हा प्रयत्न करा", isMarathi)
    fun error(isMarathi: Boolean) = get("Error", "त्रुटी", isMarathi)
    fun loading(isMarathi: Boolean) = get("Loading...", "लोड होत आहे...", isMarathi)
    fun submit(isMarathi: Boolean) = get("Submit", "सबमिट करा", isMarathi)
    fun ok(isMarathi: Boolean) = get("OK", "ठीक आहे", isMarathi)
    fun yes(isMarathi: Boolean) = get("Yes", "हो", isMarathi)
    fun no(isMarathi: Boolean) = get("No", "नाही", isMarathi)
    fun done(isMarathi: Boolean) = get("Done", "पूर्ण", isMarathi)
    fun next(isMarathi: Boolean) = get("Next", "पुढे", isMarathi)
    fun back(isMarathi: Boolean) = get("Back", "मागे", isMarathi)
    fun search(isMarathi: Boolean) = get("Search", "शोधा", isMarathi)
    fun loadMore(isMarathi: Boolean) = get("Load more", "अधिक लोड करा", isMarathi)
    
    // ===== AUTH / LOGIN =====
    fun enterMobileNumber(isMarathi: Boolean) = get("Enter Mobile Number", "मोबाईल नंबर टाका", isMarathi)
    fun password(isMarathi: Boolean) = get("Password", "पासवर्ड", isMarathi)
    fun enterPassword(isMarathi: Boolean) = get("Enter your password", "तुमचा पासवर्ड टाका", isMarathi)
    fun enterOtp(isMarathi: Boolean) = get("Enter 6-digit OTP", "६ अंकी OTP टाका", isMarathi)
    fun newPassword(isMarathi: Boolean) = get("New Password", "नवीन पासवर्ड", isMarathi)
    fun confirmPassword(isMarathi: Boolean) = get("Confirm Password", "पासवर्ड पुन्हा टाका", isMarathi)
    fun enterNewPassword(isMarathi: Boolean) = get("Enter new password (min 6 characters)", "नवीन पासवर्ड टाका (किमान ६ अक्षरे)", isMarathi)
    fun reEnterPassword(isMarathi: Boolean) = get("Re-enter password", "पासवर्ड पुन्हा टाका", isMarathi)
    fun passwordsDontMatch(isMarathi: Boolean) = get("Passwords don't match", "पासवर्ड जुळत नाहीत", isMarathi)
    fun login(isMarathi: Boolean) = get("Login", "लॉगिन", isMarathi)
    fun register(isMarathi: Boolean) = get("Register", "नोंदणी करा", isMarathi)
    fun forgotPassword(isMarathi: Boolean) = get("Forgot Password?", "पासवर्ड विसरलात?", isMarathi)
    fun sendOtp(isMarathi: Boolean) = get("Send OTP", "OTP पाठवा", isMarathi)
    fun verifyOtp(isMarathi: Boolean) = get("Verify OTP", "OTP सत्यापित करा", isMarathi)
    fun resendOtp(isMarathi: Boolean) = get("Resend OTP", "OTP पुन्हा पाठवा", isMarathi)
    
    // ===== PROFILE =====
    fun editProfile(isMarathi: Boolean) = get("Edit Profile", "प्रोफाइल संपादित करा", isMarathi)
    fun name(isMarathi: Boolean) = get("Name", "नाव", isMarathi)
    fun mobileNumber(isMarathi: Boolean) = get("Mobile Number", "मोबाईल नंबर", isMarathi)
    fun email(isMarathi: Boolean) = get("Email", "ईमेल", isMarathi)
    fun gender(isMarathi: Boolean) = get("Gender", "लिंग", isMarathi)
    fun male(isMarathi: Boolean) = get("Male", "पुरुष", isMarathi)
    fun female(isMarathi: Boolean) = get("Female", "स्त्री", isMarathi)
    fun other(isMarathi: Boolean) = get("Other", "इतर", isMarathi)
    fun dob(isMarathi: Boolean) = get("DOB", "जन्मतारीख", isMarathi)
    fun saveChanges(isMarathi: Boolean) = get("Save Changes", "बदल सेव्ह करा", isMarathi)
    fun saveAndContinue(isMarathi: Boolean) = get("Save & Continue", "सेव्ह करा आणि पुढे जा", isMarathi)
    fun profileUpdatedSuccess(isMarathi: Boolean) = get("Profile updated successfully!", "प्रोफाइल यशस्वीपणे अपडेट झाली!", isMarathi)
    fun tapToChange(isMarathi: Boolean) = get("Tap to change", "बदलण्यासाठी टॅप करा", isMarathi)
    
    // ===== LISTINGS =====
    fun advertiseAndGrow(isMarathi: Boolean) = get("Advertise & Grow", "जाहिरात करा आणि व्यवसाय वाढवा", isMarathi)
    fun deleteListing(isMarathi: Boolean) = get("Delete Listing?", "जाहिरात हटवायची?", isMarathi)
    fun deleteListingConfirm(title: String, isMarathi: Boolean) = 
        get("Are you sure you want to delete \"$title\"? This cannot be undone.", 
            "तुम्हाला खात्री आहे की \"$title\" हटवायची? हे पूर्ववत करता येणार नाही.", isMarathi)
    fun postListing(isMarathi: Boolean) = get("Post Listing", "जाहिरात पोस्ट करा", isMarathi)
    fun editListing(isMarathi: Boolean) = get("Edit Listing", "जाहिरात संपादित करा", isMarathi)
    fun updateListing(isMarathi: Boolean) = get("Update Listing", "जाहिरात अपडेट करा", isMarathi)
    
    // ===== E-COMMERCE =====
    fun myCart(isMarathi: Boolean) = get("My Cart", "माझी कार्ट", isMarathi)
    fun checkout(isMarathi: Boolean) = get("Checkout", "चेकआउट", isMarathi)
    fun myOrders(isMarathi: Boolean) = get("My Orders", "माझे ऑर्डर्स", isMarathi)
    fun orderDetails(isMarathi: Boolean) = get("Order Details", "ऑर्डर तपशील", isMarathi)
    fun placeOrder(isMarathi: Boolean) = get("Place Order", "ऑर्डर द्या", isMarathi)
    fun viewMyOrders(isMarathi: Boolean) = get("View My Orders", "माझे ऑर्डर्स पहा", isMarathi)
    fun continueShopping(isMarathi: Boolean) = get("Continue Shopping", "खरेदी सुरू ठेवा", isMarathi)
    fun addNewAddress(isMarathi: Boolean) = get("Add New Address", "नवीन पत्ता जोडा", isMarathi)
    fun addNew(isMarathi: Boolean) = get("Add New", "नवीन जोडा", isMarathi)
    fun fullName(isMarathi: Boolean) = get("Full Name", "पूर्ण नाव", isMarathi)
    fun phone(isMarathi: Boolean) = get("Phone", "फोन", isMarathi)
    fun addressLine1(isMarathi: Boolean) = get("Address Line 1", "पत्ता ओळ 1", isMarathi)
    fun addressLine2(isMarathi: Boolean) = get("Address Line 2", "पत्ता ओळ 2", isMarathi)
    fun city(isMarathi: Boolean) = get("City", "शहर", isMarathi)
    fun pincode(isMarathi: Boolean) = get("Pincode", "पिनकोड", isMarathi)
    fun saveAddress(isMarathi: Boolean) = get("Save Address", "पत्ता सेव्ह करा", isMarathi)
    fun viewDetails(isMarathi: Boolean) = get("View Details", "तपशील पहा", isMarathi)
    fun addToCart(isMarathi: Boolean) = get("Add to Cart", "कार्टमध्ये जोडा", isMarathi)
    fun buyNow(isMarathi: Boolean) = get("Buy Now", "आता खरेदी करा", isMarathi)
    fun productDetails(isMarathi: Boolean) = get("Product Details", "वस्तू तपशील", isMarathi)
    fun orderPlacedSuccess(isMarathi: Boolean) = get("Order Placed Successfully!", "ऑर्डर यशस्वीपणे दिला!", isMarathi)
    
    // ===== LISTING DETAIL =====
    fun writeReview(isMarathi: Boolean) = get("Write a Review", "रिव्ह्यू लिहा", isMarathi)
    fun addProduct(isMarathi: Boolean) = get("Add Product", "वस्तू जोडा", isMarathi)
    fun addService(isMarathi: Boolean) = get("Add Service", "सेवा जोडा", isMarathi)
    fun addPriceItem(isMarathi: Boolean) = get("Add Price Item", "किंमत आयटम जोडा", isMarathi)
    fun editDescription(isMarathi: Boolean) = get("Edit Description", "वर्णन संपादित करा", isMarathi)
    fun addPhotos(isMarathi: Boolean) = get("Add Photos", "फोटो जोडा", isMarathi)
    fun itemName(isMarathi: Boolean) = get("Item Name", "आयटम नाव", isMarathi)
    fun price(isMarathi: Boolean) = get("Price (₹)", "किंमत (₹)", isMarathi)
    fun description(isMarathi: Boolean) = get("Description", "वर्णन", isMarathi)
    fun category(isMarathi: Boolean) = get("Category", "श्रेणी", isMarathi)
    fun subcategory(isMarathi: Boolean) = get("Subcategory", "उपश्रेणी", isMarathi)
    fun sellOnline(isMarathi: Boolean) = get("Sell Online", "ऑनलाइन विक्री", isMarathi)
    fun new(isMarathi: Boolean) = get("New", "नवीन", isMarathi)
    fun used(isMarathi: Boolean) = get("Used", "वापरलेले", isMarathi)
    fun availableOnline(isMarathi: Boolean) = get("Available for online purchase", "ऑनलाइन खरेदीसाठी उपलब्ध", isMarathi)
    fun showcaseOnly(isMarathi: Boolean) = get("Showcase only", "फक्त प्रदर्शन", isMarathi)
    fun submitReview(isMarathi: Boolean) = get("Submit Review", "रिव्ह्यू सबमिट करा", isMarathi)
    fun titleOptional(isMarathi: Boolean) = get("Title (optional)", "शीर्षक (वैकल्पिक)", isMarathi)
    fun yourReview(isMarathi: Boolean) = get("Your Review", "तुमचा रिव्ह्यू", isMarathi)
    fun addImage(isMarathi: Boolean) = get("Add Image", "फोटो जोडा", isMarathi)
    fun imageSelected(isMarathi: Boolean) = get("Image Selected ✓", "फोटो निवडला ✓", isMarathi)
    fun selectCategory(isMarathi: Boolean) = get("Select Category", "श्रेणी निवडा", isMarathi)
    fun selectSubcategory(isMarathi: Boolean) = get("Select Subcategory", "उपश्रेणी निवडा", isMarathi)
    
    // ===== NOTIFICATIONS =====
    fun notifications(isMarathi: Boolean) = get("Notifications", "सूचना", isMarathi)
    fun markAllRead(isMarathi: Boolean) = get("Mark all read", "सर्व वाचले म्हणून मार्क करा", isMarathi)
    fun noNotifications(isMarathi: Boolean) = get("No notifications yet", "अद्याप सूचना नाहीत", isMarathi)
    
    // ===== CHAT =====
    fun typeMessage(isMarathi: Boolean) = get("Type a message...", "संदेश टाइप करा...", isMarathi)
    fun noMessages(isMarathi: Boolean) = get("No messages yet", "अद्याप संदेश नाहीत", isMarathi)
    
    // ===== JOBS =====
    fun searchJobs(isMarathi: Boolean) = get("Search jobs...", "नोकरी शोधा...", isMarathi)
    fun applyNow(isMarathi: Boolean) = get("Apply Now", "आता अर्ज करा", isMarathi)
    
    // ===== BOTTOM BAR =====  
    fun callNow(isMarathi: Boolean) = get("Call Now", "कॉल करा", isMarathi)
    fun bookNow(isMarathi: Boolean) = get("Book Now", "बुक करा", isMarathi)
    fun chatNow(isMarathi: Boolean) = get("Chat Now", "चॅट करा", isMarathi)
    fun yourListing(isMarathi: Boolean) = get("Your Listing", "तुमची जाहिरात", isMarathi)
}
