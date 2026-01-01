# Firebase Cloud Functions for HelloHingoli Push Notifications

This folder contains Firebase Cloud Functions that send push notifications when new chat messages are received.

## Prerequisites

1. **Node.js 18** or higher
2. **Firebase CLI**: Install with `npm install -g firebase-tools`
3. **Firebase Blaze Plan**: Cloud Functions require the pay-as-you-go plan (free tier includes 2M invocations/month)

## Setup Instructions

### 1. Login to Firebase
```bash
firebase login
```

### 2. Install Dependencies
```bash
cd firebase-functions
npm install
```

### 3. Upgrade to Blaze Plan (if not already)
Go to [Firebase Console](https://console.firebase.google.com) → Your Project → Upgrade to Blaze plan

### 4. Deploy Functions
```bash
# From the MH directory (not firebase-functions)
cd ..
firebase deploy --only functions
```

### 5. Deploy Database Rules
```bash
firebase deploy --only database
```

## Functions

### `sendChatNotification`
Triggers when a new entry is added to `/notifications/{userId}/{notificationId}`.
Sends FCM push notification to the user's device.

### `onNewMessage`
Triggers when a new message is added to `/messages/{conversationId}/{messageId}`.
Automatically sends push notification to the recipient.

## Testing

1. Open the app on two devices with different user accounts
2. Send a message from one device
3. The other device should receive a push notification

## Monitoring

View logs in Firebase Console → Functions → Logs

Or via CLI:
```bash
firebase functions:log
```

## Troubleshooting

- **No notifications received**: Check if FCM token is saved in `/userTokens/{userId}`
- **Invalid token errors**: The function will auto-remove invalid tokens
- **Permission denied**: Make sure database rules allow read/write
