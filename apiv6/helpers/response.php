<?php
/**
 * Response Helper Functions
 */

/**
 * Send JSON response
 */
function jsonResponse(array $data, int $statusCode = 200): void {
    http_response_code($statusCode);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit;
}

/**
 * Send success response
 */
function successResponse($data = null, string $message = 'Success', int $statusCode = 200): void {
    $response = [
        'success' => true,
        'message' => $message
    ];
    
    if ($data !== null) {
        $response['data'] = $data;
    }
    
    jsonResponse($response, $statusCode);
}

/**
 * Send error response
 */
function errorResponse(string $message, int $statusCode = 400, ?array $errors = null): void {
    $response = [
        'success' => false,
        'message' => $message
    ];
    
    if ($errors !== null) {
        $response['errors'] = $errors;
    }
    
    jsonResponse($response, $statusCode);
}

/**
 * Send paginated response
 */
function paginatedResponse(array $data, int $page, int $perPage, int $total): void {
    jsonResponse([
        'success' => true,
        'message' => 'Success',
        'data' => $data,
        'pagination' => [
            'page' => $page,
            'per_page' => $perPage,
            'total' => $total,
            'total_pages' => ceil($total / $perPage)
        ]
    ]);
}

/**
 * Get JSON body from request
 */
function getJsonBody(): array {
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
    return $data ?? [];
}

/**
 * Alias for getJsonBody - used by some functions
 */
function getJsonInput(): array {
    return getJsonBody();
}

/**
 * Get query parameter with default
 */
function getQueryParam(string $key, $default = null) {
    return $_GET[$key] ?? $default;
}

/**
 * Validate required fields
 */
function validateRequired(array $data, array $fields): ?array {
    $errors = [];
    
    foreach ($fields as $field) {
        if (!isset($data[$field]) || trim($data[$field]) === '') {
            $errors[$field] = ucfirst(str_replace('_', ' ', $field)) . ' is required';
        }
    }
    
    return empty($errors) ? null : $errors;
}
