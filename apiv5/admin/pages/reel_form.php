<?php
$reel = null;
$isEdit = false;
$editId = $_GET['id'] ?? $_POST['reel_id'] ?? null;

// Load existing reel for edit
if ($editId) {
    $isEdit = true;
    $stmt = $db->prepare("SELECT * FROM reels WHERE reel_id = ?");
    $stmt->execute([$editId]);
    $reel = $stmt->fetch();
    if (!$reel) {
        header("Location: index.php?page=reels&error=" . urlencode("Reel not found"));
        exit;
    }
}

// Handle AJAX Upload
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['ajax_upload'])) {
    // Enable error reporting to capture all errors
    error_reporting(E_ALL);
    ini_set('display_errors', 0);
    ini_set('log_errors', 1);
    
    // Set error handler to capture PHP errors
    set_error_handler(function($errno, $errstr, $errfile, $errline) {
        throw new ErrorException($errstr, 0, $errno, $errfile, $errline);
    });
    
    header('Content-Type: application/json');
    
    try {
        // Debug: Log upload info
        $debugInfo = [
            'files' => isset($_FILES['video_file']) ? [
                'name' => $_FILES['video_file']['name'] ?? 'N/A',
                'size' => $_FILES['video_file']['size'] ?? 'N/A',
                'error' => $_FILES['video_file']['error'] ?? 'N/A',
                'tmp_name' => isset($_FILES['video_file']['tmp_name']) ? 'SET' : 'NOT SET'
            ] : 'NOT SET',
            'upload_max_filesize' => ini_get('upload_max_filesize'),
            'post_max_size' => ini_get('post_max_size'),
            'memory_limit' => ini_get('memory_limit')
        ];
        
        if (empty($_FILES['video_file']['name'])) {
            throw new Exception('No video file selected. Debug: ' . json_encode($debugInfo));
        }
        
        if ($_FILES['video_file']['error'] !== UPLOAD_ERR_OK) {
            $uploadErrors = [
                UPLOAD_ERR_INI_SIZE => 'File exceeds upload_max_filesize (' . ini_get('upload_max_filesize') . ')',
                UPLOAD_ERR_FORM_SIZE => 'File exceeds MAX_FILE_SIZE directive',
                UPLOAD_ERR_PARTIAL => 'File was only partially uploaded',
                UPLOAD_ERR_NO_FILE => 'No file was uploaded',
                UPLOAD_ERR_NO_TMP_DIR => 'Missing temporary folder',
                UPLOAD_ERR_CANT_WRITE => 'Failed to write file to disk',
                UPLOAD_ERR_EXTENSION => 'A PHP extension stopped the upload'
            ];
            $errorCode = $_FILES['video_file']['error'];
            $errorMsg = $uploadErrors[$errorCode] ?? "Unknown error (code: $errorCode)";
            throw new Exception('Upload error: ' . $errorMsg);
        }
        
        $videoUrl = uploadReelVideo($_FILES['video_file']);
        echo json_encode(['success' => true, 'url' => $videoUrl, 'message' => 'Video uploaded successfully!']);
        
    } catch (Throwable $e) {
        // Catch both Exception and Error (PHP 7+)
        echo json_encode([
            'success' => false, 
            'error' => $e->getMessage(),
            'file' => basename($e->getFile()),
            'line' => $e->getLine()
        ]);
    }
    
    restore_error_handler();
    exit;
}

// Handle Form Submission (not AJAX)
if ($_SERVER['REQUEST_METHOD'] === 'POST' && !isset($_POST['ajax_upload'])) {
    ob_start(); // Start output buffering to prevent blank page issues
    try {
        $title = trim($_POST['title'] ?? '');
        $instagramUrl = trim($_POST['instagram_url'] ?? '');
        $videoUrl = trim($_POST['video_url'] ?? '');
        $thumbnailUrl = trim($_POST['thumbnail_url'] ?? '');
        $sortOrder = (int)($_POST['sort_order'] ?? 0);
        $status = $_POST['status'] ?? 'active';
        
        // Handle thumbnail upload (optional)
        if (!empty($_FILES['thumbnail_file']['name']) && $_FILES['thumbnail_file']['error'] === UPLOAD_ERR_OK) {
            try {
                $uploadedThumb = uploadImage($_FILES['thumbnail_file'], 'reels/thumbnails');
                if ($uploadedThumb) {
                    $thumbnailUrl = $uploadedThumb;
                }
            } catch (Exception $thumbErr) {
                // Ignore thumbnail error, continue with save
            }
        }
        
        if ($isEdit && $editId) {
            $stmt = $db->prepare("UPDATE reels SET title = ?, instagram_url = ?, video_url = ?, thumbnail_url = ?, sort_order = ?, status = ?, updated_at = NOW() WHERE reel_id = ?");
            $stmt->execute([$title, $instagramUrl ?: null, $videoUrl ?: null, $thumbnailUrl ?: null, $sortOrder, $status, $editId]);
            $msg = "Reel updated successfully!";
        } else {
            $stmt = $db->prepare("INSERT INTO reels (title, instagram_url, video_url, thumbnail_url, sort_order, status, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())");
            $stmt->execute([$title, $instagramUrl ?: null, $videoUrl ?: null, $thumbnailUrl ?: null, $sortOrder, $status]);
            $msg = "Reel added successfully!";
        }
        
        ob_end_clean(); // Clear any output
        header("Location: index.php?page=reels&msg=" . urlencode($msg));
        exit;
        
    } catch (Exception $e) {
        ob_end_clean();
        $error = "Error: " . $e->getMessage();
    }
}

/**
 * Upload reel video to Cloudflare R2
 */
function uploadReelVideo($file) {
    $allowedTypes = ['video/mp4', 'video/webm', 'video/quicktime'];
    $maxSize = 100 * 1024 * 1024; // 100MB max
    
    // Validate type
    $finfo = new finfo(FILEINFO_MIME_TYPE);
    $mimeType = $finfo->file($file['tmp_name']);
    if (!in_array($mimeType, $allowedTypes)) {
        throw new Exception('Invalid video type. Allowed: MP4, WebM, MOV');
    }
    
    // Validate size
    if ($file['size'] > $maxSize) {
        throw new Exception('Video too large. Maximum 100MB allowed.');
    }
    
    // Generate filename
    $extension = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
    if ($extension === 'quicktime' || $extension === 'mov') $extension = 'mp4';
    $filename = 'reels/reel_' . uniqid() . '_' . time() . '.' . $extension;
    
    // Read file content
    $fileContent = file_get_contents($file['tmp_name']);
    
    // R2 Configuration
    $r2Config = [
        'endpoint' => 'https://62b435cd6e08605f2c7c1aadedc6a591.r2.cloudflarestorage.com',
        'accessKeyId' => '6d12f3c5c7a0b68722e46063c8befec4',
        'secretAccessKey' => 'fd01dd18c77b8de4ccbf036b4dfafdaa062c5a32685dc8b482ac40d0a9d50d60',
        'bucket' => 'hello-hingoli-bucket',
        'publicUrl' => 'https://pub-f50d30ee223d4536a0ce3f175f922495.r2.dev'
    ];
    
    // Upload to R2
    $result = uploadToR2($r2Config, $filename, $fileContent, 'video/mp4');
    
    if ($result['success']) {
        return $r2Config['publicUrl'] . '/' . $filename;
    }
    
    throw new Exception('Failed to upload video to R2: ' . ($result['error'] ?? 'Unknown error'));
}
?>

<div class="header">
    <div class="page-title"><?= $isEdit ? '✏️ Edit Reel' : '➕ Add Reel' ?></div>
    <a href="index.php?page=reels" class="btn btn-outline">← Back to Reels</a>
</div>

<?php if (isset($error)): ?>
<div class="alert alert-error" id="errorAlert"><?= htmlspecialchars($error) ?></div>
<?php endif; ?>

<!-- Upload Progress Overlay -->
<div id="uploadOverlay" style="display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.8); z-index: 9999; justify-content: center; align-items: center;">
    <div style="background: var(--card-bg); padding: 2rem; border-radius: 12px; width: 400px; text-align: center;">
        <h3 style="margin: 0 0 1rem 0;">📤 Uploading Video...</h3>
        <div style="background: var(--bg); border-radius: 8px; height: 20px; overflow: hidden; margin-bottom: 1rem;">
            <div id="progressBar" style="width: 0%; height: 100%; background: linear-gradient(90deg, var(--primary), var(--primary-hover)); transition: width 0.3s;"></div>
        </div>
        <p id="progressText" style="color: var(--text-light); margin: 0;">0% - Starting upload...</p>
    </div>
</div>

<!-- Toast Notification -->
<div id="toast" style="display: none; position: fixed; bottom: 20px; right: 20px; padding: 1rem 1.5rem; border-radius: 8px; z-index: 9999; font-weight: 500; animation: slideIn 0.3s;">
</div>

<div class="card">
    <form method="POST" enctype="multipart/form-data" id="reelForm">
        <?php if ($isEdit && $editId): ?>
        <input type="hidden" name="reel_id" value="<?= htmlspecialchars($editId) ?>">
        <?php endif; ?>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem;">
            <div>
                <label style="display: block; margin-bottom: 0.5rem; font-weight: 500;">📝 Title</label>
                <input type="text" name="title" value="<?= htmlspecialchars($reel['title'] ?? '') ?>" 
                       placeholder="Reel title (optional)" style="width: 100%;">
            </div>
            <div>
                <label style="display: block; margin-bottom: 0.5rem; font-weight: 500;">📊 Sort Order</label>
                <input type="number" name="sort_order" value="<?= $reel['sort_order'] ?? 0 ?>" 
                       placeholder="0" style="width: 100%;">
            </div>
        </div>
        
        <!-- Video Upload Section -->
        <div style="margin-bottom: 1rem; padding: 1.5rem; border: 2px dashed var(--border); border-radius: 12px; background: rgba(99, 102, 241, 0.05);">
            <label style="display: block; margin-bottom: 0.75rem; font-weight: 600; font-size: 1.1rem;">📹 Upload Video File</label>
            <input type="file" name="video_file" id="videoFile" accept="video/mp4,video/webm,video/quicktime" style="width: 100%; padding: 0.5rem;">
            <div style="display: flex; gap: 1rem; margin-top: 0.75rem; align-items: center;">
                <button type="button" id="uploadBtn" class="btn btn-primary" style="display: none;">
                    ⬆️ Upload to R2
                </button>
                <span id="fileInfo" style="color: var(--text-light); font-size: 0.9rem;"></span>
            </div>
            <small style="color: var(--text-light); display: block; margin-top: 0.5rem;">Max 100MB. Supported: MP4, WebM, MOV</small>
        </div>
        
        <div style="margin-bottom: 1rem;">
            <label style="display: block; margin-bottom: 0.5rem; font-weight: 500;">🔗 Video URL (R2/CDN)</label>
            <input type="url" name="video_url" id="videoUrl" value="<?= htmlspecialchars($reel['video_url'] ?? '') ?>" 
                   placeholder="https://pub-xxx.r2.dev/reels/video.mp4" style="width: 100%;">
            <small style="color: var(--text-light);">Auto-filled after upload, or paste direct URL</small>
        </div>
        
        <div style="margin-bottom: 1rem;">
            <label style="display: block; margin-bottom: 0.5rem; font-weight: 500;">📸 Instagram URL (Reference)</label>
            <input type="url" name="instagram_url" value="<?= htmlspecialchars($reel['instagram_url'] ?? '') ?>" 
                   placeholder="https://www.instagram.com/reel/XXX/" style="width: 100%;">
        </div>
        
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1rem;">
            <div>
                <label style="display: block; margin-bottom: 0.5rem; font-weight: 500;">🖼️ Thumbnail URL</label>
                <input type="url" name="thumbnail_url" value="<?= htmlspecialchars($reel['thumbnail_url'] ?? '') ?>" 
                       placeholder="https://..." style="width: 100%;">
            </div>
            <div>
                <label style="display: block; margin-bottom: 0.5rem; font-weight: 500;">Or Upload Thumbnail</label>
                <input type="file" name="thumbnail_file" accept="image/*" style="width: 100%;">
            </div>
        </div>
        
        <div style="margin-bottom: 1.5rem;">
            <label style="display: block; margin-bottom: 0.5rem; font-weight: 500;">🔄 Status</label>
            <select name="status" style="width: 200px;">
                <option value="active" <?= ($reel['status'] ?? 'active') === 'active' ? 'selected' : '' ?>>✅ Active</option>
                <option value="inactive" <?= ($reel['status'] ?? '') === 'inactive' ? 'selected' : '' ?>>⏸️ Inactive</option>
            </select>
        </div>
        
        <?php if ($isEdit && $reel['video_url']): ?>
        <div style="margin-bottom: 1.5rem; padding: 1rem; background: var(--bg); border-radius: 8px;">
            <label style="display: block; margin-bottom: 0.5rem; font-weight: 500;">📺 Current Video</label>
            <video src="<?= htmlspecialchars($reel['video_url']) ?>" controls style="max-width: 300px; max-height: 400px; border-radius: 8px;"></video>
        </div>
        <?php endif; ?>
        
        <div style="display: flex; gap: 1rem; align-items: center;">
            <button type="submit" class="btn btn-primary" style="padding: 0.75rem 2rem; font-size: 1rem;">
                <?= $isEdit ? '💾 Save Changes' : '➕ Add Reel' ?>
            </button>
            <a href="index.php?page=reels" class="btn btn-outline">Cancel</a>
        </div>
    </form>
</div>

<style>
@keyframes slideIn {
    from { transform: translateX(100px); opacity: 0; }
    to { transform: translateX(0); opacity: 1; }
}
.toast-success { background: var(--success); color: white; }
.toast-error { background: var(--danger); color: white; }
.toast-info { background: var(--primary); color: white; }
</style>

<script>
const videoFile = document.getElementById('videoFile');
const uploadBtn = document.getElementById('uploadBtn');
const fileInfo = document.getElementById('fileInfo');
const videoUrl = document.getElementById('videoUrl');
const overlay = document.getElementById('uploadOverlay');
const progressBar = document.getElementById('progressBar');
const progressText = document.getElementById('progressText');

// Show toast notification
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.className = 'toast-' + type;
    toast.innerHTML = (type === 'success' ? '✅ ' : type === 'error' ? '❌ ' : 'ℹ️ ') + message;
    toast.style.display = 'block';
    setTimeout(() => { toast.style.display = 'none'; }, 5000);
}

// File selected
videoFile.addEventListener('change', function() {
    if (this.files.length > 0) {
        const file = this.files[0];
        const sizeMB = (file.size / 1024 / 1024).toFixed(2);
        
        if (file.size > 100 * 1024 * 1024) {
            fileInfo.innerHTML = '<span style="color: var(--danger);">❌ File too large! Max 100MB</span>';
            uploadBtn.style.display = 'none';
            return;
        }
        
        fileInfo.innerHTML = `📁 ${file.name} (${sizeMB} MB)`;
        uploadBtn.style.display = 'inline-flex';
    } else {
        fileInfo.innerHTML = '';
        uploadBtn.style.display = 'none';
    }
});

// Upload button click
uploadBtn.addEventListener('click', function() {
    const file = videoFile.files[0];
    if (!file) return;
    
    const formData = new FormData();
    formData.append('video_file', file);
    formData.append('ajax_upload', '1');
    
    // Show overlay
    overlay.style.display = 'flex';
    progressBar.style.width = '0%';
    progressText.textContent = '0% - Starting upload...';
    
    const xhr = new XMLHttpRequest();
    
    // Progress
    xhr.upload.addEventListener('progress', function(e) {
        if (e.lengthComputable) {
            const percent = Math.round((e.loaded / e.total) * 100);
            progressBar.style.width = percent + '%';
            
            if (percent < 100) {
                const sizeMB = (e.loaded / 1024 / 1024).toFixed(1);
                const totalMB = (e.total / 1024 / 1024).toFixed(1);
                progressText.textContent = `${percent}% - Uploading ${sizeMB}MB / ${totalMB}MB`;
            } else {
                progressText.textContent = '100% - Processing on server...';
            }
        }
    });
    
    // Complete
    xhr.addEventListener('load', function() {
        overlay.style.display = 'none';
        
        try {
            const response = JSON.parse(xhr.responseText);
            if (response.success) {
                videoUrl.value = response.url;
                showToast(response.message || 'Video uploaded successfully!', 'success');
                uploadBtn.style.display = 'none';
                fileInfo.innerHTML = '<span style="color: var(--success);">✅ Uploaded!</span>';
            } else {
                showToast(response.error || 'Upload failed!', 'error');
            }
        } catch (e) {
            showToast('Server error: ' + e.message, 'error');
        }
    });
    
    // Error
    xhr.addEventListener('error', function() {
        overlay.style.display = 'none';
        showToast('Network error - upload failed!', 'error');
    });
    
    xhr.open('POST', window.location.href);
    xhr.send(formData);
});
</script>
