-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000
-- Generation Time: Dec 31, 2025 at 06:03 AM
-- Server version: 8.0.11-TiDB-v7.5.6-serverless
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `hellohingoli`
--

-- --------------------------------------------------------

--
-- Table structure for table `app_config`
--

CREATE TABLE `app_config` (
  `id` int(11) NOT NULL,
  `config_key` varchar(50) NOT NULL,
  `config_value` text NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Table structure for table `banners`
--

CREATE TABLE `banners` (
  `banner_id` int(10) UNSIGNED NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `link_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `link_type` enum('listing','category','external','screen') COLLATE utf8mb4_unicode_ci DEFAULT 'external',
  `link_id` bigint(20) UNSIGNED DEFAULT NULL,
  `placement` enum('home_top','home_bottom','services_top','services_bottom','selling_top','selling_bottom','business_top','business_bottom','jobs_top','jobs_bottom','listing_detail_bottom','category_bottom','search_bottom','profile_bottom') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'home_top',
  `start_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` timestamp NULL DEFAULT NULL,
  `sort_order` tinyint(3) UNSIGNED DEFAULT '0',
  `is_active` tinyint(1) DEFAULT '1',
  `view_count` int(10) UNSIGNED DEFAULT '0',
  `click_count` int(10) UNSIGNED DEFAULT '0',
  `target_city` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_category_id` int(10) UNSIGNED DEFAULT NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `target_listing_type` enum('all','services','selling','business','jobs') COLLATE utf8mb4_unicode_ci DEFAULT 'all'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `business_listings`
--

CREATE TABLE `business_listings` (
  `listing_id` bigint(20) UNSIGNED NOT NULL,
  `business_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `registration_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `industry` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `business_type` enum('sole_proprietor','partnership','llp','pvt_ltd','public_ltd','franchise') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `established_year` year(4) DEFAULT NULL,
  `employee_count` enum('1-10','11-50','51-200','201-500','500+') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `annual_revenue_range` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `business_email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `business_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `asking_price` decimal(15,2) DEFAULT NULL,
  `reason_for_sale` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `custom_attributes` json DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `calls`
--

CREATE TABLE `calls` (
  `call_id` bigint(20) UNSIGNED NOT NULL,
  `conversation_id` bigint(20) UNSIGNED NOT NULL,
  `caller_id` bigint(20) UNSIGNED NOT NULL,
  `receiver_id` bigint(20) UNSIGNED NOT NULL,
  `call_type` enum('voice','video') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'voice',
  `call_status` enum('initiated','ringing','ongoing','completed','missed','declined','failed') COLLATE utf8mb4_unicode_ci DEFAULT 'initiated',
  `zego_room_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `zego_app_id` int(10) UNSIGNED DEFAULT NULL,
  `caller_zego_token` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `receiver_zego_token` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `caller_stream_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `receiver_stream_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `started_at` timestamp NULL DEFAULT NULL,
  `answered_at` timestamp NULL DEFAULT NULL,
  `ended_at` timestamp NULL DEFAULT NULL,
  `duration_seconds` int(10) UNSIGNED DEFAULT '0',
  `caller_device_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `receiver_device_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `call_quality` json DEFAULT NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `cart_items`
--

CREATE TABLE `cart_items` (
  `cart_item_id` int(11) NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `listing_id` bigint(20) UNSIGNED DEFAULT NULL,
  `product_id` bigint(20) UNSIGNED DEFAULT NULL,
  `quantity` int(11) DEFAULT '1',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `category_id` int(10) UNSIGNED NOT NULL,
  `parent_id` int(10) UNSIGNED DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name_mr` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `slug` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `listing_type` enum('services','selling','business','jobs') COLLATE utf8mb4_unicode_ci NOT NULL,
  `depth` tinyint(3) UNSIGNED DEFAULT '0',
  `path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `icon_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int(11) DEFAULT '0',
  `is_active` tinyint(1) DEFAULT '1',
  `listing_count` int(10) UNSIGNED DEFAULT '0',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `cities`
--

CREATE TABLE `cities` (
  `city_id` int(10) UNSIGNED NOT NULL,
  `state_id` int(10) UNSIGNED NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name_mr` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `slug` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `is_popular` tinyint(1) DEFAULT '0',
  `listing_count` int(10) UNSIGNED DEFAULT '0',
  `sort_order` int(11) DEFAULT '0',
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `conversations`
--

CREATE TABLE `conversations` (
  `conversation_id` bigint(20) UNSIGNED NOT NULL,
  `listing_id` bigint(20) UNSIGNED DEFAULT NULL,
  `user_one_id` bigint(20) UNSIGNED NOT NULL,
  `user_two_id` bigint(20) UNSIGNED NOT NULL,
  `last_message_id` bigint(20) UNSIGNED DEFAULT NULL,
  `last_message_at` timestamp NULL DEFAULT NULL,
  `is_blocked` tinyint(1) DEFAULT '0',
  `blocked_by` bigint(20) UNSIGNED DEFAULT NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `delivery_users`
--

CREATE TABLE `delivery_users` (
  `delivery_user_id` bigint(20) UNSIGNED NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `vehicle_type` enum('bike','scooter','bicycle','auto') COLLATE utf8mb4_unicode_ci DEFAULT 'bike',
  `vehicle_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('active','inactive','blocked') COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `fcm_token` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_deliveries` int(10) UNSIGNED DEFAULT '0',
  `total_earnings` decimal(12,2) DEFAULT '0.00',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `upi_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `enquiries`
--

CREATE TABLE `enquiries` (
  `enquiry_id` bigint(20) UNSIGNED NOT NULL,
  `listing_id` bigint(20) UNSIGNED DEFAULT NULL,
  `user_id` bigint(20) UNSIGNED DEFAULT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enquiry_type` enum('call','chat','contact_form') COLLATE utf8mb4_unicode_ci DEFAULT 'contact_form',
  `status` enum('new','contacted','resolved','spam') COLLATE utf8mb4_unicode_ci DEFAULT 'new',
  `admin_notes` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `favorites`
--

CREATE TABLE `favorites` (
  `favorite_id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `listing_id` bigint(20) UNSIGNED NOT NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `gateway_devices`
--

CREATE TABLE `gateway_devices` (
  `id` int(11) NOT NULL,
  `device_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `device_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fcm_token` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sim1_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sim2_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `active_sim` tinyint(4) DEFAULT '1' COMMENT '1 or 2',
  `sms_template` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT 'Your Hello Hingoli OTP is: {otp}. Valid for 5 min. Do not share.',
  `status` enum('online','offline') COLLATE utf8mb4_unicode_ci DEFAULT 'offline',
  `last_active_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `job_listings`
--

CREATE TABLE `job_listings` (
  `listing_id` bigint(20) UNSIGNED NOT NULL,
  `job_title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `employment_type` enum('full_time','part_time','contract','internship','freelance') COLLATE utf8mb4_unicode_ci NOT NULL,
  `salary_min` decimal(12,2) DEFAULT NULL,
  `salary_max` decimal(12,2) DEFAULT NULL,
  `salary_period` enum('hourly','daily','weekly','monthly','yearly') COLLATE utf8mb4_unicode_ci DEFAULT 'monthly',
  `experience_required_years` tinyint(3) UNSIGNED DEFAULT '0',
  `education_required` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remote_option` enum('on_site','remote','hybrid') COLLATE utf8mb4_unicode_ci DEFAULT 'on_site',
  `vacancies` int(10) UNSIGNED DEFAULT '1',
  `application_deadline` date DEFAULT NULL,
  `required_skills` json DEFAULT NULL,
  `benefits` json DEFAULT NULL,
  `custom_attributes` json DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `listings`
--

CREATE TABLE `listings` (
  `listing_id` bigint(20) UNSIGNED NOT NULL,
  `listing_type` enum('services','selling','business','jobs') COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category_id` int(10) UNSIGNED NOT NULL,
  `subcategory_id` int(10) UNSIGNED DEFAULT NULL,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `state` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `country` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'India',
  `postal_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `main_image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `status` enum('draft','pending','active','sold','expired','deleted') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `is_verified` tinyint(1) DEFAULT '0',
  `is_featured` tinyint(1) DEFAULT '0',
  `verified_at` timestamp NULL DEFAULT NULL,
  `verified_by` bigint(20) UNSIGNED DEFAULT NULL,
  `rejection_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `view_count` int(10) UNSIGNED DEFAULT '0',
  `review_count` int(10) UNSIGNED DEFAULT '0',
  `avg_rating` decimal(2,1) DEFAULT '0.0',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `expires_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `listing_images`
--

CREATE TABLE `listing_images` (
  `image_id` bigint(20) UNSIGNED NOT NULL,
  `listing_id` bigint(20) UNSIGNED NOT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `thumbnail_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` tinyint(3) UNSIGNED DEFAULT '0',
  `image_type` enum('gallery','document','certificate') COLLATE utf8mb4_unicode_ci DEFAULT 'gallery',
  `alt_text` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_size_kb` int(10) UNSIGNED DEFAULT NULL,
  `width` int(10) UNSIGNED DEFAULT NULL,
  `height` int(10) UNSIGNED DEFAULT NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `listing_price_list`
--

CREATE TABLE `listing_price_list` (
  `item_id` bigint(20) UNSIGNED NOT NULL,
  `listing_id` bigint(20) UNSIGNED NOT NULL,
  `item_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_description` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `item_category` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `discounted_price` decimal(10,2) DEFAULT NULL,
  `currency` varchar(3) COLLATE utf8mb4_unicode_ci DEFAULT 'INR',
  `duration_minutes` int(10) UNSIGNED DEFAULT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` tinyint(3) UNSIGNED DEFAULT '0',
  `is_popular` tinyint(1) DEFAULT '0',
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `messages`
--

CREATE TABLE `messages` (
  `message_id` bigint(20) UNSIGNED NOT NULL,
  `conversation_id` bigint(20) UNSIGNED NOT NULL,
  `sender_id` bigint(20) UNSIGNED NOT NULL,
  `message_type` enum('text','image','video','audio','file','location','offer') COLLATE utf8mb4_unicode_ci DEFAULT 'text',
  `content` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `media_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `offer_amount` decimal(15,2) DEFAULT NULL,
  `offer_status` enum('pending','accepted','rejected','expired') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `read_at` timestamp NULL DEFAULT NULL,
  `is_deleted_by_sender` tinyint(1) DEFAULT '0',
  `is_deleted_by_receiver` tinyint(1) DEFAULT '0',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `type` enum('listing_approved','admin_broadcast') COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `body` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `deep_link` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `listing_id` bigint(20) UNSIGNED DEFAULT NULL,
  `sent_count` int(10) UNSIGNED DEFAULT '0',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notification_logs`
--

CREATE TABLE `notification_logs` (
  `log_id` bigint(20) UNSIGNED NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `body` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_type` enum('all','user','listing_type','city') COLLATE utf8mb4_unicode_ci DEFAULT 'all',
  `target_value` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sent_count` int(10) UNSIGNED DEFAULT '0',
  `sent_by` bigint(20) UNSIGNED DEFAULT NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `notification_settings`
--

CREATE TABLE `notification_settings` (
  `id` int(11) NOT NULL,
  `setting_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `setting_value` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `old_categories`
--

CREATE TABLE `old_categories` (
  `id` int(10) UNSIGNED NOT NULL,
  `parent_id` int(10) UNSIGNED DEFAULT NULL,
  `level` tinyint(3) UNSIGNED DEFAULT '1' COMMENT '1=Main, 2=Sub, 3=Type',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name_mr` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Marathi name',
  `slug` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Material icon name or emoji',
  `color` varchar(7) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Hex color code',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int(11) DEFAULT '0',
  `is_active` tinyint(1) DEFAULT '1',
  `product_count` int(10) UNSIGNED DEFAULT '0',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `old_products`
--

CREATE TABLE `old_products` (
  `product_id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT 'User who is selling the item',
  `listing_id` bigint(20) UNSIGNED DEFAULT NULL,
  `product_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category_id` int(10) UNSIGNED DEFAULT NULL,
  `old_category_id` int(10) UNSIGNED DEFAULT NULL,
  `subcategory_id` int(10) UNSIGNED DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `original_price` decimal(10,2) DEFAULT NULL COMMENT 'Original purchase price for reference',
  `currency` varchar(3) COLLATE utf8mb4_unicode_ci DEFAULT 'INR',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `additional_images` json DEFAULT NULL COMMENT 'Array of additional image URLs',
  `condition` enum('like_new','good','fair','poor') COLLATE utf8mb4_unicode_ci DEFAULT 'good',
  `age_months` int(10) UNSIGNED DEFAULT NULL COMMENT 'How old is the item (in months)',
  `has_warranty` tinyint(1) DEFAULT '0',
  `warranty_months` int(10) UNSIGNED DEFAULT NULL COMMENT 'Remaining warranty months',
  `has_bill` tinyint(1) DEFAULT '0' COMMENT 'Has original purchase bill',
  `reason_for_selling` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `brand` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `model` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'Hingoli',
  `pincode` varchar(6) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `show_phone` tinyint(1) DEFAULT '1' COMMENT 'Show phone number to buyers',
  `accept_offers` tinyint(1) DEFAULT '1' COMMENT 'Open to price negotiation',
  `status` enum('active','sold','expired','deleted') COLLATE utf8mb4_unicode_ci DEFAULT 'active',
  `is_featured` tinyint(1) DEFAULT '0',
  `view_count` int(10) UNSIGNED DEFAULT '0',
  `inquiry_count` int(10) UNSIGNED DEFAULT '0',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `sold_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` bigint(20) NOT NULL,
  `order_number` varchar(20) NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `address_id` int(11) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `shipping_fee` decimal(10,2) DEFAULT '0',
  `total_amount` decimal(10,2) NOT NULL,
  `payment_method` enum('razorpay','cod') DEFAULT 'razorpay',
  `payment_status` enum('pending','paid','failed','refunded') DEFAULT 'pending',
  `razorpay_order_id` varchar(100) DEFAULT NULL,
  `razorpay_payment_id` varchar(100) DEFAULT NULL,
  `order_status` enum('pending','confirmed','processing','accepted','out_for_delivery','shipped','delivered','cancelled') DEFAULT 'pending',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `webhook_verified` tinyint(1) DEFAULT '0' COMMENT 'Whether payment was verified via webhook',
  `webhook_verified_at` datetime DEFAULT NULL COMMENT 'When webhook verified the payment',
  `refund_id` varchar(50) DEFAULT NULL COMMENT 'Razorpay refund ID',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT 'Amount refunded',
  `payment_error` text DEFAULT NULL COMMENT 'Payment error message if failed',
  `payment_method_detail` varchar(50) DEFAULT NULL COMMENT 'Detailed payment method (upi, card, netbanking)',
  `dispute_id` varchar(50) DEFAULT NULL COMMENT 'Razorpay dispute ID',
  `dispute_status` varchar(20) DEFAULT NULL COMMENT 'Dispute status: open, won, lost',
  `dispute_reason` varchar(100) DEFAULT NULL COMMENT 'Reason for dispute',
  `dispute_amount` decimal(10,2) DEFAULT NULL COMMENT 'Disputed amount',
  `estimated_delivery_date` date DEFAULT NULL,
  `delivery_time_slot` varchar(50) DEFAULT NULL,
  `delivery_user_id` bigint(20) UNSIGNED DEFAULT NULL,
  `delivery_earnings` decimal(10,2) DEFAULT NULL COMMENT '10% of total_amount',
  `delivery_accepted_at` timestamp NULL DEFAULT NULL,
  `delivery_picked_at` timestamp NULL DEFAULT NULL,
  `delivered_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Table structure for table `order_items`
--

CREATE TABLE `order_items` (
  `order_item_id` bigint(20) NOT NULL,
  `order_id` bigint(20) NOT NULL,
  `listing_id` bigint(20) NOT NULL,
  `seller_id` bigint(20) UNSIGNED NOT NULL,
  `quantity` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `item_status` enum('pending','confirmed','shipped','delivered','cancelled') DEFAULT 'pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Table structure for table `otp_send_logs`
--

CREATE TABLE `otp_send_logs` (
  `id` int(11) NOT NULL,
  `device_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Unique device identifier',
  `device_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'User-friendly device name',
  `sender_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SIM phone number that sent the SMS',
  `recipient_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Phone receiving the OTP',
  `otp_code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'The OTP that was sent',
  `status` enum('sent','failed','pending') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `error_message` text COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Error details if failed',
  `request_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Firebase request ID',
  `sent_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `otp_verifications`
--

CREATE TABLE `otp_verifications` (
  `id` int(11) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `otp_code` varchar(255) NOT NULL,
  `purpose` enum('signup','reset_password','login') DEFAULT 'signup',
  `attempts` int(11) DEFAULT '0',
  `expires_at` datetime NOT NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Table structure for table `reviews`
--

CREATE TABLE `reviews` (
  `review_id` bigint(20) UNSIGNED NOT NULL,
  `listing_id` bigint(20) UNSIGNED DEFAULT NULL,
  `product_id` bigint(20) UNSIGNED DEFAULT NULL,
  `old_product_id` bigint(20) UNSIGNED DEFAULT NULL,
  `reviewer_id` bigint(20) UNSIGNED NOT NULL,
  `rating` tinyint(3) UNSIGNED NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `images` json DEFAULT NULL,
  `is_approved` tinyint(1) DEFAULT '0',
  `approval_status` enum('pending','approved','rejected') COLLATE utf8mb4_unicode_ci DEFAULT 'pending',
  `moderated_by` bigint(20) UNSIGNED DEFAULT NULL,
  `moderated_at` timestamp NULL DEFAULT NULL,
  `rejection_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seller_response` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seller_response_at` timestamp NULL DEFAULT NULL,
  `helpful_count` int(10) UNSIGNED DEFAULT '0',
  `is_verified_purchase` tinyint(1) DEFAULT '0',
  `is_featured` tinyint(1) DEFAULT '0',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `services_listings`
--

CREATE TABLE `services_listings` (
  `listing_id` bigint(20) UNSIGNED NOT NULL,
  `service_type` enum('one_time','recurring','contract','hourly') COLLATE utf8mb4_unicode_ci DEFAULT 'one_time',
  `experience_years` tinyint(3) UNSIGNED DEFAULT NULL,
  `availability` enum('immediate','scheduled','weekends_only','flexible') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `service_area_radius_km` int(10) UNSIGNED DEFAULT NULL,
  `hourly_rate` decimal(10,2) DEFAULT NULL,
  `certifications` json DEFAULT NULL,
  `custom_attributes` json DEFAULT NULL,
  `price_min` decimal(10,2) DEFAULT NULL,
  `price_max` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `service_pincodes`
--

CREATE TABLE `service_pincodes` (
  `pincode` varchar(6) NOT NULL,
  `city_name` varchar(100) DEFAULT NULL,
  `state` varchar(50) DEFAULT 'Maharashtra',
  `delivery_days` int(11) DEFAULT '1',
  `delivery_time` varchar(20) DEFAULT '5 PM',
  `is_serviceable` tinyint(1) DEFAULT '1',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Table structure for table `settings`
--

CREATE TABLE `settings` (
  `id` int(11) NOT NULL,
  `setting_key` varchar(100) NOT NULL,
  `setting_value` text DEFAULT NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Table structure for table `shop_categories`
--

CREATE TABLE `shop_categories` (
  `id` int(10) UNSIGNED NOT NULL,
  `parent_id` int(10) UNSIGNED DEFAULT NULL,
  `level` tinyint(3) UNSIGNED DEFAULT '1' COMMENT '1=Main, 2=Sub, 3=Type',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name_mr` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Marathi name',
  `slug` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `icon` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Material icon name or emoji',
  `color` varchar(7) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Hex color code',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sort_order` int(11) DEFAULT '0',
  `is_active` tinyint(1) DEFAULT '1',
  `product_count` int(10) UNSIGNED DEFAULT '0',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `shop_products`
--

CREATE TABLE `shop_products` (
  `product_id` bigint(20) UNSIGNED NOT NULL,
  `listing_id` bigint(20) UNSIGNED NOT NULL,
  `product_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category_id` int(10) UNSIGNED DEFAULT NULL,
  `subcategory_id` int(10) UNSIGNED DEFAULT NULL,
  `shop_category_id` int(10) UNSIGNED DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `discounted_price` decimal(10,2) DEFAULT NULL,
  `currency` varchar(3) COLLATE utf8mb4_unicode_ci DEFAULT 'INR',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sell_online` tinyint(1) DEFAULT '0',
  `condition` enum('new','old') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'new',
  `stock_qty` int(10) UNSIGNED DEFAULT NULL,
  `min_qty` int(10) UNSIGNED DEFAULT '1',
  `sort_order` tinyint(3) UNSIGNED DEFAULT '0',
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `states`
--

CREATE TABLE `states` (
  `state_id` int(10) UNSIGNED NOT NULL,
  `country_code` varchar(2) COLLATE utf8mb4_unicode_ci DEFAULT 'IN',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `slug` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_active` tinyint(1) DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_verified` tinyint(1) DEFAULT '0',
  `is_active` tinyint(1) DEFAULT '1',
  `listing_count` int(10) UNSIGNED DEFAULT '0',
  `avg_rating` decimal(2,1) DEFAULT '0.0',
  `review_count` int(10) UNSIGNED DEFAULT '0',
  `response_rate` decimal(5,2) DEFAULT '0.0',
  `last_active_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `gender` enum('male','female','other') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `reset_token` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reset_token_expires` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user_addresses`
--

CREATE TABLE `user_addresses` (
  `address_id` int(11) NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `name` varchar(100) NOT NULL,
  `phone` varchar(15) NOT NULL,
  `address_line1` varchar(255) NOT NULL,
  `address_line2` varchar(255) DEFAULT NULL,
  `city` varchar(100) NOT NULL,
  `state` varchar(100) DEFAULT 'Maharashtra',
  `pincode` varchar(10) NOT NULL,
  `is_default` tinyint(1) DEFAULT '0',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- --------------------------------------------------------

--
-- Table structure for table `user_fcm_tokens`
--

CREATE TABLE `user_fcm_tokens` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `fcm_token` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `device_info` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `user_notifications`
--

CREATE TABLE `user_notifications` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `notification_id` bigint(20) UNSIGNED DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `body` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('listing_approved','admin_broadcast','chat','call') COLLATE utf8mb4_unicode_ci DEFAULT 'admin_broadcast',
  `deep_link` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `listing_id` bigint(20) UNSIGNED DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `app_config`
--
ALTER TABLE `app_config`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `config_key` (`config_key`);

--
-- Indexes for table `banners`
--
ALTER TABLE `banners`
  ADD PRIMARY KEY (`banner_id`),
  ADD KEY `idx_placement_active` (`placement`,`is_active`,`start_date`,`end_date`),
  ADD KEY `idx_city` (`target_city`),
  ADD KEY `idx_dates` (`start_date`,`end_date`);

--
-- Indexes for table `business_listings`
--
ALTER TABLE `business_listings`
  ADD PRIMARY KEY (`listing_id`),
  ADD KEY `idx_industry` (`industry`),
  ADD KEY `idx_business_type` (`business_type`),
  ADD KEY `idx_established` (`established_year`);

--
-- Indexes for table `calls`
--
ALTER TABLE `calls`
  ADD PRIMARY KEY (`call_id`),
  ADD KEY `idx_caller` (`caller_id`,`created_at`),
  ADD KEY `idx_receiver` (`receiver_id`,`created_at`),
  ADD KEY `idx_conversation` (`conversation_id`),
  ADD KEY `idx_status` (`call_status`,`created_at`),
  ADD KEY `idx_zego_room` (`zego_room_id`);

--
-- Indexes for table `cart_items`
--
ALTER TABLE `cart_items`
  ADD PRIMARY KEY (`cart_item_id`),
  ADD UNIQUE KEY `unique_cart_item` (`user_id`,`listing_id`),
  ADD KEY `idx_cart_items_user` (`user_id`),
  ADD KEY (`user_id`),
  ADD KEY `fk_cart_product` (`product_id`),
  ADD KEY `idx_cart_product` (`user_id`,`product_id`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`category_id`),
  ADD UNIQUE KEY `uk_slug` (`slug`),
  ADD KEY `idx_parent` (`parent_id`),
  ADD KEY `idx_listing_type` (`listing_type`),
  ADD KEY `idx_depth` (`depth`),
  ADD KEY `idx_path` (`path`);

--
-- Indexes for table `cities`
--
ALTER TABLE `cities`
  ADD PRIMARY KEY (`city_id`),
  ADD UNIQUE KEY `uk_slug` (`slug`),
  ADD KEY `idx_state` (`state_id`),
  ADD KEY `idx_popular` (`is_popular`,`listing_count`),
  ADD KEY `idx_name` (`name`);

--
-- Indexes for table `conversations`
--
ALTER TABLE `conversations`
  ADD PRIMARY KEY (`conversation_id`),
  ADD UNIQUE KEY `uk_users_listing` (`user_one_id`,`user_two_id`,`listing_id`),
  ADD KEY `idx_user_one` (`user_one_id`,`last_message_at`),
  ADD KEY `idx_user_two` (`user_two_id`,`last_message_at`),
  ADD KEY `idx_listing` (`listing_id`);

--
-- Indexes for table `delivery_users`
--
ALTER TABLE `delivery_users`
  ADD PRIMARY KEY (`delivery_user_id`),
  ADD UNIQUE KEY `phone` (`phone`);

--
-- Indexes for table `enquiries`
--
ALTER TABLE `enquiries`
  ADD PRIMARY KEY (`enquiry_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_listing` (`listing_id`),
  ADD KEY `idx_created` (`created_at`),
  ADD KEY `idx_enquiries_user` (`user_id`,`created_at`);

--
-- Indexes for table `favorites`
--
ALTER TABLE `favorites`
  ADD PRIMARY KEY (`favorite_id`),
  ADD UNIQUE KEY `uk_user_listing` (`user_id`,`listing_id`),
  ADD KEY `idx_user` (`user_id`,`created_at`),
  ADD KEY `idx_listing` (`listing_id`);

--
-- Indexes for table `gateway_devices`
--
ALTER TABLE `gateway_devices`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `device_id` (`device_id`);

--
-- Indexes for table `job_listings`
--
ALTER TABLE `job_listings`
  ADD PRIMARY KEY (`listing_id`),
  ADD KEY `idx_employment_type` (`employment_type`),
  ADD KEY `idx_salary_range` (`salary_min`,`salary_max`),
  ADD KEY `idx_remote` (`remote_option`),
  ADD KEY `idx_deadline` (`application_deadline`);

--
-- Indexes for table `listings`
--
ALTER TABLE `listings`
  ADD PRIMARY KEY (`listing_id`),
  ADD KEY `idx_listing_type` (`listing_type`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_user` (`user_id`),
  ADD KEY `idx_category` (`category_id`,`subcategory_id`),
  ADD KEY `idx_created` (`created_at`),
  ADD KEY `idx_city` (`city`),
  ADD KEY `idx_listings_type_status` (`listing_type`,`status`),
  ADD KEY `idx_listings_category_status` (`category_id`,`status`),
  ADD KEY `idx_listings_subcategory_status` (`subcategory_id`,`status`),
  ADD KEY `idx_listings_city_status` (`city`,`status`),
  ADD KEY `idx_listings_combined` (`listing_type`,`category_id`,`city`,`status`,`created_at`),
  ADD KEY `idx_listings_featured_created` (`is_featured`,`created_at`),
  ADD KEY `idx_listings_title` (`title`(100)),
  ADD KEY `idx_listings_user_status` (`user_id`,`status`,`created_at`),
  ADD KEY (`listing_type`,`status`);

--
-- Indexes for table `listing_images`
--
ALTER TABLE `listing_images`
  ADD PRIMARY KEY (`image_id`),
  ADD KEY `idx_listing_order` (`listing_id`,`sort_order`);

--
-- Indexes for table `listing_price_list`
--
ALTER TABLE `listing_price_list`
  ADD PRIMARY KEY (`item_id`),
  ADD KEY `idx_listing_category` (`listing_id`,`item_category`,`sort_order`),
  ADD KEY `idx_listing_order` (`listing_id`,`sort_order`);

--
-- Indexes for table `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`message_id`),
  ADD KEY `idx_conversation_created` (`conversation_id`,`created_at`),
  ADD KEY `idx_sender` (`sender_id`),
  ADD KEY `idx_unread` (`conversation_id`,`is_read`,`created_at`),
  ADD KEY `idx_receiver_unread` (`sender_id`,`is_read`,`created_at`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_type` (`type`),
  ADD KEY `idx_created` (`created_at`);

--
-- Indexes for table `notification_logs`
--
ALTER TABLE `notification_logs`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `idx_created` (`created_at`);

--
-- Indexes for table `notification_settings`
--
ALTER TABLE `notification_settings`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `setting_key` (`setting_key`);

--
-- Indexes for table `old_categories`
--
ALTER TABLE `old_categories`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_slug` (`slug`),
  ADD KEY `idx_parent` (`parent_id`),
  ADD KEY `idx_level` (`level`),
  ADD KEY `idx_active` (`is_active`),
  ADD KEY `idx_sort` (`sort_order`);

--
-- Indexes for table `old_products`
--
ALTER TABLE `old_products`
  ADD PRIMARY KEY (`product_id`),
  ADD KEY `idx_user` (`user_id`),
  ADD KEY `idx_category` (`category_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_city` (`city`),
  ADD KEY `idx_price` (`price`),
  ADD KEY `idx_created` (`created_at`),
  ADD KEY `idx_condition` (`condition`),
  ADD KEY `fk_3` (`old_category_id`),
  ADD KEY `idx_old_products_filter` (`status`,`category_id`,`created_at`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD UNIQUE KEY `order_number` (`order_number`),
  ADD KEY `idx_orders_user` (`user_id`,`created_at`),
  ADD KEY (`user_id`,`created_at`),
  ADD KEY `idx_orders_razorpay_order_id` (`razorpay_order_id`),
  ADD KEY `idx_orders_razorpay_payment_id` (`razorpay_payment_id`),
  ADD KEY `idx_order_status` (`order_status`,`created_at`),
  ADD KEY `idx_orders_delivery` (`delivery_user_id`,`order_status`),
  ADD KEY `idx_orders_available` (`order_status`,`payment_status`,`delivery_user_id`);

--
-- Indexes for table `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`order_item_id`),
  ADD KEY `idx_order_items_seller` (`seller_id`,`item_status`),
  ADD KEY (`seller_id`,`item_status`),
  ADD KEY `fk_order_item` (`order_id`),
  ADD KEY `idx_seller_order` (`seller_id`,`order_id`);

--
-- Indexes for table `otp_send_logs`
--
ALTER TABLE `otp_send_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_device` (`device_id`),
  ADD KEY `idx_recipient` (`recipient_phone`),
  ADD KEY `idx_sent_at` (`sent_at`),
  ADD KEY `idx_otp_logs_recipient` (`recipient_phone`,`sent_at`);

--
-- Indexes for table `otp_verifications`
--
ALTER TABLE `otp_verifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_phone` (`phone`),
  ADD KEY `idx_expires` (`expires_at`);

--
-- Indexes for table `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`review_id`),
  ADD UNIQUE KEY `uk_listing_review` (`reviewer_id`,`listing_id`),
  ADD KEY `idx_listing` (`listing_id`,`is_approved`,`created_at`),
  ADD KEY `idx_approval` (`approval_status`,`created_at`),
  ADD KEY `idx_rating` (`rating`),
  ADD KEY `fk_review_moderator` (`moderated_by`),
  ADD KEY `idx_reviews_listing` (`listing_id`,`created_at`),
  ADD KEY `idx_product_reviews` (`product_id`,`is_approved`,`created_at`),
  ADD KEY `idx_reviews_old_product` (`old_product_id`);

--
-- Indexes for table `services_listings`
--
ALTER TABLE `services_listings`
  ADD PRIMARY KEY (`listing_id`),
  ADD KEY `idx_service_type` (`service_type`),
  ADD KEY `idx_experience` (`experience_years`);

--
-- Indexes for table `service_pincodes`
--
ALTER TABLE `service_pincodes`
  ADD PRIMARY KEY (`pincode`),
  ADD KEY `idx_service_pincodes_serviceable` (`is_serviceable`,`delivery_days`),
  ADD KEY (`is_serviceable`,`delivery_days`);

--
-- Indexes for table `settings`
--
ALTER TABLE `settings`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `setting_key` (`setting_key`);

--
-- Indexes for table `shop_categories`
--
ALTER TABLE `shop_categories`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_slug` (`slug`),
  ADD KEY `idx_parent` (`parent_id`),
  ADD KEY `idx_level` (`level`),
  ADD KEY `idx_active` (`is_active`),
  ADD KEY `idx_sort` (`sort_order`);

--
-- Indexes for table `shop_products`
--
ALTER TABLE `shop_products`
  ADD PRIMARY KEY (`product_id`),
  ADD KEY `idx_listing` (`listing_id`),
  ADD KEY `idx_category` (`category_id`),
  ADD KEY `idx_sell_online` (`sell_online`,`is_active`),
  ADD KEY `idx_created` (`created_at`),
  ADD KEY `idx_selling_feed` (`sell_online`,`condition`,`is_active`,`created_at`),
  ADD KEY `idx_product_search` (`product_name`(50),`is_active`),
  ADD KEY `idx_shop_category` (`shop_category_id`),
  ADD KEY `fk_2` (`shop_category_id`),
  ADD KEY `idx_shop_products_listing_active` (`listing_id`,`is_active`);

--
-- Indexes for table `states`
--
ALTER TABLE `states`
  ADD PRIMARY KEY (`state_id`),
  ADD UNIQUE KEY `uk_slug` (`slug`),
  ADD KEY `idx_country` (`country_code`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `uk_email` (`email`),
  ADD UNIQUE KEY `uk_username` (`username`),
  ADD KEY `idx_phone` (`phone`),
  ADD KEY `idx_created` (`created_at`),
  ADD KEY `idx_phone_active` (`phone`,`is_active`);

--
-- Indexes for table `user_addresses`
--
ALTER TABLE `user_addresses`
  ADD PRIMARY KEY (`address_id`),
  ADD KEY `idx_addresses_user` (`user_id`),
  ADD KEY (`user_id`);

--
-- Indexes for table `user_fcm_tokens`
--
ALTER TABLE `user_fcm_tokens`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user` (`user_id`),
  ADD UNIQUE KEY `uk_token` (`fcm_token`);

--
-- Indexes for table `user_notifications`
--
ALTER TABLE `user_notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_unread` (`user_id`,`is_read`,`created_at`),
  ADD KEY `idx_user_created` (`user_id`,`created_at`),
  ADD KEY `idx_user_read_created` (`user_id`,`is_read`,`created_at`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `app_config`
--
ALTER TABLE `app_config`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `banners`
--
ALTER TABLE `banners`
  MODIFY `banner_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `calls`
--
ALTER TABLE `calls`
  MODIFY `call_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `cart_items`
--
ALTER TABLE `cart_items`
  MODIFY `cart_item_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `category_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `cities`
--
ALTER TABLE `cities`
  MODIFY `city_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `conversations`
--
ALTER TABLE `conversations`
  MODIFY `conversation_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `delivery_users`
--
ALTER TABLE `delivery_users`
  MODIFY `delivery_user_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `enquiries`
--
ALTER TABLE `enquiries`
  MODIFY `enquiry_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `favorites`
--
ALTER TABLE `favorites`
  MODIFY `favorite_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `gateway_devices`
--
ALTER TABLE `gateway_devices`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `listings`
--
ALTER TABLE `listings`
  MODIFY `listing_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `listing_images`
--
ALTER TABLE `listing_images`
  MODIFY `image_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `listing_price_list`
--
ALTER TABLE `listing_price_list`
  MODIFY `item_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `messages`
--
ALTER TABLE `messages`
  MODIFY `message_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notification_logs`
--
ALTER TABLE `notification_logs`
  MODIFY `log_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `notification_settings`
--
ALTER TABLE `notification_settings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `old_categories`
--
ALTER TABLE `old_categories`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `old_products`
--
ALTER TABLE `old_products`
  MODIFY `product_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `order_items`
--
ALTER TABLE `order_items`
  MODIFY `order_item_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `otp_send_logs`
--
ALTER TABLE `otp_send_logs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `otp_verifications`
--
ALTER TABLE `otp_verifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `reviews`
--
ALTER TABLE `reviews`
  MODIFY `review_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `settings`
--
ALTER TABLE `settings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `shop_categories`
--
ALTER TABLE `shop_categories`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `shop_products`
--
ALTER TABLE `shop_products`
  MODIFY `product_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `states`
--
ALTER TABLE `states`
  MODIFY `state_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user_addresses`
--
ALTER TABLE `user_addresses`
  MODIFY `address_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user_fcm_tokens`
--
ALTER TABLE `user_fcm_tokens`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `user_notifications`
--
ALTER TABLE `user_notifications`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `business_listings`
--
ALTER TABLE `business_listings`
  ADD CONSTRAINT `fk_business_listing` FOREIGN KEY (`listing_id`) REFERENCES `hellohingoli`.`listings` (`listing_id`) ON DELETE CASCADE;

--
-- Constraints for table `calls`
--
ALTER TABLE `calls`
  ADD CONSTRAINT `fk_call_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `hellohingoli`.`conversations` (`conversation_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_call_caller` FOREIGN KEY (`caller_id`) REFERENCES `hellohingoli`.`users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_call_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `hellohingoli`.`users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `cart_items`
--
ALTER TABLE `cart_items`
  ADD CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `hellohingoli`.`shop_products` (`product_id`) ON DELETE CASCADE;

--
-- Constraints for table `categories`
--
ALTER TABLE `categories`
  ADD CONSTRAINT `fk_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `hellohingoli`.`categories` (`category_id`) ON DELETE SET NULL;

--
-- Constraints for table `cities`
--
ALTER TABLE `cities`
  ADD CONSTRAINT `fk_city_state` FOREIGN KEY (`state_id`) REFERENCES `hellohingoli`.`states` (`state_id`) ON DELETE CASCADE;

--
-- Constraints for table `conversations`
--
ALTER TABLE `conversations`
  ADD CONSTRAINT `fk_conv_user_one` FOREIGN KEY (`user_one_id`) REFERENCES `hellohingoli`.`users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_conv_user_two` FOREIGN KEY (`user_two_id`) REFERENCES `hellohingoli`.`users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_conv_listing` FOREIGN KEY (`listing_id`) REFERENCES `hellohingoli`.`listings` (`listing_id`) ON DELETE SET NULL;

--
-- Constraints for table `favorites`
--
ALTER TABLE `favorites`
  ADD CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `hellohingoli`.`users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_fav_listing` FOREIGN KEY (`listing_id`) REFERENCES `hellohingoli`.`listings` (`listing_id`) ON DELETE CASCADE;

--
-- Constraints for table `job_listings`
--
ALTER TABLE `job_listings`
  ADD CONSTRAINT `fk_job_listing` FOREIGN KEY (`listing_id`) REFERENCES `hellohingoli`.`listings` (`listing_id`) ON DELETE CASCADE;

--
-- Constraints for table `listing_images`
--
ALTER TABLE `listing_images`
  ADD CONSTRAINT `fk_image_listing` FOREIGN KEY (`listing_id`) REFERENCES `hellohingoli`.`listings` (`listing_id`) ON DELETE CASCADE;

--
-- Constraints for table `listing_price_list`
--
ALTER TABLE `listing_price_list`
  ADD CONSTRAINT `fk_price_listing` FOREIGN KEY (`listing_id`) REFERENCES `hellohingoli`.`listings` (`listing_id`) ON DELETE CASCADE;

--
-- Constraints for table `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `fk_msg_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `hellohingoli`.`conversations` (`conversation_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `hellohingoli`.`users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `old_categories`
--
ALTER TABLE `old_categories`
  ADD CONSTRAINT `fk_1` FOREIGN KEY (`parent_id`) REFERENCES `hellohingoli`.`old_categories` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `old_products`
--
ALTER TABLE `old_products`
  ADD CONSTRAINT `fk_1` FOREIGN KEY (`user_id`) REFERENCES `hellohingoli`.`users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_2` FOREIGN KEY (`category_id`) REFERENCES `hellohingoli`.`categories` (`category_id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_3` FOREIGN KEY (`old_category_id`) REFERENCES `hellohingoli`.`old_categories` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `fk_order_item` FOREIGN KEY (`order_id`) REFERENCES `hellohingoli`.`orders` (`order_id`) ON DELETE CASCADE;

--
-- Constraints for table `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `fk_review_listing` FOREIGN KEY (`listing_id`) REFERENCES `hellohingoli`.`listings` (`listing_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `hellohingoli`.`users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_review_moderator` FOREIGN KEY (`moderated_by`) REFERENCES `hellohingoli`.`users` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `services_listings`
--
ALTER TABLE `services_listings`
  ADD CONSTRAINT `fk_services_listing` FOREIGN KEY (`listing_id`) REFERENCES `hellohingoli`.`listings` (`listing_id`) ON DELETE CASCADE;

--
-- Constraints for table `shop_categories`
--
ALTER TABLE `shop_categories`
  ADD CONSTRAINT `fk_1` FOREIGN KEY (`parent_id`) REFERENCES `hellohingoli`.`shop_categories` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `shop_products`
--
ALTER TABLE `shop_products`
  ADD CONSTRAINT `fk_1` FOREIGN KEY (`listing_id`) REFERENCES `hellohingoli`.`listings` (`listing_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_2` FOREIGN KEY (`shop_category_id`) REFERENCES `hellohingoli`.`shop_categories` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `user_notifications`
--
ALTER TABLE `user_notifications`
  ADD CONSTRAINT `fk_1` FOREIGN KEY (`user_id`) REFERENCES `hellohingoli`.`users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
