# Inventory Service

Inventory Service owns product stock balances and reservation state for ShopCard.

## Public API Compatibility

The existing API surface is preserved:

- `POST /api/v1/inventory/stock-in?shopId={shopId}`
- `GET /api/v1/inventory?shopId={shopId}`
- Kafka consumers: `order.placed`, `order.cancelled`, `order.completed`
- Kafka producers: `inventory.reserved`, `inventory.failed`

## Business Invariants

- `stockQty` cannot be negative.
- `reservedQty` cannot be negative.
- `reservedQty` cannot exceed `stockQty`.
- A product inventory row belongs to exactly one shop.
- Reservation only succeeds when every item exists, belongs to the order shop, and has enough available quantity.
- Reservation failure leaves all stock unchanged and publishes `inventory.failed`.
- Duplicate order reservation, cancellation, and completion events are idempotent.

## Concurrency

Inventory rows are read with pessimistic write locks during stock mutation, reservation, release, and deduction. The entity also uses optimistic versioning, giving the database a second guard against concurrent stock corruption.

## Event Handling

`order.placed` attempts an all-or-nothing reservation.

`order.cancelled` releases previously reserved quantity without reducing physical stock.

`order.completed` releases reserved quantity and deducts physical stock.

Stock transaction rows store before/after balances plus idempotency keys so repeated Kafka deliveries do not double-apply inventory movement.
