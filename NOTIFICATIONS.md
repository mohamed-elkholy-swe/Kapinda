# Kapinda push notifications

## Configuration

Set these public settings in `web/.env.local` locally and in the web host's build environment for production:

```
VITE_FIREBASE_MESSAGING_SENDER_ID=<Firebase project number / messagingSenderId from web app config>
VITE_FIREBASE_VAPID_KEY=<public Web Push certificate key>
```

Keep the existing Firebase API key, auth domain, project ID, and app ID. Rebuild/restart the website after environment changes. Enable Firebase Cloud Messaging API (HTTP v1) and, if token registration reports an API error, FCM Registration API for the same project. Use the existing backend service account, with permission to send FCM messages. Never put that JSON or any private key in the website.

Restart the backend to create `push_device` and `push_job` tables using the existing Hibernate schema update configuration. Production hosts must allow outbound HTTPS to Firebase. Host the website over HTTPS; localhost is allowed for development. Serve `/firebase-messaging-sw.js` as JavaScript at the root, without authentication or an HTML fallback, preferably with `Cache-Control: no-cache`.

## User setup and testing

1. After signing in on a supported browser, an automatic offer appears with **Enable notifications** and **Not now**. Enabling opens the browser's permission request. **Not now** is remembered per account on this browser, so the offer does not repeat. The notification bell retains its enable/disable control. A previously accepted choice reconnects notifications on later sign-ins; explicitly disabling from the bell prevents reconnection. If the browser permission is blocked, unblock it in browser settings before enabling from the bell.
2. On iPhone/iPad (16.4+), first add the site to the Home Screen and open its icon. A regular browser tab is insufficient.
3. Close Kapinda's tab/app. Using a different authorized staff account/device, update that customer's order status. Expect an OS notification, normally after the backend's next 10-second delivery cycle.
4. Tap the notification. It opens the signed-in account's order workspace (administration, restaurant, delivery, or customer orders). The push text is deliberately generic; order details remain behind authentication.
5. Verify each role, multiple devices, disabling notifications, and logout. Logout unregisters the current device before signing out. A failed unregister is shown as an error and can be retried.
6. Browser/OS notification settings, Focus modes, connectivity, and force-stopped browsers can delay or suppress delivery. Existing in-app notification history remains available.

## Delivery behavior

Order changes create delivery jobs in the same database transaction as notification history. Only new notification records enqueue push messages. The scheduled worker sees committed jobs, checks current role/order access and device ownership, and retries failures with exponential backoff up to eight attempts. Expired tokens are removed. Superseded order statuses and device registrations older than 60 days are skipped. Enabled devices refresh registration when Kapinda loads or regains focus.

Jobs use database row locks to prevent simultaneous workers claiming the same job. Delivery is at least once: a server crash after Firebase accepts a message but before the database commit can cause a retry. The stable notification tag helps replace repeated alerts. This is an installable online web app; offline browsing is not implemented.

Reference: https://firebase.google.com/docs/cloud-messaging/web/get-started
