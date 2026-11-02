# Kapinda — React web app

A separate React + TypeScript + Vite frontend for the existing Spring Boot backend. The website connects to the existing Spring Boot backend.

## Run locally

Requires Node.js 20.19+ or 22.12+.

```powershell
cd "D:\projects\Delivery Application\web"
npm.cmd install
if (!(Test-Path .env.local)) { Copy-Item .env.example .env.local }
npm.cmd run dev
```

Open http://localhost:5173. Start the existing backend separately on port 8080 with PostgreSQL configured. The development server proxies `/api` to `http://localhost:8080`.

## Firebase sign-in

Fill the four `VITE_FIREBASE_*` values in `.env.local` using the public web app configuration from your existing Firebase project. Restart Vite after changes. In Firebase Authentication, enable Email/Password and/or Google and add your local and deployed hostnames to Authorized domains. New Firebase projects may need `localhost` added explicitly.

Do not copy backend/.env into the web project: it contains backend secrets. Never put service-account JSON, database passwords, or private keys in a VITE variable: all VITE values are public browser data.

The backend must also have Firebase Admin configured. `/api/account/me` supplies the account role; the browser never grants admin access. Existing backend admin-email configuration determines administrators.

## Logs and Google sign-in troubleshooting

During `npm run dev`, development diagnostics appear in **both the browser console and the Vite terminal**. Restart Vite after updating `vite.config.ts` or environment values. Open the browser developer tools with F12 or Ctrl+Shift+J, select Console, and filter for `[Kapinda]`. Enable Info and Warning messages. The terminal receives browser events while the Vite WebSocket connection is active; earlier initialization events are still available in the browser console. These diagnostics are disabled in production builds.

Examples:

```text
[Kapinda] auth.google.started {}
[Kapinda] auth.google.failed {"code":"auth/internal-error"}
```

This indicates a Firebase/Google sign-in failure before backend account loading. By contrast, `auth.google.succeeded`, followed by `api.failed` for `/account/me` with status 503, indicates a backend Firebase Admin configuration failure. Metadata is allowlisted: passwords, user details, tokens, headers, request/response bodies, and raw Firebase error objects are not logged.

`auth/internal-error` is generic; the code alone does not establish its cause. Check Authentication → Sign-in method → Google in the same Firebase project and confirm it is enabled with a support email. Check that your public configuration matches the Firebase **web app** configuration under Project settings. Ensure the hostname you use (`localhost` or `127.0.0.1`) is listed in Authentication → Settings → Authorized domains. Test in a normal Chrome or Edge browser with popups allowed. If it still fails, open developer tools → Network, preserve the log, reproduce the failure, and inspect the failing Firebase request's error response. Never share tokens, authorization headers, full HAR files, or private credentials.

The server service-account JSON is used only by the backend; replacing it does not fix a browser Google popup error.

## Included

- Arabic-first RTL interface with an English switch and responsive layouts.
- Public restaurant browsing, category filters, search across restaurants and dishes, and menus.
- In-memory basket, quantity adjustment, cash checkout with phone/address/area validation.
- Firebase email registration, email/password login, Google popup login, and logout.
- Account synchronization, order confirmation, identity-scoped order history, and manual status refresh.
- Admin order listing/status updates and restaurant/menu create, edit, and delete tools.
- Loading, empty, error, timeout, and retry states; backend data only, without mock restaurants.

The signed-in avatar opens My account, with editable name and phone, a read-only sign-in email, and the complete order history. A random illustrated avatar is stored per account in this browser; no external avatar service receives account details. My orders lists the signed-in customer's history, with current/past filters and detail links. Phone numbers saved in the profile prefill checkout. The basket intentionally stays in memory and clears on page reload or sign-out. Delivery fees and final prices are calculated by the backend (currently EGP 18 delivery). An uncertain network response after submitting should be checked before placing another order: the existing backend has no idempotency support.

Administrators land on a separate workspace with Overview, Orders, Restaurants, Add Restaurant, Menu management, and People & support screens. Summary values use actual backend records; delivered order value includes delivery fees and excludes cancelled or pending orders. Orders support search, status filters, details, and status changes. Restaurant and dish deletion require confirmation.

Restart the backend after this update. It adds nullable profile columns using the existing Hibernate `ddl-auto=update` setting, `PUT /api/account/me` for name/phone, and `GET /api/account/me/orders` for identity-scoped customer history. The backend derives ownership from the verified Firebase identity; callers cannot supply another customer's UID. Existing accounts and mobile response fields are preserved.

This version does not include driver tracking, online payments, offline behavior, or the mobile restaurant-application/portal screens. No corresponding restaurant-application/portal endpoints exist in the current backend.

## Build and deployment

```powershell
npm.cmd run build
npm.cmd run preview
```

`dist` contains the production website. For production either route `/api` to the backend through your hosting reverse proxy or set `VITE_API_BASE_URL=https://your-backend.example/api` before building. Vite's development proxy does not run in production or `preview`. Use HTTPS and configure the backend's allowed origins for your website domain. The current backend allows all origins; restrict it before production. Configure Firebase's authorized domains as above.

The UI uses Google Fonts with local sans-serif fallbacks. Food illustrations use emoji and do not represent restaurant photography.

## Roles, restaurant assignments, delivery, and support

| Role | Customer browsing / checkout / own history | Additional workspace |
| --- | --- | --- |
| CUSTOMER | Yes | None |
| DELIVERY | Yes | Ready delivery queue, claim orders, own deliveries, completion history |
| RESTAURANT | Yes | Details and menu editing for assigned restaurants only |
| ADMIN | Yes | Every restaurant and delivery workspace, platform management, all role assignments |
| SUB_ADMIN | Yes | Same operational workspaces; cannot grant Admin or modify existing Admin accounts |

In Admin → People & support, search by email, name, phone, or exact account ID. A person must sign in once before their account appears. Select the role and, for restaurant staff, choose one or more restaurants. All authorization is enforced by the backend on every request. A person changing role should refresh the website (returning focus also refreshes account permissions). An admin cannot demote their own account.

Customer restaurant cards open a full menu page with search, dish cards, quantity controls, desktop basket, and a phone checkout bar. Follow your orders loads only the signed-in account's orders; there is no manual order-number field. Cards show progress, address, itemised totals, and support calls. The account screen includes the same complete history and filters for all active roles.

Set the real support number in Admin → People & support → Order support phone. It is stored in PostgreSQL and returned by the public /api/settings endpoint. The configured initial help number is 01280193490 (override with DELIVERY_SUPPORT_PHONE). An admin-saved number takes precedence; saving an empty value disables the call link. Changing it takes effect when the order screen next loads.

Ready, unassigned orders appear in the delivery queue without customer phone/address. A delivery person claims an order before seeing those details. Claiming uses a database row lock so two people cannot take the same order. Only the assigned person can move it from READY to OUT_FOR_DELIVERY to DELIVERED. Admins retain order-status control. Delivery staff do not receive user search or restaurant editing permissions.

Restart the backend for the new endpoints and schema. The existing development schema update creates account_restaurant_assignments, support_settings, and the nullable orders.delivery_person_uid column. A transactional startup upgrade expands PostgreSQL's existing app_users_role_check to the four supported roles; no existing account or order is deleted. New accounts remain customers unless the existing admin-email bootstrap applies. Existing roles are not reassigned automatically.

## Kapinda design and simple backend startup

Kapinda uses deep teal, antique gold, warm ivory, Ottoman-inspired arches, geometric tiles and a tulip medallion logo. The theme covers the customer homepage, menus, basket, checkout, profile, orders, login, admin and staff workspaces. Existing restaurant photographs and menu scans are retained. Amiri and Cormorant Garamond headings use serif fallbacks if Google Fonts cannot load.

Click Run on DeliveryBackendApplication in IntelliJ, or run mvn spring-boot:run from backend/. The backend loads backend/.env itself; no script or persistent Windows environment setup is required. Backend secrets remain outside the website and application.properties. Restart the backend after updating Java code. Restart Vite after updating its configuration or environment.

Personal history uses GET /api/account/me/orders and requires the response accountUid to match the active Firebase identity. Authenticated fetches disable caching and reject responses when the identity changes. History resets on account switches and new accounts show zero orders; admin summaries explicitly say Platform orders and Across all customers. Existing GET /api/orders is retained for compatibility.

## Full Admin and Sub Admin

Full ADMIN has every operational workspace: customer browsing and personal orders, all restaurants and menus, the delivery queue, assigned deliveries across the team, platform orders, user access, and support settings. SUB_ADMIN has the same operational access, but only a full ADMIN can grant ADMIN or change an existing ADMIN account. Neither administrator role can remove its own management access. Ordinary restaurant and delivery roles retain their existing scope restrictions.

The role picker omits ADMIN for a Sub Admin and locks existing full-Admin accounts. These restrictions are enforced again on the server, including direct API requests. The PostgreSQL role-check upgrade adds SUB_ADMIN when the backend starts; existing roles are preserved. Refresh the website after role changes.

Delivery responses include assignedToMe, assignedToOther, canClaim and canUpdate. Managers see all delivery work and can update another courier's delivery progress without taking ownership. Couriers remain limited to their own jobs and available ready orders. Restaurant managers can open the restaurant workspace for every restaurant without individual assignments.

The Kapinda homepage now includes a stylised illustration of the Mohamed Ali barrages, their repeated arches and towers above the Nile. It is an artistic homage rather than a measured architectural drawing. Landmark context: https://sis.gov.eg/en/egypt/tourism/recreational-tourism/el-qanater-el-khayreya/

## Restaurant inbox and in-app notifications

Restaurant staff can open Restaurant orders to see orders containing items from their assigned restaurants. For mixed-restaurant orders they see only their restaurants' items and subtotal; customer contact and unrelated items are omitted. Personal order history remains in the account screen. Admin and Sub Admin can open the restaurant inbox too.

The header bell shows persisted new-order and status-change notifications with an unread count. It refreshes every 30 seconds while the tab is visible and on focus/reconnection. Clicking a notification records read state and opens the appropriate order workspace. Device push notifications are optional; see setup below. Email and SMS are not included.

Restart the backend after this update to create the notifications table. GET /api/restaurant-workspace/orders is restricted to restaurant staff and management. GET /api/notifications and PUT /api/notifications/{id}/read require an active authenticated account and enforce recipient ownership. Notification visibility is checked again against current restaurant assignments and roles.

Notification actions: Mark all as read updates only the signed-in account; Remove all dismisses that account's existing notifications persistently. A later new status can still create a new notification. GET requests never create notifications from historical orders. Customer notifications concern their own orders; restaurant notifications concern assigned restaurants; delivery notifications cover pickup/delivery stages only, with active assignment checks; management sees platform updates. Restaurant staff have one Restaurant orders navigation entry.

## Device push notifications (phone and desktop)

1. In the existing Firebase project, open Project settings > General > Your apps > web app configuration. Copy `messagingSenderId` into `VITE_FIREBASE_MESSAGING_SENDER_ID` in `web/.env.local` or your website hosting build environment.
2. Open Project settings > Cloud Messaging > Web Push certificates > Generate key pair. Set `VITE_FIREBASE_VAPID_KEY` to its **public** key. Never put a service-account JSON or private key into the website environment. Enable the FCM Registration API / Firebase Cloud Messaging API in the same Google Cloud project if disabled.
3. Keep the backend's existing Firebase Admin project and credentials configured for that same project, with permission to send FCM messages. Restart the backend to create `push_devices` using the existing schema-update configuration. Device endpoints require authentication; registrations belong to the authenticated account, not a caller-supplied UID.
4. Rebuild and deploy the website over HTTPS (localhost is permitted for development). Host `/firebase-messaging-sw.js` as JavaScript at the origin root, without rewriting it to index.html, and avoid long-lived caching of this file. Serve `/manifest.webmanifest` too.
5. Sign in, open the notification bell, and choose **Enable notifications on this device**. Approve the browser prompt on each device. On iPhone/iPad (iOS/iPadOS 16.4+), use Add to Home Screen, open the installed app, then enable notifications. Browser/OS notification settings must also allow alerts. A fully stopped browser, device power restrictions, or offline device can delay/prevent delivery.

New order/status inbox entries trigger a generic lock-screen alert after the order transaction commits. Existing history is not resent. Clicks open the appropriate customer/staff workspace; normal authentication and permissions still apply. The website refreshes device registration on return while enabled. Disable notifications affects this browser only; signing out unregisters its token before completing sign-out. Enable separately on your other devices. Invalid/unregistered FCM tokens are removed by the backend.

Delivery is best effort: FCM failures do not roll back orders or remove inbox history, and this implementation does not retry transient delivery failures. Generic alerts intentionally omit customer, address and order details. Inbox polling remains available if push is denied or unsupported.

Verification after configuration: enable on a desktop and phone, close the website tabs (keep the browser permitted to run in the background), create/change an eligible order from another account, confirm one system notification, and tap it to open the proper workspace. Then sign out or disable on one device and confirm it receives no further alerts. Test another account and a revoked/blocked staff account to confirm recipient isolation. Actual device delivery requires configured Firebase credentials and cannot be verified by local unit tests alone.

References: https://firebase.google.com/docs/cloud-messaging/web/get-started and https://webkit.org/blog/13878/.
