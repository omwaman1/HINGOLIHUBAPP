import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

// Initialize Firebase Admin SDK
admin.initializeApp();

const db = admin.database();
const messaging = admin.messaging();

/**
 * Cloud Function that triggers when a new notification is created
 * Sends FCM push notification to the user's device
 */
export const sendChatNotification = functions.database
    .ref("/notifications/{userId}/{notificationId}")
    .onCreate(async (snapshot, context) => {
        const userId = context.params.userId;
        const notificationId = context.params.notificationId;
        const notificationData = snapshot.val();

        console.log("========================================");
        console.log(`📩 NEW NOTIFICATION TRIGGER`);
        console.log(`   Target User ID: ${userId}`);
        console.log(`   Notification ID: ${notificationId}`);
        console.log(`   Type: ${notificationData?.type}`);
        console.log(`   Title: ${notificationData?.title}`);
        console.log(`   Body: ${notificationData?.body}`);
        console.log(`   Data:`, JSON.stringify(notificationData));
        console.log("========================================");

        try {
            // Get the user's FCM token
            console.log(`🔑 Looking up FCM token at /userTokens/${userId}`);
            const tokenSnapshot = await db.ref(`/userTokens/${userId}`).once("value");
            const fcmToken = tokenSnapshot.val();

            if (!fcmToken) {
                console.log(`❌ No FCM token found for user ${userId} - Cannot send notification!`);
                return null;
            }

            console.log(`✅ FCM token found for user ${userId}: ${fcmToken.substring(0, 20)}...`);

            // Check if this is a call invitation or cancellation - needs special handling
            const isCallInvitation = notificationData.type === "call_invitation";
            const isCallCancelled = notificationData.type === "call_cancelled";

            let message: admin.messaging.Message;

            if (isCallInvitation) {
                // CALL INVITATIONS: Send DATA-ONLY message (no 'notification' block)
                // This ensures onMessageReceived is called even when app is in background
                // so we can show custom notification with Answer/Decline buttons
                console.log(`📞 CALL INVITATION - Sending DATA-ONLY message for custom notification`);

                message = {
                    token: fcmToken,
                    // NO notification block - forces onMessageReceived to be called
                    data: {
                        title: notificationData.title || "Incoming Call",
                        body: notificationData.body || "Incoming voice call",
                        conversationId: notificationData.conversationId || "",
                        senderName: notificationData.senderName || notificationData.title || "",
                        type: "call_invitation",
                        callId: notificationData.callId || "",
                        callerId: String(notificationData.callerId || ""),
                    },
                    android: {
                        priority: "high",
                        ttl: 60 * 1000, // 60 seconds TTL for calls
                    },
                };
            } else if (isCallCancelled) {
                // CALL CANCELLED: Send DATA-ONLY message to dismiss notification
                // This allows onMessageReceived to cancel the notification and stop ringtone
                console.log(`📞 CALL CANCELLED - Sending DATA-ONLY message to dismiss notification`);

                message = {
                    token: fcmToken,
                    data: {
                        title: notificationData.title || "Call Cancelled",
                        body: notificationData.body || "Call was cancelled",
                        conversationId: notificationData.conversationId || "",
                        type: "call_cancelled",
                        callId: notificationData.callId || "",
                    },
                    android: {
                        priority: "high",
                        ttl: 30 * 1000, // 30 seconds TTL for cancellation
                    },
                };
            } else {
                // REGULAR NOTIFICATIONS: Send with notification block (Android handles display)
                message = {
                    token: fcmToken,
                    notification: {
                        title: notificationData.title || "New Message",
                        body: notificationData.body || "You have a new message",
                    },
                    data: {
                        conversationId: notificationData.conversationId || "",
                        senderName: notificationData.senderName || notificationData.title || "",
                        type: notificationData.type || "message",
                        callId: notificationData.callId || "",
                        callerId: String(notificationData.callerId || ""),
                        click_action: "FLUTTER_NOTIFICATION_CLICK",
                    },
                    android: {
                        priority: "high",
                        notification: {
                            channelId: "chat_notifications",
                            priority: "max",
                            defaultSound: true,
                            defaultVibrateTimings: true,
                            visibility: "public",
                        },
                    },
                };
            }

            console.log(`📤 Sending FCM message...`);

            // Send the notification
            const response = await messaging.send(message);
            console.log(`✅ Successfully sent notification to user ${userId}!`);
            console.log(`   FCM Response: ${response}`);

            // Delete the notification from database after sending
            await snapshot.ref.remove();
            console.log(`🗑️ Notification ${notificationId} removed after sending`);

            return response;
        } catch (error: any) {
            console.log("========================================");
            console.error(`❌ ERROR sending notification to user ${userId}`);
            console.error(`   Error Code: ${error?.code}`);
            console.error(`   Error Message: ${error?.message}`);
            console.log("========================================");

            // If token is invalid, remove it
            if (error?.code === "messaging/invalid-registration-token" ||
                error?.code === "messaging/registration-token-not-registered") {
                console.log(`🗑️ Removing invalid FCM token for user ${userId}`);
                await db.ref(`/userTokens/${userId}`).remove();
            }

            return null;
        }
    });

/**
 * Alternative: Trigger on new message creation
 * This sends notifications directly when a message is added
 */
export const onNewMessage = functions.database
    .ref("/messages/{conversationId}/{messageId}")
    .onCreate(async (snapshot, context) => {
        const conversationId = context.params.conversationId;
        const messageId = context.params.messageId;
        const messageData = snapshot.val();

        console.log(`New message in conversation ${conversationId}:`, messageData);

        try {
            // Get conversation to find participants
            const convSnapshot = await db.ref(`/conversations/${conversationId}`).once("value");
            const conversation = convSnapshot.val();

            if (!conversation || !conversation.participantIds) {
                console.log("Conversation not found or no participants");
                return null;
            }

            // Find the recipient (other participant)
            const senderId = messageData.senderId;
            const recipientId = conversation.participantIds.find(
                (id: number) => id !== senderId
            );

            if (!recipientId) {
                console.log("No recipient found");
                return null;
            }

            // Get recipient's FCM token
            const tokenSnapshot = await db.ref(`/userTokens/${recipientId}`).once("value");
            const fcmToken = tokenSnapshot.val();

            if (!fcmToken) {
                console.log(`No FCM token for recipient ${recipientId}`);
                return null;
            }

            // Get sender name (try from conversation data)
            let senderName = "Someone";
            const senderNameKey = `participant_${senderId}_name`;
            if (conversation[senderNameKey]) {
                senderName = conversation[senderNameKey];
            }

            // Prepare notification
            const messageText = messageData.type === "listing_card"
                ? "Sent a listing inquiry"
                : messageData.text;

            const message: admin.messaging.Message = {
                token: fcmToken,
                notification: {
                    title: senderName,
                    body: messageText,
                },
                data: {
                    conversationId: conversationId,
                    messageId: messageId,
                    senderName: senderName,
                    type: "chat_message",
                },
                android: {
                    priority: "high",
                    notification: {
                        channelId: "chat_notifications",
                        priority: "high",
                        defaultSound: true,
                    },
                },
            };

            const response = await messaging.send(message);
            console.log(`Notification sent to ${recipientId}:`, response);

            return response;
        } catch (error) {
            console.error("Error sending message notification:", error);
            return null;
        }
    });

/**
 * Cloud Function to send OTP via SMS Gateway
 * Triggers when a new OTP request is created at /otp_requests/{requestId}
 * Sends FCM to the gateway device which then sends the SMS
 */
export const sendOtpViaGateway = functions.database
    .ref("/otp_requests/{requestId}")
    .onCreate(async (snapshot, context) => {
        const requestId = context.params.requestId;
        const data = snapshot.val();

        console.log("========================================");
        console.log(`📱 OTP REQUEST RECEIVED`);
        console.log(`   Request ID: ${requestId}`);
        console.log(`   Phone: ${data?.phone}`);
        console.log(`   OTP: ${data?.otp}`);
        console.log("========================================");

        try {
            // Get the gateway device's FCM token
            const gatewaySnapshot = await db.ref("/gateway_devices/primary").once("value");
            const gatewayData = gatewaySnapshot.val();

            if (!gatewayData?.token) {
                console.error("❌ No gateway device registered!");
                await snapshot.ref.update({ status: "no_gateway", error: "No gateway device" });
                return null;
            }

            console.log(`✅ Gateway device found, sending FCM...`);

            // Send FCM to gateway device
            const message: admin.messaging.Message = {
                token: gatewayData.token,
                data: {
                    type: "otp_request",
                    phone: data.phone,
                    otp: data.otp,
                    requestId: requestId,
                },
                android: {
                    priority: "high",
                },
            };

            const response = await messaging.send(message);
            console.log(`✅ FCM sent to gateway: ${response}`);

            // Update status
            await snapshot.ref.update({
                status: "sent_to_gateway",
                sentAt: Date.now(),
            });

            return response;
        } catch (error: any) {
            console.error(`❌ Error sending OTP via gateway:`, error);
            await snapshot.ref.update({
                status: "error",
                error: error?.message || "Unknown error",
            });

            // If token is invalid, mark gateway as offline
            if (error?.code === "messaging/invalid-registration-token" ||
                error?.code === "messaging/registration-token-not-registered") {
                await db.ref("/gateway_devices/primary/status").set("offline");
            }

            return null;
        }
    });

