-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000
-- Generation Time: Dec 30, 2025 at 05:36 AM
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

--
-- Dumping data for table `app_config`
--

INSERT INTO `app_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES
(1, 'min_version', '1.0.4', 'Minimum required app version - users below this MUST update', '2025-12-28 19:30:29'),
(2, 'latest_version', '1.0.4', 'Latest available app version', '2025-12-28 19:30:29'),
(3, 'force_update', 'false', 'Force all users to update (true/false)', '2025-12-29 19:20:25'),
(4, 'update_message', 'A new version is available with important updates. Please update to continue.', 'Message shown in update dialog', '2025-12-28 19:30:30'),
(5, 'update_message_mr', 'हिंगोली हब अ‍ॅप लाँच झाले आहे', 'Marathi update message', '2025-12-28 19:30:30'),
(6, 'play_store_url', 'https://play.google.com/store/apps/details?id=com.hingoli.hub', 'Play Store URL', '2025-12-28 19:30:30'),
(30001, 'call_timing_enabled', 'true', NULL, '2025-12-29 15:56:37'),
(30002, 'call_start_hour', '8', NULL, '2025-12-29 15:56:37'),
(30003, 'call_end_hour', '21', NULL, '2025-12-29 16:51:00'),
(30004, 'call_timing_message', 'Call service available from 8 AM to 10 PM', NULL, '2025-12-29 15:56:37'),
(30006, 'call_timing_message_mr', 'कॉल सेवा सकाळी 8 ते रात्री 10 वाजेपर्यंत उपलब्ध', 'Marathi call timing message', '2025-12-29 16:08:23');

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

--
-- Dumping data for table `banners`
--

INSERT INTO `banners` (`banner_id`, `title`, `image_url`, `link_url`, `link_type`, `link_id`, `placement`, `start_date`, `end_date`, `sort_order`, `is_active`, `view_count`, `click_count`, `target_city`, `target_category_id`, `created_at`, `updated_at`, `target_listing_type`) VALUES
(1, 'SERVICE BANNER', 'https://img-s-msn-com.akamaized.net/tenant/amp/entityid/AA1RSsSe.img?w=549&h=309&m=6&x=228&y=38&s=71&d=71', '', 'external', NULL, 'home_bottom', '2025-12-10 06:14:11', NULL, 0, 0, 10, 0, NULL, NULL, '2025-12-10 06:14:11', '2025-12-22 13:32:39', 'all'),
(30001, 'TEST1', 'https://hellohingoli.com/api/uploads/banners/693d0424bc158_1765606436.png', '', 'external', NULL, 'services_top', '2025-12-13 06:13:59', NULL, 0, 0, 145, 0, NULL, NULL, '2025-12-13 06:13:59', '2025-12-22 13:33:03', 'all'),
(60001, 'dfgd', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/banners/693d06f3bf92c_1765607155.png', '', 'external', NULL, 'services_bottom', '2025-12-13 06:26:00', NULL, 0, 0, 138, 0, NULL, NULL, '2025-12-13 06:26:00', '2025-12-22 13:32:50', 'all'),
(120005, '6', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/banners/banners_693d5ba112ac6_1765628833.png', '', 'external', NULL, 'home_top', '2025-12-13 12:27:17', NULL, 1, 1, 946, 0, NULL, NULL, '2025-12-13 12:27:17', '2025-12-29 12:07:19', 'all'),
(120006, '7', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/banners/banners_693d5bc5122d4_1765628869.png', '', 'external', NULL, 'home_top', '2025-12-13 12:27:53', NULL, 3, 0, 906, 0, NULL, NULL, '2025-12-13 12:27:53', '2025-12-25 10:53:53', 'all'),
(180001, 'g', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/banners/694d16b79f811_1766659767.webp', '', 'external', NULL, 'home_top', '2025-12-25 10:49:31', NULL, 7, 1, 37, 0, NULL, NULL, '2025-12-25 10:49:31', '2025-12-29 12:07:19', 'all');

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

--
-- Dumping data for table `business_listings`
--

INSERT INTO `business_listings` (`listing_id`, `business_name`, `registration_number`, `industry`, `business_type`, `established_year`, `employee_count`, `annual_revenue_range`, `website_url`, `business_email`, `business_phone`, `asking_price`, `reason_for_sale`, `custom_attributes`) VALUES
(11, 'Sharma Electronics', NULL, 'retail', 'sole_proprietor', '2005', '1-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(12, 'Hotel Marathi Tadka', NULL, 'food_service', 'sole_proprietor', '2010', '1-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(13, 'Dr. Patil Health Clinic', NULL, 'healthcare', 'sole_proprietor', '2003', '1-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(14, 'Patil Kirana & General Store', NULL, 'retail', 'sole_proprietor', '1998', '1-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(15, 'Digital World Mobile Shop', NULL, 'retail', 'sole_proprietor', '2015', '1-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(16, 'Shree Bakery & Cake Shop', NULL, 'food_service', 'sole_proprietor', '2012', '1-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(17, 'Shivaji Medical Store', NULL, 'healthcare', 'sole_proprietor', '2008', '1-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(23, 'HINGOLI HUB', NULL, NULL, NULL, NULL, '1-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(24, 'शिवरेखा पुस्तकालय 📚', NULL, 'books 📚', NULL, '2023', '1-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(25, 'Aaroh\'s Shop', NULL, NULL, NULL, NULL, '1-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(27, 'ELECTRICAL', NULL, NULL, NULL, NULL, '1-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(30, 'my business', NULL, 'food', NULL, '2010', '1-10', NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(31, 'jio\'s Shop', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

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

--
-- Dumping data for table `cart_items`
--

INSERT INTO `cart_items` (`cart_item_id`, `user_id`, `listing_id`, `product_id`, `quantity`, `created_at`) VALUES
(660004, 480002, NULL, 30002, 2, '2025-12-21 01:40:59'),
(660005, 480002, NULL, 293950, 1, '2025-12-21 01:44:37'),
(1110001, 7020034431, NULL, 20, 1, '2025-12-26 13:30:13'),
(1110002, 7020034431, NULL, 19, 1, '2025-12-26 13:30:17'),
(1140001, 9595370264, NULL, 20, 1, '2025-12-26 14:19:46'),
(1200002, 450002, NULL, 773950, 1, '2025-12-26 17:13:35'),
(1290001, 9595370265, NULL, 743950, 1, '2025-12-27 15:32:19');

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

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`category_id`, `parent_id`, `name`, `name_mr`, `slug`, `listing_type`, `depth`, `path`, `icon_url`, `image_url`, `description`, `sort_order`, `is_active`, `listing_count`, `created_at`, `updated_at`) VALUES
(120001, NULL, 'Home Services', 'घर सेवा', 'home-services', 'services', 0, NULL, 'home', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/home-services.jpg', NULL, 1, 1, 2, '2025-12-12 12:53:32', '2025-12-30 04:09:02'),
(120002, NULL, 'Vehicle Services', 'वाहन सेवा', 'vehicle-services', 'services', 0, NULL, 'directions_car', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/vehicle-services.jpg', NULL, 2, 1, 0, '2025-12-12 12:53:32', '2025-12-21 17:42:45'),
(120003, NULL, 'Beauty & Wellness', 'सौंदर्य आणि आरोग्य', 'beauty-wellness', 'services', 0, NULL, 'spa', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/beauty-wellness.jpg', NULL, 3, 1, 0, '2025-12-12 12:53:32', '2025-12-12 16:06:48'),
(120004, NULL, 'Health Services', 'आरोग्य सेवा', 'health-services', 'services', 0, NULL, 'health_and_safety', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/health-services.jpg', NULL, 4, 1, 0, '2025-12-12 12:53:32', '2025-12-12 16:06:49'),
(120005, NULL, 'Education & Tuition', 'शिक्षण आणि ट्यूशन', 'education-tuition', 'services', 0, NULL, 'school', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/education-tuition.jpg', NULL, 5, 1, 0, '2025-12-12 12:53:32', '2025-12-12 16:06:49'),
(120006, NULL, 'Event Services', 'कार्यक्रम सेवा', 'event-services', 'services', 0, NULL, 'celebration', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/event-services.jpg', NULL, 6, 1, 0, '2025-12-12 12:53:32', '2025-12-12 16:06:49'),
(120007, NULL, 'Transport & Logistics', 'वाहतूक आणि लॉजिस्टिक्स', 'transport-logistics', 'services', 0, NULL, 'local_shipping', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/transport-logistics.jpg', NULL, 7, 1, 0, '2025-12-12 12:53:32', '2025-12-12 16:06:49'),
(120008, NULL, 'Agricultural Services', 'कृषी सेवा', 'agricultural-services', 'services', 0, NULL, 'agriculture', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/agricultural-services.jpg', NULL, 8, 1, 0, '2025-12-12 12:53:32', '2025-12-12 16:06:50'),
(120009, NULL, 'Construction & Renovation', 'बांधकाम आणि नूतनीकरण', 'construction-renovation', 'services', 0, NULL, 'construction', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/construction-renovation.jpg', NULL, 9, 1, 0, '2025-12-12 12:53:32', '2025-12-12 16:06:50'),
(120010, NULL, 'Professional Services', 'व्यावसायिक सेवा', 'professional-services', 'services', 0, NULL, 'business_center', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/professional-services.jpg', NULL, 10, 1, 0, '2025-12-12 12:53:32', '2025-12-12 16:06:51'),
(120011, NULL, 'IT & Digital Services', 'आयटी आणि डिजिटल सेवा', 'it-digital-services', 'services', 0, NULL, 'computer', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/it-digital-services.jpg', NULL, 11, 1, 0, '2025-12-12 12:53:32', '2025-12-12 16:06:51'),
(120012, NULL, 'Daily Needs', 'दैनंदिन गरजा', 'daily-needs', 'services', 0, NULL, 'shopping_basket', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/daily-needs.jpg', NULL, 12, 1, 0, '2025-12-12 12:53:32', '2025-12-12 16:06:51'),
(120013, 120001, 'Electrician', 'इलेक्ट्रिशियन', 'electrician', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/electrician.jpg', NULL, 1, 1, 1, '2025-12-12 12:53:41', '2025-12-21 17:42:45'),
(120014, 120001, 'Plumber', 'प्लंबर', 'plumber', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/plumber.jpg', NULL, 2, 1, 1, '2025-12-12 12:53:41', '2025-12-21 17:42:45'),
(120015, 120001, 'Carpenter', 'सुतार', 'carpenter', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/carpenter.jpg', NULL, 3, 1, 1, '2025-12-12 12:53:41', '2025-12-21 17:42:45'),
(120016, 120001, 'Painter', 'पेंटर', 'painter', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/painter.jpg', NULL, 4, 1, 1, '2025-12-12 12:53:41', '2025-12-21 17:42:45'),
(120017, 120001, 'AC Repair & Service', 'AC दुरुस्ती', 'ac-repair-service', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/ac-repair-service.jpg', NULL, 5, 1, 1, '2025-12-12 12:53:41', '2025-12-21 17:42:45'),
(120018, 120001, 'Refrigerator Repair', 'फ्रिज दुरुस्ती', 'refrigerator-repair', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/refrigerator-repair.jpg', NULL, 6, 1, 0, '2025-12-12 12:53:41', '2025-12-12 16:06:53'),
(120019, 120001, 'Washing Machine Repair', 'वॉशिंग मशीन दुरुस्ती', 'washing-machine-repair', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/washing-machine-repair.jpg', NULL, 7, 1, 0, '2025-12-12 12:53:41', '2025-12-12 16:06:54'),
(120020, 120001, 'TV Repair', 'टीव्ही दुरुस्ती', 'tv-repair', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/tv-repair.jpg', NULL, 8, 1, 0, '2025-12-12 12:53:41', '2025-12-12 16:06:54'),
(120021, 120001, 'RO/Water Purifier Service', 'आरओ सर्व्हिस', 'ro-water-purifier', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/ro-water-purifier.jpg', NULL, 9, 1, 0, '2025-12-12 12:53:41', '2025-12-12 16:06:54'),
(120022, 120001, 'Pest Control', 'कीटक नियंत्रण', 'pest-control', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/pest-control.jpg', NULL, 10, 1, 1, '2025-12-12 12:53:41', '2025-12-21 17:42:45'),
(120023, 120001, 'Home Cleaning', 'घर साफसफाई', 'home-cleaning', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/home-cleaning.jpg', NULL, 11, 1, 1, '2025-12-12 12:53:41', '2025-12-21 17:42:45'),
(120024, 120001, 'Sofa/Carpet Cleaning', 'सोफा/कार्पेट क्लीनिंग', 'sofa-carpet-cleaning', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/sofa-carpet-cleaning.jpg', NULL, 12, 1, 0, '2025-12-12 12:53:41', '2025-12-12 16:06:54'),
(120025, 120001, 'Water Tank Cleaning', 'पाण्याची टाकी साफ', 'water-tank-cleaning', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/water-tank-cleaning.jpg', NULL, 13, 1, 0, '2025-12-12 12:53:41', '2025-12-12 16:06:55'),
(120026, 120002, 'Bike Repair', 'बाईक दुरुस्ती', 'bike-repair', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/bike-repair.jpg', NULL, 1, 1, 0, '2025-12-12 12:53:42', '2025-12-12 16:06:55'),
(120027, 120002, 'Car Mechanic', 'कार मेकॅनिक', 'car-mechanic', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/car-mechanic.jpg', NULL, 2, 1, 0, '2025-12-12 12:53:42', '2025-12-12 16:06:55'),
(120028, 120002, 'Tractor Repair', 'ट्रॅक्टर दुरुस्ती', 'tractor-repair', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/tractor-repair.jpg', NULL, 3, 1, 0, '2025-12-12 12:53:42', '2025-12-12 16:06:55'),
(120029, 120002, 'Puncture Repair', 'पंक्चर दुरुस्ती', 'puncture-repair', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/puncture-repair.jpg', NULL, 4, 1, 0, '2025-12-12 12:53:42', '2025-12-12 16:06:56'),
(120030, 120002, 'Car Wash', 'कार वॉश', 'car-wash', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/car-wash.jpg', NULL, 5, 1, 0, '2025-12-12 12:53:42', '2025-12-12 16:06:56'),
(120031, 120002, 'Auto Electrician', 'ऑटो इलेक्ट्रिशियन', 'auto-electrician', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/auto-electrician.jpg', NULL, 6, 1, 0, '2025-12-12 12:53:42', '2025-12-12 16:06:57'),
(120032, 120002, 'Denting & Painting', 'डेंटिंग पेंटिंग', 'denting-painting', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/denting-painting.jpg', NULL, 7, 1, 0, '2025-12-12 12:53:42', '2025-12-12 16:06:57'),
(120033, 120002, 'Towing Service', 'टोइंग सेवा', 'towing-service', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/towing-service.jpg', NULL, 8, 1, 0, '2025-12-12 12:53:42', '2025-12-12 16:06:57'),
(120034, 120003, 'Ladies Salon', 'लेडीज सलून', 'ladies-salon', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/ladies-salon.jpg', NULL, 1, 1, 0, '2025-12-12 12:53:43', '2025-12-12 16:06:57'),
(120035, 120003, 'Gents Salon', 'जेंट्स सलून', 'gents-salon', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/gents-salon.jpg', NULL, 2, 1, 0, '2025-12-12 12:53:43', '2025-12-12 16:06:57'),
(120036, 120003, 'Bridal Makeup', 'ब्रायडल मेकअप', 'bridal-makeup', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/bridal-makeup.jpg', NULL, 3, 1, 1, '2025-12-12 12:53:43', '2025-12-21 17:42:45'),
(120037, 120003, 'Mehendi Artist', 'मेहंदी आर्टिस्ट', 'mehendi-artist', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/mehendi-artist.jpg', NULL, 4, 1, 0, '2025-12-12 12:53:43', '2025-12-12 16:06:57'),
(120038, 120003, 'Spa & Massage', 'स्पा आणि मसाज', 'spa-massage', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/spa-massage.jpg', NULL, 5, 1, 0, '2025-12-12 12:53:43', '2025-12-12 16:06:58'),
(120039, 120003, 'Home Salon Service', 'होम सलून सर्व्हिस', 'home-salon-service', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/home-salon-service.jpg', NULL, 6, 1, 0, '2025-12-12 12:53:43', '2025-12-12 16:06:58'),
(120040, 120004, 'Doctor Consultation', 'डॉक्टर सल्लामसलत', 'doctor-consultation', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/doctor-consultation.jpg', NULL, 1, 1, 0, '2025-12-12 12:53:43', '2025-12-12 16:06:58'),
(120041, 120004, 'Nurse at Home', 'होम नर्स', 'nurse-at-home', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/nurse-at-home.jpg', NULL, 2, 1, 0, '2025-12-12 12:53:43', '2025-12-12 16:06:58'),
(120042, 120004, 'Physiotherapy', 'फिजिओथेरपी', 'physiotherapy', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/physiotherapy.jpg', NULL, 3, 1, 0, '2025-12-12 12:53:43', '2025-12-12 16:06:58'),
(120043, 120004, 'Lab Tests at Home', 'घरी लॅब टेस्ट', 'lab-tests-home', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/lab-tests-home.jpg', NULL, 4, 1, 0, '2025-12-12 12:53:43', '2025-12-12 16:06:58'),
(120044, 120004, 'Medical Equipment Rental', 'मेडिकल उपकरणे भाड्याने', 'medical-equipment-rental', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/medical-equipment-rental.jpg', NULL, 5, 1, 0, '2025-12-12 12:53:43', '2025-12-12 16:06:59'),
(120045, 120004, 'Ambulance Service', 'रुग्णवाहिका सेवा', 'ambulance-service', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/ambulance-service.jpg', NULL, 6, 1, 0, '2025-12-12 12:53:43', '2025-12-12 16:06:59'),
(120046, 120005, 'Home Tutor', 'होम ट्युटर', 'home-tutor', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/home-tutor.jpg', NULL, 1, 1, 1, '2025-12-12 12:53:44', '2025-12-21 17:42:45'),
(120047, 120005, 'Computer Training', 'कॉम्प्युटर प्रशिक्षण', 'computer-training', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/computer-training.jpg', NULL, 2, 1, 0, '2025-12-12 12:53:44', '2025-12-12 16:06:59'),
(120048, 120005, 'English Speaking', 'इंग्रजी बोलणे', 'english-speaking', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/english-speaking.jpg', NULL, 3, 1, 0, '2025-12-12 12:53:44', '2025-12-12 16:06:59'),
(120049, 120005, 'Music Classes', 'संगीत वर्ग', 'music-classes', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/music-classes.jpg', NULL, 4, 1, 0, '2025-12-12 12:53:44', '2025-12-12 16:06:59'),
(120050, 120005, 'Dance Classes', 'नृत्य वर्ग', 'dance-classes', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/dance-classes.jpg', NULL, 5, 1, 0, '2025-12-12 12:53:44', '2025-12-12 16:07:00'),
(120051, 120005, 'Yoga Classes', 'योग वर्ग', 'yoga-classes', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/yoga-classes.jpg', NULL, 6, 1, 0, '2025-12-12 12:53:44', '2025-12-12 16:07:00'),
(120052, 120005, 'Coaching Classes', 'कोचिंग क्लासेस', 'coaching-classes', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/coaching-classes.jpg', NULL, 7, 1, 0, '2025-12-12 12:53:44', '2025-12-12 16:07:00'),
(120053, 120006, 'Wedding Photography', 'लग्न फोटोग्राफी', 'wedding-photography', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/wedding-photography.jpg', NULL, 1, 1, 1, '2025-12-12 12:53:45', '2025-12-21 17:42:45'),
(120054, 120006, 'Video Shooting', 'व्हिडिओ शूटिंग', 'video-shooting', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/video-shooting.jpg', NULL, 2, 1, 0, '2025-12-12 12:53:45', '2025-12-12 16:07:01'),
(120055, 120006, 'Catering Service', 'केटरिंग सेवा', 'catering-service', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/catering-service.jpg', NULL, 3, 1, 0, '2025-12-12 12:53:45', '2025-12-12 16:07:01'),
(120056, 120006, 'Tent & Decoration', 'मंडप आणि सजावट', 'tent-decoration', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/tent-decoration.jpg', NULL, 4, 1, 0, '2025-12-12 12:53:45', '2025-12-12 16:07:01'),
(120057, 120006, 'DJ & Sound System', 'डीजे आणि साउंड', 'dj-sound-system', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/dj-sound-system.jpg', NULL, 5, 1, 0, '2025-12-12 12:53:45', '2025-12-12 16:07:02'),
(120058, 120006, 'Event Planner', 'इव्हेंट प्लॅनर', 'event-planner', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/event-planner.jpg', NULL, 6, 1, 0, '2025-12-12 12:53:45', '2025-12-12 16:07:02'),
(120059, 120006, 'Flower Decoration', 'फुलांची सजावट', 'flower-decoration', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/flower-decoration.jpg', NULL, 7, 1, 0, '2025-12-12 12:53:45', '2025-12-12 16:07:02'),
(120060, 120006, 'Pandit/Priest', 'पुजारी/भटजी', 'pandit-priest', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/pandit-priest.jpg', NULL, 8, 1, 0, '2025-12-12 12:53:45', '2025-12-12 16:07:02'),
(120061, 120007, 'Packers & Movers', 'पॅकर्स अँड मूव्हर्स', 'packers-movers', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/packers-movers.jpg', NULL, 1, 1, 0, '2025-12-12 12:53:46', '2025-12-12 16:07:02'),
(120062, 120007, 'Tempo/Truck on Rent', 'टेम्पो/ट्रक भाड्याने', 'tempo-truck-rent', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/tempo-truck-rent.jpg', NULL, 2, 1, 0, '2025-12-12 12:53:46', '2025-12-12 16:07:02'),
(120063, 120007, 'Auto Rickshaw Booking', 'ऑटो रिक्षा बुकिंग', 'auto-rickshaw', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/auto-rickshaw.jpg', NULL, 3, 1, 0, '2025-12-12 12:53:46', '2025-12-12 16:07:03'),
(120064, 120007, 'Taxi/Cab Service', 'टॅक्सी सेवा', 'taxi-cab-service', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/taxi-cab-service.jpg', NULL, 4, 1, 0, '2025-12-12 12:53:46', '2025-12-12 16:07:03'),
(120065, 120007, 'Courier & Delivery', 'कुरिअर आणि डिलिव्हरी', 'courier-delivery', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/courier-delivery.jpg', NULL, 5, 1, 0, '2025-12-12 12:53:46', '2025-12-12 16:07:03'),
(120066, 120008, 'Tractor on Rent', 'ट्रॅक्टर भाड्याने', 'tractor-rent', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/tractor-rent.jpg', NULL, 1, 1, 0, '2025-12-12 12:53:46', '2025-12-12 16:07:04'),
(120067, 120008, 'JCB on Rent', 'JCB भाड्याने', 'jcb-rent', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/jcb-rent.jpg', NULL, 2, 1, 0, '2025-12-12 12:53:46', '2025-12-12 16:07:04'),
(120068, 120008, 'Harvester on Rent', 'हार्वेस्टर भाड्याने', 'harvester-rent', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/harvester-rent.jpg', NULL, 3, 1, 0, '2025-12-12 12:53:46', '2025-12-12 16:07:04'),
(120069, 120008, 'Borewell Drilling', 'बोअरवेल खोदकाम', 'borewell-drilling', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/borewell-drilling.jpg', NULL, 4, 1, 0, '2025-12-12 12:53:46', '2025-12-12 16:07:05'),
(120070, 120008, 'Pesticide Spraying', 'कीटकनाशक फवारणी', 'pesticide-spraying', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/pesticide-spraying.jpg', NULL, 5, 1, 0, '2025-12-12 12:53:46', '2025-12-12 16:07:05'),
(120071, 120008, 'Veterinary Doctor', 'पशुवैद्य', 'veterinary-doctor', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/veterinary-doctor.jpg', NULL, 6, 1, 0, '2025-12-12 12:53:46', '2025-12-12 16:07:05'),
(120072, 120008, 'Farm Labour', 'शेतमजूर', 'farm-labour', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/farm-labour.jpg', NULL, 7, 1, 0, '2025-12-12 12:53:46', '2025-12-12 16:07:05'),
(120073, 120009, 'Building Contractor', 'बिल्डिंग कॉन्ट्रॅक्टर', 'building-contractor', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/building-contractor.jpg', NULL, 1, 1, 0, '2025-12-12 12:53:47', '2025-12-12 16:07:05'),
(120074, 120009, 'Mason/Bricklayer', 'गवंडी', 'mason-bricklayer', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/mason-bricklayer.jpg', NULL, 2, 1, 0, '2025-12-12 12:53:47', '2025-12-12 16:07:05'),
(120075, 120009, 'Tile & Flooring', 'टाइल आणि फ्लोअरिंग', 'tile-flooring', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/tile-flooring.jpg', NULL, 3, 1, 0, '2025-12-12 12:53:47', '2025-12-12 16:07:06'),
(120076, 120009, 'Fabrication Work', 'फॅब्रिकेशन काम', 'fabrication-work', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/fabrication-work.jpg', NULL, 4, 1, 0, '2025-12-12 12:53:47', '2025-12-12 16:07:06'),
(120077, 120009, 'Welding', 'वेल्डिंग', 'welding', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/welding.jpg', NULL, 5, 1, 0, '2025-12-12 12:53:47', '2025-12-12 16:07:06'),
(120078, 120009, 'Interior Designer', 'इंटीरियर डिझायनर', 'interior-designer', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/interior-designer.jpg', NULL, 6, 1, 0, '2025-12-12 12:53:47', '2025-12-12 16:07:06'),
(120079, 120009, 'False Ceiling', 'फॉल्स सीलिंग', 'false-ceiling', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/false-ceiling.jpg', NULL, 7, 1, 0, '2025-12-12 12:53:47', '2025-12-12 16:07:06'),
(120080, 120009, 'Aluminium/Glass Work', 'अल्युमिनियम/काच काम', 'aluminium-glass-work', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/aluminium-glass-work.jpg', NULL, 8, 1, 0, '2025-12-12 12:53:47', '2025-12-12 16:07:07'),
(120081, 120010, 'CA/Tax Consultant', 'सीए/टॅक्स सल्लागार', 'ca-tax-consultant', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/ca-tax-consultant.jpg', NULL, 1, 1, 0, '2025-12-12 12:53:48', '2025-12-12 16:07:07'),
(120082, 120010, 'Advocate/Lawyer', 'वकील', 'advocate-lawyer', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/advocate-lawyer.jpg', NULL, 2, 1, 0, '2025-12-12 12:53:48', '2025-12-12 16:07:07'),
(120083, 120010, 'Insurance Agent', 'विमा एजंट', 'insurance-agent', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/insurance-agent.jpg', NULL, 3, 1, 0, '2025-12-12 12:53:48', '2025-12-12 16:07:07'),
(120084, 120010, 'Real Estate Agent', 'रिअल इस्टेट एजंट', 'real-estate-agent', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/real-estate-agent.jpg', NULL, 4, 1, 0, '2025-12-12 12:53:48', '2025-12-12 16:07:08'),
(120085, 120010, 'Loan Consultant', 'कर्ज सल्लागार', 'loan-consultant', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/loan-consultant.jpg', NULL, 5, 1, 0, '2025-12-12 12:53:48', '2025-12-12 16:07:08'),
(120086, 120010, 'Photocopy/Printing', 'फोटोकॉपी/प्रिंटिंग', 'photocopy-printing', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/photocopy-printing.jpg', NULL, 6, 1, 0, '2025-12-12 12:53:48', '2025-12-12 16:07:08'),
(120087, 120010, 'Passport/Visa Agent', 'पासपोर्ट/व्हिसा एजंट', 'passport-visa-agent', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/passport-visa-agent.jpg', NULL, 7, 1, 0, '2025-12-12 12:53:48', '2025-12-12 16:07:09'),
(120088, 120011, 'Computer Repair', 'कॉम्प्युटर दुरुस्ती', 'computer-repair', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/computer-repair.jpg', NULL, 1, 1, 0, '2025-12-12 12:53:49', '2025-12-12 16:07:09'),
(120089, 120011, 'Mobile Repair', 'मोबाइल दुरुस्ती', 'mobile-repair', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/mobile-repair.jpg', NULL, 2, 1, 0, '2025-12-12 12:53:49', '2025-12-12 16:07:09'),
(120090, 120011, 'CCTV Installation', 'सीसीटीव्ही बसवणे', 'cctv-installation', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/cctv-installation.jpg', NULL, 3, 1, 0, '2025-12-12 12:53:49', '2025-12-12 16:07:09'),
(120091, 120011, 'WiFi/Internet Setup', 'वायफाय/इंटरनेट सेटअप', 'wifi-internet-setup', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/wifi-internet-setup.jpg', NULL, 4, 1, 0, '2025-12-12 12:53:49', '2025-12-12 16:07:09'),
(120092, 120011, 'Website Development', 'वेबसाइट बनवणे', 'website-development', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/website-development.jpg', NULL, 5, 1, 0, '2025-12-12 12:53:49', '2025-12-12 16:07:09'),
(120093, 120011, 'Online Form Filling', 'ऑनलाइन फॉर्म भरणे', 'online-form-filling', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/online-form-filling.jpg', NULL, 6, 1, 0, '2025-12-12 12:53:49', '2025-12-12 16:07:10'),
(120094, 120012, 'Milkman', 'दूधवाला', 'milkman', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/milkman.jpg', NULL, 1, 1, 0, '2025-12-12 12:53:50', '2025-12-12 16:07:10'),
(120095, 120012, 'Newspaper Delivery', 'वर्तमानपत्र वितरण', 'newspaper-delivery', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/newspaper-delivery.jpg', NULL, 2, 1, 0, '2025-12-12 12:53:50', '2025-12-12 16:07:10'),
(120096, 120012, 'Tiffin Service', 'टिफिन सेवा', 'tiffin-service', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/tiffin-service.jpg', NULL, 3, 1, 0, '2025-12-12 12:53:50', '2025-12-12 16:07:11'),
(120097, 120012, 'Cook/Chef at Home', 'घरी स्वयंपाकी', 'cook-chef-home', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/cook-chef-home.jpg', NULL, 4, 1, 0, '2025-12-12 12:53:50', '2025-12-12 16:07:11'),
(120098, 120012, 'Maid/Domestic Help', 'मोलकरीण', 'maid-domestic-help', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/maid-domestic-help.jpg', NULL, 5, 1, 0, '2025-12-12 12:53:50', '2025-12-12 16:07:12'),
(120099, 120012, 'Driver on Call', 'चालक', 'driver-on-call', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/driver-on-call.jpg', NULL, 6, 1, 0, '2025-12-12 12:53:50', '2025-12-12 16:07:13'),
(120100, 120012, 'Security Guard', 'सुरक्षा रक्षक', 'security-guard', 'services', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/category_images/security-guard.jpg', NULL, 7, 1, 0, '2025-12-12 12:53:50', '2025-12-12 16:07:13'),
(150001, NULL, 'Vehicles', 'वाहने', 'vehicles', 'selling', 0, NULL, 'directions_car', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/vehicles.jpg', NULL, 1, 1, 0, '2025-12-12 16:24:47', '2025-12-12 16:25:50'),
(150002, NULL, 'Properties', 'मालमत्ता', 'properties', 'selling', 0, NULL, 'home', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/properties.jpg', NULL, 2, 1, 0, '2025-12-12 16:24:47', '2025-12-12 16:25:50'),
(150003, NULL, 'Mobiles & Electronics', 'मोबाईल आणि इलेक्ट्रॉनिक्स', 'mobiles-electronics', 'selling', 0, NULL, 'smartphone', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/mobiles-electronics.jpg', NULL, 3, 1, 0, '2025-12-12 16:24:47', '2025-12-12 16:25:50'),
(150004, NULL, 'Home & Furniture', 'घर आणि फर्निचर', 'home-furniture', 'selling', 0, NULL, 'chair', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/home-furniture.jpg', NULL, 4, 1, 0, '2025-12-12 16:24:47', '2025-12-12 16:25:50'),
(150005, NULL, 'Fashion', 'फॅशन', 'fashion', 'selling', 0, NULL, 'checkroom', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fashion.jpg', NULL, 5, 1, 0, '2025-12-12 16:24:47', '2025-12-12 16:25:50'),
(150006, NULL, 'Agriculture', 'शेती', 'agriculture-selling', 'selling', 0, NULL, 'agriculture', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/agriculture-selling.jpg', NULL, 6, 1, 0, '2025-12-12 16:24:47', '2025-12-12 16:25:51'),
(150007, NULL, 'Books, Sports & Hobbies', 'पुस्तके, खेळ आणि छंद', 'books-sports-hobbies', 'selling', 0, NULL, 'sports_soccer', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', NULL, 7, 1, 0, '2025-12-12 16:24:47', '2025-12-21 17:42:45'),
(150008, NULL, 'Business & Industrial', 'व्यवसाय आणि औद्योगिक', 'business-industrial', 'selling', 0, NULL, 'business', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/business-industrial.jpg', NULL, 8, 1, 0, '2025-12-12 16:24:47', '2025-12-12 16:25:51'),
(150009, NULL, 'Pets', 'पाळीव प्राणी', 'pets', 'selling', 0, NULL, 'pets', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/pets.jpg', NULL, 9, 1, 0, '2025-12-12 16:24:47', '2025-12-12 16:25:51'),
(150010, NULL, 'Other Items', 'इतर वस्तू', 'other-items', 'selling', 0, NULL, 'category', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/other-items.jpg', NULL, 10, 1, 0, '2025-12-12 16:24:47', '2025-12-12 16:25:51'),
(150011, 150001, 'Cars', 'कार', 'cars', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/cars.jpg', NULL, 1, 1, 0, '2025-12-12 16:25:02', '2025-12-12 16:25:51'),
(150012, 150001, 'Motorcycles', 'मोटरसायकल', 'motorcycles', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/motorcycles.jpg', NULL, 2, 1, 0, '2025-12-12 16:25:02', '2025-12-12 16:25:51'),
(150013, 150001, 'Scooters', 'स्कूटर', 'scooters', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/scooters.jpg', NULL, 3, 1, 0, '2025-12-12 16:25:02', '2025-12-12 16:25:51'),
(150014, 150001, 'Bicycles', 'सायकल', 'bicycles', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/bicycles.jpg', NULL, 4, 1, 0, '2025-12-12 16:25:02', '2025-12-12 16:25:52'),
(150015, 150001, 'Commercial Vehicles', 'व्यावसायिक वाहने', 'commercial-vehicles', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/commercial-vehicles.jpg', NULL, 5, 1, 0, '2025-12-12 16:25:02', '2025-12-12 16:25:52'),
(150016, 150001, 'Spare Parts', 'स्पेअर पार्ट्स', 'spare-parts', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/spare-parts.jpg', NULL, 6, 1, 0, '2025-12-12 16:25:02', '2025-12-12 16:25:52'),
(150017, 150001, 'Auto Accessories', 'ऑटो अॅक्सेसरीज', 'auto-accessories', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/auto-accessories.jpg', NULL, 7, 1, 0, '2025-12-12 16:25:02', '2025-12-12 16:25:52'),
(150018, 150002, 'House/Villa for Sale', 'विक्रीसाठी घर/बंगला', 'house-villa-sale', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/house-villa-sale.jpg', NULL, 1, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:52'),
(150019, 150002, 'Flat/Apartment for Sale', 'विक्रीसाठी फ्लॅट', 'flat-apartment-sale', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/flat-apartment-sale.jpg', NULL, 2, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:52'),
(150020, 150002, 'Land/Plot for Sale', 'विक्रीसाठी जमीन/प्लॉट', 'land-plot-sale', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/land-plot-sale.jpg', NULL, 3, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:52'),
(150021, 150002, 'Commercial Property', 'व्यावसायिक मालमत्ता', 'commercial-property', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/commercial-property.jpg', NULL, 4, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:52'),
(150022, 150002, 'House/Flat for Rent', 'भाड्याने घर/फ्लॅट', 'house-flat-rent', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/house-flat-rent.jpg', NULL, 5, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:53'),
(150023, 150002, 'Room/PG for Rent', 'भाड्याने खोली/PG', 'room-pg-rent', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/room-pg-rent.jpg', NULL, 6, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:53'),
(150024, 150002, 'Shop for Rent', 'भाड्याने दुकान', 'shop-rent', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/shop-rent.jpg', NULL, 7, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:53'),
(150025, 150002, 'Agricultural Land', 'शेतजमीन', 'agricultural-land', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/agricultural-land.jpg', NULL, 8, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:53'),
(150026, 150002, 'Warehouse/Godown', 'गोदाम', 'warehouse-godown', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/warehouse-godown.jpg', NULL, 9, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:53'),
(150027, 150003, 'Mobile Phones', 'मोबाईल फोन', 'mobile-phones', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/mobile-phones.jpg', NULL, 1, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:53'),
(150028, 150003, 'Tablets', 'टॅबलेट', 'tablets', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/tablets.jpg', NULL, 2, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:53'),
(150029, 150003, 'Laptops', 'लॅपटॉप', 'laptops', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/laptops.jpg', NULL, 3, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:53'),
(150030, 150003, 'Desktop Computers', 'डेस्कटॉप कॉम्प्युटर', 'desktop-computers', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/desktop-computers.jpg', NULL, 4, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:53'),
(150031, 150003, 'Cameras', 'कॅमेरा', 'cameras', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/cameras.jpg', NULL, 5, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:54'),
(150032, 150003, 'TV & Video', 'टीव्ही आणि व्हिडिओ', 'tv-video', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/tv-video.jpg', NULL, 6, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:54'),
(150033, 150003, 'Audio & Speakers', 'ऑडिओ आणि स्पीकर', 'audio-speakers', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/audio-speakers.jpg', NULL, 7, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:54'),
(150034, 150003, 'Computer Accessories', 'कॉम्प्युटर अॅक्सेसरीज', 'computer-accessories', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/computer-accessories.jpg', NULL, 8, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:54'),
(150035, 150003, 'Gaming', 'गेमिंग', 'gaming', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/gaming.jpg', NULL, 9, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:54'),
(150036, 150003, 'Printers & Scanners', 'प्रिंटर आणि स्कॅनर', 'printers-scanners', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/printers-scanners.jpg', NULL, 10, 1, 0, '2025-12-12 16:25:03', '2025-12-12 16:25:54'),
(150037, 150004, 'Sofa & Dining', 'सोफा आणि डायनिंग', 'sofa-dining', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/sofa-dining.jpg', NULL, 1, 1, 0, '2025-12-12 16:25:05', '2025-12-12 16:25:54'),
(150038, 150004, 'Beds & Wardrobes', 'बेड आणि वॉर्डरोब', 'beds-wardrobes', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/beds-wardrobes.jpg', NULL, 2, 1, 0, '2025-12-12 16:25:05', '2025-12-12 16:25:54'),
(150039, 150004, 'Kitchen & Appliances', 'किचन आणि उपकरणे', 'kitchen-appliances', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/kitchen-appliances.jpg', NULL, 3, 1, 0, '2025-12-12 16:25:05', '2025-12-12 16:25:55'),
(150040, 150004, 'Home Decor', 'होम डेकोर', 'home-decor', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/home-decor.jpg', NULL, 4, 1, 0, '2025-12-12 16:25:05', '2025-12-12 16:25:55'),
(150041, 150004, 'Washing Machine', 'वॉशिंग मशीन', 'washing-machine-sell', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/washing-machine-sell.jpg', NULL, 5, 1, 0, '2025-12-12 16:25:05', '2025-12-12 16:25:55'),
(150042, 150004, 'Refrigerator', 'फ्रिज', 'refrigerator-sell', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/refrigerator-sell.jpg', NULL, 6, 1, 0, '2025-12-12 16:25:05', '2025-12-12 16:25:55'),
(150043, 150004, 'AC', 'एसी', 'ac-sell', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/ac-sell.jpg', NULL, 7, 1, 0, '2025-12-12 16:25:05', '2025-12-12 16:25:55'),
(150044, 150004, 'Fans & Coolers', 'पंखे आणि कूलर', 'fans-coolers', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fans-coolers.jpg', NULL, 8, 1, 0, '2025-12-12 16:25:05', '2025-12-12 16:25:55'),
(150045, 150004, 'Tables & Chairs', 'टेबल आणि खुर्च्या', 'tables-chairs', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/tables-chairs.jpg', NULL, 9, 1, 0, '2025-12-12 16:25:05', '2025-12-12 16:25:55'),
(150046, 150004, 'Other Appliances', 'इतर उपकरणे', 'other-appliances', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/other-appliances.jpg', NULL, 10, 1, 0, '2025-12-12 16:25:05', '2025-12-12 16:25:55'),
(150047, 150005, 'Mens Clothing', 'पुरुषांचे कपडे', 'mens-clothing', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/mens-clothing.jpg', NULL, 1, 1, 0, '2025-12-12 16:25:06', '2025-12-12 16:25:56'),
(150048, 150005, 'Womens Clothing', 'महिलांचे कपडे', 'womens-clothing', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/womens-clothing.jpg', NULL, 2, 1, 0, '2025-12-12 16:25:06', '2025-12-12 16:25:56'),
(150049, 150005, 'Kids Clothing', 'मुलांचे कपडे', 'kids-clothing', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/kids-clothing.jpg', NULL, 3, 1, 0, '2025-12-12 16:25:06', '2025-12-12 16:25:56'),
(150050, 150005, 'Footwear', 'पादत्राणे', 'footwear', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/footwear.jpg', NULL, 4, 1, 0, '2025-12-12 16:25:06', '2025-12-12 16:25:56'),
(150051, 150005, 'Watches', 'घड्याळे', 'watches', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/watches.jpg', NULL, 5, 1, 0, '2025-12-12 16:25:06', '2025-12-12 16:25:56'),
(150052, 150005, 'Jewellery', 'दागिने', 'jewellery', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/jewellery.jpg', NULL, 6, 1, 0, '2025-12-12 16:25:06', '2025-12-12 16:25:56'),
(150053, 150005, 'Bags & Luggage', 'बॅग आणि लगेज', 'bags-luggage', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/bags-luggage.jpg', NULL, 7, 1, 0, '2025-12-12 16:25:06', '2025-12-12 16:25:56'),
(150054, 150005, 'Sunglasses', 'सनग्लासेस', 'sunglasses', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/sunglasses.jpg', NULL, 8, 1, 0, '2025-12-12 16:25:06', '2025-12-12 16:25:56'),
(150055, 150006, 'Tractors', 'ट्रॅक्टर', 'tractors', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/tractors.jpg', NULL, 1, 1, 0, '2025-12-12 16:25:07', '2025-12-12 16:25:56'),
(150056, 150006, 'Farm Equipment', 'शेती उपकरणे', 'farm-equipment', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/farm-equipment.jpg', NULL, 2, 1, 0, '2025-12-12 16:25:07', '2025-12-12 16:25:57'),
(150057, 150006, 'Seeds & Plants', 'बियाणे आणि रोपे', 'seeds-plants', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/seeds-plants.jpg', NULL, 3, 1, 0, '2025-12-12 16:25:07', '2025-12-12 16:25:57'),
(150058, 150006, 'Fertilizers', 'खते', 'fertilizers', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fertilizers.jpg', NULL, 4, 1, 0, '2025-12-12 16:25:07', '2025-12-12 16:25:57'),
(150059, 150006, 'Cattle/Livestock', 'गुरे/पशुधन', 'cattle-livestock', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/cattle-livestock.jpg', NULL, 5, 1, 0, '2025-12-12 16:25:07', '2025-12-12 16:25:57'),
(150060, 150006, 'Poultry', 'कुक्कुटपालन', 'poultry', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/poultry.jpg', NULL, 6, 1, 0, '2025-12-12 16:25:07', '2025-12-12 16:25:57'),
(150061, 150006, 'Fish/Aquaculture', 'मत्स्यव्यवसाय', 'fish-aquaculture', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fish-aquaculture.jpg', NULL, 7, 1, 0, '2025-12-12 16:25:07', '2025-12-12 16:25:57'),
(150062, 150006, 'Farm Produce', 'शेतीमाल', 'farm-produce', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/farm-produce.jpg', NULL, 8, 1, 0, '2025-12-12 16:25:07', '2025-12-12 16:25:57'),
(150063, 150007, 'Books', 'पुस्तके', 'books', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books.jpg', NULL, 1, 1, 0, '2025-12-12 16:25:08', '2025-12-12 16:25:57'),
(150064, 150007, 'Sports Equipment', 'क्रीडा साहित्य', 'sports-equipment', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/sports-equipment.jpg', NULL, 2, 1, 0, '2025-12-12 16:25:08', '2025-12-12 16:25:58'),
(150065, 150007, 'Gym Equipment', 'जिम उपकरणे', 'gym-equipment', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/gym-equipment.jpg', NULL, 3, 1, 0, '2025-12-12 16:25:08', '2025-12-12 16:25:58'),
(150066, 150007, 'Musical Instruments', 'वाद्ये', 'musical-instruments', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/musical-instruments.jpg', NULL, 4, 1, 0, '2025-12-12 16:25:08', '2025-12-12 16:25:58'),
(150067, 150007, 'Collectibles', 'संग्रहणीय वस्तू', 'collectibles', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/collectibles.jpg', NULL, 5, 1, 0, '2025-12-12 16:25:08', '2025-12-12 16:25:58'),
(150068, 150007, 'Toys', 'खेळणी', 'toys', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/toys.jpg', NULL, 6, 1, 0, '2025-12-12 16:25:08', '2025-12-12 16:25:58'),
(150069, 150007, 'Art & Crafts', 'कला आणि हस्तकला', 'art-crafts', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/art-crafts.jpg', NULL, 7, 1, 0, '2025-12-12 16:25:08', '2025-12-12 16:25:58'),
(150070, 150008, 'Office Furniture', 'ऑफिस फर्निचर', 'office-furniture', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/office-furniture.jpg', NULL, 1, 1, 0, '2025-12-12 16:25:09', '2025-12-12 16:25:58'),
(150071, 150008, 'Machinery', 'यंत्रसामग्री', 'machinery', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/machinery.jpg', NULL, 2, 1, 0, '2025-12-12 16:25:09', '2025-12-12 16:25:59'),
(150072, 150008, 'Medical Equipment', 'वैद्यकीय उपकरणे', 'medical-equipment', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/medical-equipment.jpg', NULL, 3, 1, 0, '2025-12-12 16:25:09', '2025-12-12 16:25:59'),
(150073, 150008, 'Restaurant Equipment', 'रेस्टॉरंट उपकरणे', 'restaurant-equipment', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/restaurant-equipment.jpg', NULL, 4, 1, 0, '2025-12-12 16:25:09', '2025-12-12 16:25:59'),
(150074, 150008, 'Raw Materials', 'कच्चा माल', 'raw-materials', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/raw-materials.jpg', NULL, 5, 1, 0, '2025-12-12 16:25:09', '2025-12-12 16:25:59'),
(150075, 150008, 'Safety Equipment', 'सुरक्षा उपकरणे', 'safety-equipment', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/safety-equipment.jpg', NULL, 6, 1, 0, '2025-12-12 16:25:09', '2025-12-12 16:25:59'),
(150076, 150008, 'Tools', 'साधने', 'tools', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/tools.jpg', NULL, 7, 1, 0, '2025-12-12 16:25:09', '2025-12-12 16:25:59'),
(150077, 150009, 'Dogs', 'कुत्रे', 'dogs', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/dogs.jpg', NULL, 1, 1, 0, '2025-12-12 16:25:10', '2025-12-12 16:25:59'),
(150078, 150009, 'Cats', 'मांजर', 'cats', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/cats.jpg', NULL, 2, 1, 0, '2025-12-12 16:25:10', '2025-12-12 16:25:59'),
(150079, 150009, 'Birds', 'पक्षी', 'birds', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/birds.jpg', NULL, 3, 1, 0, '2025-12-12 16:25:10', '2025-12-12 16:26:00'),
(150080, 150009, 'Fish & Aquarium', 'मासे आणि मत्स्यालय', 'fish-aquarium', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fish-aquarium.jpg', NULL, 4, 1, 0, '2025-12-12 16:25:10', '2025-12-12 16:26:00'),
(150081, 150009, 'Pet Food & Accessories', 'पाळीव प्राण्यांचे अन्न आणि सामान', 'pet-food-accessories', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/pet-food-accessories.jpg', NULL, 5, 1, 0, '2025-12-12 16:25:10', '2025-12-12 16:26:00'),
(150082, 150010, 'Antiques', 'प्राचीन वस्तू', 'antiques', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/antiques.jpg', NULL, 1, 1, 0, '2025-12-12 16:25:11', '2025-12-12 16:26:01'),
(150083, 150010, 'Free Items', 'मोफत वस्तू', 'free-items', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/free-items.jpg', NULL, 2, 1, 0, '2025-12-12 16:25:11', '2025-12-12 16:26:01'),
(150084, 150010, 'Exchange/Swap', 'बदली/स्वॅप', 'exchange-swap', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/exchange-swap.jpg', NULL, 3, 1, 0, '2025-12-12 16:25:11', '2025-12-12 16:26:01'),
(150085, 150010, 'Miscellaneous', 'विविध', 'miscellaneous', 'selling', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/miscellaneous.jpg', NULL, 4, 1, 0, '2025-12-12 16:25:11', '2025-12-12 16:26:01'),
(150086, NULL, 'Food & Restaurants', 'खाद्य आणि रेस्टॉरंट', 'food-restaurants', 'business', 0, NULL, 'restaurant', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/food-restaurants.jpg', NULL, 1, 1, 1, '2025-12-12 16:30:44', '2025-12-30 04:10:02'),
(150087, NULL, 'Grocery & Daily Needs', 'किराणा आणि दैनंदिन गरजा', 'grocery-daily-needs', 'business', 0, NULL, 'shopping_cart', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/grocery-daily-needs.jpg', NULL, 2, 1, 0, '2025-12-12 16:30:44', '2025-12-21 17:42:45'),
(150088, NULL, 'Shopping & Retail', 'खरेदी आणि किरकोळ', 'shopping-retail', 'business', 0, NULL, 'store', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/shopping-retail.jpg', NULL, 3, 1, 2, '2025-12-12 16:30:44', '2025-12-29 11:52:41'),
(150089, NULL, 'Healthcare', 'आरोग्यसेवा', 'healthcare', 'business', 0, NULL, 'local_hospital', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/healthcare.jpg', NULL, 4, 1, 0, '2025-12-12 16:30:44', '2025-12-12 16:37:50'),
(150090, NULL, 'Education', 'शिक्षण', 'education-business', 'business', 0, NULL, 'school', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/education-business.jpg', NULL, 5, 1, 1, '2025-12-12 16:30:44', '2025-12-27 06:42:38'),
(150091, NULL, 'Automotive', 'ऑटोमोटिव्ह', 'automotive', 'business', 0, NULL, 'directions_car', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/automotive.jpg', NULL, 6, 1, 0, '2025-12-12 16:30:44', '2025-12-12 16:37:51'),
(150092, NULL, 'Finance & Banking', 'वित्त आणि बँकिंग', 'finance-banking', 'business', 0, NULL, 'account_balance', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/finance-banking.jpg', NULL, 7, 1, 0, '2025-12-12 16:30:44', '2025-12-12 16:37:51'),
(150093, NULL, 'Hotels & Travel', 'हॉटेल्स आणि प्रवास', 'hotels-travel', 'business', 0, NULL, 'hotel', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/hotels-travel.jpg', NULL, 8, 1, 0, '2025-12-12 16:30:44', '2025-12-12 16:37:51'),
(150094, NULL, 'Agriculture & Farming', 'कृषी आणि शेती', 'agri-farming', 'business', 0, NULL, 'agriculture', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/agri-farming.jpg', NULL, 9, 1, 0, '2025-12-12 16:30:44', '2025-12-12 16:37:51'),
(150095, NULL, 'Professional Services', 'व्यावसायिक सेवा', 'professional-services-biz', 'business', 0, NULL, 'business_center', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/professional-services-biz.jpg', NULL, 10, 1, 0, '2025-12-12 16:30:44', '2025-12-12 16:37:51'),
(150096, NULL, 'Home & Living', 'घर आणि राहणीमान', 'home-living', 'business', 0, NULL, 'home', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/home-living.jpg', NULL, 11, 1, 0, '2025-12-12 16:30:44', '2025-12-12 16:37:52'),
(150097, NULL, 'Government Offices', 'सरकारी कार्यालये', 'government-offices', 'business', 0, NULL, 'account_balance', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/government-offices.jpg', NULL, 12, 1, 0, '2025-12-12 16:30:44', '2025-12-12 16:37:52');
INSERT INTO `categories` (`category_id`, `parent_id`, `name`, `name_mr`, `slug`, `listing_type`, `depth`, `path`, `icon_url`, `image_url`, `description`, `sort_order`, `is_active`, `listing_count`, `created_at`, `updated_at`) VALUES
(150098, 150086, 'Restaurants', 'रेस्टॉरंट', 'restaurants', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/restaurants.jpg', NULL, 1, 1, 1, '2025-12-12 16:31:00', '2025-12-21 17:42:45'),
(150099, 150086, 'Hotels/Dhabas', 'हॉटेल/ढाबा', 'hotels-dhabas', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/hotels-dhabas.jpg', NULL, 2, 1, 0, '2025-12-12 16:31:00', '2025-12-12 16:37:52'),
(150100, 150086, 'Sweet Shops', 'मिठाईची दुकाने', 'sweet-shops', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/sweet-shops.jpg', NULL, 3, 1, 0, '2025-12-12 16:31:00', '2025-12-12 16:37:52'),
(150101, 150086, 'Bakery', 'बेकरी', 'bakery', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/bakery.jpg', NULL, 4, 1, 1, '2025-12-12 16:31:00', '2025-12-21 17:42:45'),
(150102, 150086, 'Tea/Coffee Shops', 'चहा/कॉफी शॉप', 'tea-coffee-shops', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/tea-coffee-shops.jpg', NULL, 5, 1, 0, '2025-12-12 16:31:00', '2025-12-12 16:37:53'),
(150103, 150086, 'Fast Food', 'फास्ट फूड', 'fast-food', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/fast-food.jpg', NULL, 6, 1, 0, '2025-12-12 16:31:00', '2025-12-12 16:37:53'),
(150104, 150086, 'Ice Cream Parlour', 'आईस्क्रीम पार्लर', 'ice-cream-parlour', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/ice-cream-parlour.jpg', NULL, 7, 1, 0, '2025-12-12 16:31:00', '2025-12-12 16:37:53'),
(150105, 150086, 'Juice & Shakes', 'ज्यूस आणि शेक्स', 'juice-shakes', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/juice-shakes.jpg', NULL, 8, 1, 0, '2025-12-12 16:31:00', '2025-12-12 16:37:53'),
(150106, 150087, 'Kirana/General Store', 'किराणा दुकान', 'kirana-store', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/kirana-store.jpg', NULL, 1, 1, 1, '2025-12-12 16:31:02', '2025-12-21 17:42:45'),
(150107, 150087, 'Vegetable Shop', 'भाजी दुकान', 'vegetable-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/vegetable-shop.jpg', NULL, 2, 1, 0, '2025-12-12 16:31:02', '2025-12-12 16:37:54'),
(150108, 150087, 'Fruit Shop', 'फळ दुकान', 'fruit-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/fruit-shop.jpg', NULL, 3, 1, 0, '2025-12-12 16:31:02', '2025-12-12 16:37:54'),
(150109, 150087, 'Dairy & Milk', 'दुग्धालय', 'dairy-milk', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/dairy-milk.jpg', NULL, 4, 1, 0, '2025-12-12 16:31:02', '2025-12-12 16:37:55'),
(150110, 150087, 'Meat & Fish Shop', 'मांस आणि मासे', 'meat-fish-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/meat-fish-shop.jpg', NULL, 5, 1, 0, '2025-12-12 16:31:02', '2025-12-12 16:37:55'),
(150111, 150087, 'Medical Store/Pharmacy', 'मेडिकल स्टोअर', 'medical-store', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/medical-store.jpg', NULL, 6, 1, 1, '2025-12-12 16:31:02', '2025-12-21 17:42:45'),
(150112, 150088, 'Clothing Store', 'कपड्यांचे दुकान', 'clothing-store', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/clothing-store.jpg', NULL, 1, 1, 0, '2025-12-12 16:31:03', '2025-12-12 16:37:55'),
(150113, 150088, 'Footwear Shop', 'पादत्राणे दुकान', 'footwear-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/footwear-shop.jpg', NULL, 2, 1, 0, '2025-12-12 16:31:03', '2025-12-12 16:37:55'),
(150114, 150088, 'Jewellery Shop', 'दागिन्यांचे दुकान', 'jewellery-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/jewellery-shop.jpg', NULL, 3, 1, 0, '2025-12-12 16:31:03', '2025-12-12 16:37:55'),
(150115, 150088, 'Electronics Shop', 'इलेक्ट्रॉनिक्स दुकान', 'electronics-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/electronics-shop.jpg', NULL, 4, 1, 1, '2025-12-12 16:31:03', '2025-12-21 17:42:45'),
(150116, 150088, 'Mobile Shop', 'मोबाईल दुकान', 'mobile-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/mobile-shop.jpg', NULL, 5, 1, 1, '2025-12-12 16:31:03', '2025-12-21 17:42:45'),
(150117, 150088, 'Hardware Store', 'हार्डवेअर दुकान', 'hardware-store', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/hardware-store.jpg', NULL, 6, 1, 0, '2025-12-12 16:31:03', '2025-12-12 16:37:56'),
(150118, 150088, 'Stationery Shop', 'स्टेशनरी दुकान', 'stationery-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/stationery-shop.jpg', NULL, 7, 1, 0, '2025-12-12 16:31:03', '2025-12-12 16:37:56'),
(150119, 150088, 'Gift Shop', 'गिफ्ट शॉप', 'gift-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/gift-shop.jpg', NULL, 8, 1, 0, '2025-12-12 16:31:03', '2025-12-12 16:37:56'),
(150120, 150088, 'Cosmetics Shop', 'कॉस्मेटिक्स दुकान', 'cosmetics-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/cosmetics-shop.jpg', NULL, 9, 1, 0, '2025-12-12 16:31:03', '2025-12-12 16:37:56'),
(150121, 150089, 'Hospital', 'रुग्णालय', 'hospital', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/hospital.jpg', NULL, 1, 1, 0, '2025-12-12 16:31:04', '2025-12-12 16:37:56'),
(150122, 150089, 'Clinic', 'दवाखाना', 'clinic', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/clinic.jpg', NULL, 2, 1, 1, '2025-12-12 16:31:04', '2025-12-21 17:42:45'),
(150123, 150089, 'Diagnostic Center', 'डायग्नोस्टिक सेंटर', 'diagnostic-center', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/diagnostic-center.jpg', NULL, 3, 1, 0, '2025-12-12 16:31:04', '2025-12-12 16:37:57'),
(150124, 150089, 'Dental Clinic', 'दंत दवाखाना', 'dental-clinic', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/dental-clinic.jpg', NULL, 4, 1, 0, '2025-12-12 16:31:04', '2025-12-12 16:37:57'),
(150125, 150089, 'Eye Hospital', 'नेत्र रुग्णालय', 'eye-hospital', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/eye-hospital.jpg', NULL, 5, 1, 0, '2025-12-12 16:31:04', '2025-12-12 16:37:57'),
(150126, 150089, 'Veterinary Clinic', 'पशुवैद्यकीय दवाखाना', 'veterinary-clinic', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/veterinary-clinic.jpg', NULL, 6, 1, 0, '2025-12-12 16:31:04', '2025-12-12 16:37:57'),
(150127, 150089, 'Ayurvedic Clinic', 'आयुर्वेदिक दवाखाना', 'ayurvedic-clinic', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/ayurvedic-clinic.jpg', NULL, 7, 1, 0, '2025-12-12 16:31:04', '2025-12-12 16:37:57'),
(150128, 150090, 'Schools', 'शाळा', 'schools', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/schools.jpg', NULL, 1, 1, 0, '2025-12-12 16:31:06', '2025-12-12 16:37:57'),
(150129, 150090, 'Colleges', 'महाविद्यालय', 'colleges', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/colleges.jpg', NULL, 2, 1, 0, '2025-12-12 16:31:06', '2025-12-12 16:37:58'),
(150130, 150090, 'Coaching Centers', 'कोचिंग सेंटर', 'coaching-centers', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/coaching-centers.jpg', NULL, 3, 1, 0, '2025-12-12 16:31:06', '2025-12-12 16:37:58'),
(150131, 150090, 'Computer Institute', 'कॉम्प्युटर संस्था', 'computer-institute', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/computer-institute.jpg', NULL, 4, 1, 0, '2025-12-12 16:31:06', '2025-12-12 16:37:58'),
(150132, 150090, 'Driving School', 'ड्रायव्हिंग स्कूल', 'driving-school', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/driving-school.jpg', NULL, 5, 1, 0, '2025-12-12 16:31:06', '2025-12-12 16:37:58'),
(150133, 150090, 'Library', 'ग्रंथालय', 'library', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/library.jpg', NULL, 6, 1, 0, '2025-12-12 16:31:06', '2025-12-12 16:37:58'),
(150134, 150090, 'Play School/Creche', 'प्ले स्कूल', 'play-school', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/play-school.jpg', NULL, 7, 1, 0, '2025-12-12 16:31:06', '2025-12-12 16:37:58'),
(150135, 150091, 'Car Showroom', 'कार शोरूम', 'car-showroom', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/car-showroom.jpg', NULL, 1, 1, 0, '2025-12-12 16:31:07', '2025-12-12 16:37:58'),
(150136, 150091, 'Bike Showroom', 'बाईक शोरूम', 'bike-showroom', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/bike-showroom.jpg', NULL, 2, 1, 0, '2025-12-12 16:31:07', '2025-12-12 16:37:58'),
(150137, 150091, 'Auto Parts Shop', 'ऑटो पार्ट्स दुकान', 'auto-parts-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/auto-parts-shop.jpg', NULL, 3, 1, 0, '2025-12-12 16:31:07', '2025-12-12 16:37:59'),
(150138, 150091, 'Petrol Pump', 'पेट्रोल पंप', 'petrol-pump', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/petrol-pump.jpg', NULL, 4, 1, 0, '2025-12-12 16:31:07', '2025-12-12 16:37:59'),
(150139, 150091, 'Car Service Center', 'कार सर्व्हिस सेंटर', 'car-service-center', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/car-service-center.jpg', NULL, 5, 1, 0, '2025-12-12 16:31:07', '2025-12-12 16:37:59'),
(150140, 150091, 'Tyre Shop', 'टायर दुकान', 'tyre-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/tyre-shop.jpg', NULL, 6, 1, 0, '2025-12-12 16:31:07', '2025-12-12 16:37:59'),
(150141, 150091, 'Garage', 'गॅरेज', 'garage', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/garage.jpg', NULL, 7, 1, 0, '2025-12-12 16:31:07', '2025-12-12 16:37:59'),
(150142, 150092, 'Banks', 'बँक', 'banks', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/banks.jpg', NULL, 1, 1, 0, '2025-12-12 16:31:08', '2025-12-12 16:38:00'),
(150143, 150092, 'ATM', 'एटीएम', 'atm', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/atm.jpg', NULL, 2, 1, 0, '2025-12-12 16:31:08', '2025-12-12 16:38:00'),
(150144, 150092, 'Insurance Office', 'विमा कार्यालय', 'insurance-office', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/insurance-office.jpg', NULL, 3, 1, 0, '2025-12-12 16:31:08', '2025-12-12 16:38:00'),
(150145, 150092, 'Finance Company', 'फायनान्स कंपनी', 'finance-company', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/finance-company.jpg', NULL, 4, 1, 0, '2025-12-12 16:31:08', '2025-12-12 16:38:00'),
(150146, 150092, 'CA/Tax Office', 'सीए/टॅक्स कार्यालय', 'ca-tax-office', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/ca-tax-office.jpg', NULL, 5, 1, 0, '2025-12-12 16:31:08', '2025-12-12 16:38:00'),
(150147, 150092, 'Microfinance', 'मायक्रोफायनान्स', 'microfinance', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/microfinance.jpg', NULL, 6, 1, 0, '2025-12-12 16:31:08', '2025-12-12 16:38:01'),
(150148, 150093, 'Lodges/Hotels', 'लॉज/हॉटेल', 'lodges-hotels', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/lodges-hotels.jpg', NULL, 1, 1, 0, '2025-12-12 16:31:09', '2025-12-12 16:38:01'),
(150149, 150093, 'Travel Agency', 'ट्रॅव्हल एजन्सी', 'travel-agency', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/travel-agency.jpg', NULL, 2, 1, 0, '2025-12-12 16:31:09', '2025-12-12 16:38:01'),
(150150, 150093, 'Bus Booking', 'बस बुकिंग', 'bus-booking', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/bus-booking.jpg', NULL, 3, 1, 0, '2025-12-12 16:31:09', '2025-12-12 16:38:01'),
(150151, 150093, 'Tour Operator', 'टूर ऑपरेटर', 'tour-operator', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/tour-operator.jpg', NULL, 4, 1, 0, '2025-12-12 16:31:09', '2025-12-12 16:38:01'),
(150152, 150093, 'Cab Services', 'कॅब सेवा', 'cab-services', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/cab-services.jpg', NULL, 5, 1, 0, '2025-12-12 16:31:09', '2025-12-12 16:38:01'),
(150153, 150094, 'Seed Shop', 'बियाणे दुकान', 'seed-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/seed-shop.jpg', NULL, 1, 1, 0, '2025-12-12 16:31:10', '2025-12-12 16:38:01'),
(150154, 150094, 'Fertilizer Shop', 'खते दुकान', 'fertilizer-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/fertilizer-shop.jpg', NULL, 2, 1, 0, '2025-12-12 16:31:10', '2025-12-12 16:38:01'),
(150155, 150094, 'Pesticide Shop', 'कीटकनाशक दुकान', 'pesticide-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/pesticide-shop.jpg', NULL, 3, 1, 0, '2025-12-12 16:31:10', '2025-12-12 16:38:02'),
(150156, 150094, 'Tractor Dealer', 'ट्रॅक्टर डीलर', 'tractor-dealer', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/tractor-dealer.jpg', NULL, 4, 1, 0, '2025-12-12 16:31:10', '2025-12-12 16:38:02'),
(150157, 150094, 'Farm Equipment Shop', 'शेती उपकरणे दुकान', 'farm-equipment-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/farm-equipment-shop.jpg', NULL, 5, 1, 0, '2025-12-12 16:31:10', '2025-12-12 16:38:02'),
(150158, 150094, 'Grain Market/Mandi', 'धान्य बाजार/मंडी', 'grain-market', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/grain-market.jpg', NULL, 6, 1, 0, '2025-12-12 16:31:10', '2025-12-12 16:38:02'),
(150159, 150095, 'Lawyer Office', 'वकील कार्यालय', 'lawyer-office', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/lawyer-office.jpg', NULL, 1, 1, 0, '2025-12-12 16:31:11', '2025-12-12 16:38:02'),
(150160, 150095, 'Architect', 'आर्किटेक्ट', 'architect', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/architect.jpg', NULL, 2, 1, 0, '2025-12-12 16:31:11', '2025-12-12 16:38:02'),
(150161, 150095, 'Real Estate Office', 'रिअल इस्टेट कार्यालय', 'real-estate-office', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/real-estate-office.jpg', NULL, 3, 1, 0, '2025-12-12 16:31:11', '2025-12-12 16:38:03'),
(150162, 150095, 'Photo Studio', 'फोटो स्टुडिओ', 'photo-studio', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/photo-studio.jpg', NULL, 4, 1, 0, '2025-12-12 16:31:11', '2025-12-12 16:38:03'),
(150163, 150095, 'Printing Press', 'प्रिंटिंग प्रेस', 'printing-press', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/printing-press.jpg', NULL, 5, 1, 0, '2025-12-12 16:31:11', '2025-12-12 16:38:04'),
(150164, 150095, 'Xerox/Cyber Cafe', 'झेरॉक्स/सायबर कॅफे', 'xerox-cyber-cafe', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/xerox-cyber-cafe.jpg', NULL, 6, 1, 0, '2025-12-12 16:31:11', '2025-12-12 16:38:04'),
(150165, 150096, 'Furniture Shop', 'फर्निचर दुकान', 'furniture-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/furniture-shop.jpg', NULL, 1, 1, 0, '2025-12-12 16:31:12', '2025-12-12 16:38:04'),
(150166, 150096, 'Electrical Shop', 'इलेक्ट्रिकल दुकान', 'electrical-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/electrical-shop.jpg', NULL, 2, 1, 0, '2025-12-12 16:31:12', '2025-12-12 16:38:04'),
(150167, 150096, 'Sanitary Ware', 'सॅनिटरी वेअर', 'sanitary-ware', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/sanitary-ware.jpg', NULL, 3, 1, 0, '2025-12-12 16:31:12', '2025-12-12 16:38:04'),
(150168, 150096, 'Paint Shop', 'पेंट दुकान', 'paint-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/paint-shop.jpg', NULL, 4, 1, 0, '2025-12-12 16:31:12', '2025-12-12 16:38:04'),
(150169, 150096, 'Building Materials', 'बांधकाम साहित्य', 'building-materials', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/building-materials.jpg', NULL, 5, 1, 0, '2025-12-12 16:31:12', '2025-12-12 16:38:04'),
(150170, 150096, 'Tiles Shop', 'टाइल्स दुकान', 'tiles-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/tiles-shop.jpg', NULL, 6, 1, 0, '2025-12-12 16:31:12', '2025-12-12 16:38:05'),
(150171, 150096, 'Steel/Cement Shop', 'स्टील/सिमेंट दुकान', 'steel-cement-shop', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/steel-cement-shop.jpg', NULL, 7, 1, 0, '2025-12-12 16:31:12', '2025-12-12 16:38:05'),
(150172, 150097, 'Tahsil Office', 'तहसील कार्यालय', 'tahsil-office', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/tahsil-office.jpg', NULL, 1, 1, 0, '2025-12-12 16:31:13', '2025-12-12 16:38:05'),
(150173, 150097, 'Police Station', 'पोलीस स्टेशन', 'police-station', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/police-station.jpg', NULL, 2, 1, 0, '2025-12-12 16:31:13', '2025-12-12 16:38:05'),
(150174, 150097, 'Post Office', 'पोस्ट ऑफिस', 'post-office', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/post-office.jpg', NULL, 3, 1, 0, '2025-12-12 16:31:13', '2025-12-12 16:38:06'),
(150175, 150097, 'Electricity Office', 'वीज कार्यालय', 'electricity-office', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/electricity-office.jpg', NULL, 4, 1, 0, '2025-12-12 16:31:13', '2025-12-12 16:38:06'),
(150176, 150097, 'RTO Office', 'आरटीओ कार्यालय', 'rto-office', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/rto-office.jpg', NULL, 5, 1, 0, '2025-12-12 16:31:13', '2025-12-12 16:38:06'),
(150177, 150097, 'Court', 'न्यायालय', 'court', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/court.jpg', NULL, 6, 1, 0, '2025-12-12 16:31:13', '2025-12-12 16:38:06'),
(150178, 150097, 'Gram Panchayat', 'ग्राम पंचायत', 'gram-panchayat', 'business', 1, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/business_category_images/gram-panchayat.jpg', NULL, 7, 1, 0, '2025-12-12 16:31:13', '2025-12-12 16:38:06'),
(210001, NULL, 'Construction & Labour', 'बांधकाम व मजूर', 'jobs-construction-labour', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1504307651254-35680f356dfd?w=800&q=80', NULL, 1, 1, 1, '2025-12-13 08:00:21', '2025-12-29 11:55:40'),
(210002, NULL, 'Driver & Transport', 'ड्रायव्हर व वाहतूक', 'jobs-driver-transport', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=800&q=80', NULL, 2, 1, 1, '2025-12-13 08:00:21', '2025-12-21 17:42:45'),
(210003, NULL, 'Domestic Help', 'घरगुती कामगार', 'jobs-domestic-help', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=800&q=80', NULL, 3, 1, 0, '2025-12-13 08:00:21', '2025-12-13 08:00:21'),
(210004, NULL, 'Sales & Marketing', 'विक्री व मार्केटिंग', 'jobs-sales-marketing', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1552664730-d307ca884978?w=800&q=80', NULL, 4, 1, 1, '2025-12-13 08:00:21', '2025-12-21 17:42:45'),
(210005, NULL, 'Office & Admin', 'ऑफिस व प्रशासन', 'jobs-office-admin', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1497366216548-37526070297c?w=800&q=80', NULL, 5, 1, 1, '2025-12-13 08:00:21', '2025-12-21 17:42:45'),
(210006, NULL, 'Retail & Shop', 'दुकान', 'jobs-retail-shop', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1604719312566-8912e9227c6a?w=800&q=80', NULL, 6, 1, 0, '2025-12-13 08:00:21', '2025-12-13 08:00:21'),
(210007, NULL, 'Factory & Manufacturing', 'कारखाना', 'jobs-factory-manufacturing', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1565688534245-05d6b5be184a?w=800&q=80', NULL, 7, 1, 0, '2025-12-13 08:00:21', '2025-12-13 08:00:21'),
(210008, NULL, 'Hotel & Restaurant', 'हॉटेल व रेस्टॉरंट', 'jobs-hotel-restaurant', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&q=80', NULL, 8, 1, 0, '2025-12-13 08:00:21', '2025-12-13 08:00:21'),
(210009, NULL, 'Healthcare', 'आरोग्य सेवा', 'jobs-healthcare', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800&q=80', NULL, 9, 1, 0, '2025-12-13 08:00:21', '2025-12-13 08:00:21'),
(210010, NULL, 'Education & Tuition', 'शिक्षण', 'jobs-education-tuition', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=800&q=80', NULL, 10, 1, 1, '2025-12-13 08:00:21', '2025-12-21 17:42:45'),
(210011, NULL, 'Beauty & Salon', 'ब्युटी व सलून', 'jobs-beauty-salon', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1560066984-138dadb4c035?w=800&q=80', NULL, 11, 1, 0, '2025-12-13 08:00:21', '2025-12-13 08:00:21'),
(210012, NULL, 'Agriculture & Farming', 'शेती व कृषी', 'jobs-agriculture-farming', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1625246333195-78d9c38ad449?w=800&q=80', NULL, 12, 1, 0, '2025-12-13 08:00:21', '2025-12-13 08:00:21'),
(210013, NULL, 'Security', 'सुरक्षा', 'jobs-security', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1555817128-342e1c8b3f24?w=800&q=80', NULL, 13, 1, 1, '2025-12-13 08:00:21', '2025-12-21 17:42:45'),
(210014, NULL, 'IT & Computer', 'आयटी व कॉम्प्युटर', 'jobs-it-computer', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800&q=80', NULL, 14, 1, 0, '2025-12-13 08:00:21', '2025-12-13 08:00:21'),
(210015, NULL, 'Government Jobs', 'सरकारी नोकरी', 'jobs-government', 'jobs', 0, NULL, NULL, 'https://images.unsplash.com/photo-1523292562811-8fa7962a78c8?w=800&q=80', NULL, 15, 1, 0, '2025-12-13 08:00:21', '2025-12-13 08:00:21');

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

--
-- Dumping data for table `cities`
--

INSERT INTO `cities` (`city_id`, `state_id`, `name`, `name_mr`, `slug`, `latitude`, `longitude`, `is_popular`, `listing_count`, `sort_order`, `is_active`, `created_at`) VALUES
(1, 1, 'Hingoli', 'हिंगोली', 'hingoli', 19.71780000, 77.14670000, 1, 0, 0, 1, '2025-12-21 14:53:08'),
(2, 1, 'Washim', 'वाशीम', 'washim', 19.45845000, 77.78384000, 1, 0, 1, 1, '2025-12-21 14:53:08'),
(3, 1, 'Parbhani', 'परभणी', 'parbhani', NULL, NULL, 1, 0, 0, 1, '2025-12-21 14:53:08'),
(4, 1, 'Akola', 'अकोला', 'akola', NULL, NULL, 1, 0, 0, 1, '2025-12-21 14:53:08'),
(5, 1, 'Nanded', 'नांदेड', 'nanded', NULL, NULL, 1, 0, 0, 1, '2025-12-21 14:53:08');

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

--
-- Dumping data for table `delivery_users`
--

INSERT INTO `delivery_users` (`delivery_user_id`, `phone`, `name`, `vehicle_type`, `vehicle_number`, `status`, `fcm_token`, `total_deliveries`, `total_earnings`, `created_at`, `updated_at`, `email`, `address`, `upi_id`) VALUES
(1, '9595340263', 'om delivery boy', 'bike', NULL, 'active', NULL, 6, 7636.50, '2025-12-27 07:15:08', '2025-12-29 07:34:53', 'omwaman1@gmail.com', 'malselu', '9595340263@ybl');

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

--
-- Dumping data for table `enquiries`
--

INSERT INTO `enquiries` (`enquiry_id`, `listing_id`, `user_id`, `name`, `phone`, `email`, `message`, `enquiry_type`, `status`, `admin_notes`, `created_at`) VALUES
(1, 270102, 1, 'Test User', '9999999999', NULL, 'Test enquiry from debug script', 'chat', 'new', NULL, '2025-12-17 03:41:28'),
(30001, 270102, 1, 'Test User', '9999999999', NULL, 'Test enquiry from debug script', 'chat', 'new', NULL, '2025-12-17 03:51:37'),
(30002, 270102, 1, 'Test User', '9999999999', NULL, 'Test enquiry from debug script', 'chat', 'new', NULL, '2025-12-17 03:51:52'),
(30003, 270102, 1, 'Test User', '9999999999', NULL, 'Test enquiry from debug script', 'chat', 'new', NULL, '2025-12-17 03:53:47'),
(30004, 270102, 1, 'Test User', '9999999999', NULL, 'Test enquiry from debug script', 'chat', 'new', NULL, '2025-12-17 03:53:53'),
(30005, 270102, 1, 'Test User', '9999999999', NULL, 'Test enquiry from debug script', 'chat', 'new', NULL, '2025-12-17 03:53:58'),
(30006, 270102, 1, 'Test User', '9999999999', NULL, 'Test enquiry from debug script', 'chat', 'new', NULL, '2025-12-17 03:55:45'),
(30007, 270102, 1, 'Test User', '9999999999', NULL, 'Test enquiry from debug script', 'chat', 'new', NULL, '2025-12-17 03:56:56'),
(30008, 270102, 1, 'Test User', '9999999999', NULL, 'Test enquiry from debug script', 'chat', 'new', NULL, '2025-12-17 03:59:03'),
(30009, 270102, NULL, 'Guest', '', NULL, NULL, 'call', 'new', NULL, '2025-12-17 03:59:04'),
(60001, 270102, 60002, 'ok', '9595340263', NULL, NULL, 'chat', 'resolved', NULL, '2025-12-17 05:04:13'),
(90001, 150102, 450002, 'Support', '9096632830', NULL, NULL, 'chat', 'new', NULL, '2025-12-21 07:01:07'),
(120001, 120102, 60002, 'ok', '9595340263', NULL, NULL, 'call', 'new', NULL, '2025-12-21 09:18:34'),
(150001, 120102, 60002, 'ok', '9595340263', NULL, NULL, 'call', 'new', NULL, '2025-12-21 09:28:56'),
(150002, 120102, 60002, 'ok', '9595340263', NULL, NULL, 'chat', 'new', NULL, '2025-12-21 09:29:00'),
(180001, 1190678, 540002, 'Manohar waman', '7559262920', NULL, NULL, 'call', 'new', NULL, '2025-12-21 13:21:47'),
(210001, 1190678, 540002, 'Manohar waman', '7559262920', NULL, NULL, 'call', 'new', NULL, '2025-12-21 13:37:34'),
(210002, 1190678, 540002, 'Manohar waman', '7559262920', NULL, NULL, 'chat', 'new', NULL, '2025-12-21 13:38:43'),
(240001, 150102, 480002, '70', '7020034431', NULL, NULL, 'call', 'new', NULL, '2025-12-21 14:11:38'),
(270001, 150102, 480002, '70', '7020034431', NULL, NULL, 'call', 'new', NULL, '2025-12-21 14:23:08'),
(270002, 150102, 480002, '70', '7020034431', NULL, NULL, 'chat', 'new', NULL, '2025-12-21 14:23:27'),
(300001, 8, 9595340263, 'Hingoli Demo User 1', '9595340263', NULL, NULL, 'call', 'new', NULL, '2025-12-22 12:50:18'),
(300002, 8, 9595340263, 'Hingoli Demo User 1', '9595340263', NULL, NULL, 'chat', 'new', NULL, '2025-12-22 12:50:30'),
(330001, 12, 9595340263, 'Hingoli Demo User 1', '9595340263', NULL, NULL, 'call', 'new', NULL, '2025-12-24 05:01:31'),
(360001, 16, 9595340263, 'Hingoli Demo User 1', '9595340263', NULL, NULL, 'call', 'new', NULL, '2025-12-24 05:36:26'),
(390001, 7, 7020034431, 'Hingoli Demo User 2', '7020034431', NULL, NULL, 'call', 'new', NULL, '2025-12-26 13:29:40'),
(390002, 1, 7020034431, 'Hingoli Demo User 2', '7020034431', NULL, NULL, 'call', 'new', NULL, '2025-12-26 13:30:00'),
(420001, 1, 7709082672, 'vishal Karhale', '7709082672', NULL, NULL, 'chat', 'new', NULL, '2025-12-29 04:30:30'),
(450001, 11, 210002, 'Hingoli Hub Tester', '8788428166', NULL, NULL, 'chat', 'new', NULL, '2025-12-29 07:12:30'),
(450002, 11, 210002, 'Hingoli Hub Tester', '8788428166', NULL, NULL, 'chat', 'new', NULL, '2025-12-29 07:18:52'),
(480001, 27, 210002, 'Hingoli Hub Tester', '8788428166', NULL, NULL, 'chat', 'new', NULL, '2025-12-29 12:05:04'),
(510001, 25, 9595340263, 'Hingoli Demo User 1', '9595340263', NULL, NULL, 'call', 'new', NULL, '2025-12-29 16:06:22'),
(510002, 27, 8669702031, 'jio', '8669702031', NULL, NULL, 'call', 'new', NULL, '2025-12-29 16:09:37'),
(540001, 27, 8669702031, 'jio', '8669702031', NULL, NULL, 'call', 'new', NULL, '2025-12-29 16:25:49'),
(570001, 27, 8669702031, 'jio', '8669702031', NULL, NULL, 'call', 'new', NULL, '2025-12-29 16:50:41'),
(600001, 14, 9595340263, 'Hingoli Demo User 1', '9595340263', NULL, NULL, 'call', 'new', NULL, '2025-12-29 18:10:46');

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

--
-- Dumping data for table `gateway_devices`
--

INSERT INTO `gateway_devices` (`id`, `device_id`, `device_name`, `fcm_token`, `sim1_phone`, `sim2_phone`, `active_sim`, `sms_template`, `status`, `last_active_at`, `created_at`) VALUES
(1, 'device_72b21823', 'samsung SM-M346B', 'dFT7hvKmTCKhVdHmBnwzRd:APA91bEOqMKP51fRTY9TT9Aose-oLYzhYKgRKtHGsSEebJzib4MubW-E-lHDt-8EOcAPIhXRNaK0L5N4qZ1HfLj9nA9cYOwQU3uBbm0Oplr2IZZq4h2X08c', NULL, NULL, 1, 'Your Hello Hingoli OTP is: {otp}. Valid for 5 min. Do not share. TQp93m8T4ZW', 'online', '2025-12-12 17:48:36', '2025-12-12 17:45:02'),
(2, 'device_787172d1', 'samsung SM-M346B', 'cROttJjET0u_EiXEfqYxVb:APA91bH90xYWw76N94dLLSQ67iP0vK3l5GgJzHF22QQGYwA_M8PS34oiixHpIMi21pHL61uvCEkd6Br6jNbZQUlZlyY722e-ylxbWuDL-mIjG-GgKdFQQsE', NULL, NULL, 1, 'Your Hello Hingoli OTP is: {otp}. Valid for 5 min. Do not share.', 'online', '2025-12-12 17:52:52', '2025-12-12 17:52:52'),
(30001, 'device_189fcfbb', 'samsung SM-M346B', 'fgAkFstMQGeyywJwJ63OQ0:APA91bFet05gU6QIRo2P-KX_3M3QrERXEj6NneyPGyklJwscNRGAaO-y_NpDtGFt77V9-_mpNOapl9y1h5h7OJHyQX4FUJ5Sl75zcwI7y2v9_kjrRWTxSJ8', NULL, NULL, 1, 'Your Hello Hingoli OTP is: {otp}. Valid for 5 min. Do not share.', 'online', '2025-12-14 02:58:51', '2025-12-14 02:58:51'),
(60001, 'device_b65ade7c', 'samsung SM-M346B', 'erpF-DM5RxiTT-apSsVNAg:APA91bHbYIAtaT_dcK9MZKyHhG3RDsU24jKgfNnT-LG-R9cSPLJgUqIIYAQHN36tjl7niaGt90dG658zQ7_BAmoYbUvr6LAVMF0gmjzHrRgZKftENY6Tkzc', NULL, NULL, 1, 'Your Hello Hingoli OTP is: {otp}. Valid for 5 min. Do not share.', 'online', '2025-12-14 16:25:17', '2025-12-14 16:25:17'),
(90001, 'device_784cdb1f', 'samsung SM-M346B', 'eeb0lBgJT1Sjzeo9hzFjqb:APA91bG65cKQCXkjYakeqwkAT2rJwN_S0CcOs5Cg64dLyUUWVgggDAF7m3RDzl0cOZWYBYesDNX1LjK849wPFanfVOQWaOeRZwQ2yfejAAmi9KijtG1qlsI', NULL, NULL, 1, 'Your Hello Hingoli OTP is: {otp}. Valid for 5 min. Do not share.', 'online', '2025-12-25 17:47:56', '2025-12-25 17:47:56');

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

--
-- Dumping data for table `job_listings`
--

INSERT INTO `job_listings` (`listing_id`, `job_title`, `employment_type`, `salary_min`, `salary_max`, `salary_period`, `experience_required_years`, `education_required`, `remote_option`, `vacancies`, `application_deadline`, `required_skills`, `benefits`, `custom_attributes`) VALUES
(18, 'Accountant', 'full_time', 15000.00, 25000.00, 'monthly', 1, 'B.Com', 'on_site', 2, NULL, NULL, NULL, NULL),
(19, 'Sales Executive', 'full_time', 12000.00, 18000.00, 'monthly', 0, '12th Pass', 'on_site', 5, NULL, NULL, NULL, NULL),
(20, 'English Teacher', 'full_time', 18000.00, 30000.00, 'monthly', 2, 'B.Ed', 'on_site', 1, NULL, NULL, NULL, NULL),
(21, 'Delivery Driver', 'part_time', 8000.00, 15000.00, 'monthly', 0, '10th Pass', 'on_site', 10, NULL, NULL, NULL, NULL),
(22, 'Security Guard', 'full_time', 10000.00, 12000.00, 'monthly', 0, '8th Pass', 'on_site', 4, NULL, NULL, NULL, NULL),
(28, 'labour', 'full_time', NULL, NULL, 'monthly', 0, NULL, 'on_site', 1, NULL, NULL, NULL, NULL);

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

--
-- Dumping data for table `listings`
--

INSERT INTO `listings` (`listing_id`, `listing_type`, `title`, `description`, `category_id`, `subcategory_id`, `location`, `city`, `state`, `country`, `postal_code`, `latitude`, `longitude`, `main_image_url`, `user_id`, `status`, `is_verified`, `is_featured`, `verified_at`, `verified_by`, `rejection_reason`, `view_count`, `review_count`, `avg_rating`, `created_at`, `updated_at`, `expires_at`) VALUES
(1, 'services', 'Ramesh Electric Works', 'Professional electrical services in Hingoli. House wiring, fan installation, AC repair, and all electrical work. 15+ years experience. Available 24/7 for emergency repairs. Quality work guaranteed.', 120001, 120013, 'Near Bus Stand, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=600', 9595340263, 'active', 1, 1, NULL, NULL, NULL, 308, 2, 4.5, '2025-12-21 17:42:39', '2025-12-29 18:35:57', NULL),
(2, 'services', 'Shivaji Plumbing Services', 'Expert plumbing solutions for homes and businesses. Pipe fitting, bathroom installation, water tank repair, drainage cleaning. Quality work at affordable prices. Serving Hingoli for 12 years.', 120001, 120014, 'Nanded Road, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1607472586893-edb57bdc0e39?w=600', 7020034431, 'active', 1, 0, NULL, NULL, NULL, 191, 0, 4.3, '2025-12-21 17:42:39', '2025-12-22 04:05:22', NULL),
(3, 'services', 'Vishwakarma Furniture Works', 'Custom furniture design and manufacturing. Modular kitchen, wardrobes, beds, sofa sets. Using quality wood and modern designs. Free home visit for measurement.', 120001, 120015, 'Mahavir Chowk, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600', 9595340263, 'active', 1, 1, NULL, NULL, NULL, 315, 1, 4.7, '2025-12-21 17:42:39', '2025-12-27 08:13:53', NULL),
(4, 'services', 'Swachh Home Cleaning Services', 'Professional home cleaning services. Deep cleaning, sofa shampooing, kitchen cleaning, bathroom sanitization. Trained staff with quality cleaning products.', 120001, 120023, 'Basmath Road, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=600', 7020034431, 'active', 1, 0, NULL, NULL, NULL, 186, 0, 4.4, '2025-12-21 17:42:40', '2025-12-29 14:44:11', NULL),
(5, 'services', 'Cool Tech AC Services', 'AC installation, repair, and maintenance. All brands - Split AC, Window AC. Gas filling, compressor repair, deep cleaning. Annual maintenance contracts available.', 120001, 120017, 'Station Road, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=600', 9595340263, 'active', 1, 0, NULL, NULL, NULL, 234, 1, 4.6, '2025-12-21 17:42:40', '2025-12-21 18:00:09', NULL),
(6, 'services', 'Rainbow Painting Services', 'Interior and exterior painting services. Wall textures, POP, waterproofing, wood polish. Using Asian Paints, Berger Paints. Free color consultation.', 120001, 120016, 'Aundha Road, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1562259949-e8e7689d7828?w=600', 7020034431, 'active', 1, 0, NULL, NULL, NULL, 160, 0, 4.2, '2025-12-21 17:42:40', '2025-12-22 07:39:08', NULL),
(7, 'services', 'Lakshmi Bridal Studio', 'Complete bridal makeup and beauty services. HD makeup, airbrush makeup, party makeup. VLCC trained beauticians. Home service available for bridal packages.', 120003, 120036, 'Main Market, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1560066984-138dadb4c035?w=600', 9595340263, 'active', 1, 1, NULL, NULL, NULL, 434, 1, 4.8, '2025-12-21 17:42:40', '2025-12-28 19:25:34', NULL),
(8, 'services', 'Vidya Home Tuitions', 'Experienced teacher for 1st to 10th standard. All subjects including English, Maths, Science. SSC and CBSE board. Individual attention, batch size limited to 5 students.', 120005, 120046, 'Vidya Nagar, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1427504494785-3a9ca7044f45?w=600', 7020034431, 'active', 1, 0, NULL, NULL, NULL, 306, 0, 4.6, '2025-12-21 17:42:41', '2025-12-29 04:27:32', NULL),
(9, 'services', 'Safe Guard Pest Control', 'Complete pest control solutions. Cockroach, termite, bed bugs, rodent control. Odorless and safe chemicals. Annual maintenance contracts for homes and offices.', 120001, 120022, 'Near College, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1632935190605-812de88c7891?w=600', 9595340263, 'active', 1, 0, NULL, NULL, NULL, 154, 0, 4.4, '2025-12-21 17:42:41', '2025-12-28 18:24:20', NULL),
(10, 'services', 'Royal Wedding Photography', 'Professional wedding photography and videography. Candid shots, drone coverage, pre-wedding shoots. Album design included. Serving all of Hingoli district.', 120006, 120053, 'Station Road, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1537633552985-df8429e8048b?w=600', 7020034431, 'active', 1, 1, NULL, NULL, NULL, 375, 1, 4.7, '2025-12-21 17:42:41', '2025-12-29 19:21:06', NULL),
(11, 'business', 'Sharma Electronics', 'Authorized dealer for Samsung, LG, Sony. TVs, refrigerators, washing machines, ACs. EMI available. Installation and after-sales service. Established since 2005.', 150115, NULL, 'Main Market, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=600', 9595340263, 'active', 1, 1, NULL, NULL, NULL, 584, 1, 4.5, '2025-12-21 17:42:41', '2025-12-30 02:52:34', NULL),
(12, 'business', 'Hotel Marathi Tadka', 'Pure vegetarian restaurant serving Maharashtrian cuisine. Misal Pav, Vada Pav, Puran Poli, Thali meals. AC dining hall. Catering services for events.', 150086, 150099, 'Bus Stand Road, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=600', 7020034431, 'active', 1, 1, NULL, NULL, NULL, 841, 1, 4.6, '2025-12-21 17:42:41', '2025-12-29 14:42:35', NULL),
(13, 'business', 'Dr. Patil Health Clinic', 'General physician and family doctor. OPD timings 9 AM to 1 PM and 5 PM to 9 PM. Pathology lab attached. ECG, vaccination available. 20+ years experience.', 150122, NULL, 'Station Road, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1631217868264-e5b90bb7e133?w=600', 9595340263, 'active', 1, 1, NULL, NULL, NULL, 734, 1, 4.8, '2025-12-21 17:42:42', '2025-12-30 03:19:05', NULL),
(14, 'business', 'Patil Kirana & General Store', 'Complete grocery and general store. Daily needs, pulses, spices, oils, snacks, beverages. Home delivery available within Hingoli city. Wholesale rates for bulk orders.', 150087, 150106, 'Gandhi Chowk, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1604719312566-8912e9227c6a?w=600', 7020034431, 'active', 1, 0, NULL, NULL, NULL, 470, 1, 4.4, '2025-12-21 17:42:42', '2025-12-29 18:10:44', NULL),
(15, 'business', 'Digital World Mobile Shop', 'Latest smartphones, accessories, and repairs. Samsung, Vivo, Oppo, Realme, iPhone. Screen replacement, battery change, software update. All mobiles with warranty.', 150116, NULL, 'Main Market, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1556656793-08538906a9f8?w=600', 9595340263, 'active', 1, 0, NULL, NULL, NULL, 402, 0, 4.3, '2025-12-21 17:42:42', '2025-12-26 15:42:34', NULL),
(16, 'business', 'Shree Bakery & Cake Shop', 'Fresh bakery items daily. Birthday cakes, pastries, cookies, bread, biscuits. Custom cake orders for all occasions. Eggless options available.', 150086, 150101, 'Near College, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600', 7020034431, 'active', 1, 0, NULL, NULL, NULL, 308, 0, 4.5, '2025-12-21 17:42:42', '2025-12-29 11:30:42', NULL),
(17, 'business', 'Shivaji Medical Store', 'All medicines available. Open 24 hours. Home delivery for senior citizens. Surgical items, baby products, health supplements. Discount on bulk orders.', 150111, NULL, 'Hospital Road, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1576602976047-174e57a47881?w=600', 9595340263, 'active', 1, 0, NULL, NULL, NULL, 514, 0, 4.6, '2025-12-21 17:42:43', '2025-12-27 06:33:52', NULL),
(18, 'jobs', 'Accountant Required', 'Hiring experienced accountant for Hingoli-based trading firm. Tally knowledge mandatory. GST filing, balance sheet preparation. Freshers with B.Com can also apply.', 210005, NULL, 'MIDC Area, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1554224155-6726b3ff858f?w=600', 7020034431, 'active', 1, 0, NULL, NULL, NULL, 235, 0, 0.0, '2025-12-21 17:42:43', '2025-12-22 09:42:37', NULL),
(19, 'jobs', 'Sales Executive - FMCG', 'Required sales executive for FMCG distribution company. Field work in Hingoli district. Two-wheeler and valid license must. Attractive incentives.', 210004, NULL, 'Nanded Road, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1556740758-90de374c12ad?w=600', 9595340263, 'active', 1, 0, NULL, NULL, NULL, 198, 0, 0.0, '2025-12-21 17:42:43', '2025-12-28 18:22:14', NULL),
(20, 'jobs', 'English Teacher - School', 'English medium school in Hingoli requires English teacher for classes 5-10. B.Ed required. Competitive salary and benefits. Experienced preferred.', 210010, NULL, 'Vidya Nagar, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1509062522246-3755977927d7?w=600', 7020034431, 'active', 1, 0, NULL, NULL, NULL, 318, 0, 0.0, '2025-12-21 17:42:43', '2025-12-29 12:03:12', NULL),
(21, 'jobs', 'Delivery Driver Required', 'E-commerce company hiring delivery drivers in Hingoli. Own two-wheeler required. Fuel allowance and per-parcel incentive. Flexible hours.', 210002, NULL, 'Hingoli City', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=600', 9595340263, 'active', 1, 0, NULL, NULL, NULL, 210, 0, 0.0, '2025-12-21 17:42:44', '2025-12-29 06:17:37', NULL),
(22, 'jobs', 'Security Guard - Factory', 'Security guards required for manufacturing unit. 12-hour shifts. Previous experience preferred. Uniforms provided. PF and ESI benefits.', 210013, NULL, 'MIDC, Hingoli', 'Hingoli', 'Maharashtra', 'India', '431513', NULL, NULL, 'https://images.unsplash.com/photo-1461685265823-f8d5d0b08b9b?w=600', 7020034431, 'active', 1, 0, NULL, NULL, NULL, 184, 0, 0.0, '2025-12-21 17:42:44', '2025-12-30 02:05:39', NULL),
(23, 'business', 'HINGOLI HUB', 'HINGOLI HUB', 150088, 150112, '', 'Hingoli', 'Maharashtra', 'India', NULL, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_694eb33c48b0a_1766765372.webp', 9595340263, 'active', 1, 0, NULL, NULL, NULL, 27, 0, 0.0, '2025-12-26 16:09:37', '2025-12-29 08:42:05', NULL),
(24, 'business', 'शिवरेखा पुस्तकालय 📚', '', 150090, 150130, '', 'Hingoli', 'Maharashtra', 'India', NULL, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_694f7fd905116_1766817753.webp', 390002, 'active', 1, 0, NULL, NULL, NULL, 18, 0, 0.0, '2025-12-27 06:42:37', '2025-12-30 02:51:55', NULL),
(25, 'business', 'Aaroh\'s Shop', 'Second hand books seller shop.', 3, NULL, NULL, 'Hingoli', 'Maharashtra', 'India', NULL, NULL, NULL, NULL, 210002, 'active', 1, 0, NULL, NULL, NULL, 47, 0, 0.0, '2025-12-27 17:07:41', '2025-12-30 02:52:08', NULL),
(26, 'services', 'wifi fix', 'jio fiber setup', 120001, 120013, 'hingoli', 'Hingoli', 'Maharashtra', 'India', NULL, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_69526b0555fe4_1767009029.webp', 9595340263, 'active', 0, 0, NULL, NULL, NULL, 6, 0, 0.0, '2025-12-29 11:50:34', '2025-12-29 14:45:12', NULL),
(27, 'business', 'ELECTRICAL', '', 150088, 150115, '', 'Hingoli', 'Maharashtra', 'India', NULL, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_69526b843e51f_1767009156.webp', 9595340263, 'active', 0, 0, NULL, NULL, NULL, 24, 0, 0.0, '2025-12-29 11:52:41', '2025-12-30 03:18:05', NULL),
(28, 'jobs', 'labour', 'test', 210001, NULL, '', 'Hingoli', 'Maharashtra', 'India', NULL, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_69526c3778fa8_1767009335.webp', 9595340263, 'active', 1, 0, NULL, NULL, NULL, 12, 0, 0.0, '2025-12-29 11:55:40', '2025-12-30 03:58:52', NULL),
(29, 'services', 'electrician', 'desc', 120001, 120013, 'shivaji nagar', 'Hingoli', 'Maharashtra', 'India', NULL, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_695350596cdb9_1767067737.webp', 8669702031, 'pending', 0, 0, NULL, NULL, NULL, 0, 0, 0.0, '2025-12-30 04:09:02', '2025-12-30 04:09:02', NULL),
(30, 'business', 'my business', 'my description', 150086, 150098, 'shivaji nagar', 'Hingoli', 'Maharashtra', 'India', NULL, NULL, NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_6953509492912_1767067796.webp', 8669702031, 'pending', 0, 0, NULL, NULL, NULL, 0, 0, 0.0, '2025-12-30 04:10:01', '2025-12-30 04:10:01', NULL),
(31, 'business', 'jio\'s Shop', 'Personal seller shop', 3, NULL, NULL, 'Hingoli', 'Maharashtra', 'India', NULL, NULL, NULL, NULL, 8669702031, 'active', 1, 0, NULL, NULL, NULL, 0, 0, 0.0, '2025-12-30 04:10:48', '2025-12-30 04:10:48', NULL);

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

--
-- Dumping data for table `listing_images`
--

INSERT INTO `listing_images` (`image_id`, `listing_id`, `image_url`, `thumbnail_url`, `sort_order`, `image_type`, `alt_text`, `file_size_kb`, `width`, `height`, `created_at`) VALUES
(180001, 8, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listing_69491193edaf3_1766396307.webp', NULL, 0, 'gallery', NULL, NULL, NULL, NULL, '2025-12-22 09:38:32'),
(240001, 26, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_69526b359b1ff_1767009077.webp', NULL, 0, 'gallery', NULL, NULL, NULL, NULL, '2025-12-29 11:51:22'),
(240002, 27, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_69526bf424111_1767009268.webp', NULL, 0, 'gallery', NULL, NULL, NULL, NULL, '2025-12-29 11:54:32');

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

--
-- Dumping data for table `listing_price_list`
--

INSERT INTO `listing_price_list` (`item_id`, `listing_id`, `item_name`, `item_description`, `item_category`, `price`, `discounted_price`, `currency`, `duration_minutes`, `image_url`, `sort_order`, `is_popular`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 1, 'Fan Installation', NULL, NULL, 200.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:44', '2025-12-21 17:42:44'),
(2, 1, 'Switch Board Repair', NULL, NULL, 150.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:44', '2025-12-21 17:42:44'),
(3, 1, 'MCB Installation', NULL, NULL, 300.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:44', '2025-12-21 17:42:44'),
(4, 1, 'House Wiring (per point)', NULL, NULL, 500.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:44', '2025-12-21 17:42:44'),
(5, 1, 'AC Installation', NULL, NULL, 1500.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:44', '2025-12-21 17:42:44'),
(6, 2, 'Tap Repair', NULL, NULL, 150.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(7, 2, 'Pipe Leakage Fix', NULL, NULL, 250.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(8, 2, 'Drainage Cleaning', NULL, NULL, 500.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(9, 2, 'Toilet Installation', NULL, NULL, 800.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(10, 2, 'Water Tank Repair', NULL, NULL, 600.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(11, 7, 'Facial (Gold)', NULL, NULL, 500.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(12, 7, 'Hair Cut', NULL, NULL, 150.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(13, 7, 'Hair Coloring', NULL, NULL, 800.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(14, 7, 'Bridal Makeup', NULL, NULL, 5000.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(15, 7, 'Mehendi (Full Hands)', NULL, NULL, 300.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(16, 10, 'Pre-Wedding Shoot', NULL, NULL, 15000.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(17, 10, 'Wedding Day Photography', NULL, NULL, 25000.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(18, 10, 'Full Wedding Package', NULL, NULL, 50000.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(19, 10, 'Drone Coverage', NULL, NULL, 10000.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45'),
(20, 10, 'Photo Album (100 pages)', NULL, NULL, 8000.00, NULL, 'INR', NULL, NULL, 0, 0, 1, '2025-12-21 17:42:45', '2025-12-21 17:42:45');

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

--
-- Dumping data for table `notification_logs`
--

INSERT INTO `notification_logs` (`log_id`, `title`, `body`, `target_type`, `target_value`, `sent_count`, `sent_by`, `created_at`) VALUES
(1, 'dgfd', 'dfgdf', 'all', NULL, 0, NULL, '2025-12-13 05:57:10'),
(2, 'dfd', 'fgdfxgf', 'all', NULL, 1, NULL, '2025-12-13 05:59:46'),
(3, 'पॅराग्राफ लिहा आपण ज्यावर पॅराग्राफ लिहायचा आहे ', 'पॅराग्राफ लिहा आपण ज्यावर पॅराग्राफ लिहायचा आहे त्या विषयाबद्दल थोडक्यात माहिती देणारा आणि त्यावर सुसंवादी पैलू मांडणारा पॅराग्राफ खाली दिला आहे. ', 'all', NULL, 1, NULL, '2025-12-13 06:00:46'),
(44005, 'Test Notification', 'Hi there this is the api test notification you can simply swipe this or ignore', 'all', NULL, 4, NULL, '2025-12-15 02:38:44'),
(74005, 'Good Night Test Message From Hello Hingoli', 'हेलो हिंगोली ऐप की तरफ से शुभ रात्रि।', 'all', NULL, 10, NULL, '2025-12-16 16:40:57'),
(104005, 'Test', 'Message here', 'all', NULL, 9, NULL, '2025-12-21 02:33:50'),
(104006, 'Test 2', 'Test2', 'all', NULL, 9, NULL, '2025-12-21 02:36:40'),
(104007, 'test3', 'test3', 'all', NULL, 0, NULL, '2025-12-21 02:45:03'),
(104008, 'test3', 'test3', 'all', NULL, 0, NULL, '2025-12-21 02:45:17'),
(134005, 'Test5', 'TEST', 'all', NULL, 0, NULL, '2025-12-22 09:43:47'),
(164005, 'test5', 'test', 'all', NULL, 0, NULL, '2025-12-22 09:55:43'),
(164006, 'test6', 'test', 'all', NULL, 0, NULL, '2025-12-22 09:56:25'),
(164007, 'test7', 'testing', 'all', NULL, 0, NULL, '2025-12-22 09:57:34'),
(164008, 'test8', 'test', 'all', NULL, 0, NULL, '2025-12-22 10:02:24'),
(164009, 'test9', 'test', 'all', NULL, 0, NULL, '2025-12-22 10:06:52'),
(164010, 'test10', 'test', 'all', NULL, 11, NULL, '2025-12-22 10:10:02'),
(164011, 'test11', 'test11', 'all', NULL, 11, NULL, '2025-12-22 10:19:05'),
(194005, 'TESTING NEW VERSION', 'TEST', 'all', NULL, 15, NULL, '2025-12-29 12:00:16'),
(194006, 'FINAL TEST', 'FINAL TEST', 'all', NULL, 15, NULL, '2025-12-29 12:25:46'),
(224005, 'test', 'test', 'all', NULL, 15, NULL, '2025-12-29 13:00:27');

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

--
-- Dumping data for table `notification_settings`
--

INSERT INTO `notification_settings` (`id`, `setting_key`, `setting_value`, `description`, `updated_at`) VALUES
(1, 'new_listing_enabled', 'true', 'Send notification when new listing is approved', '2025-12-12 10:26:58'),
(2, 'new_service_title', 'New Service Available!', 'Title for new service notifications', '2025-12-12 10:26:58'),
(3, 'new_service_message', 'A new service has been listed in your area.', 'Message for new service notifications', '2025-12-12 10:26:58'),
(4, 'new_business_title', 'New Business Listed!', 'Title for new business notifications', '2025-12-12 10:26:58'),
(5, 'new_business_message', 'A new business has been listed near you.', 'Message for new business notifications', '2025-12-12 10:26:58'),
(6, 'new_job_title', 'New Job Opportunity!', 'Title for new job notifications', '2025-12-12 10:26:58'),
(7, 'new_job_message', 'A new job has been posted. Check it out!', 'Message for new job notifications', '2025-12-12 10:26:58'),
(8, 'new_selling_title', 'New Item For Sale!', 'Title for new selling notifications', '2025-12-12 10:26:58'),
(9, 'new_selling_message', 'A new item is available for sale.', 'Message for new selling notifications', '2025-12-12 10:26:58');

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

--
-- Dumping data for table `old_categories`
--

INSERT INTO `old_categories` (`id`, `parent_id`, `level`, `name`, `name_mr`, `slug`, `icon`, `color`, `image_url`, `sort_order`, `is_active`, `product_count`, `created_at`, `updated_at`) VALUES
(1, NULL, 1, 'Mobile Phones', 'मोबाइल फोन', 'old-mobiles', 'smartphone', '#2196F3', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/mobile-phones.jpg', 1, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(2, NULL, 1, 'Electronics', 'इलेक्ट्रॉनिक्स', 'old-electronics', 'devices', '#673AB7', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/mobiles-electronics.jpg', 2, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(3, NULL, 1, 'Furniture', 'फर्निचर', 'old-furniture', 'chair', '#795548', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/home-furniture.jpg', 3, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(4, NULL, 1, 'Home Appliances', 'घरगुती उपकरणे', 'old-appliances', 'kitchen', '#FF5722', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/kitchen-appliances.jpg', 4, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(5, NULL, 1, 'Vehicles', 'वाहने', 'old-vehicles', 'directions_bike', '#F44336', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/vehicles.jpg', 5, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(6, NULL, 1, 'Books & Education', 'पुस्तके आणि शिक्षण', 'old-books', 'menu_book', '#4CAF50', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 6, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(7, NULL, 1, 'Clothing & Fashion', 'कपडे आणि फॅशन', 'old-clothing', 'checkroom', '#E91E63', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fashion.jpg', 7, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(8, NULL, 1, 'Sports & Fitness', 'खेळ आणि फिटनेस', 'old-sports', 'fitness_center', '#00BCD4', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 8, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(9, NULL, 1, 'Kids & Baby', 'मुले आणि बाळ', 'old-kids', 'child_care', '#FFEB3B', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fashion.jpg', 9, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(10, NULL, 1, 'Musical Instruments', 'वाद्ये', 'old-music', 'music_note', '#9C27B0', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 10, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(11, NULL, 1, 'Computer & Laptop', 'कॉम्प्युटर आणि लॅपटॉप', 'old-computers', 'computer', '#607D8B', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/laptops.jpg', 11, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(12, NULL, 1, 'Camera & Photography', 'कॅमेरा आणि फोटोग्राफी', 'old-camera', 'photo_camera', '#FF9800', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/cameras.jpg', 12, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(13, NULL, 1, 'Games & Consoles', 'गेम्स आणि कन्सोल', 'old-games', 'sports_esports', '#3F51B5', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/gaming.jpg', 13, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(14, NULL, 1, 'Antiques & Collectibles', 'प्राचीन वस्तू', 'old-antiques', 'diamond', '#8D6E63', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/home-decor.jpg', 14, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(15, NULL, 1, 'Other Items', 'इतर वस्तू', 'old-other', 'more_horiz', '#9E9E9E', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/other-items.jpg', 15, 1, 0, '2025-12-27 17:38:49', '2025-12-29 04:33:54'),
(16, 1, 2, 'Smartphones', 'स्मार्टफोन', 'old-smartphones', 'phone_android', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/mobile-phones.jpg', 1, 1, 0, '2025-12-27 17:38:50', '2025-12-29 04:35:24'),
(17, 1, 2, 'Feature Phones', 'फीचर फोन', 'old-feature-phones', 'phone', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/mobile-phones.jpg', 2, 1, 0, '2025-12-27 17:38:50', '2025-12-29 04:35:24'),
(18, 1, 2, 'Tablets', 'टॅबलेट', 'old-tablets', 'tablet', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/tablets.jpg', 3, 1, 0, '2025-12-27 17:38:50', '2025-12-29 04:35:24'),
(19, 1, 2, 'Mobile Accessories', 'मोबाइल ॲक्सेसरीज', 'old-mobile-accessories', 'headphones', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/computer-accessories.jpg', 4, 1, 0, '2025-12-27 17:38:50', '2025-12-29 04:35:24'),
(20, 2, 2, 'TV & Monitors', 'टीव्ही आणि मॉनिटर', 'old-tv-monitors', 'tv', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/tv-video.jpg', 1, 1, 0, '2025-12-27 17:38:51', '2025-12-29 04:35:24'),
(21, 2, 2, 'Audio & Speakers', 'ऑडिओ आणि स्पीकर', 'old-audio', 'speaker', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/audio-speakers.jpg', 2, 1, 0, '2025-12-27 17:38:51', '2025-12-29 04:35:24'),
(22, 2, 2, 'UPS & Inverters', 'यूपीएस आणि इन्व्हर्टर', 'old-ups', 'battery_charging_full', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/mobiles-electronics.jpg', 3, 1, 0, '2025-12-27 17:38:51', '2025-12-29 04:35:24'),
(23, 2, 2, 'Fans & Coolers', 'पंखे आणि कूलर', 'old-fans', 'mode_fan', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/kitchen-appliances.jpg', 4, 1, 0, '2025-12-27 17:38:51', '2025-12-29 04:35:24'),
(24, 3, 2, 'Beds & Mattresses', 'पलंग आणि गादी', 'old-beds', 'bed', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/beds-wardrobes.jpg', 1, 1, 0, '2025-12-27 17:38:52', '2025-12-29 04:35:24'),
(25, 3, 2, 'Sofas & Chairs', 'सोफा आणि खुर्च्या', 'old-sofas', 'weekend', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/sofa-dining.jpg', 2, 1, 0, '2025-12-27 17:38:52', '2025-12-29 04:35:24'),
(26, 3, 2, 'Tables & Desks', 'टेबल आणि डेस्क', 'old-tables', 'table_restaurant', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/home-furniture.jpg', 3, 1, 0, '2025-12-27 17:38:52', '2025-12-29 04:35:24'),
(27, 3, 2, 'Wardrobes & Storage', 'कपाट आणि स्टोरेज', 'old-wardrobes', 'door_sliding', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/beds-wardrobes.jpg', 4, 1, 0, '2025-12-27 17:38:52', '2025-12-29 04:35:24'),
(28, 3, 2, 'Office Furniture', 'ऑफिस फर्निचर', 'old-office-furniture', 'desk', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/home-furniture.jpg', 5, 1, 0, '2025-12-27 17:38:52', '2025-12-29 04:35:24'),
(29, 4, 2, 'Washing Machine', 'वॉशिंग मशीन', 'old-washing-machine', 'local_laundry_service', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/kitchen-appliances.jpg', 1, 1, 0, '2025-12-27 17:38:53', '2025-12-29 04:35:24'),
(30, 4, 2, 'Refrigerator', 'फ्रिज', 'old-refrigerator', 'kitchen', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/kitchen-appliances.jpg', 2, 1, 0, '2025-12-27 17:38:53', '2025-12-29 04:35:24'),
(31, 4, 2, 'AC & Cooler', 'एसी आणि कूलर', 'old-ac', 'ac_unit', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/kitchen-appliances.jpg', 3, 1, 0, '2025-12-27 17:38:53', '2025-12-29 04:35:24'),
(32, 4, 2, 'Kitchen Appliances', 'किचन उपकरणे', 'old-kitchen-appliances', 'blender', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/kitchen-appliances.jpg', 4, 1, 0, '2025-12-27 17:38:53', '2025-12-29 04:35:24'),
(33, 4, 2, 'Water Purifier', 'वॉटर प्युरीफायर', 'old-water-purifier', 'water_drop', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/kitchen-appliances.jpg', 5, 1, 0, '2025-12-27 17:38:53', '2025-12-29 04:35:24'),
(34, 5, 2, 'Bikes & Scooters', 'बाइक आणि स्कूटर', 'old-bikes', 'two_wheeler', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/scooters.jpg', 1, 1, 0, '2025-12-27 17:38:53', '2025-12-29 04:35:24'),
(35, 5, 2, 'Cars', 'कार', 'old-cars', 'directions_car', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/cars.jpg', 2, 1, 0, '2025-12-27 17:38:53', '2025-12-29 04:35:24'),
(36, 5, 2, 'Bicycles', 'सायकल', 'old-bicycles', 'pedal_bike', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/bicycles.jpg', 3, 1, 0, '2025-12-27 17:38:53', '2025-12-29 04:35:24'),
(37, 5, 2, 'Electric Vehicles', 'इलेक्ट्रिक वाहने', 'old-ev', 'electric_bike', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/vehicles.jpg', 4, 1, 0, '2025-12-27 17:38:53', '2025-12-29 04:35:24'),
(38, 5, 2, 'Spare Parts', 'स्पेअर पार्ट्स', 'old-spare-parts', 'settings', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/spare-parts.jpg', 5, 1, 0, '2025-12-27 17:38:53', '2025-12-29 04:35:24'),
(39, 6, 2, 'School Books', 'शाळेची पुस्तके', 'old-school-books', 'school', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 1, 1, 0, '2025-12-27 17:38:54', '2025-12-29 04:36:24'),
(40, 6, 2, 'Competitive Exam', 'स्पर्धा परीक्षा', 'old-competitive-books', 'emoji_events', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 2, 1, 0, '2025-12-27 17:38:54', '2025-12-29 04:36:24'),
(41, 6, 2, 'College/Professional', 'कॉलेज/व्यावसायिक', 'old-college-books', 'science', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 3, 1, 0, '2025-12-27 17:38:54', '2025-12-29 04:36:24'),
(42, 6, 2, 'Novels & Fiction', 'कादंबऱ्या', 'old-novels', 'auto_stories', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 4, 1, 0, '2025-12-27 17:38:54', '2025-12-29 04:36:24'),
(43, 6, 2, 'Stationery', 'स्टेशनरी', 'old-stationery', 'edit', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 5, 1, 0, '2025-12-27 17:38:54', '2025-12-29 04:36:24'),
(44, 7, 2, 'Men Clothing', 'पुरुष कपडे', 'old-men-clothing', 'man', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fashion.jpg', 1, 1, 0, '2025-12-27 17:38:55', '2025-12-29 04:36:24'),
(45, 7, 2, 'Women Clothing', 'महिला कपडे', 'old-women-clothing', 'woman', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fashion.jpg', 2, 1, 0, '2025-12-27 17:38:55', '2025-12-29 04:36:24'),
(46, 7, 2, 'Kids Clothing', 'मुलांचे कपडे', 'old-kids-clothing', 'child_friendly', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fashion.jpg', 3, 1, 0, '2025-12-27 17:38:55', '2025-12-29 04:36:24'),
(47, 7, 2, 'Footwear', 'पादत्राणे', 'old-footwear', 'directions_walk', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fashion.jpg', 4, 1, 0, '2025-12-27 17:38:55', '2025-12-29 04:36:24'),
(48, 7, 2, 'Bags & Luggage', 'बॅग आणि लगेज', 'old-bags', 'luggage', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fashion.jpg', 5, 1, 0, '2025-12-27 17:38:55', '2025-12-29 04:36:24'),
(49, 8, 2, 'Cricket', 'क्रिकेट', 'old-cricket', 'sports_cricket', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 1, 1, 0, '2025-12-27 17:38:56', '2025-12-29 04:36:24'),
(50, 8, 2, 'Gym Equipment', 'जिम उपकरणे', 'old-gym', 'fitness_center', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 2, 1, 0, '2025-12-27 17:38:56', '2025-12-29 04:36:24'),
(51, 8, 2, 'Badminton & Tennis', 'बॅडमिंटन आणि टेनिस', 'old-racquet', 'sports_tennis', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 3, 1, 0, '2025-12-27 17:38:56', '2025-12-29 04:36:24'),
(52, 8, 2, 'Cycling', 'सायकलिंग', 'old-cycling', 'pedal_bike', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/bicycles.jpg', 4, 1, 0, '2025-12-27 17:38:56', '2025-12-29 04:36:24'),
(53, 8, 2, 'Other Sports', 'इतर खेळ', 'old-other-sports', 'sports', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 5, 1, 0, '2025-12-27 17:38:56', '2025-12-29 04:36:24'),
(54, 9, 2, 'Toys', 'खेळणी', 'old-toys', 'toys', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 1, 1, 0, '2025-12-27 17:38:57', '2025-12-29 04:36:24'),
(55, 9, 2, 'Baby Gear', 'बेबी गियर', 'old-baby-gear', 'stroller', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/fashion.jpg', 2, 1, 0, '2025-12-27 17:38:57', '2025-12-29 04:36:24'),
(56, 9, 2, 'Kids Furniture', 'मुलांचे फर्निचर', 'old-kids-furniture', 'crib', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/home-furniture.jpg', 3, 1, 0, '2025-12-27 17:38:57', '2025-12-29 04:36:24'),
(57, 9, 2, 'School Items', 'शाळेच्या वस्तू', 'old-school-items', 'backpack', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/books-sports-hobbies.jpg', 4, 1, 0, '2025-12-27 17:38:57', '2025-12-29 04:36:24'),
(58, 11, 2, 'Laptops', 'लॅपटॉप', 'old-laptops', 'laptop', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/laptops.jpg', 1, 1, 0, '2025-12-27 17:38:58', '2025-12-29 04:36:24'),
(59, 11, 2, 'Desktop PCs', 'डेस्कटॉप पीसी', 'old-desktops', 'desktop_windows', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/desktop-computers.jpg', 2, 1, 0, '2025-12-27 17:38:58', '2025-12-29 04:36:24'),
(60, 11, 2, 'Printers', 'प्रिंटर', 'old-printers', 'print', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/printers-scanners.jpg', 3, 1, 0, '2025-12-27 17:38:58', '2025-12-29 04:36:24'),
(61, 11, 2, 'Computer Parts', 'कॉम्प्युटर पार्ट्स', 'old-computer-parts', 'memory', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/computer-accessories.jpg', 4, 1, 0, '2025-12-27 17:38:58', '2025-12-29 04:36:24'),
(62, 11, 2, 'Accessories', 'ॲक्सेसरीज', 'old-computer-accessories', 'mouse', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/computer-accessories.jpg', 5, 1, 0, '2025-12-27 17:38:58', '2025-12-29 04:36:24'),
(63, 12, 2, 'DSLR & Mirrorless', 'डीएसएलआर आणि मिररलेस', 'old-dslr', 'camera', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/cameras.jpg', 1, 1, 0, '2025-12-27 17:38:59', '2025-12-29 04:36:24'),
(64, 12, 2, 'Point & Shoot', 'पॉइंट अँड शूट', 'old-point-shoot', 'photo_camera', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/cameras.jpg', 2, 1, 0, '2025-12-27 17:38:59', '2025-12-29 04:36:24'),
(65, 12, 2, 'Lenses', 'लेन्स', 'old-lenses', 'camera', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/cameras.jpg', 3, 1, 0, '2025-12-27 17:38:59', '2025-12-29 04:36:24'),
(66, 12, 2, 'Tripods & Accessories', 'तिपाई आणि ॲक्सेसरीज', 'old-camera-accessories', 'tripod', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/cameras.jpg', 4, 1, 0, '2025-12-27 17:38:59', '2025-12-29 04:36:24'),
(67, 13, 2, 'Gaming Consoles', 'गेमिंग कन्सोल', 'old-consoles', 'videogame_asset', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/gaming.jpg', 1, 1, 0, '2025-12-27 17:39:00', '2025-12-29 04:36:24'),
(68, 13, 2, 'Video Games', 'व्हिडिओ गेम्स', 'old-video-games', 'sports_esports', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/gaming.jpg', 2, 1, 0, '2025-12-27 17:39:00', '2025-12-29 04:36:24'),
(69, 13, 2, 'Gaming Accessories', 'गेमिंग ॲक्सेसरीज', 'old-gaming-accessories', 'gamepad', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/gaming.jpg', 3, 1, 0, '2025-12-27 17:39:00', '2025-12-29 04:36:24'),
(70, 13, 2, 'Board Games', 'बोर्ड गेम्स', 'old-board-games', 'casino', NULL, 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/selling_category_images/gaming.jpg', 4, 1, 0, '2025-12-27 17:39:00', '2025-12-29 04:36:24'),
(30002, 14, 1, 'Vintage Coins & Currency', 'जुनी नाणी आणि चलन', 'vintage-coins-currency', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:29'),
(30003, 14, 1, 'Antique Furniture', 'प्राचीन फर्निचर', 'antique-furniture', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:29'),
(30004, 14, 1, 'Collectible Art', 'संग्रहणीय कला', 'collectible-art', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:30'),
(30005, 14, 1, 'Old Jewelry', 'जुने दागिने', 'old-jewelry', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:30'),
(30006, 14, 1, 'Vintage Decor', 'जुनी सजावट', 'vintage-decor', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:30'),
(30007, 10, 1, 'String Instruments', 'तंतुवाद्य', 'string-instruments', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:30'),
(30008, 10, 1, 'Keyboard Instruments', 'कीबोर्ड वाद्य', 'keyboard-instruments', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:30'),
(30009, 10, 1, 'Percussion', 'तालवाद्य', 'percussion', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:30'),
(30010, 10, 1, 'Wind Instruments', 'फुंकवाद्य', 'wind-instruments', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:30'),
(30011, 10, 1, 'Electronic Instruments', 'इलेक्ट्रॉनिक वाद्य', 'electronic-instruments', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:30'),
(30012, 15, 1, 'Miscellaneous', 'इतर', 'miscellaneous', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:30'),
(30013, 15, 1, 'Hobby Items', 'छंद वस्तू', 'hobby-items', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:31'),
(30014, 15, 1, 'Craft Supplies', 'हस्तकला साहित्य', 'craft-supplies', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:31'),
(30015, 15, 1, 'Tools & Equipment', 'साधने आणि उपकरणे', 'tools-equipment', NULL, NULL, NULL, 0, 1, 0, '2025-12-29 06:33:55', '2025-12-29 17:07:31');

-- --------------------------------------------------------

--
-- Table structure for table `old_products`
--

CREATE TABLE `old_products` (
  `product_id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT 'User who is selling the item',
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

--
-- Dumping data for table `old_products`
--

INSERT INTO `old_products` (`product_id`, `user_id`, `product_name`, `description`, `category_id`, `old_category_id`, `subcategory_id`, `price`, `original_price`, `currency`, `image_url`, `additional_images`, `condition`, `age_months`, `has_warranty`, `warranty_months`, `has_bill`, `reason_for_selling`, `brand`, `model`, `city`, `pincode`, `show_phone`, `accept_offers`, `status`, `is_featured`, `view_count`, `inquiry_count`, `created_at`, `updated_at`, `sold_at`) VALUES
(1, 9595340263, 'iPhone 12 - 64GB', 'Used iPhone 12 in excellent condition. No scratches, battery health 89%. Comes with charger and box.', 150003, 1, 150027, 35000.00, 65000.00, 'INR', 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600', NULL, 'like_new', 24, 0, NULL, 1, 'Upgrading to newer model', 'Apple', 'iPhone 12', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:26', NULL),
(2, 7020034431, 'Samsung Galaxy S21', 'Samsung S21 5G, 8GB RAM, 128GB storage. Minor scratches on back. Fast charging works perfectly.', 150003, 1, 150027, 25000.00, 55000.00, 'INR', 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=600', NULL, 'good', 18, 0, NULL, 1, 'Not using anymore', 'Samsung', 'Galaxy S21', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:26', NULL),
(3, 9595340263, 'Wooden Study Table', 'Solid wood study table with drawer. Slightly used, very sturdy. 4ft x 2ft size.', 150005, 3, 150052, 3500.00, 8000.00, 'INR', 'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=600', NULL, 'good', 36, 0, NULL, 0, 'Relocating to another city', NULL, NULL, 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:41', NULL),
(4, 7020034431, 'Double Bed with Mattress', 'Queen size double bed with mattress. Wooden frame, no termite. Mattress foam type, still comfortable.', 150005, 3, 150051, 12000.00, 28000.00, 'INR', 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=600', NULL, 'fair', 48, 0, NULL, 0, 'Buying new furniture', NULL, NULL, 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:41', NULL),
(5, 9595340263, 'Samsung Split AC - 1.5 Ton', '1.5 Ton Inverter AC, 3-star rating. Works perfectly, gas filled recently. Includes installation support.', 150004, 1, 150044, 18000.00, 35000.00, 'INR', 'https://images.unsplash.com/photo-1585338447937-7082f8fc763d?w=600', NULL, 'good', 30, 0, NULL, 1, 'Moving to company accommodation', 'Samsung', 'AR18TY3QBBU', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:26', NULL),
(6, 7020034431, 'LG Washing Machine - 7KG', 'Fully automatic top load washing machine. All programs working. Minor dent on side, no effect on function.', 150004, 4, 150041, 8000.00, 18000.00, 'INR', 'https://images.unsplash.com/photo-1626806787461-102c1bfaaea1?w=600', NULL, 'fair', 42, 0, NULL, 0, 'Upgrading to front load', 'LG', 'T7281NDDLGD', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:55', NULL),
(7, 9595340263, 'Hero Splendor Plus - 2020', 'Well maintained bike, single owner. Regular service done. New tyres, insurance valid till March 2025.', 150006, 5, NULL, 45000.00, 72000.00, 'INR', 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600', NULL, 'good', 48, 0, NULL, 1, 'Buying car', 'Hero', 'Splendor Plus', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:13', NULL),
(8, 7020034431, 'UPSC Books Set - Complete', 'Complete set of NCERT + standard books for UPSC preparation. Highlighted but readable. Best for beginners.', 150007, 6, NULL, 2500.00, 6000.00, 'INR', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600', NULL, 'good', 12, 0, NULL, 0, 'Cleared exam, helping others', NULL, NULL, 'Hingoli', NULL, 1, 0, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:53:00', NULL),
(9, 9595340263, 'Yonex Badminton Racket', 'Yonex Nanoray racket with case. String tension good. Used for 6 months only.', 150008, 4, NULL, 1200.00, 2500.00, 'INR', 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=600', NULL, 'like_new', 6, 0, NULL, 0, 'Stopped playing', 'Yonex', 'Nanoray', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:55', NULL),
(10, 7020034431, 'Baby Stroller - Graco', 'Foldable baby stroller, suitable for 0-3 years. All wheels working, canopy included. Easy to carry.', 150009, 4, NULL, 3000.00, 12000.00, 'INR', 'https://images.unsplash.com/photo-1519689680058-324335c77eba?w=600', NULL, 'good', 24, 0, NULL, 0, 'Child grown up', 'Graco', 'LiteRider', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:55', NULL),
(11, 9595340263, 'iPhone 12 - 64GB', 'Used iPhone 12 in excellent condition. No scratches, battery health 89%. Comes with charger and box.', 150003, 1, 150027, 35000.00, 65000.00, 'INR', 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600', NULL, 'like_new', 24, 0, NULL, 1, 'Upgrading to newer model', 'Apple', 'iPhone 12', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:26', NULL),
(12, 7020034431, 'Samsung Galaxy S21', 'Samsung S21 5G, 8GB RAM, 128GB storage. Minor scratches on back. Fast charging works perfectly.', 150003, 1, 150027, 25000.00, 55000.00, 'INR', 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=600', NULL, 'good', 18, 0, NULL, 1, 'Not using anymore', 'Samsung', 'Galaxy S21', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:26', NULL),
(13, 9595340263, 'Wooden Study Table', 'Solid wood study table with drawer. Slightly used, very sturdy. 4ft x 2ft size.', 150005, 3, 150052, 3500.00, 8000.00, 'INR', 'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?w=600', NULL, 'good', 36, 0, NULL, 0, 'Relocating to another city', NULL, NULL, 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:41', NULL),
(14, 7020034431, 'Double Bed with Mattress', 'Queen size double bed with mattress. Wooden frame, no termite. Mattress foam type, still comfortable.', 150005, 3, 150051, 12000.00, 28000.00, 'INR', 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=600', NULL, 'fair', 48, 0, NULL, 0, 'Buying new furniture', NULL, NULL, 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:41', NULL),
(15, 9595340263, 'Samsung Split AC - 1.5 Ton', '1.5 Ton Inverter AC, 3-star rating. Works perfectly, gas filled recently. Includes installation support.', 150004, 1, 150044, 18000.00, 35000.00, 'INR', 'https://images.unsplash.com/photo-1585338447937-7082f8fc763d?w=600', NULL, 'good', 30, 0, NULL, 1, 'Moving to company accommodation', 'Samsung', 'AR18TY3QBBU', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:26', NULL),
(16, 7020034431, 'LG Washing Machine - 7KG', 'Fully automatic top load washing machine. All programs working. Minor dent on side, no effect on function.', 150004, 4, 150041, 8000.00, 18000.00, 'INR', 'https://images.unsplash.com/photo-1626806787461-102c1bfaaea1?w=600', NULL, 'fair', 42, 0, NULL, 0, 'Upgrading to front load', 'LG', 'T7281NDDLGD', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:55', NULL),
(17, 9595340263, 'Hero Splendor Plus - 2020', 'Well maintained bike, single owner. Regular service done. New tyres, insurance valid till March 2025.', 150006, 5, NULL, 45000.00, 72000.00, 'INR', 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600', NULL, 'good', 48, 0, NULL, 1, 'Buying car', 'Hero', 'Splendor Plus', 'Hingoli', NULL, 1, 1, 'active', 0, 4, 0, '2025-12-27 17:32:52', '2025-12-30 04:26:59', NULL),
(18, 7020034431, 'UPSC Books Set - Complete', 'Complete set of NCERT + standard books for UPSC preparation. Highlighted but readable. Best for beginners.', 150007, 6, NULL, 2500.00, 6000.00, 'INR', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600', NULL, 'good', 12, 0, NULL, 0, 'Cleared exam, helping others', NULL, NULL, 'Hingoli', NULL, 1, 0, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:53:00', NULL),
(19, 9595340263, 'Yonex Badminton Racket', 'Yonex Nanoray racket with case. String tension good. Used for 6 months only.', 150008, 4, NULL, 1200.00, 2500.00, 'INR', 'https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?w=600', NULL, 'like_new', 6, 0, NULL, 0, 'Stopped playing', 'Yonex', 'Nanoray', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:55', NULL),
(20, 7020034431, 'Baby Stroller - Graco', 'Foldable baby stroller, suitable for 0-3 years. All wheels working, canopy included. Easy to carry.', 150009, 4, NULL, 3000.00, 12000.00, 'INR', 'https://images.unsplash.com/photo-1519689680058-324335c77eba?w=600', NULL, 'good', 24, 0, NULL, 0, 'Child grown up', 'Graco', 'LiteRider', 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-27 17:32:52', '2025-12-30 03:51:55', NULL),
(21, 9595340263, 'antique', 'yedt', NULL, 14, 30003, 400.00, 599.00, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_69526e5054721_1767009872.webp', NULL, 'good', NULL, 0, NULL, 0, NULL, NULL, NULL, 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-29 12:04:37', '2025-12-30 03:56:40', NULL),
(22, 9595340263, 'laptop', '', NULL, 14, 30003, 500.00, 1000.00, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_6952a3032df76_1767023363.webp', NULL, 'good', NULL, 0, NULL, 0, NULL, NULL, NULL, 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-29 15:49:28', '2025-12-30 03:55:58', NULL),
(23, 8669702031, 'antique', 'antique piece', NULL, 14, 30003, 300.00, 500.00, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_695350fae948d_1767067898.webp', NULL, 'good', NULL, 0, NULL, 0, NULL, NULL, NULL, 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-30 04:11:43', '2025-12-30 04:12:30', NULL),
(24, 8669702031, 'anitique', '', NULL, 14, NULL, 84.00, 300.00, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_69535172c13d0_1767068018.webp', NULL, 'good', NULL, 0, NULL, 0, NULL, NULL, NULL, 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-30 04:13:43', '2025-12-30 04:13:43', NULL),
(25, 8669702031, 'antique', '', NULL, 14, 30003, 39.00, 93.00, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_6953531da9eaf_1767068445.webp', NULL, 'good', NULL, 0, NULL, 0, NULL, NULL, NULL, 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-30 04:20:50', '2025-12-30 04:28:54', NULL),
(26, 8669702031, '844884', '8338', NULL, 14, NULL, 82.00, 3883.00, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_6953542260809_1767068706.webp', NULL, 'good', NULL, 0, NULL, 0, NULL, NULL, NULL, 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-30 04:25:11', '2025-12-30 04:25:11', NULL),
(76252, 9595340263, 'Samsung 32\" Smart TV', 'Samsung 32 inch HD Ready Smart LED TV. Built-in WiFi, YouTube, Netflix. 2 HDMI ports. 1 Year warranty.', NULL, 2, 20, 18999.00, 22999.00, 'INR', 'https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=600', NULL, 'good', NULL, 0, NULL, 0, NULL, NULL, NULL, 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-29 06:03:59', '2025-12-30 03:57:44', NULL),
(76253, 210002, 'Homi Bhabha book', 'I have a new Homi Bhabha book of 2025.', NULL, 6, NULL, 350.00, 350.00, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_695215920d3cb_1766987154.webp', NULL, 'good', NULL, 0, NULL, 0, NULL, NULL, NULL, 'Hingoli', NULL, 1, 1, 'active', 0, 0, 0, '2025-12-29 06:03:59', '2025-12-29 06:03:59', NULL);

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

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`order_id`, `order_number`, `user_id`, `address_id`, `subtotal`, `shipping_fee`, `total_amount`, `payment_method`, `payment_status`, `razorpay_order_id`, `razorpay_payment_id`, `order_status`, `created_at`, `webhook_verified`, `webhook_verified_at`, `refund_id`, `refund_amount`, `payment_error`, `payment_method_detail`, `dispute_id`, `dispute_status`, `dispute_reason`, `dispute_amount`, `estimated_delivery_date`, `delivery_time_slot`, `delivery_user_id`, `delivery_earnings`, `delivery_accepted_at`, `delivery_picked_at`, `delivered_at`) VALUES
(1, 'HH20251215E4BBDB', 60002, 1, 1.00, 0.00, 1.00, 'razorpay', 'pending', NULL, NULL, 'pending', '2025-12-15 06:23:45', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(30001, 'HH20251215F45990', 60002, 1, 1.00, 0.00, 1.00, 'razorpay', 'pending', NULL, NULL, 'pending', '2025-12-15 07:53:06', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(30002, 'HH20251215CD5FD3', 60002, 1, 1.00, 0.00, 1.00, 'razorpay', 'paid', NULL, NULL, 'pending', '2025-12-15 08:02:55', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(30003, 'HH20251215984869', 60002, 1, 1.00, 0.00, 1.00, 'razorpay', 'pending', NULL, NULL, 'pending', '2025-12-15 08:04:44', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(60001, 'HH202512158E2BDF', 60002, 1, 1.00, 0.00, 1.00, 'razorpay', 'pending', NULL, NULL, 'pending', '2025-12-15 08:50:35', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(90001, 'HH20251215C3AB03', 60002, 1, 1.00, 0.00, 1.00, 'razorpay', 'pending', NULL, NULL, 'pending', '2025-12-15 08:57:35', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(120001, 'HH202512156EF1C0', 60002, 1, 1.00, 0.00, 1.00, 'razorpay', 'pending', NULL, NULL, 'pending', '2025-12-15 09:09:45', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(150001, 'HH202512151BBFD1', 60002, 1, 1.00, 0.00, 1.00, 'razorpay', 'pending', 'order_RrqacBXdPpktIp', NULL, 'pending', '2025-12-15 10:02:44', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(180001, 'HH2025121592AD7A', 60002, 1, 1.00, 0.00, 1.00, 'razorpay', 'failed', 'order_Rrqid4VoDXTNw8', NULL, 'pending', '2025-12-15 10:10:20', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(180002, 'HH20251215238877', 60002, 1, 1.00, 0.00, 1.00, 'razorpay', 'paid', 'order_RrqmR9HTwmzp76', 'pay_RrqmbC95awZRrL', 'delivered', '2025-12-15 10:13:57', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 0.10, '2025-12-27 07:29:46', '2025-12-27 10:38:24', '2025-12-27 10:38:31'),
(210001, 'HH20251215F36042', 60002, 1, 1.00, 0.00, 1.00, 'razorpay', 'pending', 'order_Rrr3ZYcIpdU99y', NULL, 'pending', '2025-12-15 10:30:10', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(240001, 'HH202512156D09F8', 360002, 30001, 1.00, 0.00, 1.00, 'razorpay', 'pending', 'order_RryAHvqFwUTlmb', NULL, 'pending', '2025-12-15 17:27:21', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(240002, 'HH202512150CC722', 360002, 30001, 1.00, 0.00, 1.00, 'razorpay', 'paid', 'order_RryEfDOdvbzyA9', 'pay_RryFBMfWUYfSqS', 'out_for_delivery', '2025-12-15 17:31:31', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 0.10, '2025-12-27 10:42:17', '2025-12-27 10:42:32', NULL),
(270001, 'HH202512169BD94C', 60002, 1, 2.00, 0.00, 2.00, 'razorpay', 'pending', 'order_Rs7h1uzfn0mgzI', NULL, 'pending', '2025-12-16 02:46:36', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(300001, 'HH20251217FB6391', 450002, 60001, 1800.00, 0.00, 1800.00, 'cod', 'pending', NULL, NULL, 'confirmed', '2025-12-17 10:22:26', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-18', 'by 5 PM', NULL, NULL, NULL, NULL, NULL),
(330001, 'HH202512178037FB', 480002, 90001, 5.00, 0.00, 5.00, 'razorpay', 'paid', 'order_RsiqeWHnFGveqK', 'pay_Rsit7KexFNL2G9', 'delivered', '2025-12-17 15:07:22', 1, '2025-12-17 15:10:03', NULL, NULL, NULL, 'upi', NULL, NULL, NULL, NULL, '2025-12-18', 'by 5 PM', 1, 0.50, '2025-12-27 11:47:49', '2025-12-27 11:48:19', '2025-12-27 13:27:57'),
(360001, 'HH202512182B916D', 480002, 90001, 6.00, 0.00, 6.00, 'cod', 'pending', NULL, NULL, 'confirmed', '2025-12-18 14:22:29', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-19', 'by 5 PM', NULL, NULL, NULL, NULL, NULL),
(390001, 'HH202512204BED18', 150002, 120001, 5.00, 0.00, 5.00, 'razorpay', 'pending', 'order_RtxY4y7RlgcEC3', NULL, 'pending', '2025-12-20 18:09:11', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-21', 'by 5 PM', NULL, NULL, NULL, NULL, NULL),
(390002, 'HH202512203367D9', 150002, 120001, 55.00, 0.00, 55.00, 'cod', 'pending', NULL, NULL, 'confirmed', '2025-12-20 18:10:30', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-21', 'by 5 PM', NULL, NULL, NULL, NULL, NULL),
(390003, 'HH20251220D987AD', 150002, 120001, 4.00, 0.00, 4.00, 'razorpay', 'pending', 'order_RtxhVYx8yiR5s1', NULL, 'pending', '2025-12-20 18:18:08', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-21', 'by 5 PM', NULL, NULL, NULL, NULL, NULL),
(420001, 'HH2025122130AE0D', 60002, 1, 5.00, 0.00, 5.00, 'cod', 'pending', NULL, NULL, 'confirmed', '2025-12-21 16:12:22', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-22', 'by 5 PM', NULL, NULL, NULL, NULL, NULL),
(420002, 'HH20251221EB542E', 60002, 1, 5.00, 0.00, 5.00, 'razorpay', 'paid', 'order_RuK6Gk3FbMC0tZ', 'pay_RuKAt0LsQmzhcW', 'processing', '2025-12-21 16:12:49', 1, '2025-12-21 16:18:05', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-22', 'by 5 PM', 1, 0.50, '2025-12-27 07:30:23', '2025-12-27 07:31:13', '2025-12-27 07:31:32'),
(450001, 'HH20251223764609', 7020034431, 150001, 19244.00, 0.00, 19244.00, 'cod', 'pending', NULL, NULL, 'confirmed', '2025-12-23 03:46:02', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-24', 'by 5 PM', NULL, NULL, NULL, NULL, NULL),
(480001, 'HH2025122532322D', 450002, 60001, 169.00, 0.00, 169.00, 'razorpay', 'pending', 'order_RvvsWEUOf3oObF', NULL, 'pending', '2025-12-25 17:49:10', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-26', 'by 5 PM', NULL, NULL, NULL, NULL, NULL),
(510001, 'HH2025122696969F', 450002, 60001, 38147.00, 0.00, 38147.00, 'cod', 'paid', NULL, NULL, 'delivered', '2025-12-26 08:33:00', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-27', 'by 5 PM', 1, 3814.70, '2025-12-27 10:54:13', '2025-12-27 10:55:05', '2025-12-27 13:27:34'),
(540001, 'HH20251226FCF362', 450002, 60001, 149.00, 0.00, 149.00, 'razorpay', 'paid', 'order_RwHM0chAI0f8K9', 'pay_RwHMSQa9tEfCyE', 'processing', '2025-12-26 14:49:39', 1, '2025-12-26 14:50:34', NULL, NULL, NULL, 'upi', NULL, NULL, NULL, NULL, '2025-12-27', 'by 5 PM', NULL, NULL, NULL, NULL, NULL),
(570001, 'HH202512272DB73B', 7507465080, 180001, 4099.00, 100.00, 4199.00, 'cod', 'pending', NULL, NULL, 'confirmed', '2025-12-27 11:07:02', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-01-01', NULL, NULL, NULL, NULL, NULL, NULL),
(570002, 'HH20251227C7C03D', 7507465080, 180001, 160.00, 100.00, 260.00, 'razorpay', 'pending', 'order_Rwc6hqVPdxLLGY', NULL, 'pending', '2025-12-27 11:07:43', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-01-01', NULL, NULL, NULL, NULL, NULL, NULL),
(600001, 'HH2025122705B149', 9595340263, 210001, 139.00, 0.00, 139.00, 'cod', 'pending', NULL, NULL, 'out_for_delivery', '2025-12-27 11:29:39', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-28', 'by 5 PM', 1, 13.90, '2025-12-29 08:51:53', '2025-12-29 08:52:38', NULL),
(630001, 'HH2025122996EB6A', 210002, 240001, 60.00, 0.00, 60.00, 'cod', 'pending', NULL, NULL, 'delivered', '2025-12-29 05:14:36', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-30', 'by 5 PM', 1, 6.00, '2025-12-29 07:34:44', '2025-12-29 07:34:50', '2025-12-29 07:34:53'),
(660001, 'HH20251229A1F32A', 210002, 240001, 2452.00, 0.00, 2452.00, 'razorpay', 'pending', 'order_RxSqVhb0Qxko0r', NULL, 'pending', '2025-12-29 14:43:09', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-30', 'by 5 PM', NULL, NULL, NULL, NULL, NULL);

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

--
-- Dumping data for table `order_items`
--

INSERT INTO `order_items` (`order_item_id`, `order_id`, `listing_id`, `seller_id`, `quantity`, `price`, `item_status`) VALUES
(1, 1, 210103, 1, 1, 1.00, 'pending'),
(30001, 30001, 210103, 1, 1, 1.00, 'pending'),
(30002, 30002, 210103, 1, 1, 1.00, 'pending'),
(30003, 30003, 210103, 1, 1, 1.00, 'pending'),
(60001, 60001, 210103, 1, 1, 1.00, 'pending'),
(90001, 90001, 210103, 1, 1, 1.00, 'pending'),
(120001, 120001, 210103, 1, 1, 1.00, 'pending'),
(150001, 150001, 210103, 1, 1, 1.00, 'pending'),
(180001, 180001, 210103, 1, 1, 1.00, 'pending'),
(180002, 180002, 210103, 1, 1, 1.00, 'pending'),
(210001, 210001, 210103, 1, 1, 1.00, 'pending'),
(240001, 240001, 210103, 1, 1, 1.00, 'pending'),
(240002, 240002, 210103, 1, 1, 1.00, 'pending'),
(270001, 270001, 210103, 1, 2, 1.00, 'pending'),
(300001, 300001, 270102, 390002, 4, 450.00, 'pending'),
(330001, 330001, 752341, 3, 1, 5.00, 'pending'),
(360001, 360001, 602341, 60002, 1, 6.00, 'pending'),
(390001, 390001, 1190678, 60002, 1, 5.00, 'pending'),
(390002, 390002, 1190678, 60002, 1, 55.00, 'pending'),
(390003, 390003, 602341, 60002, 1, 4.00, 'pending'),
(420001, 420001, 752341, 3, 1, 5.00, 'pending'),
(420002, 420002, 752341, 3, 1, 5.00, 'pending'),
(450001, 450001, 11, 9595340263, 1, 18999.00, 'pending'),
(450002, 450001, 15, 9595340263, 1, 199.00, 'pending'),
(450003, 450001, 11, 9595340263, 2, 23.00, 'pending'),
(480001, 480001, 14, 7020034431, 1, 169.00, 'pending'),
(510001, 510001, 11, 9595340263, 2, 18999.00, 'pending'),
(510002, 510001, 15, 9595340263, 1, 149.00, 'pending'),
(540001, 540001, 15, 9595340263, 1, 149.00, 'pending'),
(570001, 570001, 11, 9595340263, 1, 3999.00, 'pending'),
(570002, 570001, 23, 9595340263, 1, 100.00, 'pending'),
(570003, 570002, 23, 9595340263, 1, 100.00, 'pending'),
(570004, 570002, 23, 9595340263, 1, 60.00, 'pending'),
(600001, 600001, 14, 7020034431, 1, 139.00, 'confirmed'),
(630001, 630001, 23, 9595340263, 1, 60.00, 'confirmed'),
(660001, 660001, 11, 9595340263, 1, 2452.00, 'pending');

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

--
-- Dumping data for table `otp_send_logs`
--

INSERT INTO `otp_send_logs` (`id`, `device_id`, `device_name`, `sender_phone`, `recipient_phone`, `otp_code`, `status`, `error_message`, `request_id`, `sent_at`) VALUES
(1, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '211848', 'sent', NULL, 'otp_693bbcd2067bf5_51992157', '2025-12-12 06:57:29'),
(30001, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '317806', 'sent', NULL, 'otp_693bbf47930e00_10776849', '2025-12-12 07:07:56'),
(60001, 'device_4faa492c', 'samsung SM-M346B', NULL, '9860530162', '757243', 'sent', NULL, 'otp_693bcbb1e4cc24_09800553', '2025-12-12 08:00:56'),
(90001, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '400372', 'sent', NULL, 'otp_693bde6573d0b2_45153318', '2025-12-12 09:20:44'),
(120001, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '411519', 'sent', NULL, 'otp_693be1a35abfb1_33510106', '2025-12-12 09:34:35'),
(150001, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '411519', 'sent', NULL, 'otp_693be1a35abfb1_33510106', '2025-12-12 10:08:15'),
(180001, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '983703', 'sent', NULL, 'otp_693c05b54233e5_15011901', '2025-12-12 12:08:26'),
(180002, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '116658', 'sent', NULL, 'otp_693c0592ae6c47_19191856', '2025-12-12 12:09:53'),
(210001, 'device_4faa492c', 'samsung SM-M346B', NULL, '7823080840', '342591', 'sent', NULL, 'otp_693c14e6ded0b2_77736624', '2025-12-12 13:13:17'),
(240001, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '825598', 'sent', NULL, 'otp_693c44d43863f4_99068896', '2025-12-12 16:37:47'),
(240002, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '072272', 'sent', NULL, 'otp_693c473168aef7_11485707', '2025-12-12 16:47:50'),
(270001, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '518364', 'sent', NULL, 'otp_693c4ecb96a880_25007449', '2025-12-12 17:20:19'),
(300001, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '578677', 'sent', NULL, 'otp_693c53c392b893_16018734', '2025-12-12 17:41:30'),
(300002, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '465023', 'sent', NULL, 'otp_693c5456714956_96583533', '2025-12-12 17:43:55'),
(300003, 'device_4faa492c', 'samsung SM-M346B', NULL, '9595340263', '281902', 'sent', NULL, 'otp_693c5470bb7b30_12537620', '2025-12-12 17:44:21'),
(300004, 'device_72b21823', 'samsung SM-M346B', NULL, '9595340263', '324179', 'sent', NULL, 'otp_693c56550d1bb3_40352070', '2025-12-12 17:52:26'),
(300005, 'device_f233fa2b', 'samsung SM-M346B', NULL, '9145216091', '395396', 'sent', NULL, 'otp_693c5696833007_24343165', '2025-12-12 17:53:31'),
(300006, 'device_f233fa2b', 'samsung SM-M346B', NULL, '9145216091', '528968', 'sent', NULL, 'otp_693c56c7b4bba4_52067505', '2025-12-12 17:54:20'),
(300007, 'device_f233fa2b', 'samsung SM-M346B', NULL, '9145216091', '147013', 'sent', NULL, 'otp_693c5700120398_64312814', '2025-12-12 17:55:16'),
(300008, 'device_f233fa2b', 'samsung SM-M346B', NULL, '9595340263', '701351', 'sent', NULL, 'otp_693c574f26a814_15261641', '2025-12-12 17:56:36'),
(330001, 'device_f233fa2b', 'samsung SM-M346B', NULL, '9595340263', '204091', 'sent', NULL, 'otp_693cc8361670e7_11388598', '2025-12-13 01:58:21'),
(330002, 'device_f233fa2b', 'samsung SM-M346B', NULL, '9529239224', '255977', 'sent', NULL, 'otp_693ccc4ee38b45_16876208', '2025-12-13 02:15:49'),
(330003, 'device_f233fa2b', 'samsung SM-M346B', NULL, '9529239224', '908094', 'sent', NULL, 'otp_693ccc77165669_26825742', '2025-12-13 02:16:27'),
(360001, 'device_f233fa2b', 'samsung SM-M346B', NULL, '9595340263', '700510', 'sent', NULL, 'otp_693cec9b2fec56_92168859', '2025-12-13 04:33:38'),
(360002, 'device_f233fa2b', 'samsung SM-M346B', NULL, '9595340263', '006127', 'sent', NULL, 'otp_693cecd5ddf2e7_57194462', '2025-12-13 04:34:35'),
(360003, 'device_f233fa2b', 'samsung SM-M346B', NULL, '9595340263', '156266', 'sent', NULL, 'otp_693ced1f007536_18421421', '2025-12-13 04:35:50'),
(390001, 'device_f233fa2b', 'samsung SM-M346B', NULL, '9595340263', '137886', 'sent', NULL, 'otp_693d03826d4b05_39633762', '2025-12-13 06:11:21'),
(420001, 'device_f233fa2b', 'samsung SM-M346B', NULL, '9595340263', '817882', 'sent', NULL, 'otp_693d48da062fd9_51497896', '2025-12-13 11:07:13'),
(450001, 'device_f233fa2b', 'samsung SM-M346B', NULL, '7823080840', '355997', 'sent', NULL, 'otp_693d8005ba4bd2_48558814', '2025-12-13 15:02:36'),
(480001, 'device_f233fa2b', 'samsung SM-M346B', NULL, '8788428166', '303019', 'sent', NULL, 'otp_693d8c1290d893_50089254', '2025-12-13 15:54:02'),
(510001, 'device_f233fa2b', 'samsung SM-M346B', NULL, '8975332653', '192508', 'sent', NULL, 'otp_693d99b46d5b97_41500169', '2025-12-13 16:52:11'),
(540001, 'device_189fcfbb', 'samsung SM-M346B', NULL, '7428730894', '093608', 'failed', 'Sending SMS message: uid 10329 does not have android.permission.SEND_SMS.', 'otp_693e27e0035ee1_64026740', '2025-12-14 02:58:48'),
(570001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '196056', 'sent', NULL, 'otp_693eec6a6183e1_06133573', '2025-12-14 16:57:21'),
(600001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '596131', 'sent', NULL, 'otp_693f5d2299f959_49175481', '2025-12-15 00:58:17'),
(630001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '8888423121', '036001', 'sent', NULL, 'otp_693f63917b4d72_16318514', '2025-12-15 01:25:44'),
(660001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '595619', 'sent', NULL, 'otp_693f6a1fc4ee86_71395268', '2025-12-15 01:53:42'),
(690001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '8975114674', '940346', 'sent', NULL, 'otp_693f70c574a333_38931816', '2025-12-15 02:22:13'),
(720001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '662050', 'sent', NULL, 'otp_693f763e2375f7_44269208', '2025-12-15 02:45:25'),
(750001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '8975114674', '707939', 'sent', NULL, 'otp_693f7c35127773_23308221', '2025-12-15 03:10:52'),
(780001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9156233733', '031211', 'sent', NULL, 'otp_693fe4fcb626c2_24121307', '2025-12-15 10:37:56'),
(780002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '756276', 'sent', NULL, 'otp_693fe5f44b2c71_48233751', '2025-12-15 10:42:02'),
(810001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '824061', 'sent', NULL, 'otp_693ff8c6c0a3e7_11355266', '2025-12-15 12:02:21'),
(810002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '707847', 'sent', NULL, 'otp_693ff8d4178ab3_21565617', '2025-12-15 12:02:33'),
(840001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '8459545945', '068814', 'sent', NULL, 'otp_69400531086463_94122674', '2025-12-15 12:55:20'),
(870001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '362168', 'sent', NULL, 'otp_69404002eb3910_20190528', '2025-12-15 17:06:18'),
(870002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '8805281502', '621630', 'sent', NULL, 'otp_694040657caa16_86656828', '2025-12-15 17:08:02'),
(900001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '245177', 'sent', NULL, 'otp_6940c85ea6d273_45304188', '2025-12-16 02:48:05'),
(930001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '511130', 'sent', NULL, 'otp_6940d17ec9cfe4_25225173', '2025-12-16 03:27:04'),
(960001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '657288', 'sent', NULL, 'otp_6940ee0eda1c00_68884627', '2025-12-16 05:28:53'),
(990001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '181994', 'sent', NULL, 'otp_6940f6d0a77bc0_45048116', '2025-12-16 06:06:16'),
(1020001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '116804', 'sent', NULL, 'otp_6940f9b3ef0070_17332009', '2025-12-16 06:18:33'),
(1050001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '699712', 'sent', NULL, 'otp_694108a32a88f8_47931561', '2025-12-16 07:22:22'),
(1080001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '647993', 'sent', NULL, 'otp_69413013901327_09704576', '2025-12-16 10:10:34'),
(1080002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '180980', 'sent', NULL, 'otp_694132d61b4db0_01970111', '2025-12-16 10:22:19'),
(1080003, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '858482', 'sent', NULL, 'otp_6941333396f889_95788291', '2025-12-16 10:23:52'),
(1080004, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '699303', 'sent', NULL, 'otp_69413379e99680_06445715', '2025-12-16 10:25:02'),
(1110001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9689969814', '536467', 'sent', NULL, 'otp_69413f840947a8_63376516', '2025-12-16 11:16:26'),
(1140001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9370782939', '663465', 'sent', NULL, 'otp_69414e2c813575_62917869', '2025-12-16 12:18:59'),
(1170001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7823080840', '215265', 'sent', NULL, 'otp_6941592f608107_52762701', '2025-12-16 13:06:00'),
(1200001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9096632830', '776934', 'sent', NULL, 'otp_69418d65542564_85674117', '2025-12-16 16:48:51'),
(1230001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9096632830', '791302', 'sent', NULL, 'otp_694195c1593787_09212046', '2025-12-16 17:24:25'),
(1230002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9096632830', '040251', 'sent', NULL, 'otp_694195c160cbd0_33946521', '2025-12-16 17:24:25'),
(1230003, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9096632830', '600703', 'sent', NULL, 'otp_694195e0537ba9_25135152', '2025-12-16 17:24:53'),
(1260001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '391467', 'sent', NULL, 'otp_69421f8eb47fb2_40376530', '2025-12-17 03:12:22'),
(1260002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9096632830', '250141', 'sent', NULL, 'otp_69421fca1b4e12_12780029', '2025-12-17 03:13:19'),
(1260003, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '731990', 'sent', NULL, 'otp_69422207d82b90_78402752', '2025-12-17 03:22:53'),
(1290001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9096632830', '149913', 'sent', NULL, 'otp_69423b5eed1f06_27374445', '2025-12-17 05:11:19'),
(1320001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '957664', 'sent', NULL, 'otp_69429a315acd40_34931283', '2025-12-17 11:55:37'),
(1350001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '014469', 'sent', NULL, 'otp_6942c6d9b3f988_60730246', '2025-12-17 15:06:12'),
(1350002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '168569', 'sent', NULL, 'otp_6942c8cf00a691_02159937', '2025-12-17 15:14:28'),
(1380001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '163459', 'sent', NULL, 'otp_6942d2ff0ec9d2_32770508', '2025-12-17 15:57:58'),
(1380002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '157302', 'sent', NULL, 'otp_6942d3a14908b0_32851223', '2025-12-17 16:00:40'),
(1380003, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '405833', 'sent', NULL, 'otp_6942d3e3122f90_75370260', '2025-12-17 16:01:46'),
(1380004, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '885988', 'sent', NULL, 'otp_6942d45a09f428_25683191', '2025-12-17 16:03:49'),
(1380005, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '147428', 'sent', NULL, 'otp_6942d4d7b36d34_71622832', '2025-12-17 16:05:50'),
(1410001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '840234', 'sent', NULL, 'otp_6942dae7ce5ea3_59743239', '2025-12-17 16:31:44'),
(1440001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '194683', 'sent', NULL, 'otp_6942ddd976b336_01330967', '2025-12-17 16:44:15'),
(1470001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '616733', 'sent', NULL, 'otp_694372c6782976_04227184', '2025-12-18 03:19:41'),
(1470002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '596690', 'sent', NULL, 'otp_69437480675998_74911851', '2025-12-18 03:27:02'),
(1500001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9096632830', '396244', 'sent', NULL, 'otp_694392eb126b27_87584846', '2025-12-18 05:36:50'),
(1530001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '952429', 'sent', NULL, 'otp_69439683aae965_16033479', '2025-12-18 05:52:12'),
(1530002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '591301', 'sent', NULL, 'otp_694398bd0c67d4_15823788', '2025-12-18 06:01:39'),
(1530003, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '525892', 'sent', NULL, 'otp_694399de6dfe16_94993097', '2025-12-18 06:06:27'),
(1560001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9529995115', '671465', 'sent', NULL, 'otp_6943cb5da8fb88_69220420', '2025-12-18 09:37:40'),
(1590001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '906063', 'sent', NULL, 'otp_6943d5a5787e69_17319730', '2025-12-18 10:21:43'),
(1620001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '626862', 'sent', NULL, 'otp_69440531c1dfa0_76004362', '2025-12-18 13:44:25'),
(1650001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '490716', 'sent', NULL, 'otp_694411a43cb9a2_74177555', '2025-12-18 14:37:32'),
(1680001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '528987', 'sent', NULL, 'otp_694419f1395971_84324801', '2025-12-18 15:13:02'),
(1680002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '405363', 'sent', NULL, 'otp_69441a0aca4992_56930927', '2025-12-18 15:13:20'),
(1710001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '8888423121', '741394', 'sent', NULL, 'otp_69452e735eede5_69969980', '2025-12-19 10:52:42'),
(1740001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '8888423121', '677651', 'sent', NULL, 'otp_69452fce194190_94318630', '2025-12-19 10:58:27'),
(1740002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '8888423121', '134591', 'sent', NULL, 'otp_69452fd172c6f4_39771876', '2025-12-19 10:58:30'),
(1770001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '696275', 'sent', NULL, 'otp_69469078eedae8_33181934', '2025-12-20 12:03:15'),
(1800001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '448435', 'sent', NULL, 'otp_6946d8cfcee1a0_97639618', '2025-12-20 17:11:52'),
(1800002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '606182', 'sent', NULL, 'otp_6946dc58559338_64743054', '2025-12-20 17:26:54'),
(1830001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9145216091', '821035', 'sent', NULL, 'otp_6946e5d6304656_98618291', '2025-12-20 18:07:26'),
(1860001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '174149', 'sent', NULL, 'otp_69474fc5169714_13440305', '2025-12-21 01:39:24'),
(1890001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9096632830', '296686', 'sent', NULL, 'otp_694756c13336d7_07048391', '2025-12-21 02:09:12'),
(1920001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9096632830', '314764', 'sent', NULL, 'otp_6947880774dae5_79468735', '2025-12-21 05:39:26'),
(1950001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '952141', 'sent', NULL, 'otp_69479c9e398367_39102565', '2025-12-21 07:07:18'),
(1980001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9096632830', '445631', 'sent', NULL, 'otp_6947be6acb9cc8_63390537', '2025-12-21 09:31:30'),
(2010001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7559262920', '188288', 'sent', NULL, 'otp_6947f311286300_18478756', '2025-12-21 13:16:10'),
(2040001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '069840', 'sent', NULL, 'otp_6947ffa1554a87_55312808', '2025-12-21 14:09:44'),
(2070001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '766214', 'sent', NULL, 'otp_6948354bc5c909_60142581', '2025-12-21 17:58:43'),
(2070002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '594608', 'sent', NULL, 'otp_694837bd69a218_67570865', '2025-12-21 18:09:07'),
(2100001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '530520', 'sent', NULL, 'otp_694900ea12c670_20763056', '2025-12-22 08:27:29'),
(2130001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '711810', 'sent', NULL, 'otp_69491ddd79fb14_04505803', '2025-12-22 10:31:02'),
(2160001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '034519', 'sent', NULL, 'otp_69493e1dccbe75_19280690', '2025-12-22 12:48:37'),
(2190001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '544778', 'sent', NULL, 'otp_6949412ca38665_39925670', '2025-12-22 13:01:37'),
(2190002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7823080840', '712606', 'sent', NULL, 'otp_6949485f54baf9_96101887', '2025-12-22 13:32:22'),
(2220001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '577961', 'sent', NULL, 'otp_694a1041357cb9_95133284', '2025-12-23 03:45:12'),
(2250001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '872516', 'sent', NULL, 'otp_694aa3d3396159_19101346', '2025-12-23 14:14:50'),
(2250002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '782006', 'sent', NULL, 'otp_694aa48dac6b33_03762971', '2025-12-23 14:17:55'),
(2280001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9595340263', '537699', 'sent', NULL, 'otp_694aabc00ece35_16700904', '2025-12-23 14:48:39'),
(2310001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '021876', 'sent', NULL, 'otp_694abbc90f3c83_98848328', '2025-12-23 15:57:04'),
(2340001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '9860530162', '730134', 'sent', NULL, 'otp_694b7e128d6800_56150199', '2025-12-24 05:46:01'),
(2370001, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '564388', 'sent', NULL, 'otp_694bb68275b692_71882789', '2025-12-24 09:46:51'),
(2370002, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '588442', 'sent', NULL, 'otp_694bb6df508310_46142021', '2025-12-24 09:48:20'),
(2370003, 'device_b65ade7c', 'samsung SM-M346B', NULL, '7020034431', '966782', 'sent', NULL, 'otp_694bb74a719ee6_49932198', '2025-12-24 09:50:07'),
(2400001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9096632830', '695885', 'failed', 'Sending SMS message: uid 10329 does not have android.permission.SEND_SMS.', 'otp_694d78bce44a18_04419786', '2025-12-25 17:47:52'),
(2400002, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9096632830', '711826', 'sent', NULL, 'otp_694d78d37abd46_28444408', '2025-12-25 17:48:10'),
(2430001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '680044', 'sent', NULL, 'otp_694df9e46b8296_53406189', '2025-12-26 02:58:51'),
(2460001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '657441', 'sent', NULL, 'otp_694e2216b20ac7_95231257', '2025-12-26 05:50:22'),
(2490001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9096632830', '497970', 'sent', NULL, 'otp_694e47ce2a1635_28638268', '2025-12-26 08:31:18'),
(2520001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9623347135', '626760', 'sent', NULL, 'otp_694e99207f7043_09192614', '2025-12-26 14:18:16'),
(2520002, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9890038134', '633566', 'sent', NULL, 'otp_694e994d815cd2_67474275', '2025-12-26 14:18:59'),
(2550001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9325872359', '773039', 'sent', NULL, 'otp_694eac59912854_24595011', '2025-12-26 15:40:16'),
(2580001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9689969814', '257834', 'sent', NULL, 'otp_694f7c85a17771_20722205', '2025-12-27 06:28:36'),
(2580002, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9689969814', '366097', 'sent', NULL, 'otp_694f7ca7153355_29690958', '2025-12-27 06:29:00'),
(2580003, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9359614612', '184979', 'sent', NULL, 'otp_694f7e68772881_02290676', '2025-12-27 06:36:36'),
(2610001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '623740', 'sent', NULL, 'otp_694f876a04dd26_25266904', '2025-12-27 07:14:57'),
(2640001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '579437', 'sent', NULL, 'otp_694f895e06dc39_14532512', '2025-12-27 07:23:15'),
(2640002, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '506631', 'sent', NULL, 'otp_694f89dd6e9dc8_18850627', '2025-12-27 07:25:23'),
(2640003, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '023022', 'sent', NULL, 'otp_694f8a96577f60_85211236', '2025-12-27 07:28:27'),
(2670001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9145216091', '830922', 'sent', NULL, 'otp_694f9511429829_99089686', '2025-12-27 08:13:12'),
(2700001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '169309', 'sent', NULL, 'otp_694fb1a84b11a3_56551888', '2025-12-27 10:15:11'),
(2700002, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '594729', 'sent', NULL, 'otp_694fb1bb1ee8e6_78340213', '2025-12-27 10:15:28'),
(2730001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '7007465080', '947786', 'sent', NULL, 'otp_694fbd148a61c2_40696311', '2025-12-27 11:03:55'),
(2730002, 'device_784cdb1f', 'samsung SM-M346B', NULL, '7507465080', '211928', 'sent', NULL, 'otp_694fbd1fe00277_26369625', '2025-12-27 11:04:05'),
(2760001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '090336', 'sent', NULL, 'otp_694fdf1507e3a0_99823397', '2025-12-27 13:29:00'),
(2760002, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '489166', 'sent', NULL, 'otp_694fdfcb180688_67776368', '2025-12-27 13:32:00'),
(2790001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '8788428166', '326800', 'sent', NULL, 'otp_69500cded1fe34_21767734', '2025-12-27 16:44:22'),
(2790002, 'device_784cdb1f', 'samsung SM-M346B', NULL, '8788428166', '252192', 'sent', NULL, 'otp_69500d03924c06_58087598', '2025-12-27 16:44:57'),
(2820001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '192392', 'sent', NULL, 'otp_695142011be889_93286695', '2025-12-28 14:43:22'),
(2820002, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '830184', 'sent', NULL, 'otp_69514224b217e5_79089753', '2025-12-28 14:43:54'),
(2850001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '525447', 'sent', NULL, 'otp_69516488b8bd52_74508612', '2025-12-28 17:10:40'),
(2880001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '9595340263', '263216', 'sent', NULL, 'otp_69517d1b9c43a8_74070910', '2025-12-28 18:55:30'),
(2910001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '7709082672', '140337', 'sent', NULL, 'otp_69520310cc7088_19480869', '2025-12-29 04:27:04'),
(2910002, 'device_784cdb1f', 'samsung SM-M346B', NULL, '8669702031', '866079', 'sent', NULL, 'otp_6952106d6621e0_67576822', '2025-12-29 05:24:05'),
(2940001, 'device_784cdb1f', 'samsung SM-M346B', NULL, '8669702031', '507242', 'sent', NULL, 'otp_6952a745d24916_44252174', '2025-12-29 16:07:42'),
(2940002, 'device_784cdb1f', 'samsung SM-M346B', NULL, '8767507375', '794924', 'sent', NULL, 'otp_6952a82b6d7664_04959238', '2025-12-29 16:11:34');

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

--
-- Dumping data for table `otp_verifications`
--

INSERT INTO `otp_verifications` (`id`, `phone`, `otp_code`, `purpose`, `attempts`, `expires_at`, `created_at`) VALUES
(1, '8999830628', '$2y$10$Xdz3qnYuO6/BNeVE2aQ27eGwpt8eDmbypsRPiGT9DkERuHzLprwku', 'signup', 0, '2025-12-11 16:09:43', '2025-12-11 16:04:47'),
(60003, '9595929372', '$2y$10$zhv.xHShE3.qhDo7qzZUXe5bGvWHt2OVXLXXgjgacjmtpj0OcaRWe', 'signup', 0, '2025-12-12 03:54:19', '2025-12-12 03:49:22'),
(690001, '8975332653', '$2y$10$XATX0JLsdZCnMNd7.zuxnuQ1rQ8oB7keTJFvJItKaPGrgE73ttk.G', 'signup', 0, '2025-12-13 16:57:03', '2025-12-13 16:52:07'),
(720001, '7428730894', '$2y$10$IcIk6tnWl69cHkfUjQT6q.vk.NNH0zI.BuJmzoldF65EeMIW6LU6e', 'signup', 2, '2025-12-14 03:03:39', '2025-12-14 02:58:42'),
(2580003, '7020034431', '$2y$10$rtdtQEn.yIVMUgGOfWtfOeRnIrY6JdTC08fb1DYfNcAxq5mL6vlcu', 'reset_password', 1, '2025-12-24 09:55:01', '2025-12-24 09:50:05'),
(2700001, '9096632830', '$2y$10$DzgfMNLI91ysl2u0TnmJHeEeTHUt9cepFzxWHDUsufZCnoUIhKCqu', 'signup', 0, '2025-12-26 08:36:09', '2025-12-26 08:31:13'),
(2940001, '7007465080', '$2y$10$bVx9A09t/sWz.8oKAwbYpuxViWt8q8HKHcKJnXauKGJrHNiH5eoGe', 'signup', 0, '2025-12-27 11:08:47', '2025-12-27 11:03:51');

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

--
-- Dumping data for table `reviews`
--

INSERT INTO `reviews` (`review_id`, `listing_id`, `product_id`, `old_product_id`, `reviewer_id`, `rating`, `title`, `content`, `images`, `is_approved`, `approval_status`, `moderated_by`, `moderated_at`, `rejection_reason`, `seller_response`, `seller_response_at`, `helpful_count`, `is_verified_purchase`, `is_featured`, `created_at`, `updated_at`) VALUES
(1, 1, 1, NULL, 7020034431, 5, 'Excellent Service!', 'Ramesh ji came on time and fixed all electrical issues in my home. Very professional and reasonable rates. Highly recommended!', NULL, 1, 'approved', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, '2025-12-11 17:42:44', '2025-12-23 03:52:42'),
(2, 1, NULL, NULL, 9595340263, 4, 'Good Work', 'Good electrician. Completed the work properly. Slightly late but overall satisfied.', NULL, 1, 'approved', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, '2025-12-16 17:42:44', '2025-12-21 17:42:44'),
(3, 3, NULL, NULL, 7020034431, 5, 'Beautiful Furniture', 'Got modular kitchen done from Vishwakarma Furniture. Amazing quality and design. Worth every rupee!', NULL, 1, 'approved', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, '2025-12-14 17:42:44', '2025-12-21 17:42:44'),
(4, 7, NULL, NULL, 9595340263, 5, 'Best Makeup Artist', 'Lakshmi madam did bridal makeup for my sister. Everyone praised the look. Very talented!', NULL, 1, 'approved', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, '2025-12-18 17:42:44', '2025-12-21 17:42:44'),
(5, 12, NULL, NULL, 7020034431, 4, 'Tasty Food', 'Authentic Maharashtrian taste. Misal Pav is amazing here. Clean restaurant with good service.', NULL, 1, 'approved', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, '2025-12-13 17:42:44', '2025-12-21 17:42:44'),
(6, 13, NULL, NULL, 9595340263, 5, 'Caring Doctor', 'Dr. Patil is very experienced and caring. Explains everything properly. Our family doctor for years.', NULL, 1, 'approved', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, '2025-12-19 17:42:44', '2025-12-21 17:42:44'),
(7, 11, NULL, NULL, 7020034431, 4, 'Good Electronics Shop', 'Bought TV from Sharma Electronics. Good variety, reasonable prices. Installation team came on same day.', NULL, 1, 'approved', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, '2025-12-15 17:42:44', '2025-12-21 17:42:44'),
(8, 10, NULL, NULL, 9595340263, 5, 'Amazing Photography', 'Royal Wedding Photography captured our wedding beautifully. The drone shots were incredible!', NULL, 1, 'approved', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, '2025-12-17 17:42:44', '2025-12-21 17:42:44'),
(9, 5, NULL, NULL, 7020034431, 5, 'Quick AC Repair', 'My AC was not cooling. Cool Tech fixed it within an hour. Gas filling done at fair price.', NULL, 1, 'approved', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, '2025-12-12 17:42:44', '2025-12-21 17:42:44'),
(10, 14, NULL, NULL, 9595340263, 4, 'Good Kirana Store', 'Fresh items, good quality. Home delivery is convenient. Prices are reasonable.', NULL, 1, 'approved', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, '2025-12-20 17:42:44', '2025-12-21 17:42:44'),
(12, NULL, 20, NULL, 7020034431, 5, 'excellent', NULL, NULL, 1, 'approved', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, '2025-12-23 04:45:42', '2025-12-23 04:45:42'),
(13, NULL, 1, NULL, 8669702031, 5, 'i want to buy this', NULL, NULL, 1, 'approved', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, '2025-12-29 05:24:50', '2025-12-29 05:24:50');

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

--
-- Dumping data for table `services_listings`
--

INSERT INTO `services_listings` (`listing_id`, `service_type`, `experience_years`, `availability`, `service_area_radius_km`, `hourly_rate`, `certifications`, `custom_attributes`, `price_min`, `price_max`) VALUES
(1, 'one_time', 15, 'immediate', NULL, 300.00, NULL, NULL, 500.00, 1000.00),
(2, 'one_time', 12, 'immediate', NULL, 250.00, NULL, NULL, NULL, NULL),
(3, 'one_time', 20, 'scheduled', NULL, 400.00, NULL, NULL, NULL, NULL),
(4, 'one_time', 5, 'scheduled', NULL, 200.00, NULL, NULL, NULL, NULL),
(5, 'one_time', 8, 'immediate', NULL, 350.00, NULL, NULL, NULL, NULL),
(6, 'one_time', 10, 'scheduled', NULL, 280.00, NULL, NULL, NULL, NULL),
(7, 'one_time', 8, 'scheduled', NULL, 500.00, NULL, NULL, NULL, NULL),
(8, 'recurring', 12, 'scheduled', NULL, 200.00, NULL, NULL, 500.00, 3000.00),
(9, 'one_time', 7, 'scheduled', NULL, 1500.00, NULL, NULL, NULL, NULL),
(10, 'one_time', 10, 'scheduled', NULL, 1000.00, NULL, NULL, NULL, NULL),
(26, 'one_time', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(29, 'one_time', 10, NULL, NULL, NULL, NULL, NULL, 500.00, 1000.00);

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

--
-- Dumping data for table `service_pincodes`
--

INSERT INTO `service_pincodes` (`pincode`, `city_name`, `state`, `delivery_days`, `delivery_time`, `is_serviceable`, `created_at`) VALUES
('431501', 'Hingoli City', 'Maharashtra', 0, '5 PM', 1, '2025-12-16 05:07:54'),
('431502', 'Basmat', 'Maharashtra', 1, '5 PM', 1, '2025-12-16 05:07:54'),
('431503', 'Kalamnuri', 'Maharashtra', 1, '5 PM', 1, '2025-12-16 05:07:54'),
('431513', 'Hingoli', 'Maharashtra', 0, '5 PM', 1, '2025-12-16 05:07:54');

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

--
-- Dumping data for table `settings`
--

INSERT INTO `settings` (`id`, `setting_key`, `setting_value`, `created_at`, `updated_at`) VALUES
(1, 'auto_moderation_products', 'false', '2025-12-29 06:35:55', '2025-12-29 12:02:13'),
(2, 'auto_moderation_listings', 'false', '2025-12-29 06:35:55', '2025-12-29 12:16:54');

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

--
-- Dumping data for table `shop_categories`
--

INSERT INTO `shop_categories` (`id`, `parent_id`, `level`, `name`, `name_mr`, `slug`, `icon`, `color`, `image_url`, `sort_order`, `is_active`, `product_count`, `created_at`, `updated_at`) VALUES
(1, NULL, 1, 'Fruits & Vegetables', 'फळे आणि भाज्या', 'fruits-vegetables', 'eco', '#4CAF50', 'https://cdn-icons-png.flaticon.com/128/2153/2153786.png', 1, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:18'),
(2, NULL, 1, 'Dairy & Breakfast', 'दूध आणि नाश्ता', 'dairy-breakfast', 'egg_alt', '#2196F3', 'https://cdn-icons-png.flaticon.com/128/3050/3050158.png', 2, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:18'),
(3, NULL, 1, 'Atta, Rice & Dal', 'पीठ, तांदूळ आणि डाळ', 'atta-rice-dal', 'grain', '#FF9800', 'https://cdn-icons-png.flaticon.com/128/3082/3082045.png', 3, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:18'),
(4, NULL, 1, 'Masala & Dry Fruits', 'मसाले आणि सुकामेवा', 'masala-dry-fruits', 'spa', '#795548', 'https://cdn-icons-png.flaticon.com/128/2515/2515183.png', 4, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:18'),
(5, NULL, 1, 'Snacks & Packaged', 'नाश्ता आणि पॅकेज्ड', 'snacks-packaged', 'cookie', '#E91E63', 'https://cdn-icons-png.flaticon.com/128/2553/2553691.png', 5, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:19'),
(6, NULL, 1, 'Beverages', 'पेये', 'beverages', 'local_cafe', '#00BCD4', 'https://cdn-icons-png.flaticon.com/128/3050/3050130.png', 6, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:19'),
(7, NULL, 1, 'Mobile & Accessories', 'मोबाईल आणि अॅक्सेसरीज', 'mobile-accessories', 'smartphone', '#9C27B0', 'https://cdn-icons-png.flaticon.com/128/2586/2586488.png', 7, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:19'),
(8, NULL, 1, 'Electronics & Appliances', 'इलेक्ट्रॉनिक्स', 'electronics-appliances', 'devices', '#3F51B5', 'https://cdn-icons-png.flaticon.com/128/3659/3659899.png', 8, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:19'),
(9, NULL, 1, 'Home & Kitchen', 'घर आणि किचन', 'home-kitchen', 'kitchen', '#607D8B', 'https://cdn-icons-png.flaticon.com/128/3144/3144456.png', 9, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:19'),
(10, NULL, 1, 'Fashion & Apparel', 'फॅशन आणि कपडे', 'fashion-apparel', 'checkroom', '#F44336', 'https://cdn-icons-png.flaticon.com/128/1078/1078360.png', 10, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:19'),
(11, NULL, 1, 'Personal Care', 'वैयक्तिक काळजी', 'personal-care', 'face_retouching_natural', '#FF5722', 'https://cdn-icons-png.flaticon.com/128/3163/3163186.png', 11, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:19'),
(12, NULL, 1, 'Pharmacy & Wellness', 'फार्मसी आणि आरोग्य', 'pharmacy-wellness', 'medication', '#8BC34A', 'https://cdn-icons-png.flaticon.com/128/2376/2376100.png', 12, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:19'),
(13, NULL, 1, 'Baby & Kids', 'बेबी आणि मुले', 'baby-kids', 'child_care', '#FFEB3B', 'https://cdn-icons-png.flaticon.com/128/2553/2553659.png', 13, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:19'),
(14, NULL, 1, 'Gifts & Seasonal', 'भेटवस्तू', 'gifts-seasonal', 'card_giftcard', '#673AB7', 'https://cdn-icons-png.flaticon.com/128/1139/1139982.png', 14, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:20'),
(15, NULL, 1, 'Pet Care', 'पाळीव प्राणी', 'pet-care', 'pets', '#00BFA5', 'https://cdn-icons-png.flaticon.com/128/194/194279.png', 15, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:20'),
(16, NULL, 1, 'Stationery & Books', 'स्टेशनरी आणि पुस्तके', 'stationery-books', 'menu_book', '#1976D2', 'https://cdn-icons-png.flaticon.com/128/167/167755.png', 16, 1, 0, '2025-12-27 17:32:02', '2025-12-28 19:00:20'),
(17, 1, 2, 'Fresh Vegetables', 'ताज्या भाज्या', 'fresh-vegetables', 'grass', NULL, 'https://cdn-icons-png.flaticon.com/128/2153/2153788.png', 1, 1, 0, '2025-12-27 17:32:03', '2025-12-28 19:02:25'),
(18, 1, 2, 'Fresh Fruits', 'ताजी फळे', 'fresh-fruits', 'nutrition', NULL, 'https://cdn-icons-png.flaticon.com/128/415/415733.png', 2, 1, 0, '2025-12-27 17:32:03', '2025-12-28 19:02:25'),
(19, 1, 2, 'Herbs & Sprouts', 'औषधी वनस्पती', 'herbs-sprouts', 'spa', NULL, 'https://cdn-icons-png.flaticon.com/128/2909/2909841.png', 3, 1, 0, '2025-12-27 17:32:03', '2025-12-28 19:02:25'),
(20, 1, 2, 'Organic', 'ऑर्गेनिक', 'organic', 'eco', NULL, 'https://cdn-icons-png.flaticon.com/128/1728/1728788.png', 4, 1, 0, '2025-12-27 17:32:03', '2025-12-28 19:02:25'),
(21, 2, 2, 'Milk & Curd', 'दूध आणि दही', 'milk-curd', 'water_drop', NULL, 'https://cdn-icons-png.flaticon.com/128/3050/3050158.png', 1, 1, 0, '2025-12-27 17:32:04', '2025-12-28 19:02:25'),
(22, 2, 2, 'Paneer & Cheese', 'पनीर आणि चीज', 'paneer-cheese', 'lunch_dining', NULL, 'https://cdn-icons-png.flaticon.com/128/5787/5787100.png', 2, 1, 0, '2025-12-27 17:32:04', '2025-12-28 19:02:26'),
(23, 2, 2, 'Butter & Ghee', 'लोणी आणि तूप', 'butter-ghee', 'bakery_dining', NULL, 'https://cdn-icons-png.flaticon.com/128/5356/5356361.png', 3, 1, 0, '2025-12-27 17:32:04', '2025-12-28 19:02:26'),
(24, 2, 2, 'Eggs', 'अंडी', 'eggs', 'egg', NULL, 'https://cdn-icons-png.flaticon.com/128/2515/2515212.png', 4, 1, 0, '2025-12-27 17:32:04', '2025-12-28 19:02:26'),
(25, 2, 2, 'Bread & Bakery', 'ब्रेड आणि बेकरी', 'bread-bakery', 'bakery_dining', NULL, 'https://cdn-icons-png.flaticon.com/128/3081/3081986.png', 5, 1, 0, '2025-12-27 17:32:04', '2025-12-28 19:02:26'),
(26, 3, 2, 'Atta & Flours', 'पीठ', 'atta-flours', 'grain', NULL, 'https://cdn-icons-png.flaticon.com/128/3082/3082045.png', 1, 1, 0, '2025-12-27 17:32:05', '2025-12-28 19:02:26'),
(27, 3, 2, 'Rice', 'तांदूळ', 'rice', 'rice_bowl', NULL, 'https://cdn-icons-png.flaticon.com/128/3174/3174880.png', 2, 1, 0, '2025-12-27 17:32:05', '2025-12-28 19:02:26'),
(28, 3, 2, 'Dals & Pulses', 'डाळी', 'dals-pulses', 'grain', NULL, 'https://cdn-icons-png.flaticon.com/128/2515/2515207.png', 3, 1, 0, '2025-12-27 17:32:05', '2025-12-28 19:02:26'),
(29, 4, 2, 'Spices & Masalas', 'मसाले', 'spices-masalas', 'spa', NULL, 'https://cdn-icons-png.flaticon.com/128/2515/2515183.png', 1, 1, 0, '2025-12-27 17:32:06', '2025-12-28 19:02:26'),
(30, 4, 2, 'Salt, Sugar & Jaggery', 'मीठ, साखर आणि गूळ', 'salt-sugar-jaggery', 'grain', NULL, 'https://cdn-icons-png.flaticon.com/128/2909/2909808.png', 2, 1, 0, '2025-12-27 17:32:06', '2025-12-28 19:02:26'),
(31, 4, 2, 'Dry Fruits & Nuts', 'सुकामेवा', 'dry-fruits-nuts', 'nutrition', NULL, 'https://cdn-icons-png.flaticon.com/128/2224/2224205.png', 3, 1, 0, '2025-12-27 17:32:06', '2025-12-28 19:02:26'),
(32, 4, 2, 'Papad, Pickle & Chutney', 'पापड, लोणचे', 'papad-pickle', 'restaurant', NULL, 'https://cdn-icons-png.flaticon.com/128/2515/2515236.png', 4, 1, 0, '2025-12-27 17:32:06', '2025-12-28 19:02:26'),
(33, 5, 2, 'Chips & Namkeen', 'चिप्स आणि नमकीन', 'chips-namkeen', 'cookie', NULL, 'https://cdn-icons-png.flaticon.com/128/2553/2553691.png', 1, 1, 0, '2025-12-27 17:32:07', '2025-12-28 19:02:27'),
(34, 5, 2, 'Biscuits & Cookies', 'बिस्किटे', 'biscuits-cookies', 'cookie', NULL, 'https://cdn-icons-png.flaticon.com/128/541/541732.png', 2, 1, 0, '2025-12-27 17:32:07', '2025-12-28 19:02:27'),
(35, 5, 2, 'Chocolates & Candies', 'चॉकलेट आणि मिठाई', 'chocolates-candies', 'cake', NULL, 'https://cdn-icons-png.flaticon.com/128/3020/3020693.png', 3, 1, 0, '2025-12-27 17:32:07', '2025-12-28 19:02:27'),
(36, 5, 2, 'Instant Noodles & Pasta', 'नूडल्स आणि पास्ता', 'noodles-pasta', 'ramen_dining', NULL, 'https://cdn-icons-png.flaticon.com/128/1046/1046887.png', 4, 1, 0, '2025-12-27 17:32:07', '2025-12-28 19:02:27'),
(37, 5, 2, 'Ready to Eat', 'रेडी टू ईट', 'ready-to-eat', 'dinner_dining', NULL, 'https://cdn-icons-png.flaticon.com/128/857/857681.png', 5, 1, 0, '2025-12-27 17:32:07', '2025-12-28 19:02:27'),
(38, 5, 2, 'Breakfast Cereals', 'ब्रेकफास्ट सीरियल्स', 'breakfast-cereals', 'breakfast_dining', NULL, 'https://cdn-icons-png.flaticon.com/128/3050/3050266.png', 6, 1, 0, '2025-12-27 17:32:07', '2025-12-28 19:02:27'),
(39, 6, 2, 'Tea & Coffee', 'चहा आणि कॉफी', 'tea-coffee', 'local_cafe', NULL, 'https://cdn-icons-png.flaticon.com/128/924/924514.png', 1, 1, 0, '2025-12-27 17:32:08', '2025-12-28 19:02:27'),
(40, 6, 2, 'Cold Drinks & Juices', 'कोल्ड ड्रिंक्स आणि ज्यूस', 'cold-drinks-juices', 'local_drink', NULL, 'https://cdn-icons-png.flaticon.com/128/3050/3050130.png', 2, 1, 0, '2025-12-27 17:32:08', '2025-12-28 19:02:27'),
(41, 6, 2, 'Health Drinks', 'हेल्थ ड्रिंक्स', 'health-drinks', 'sports_bar', NULL, 'https://cdn-icons-png.flaticon.com/128/2405/2405479.png', 3, 1, 0, '2025-12-27 17:32:08', '2025-12-28 19:02:27'),
(42, 6, 2, 'Water & Soda', 'पाणी आणि सोडा', 'water-soda', 'water_drop', NULL, 'https://cdn-icons-png.flaticon.com/128/824/824239.png', 4, 1, 0, '2025-12-27 17:32:08', '2025-12-28 19:02:27'),
(43, 7, 2, 'Mobile Phones', 'मोबाईल फोन', 'mobile-phones', 'smartphone', NULL, 'https://cdn-icons-png.flaticon.com/128/2586/2586488.png', 1, 1, 0, '2025-12-27 17:32:09', '2025-12-28 19:02:56'),
(44, 7, 2, 'Chargers & Cables', 'चार्जर आणि केबल्स', 'chargers-cables', 'cable', NULL, 'https://cdn-icons-png.flaticon.com/128/2888/2888643.png', 2, 1, 0, '2025-12-27 17:32:09', '2025-12-28 19:02:56'),
(45, 7, 2, 'Power Banks', 'पॉवर बँक', 'power-banks', 'battery_charging_full', NULL, 'https://cdn-icons-png.flaticon.com/128/3659/3659832.png', 3, 1, 0, '2025-12-27 17:32:09', '2025-12-28 19:02:56'),
(46, 7, 2, 'Covers & Cases', 'कव्हर आणि केस', 'covers-cases', 'phone_android', NULL, 'https://cdn-icons-png.flaticon.com/128/545/545245.png', 4, 1, 0, '2025-12-27 17:32:09', '2025-12-28 19:02:57'),
(47, 7, 2, 'Earphones & Headphones', 'इअरफोन आणि हेडफोन', 'earphones-headphones', 'headphones', NULL, 'https://cdn-icons-png.flaticon.com/128/2888/2888570.png', 5, 1, 0, '2025-12-27 17:32:09', '2025-12-28 19:02:57'),
(48, 7, 2, 'Screen Protectors', 'स्क्रीन प्रोटेक्टर', 'screen-protectors', 'phone_android', NULL, 'https://cdn-icons-png.flaticon.com/128/545/545245.png', 6, 1, 0, '2025-12-27 17:32:09', '2025-12-28 19:02:57'),
(49, 7, 2, 'Mobile Stands', 'मोबाईल स्टँड', 'mobile-stands', 'phone_android', NULL, 'https://cdn-icons-png.flaticon.com/128/2888/2888634.png', 7, 1, 0, '2025-12-27 17:32:09', '2025-12-28 19:02:57'),
(50, 8, 2, 'TV & Audio', 'टीव्ही आणि ऑडिओ', 'tv-audio', 'tv', NULL, 'https://cdn-icons-png.flaticon.com/128/2965/2965278.png', 1, 1, 0, '2025-12-27 17:32:09', '2025-12-28 19:02:57'),
(51, 8, 2, 'Home Appliances', 'घरगुती उपकरणे', 'home-appliances', 'home', NULL, 'https://cdn-icons-png.flaticon.com/128/2553/2553617.png', 2, 1, 0, '2025-12-27 17:32:09', '2025-12-28 19:02:57'),
(52, 8, 2, 'Kitchen Appliances', 'किचन उपकरणे', 'kitchen-appliances', 'blender', NULL, 'https://cdn-icons-png.flaticon.com/128/2920/2920346.png', 3, 1, 0, '2025-12-27 17:32:09', '2025-12-28 19:02:57'),
(53, 8, 2, 'Computer & Laptop', 'कॉम्प्युटर आणि लॅपटॉप', 'computer-laptop', 'laptop', NULL, 'https://cdn-icons-png.flaticon.com/128/689/689396.png', 4, 1, 0, '2025-12-27 17:32:09', '2025-12-28 19:02:57'),
(54, 8, 2, 'Cameras & Printers', 'कॅमेरा आणि प्रिंटर', 'cameras-printers', 'camera_alt', NULL, 'https://cdn-icons-png.flaticon.com/128/2956/2956744.png', 5, 1, 0, '2025-12-27 17:32:09', '2025-12-28 19:02:57'),
(55, 9, 2, 'Cookware', 'भांडी', 'cookware', 'soup_kitchen', NULL, 'https://cdn-icons-png.flaticon.com/128/1046/1046874.png', 1, 1, 0, '2025-12-27 17:32:10', '2025-12-28 19:02:57'),
(56, 9, 2, 'Kitchen Storage', 'किचन स्टोरेज', 'kitchen-storage', 'inventory_2', NULL, 'https://cdn-icons-png.flaticon.com/128/2331/2331716.png', 2, 1, 0, '2025-12-27 17:32:10', '2025-12-28 19:02:58'),
(57, 9, 2, 'Dinnerware', 'जेवणाची भांडी', 'dinnerware', 'dinner_dining', NULL, 'https://cdn-icons-png.flaticon.com/128/1046/1046845.png', 3, 1, 0, '2025-12-27 17:32:10', '2025-12-28 19:02:58'),
(58, 9, 2, 'Cleaning Supplies', 'साफसफाई', 'cleaning-supplies', 'cleaning_services', NULL, 'https://cdn-icons-png.flaticon.com/128/2553/2553601.png', 4, 1, 0, '2025-12-27 17:32:10', '2025-12-28 19:02:58'),
(59, 9, 2, 'Bathroom Accessories', 'बाथरूम अॅक्सेसरीज', 'bathroom-accessories', 'bathroom', NULL, 'https://cdn-icons-png.flaticon.com/128/1752/1752675.png', 5, 1, 0, '2025-12-27 17:32:10', '2025-12-28 19:02:58'),
(60, 9, 2, 'Pooja Items', 'पूजा साहित्य', 'pooja-items', 'self_improvement', NULL, 'https://cdn-icons-png.flaticon.com/128/1041/1041916.png', 6, 1, 0, '2025-12-27 17:32:10', '2025-12-28 19:02:58'),
(61, 9, 2, 'Home Decor', 'होम डेकोर', 'home-decor', 'chair', NULL, 'https://cdn-icons-png.flaticon.com/128/2250/2250258.png', 7, 1, 0, '2025-12-27 17:32:10', '2025-12-28 19:02:58'),
(62, 10, 2, 'Men\'s Wear', 'पुरुषांचे कपडे', 'mens-wear', 'checkroom', NULL, 'https://cdn-icons-png.flaticon.com/128/892/892458.png', 1, 1, 0, '2025-12-27 17:32:11', '2025-12-28 19:02:58'),
(63, 10, 2, 'Women\'s Wear', 'स्त्रियांचे कपडे', 'womens-wear', 'checkroom', NULL, 'https://cdn-icons-png.flaticon.com/128/892/892447.png', 2, 1, 0, '2025-12-27 17:32:11', '2025-12-28 19:02:58'),
(64, 10, 2, 'Kids Wear', 'मुलांचे कपडे', 'kids-wear', 'child_care', NULL, 'https://cdn-icons-png.flaticon.com/128/2503/2503380.png', 3, 1, 0, '2025-12-27 17:32:11', '2025-12-28 19:02:58'),
(65, 10, 2, 'Footwear', 'पादत्राणे', 'footwear', 'footwear', NULL, 'https://cdn-icons-png.flaticon.com/128/2589/2589903.png', 4, 1, 0, '2025-12-27 17:32:11', '2025-12-28 19:02:58'),
(66, 10, 2, 'Bags & Wallets', 'बॅग आणि वॉलेट', 'bags-wallets', 'backpack', NULL, 'https://cdn-icons-png.flaticon.com/128/679/679821.png', 5, 1, 0, '2025-12-27 17:32:11', '2025-12-28 19:02:59'),
(67, 10, 2, 'Watches', 'घड्याळे', 'watches', 'watch', NULL, 'https://cdn-icons-png.flaticon.com/128/2972/2972531.png', 6, 1, 0, '2025-12-27 17:32:11', '2025-12-28 19:02:59'),
(68, 10, 2, 'Fashion Accessories', 'फॅशन अॅक्सेसरीज', 'fashion-accessories', 'diamond', NULL, 'https://cdn-icons-png.flaticon.com/128/1918/1918637.png', 7, 1, 0, '2025-12-27 17:32:11', '2025-12-28 19:02:59'),
(69, 11, 2, 'Hair Care', 'केसांची काळजी', 'hair-care', 'face', NULL, 'https://cdn-icons-png.flaticon.com/128/2553/2553675.png', 1, 1, 0, '2025-12-27 17:32:12', '2025-12-28 19:03:36'),
(70, 11, 2, 'Skin Care', 'त्वचेची काळजी', 'skin-care', 'face_retouching_natural', NULL, 'https://cdn-icons-png.flaticon.com/128/3163/3163186.png', 2, 1, 0, '2025-12-27 17:32:12', '2025-12-28 19:03:36'),
(71, 11, 2, 'Oral Care', 'तोंडाची काळजी', 'oral-care', 'sentiment_satisfied', NULL, 'https://cdn-icons-png.flaticon.com/128/2915/2915633.png', 3, 1, 0, '2025-12-27 17:32:12', '2025-12-28 19:03:36'),
(72, 11, 2, 'Bath & Body', 'बाथ आणि बॉडी', 'bath-body', 'soap', NULL, 'https://cdn-icons-png.flaticon.com/128/2553/2553606.png', 4, 1, 0, '2025-12-27 17:32:12', '2025-12-28 19:03:37'),
(73, 11, 2, 'Men\'s Grooming', 'पुरुषांचे ग्रूमिंग', 'mens-grooming', 'face_6', NULL, 'https://cdn-icons-png.flaticon.com/128/2553/2553690.png', 5, 1, 0, '2025-12-27 17:32:12', '2025-12-28 19:03:37'),
(74, 11, 2, 'Feminine Hygiene', 'महिला स्वच्छता', 'feminine-hygiene', 'favorite', NULL, 'https://cdn-icons-png.flaticon.com/128/2700/2700223.png', 6, 1, 0, '2025-12-27 17:32:12', '2025-12-28 19:03:37'),
(75, 12, 2, 'OTC Medicines', 'ओटीसी औषधे', 'otc-medicines', 'medication', NULL, 'https://cdn-icons-png.flaticon.com/128/2376/2376100.png', 1, 1, 0, '2025-12-27 17:32:13', '2025-12-28 19:03:37'),
(76, 12, 2, 'Health Supplements', 'हेल्थ सप्लीमेंट्स', 'health-supplements', 'fitness_center', NULL, 'https://cdn-icons-png.flaticon.com/128/2376/2376077.png', 2, 1, 0, '2025-12-27 17:32:13', '2025-12-28 19:03:37'),
(77, 12, 2, 'First Aid', 'प्रथमोपचार', 'first-aid', 'medical_services', NULL, 'https://cdn-icons-png.flaticon.com/128/2376/2376049.png', 3, 1, 0, '2025-12-27 17:32:13', '2025-12-28 19:03:37'),
(78, 12, 2, 'Ayurvedic & Homeopathic', 'आयुर्वेदिक आणि होमिओपॅथिक', 'ayurvedic-homeopathic', 'spa', NULL, 'https://cdn-icons-png.flaticon.com/128/2909/2909841.png', 4, 1, 0, '2025-12-27 17:32:13', '2025-12-28 19:03:37'),
(79, 12, 2, 'Health Devices', 'आरोग्य उपकरणे', 'health-devices', 'monitor_heart', NULL, 'https://cdn-icons-png.flaticon.com/128/2376/2376064.png', 5, 1, 0, '2025-12-27 17:32:13', '2025-12-28 19:03:37'),
(80, 13, 2, 'Diapers & Wipes', 'डायपर आणि वाइप्स', 'diapers-wipes', 'baby_changing_station', NULL, 'https://cdn-icons-png.flaticon.com/128/2553/2553639.png', 1, 1, 0, '2025-12-27 17:32:14', '2025-12-28 19:03:37'),
(81, 13, 2, 'Baby Food', 'बाळाचे अन्न', 'baby-food', 'restaurant', NULL, 'https://cdn-icons-png.flaticon.com/128/2553/2553659.png', 2, 1, 0, '2025-12-27 17:32:14', '2025-12-28 19:03:37'),
(82, 13, 2, 'Baby Bath & Skin', 'बाळाची त्वचा काळजी', 'baby-bath-skin', 'bathtub', NULL, 'https://cdn-icons-png.flaticon.com/128/2553/2553653.png', 3, 1, 0, '2025-12-27 17:32:14', '2025-12-28 19:03:37'),
(83, 13, 2, 'Toys & Games', 'खेळणी', 'toys-games', 'toys', NULL, 'https://cdn-icons-png.flaticon.com/128/2553/2553624.png', 4, 1, 0, '2025-12-27 17:32:14', '2025-12-28 19:03:38'),
(84, 13, 2, 'School Supplies', 'शाळेचे साहित्य', 'school-supplies', 'school', NULL, 'https://cdn-icons-png.flaticon.com/128/167/167755.png', 5, 1, 0, '2025-12-27 17:32:14', '2025-12-28 19:03:38'),
(85, 14, 2, 'Gift Sets & Combos', 'गिफ्ट सेट', 'gift-sets', 'card_giftcard', NULL, 'https://cdn-icons-png.flaticon.com/128/1139/1139982.png', 1, 1, 0, '2025-12-27 17:32:15', '2025-12-28 19:03:38'),
(86, 14, 2, 'Festival Items', 'सणासुदीचे साहित्य', 'festival-items', 'celebration', NULL, 'https://cdn-icons-png.flaticon.com/128/2107/2107845.png', 2, 1, 0, '2025-12-27 17:32:15', '2025-12-28 19:03:38'),
(87, 14, 2, 'Wedding Essentials', 'लग्नाचे साहित्य', 'wedding-essentials', 'favorite', NULL, 'https://cdn-icons-png.flaticon.com/128/2107/2107847.png', 3, 1, 0, '2025-12-27 17:32:15', '2025-12-28 19:03:38'),
(88, 14, 2, 'Greeting Cards', 'ग्रीटिंग कार्ड', 'greeting-cards', 'mail', NULL, 'https://cdn-icons-png.flaticon.com/128/1139/1139997.png', 4, 1, 0, '2025-12-27 17:32:15', '2025-12-28 19:03:38'),
(89, 14, 2, 'Gift Wrapping', 'गिफ्ट रॅपिंग', 'gift-wrapping', 'card_giftcard', NULL, 'https://cdn-icons-png.flaticon.com/128/1139/1139990.png', 5, 1, 0, '2025-12-27 17:32:15', '2025-12-28 19:03:38'),
(90, 15, 2, 'Pet Food', 'पाळीव प्राण्यांचे अन्न', 'pet-food', 'pets', NULL, 'https://cdn-icons-png.flaticon.com/128/194/194279.png', 1, 1, 0, '2025-12-27 17:32:15', '2025-12-28 19:03:38'),
(91, 15, 2, 'Pet Accessories', 'पाळीव प्राण्यांचे अॅक्सेसरीज', 'pet-accessories', 'pets', NULL, 'https://cdn-icons-png.flaticon.com/128/194/194287.png', 2, 1, 0, '2025-12-27 17:32:15', '2025-12-28 19:03:39'),
(92, 15, 2, 'Pet Grooming', 'पाळीव प्राण्यांचे ग्रूमिंग', 'pet-grooming', 'pets', NULL, 'https://cdn-icons-png.flaticon.com/128/194/194296.png', 3, 1, 0, '2025-12-27 17:32:15', '2025-12-28 19:03:39'),
(93, 16, 2, 'Notebooks & Registers', 'वह्या आणि रजिस्टर', 'notebooks-registers', 'menu_book', NULL, 'https://cdn-icons-png.flaticon.com/128/167/167755.png', 1, 1, 0, '2025-12-27 17:32:16', '2025-12-28 19:03:39'),
(94, 16, 2, 'Pens & Pencils', 'पेन आणि पेन्सिल', 'pens-pencils', 'edit', NULL, 'https://cdn-icons-png.flaticon.com/128/1083/1083326.png', 2, 1, 0, '2025-12-27 17:32:16', '2025-12-28 19:03:39'),
(95, 16, 2, 'School Supplies', 'शाळेचे साहित्य', 'school-supplies-stationery', 'school', NULL, 'https://cdn-icons-png.flaticon.com/128/167/167754.png', 3, 1, 0, '2025-12-27 17:32:16', '2025-12-28 19:03:39'),
(96, 16, 2, 'Office Supplies', 'ऑफिस साहित्य', 'office-supplies', 'business_center', NULL, 'https://cdn-icons-png.flaticon.com/128/1051/1051269.png', 4, 1, 0, '2025-12-27 17:32:16', '2025-12-28 19:03:39'),
(97, 16, 2, 'Books', 'पुस्तके', 'books', 'auto_stories', NULL, 'https://cdn-icons-png.flaticon.com/128/2702/2702134.png', 5, 1, 0, '2025-12-27 17:32:16', '2025-12-28 19:03:39');

-- --------------------------------------------------------

--
-- Table structure for table `shop_products`
--

CREATE TABLE `shop_products` (
  `product_id` bigint(20) UNSIGNED NOT NULL,
  `listing_id` bigint(20) UNSIGNED NOT NULL,
  `product_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category_id` int(10) UNSIGNED NOT NULL,
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

--
-- Dumping data for table `shop_products`
--

INSERT INTO `shop_products` (`product_id`, `listing_id`, `product_name`, `description`, `category_id`, `subcategory_id`, `shop_category_id`, `price`, `discounted_price`, `currency`, `image_url`, `sell_online`, `condition`, `stock_qty`, `min_qty`, `sort_order`, `is_active`, `created_at`, `updated_at`) VALUES
(2, 11, 'LG 260L Refrigerator', 'LG 260 Litre Double Door Refrigerator. Frost Free, Inverter Compressor. Smart Connect. Silver color.', 150004, 150042, 8, 24999.00, 29999.00, 'INR', 'https://images.unsplash.com/photo-1571175443880-49e1d25b2bc5?w=600', 1, 'new', 3, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:05'),
(3, 11, 'Whirlpool Washing Machine', 'Whirlpool 7 KG Fully Automatic Top Load Washing Machine. StainWash technology. 2 Year warranty.', 150004, 51, 8, 15999.00, 18999.00, 'INR', 'https://images.unsplash.com/photo-1626806787461-102c1bfaaea1?w=600', 1, 'new', 4, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-29 12:21:38'),
(4, 11, 'Bajaj Room Heater', 'Bajaj RHX-2 Room Heater. 2000W, 3 heat settings, overheat protection. Compact design.', 150004, 150046, 8, 1899.00, 2299.00, 'INR', 'https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=600', 1, 'new', 10, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:05'),
(5, 11, 'Philips Mixer Grinder', 'Philips HL7756/00 750W Mixer Grinder. 3 jars, turbo speed. Stainless steel blades.', 150004, 150039, 8, 3499.00, 4199.00, 'INR', 'https://images.unsplash.com/photo-1570222094114-d054a817e56b?w=600', 1, 'new', 8, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:05'),
(6, 11, 'Sony Bluetooth Speaker', 'Sony SRS-XB13 Extra Bass Wireless Speaker. Waterproof, 16H battery. Travel friendly.', 150003, 150033, 8, 3999.00, 4999.00, 'INR', 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600', 1, 'new', 12, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:05'),
(7, 14, 'Toor Dal - 1 KG', 'Premium quality Toor Dal. Clean and sorted. Fresh stock.', 150087, NULL, 3, 169.00, NULL, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listing_6948c28f68ba8_1766376079.webp', 1, 'new', 50, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:07'),
(8, 14, 'Basmati Rice - 5 KG', 'India Gate Basmati Rice. Long grain, aromatic. Best for biryani and pulao.', 150087, NULL, 3, 499.00, 549.00, 'INR', 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600', 1, 'new', 30, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:07'),
(9, 14, 'Refined Oil - 5 Litre', 'Fortune Refined Sunflower Oil. 5 Litre pack. Low cholesterol.', 150087, NULL, 3, 699.00, 749.00, 'INR', 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600', 1, 'new', 25, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:07'),
(10, 14, 'Sugar - 5 KG', 'Refined white sugar. Clean and pure. Free flowing crystals.', 150087, NULL, 3, 249.00, NULL, 'INR', 'https://images.unsplash.com/photo-1558642452-9d2a7deb7f62?w=600', 1, 'new', 40, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:07'),
(11, 14, 'Atta Flour - 10 KG', 'Aashirvaad Atta whole wheat flour. Superior quality for soft rotis.', 150087, NULL, 3, 449.00, 499.00, 'INR', 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=600', 1, 'new', 35, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:07'),
(12, 14, 'Chana Dal - 1 KG', 'Premium Chana Dal. Yellow split chickpeas. Perfect for curries.', 150087, NULL, 3, 139.00, NULL, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listing_694901dcd174c_1766392284.webp', 1, 'new', 45, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:07'),
(13, 15, 'Realme C55 Smartphone', 'Realme C55 (4GB RAM, 64GB Storage). 50MP AI Camera. 5000mAh Battery. 1 Year Warranty.', 150003, 150027, 7, 9999.00, 11999.00, 'INR', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600', 1, 'new', 10, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:06'),
(14, 15, 'Samsung Galaxy A14', 'Samsung Galaxy A14 5G (6GB RAM, 128GB). Triple Camera. Long battery life.', 150003, 150027, 7, 14999.00, 17999.00, 'INR', 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=600', 1, 'new', 8, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:06'),
(15, 15, 'Boat Earbuds', 'Boat Airdopes 141. True Wireless Earbuds. 42H Playtime. IPX4 Water Resistant.', 150003, 47, 7, 1299.00, 1999.00, 'INR', 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=600', 1, 'new', 20, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-28 17:46:02'),
(16, 15, 'Mobile Back Cover M34 Samsung', 'Premium quality mobile back cover. Shockproof design. Available for all models.', 150003, 46, 7, 199.00, 299.00, 'INR', 'https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?w=600', 1, 'new', 50, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-28 17:46:34'),
(17, 15, 'Fast Charger 33W', 'Original fast charger 33W with Type-C cable. Compatible with all smartphones.', 150003, 150034, 8, 599.00, 799.00, 'INR', 'https://images.unsplash.com/photo-1583863788434-e62f75b62e89?w=600', 1, 'new', 25, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:05'),
(18, 15, 'Power Bank 10000mAh', 'Mi Power Bank 3i 10000mAh. Dual USB output, fast charging. Compact design.', 150003, 150034, 8, 999.00, 1299.00, 'INR', 'https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?w=600', 1, 'new', 15, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:05'),
(19, 15, 'Tempered Glass', 'Full screen tempered glass protector. 9H hardness. Anti-scratch.', 150003, 150034, 8, 149.00, 199.00, 'INR', 'https://images.unsplash.com/photo-1601972602237-8c79241e468b?w=600', 1, 'new', 100, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:05'),
(20, 15, 'USB Cable Type-C', 'Braided USB Type-C cable. 1 meter. Fast charging support.', 150003, 150034, 8, 149.00, 199.00, 'INR', 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600', 1, 'new', 60, 1, 0, 1, '2025-12-21 17:42:44', '2025-12-27 18:07:05'),
(21, 31, 'my product', 'my description of product', 1, NULL, NULL, 508.00, 507.00, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_695350c386f32_1767067843.webp', 1, 'new', 50, 1, 0, 0, '2025-12-30 04:10:49', '2025-12-30 04:10:49'),
(743950, 23, 'Fruit Box', 'FRUIT BOX\r\nDelivery Timings:\r\nMorning: 7 AM', 150086, 18, 1, 100.00, 150.00, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/products/694eb49809829_1766765720.png', 1, 'new', 500, 1, 0, 1, '2025-12-26 16:15:25', '2025-12-28 17:45:18'),
(773950, 23, 'Tender Coconut', 'Tender Coconut', 150087, 17, 1, 60.00, 80.00, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/products/694ebef9aa031_1766768377.png', 1, 'new', 1, 1, 0, 1, '2025-12-26 16:59:43', '2025-12-28 17:40:31'),
(803950, 24, 'Ravan', 'by Shrad Tandale', 150007, 150063, NULL, 400.00, NULL, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_694f806deb11d_1766817901.webp', 0, 'new', NULL, 1, 0, 1, '2025-12-27 06:45:06', '2025-12-27 06:45:06'),
(863951, 25, 'Homi Bhabha Book', 'It\'s Homi Bhabha Book of year 2025.', 150007, 150063, NULL, 350.00, NULL, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_695227f7529ac_1766991863.webp', 1, 'new', 1, 1, 1, 1, '2025-12-29 07:04:28', '2025-12-29 07:07:01'),
(893950, 1, 'test', 'test', 1, 17, NULL, 200.00, NULL, 'INR', NULL, 0, 'new', NULL, 1, 0, 1, '2025-12-29 11:37:37', '2025-12-29 11:37:37'),
(923950, 1, 'sfndgj', 'xgndgw4', 120001, 120013, NULL, 459.00, NULL, 'INR', NULL, 0, 'new', NULL, 1, 1, 1, '2025-12-29 11:45:57', '2025-12-29 11:45:57'),
(923951, 26, 'airtelwifi', '', 120001, 120013, NULL, 500.00, NULL, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_69526b296fe09_1767009065.webp', 0, 'new', NULL, 1, 0, 1, '2025-12-29 11:51:09', '2025-12-29 11:51:09'),
(923952, 27, 'wifi', '', 8, 51, 8, 509.00, NULL, 'INR', 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev/listings/listings_69526cac5f4d4_1767009452.webp', 0, 'new', NULL, 1, 0, 1, '2025-12-29 11:54:23', '2025-12-29 12:02:23');

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

--
-- Dumping data for table `states`
--

INSERT INTO `states` (`state_id`, `country_code`, `name`, `slug`, `is_active`) VALUES
(1, 'IN', 'Maharashtra', 'maharashtra', 1);

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

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `email`, `phone`, `password_hash`, `avatar_url`, `is_verified`, `is_active`, `listing_count`, `avg_rating`, `review_count`, `response_rate`, `last_active_at`, `created_at`, `updated_at`, `gender`, `date_of_birth`, `reset_token`, `reset_token_expires`) VALUES
(1, 'admin', 'admin@hellohingoli.com', '9999999999', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'https://hellohingoli.com/api/uploads/profiles/69392255afb9e_1765352021.png', 1, 1, 0, 0.0, 0, 0.00, '2025-12-11 05:27:57', '2025-12-09 08:04:22', '2025-12-11 05:27:57', NULL, NULL, NULL, NULL),
(2, 'seller_raj', 'raj@example.com', '8888888888', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'https://ui-avatars.com/api/?name=Raj+Electronics&background=random', 1, 1, 0, 0.0, 0, 0.00, '2025-12-11 10:17:05', '2025-12-09 08:04:22', '2025-12-11 10:17:05', NULL, NULL, NULL, NULL),
(3, 'service_pro', 'pro@example.com', '7777777777', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'https://ui-avatars.com/api/?name=Hingoli+Services&background=random', 1, 1, 1, 0.0, 0, 0.00, '2025-12-10 13:40:49', '2025-12-09 08:04:22', '2025-12-17 14:41:56', NULL, NULL, NULL, NULL),
(4, 'job_seeker', 'job@example.com', '6666666666', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'https://ui-avatars.com/api/?name=Amit+Kumar&background=random', 1, 1, 0, 0.0, 0, 0.00, '2025-12-09 15:20:06', '2025-12-09 08:04:22', '2025-12-09 15:20:06', NULL, NULL, NULL, NULL),
(30002, 'omwaman', 'omwaman1@gmail.comk', '9595340265', '$2y$10$zz9rKjvwpaY/ibPNa/Ija.LAKR7woSzVQxFEcRkb.tWf7NDIDsPMK', NULL, 1, 1, 2, 0.0, 0, 0.00, '2025-12-12 06:06:10', '2025-12-10 11:34:45', '2025-12-12 06:54:57', 'male', '2025-12-11', NULL, NULL),
(30005, 'Eknath', 'wamanekanath@gmail.com', '9096632831', '$2y$10$KxtZlJjOb.OkkMJPyLO/iufjgh4OwFqx3GianucfMIffdcYJYdmI6', NULL, 1, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-10 11:51:31', '2025-12-12 06:19:20', NULL, NULL, NULL, NULL),
(60002, 'ok', 'omwaman1@gmail.com', '9595340266', '$2y$10$TY9DAVYvBWB14tY4WL4kkOxI6njg2VInt2CSKHEROCd6SOq580hpe', NULL, 0, 1, 10, 0.0, 0, 0.00, '2025-12-21 12:48:33', '2025-12-12 07:08:23', '2025-12-21 17:45:05', 'male', '2025-12-17', NULL, NULL),
(90002, 'prafull', '9860530162_8231a7@temp.hellohingoli.com', '9860530162', '$2y$10$WtW7EWpigAGq7HEwlpsbTe.ifiuPZRmDqEJJt5Bd8lNXH1tUSPubG', NULL, 0, 1, 0, 0.0, 0, 0.00, '2025-12-24 05:46:15', '2025-12-12 08:01:38', '2025-12-24 05:46:15', 'male', NULL, NULL, NULL),
(120002, 'adityarakhonde', '7823080840_8244cc@temp.hellohingoli.com', '7823080840', '$2y$10$lH2sqmwK7lkbVCLkJWTFzuYo/luJky65yn2U7c/Jz1Py5vivTXmJq', NULL, 0, 1, 1, 0.0, 0, 0.00, '2025-12-22 13:32:38', '2025-12-12 13:14:00', '2025-12-22 13:32:38', NULL, NULL, NULL, NULL),
(150002, 'Omkar Waghmare', '9145216091_a932bd@temp.hellohingoli.com', '9145216091', '$2y$10$dGmgAh4gE/PvHc/px3z.3e/uFsQ1TkDgY4wDdI8tfUcA./8dyjXwK', NULL, 0, 1, 0, 0.0, 0, 0.00, '2025-12-27 08:13:20', '2025-12-12 17:55:24', '2025-12-27 08:13:20', 'male', NULL, NULL, NULL),
(180002, 'hanuman more', 'hanumanmore', '9529239224', '$2y$10$YT1sOwF5DgVfdvw9nn2xYeQr0K9NSOZ8B/ik9RgN2/uGgT6pnPx6K', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-13 02:17:20', '2025-12-13 04:23:22', 'male', '2025-12-13', NULL, NULL),
(210002, 'Hingoli Hub Tester', '8788428166_29784d@temp.hellohingoli.com', '8788428166', '$2y$10$0Q.OQfncTFtBsVzo0e.3S.6j5ya031kHeWkRW6NhXhqi5m07yE3eC', NULL, 0, 1, 1, 0.0, 0, 0.00, '2025-12-29 14:52:39', '2025-12-13 15:54:22', '2025-12-29 14:52:39', 'male', '1989-12-13', NULL, NULL),
(240002, 'User3121_be5d70', 'mauligaykwad6462@gmail.com', '8888423121', '$2y$10$Gr4P5.tWBIWUIPjo1HpsLOOsQSgZDbsHOs74gOyhTBM.TrdbH484G', NULL, 0, 1, 0, 0.0, 0, 0.00, '2025-12-19 10:58:38', '2025-12-15 01:26:01', '2025-12-19 10:59:21', 'male', '2015-07-22', NULL, NULL),
(270002, 'User4674_b75f3d', '8975114674_b75f3d@temp.hellohingoli.com', '8975114674', '', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-15 03:11:08', '2025-12-15 03:11:08', NULL, NULL, NULL, NULL),
(300002, 'om waman', 'abc@gmail.com ', '9156233733', '', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-15 10:38:15', '2025-12-15 10:43:14', 'male', '2025-12-15', NULL, NULL),
(330002, 'User5945_11cee5', '8459545945_11cee5@temp.hellohingoli.com', '8459545945', '', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-15 12:55:37', '2025-12-15 12:55:37', NULL, NULL, NULL, NULL),
(360002, 'Abhishek Bhakare', 'abhiearnmoney121@gmail.com', '8805281502', '', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-15 17:08:10', '2025-12-15 17:10:33', 'male', '1997-06-09', NULL, NULL),
(390002, 'Shivrekha Pustakalay', 'maheshpadole36@gmail.com', '9689969814', '$2y$10$gJ/vv3JTPR0N77CgLMQKaOQAyhhI0VQbzftEmBBaGW0LQET9nfdJy', NULL, 0, 1, 3, 0.0, 0, 0.00, '2025-12-27 06:29:09', '2025-12-16 11:16:39', '2025-12-27 06:42:38', 'male', '2000-03-13', NULL, NULL),
(420002, 'Raju Pole', '9370782939_dd73ea@temp.hellohingoli.com', '9370782939', '', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-16 12:19:25', '2025-12-16 15:58:33', NULL, NULL, NULL, NULL),
(450002, 'Support', '9096@gmail.com', '9096632830', '$2y$10$4mwx05Ol307F2fsKxZRVo.UICPtT80ORzjC7IzYFAHeclEV625k72', NULL, 0, 1, 1, 0.0, 0, 0.00, '2025-12-30 02:51:50', '2025-12-16 16:49:24', '2025-12-30 02:51:50', 'male', '2025-12-16', NULL, NULL),
(480002, '70', 'hfuf@yddy.gfd', '7020034432', '$2y$10$ypCHj49HccGyyD79HTHK9OFU4Nuk0LlR3/dKefiPn9UlqvAFRG6mO', NULL, 0, 1, 1, 0.0, 0, 0.00, '2025-12-21 14:10:08', '2025-12-17 15:06:20', '2025-12-21 17:45:19', 'male', '2025-12-21', NULL, NULL),
(510002, 'User5115_842e2b', '9529995115_842e2b@temp.hellohingoli.com', '9529995115', '', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-18 09:37:53', '2025-12-18 09:37:53', NULL, NULL, NULL, NULL),
(540002, 'Manohar waman', 'manoharwaman58@gmail.com', '7559262920', '$2y$10$XqzePp3nqT7HtvQUfcSd3ejBXCPUidgcDFyTQmPg9RW3nCi0wuR1i', NULL, 0, 1, 0, 0.0, 0, 0.00, '2025-12-27 03:24:50', '2025-12-21 13:16:22', '2025-12-27 03:24:50', 'male', '2025-12-12', NULL, NULL),
(7020034431, 'Hingoli Demo User 2', 'demo2@hellohingoli.com', '7020034431', '$2y$10$EAtgJcyw4XYXT75IKKX17Ov6Sk4Qdlwv.qpECSnnogaCYGtlRso42', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200', 1, 1, 11, 0.0, 0, 0.00, '2025-12-30 03:14:39', '2025-12-21 17:42:39', '2025-12-30 03:14:39', 'male', '2025-12-23', NULL, NULL),
(7507465080, 'User5080_a7b45c', '7507465080_a7b45c@temp.hellohingoli.com', '7507465080', '', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-27 11:05:20', '2025-12-27 11:05:20', NULL, NULL, NULL, NULL),
(7709082672, 'vishal Karhale', 'vishalkarhale37@gmail.com', '7709082672', '$2y$10$OrcoNeeIQyiJtcKcTuPyvuBPn4/7AC6J/qiZraQWY7jSdWOkLAPAC', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-29 04:27:29', '2025-12-29 04:29:32', 'male', '2000-08-15', NULL, NULL),
(8669702031, 'jio', 'jio@123.com', '8669702031', '$2y$10$8aiGSevVv719ZzOKAWy1De1pHE6.R3ID65tt1MZG52h1e6kkyasFW', NULL, 0, 1, 3, 0.0, 0, 0.00, '2025-12-30 04:07:31', '2025-12-29 05:24:20', '2025-12-30 04:10:48', 'male', '2025-12-29', NULL, NULL),
(8767507375, 'User7375_6b7e86', '8767507375_6b7e86@temp.hellohingoli.com', '8767507375', '', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-29 16:11:45', '2025-12-29 16:11:45', NULL, NULL, NULL, NULL),
(9325872359, 'User2359_395b3a', '9325872359_395b3a@temp.hellohingoli.com', '9325872359', '', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-26 15:40:33', '2025-12-26 15:40:33', NULL, NULL, NULL, NULL),
(9359614612, 'User4612_c57706', '9359614612_c57706@temp.hellohingoli.com', '9359614612', '', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-27 06:36:59', '2025-12-27 06:36:59', NULL, NULL, NULL, NULL),
(9595340263, 'Hingoli Demo User 1', 'demo1@hellohingoli.comm', '9595340263', '$2y$10$aMEL.agVDns1akuo1LTXN.oyDwdS86Z27zOEffy2EQCk7qBkfNSda', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200', 1, 1, 15, 0.0, 0, 0.00, '2025-12-30 03:55:38', '2025-12-21 17:42:39', '2025-12-30 03:55:38', 'male', '1999-08-28', NULL, NULL),
(9595370264, 'User7135_046e2c', '9623347135_046e2c@temp.hellohingoli.com', '9623347135', '', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-26 14:18:31', '2025-12-26 14:18:31', NULL, NULL, NULL, NULL),
(9595370265, 'User8134_8b2d20', '9890038134_8b2d20@temp.hellohingoli.com', '9890038134', '', NULL, 0, 1, 0, 0.0, 0, 0.00, NULL, '2025-12-26 14:19:13', '2025-12-26 14:19:13', NULL, NULL, NULL, NULL);

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

--
-- Dumping data for table `user_addresses`
--

INSERT INTO `user_addresses` (`address_id`, `user_id`, `name`, `phone`, `address_line1`, `address_line2`, `city`, `state`, `pincode`, `is_default`, `created_at`) VALUES
(1, 60002, 'om waman', '9595340263', 'hingoli', '', 'Hingoli', 'Maharashtra', '431513', 1, '2025-12-15 06:09:12'),
(30001, 360002, 'Abhishek Bhakare', '8805281502', 'Pune', '', 'pune', 'Maharashtra', '410501', 1, '2025-12-15 17:27:17'),
(60001, 450002, 'om waman', '90966 32830', 'idjdj', 'kdkkd', 'Hingoli', 'Maharashtra', '431513', 1, '2025-12-17 10:05:08'),
(90001, 480002, 'sfntsh', '426625624742', 'sfbsfb', 'xgndgj', 'Hingoli', 'Maharashtra', '431513', 1, '2025-12-17 15:07:18'),
(120001, 150002, 'om', '82736', 'ishdb', 'jsjdbd', 'Hingoli', 'Maharashtra', '431513', 1, '2025-12-20 18:09:07'),
(150001, 7020034431, 'etvetb', '36363636', 's efaas', 'etbef', 'Hingoli', 'Maharashtra', '431513', 1, '2025-12-23 03:45:58'),
(180001, 7507465080, 'Abhishek Bhakare', '7507465080', 'Chondhi bk', '', 'Hingoli', 'Maharashtra', '431703', 1, '2025-12-27 11:06:55'),
(210001, 9595340263, 'om', '9595340263', 'om', 'om', 'Hingoli', 'Maharashtra', '431513', 1, '2025-12-27 11:29:34'),
(240001, 210002, 'Aaroh', '8788428166', '13', '13', 'Hingoli', 'Maharashtra', '431513', 1, '2025-12-29 05:14:29');

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

--
-- Dumping data for table `user_fcm_tokens`
--

INSERT INTO `user_fcm_tokens` (`id`, `user_id`, `fcm_token`, `device_info`, `created_at`, `updated_at`) VALUES
(1, 120002, 'dOqgnikATnm99kE4-tG-UZ:APA91bGVjK9OE1rtNTxEFWpSePZfzUY78bgHRaQ81a68DtYqXVbfpYxd_lu687sBmGi598zNZeYYpAmHK2bAUDD6ffhbNSUKkoXzpVATuW-z313ThlVKHSY', 'samsung SM-M346B', '2025-12-12 12:08:39', '2025-12-12 13:14:01'),
(60001, 60002, 'elTmLL4fQrqgHHSxsS4rUq:APA91bGye55CI9RnyDPLrTiHe0rPypZEeKdppNJnGQo-6iFCV0mQLmUUeUry51KgUI0AYFk_ApAeaASVVxsuxXnHZbCVXa2H5ysZX3OKfcl9HhYulllgIy4', 'samsung SM-M346B', '2025-12-12 16:37:59', '2025-12-12 16:37:59'),
(60002, 60002, 'dr_GKoGnSVCkuxzOWorxRO:APA91bGApLkSMi-L0rOTf-m4I0YwAiCJdZEUEA8qEnZqmhry_PQL5KwdUCswRArKpKABrmzKhk3Z1cpXQyzm0i-QuVrcKb-jQtpby_B9Ii9pd5s5i2Qi5bA', 'samsung SM-M346B', '2025-12-12 16:48:01', '2025-12-12 16:48:01'),
(90001, 60002, 'e5zcbkIKQk-mFwUYmzkubz:APA91bEehkAqC-gpMPTNEhvZS6kyOeyMUixIKgonmjkIoR078co_ce3KYhdnsbEnWeAyHjHE7pap15IgK0nbt7gRx4ZJnGdaaP0iR1KrTRr7PTDknmwT7rY', 'samsung SM-M346B', '2025-12-12 17:20:46', '2025-12-12 17:56:38'),
(120001, 150002, 'evDFktO8Qm-kVJoMyCvu9Q:APA91bGFv9eVcJtNNqW__i-gp34B-zzDQ78thLYdFUoqNsTtnIiDtcQTFJcDst9Ng_cmN1nLeuMVE84lUc38wNR5r2RjvJFKjlxemN6p_Bg8MMtADpZwuV0', 'Xiaomi 23127PN0CG', '2025-12-12 17:55:25', '2025-12-12 17:55:25'),
(150001, 60002, 'dWJFzCHdSx2yY4CwQbqOeX:APA91bGtFX_ew4qwozQGVwRJvvMHUleoIHSlOHEssGuHSBlc0Z6LDKfpZN1qFUzcIDSOV21oERoiNwinixxG7dzkTul-dsfCHzlugZdROwrNS-r6tvIK5-E', 'samsung SM-M346B', '2025-12-13 01:58:25', '2025-12-13 04:33:41'),
(180002, 60002, 'fiPNEn1sTJi26OpOxn6lRA:APA91bFYaKQ4H_Vn0a26ccxltqVovkVOfANdl9Ezxd0O9TsVRyc5S13MICM3vhiA8eGRtqEY8Q6jYD1aSGsItmqJ64b4PI8zx-FomMXR9qmWATImSx1V7B8', 'samsung SM-M346B', '2025-12-13 04:34:40', '2025-12-13 06:11:24'),
(240001, 60002, 'dktRRREnTmaIDnbPEk7lEo:APA91bFwBaDZKPQixv9MHbB7blx1WfNPbkXQ3DUHUzQ20W4Oequeq8iZ6B7LOhkx3i55mg1R9JEq7Tt7-_pf_RP0qEXBlazBKqQJJ7TWZ18-vNs9ijLCrYE', 'samsung SM-M346B', '2025-12-13 11:07:16', '2025-12-15 00:58:35'),
(270001, 120002, 'ek1xnPl2TR2ngeHR0wYJ8K:APA91bFnwjm98g_e-MesXcW_NMf1qATzEYY9HqkIRHVHnjfbIrKJzNXqUYJzgYYKxrXI_v-EpKBW1KKA9oPsfoCojFp4ZNcHspgEPbE7VkN7ED-Rc5InBjI', 'samsung SM-M366B', '2025-12-13 15:02:53', '2025-12-13 15:02:53'),
(300001, 210002, 'fwT6xZA2RteRWVvuF4me9o:APA91bFQmxXxG9tX1oXTsQM7gNa-5x4jmnmV0Q1jaHGFw4L0G0_LKIRIyl7WcxYK1NT1PndJXTdbWGR1O6BtErNaBy69D6Ekfv9dNMNUn7goq4Byit0T0CI', 'vivo V2420', '2025-12-13 15:54:23', '2025-12-13 15:54:23'),
(390001, 240002, 'fUQIng4gQv-bM-qBEY4kXL:APA91bGBiO3TQfJb7-OPKUM3tD1UNB3KAk9iBIoD3Y_XbZbxBg3b15B3jAoUTdvKlF8Gqj3gMZFifLhiExSNuorKrL5jf0cVUi1o-keydfYEpAwykhFLIdY', 'vivo V2348', '2025-12-15 01:26:02', '2025-12-19 10:58:39'),
(420001, 60002, 'fQ6KxynwT8mg9RKcgpPwPU:APA91bGzTijnZ-7hgSvVJIjmuiaSfkBON9YZ3rH4Xsu13mzrjDdrPW0u872BT5gy0n_CHGMJBtV5X81GLH6wBfTcQYULg4WDlSZDYOUEmJSEDMycY9uHu_I', 'samsung SM-M346B', '2025-12-15 01:53:50', '2025-12-15 01:53:50'),
(450001, 60002, 'cNk1FsjITXG7OJsnaSavXs:APA91bE6ZeSFay5dOMYNsm9wjUhTUg5Rqci0F-Xg2sQHJwbxWE-62b86cQqPGNymeLXtKJv2ZQQHr426eSMEySJjxPFSUN-NCQ9MKCiFxtkQ_qEWiVc9pRQ', 'samsung SM-M346B', '2025-12-15 02:45:32', '2025-12-15 12:02:46'),
(480001, 270002, 'clwfFnwcQTm5TVLfZrt4b4:APA91bEhsYqGfwwaoeFIVfNCLlEtT6-KYgLi-yQGONVG9LWipZFDRH2NeRzBEounDfjQvi-N_6mXDkB7LDxXSPS0GwAc-YzU-Fl6IjwNhUpRdQ0I9JLpSQ0', 'INFINIX Infinix X6857B', '2025-12-15 03:11:09', '2025-12-15 03:11:09'),
(510002, 60002, 'fk_nq6I4Qaelk06lkG68Sc:APA91bELPM31EzRC0qL0CcIF18xzRBK5W4SkPwyb5gaXVriZb8CWSvz0hb-QFDKFw83pEWdjRFSdE3h3TVE_PTxaF1_k5mtG9tQ2LHHv7dtnOl__CVW3fBQ', 'OPPO CPH2643', '2025-12-15 10:42:13', '2025-12-15 10:42:13'),
(570001, 330002, 'f9r3d1lmQtK9SqTHnGK1VX:APA91bHNZnOxn8A1k9K4aROmPhDwwb3a30K29vTOHxGRdrDUW2qE_TAPhhQDeQjhNBpEpKcNEAF1EYQGChJO-4AvRw3phYJEhTLJbJjNgT45AqKlx_dLnuc', 'OPPO CPH2505', '2025-12-15 12:55:38', '2025-12-15 12:55:38'),
(600001, 60002, 'dtVjqNKXR9WRfr8tEQ7EHJ:APA91bE3WG1ZY5_8JDIsgdF0K7H00jSWpMAi4coXIem1Un-rgYdIoNYUmT9-S8HTfyOI-mH9zgHwvYu_MZMQmCNw2qD4V9uNeKV9xtyj4rPQmWSmapI-Rfk', 'samsung SM-M346B', '2025-12-15 17:06:27', '2025-12-15 17:06:27'),
(600002, 360002, 'ctQHWhqETLWM2-JeNjZ4iL:APA91bHkS5hl3en2qh1IpE2wg00llJxyZXNdBHGujy2XCH2RjxcxMyMI6xmAszn4d70Kz9h6y7zUxbGGzTvHK33aRp_UftHCSZqAAT8N6qcZ6vCIQsgZ4Qk', 'OnePlus CPH2401', '2025-12-15 17:08:11', '2025-12-15 17:08:11'),
(630001, 60002, 'fkNTnG9jRkm85qM9uw8O5b:APA91bErtGtnxZJb-Rrp0SxHQJRMaQ4plQ-zsxHz5X87HZZtbowtOzP7KNt2B6w599G_GWjwVgybTUma2Dm0r3CAGQ1wujMr27poSc5QCUZ83E9bSPGPHyY', 'samsung SM-M346B', '2025-12-16 02:48:21', '2025-12-16 05:29:01'),
(720001, 60002, 'fCwFOENpQ7OGPqnkRd0as0:APA91bFMw9rk5TVoi7hjhsYu812uNxiAEg28rrxuc4c-Tw0eBDZ_pGptvrxhQ4bMaHqxuPLh_3J3_aZx5As5x03VjweVgWBa9JbdG5VLMfhuq8xkpam8A3w', 'samsung SM-M346B', '2025-12-16 06:06:26', '2025-12-16 06:06:26'),
(750001, 60002, 'd3wP92BbTV23DanGMlNoYb:APA91bFHm6K7b6DfS83KwwWaeJYPLf5L-LuFuSzTTnDkX48jmfQV79-hc_89PvUStpak5kV-LLBukzucWurk9HO7jp0gy35jyO3GUIJV8jKZvORU5LbSstg', 'samsung SM-M346B', '2025-12-16 06:18:39', '2025-12-16 06:18:39'),
(780001, 60002, 'c4eeaL1tQ3argKO3IDqxfL:APA91bFOpIhLF37yN1ld20hkeL0EFtPgio-thN3aLx18_ft_JDMw6a3ZxXCrBqckd6yZWIOZWYPIVe5JwwP_VPvJB5pixTtaOYFX73QM4EM4c7aq0czFvCY', 'samsung SM-M346B', '2025-12-16 07:22:25', '2025-12-16 10:22:34'),
(810003, 60002, 'dc0mu2KzRgS2fEer4sRGk7:APA91bEU72AQzGFFmMnSci2TDqD0zTbcsgc6UKsdCZsC9tPQ_uiSnWhOrJUQNC5gwrRWKuB6Ck3-B4LJl1DdCk1HZjVjBIU9mslLjlolAKeRJMbahDzwi8U', 'samsung SM-M346B', '2025-12-16 10:24:00', '2025-12-16 10:24:00'),
(810004, 60002, 'eujcXFQTTc-xE_fqfUMkZz:APA91bGFLrxhEZd7NEB-czf1tLtcmR17CdCvUlyH5mhgnj-0l84S8HYjF5_UG0ixcvPMhqj3dZhkFFFVvu9wWUENOkImw59jX0F2_u3o3FxtF7ed844sVAs', 'samsung SM-M346B', '2025-12-16 10:25:11', '2025-12-16 10:25:11'),
(840001, 390002, 'dad1s-kJTH2J0BxRg1MX0i:APA91bEG5_ER7fmSvE-P8Vx4X1yAGPoVf59cQ-KxROUguat-6vB0i_myP7pGhaAPy7YTOrDmz5IZDxTU1Dtjmm0IxMwIZbzA5txVMdMvBU7pETwjGdUW3KY', 'OPPO CPH2739', '2025-12-16 11:16:40', '2025-12-16 11:16:40'),
(870001, 420002, 'f79r6EYMQGyIY-AYKgaG4C:APA91bHdtROSGt2l02GSgxdvzCcOxB7WAY0qy9NlbmFDda7t02JjIXscaTy3HEoo6NUQNGFpNfSovT8irP1NqDelUuf6gYIjm1P2Gsw72sDmp5pLby-OgV0', 'vivo V2068', '2025-12-16 12:19:27', '2025-12-16 12:19:27'),
(900001, 120002, 'cUVXTNS-QG2h7DotZyznBn:APA91bH2HHEQwKWkpgZYVztiv_qPf9n6smtCPhBmqzpm9xMuNmF6jfN8zD3BWw5rC6Spn3rSU741f5Rt_pTBL4ArNm1HpnlYvxUuGhytn4Ya3Bmq6wHCCeU', 'samsung SM-M366B', '2025-12-16 13:06:12', '2025-12-16 13:06:12'),
(930001, 450002, 'fL2XJLUDRp2myKJfY0LsCv:APA91bFhwO0oaT_vdKp8vuupzWvkasZCEcFORnpahHNt9t9GSs1n1kXK3FAOB2fHK0-SMS193GBgUmB7v3AQjPHc2VW4rpcLvmEkIgisMosRZkL_KLpRrGg', 'Google sdk_gphone64_x86_64', '2025-12-16 16:49:30', '2025-12-16 16:49:30'),
(960001, 450002, 'eCufLBQlRaOj12PgFoQMLl:APA91bErz0yYejFU5OmTnqLvOPkY2j8Fky34-u8PBxbiBVpYSJmx1cDU46lVKoFnI1CWFGF9GdZI2i1PBwqE_t-HPgbRxiHQgKzfQWFS4Ix42vNot5cRRRk', 'Google sdk_gphone64_x86_64', '2025-12-16 17:25:14', '2025-12-16 17:25:14'),
(990001, 60002, 'c91H4OQWR_iM8usVthnGdr:APA91bFTGLUvfHS38U_7XWjZ3zWegPXujiHP0qE5KNT5LBJHW33Fh_FN18W-4egDMvgjKm5ELIOaq-zOUBdp7i5lczvWHKdDcUmPCoMQHODt_2K62g8d4Dg', 'samsung SM-M346B', '2025-12-17 03:12:32', '2025-12-17 03:23:04'),
(1020001, 60002, 'cQjHBl9VSXmL5Yn10mEjZ0:APA91bEn2W5glQVg9tI92dIWyprqil4NGgnijQpRuCe1LYELnQMAfCbPzqJCSJERAaI6P3O_fFMoV5Ol9WUCbn4eKK6S7-8BaUYNQgNB0ArhrsSxui_qlCU', 'samsung SM-M346B', '2025-12-17 05:11:27', '2025-12-17 11:55:45'),
(1080001, 480002, 'frCX3A87SdqTF1k4R-tOQs:APA91bEPKG320be9JjgRckVlYUgVyeaz8I_vsWZaR21h51dHywCGnUAmkQcU5SNh9U9xrzl7fCIMKZUxegQuUl-3uMqYpKhpWFVPLcn-TXE9IDSQloa_-n0', 'samsung SM-M115F', '2025-12-17 15:06:21', '2025-12-17 15:14:55'),
(1110001, 60002, 'd8Gnn6RYS9i5w83OZf16MY:APA91bEmIW6ZbIYxE1YJm91JEgZuAdt3eVkwBMT1379QR973V75hrnBMjovxU84cpMDPzG7Kozd8vWhfiAWTnqehWMb6eLopGFGP-ZiREBuRYhkjymLVMZw', 'samsung SM-M346B', '2025-12-17 15:58:11', '2025-12-17 16:00:49'),
(1110003, 60002, 'du0p5HUOT8aOrmDBUoEy-j:APA91bEK9lFtc1UAidH6jFUGLadu9-rb4HpYIsugzVVUWJWXyemMC3tkChfkOYtnJmebYYnjIe0HLxaUoTuCj9jLZCM7JPTFFCZT0f0xuBmGNXlTlkGS5pU', 'samsung SM-M346B', '2025-12-17 16:01:51', '2025-12-17 16:01:51'),
(1110004, 60002, 'fZpkGPLYQzG7pl1ZPFXD0C:APA91bHGO45fPgxAgTTF1xB5mTSHA2kBt2631GP2uK-ksLvlkPF0vGk8XUpAnhizrFNQmLKHyHRonI5gY3a1zhvlh4Mm-Q__xyZfLhttuk6fAqIKC7l50po', 'samsung SM-M346B', '2025-12-17 16:06:04', '2025-12-17 16:06:04'),
(1140001, 60002, 'doorw8S7RJu9uK5Idt22ha:APA91bGpm5LBeFfHyZ17hIiwSs1PrrkYTT7FpR4yZGez1NTHJXca00PFmW5tean0BQmx1G_tCnxl26x_6Hl0SSMotHMFF3ut7LEmIqbLBV_ll9IL7mzA9fo', 'samsung SM-M346B', '2025-12-17 16:32:00', '2025-12-17 16:32:00'),
(1170001, 60002, 'eavYiA8fQUiNB36h6vhgsr:APA91bGOpHEeLBYYH_WkLqBph4xwBbDemK0S3ML03Cix-MXJ_-7W_gelC9Xhw--6dgsXefstyIQ40_jG4cJ02abjwqkCKrw6hFemWCIMbsPvPWSvsWAbLM8', 'samsung SM-M346B', '2025-12-17 16:44:28', '2025-12-18 15:13:36'),
(1290001, 510002, 'cX5cUPb3RhiUPzh3kVYkQm:APA91bFkKWb0_rSY09WEyfIt6tESP5STqG2wQoLqfDQYQA-zFOlsuzSefvJ9U8mkJYfuXvkxFhnbgL5kOX-zIeMLN_pgYBVSTyeQp3hEC06x4YdtHqEBC5g', 'Google Pixel 7', '2025-12-18 09:37:54', '2025-12-18 09:37:54'),
(1470001, 60002, 'e4WavipETZegso1vq1p6p8:APA91bFIJc-y1QIzvwJHRPY8S_f1xbEghphNdvJixvW3gHw6X7hEc0epVU09kO8jixmd6OJh2sqeKonb1ULbCJ9gz_BAeXaJ79OrTuSqa0kK56eCox6X24o', 'samsung SM-M346B', '2025-12-20 11:13:21', '2025-12-20 13:56:14'),
(1620001, 60002, 'fDvEOWdFQ7SgDGkb8tbjRe:APA91bG4HrDMuj9y0MC03KNsp9_5vnioSKnQxpbMwKHiwMRwplxFQdkVOnLj6_VTCRQyIMmVplvb34l65K3oHkK5TMJLkfCayXLbyH7Gw53ZmVxsiPw4sUA', 'samsung SM-M346B', '2025-12-20 15:05:25', '2025-12-20 17:49:28'),
(1830001, 7020034431, 'fuki2G9RSAGfGg7Kg2hNon:APA91bH2lS-_hCcWEZ5qJH7xc-5vzefibpJ_Q38hai6muobk8t7U-K31JIDOoUo6vzNqImfdTYDQgC-7HFqrLqjSYqBrtNKDiDvRgTr9xUr1zIVFe8qF0M8', 'samsung SM-M346B', '2025-12-20 18:06:35', '2025-12-22 08:27:40'),
(1830002, 150002, 'cRdpNJ22S4moMDnOIVkmbq:APA91bHdENt9gRAMX5nxDA88uiJEOyJi7kKJe--bySDfwmkHeZN6QQDyDvsutUAOKrv04RbkWNTicImDdbgxLjcgk_jYbNOQS1Oe2kWsoWqWyldhmVlQiVs', 'Xiaomi 23127PN0CG', '2025-12-20 18:07:33', '2025-12-20 18:07:33'),
(1860001, 480002, 'e0MfNnJsT8m_v4Ucwy5fh4:APA91bG5ScuMPeVjop-5qYDnJmJzETyHRurE3WfmgOzxTcUH2iy30AFNLobznEChchmv_fKouYZVZOZUvdFDqm2RDGENiRCXdAO8b2T4guLpe-ppRp6eHsU', 'samsung SM-M115F', '2025-12-21 01:39:45', '2025-12-21 14:10:09'),
(2070001, 540002, 'enUJspLMQgC9y55CN1Yzn0:APA91bH6ImM16-N8n4Xx_m8j6WmutuluYPbKauxJO7s3N8a6gfihBDM0245hN6ya1DH-UpJJKL4NUMmZ_QjApCgWmSskXMGW5kBVsQVBkD6AntMesHdO2vc', 'samsung SM-M326B', '2025-12-21 13:16:23', '2025-12-21 13:16:23'),
(2190001, 7020034431, 'cx1zubogTJuQaLJutL9-6s:APA91bFhZCoyTQbSWWKfWCQ2XDPm-BN863Nj03OoC1Igw605Ka15558o-gz1WlsRYTDBeNKhHNjKtT6U8drxj9vHYfKcbOhXXy-tjtB3vVkLs8RYyABRNh8', 'samsung SM-M346B', '2025-12-22 10:31:10', '2025-12-22 13:01:55'),
(2250002, 7020034431, 'ftbqpATNT0iEqc9iyo6fzr:APA91bGC3Pj27-BKnRNf6gcUy5c2oBdjjyIpSxYS57BihBWcdjal9iITue-CmCaYxWj-XQlah8A9EPKlZA8Ame3W8-4MyIyz4W1ppSosIvdSzbadKf9ZOx4', 'samsung SM-M346B', '2025-12-22 13:06:47', '2025-12-23 03:45:27'),
(2250003, 120002, 'fEoEQjjWSCShZKXzN3WNKL:APA91bHaTqckzwqrLwwXC2ekU3HKB7kse5s0S0O0hECCi1YkEn1laLNL9N61Sg9ik4GVHQwOv_Xi5M6rObwjx1lGyjvPgPjPwswf3WD97QMZUz0BSA-q340', 'samsung SM-M366B', '2025-12-22 13:32:38', '2025-12-22 13:32:38'),
(2310001, 7020034431, 'erNyIkHYSHOykaG007ze0P:APA91bEaK6F9M4FFIW9Wb3dFPSnCNGVu0vRllIbPInetfcDkZo-lplYKL_-5VPRmkFP1auNsApLXw6WyhSTXQYsOmoimkJhbw7hDEw_wILVccKrxOfd1BFg', 'samsung SM-M346B', '2025-12-23 14:14:58', '2025-12-23 15:57:19'),
(2400001, 9595340263, 'fjuPfxmySr2ue9L7GvsotE:APA91bEysr3G09IrGD9pPNKOnwx4_7pxCwxyKmoRhUxG1xMfKODc_tjppG-hVSE0k9AyYOOOHt6f1RHrjFB0wgAOhcyb-wSI5du7lxxjK4aoJp8SxyIQdNI', 'samsung SM-M346B', '2025-12-23 16:32:48', '2025-12-23 17:26:43'),
(2460001, 7020034431, 'dS_Z5QgHQ_KjlxRVrP2ku9:APA91bHbhtJMkz0lV74Kcalf25UM5FmuYZmOdnnHLjqqFneAWoSvxfAuy8wezz2dq2nINVVx6q6VrGEfiWMVaHLBNEQ02DtusJt2sUvaaVq6oq9gh4rzlKg', 'samsung SM-M346B', '2025-12-23 17:41:12', '2025-12-24 05:34:59'),
(2589188, 9595340263, 'dqxpLckDT2W2NzDD3D6ZPv:APA91bFSpPNVTXqH5Re_6RtvquqRvg5g03Js1XZ1UmNBAQidqgeAitsDjshFu4wSYVDCzLSEAu2jBkiJBy-XNc4wInEuTiyRzZqxj1GakWJr6KuP7ttMNHQ', 'Google sdk_gphone64_x86_64', '2025-12-24 05:34:26', '2025-12-24 05:34:26'),
(2619188, 90002, 'esjEZ0FRS-qttsNhSY3VOQ:APA91bFWji4Xas3Uzbmb1RelANZdBHKUeayHbtL_dMuHy1zErBD29Xpjeo2DxjtwwtTu9yWXDcatt81PPLwtej4ff9HFapWHecNAQH1ugdTOpgvK6icOeGA', 'OPPO CPH2643', '2025-12-24 05:46:15', '2025-12-24 05:46:15'),
(2649188, 450002, 'egm3TnAlT6K4TVwDXSJaIm:APA91bFYgHyeYcCtc4U9Hyx_pjbuwpKhnS4rKyUz5oV1PsXWJB0c84k5h9VhS7dWZE6fFsdb-HFW3tFnuQLxw-ekXdcK7FW2XKHBHOmS_kQTHejnQ3YoHBM', 'samsung SM-M346B', '2025-12-25 10:52:30', '2025-12-25 17:48:21'),
(2709188, 9595340263, 'c5h9XY74QF-MQrcriy326T:APA91bFO8Zn6LMErfOOfvEDaoc5Gq6X_zsO4zlcMRLz8aC364c9tbFGQnQvYKD4QiO61-D9i-p6KJS5On-EIB72LLWYjV1VGM-HctYC20699NacjauUKhiM', 'samsung SM-M346B', '2025-12-26 02:59:01', '2025-12-26 02:59:01'),
(2739188, 7020034431, 'cjJLUW1XRYOb92TWshm5Oh:APA91bE_j2s6tIBUxfB-gJ4Xdeyk20Quhsd6686KjO0XUi-Bynhk7dMqhHcDp0mqSr2pSQNDsj-TAEPfJKuxkY_OUhuim1xkUdW_vceCpgk_yPdQfIFc4PU', 'samsung SM-A115F', '2025-12-26 13:27:23', '2025-12-26 13:27:23'),
(2769188, 9595370264, 'cWwgDSXVQWK0N9uHc6IeBS:APA91bH6ktrIl-ffZtF64b4Ew24_12XEduoRdClpAjTcxx8OEHnzTBcFRZTdvQz6zP65yxZ4JE0J4C6qBcU2DFjYFE_2Ufc1IoqBxkOh8st7nA8IN8aAL6g', 'Xiaomi 23124RN87I', '2025-12-26 14:18:32', '2025-12-26 14:18:32'),
(2769189, 9595370265, 'cXU3UgxLQE-1mn8_FnkSMm:APA91bEHXitBXF9rnewrvonFjnbeygbrTs4webxeupT7IwWvM7q4KPQfHbcxJOWV4uTKClX5Vy-rq6uHmf_C7hWt9UfVB273cpjLGrPi8uRdZxOcdhDJsrs', 'realme RMX3381', '2025-12-26 14:19:14', '2025-12-26 14:19:14'),
(2799188, 450002, 'c9fTgeQXRsKzvCajdrJ49r:APA91bGfglOUy23-FweG0WQYRtsGeF_5A1oIYjhXDJnQsHZQ3g-zm0TnicpuGtHtwfa-CRpuJvJa8F8llV4j2zehUxoKrME1SfhNaBk8kOqRI9XVufJsUXQ', 'samsung SM-M346B', '2025-12-26 14:28:52', '2025-12-27 10:54:50'),
(2829189, 9325872359, 'doEXXUUxQ6yO0bzoIkU6E-:APA91bFtRMJRF74Si7FkWUB1hPqvvMX4UnNsxZmaNBrekHUW88I7emVbZtsDgpQ9H025Qpk0ueUU9ynCqwMOVZAZzHwYsu9PR59JZU-k9ngqvmNQ5fQLt0M', 'motorola motorola edge 50 fusion', '2025-12-26 15:40:34', '2025-12-26 15:40:34'),
(2889371, 540002, 'cWurm7I2SxCJVfLwBLRsWO:APA91bGPHN7ncFeT6Mq-1S9XB7-cAv_wvMPA8sAaGBq_HFFEfKNeTXEvRdo_HD9tMmn8H3a0TwBNxkClVJ_-9lASD0ZAW_1W_P3EktKOuzn0ZUpr6a-MIfY', 'samsung SM-M326B', '2025-12-27 03:24:50', '2025-12-27 03:24:50'),
(2919371, 390002, 'ews5fN-VQ5e5XXS13gy1n6:APA91bHhFR3EJ3VBOt42gpOvXx1yZAX3zcWZBDiw1FnHNPbK1rVRFu-Xm5eww8N82818EyhA55hOEMExcCDOOdeQbOnaaKs1s8c3aKAF5wQVAyBaFHHMIw8', 'OPPO CPH2739', '2025-12-27 06:29:10', '2025-12-27 06:29:10'),
(2919372, 9359614612, 'eRI2OdFFSd2mdXrhU9LnoH:APA91bEgPEKFjfqyKkBn4T_wnSyXHJHzad9D3r8sLfRPFCJsKDrhUqZe0_4Pjd90iuDgSD4J3ynG-PK9Q7Stg4OfZX0fO9Gih4sdkJY7x8IaBSwRvC9keBk', 'samsung SM-J701F', '2025-12-27 06:37:00', '2025-12-27 06:37:00'),
(2949371, 150002, 'fEWfX66VQhGJpShlhV-9Tf:APA91bGZAuC9-5gax_Yratv7eIXGKnXixMPF7oJIoBZuoWpOBb7PI0PvhJK6G-Ps31W-xsr0AEKwVeFUMWUSvZTI02OMlXPD5tVvX_e3_FuB1L0XKdoptM8', 'Xiaomi 23127PN0CG', '2025-12-27 08:13:20', '2025-12-27 08:13:20'),
(2979373, 7507465080, 'fjoYFnnmTQifmKgmpio-Uj:APA91bHT9vUWeH0C6WKedlBUBbNvwFItD-Mh_07J4Qtb8og5go7p7chqsEuuchzbi0jG-xuvofxW1P2I60h58el96kRX7GC0q6ESgNa6AqQwNx0lOF6efo8', 'OnePlus CPH2401', '2025-12-27 11:05:21', '2025-12-27 11:05:21'),
(3009371, 9595340263, 'edyFYzk1TrumbaF0OeQXoQ:APA91bFo3puzhyDf_0crApglcQvaUqOXqoKRszWBkDTvHdN_0Y9kUBe4645Y8qDR-S0gBUvl5ECgZkrjN9igy99e0G5Nv1ipzoe9SG68CnxppoYnB3-DeXk', 'samsung SM-M346B', '2025-12-27 11:28:39', '2025-12-28 14:44:05'),
(3039371, 210002, 'd7zU796UR1ucuAkNocyD5q:APA91bF7DIiBOxqUy_aVqJHK_cH0zd5HQc5p1vorFriOMikpxnpQnIvjmyeM2-qvmWFBDlLFHZEIFDQ4Ql-iiwnsXEzwhIVZjcOoGigVC9F9MA5rKDHxnzA', 'vivo V2420', '2025-12-27 16:45:09', '2025-12-27 16:45:09'),
(3099371, 9595340263, 'dPcROL4eQM6vOBbxjwFkur:APA91bH2AIslsPIZs6lVNYtzcIN8rWJP3mt282RFnTV0W8_L-urdym52JjI7kHaUTxeimLLF87155F9W6JO7THDq_TXRPPRxy-Fn_63htgZRcx35ZVr3NDg', 'samsung SM-M346B', '2025-12-28 17:11:05', '2025-12-28 17:11:05'),
(3129371, 450002, 'fngBzNlQSOWV3uWUqzm3IG:APA91bHhTivKiYyqkiacGv3_P1vvPvzTUc7mGQ6CBwwAmYm_EjU2Qt3BDIlqq4ikYCsvqsD_GmV2nfqUJDPTbWfnyviZTyOMQO5DUGOkkr7Kn8nrYxnpBkE', 'samsung SM-M346B', '2025-12-28 18:45:48', '2025-12-28 18:52:49'),
(3159372, 9595340263, 'dqv1ZX88SsmHRIFrnjeun6:APA91bFmgLBLjSBPEP0oWLlk3bGm76ZSfOaM_2hizY5c0yTPCva0Kl5WAJP6dZCME6Yk2ppQis-Ys9A3nP-gAvw7lu3q3Ub8wXoYhJ79GzwVB06k6EQJtGE', 'samsung SM-M346B', '2025-12-28 18:55:49', '2025-12-28 18:55:49'),
(3159373, 9595340263, 'fV7dAVjHRGqP5qbqDHPvB5:APA91bHvHsg_l41DKTswB0ezzEvIF566kkz-hktSIqgPeJSaX8Cu4IGxS9PQ2u4uL_DgmMfcYjstT2-FZBPA7ASAK9orWX6A_43PgZWtlC6gPLhBkZoAJQ0', 'samsung SM-M346B', '2025-12-28 19:23:29', '2025-12-28 19:23:29'),
(3159374, 9595340263, 'cnZivrjYRMS9otRd4fh_zo:APA91bGveyRJRma5-ABvXlyxxktQ1N14wLAZ7yhZbJKzDwN4J8bCjymEAVZTlESyzvyJN9c6ThesKjqHhwPL6L6OrJqcu9hXSRbztoiL3rPiYnZzn7j1M04', 'samsung SM-M346B', '2025-12-28 19:29:04', '2025-12-28 19:29:04'),
(3202768, 9595340263, 'f4sTqpE4Q_GlKuptLRyxcc:APA91bHuXlPvGbGlL6bnKiIyyMlySNXOiO6S2IeTiMD2LPD3wtMx18r0Ded5OBXcnWR-Y5l3_XbcxeQj4TX1fQbdGdxctw80dSy6wGrwembdKrFEdmHQoS0', 'samsung SM-M346B', '2025-12-29 03:00:33', '2025-12-29 03:00:33'),
(3246172, 9595340263, 'crLUo2rQTomzIHEevqnjtz:APA91bE9or15VfoYM1h2tSkNVf-e43uJGRLcPSG5wRpo9op1ipLBTCEURIs8F_PT3QA4se0KhF4rY3zk-TifPCaSg8RUsgMRHqZnJPL2i-064cXJKLuK5Yo', 'samsung SM-M346B', '2025-12-29 03:11:51', '2025-12-29 03:11:51'),
(3276172, 7709082672, 'c5F7iQogRNCc4PvP18n-KM:APA91bF4JTIwSdeh2lDrQQLK6x6oiYdvdF0jp2ofoKPkg7TwkK_B27BDxVFLz2IdnxJLevkptfdQxpRnMwifUcwFB-zfYe_C_OfCdaC0jIojGP6eyWq2X_8', 'vivo vivo 1915', '2025-12-29 04:27:29', '2025-12-29 04:27:29'),
(3276173, 9595340263, 'fJmWggFFRkSxJ_cpqgbgU8:APA91bEjxCMeCI2xT4QkqeBJBTVrd-ddHKYfQUqUX5qpJ-DOE1Gz1bdyG40J6CwgDk1a1CzXBJSbU03o6QT2RXQRPDfkZhSoo10ZYjwbzuPPVOLTVGSAPAc', 'samsung SM-M346B', '2025-12-29 04:28:24', '2025-12-29 04:28:24'),
(3276174, 9595340263, 'epE1sejZSgCasiUrxS--JY:APA91bF2TklchGQgFt8NDbYaaqmNLg1jxw87_2lJO2t6hgfEKMRpTMYadmbuI9Njvt8Vo3Wl9jEMwqMPJiuUubyT0TkyF0xFmdIyIk4XsG_ol3U48Gp7X1w', 'samsung SM-M346B', '2025-12-29 04:54:42', '2025-12-29 05:33:34'),
(3276177, 210002, 'do7OgN1_S5yB3YcJThRp2z:APA91bGnCtzOCWuI0i1lnPVWYyVWIytfITCdsriKxLDhi_AQ0Ny9XNM-OU8ssAqeJNIflhQ8DdESZvvPHAgg8qLTBFRMBC9WPI8w1tpB744SAYBvp4Mgzv0', 'vivo V2339', '2025-12-29 05:39:25', '2025-12-29 05:39:25'),
(3306172, 9595340263, 'c-IpO7NwSRaS2CBVmBFx6p:APA91bEYcT3_xhg-c3JGPul1pirxjQbs9r6w9-6AS-TlvcT4-ohpmfnBdm9KYQC3gYb3Okge1mDArWYPqihZMTibKh1DVEzp-4SF9pUqUqV2BbVbGXbf59o', 'samsung SM-M346B', '2025-12-29 06:19:18', '2025-12-29 06:19:18'),
(3336172, 9595340263, 'eKwou8AjSP-EaeFmAZo_Uv:APA91bF2p14CJtSCaGIh1KrIfaSG1Wtsj9H7HkLV7Lq5x3gQX-0SWGI8-HrEwaCh41pZMZO9BcWRphsFQfrdMlY51qQLH7QIgz2NQpO5fnjAoggRvWx5-6I', 'samsung SM-M346B', '2025-12-29 10:31:34', '2025-12-29 10:31:34'),
(3366172, 8669702031, 'coIA8r3uSZmAdo57Eu7Pun:APA91bEANVKpNN70pciK7sg9lEMBX3N1IrbQagC7AOEDQ_nG_cEQSM9g-xftDxr8qrVEqJEuTm9ILqLV7V-_cEIom_iYvxVIWHGJCsFmMDKsd54CcfkjOjM', 'samsung SM-M346B', '2025-12-29 15:29:34', '2025-12-29 16:08:52'),
(3396173, 8767507375, 'fXM5DlHoTR-yJ6Yg8HnDLZ:APA91bH5tJlMapzhPrnRXLGsEGHeilGQ4SbtEIJKeVFGIAvXXyohmGZCtVWdbHaxYct7CMP_FV2_RGWbUaehyxwV3OFKUM3OnhRCrKS-L20kOkySRfITwwg', 'realme RMX3998', '2025-12-29 16:11:47', '2025-12-29 16:11:47'),
(3426172, 9595340263, 'fJWyPf9hR86-yDGqYaoFLi:APA91bGtEjFuV_tYY9IseNnMekDcFzw-Y2rsUylFZv-Hb9TnHwCHdeZOCvuzn0ydHFKM3WvgNZ93A_L6JgdTNp6BZxKScdwWA6zFGrw2eyTd1Sbtd3-Mm-4', 'samsung SM-M346B', '2025-12-29 17:44:59', '2025-12-29 17:44:59'),
(3456172, 9595340263, 'fBGVIcpARv2nJ7p4lj7fVW:APA91bHTRfOs1Nuu83fR2jg87hh7wtCIof-dz5kZYM3hOokMfeyQ9lBojTCEFr1QUQ1FAo-53NUP7pYDOwAD9OLZ25GamOPym2esXH4uV9Tg3BLvBr-SQMg', 'samsung SM-M346B', '2025-12-29 18:51:42', '2025-12-29 18:51:42'),
(3486172, 9595340263, 'danXjtENQomLpNUaSPib5z:APA91bENQXy4lhuL10_ZEnYgnQeBt65l4AunhkSoAaFRgRoZmt83aXezouKDGaiQTC1t-hx6D2l3xqqYEvFHPl9TQtjG2SZ7yJKLH1QwLahKuZvoT94t7tk', 'samsung SM-M346B', '2025-12-29 19:03:03', '2025-12-29 19:03:03'),
(3516172, 9595340263, 'cz7ynNlMS4-LulmjxdSOQx:APA91bEsgreoTKbKKi0JNtcyX896eNtxR0HsVfynNes45YB-HRFS9gLc6QD17BoRAF1Zxq3wQ859nAgREIVI_FbaEOI5frBqxNoD4YzjiNToCu2SzFwdb0E', 'samsung SM-M346B', '2025-12-29 19:09:58', '2025-12-29 19:09:58'),
(3516173, 7020034431, 'f8DwgNdRQeqqamTVstv1HA:APA91bEf_IvaboRweYiw0lv2RJbPvzo0Yh0Oz2Y1fYDGZGRpp2gVUD84gb6z2UdVaw95G_q18B4f-XjzWXPZ50pF6dvH1E0Yd86_6MLBJg6zMSe5NnfEYL8', 'samsung SM-M346B', '2025-12-29 19:15:14', '2025-12-30 03:07:35'),
(3576173, 7020034431, 'dL-xSt8HTDiPFmVwO_g9Nv:APA91bGtHG-X5q7LnD2O5qwnlRFtv0hh4A8Eh_OFaqDhvz3bO7M3DztFMTZw6o4bCqP85Lpy-EN9HiAvb7PYTXkDw4U0rXSFUEJHwvFA5tD9TBP3KYj5Djk', 'samsung SM-M346B', '2025-12-30 03:08:43', '2025-12-30 03:08:43'),
(3576174, 9595340263, 'ftvQk2iDRiaRU9OHmIXSM1:APA91bFyydFTPm1CkTcil4G47xxjax193mAAERxxDVS-bR7Q6LBRYCEdWjDfUARligyapO6tHO1VCKQv3cL-V8VVoFfX5luB2byYFHJLSqsIKw5-zM4K4W4', 'samsung SM-M346B', '2025-12-30 03:14:40', '2025-12-30 03:45:12'),
(3606173, 8669702031, 'elCAaeFQS-OCVHC1pBIWQb:APA91bEJbkYbF_bp83lUpXDK6L198Rf-7ka4dZ1pJM0hSSZS74TRVDm59xOFFsUgNKjs83xul_gmZlloOk_JHYEtVPeE73bSxq0KaZaOI-g03z5EN4cvkMw', 'samsung SM-M346B', '2025-12-30 03:55:39', '2025-12-30 03:59:28'),
(3606175, 8669702031, 'eyLTQgs2THi4N0rRwe1dun:APA91bGS2dV2yqrP_P0EJhxBJLioHlkiHJIbluzKxh3dRFpsOyw0fHazTgfCUu5DancoFp-dU-D7KmMwSuaJMkDICC_oqRV4A06OE3n52GjuOCNNXLko0yc', 'samsung SM-M346B', '2025-12-30 04:07:38', '2025-12-30 04:07:38');

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
-- Dumping data for table `user_notifications`
--

INSERT INTO `user_notifications` (`id`, `user_id`, `notification_id`, `title`, `body`, `type`, `deep_link`, `listing_id`, `is_read`, `created_at`) VALUES
(1, 120002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:33:50'),
(2, 60002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 1, '2025-12-21 02:33:50'),
(3, 150002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:33:51'),
(4, 210002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 1, '2025-12-21 02:33:51'),
(5, 240002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:33:51'),
(6, 270002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:33:51'),
(7, 330002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:33:51'),
(8, 360002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:33:51'),
(9, 390002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:33:51'),
(10, 420002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:33:51'),
(11, 450002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 1, '2025-12-21 02:33:51'),
(12, 480002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:33:51'),
(13, 510002, NULL, 'Test', 'Message here', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:33:51'),
(14, 120002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:36:40'),
(15, 60002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 1, '2025-12-21 02:36:41'),
(16, 150002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:36:41'),
(17, 210002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 1, '2025-12-21 02:36:41'),
(18, 240002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:36:41'),
(19, 270002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:36:41'),
(20, 330002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:36:41'),
(21, 360002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:36:41'),
(22, 390002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:36:41'),
(23, 420002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:36:41'),
(24, 450002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 1, '2025-12-21 02:36:41'),
(25, 480002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:36:41'),
(26, 510002, NULL, 'Test 2', 'Test2', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:36:41'),
(27, 120002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:03'),
(28, 60002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 1, '2025-12-21 02:45:03'),
(29, 150002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:03'),
(30, 210002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 1, '2025-12-21 02:45:03'),
(31, 240002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:03'),
(32, 270002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:03'),
(33, 330002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:03'),
(34, 360002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:03'),
(35, 390002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:04'),
(36, 420002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:04'),
(37, 450002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 1, '2025-12-21 02:45:04'),
(38, 480002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:04'),
(39, 510002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:04'),
(40, 120002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:18'),
(41, 60002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 1, '2025-12-21 02:45:18'),
(42, 150002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:18'),
(43, 210002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 1, '2025-12-21 02:45:18'),
(44, 240002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:18'),
(45, 270002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:19'),
(46, 330002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:19'),
(47, 360002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:19'),
(48, 390002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:19'),
(49, 420002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:19'),
(50, 450002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 1, '2025-12-21 02:45:19'),
(51, 480002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:19'),
(52, 510002, NULL, 'test3', 'test3', 'admin_broadcast', NULL, NULL, 0, '2025-12-21 02:45:19'),
(30001, 120002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:43:47'),
(30002, 60002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:43:47'),
(30003, 150002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:43:47'),
(30004, 210002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:43:47'),
(30005, 240002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:43:47'),
(30006, 270002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:43:48'),
(30007, 330002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:43:48'),
(30008, 360002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:43:48'),
(30009, 390002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:43:48'),
(30010, 420002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:43:48'),
(30011, 450002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:43:48'),
(30012, 480002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:43:48'),
(30013, 510002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:43:48'),
(30014, 7020034431, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:43:48'),
(30015, 540002, NULL, 'Test5', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:43:48'),
(60001, 120002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:55:43'),
(60002, 60002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:55:43'),
(60003, 150002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:55:43'),
(60004, 210002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:55:43'),
(60005, 240002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:55:43'),
(60006, 270002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:55:43'),
(60007, 330002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:55:43'),
(60008, 360002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:55:43'),
(60009, 390002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:55:43'),
(60010, 420002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:55:44'),
(60011, 450002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:55:44'),
(60012, 480002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:55:44'),
(60013, 510002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:55:44'),
(60014, 7020034431, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:55:44'),
(60015, 540002, NULL, 'test5', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:55:44'),
(60016, 1, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:25'),
(60017, 2, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:25'),
(60018, 3, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:25'),
(60019, 4, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:25'),
(60020, 30002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:25'),
(60021, 30005, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:25'),
(60022, 60002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:25'),
(60023, 90002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:26'),
(60024, 120002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:26'),
(60025, 150002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:26'),
(60026, 180002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:26'),
(60027, 210002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:56:26'),
(60028, 240002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:26'),
(60029, 270002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:26'),
(60030, 300002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:26'),
(60031, 330002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:26'),
(60032, 360002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:26'),
(60033, 390002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:26'),
(60034, 420002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:26'),
(60035, 450002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:56:26'),
(60036, 480002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:26'),
(60037, 510002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:27'),
(60038, 540002, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:56:27'),
(60039, 7020034431, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:56:27'),
(60040, 9595340263, NULL, 'test6', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:56:27'),
(60041, 1, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:35'),
(60042, 2, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:35'),
(60043, 3, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:35'),
(60044, 4, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:35'),
(60045, 30002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:35'),
(60046, 30005, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:35'),
(60047, 60002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:35'),
(60048, 90002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:35'),
(60049, 120002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:35'),
(60050, 150002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:35'),
(60051, 180002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:35'),
(60052, 210002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:57:35'),
(60053, 240002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:35'),
(60054, 270002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:36'),
(60055, 300002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:36'),
(60056, 330002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:36'),
(60057, 360002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:36'),
(60058, 390002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:36'),
(60059, 420002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:36'),
(60060, 450002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:57:36'),
(60061, 480002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:36'),
(60062, 510002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:36'),
(60063, 540002, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 09:57:36'),
(60064, 7020034431, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:57:36'),
(60065, 9595340263, NULL, 'test7', 'testing', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 09:57:36'),
(60066, 1, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:24'),
(60067, 2, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:24'),
(60068, 3, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:24'),
(60069, 4, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60070, 30002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60071, 30005, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60072, 60002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60073, 90002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60074, 120002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60075, 150002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60076, 180002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60077, 210002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:02:25'),
(60078, 240002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60079, 270002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60080, 300002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60081, 330002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60082, 360002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:25'),
(60083, 390002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:26'),
(60084, 420002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:26'),
(60085, 450002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:02:26'),
(60086, 480002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:26'),
(60087, 510002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:26'),
(60088, 540002, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:02:26'),
(60089, 7020034431, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:02:26'),
(60090, 9595340263, NULL, 'test8', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:02:26'),
(60091, 1, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:53'),
(60092, 2, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:53'),
(60093, 3, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:53'),
(60094, 4, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:53'),
(60095, 30002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:53'),
(60096, 30005, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:53'),
(60097, 60002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:53'),
(60098, 90002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:53'),
(60099, 120002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:53'),
(60100, 150002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:53'),
(60101, 180002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:53'),
(60102, 210002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:06:53'),
(60103, 240002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:54'),
(60104, 270002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:54'),
(60105, 300002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:54'),
(60106, 330002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:54'),
(60107, 360002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:54'),
(60108, 390002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:54'),
(60109, 420002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:54'),
(60110, 450002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:06:54'),
(60111, 480002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:54'),
(60112, 510002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:54'),
(60113, 540002, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:06:54'),
(60114, 7020034431, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:06:54'),
(60115, 9595340263, NULL, 'test9', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:06:54'),
(60116, 1, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:02'),
(60117, 2, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:02'),
(60118, 3, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:02'),
(60119, 4, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:02'),
(60120, 30002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:02'),
(60121, 30005, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:02'),
(60122, 60002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:02'),
(60123, 90002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:02'),
(60124, 120002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:02'),
(60125, 150002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:03'),
(60126, 180002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:03'),
(60127, 210002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:10:03'),
(60128, 240002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:03'),
(60129, 270002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:03'),
(60130, 300002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:03'),
(60131, 330002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:03'),
(60132, 360002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:03'),
(60133, 390002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:03'),
(60134, 420002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:03'),
(60135, 450002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:10:03'),
(60136, 480002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:03'),
(60137, 510002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:03'),
(60138, 540002, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:10:03'),
(60139, 7020034431, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:10:04'),
(60140, 9595340263, NULL, 'test10', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:10:04'),
(60141, 1, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:05'),
(60142, 2, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:05'),
(60143, 3, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:05'),
(60144, 4, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:05'),
(60145, 30002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:05'),
(60146, 30005, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60147, 60002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60148, 90002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60149, 120002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60150, 150002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60151, 180002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60152, 210002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:19:06'),
(60153, 240002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60154, 270002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60155, 300002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60156, 330002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60157, 360002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60158, 390002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60159, 420002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:06'),
(60160, 450002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:19:07'),
(60161, 480002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:07'),
(60162, 510002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:07'),
(60163, 540002, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:07'),
(60164, 7020034431, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:19:07'),
(60165, 9595340263, NULL, 'test11', 'test11', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:19:07'),
(60166, 1, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:43'),
(60167, 2, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:43'),
(60168, 3, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:43'),
(60169, 4, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:43'),
(60170, 30002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:43'),
(60171, 30005, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:43'),
(60172, 60002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:43'),
(60173, 90002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:43'),
(60174, 120002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:44'),
(60175, 150002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:44'),
(60176, 180002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:44'),
(60177, 210002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:19:44'),
(60178, 240002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:44'),
(60179, 270002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:44'),
(60180, 300002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:44'),
(60181, 330002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:44'),
(60182, 360002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:44'),
(60183, 390002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:44'),
(60184, 420002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:44'),
(60185, 450002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:19:44'),
(60186, 480002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:44'),
(60187, 510002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:44'),
(60188, 540002, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-22 10:19:45'),
(60189, 7020034431, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:19:45'),
(60190, 9595340263, NULL, 'test12', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-22 10:19:45'),
(90001, 1, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:16'),
(90002, 2, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:16'),
(90003, 3, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:16'),
(90004, 4, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:16'),
(90005, 30002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90006, 30005, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90007, 60002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90008, 90002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90009, 120002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90010, 150002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90011, 180002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90012, 210002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 1, '2025-12-29 12:00:17'),
(90013, 240002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90014, 270002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90015, 300002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90016, 330002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90017, 360002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90018, 390002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:17'),
(90019, 420002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90020, 450002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90021, 480002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90022, 510002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90023, 540002, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90024, 7020034431, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90025, 9595340263, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 1, '2025-12-29 12:00:18'),
(90026, 9595370264, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90027, 9595370265, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90028, 9325872359, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90029, 9359614612, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90030, 7507465080, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90031, 7709082672, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90032, 8669702031, NULL, 'TESTING NEW VERSION', 'TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:00:18'),
(90033, 1, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:47'),
(90034, 2, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:47'),
(90035, 3, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:47'),
(90036, 4, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:47'),
(90037, 30002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:47'),
(90038, 30005, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:47'),
(90039, 60002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:47'),
(90040, 90002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:47'),
(90041, 120002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:47'),
(90042, 150002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:47'),
(90043, 180002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:47'),
(90044, 210002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 1, '2025-12-29 12:25:47'),
(90045, 240002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:47'),
(90046, 270002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:48'),
(90047, 300002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:48'),
(90048, 330002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:48'),
(90049, 360002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:48'),
(90050, 390002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:48'),
(90051, 420002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:48'),
(90052, 450002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:48'),
(90053, 480002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:48'),
(90054, 510002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:48'),
(90055, 540002, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:48'),
(90056, 7020034431, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:48'),
(90057, 9595340263, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 1, '2025-12-29 12:25:48'),
(90058, 9595370264, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:48'),
(90059, 9595370265, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:49'),
(90060, 9325872359, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:49'),
(90061, 9359614612, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:49'),
(90062, 7507465080, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:49'),
(90063, 7709082672, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:49'),
(90064, 8669702031, NULL, 'FINAL TEST', 'FINAL TEST', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 12:25:49'),
(120001, 1, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:27'),
(120002, 2, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:27'),
(120003, 3, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:27'),
(120004, 4, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:27'),
(120005, 30002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120006, 30005, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120007, 60002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120008, 90002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120009, 120002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120010, 150002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120011, 180002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120012, 210002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120013, 240002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120014, 270002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120015, 300002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120016, 330002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120017, 360002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120018, 390002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:28'),
(120019, 420002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29'),
(120020, 450002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29'),
(120021, 480002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29'),
(120022, 510002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29'),
(120023, 540002, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29'),
(120024, 7020034431, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29'),
(120025, 9595340263, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 1, '2025-12-29 13:00:29'),
(120026, 9595370264, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29'),
(120027, 9595370265, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29'),
(120028, 9325872359, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29'),
(120029, 9359614612, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29'),
(120030, 7507465080, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29'),
(120031, 7709082672, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29'),
(120032, 8669702031, NULL, 'test', 'test', 'admin_broadcast', NULL, NULL, 0, '2025-12-29 13:00:29');

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=60001;

--
-- AUTO_INCREMENT for table `banners`
--
ALTER TABLE `banners`
  MODIFY `banner_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=399923;

--
-- AUTO_INCREMENT for table `calls`
--
ALTER TABLE `calls`
  MODIFY `call_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `cart_items`
--
ALTER TABLE `cart_items`
  MODIFY `cart_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1410001;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `category_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=240001;

--
-- AUTO_INCREMENT for table `cities`
--
ALTER TABLE `cities`
  MODIFY `city_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30001;

--
-- AUTO_INCREMENT for table `conversations`
--
ALTER TABLE `conversations`
  MODIFY `conversation_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `delivery_users`
--
ALTER TABLE `delivery_users`
  MODIFY `delivery_user_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30001;

--
-- AUTO_INCREMENT for table `enquiries`
--
ALTER TABLE `enquiries`
  MODIFY `enquiry_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=630001;

--
-- AUTO_INCREMENT for table `favorites`
--
ALTER TABLE `favorites`
  MODIFY `favorite_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30001;

--
-- AUTO_INCREMENT for table `gateway_devices`
--
ALTER TABLE `gateway_devices`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=120001;

--
-- AUTO_INCREMENT for table `listings`
--
ALTER TABLE `listings`
  MODIFY `listing_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=1520678;

--
-- AUTO_INCREMENT for table `listing_images`
--
ALTER TABLE `listing_images`
  MODIFY `image_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=270001;

--
-- AUTO_INCREMENT for table `listing_price_list`
--
ALTER TABLE `listing_price_list`
  MODIFY `item_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=120001;

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
  MODIFY `log_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=254005;

--
-- AUTO_INCREMENT for table `notification_settings`
--
ALTER TABLE `notification_settings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30001;

--
-- AUTO_INCREMENT for table `old_categories`
--
ALTER TABLE `old_categories`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=60002;

--
-- AUTO_INCREMENT for table `old_products`
--
ALTER TABLE `old_products`
  MODIFY `product_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=196252;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=690001;

--
-- AUTO_INCREMENT for table `order_items`
--
ALTER TABLE `order_items`
  MODIFY `order_item_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=690001;

--
-- AUTO_INCREMENT for table `otp_send_logs`
--
ALTER TABLE `otp_send_logs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2970001;

--
-- AUTO_INCREMENT for table `otp_verifications`
--
ALTER TABLE `otp_verifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3180001;

--
-- AUTO_INCREMENT for table `reviews`
--
ALTER TABLE `reviews`
  MODIFY `review_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=210002;

--
-- AUTO_INCREMENT for table `settings`
--
ALTER TABLE `settings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=60001;

--
-- AUTO_INCREMENT for table `shop_categories`
--
ALTER TABLE `shop_categories`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30002;

--
-- AUTO_INCREMENT for table `shop_products`
--
ALTER TABLE `shop_products`
  MODIFY `product_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=983950;

--
-- AUTO_INCREMENT for table `states`
--
ALTER TABLE `states`
  MODIFY `state_id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30002;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9595550264;

--
-- AUTO_INCREMENT for table `user_addresses`
--
ALTER TABLE `user_addresses`
  MODIFY `address_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=270001;

--
-- AUTO_INCREMENT for table `user_fcm_tokens`
--
ALTER TABLE `user_fcm_tokens`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3636172;

--
-- AUTO_INCREMENT for table `user_notifications`
--
ALTER TABLE `user_notifications`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=150001;

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
