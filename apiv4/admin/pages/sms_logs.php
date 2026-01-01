<?php
$logs = $db->query("SELECT * FROM otp_send_logs ORDER BY created_at DESC LIMIT 100")->fetchAll();
?>

<div class="header">
    <div class="page-title">📱 SMS Logs</div>
</div>

<div class="card" style="overflow-x: auto;">
    <?php if (empty($logs)): ?>
    <p style="text-align: center; color: var(--text-light); padding: 2rem;">No SMS logs found.</p>
    <?php else: ?>
    <table>
        <thead>
            <tr>
                <th>Date</th>
                <th>Phone</th>
                <th>Type</th>
                <th>Status</th>
                <th>Message ID</th>
            </tr>
        </thead>
        <tbody>
        <?php foreach ($logs as $log): ?>
        <tr>
            <td><?= date('d M Y, h:i A', strtotime($log['created_at'])) ?></td>
            <td><?= htmlspecialchars($log['phone'] ?? $log['mobile_number'] ?? '-') ?></td>
            <td><?= $log['otp_type'] ?? 'OTP' ?></td>
            <td>
                <span class="badge" style="background: <?= ($log['status'] ?? '') === 'sent' ? 'rgba(34,197,94,0.1);color:#22c55e' : 'rgba(239,68,68,0.1);color:#ef4444' ?>">
                    <?= ucfirst($log['status'] ?? 'Unknown') ?>
                </span>
            </td>
            <td style="font-family: monospace; font-size: 0.85rem;"><?= $log['message_id'] ?? '-' ?></td>
        </tr>
        <?php endforeach; ?>
        </tbody>
    </table>
    <?php endif; ?>
</div>
