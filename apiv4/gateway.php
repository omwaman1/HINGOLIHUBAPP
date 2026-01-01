<?php
/**
 * Gateway API - Handles SMS Gateway device registration and OTP logging
 */

require_once __DIR__ . '/config/database.php';

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, X-Device-ID');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

$method = $_SERVER['REQUEST_METHOD'];
$path = $_GET['action'] ?? '';

try {
    switch ($path) {
        case 'register':
            handleRegisterDevice();
            break;
        case 'log-otp':
            handleLogOtp();
            break;
        case 'history':
            handleGetHistory();
            break;
        case 'update-settings':
            handleUpdateSettings();
            break;
        case 'get-settings':
            handleGetSettings();
            break;
        default:
            jsonResponse(['error' => 'Unknown action'], 404);
    }
} catch (PDOException $e) {
    error_log("Gateway API Error: " . $e->getMessage());
    jsonResponse(['error' => 'Database error'], 500);
}

/**
 * Register or update a gateway device
 */
function handleRegisterDevice(): void {
    $data = getJsonBody();
    $db = getDB();
    
    $deviceId = $data['device_id'] ?? '';
    $deviceName = $data['device_name'] ?? 'Unknown Device';
    $fcmToken = $data['fcm_token'] ?? '';
    $sim1Phone = $data['sim1_phone'] ?? null;
    $sim2Phone = $data['sim2_phone'] ?? null;
    
    if (empty($deviceId)) {
        jsonResponse(['error' => 'device_id is required'], 400);
    }
    
    $stmt = $db->prepare("
        INSERT INTO gateway_devices (device_id, device_name, fcm_token, sim1_phone, sim2_phone, status)
        VALUES (?, ?, ?, ?, ?, 'online')
        ON DUPLICATE KEY UPDATE
            device_name = VALUES(device_name),
            fcm_token = VALUES(fcm_token),
            sim1_phone = VALUES(sim1_phone),
            sim2_phone = VALUES(sim2_phone),
            status = 'online',
            last_active_at = CURRENT_TIMESTAMP
    ");
    $stmt->execute([$deviceId, $deviceName, $fcmToken, $sim1Phone, $sim2Phone]);
    
    // Get device settings
    $stmt = $db->prepare("SELECT * FROM gateway_devices WHERE device_id = ?");
    $stmt->execute([$deviceId]);
    $device = $stmt->fetch(PDO::FETCH_ASSOC);
    
    jsonResponse([
        'success' => true,
        'message' => 'Device registered',
        'device' => $device
    ]);
}

/**
 * Log an OTP send attempt
 */
function handleLogOtp(): void {
    $data = getJsonBody();
    $db = getDB();
    
    $deviceId = $data['device_id'] ?? '';
    $deviceName = $data['device_name'] ?? '';
    $senderPhone = $data['sender_phone'] ?? null;
    $recipientPhone = $data['recipient_phone'] ?? '';
    $otpCode = $data['otp_code'] ?? '';
    $status = $data['status'] ?? 'pending';
    $errorMessage = $data['error_message'] ?? null;
    $requestId = $data['request_id'] ?? null;
    
    if (empty($deviceId) || empty($recipientPhone)) {
        jsonResponse(['error' => 'device_id and recipient_phone are required'], 400);
    }
    
    $stmt = $db->prepare("
        INSERT INTO otp_send_logs 
        (device_id, device_name, sender_phone, recipient_phone, otp_code, status, error_message, request_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    ");
    $stmt->execute([
        $deviceId, $deviceName, $senderPhone, $recipientPhone, 
        $otpCode, $status, $errorMessage, $requestId
    ]);
    
    $logId = $db->lastInsertId();
    
    jsonResponse([
        'success' => true,
        'log_id' => $logId
    ]);
}

/**
 * Get OTP send history
 */
function handleGetHistory(): void {
    $db = getDB();
    
    $deviceId = $_GET['device_id'] ?? null;
    $limit = min((int)($_GET['limit'] ?? 100), 500);
    $offset = (int)($_GET['offset'] ?? 0);
    
    $sql = "SELECT * FROM otp_send_logs";
    $params = [];
    
    if ($deviceId) {
        $sql .= " WHERE device_id = ?";
        $params[] = $deviceId;
    }
    
    $sql .= " ORDER BY sent_at DESC LIMIT ? OFFSET ?";
    $params[] = $limit;
    $params[] = $offset;
    
    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    $logs = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Get counts
    $countSql = "SELECT 
        COUNT(*) as total,
        SUM(CASE WHEN status = 'sent' THEN 1 ELSE 0 END) as sent,
        SUM(CASE WHEN status = 'failed' THEN 1 ELSE 0 END) as failed
        FROM otp_send_logs";
    if ($deviceId) {
        $countSql .= " WHERE device_id = ?";
        $stmt = $db->prepare($countSql);
        $stmt->execute([$deviceId]);
    } else {
        $stmt = $db->query($countSql);
    }
    $counts = $stmt->fetch(PDO::FETCH_ASSOC);
    
    jsonResponse([
        'success' => true,
        'logs' => $logs,
        'counts' => $counts
    ]);
}

/**
 * Update device settings (SIM, template)
 */
function handleUpdateSettings(): void {
    $data = getJsonBody();
    $db = getDB();
    
    $deviceId = $data['device_id'] ?? '';
    
    if (empty($deviceId)) {
        jsonResponse(['error' => 'device_id is required'], 400);
    }
    
    $updates = [];
    $params = [];
    
    if (isset($data['active_sim'])) {
        $updates[] = "active_sim = ?";
        $params[] = (int)$data['active_sim'];
    }
    if (isset($data['sms_template'])) {
        $updates[] = "sms_template = ?";
        $params[] = $data['sms_template'];
    }
    if (isset($data['device_name'])) {
        $updates[] = "device_name = ?";
        $params[] = $data['device_name'];
    }
    if (isset($data['sim1_phone'])) {
        $updates[] = "sim1_phone = ?";
        $params[] = $data['sim1_phone'];
    }
    if (isset($data['sim2_phone'])) {
        $updates[] = "sim2_phone = ?";
        $params[] = $data['sim2_phone'];
    }
    
    if (empty($updates)) {
        jsonResponse(['error' => 'No fields to update'], 400);
    }
    
    $params[] = $deviceId;
    $sql = "UPDATE gateway_devices SET " . implode(", ", $updates) . " WHERE device_id = ?";
    
    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    
    jsonResponse(['success' => true, 'message' => 'Settings updated']);
}

/**
 * Get device settings
 */
function handleGetSettings(): void {
    $db = getDB();
    $deviceId = $_GET['device_id'] ?? '';
    
    if (empty($deviceId)) {
        jsonResponse(['error' => 'device_id is required'], 400);
    }
    
    $stmt = $db->prepare("SELECT * FROM gateway_devices WHERE device_id = ?");
    $stmt->execute([$deviceId]);
    $device = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$device) {
        jsonResponse(['error' => 'Device not found'], 404);
    }
    
    jsonResponse([
        'success' => true,
        'device' => $device
    ]);
}

function getJsonBody(): array {
    $json = file_get_contents('php://input');
    return json_decode($json, true) ?? [];
}

function jsonResponse(array $data, int $code = 200): void {
    http_response_code($code);
    echo json_encode($data);
    exit;
}
