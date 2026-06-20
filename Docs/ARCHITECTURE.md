# Architecture

## Overview

Single-module Android app with a thin **data layer**, **repository**, and **Jetpack Compose** UI. ViewModels drive the shop catalog and product detail flows; other screens use composable-local state with coroutines.

```
┌─────────────────────────────────────────────────────────┐
│  MainActivity + Navigation Compose + bottom tabs          │
├─────────────────────────────────────────────────────────┤
│  ui/shop          ShopViewModel                         │
│  ui/product       ProductDetailViewModel                │
│  ui/auth          Login / Register (composable state)   │
│  ui/tabs          Wishlist / Orders / Account           │
├─────────────────────────────────────────────────────────┤
│  MarketplaceRepository                                  │
├──────────────┬──────────────────────────────────────────┤
│  MarketplaceApi (Retrofit)   │  TokenStore (DataStore)   │
│  AuthInterceptor             │                           │
│  TokenAuthenticator            │                           │
└──────────────┴──────────────────────────────────────────┘
```

## Composition root

`MarketplaceApplication` creates `AppContainer`, which wires OkHttp, Retrofit, Gson, `TokenStore`, and `MarketplaceRepository`.

## Authentication flow

1. Login/register calls skip the `Authorization` header (`AuthInterceptor`).
2. Successful login persists access + refresh tokens in DataStore.
3. Protected routes attach `Bearer` access tokens.
4. On **401**, `TokenAuthenticator` calls `POST /api/auth/refresh`, saves new tokens, and retries once (loop guard at 2 responses).

## Public vs authenticated API

| Feature | Auth required |
|---------|----------------|
| Browse products / categories | No |
| Product detail | No |
| Wishlist | Yes |
| Orders / checkout | Yes |
| Profile (`/api/me`) | Yes |

## Error handling

- `MarketplaceRepository` maps HTTP failures to `Result.failure` with API `error` JSON when present.
- UI shows inline error text and retry actions (shop) or pull-to-refresh (orders).

## Related projects

- **iOS client** (primary mobile focus): SwiftUI marketplace app on the same API contract.
- **Symfony API**: `marketplace-platform-api` — Docker, JWT, OpenAPI, PHPUnit.
