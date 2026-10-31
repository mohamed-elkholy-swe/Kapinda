# Delivery App - Phase 1 Plan

## Goal

Build the first working version of the food delivery application based on the prototype:

- Customer can browse restaurants.
- Customer can filter/search restaurants and menu items.
- Customer can add items to cart.
- Customer can enter phone/address.
- Customer can place a cash-on-delivery order.
- Customer can see basic order confirmation/status.

## Recommended Starting Architecture

```text
Delivery Application/
  backend/    Spring Boot REST API
  mobile/     React Native customer app
  prototype/  Existing HTML/CSS/JS prototype
  docs/       Planning, API notes, decisions
```

## Phase 1 Backend Scope

- REST API only.
- Seed restaurant/menu data from code first.
- Add PostgreSQL after the API shape is stable.
- Use cash-on-delivery only.
- No admin panel yet.
- No driver app yet.

## Phase 1 Mobile Scope

- Arabic-first mobile UI.
- Restaurant list screen.
- Search and category filters.
- Restaurant menu screen.
- Cart screen.
- Checkout screen.
- Order confirmation screen.

## First API Endpoints

```text
GET  /api/restaurants
GET  /api/restaurants/{id}
POST /api/orders
GET  /api/orders/{id}
```

## Initial Order Statuses

```text
PLACED
ACCEPTED
PREPARING
OUT_FOR_DELIVERY
DELIVERED
CANCELLED
```

## First Milestone

The first milestone is a working backend that returns restaurant/menu data and accepts a simple order request. Once that works, the React Native app can be connected screen by screen.
