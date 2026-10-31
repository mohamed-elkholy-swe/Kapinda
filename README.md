# Kapinda

Arabic/English restaurant delivery website for Al Qanater El Khayria.

## Projects

- backend/: Spring Boot API, PostgreSQL and Firebase authentication.
- web/: React/TypeScript website, customer ordering and staff dashboards.
- docs/: API contract and project notes.

The mobile application is retired. Private configuration and local archives are excluded from Git.

## Run locally

1. Copy backend/.env.example to backend/.env and enter your database and Firebase settings. Put your Firebase service-account JSON in backend/config/firebase-service-account.json. Keep both files private.
2. From backend/, run mvn spring-boot:run, or run DeliveryBackendApplication in IntelliJ. The backend reads .env automatically.
3. Copy web/.env.example to web/.env.local and enter your public Firebase web configuration.
4. From web/, run npm ci and npm run dev. Open the address printed by Vite.

The backend uses port 8080 by default. Allow your website hostname in Firebase Authentication.

## Roles

Customers browse restaurants, order and view personal history. Restaurant staff manage assigned restaurants and menus. Delivery staff claim ready orders and update deliveries. Admin and Sub Admin manage all restaurants, deliveries, orders and support settings. Only full Admin can grant Admin access or modify existing Admin accounts.

Restart the backend after Java changes to apply role and order-status database upgrades.

## Checks

Backend: mvn test. Web: npm test and npm run build.

See web/README.md and docs/api-contract.md for details.
