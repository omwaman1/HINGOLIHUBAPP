<?php
// Simple like test
$baseUrl = 'https://hellohingoli.com/apiv5';

// Login
$ch = curl_init("$baseUrl/auth/login");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode(['phone' => '9595340263', 'password' => 'password']));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
$loginResponse = json_decode(curl_exec($ch), true);
curl_close($ch);
$token = $loginResponse['data']['access_token'];

echo "Token: " . substr($token, 0, 30) . "...\n";

// Like reel 30001
$ch = curl_init("$baseUrl/reels/like");
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode(['reel_id' => 30001]));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json', 'Authorization: Bearer ' . $token]);
curl_setopt($ch, CURLOPT_VERBOSE, true);
$responseBody = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$contentType = curl_getinfo($ch, CURLINFO_CONTENT_TYPE);
curl_close($ch);

echo "HTTP: $httpCode\n";
echo "Content-Type: $contentType\n";
echo "Response Body: " . ($responseBody ?: "(empty)") . "\n";

if ($httpCode == 200) {
    $data = json_decode($responseBody, true);
    print_r($data);
}
