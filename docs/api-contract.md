# Delivery App API Contract - Draft

Base URL for local development:

```text
http://localhost:8080/api
```

## Restaurants

### List Restaurants

```http
GET /api/restaurants
```

Optional query params:

```text
category=grill
search=burger
```

### Get Restaurant

```http
GET /api/restaurants/{id}
```

## Orders

### Create Order

```http
POST /api/orders
```

Request:

```json
{
  "customerPhone": "01000000000",
  "customerAddress": "Full delivery address",
  "area": "القناطر الخيرية",
  "items": [
    {
      "restaurantId": "el-saha-grill",
      "menuItemId": "kofta",
      "quantity": 2
    }
  ]
}
```

Response:

```json
{
  "id": "1001",
  "status": "PLACED",
  "subtotal": 240,
  "deliveryFee": 18,
  "total": 258
}
```

### Get Order

```http
GET /api/orders/{id}
```

## Customer profile and order history

- `GET /api/account/me` returns the current account, including optional `displayName` and `phoneNumber`.
- `PUT /api/account/me` saves `{ "displayName": "Customer name", "phoneNumber": "01012345678" }` for the verified account. Name is required (maximum 100 characters); phone is required (10–20 characters, digits, spaces, parentheses, plus and hyphen). Email, identity, role, and status cannot be changed by this request. Active accounts only.
- `GET /api/orders` returns only the verified active account's own orders (including delivery and restaurant staff when using the customer view), newest first, using the existing order-response shape. It accepts no customer UID parameter. Admin order listing remains at `/api/admin/orders`.

Restart the backend to enable these routes and let the existing development schema-update setting add nullable profile columns to `app_users`.

## Account roles and scoped workspaces

- Roles: CUSTOMER, ADMIN, DELIVERY, RESTAURANT. Every active role can use customer order creation and own-history endpoints.
- GET /api/admin/users?search=... searches existing application accounts by email/name/phone or exact UID (up to 50 results). ADMIN only.
- PUT /api/admin/users/{uid}/access accepts {role, restaurantIds: []}. ADMIN only. RESTAURANT requires at least one existing restaurant ID; other roles require an empty list. Self-demotion is refused. Account responses now include restaurantIds.
- GET /api/restaurant-workspace/restaurants returns assigned restaurants only. RESTAURANT only.
- PUT /api/restaurant-workspace/restaurants/{id} updates restaurant info, using the existing SaveRestaurantRequest fields. No creation/deletion permission is granted.
- POST /api/restaurant-workspace/restaurants/{id}/menu and PUT/DELETE /api/restaurant-workspace/restaurants/{id}/menu/{itemId} manage menu items. Every call checks the verified account's role and restaurant assignment. Cross-restaurant menu IDs are checked by RestaurantService.
- GET /api/delivery/orders returns {assignedToMe, order} tasks: own deliveries plus ready unassigned orders. Contact/address strings are blank until claimed. DELIVERY only.
- POST /api/delivery/orders/{id}/claim atomically claims a ready unassigned order (204).
- PUT /api/delivery/orders/{id}/status?status=OUT_FOR_DELIVERY or DELIVERED updates only the caller's delivery, in order (204).
- GET /api/settings returns {supportPhone} publicly. PUT /api/admin/settings updates it with ADMIN authorization. Empty string disables the call link; otherwise use a 10–20 character telephone number.

Private workspaces are authenticated by the Firebase filter; downstream authorization failures remain 403 rather than being relabelled as invalid Firebase tokens.

## Identity-bound personal history

GET /api/account/me/orders returns { accountUid, orders } for the verified active account only, with Cache-Control: no-store, private. No account ID parameter is accepted. The website validates accountUid against the signed-in Firebase identity and discards in-flight responses after an account change. New accounts with no owned orders receive an empty array. GET /api/orders remains available for existing clients.

## Administrator hierarchy

SUB_ADMIN is an operational administrator. Existing administrative endpoint checks accept active ADMIN and SUB_ADMIN accounts. Both roles can use /api/restaurant-workspace/restaurants for all restaurants and /api/delivery/orders in supervisor mode; the mode is derived from the verified backend account and cannot be selected by a request parameter.

PUT /api/admin/users/{uid}/access rejects a SUB_ADMIN request if the new role is ADMIN or the target already has ADMIN. This protects direct API calls as well as the UI. ADMIN can grant SUB_ADMIN and ADMIN. Self-demotion of either administrator role is rejected.

Delivery task DTOs include assignedToMe, assignedToOther, canClaim, canUpdate and order. Supervisors see full contact details and can progress assigned deliveries for the team while preserving their courier owner. Regular couriers continue to receive only available work and their own deliveries.
