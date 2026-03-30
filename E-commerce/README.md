# E-Commerce Backend

A Spring Boot backend for an e-commerce platform featuring JWT authentication, product management, cart management, order processing, and Stripe payment integration.

## Tech Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Security** with JWT (jjwt 0.12.x)
- **Spring Data JPA** with H2 (dev) / PostgreSQL (prod)
- **Stripe Java SDK** for payment processing
- **BCrypt** for password hashing
- **JUnit 5** + **Mockito** for testing

## Prerequisites

- Java 17+
- Maven 3.8+
- (Optional) PostgreSQL for production
- Stripe account for payment processing

## Configuration

Set the following environment variables before running:

| Variable | Description | Required |
|---|---|---|
| `JWT_SECRET` | HMAC-SHA256 signing key (min 256 bits / 32 chars) | Yes |
| `STRIPE_SECRET_KEY` | Stripe secret API key (`sk_test_...` or `sk_live_...`) | Yes |
| `DATABASE_URL` | PostgreSQL JDBC URL (production) | No |
| `DB_USERNAME` | Database username (production) | No |
| `DB_PASSWORD` | Database password (production) | No |

Example:
```bash
export JWT_SECRET="your-256-bit-secret-key-for-hmac-sha256-signing"
export STRIPE_SECRET_KEY="sk_test_your_stripe_key_here"
```

## Build & Run

```bash
# Build
mvn clean package

# Run
mvn spring-boot:run

# Run tests
mvn test
```

The application starts on `http://localhost:8080`.

## API Endpoints

### Authentication
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | No |
| POST | `/api/auth/login` | Login and get JWT | No |

### Products
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/products` | List all products | No |
| GET | `/api/products/{id}` | Get product by ID | No |
| GET | `/api/products/search?name=` | Search products | No |
| POST | `/api/products` | Create product | ADMIN |
| PUT | `/api/products/{id}` | Update product | ADMIN |
| DELETE | `/api/products/{id}` | Delete product | ADMIN |

### Cart
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/cart` | Get user's cart | USER |
| POST | `/api/cart/items` | Add item to cart | USER |
| PUT | `/api/cart/items/{productId}` | Update cart item quantity | USER |
| DELETE | `/api/cart/items/{productId}` | Remove item from cart | USER |
| DELETE | `/api/cart` | Clear cart | USER |

### Orders
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/orders` | Create order from cart | USER |
| GET | `/api/orders` | Get user's orders | USER |
| GET | `/api/orders/{orderId}` | Get order details | USER |
| PUT | `/api/orders/{orderId}/cancel` | Cancel pending order | USER |

### Payments
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/payments/create-intent` | Create Stripe payment intent | USER |
| POST | `/api/payments/confirm/{paymentIntentId}` | Confirm payment | USER |

## Request/Response Examples

### Register
```json
POST /api/auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

### Add to Cart
```json
POST /api/cart/items
Authorization: Bearer <jwt-token>
{
  "productId": 1,
  "quantity": 2
}
```

### Create Order
```json
POST /api/orders
Authorization: Bearer <jwt-token>
{
  "shippingAddress": "123 Main St",
  "shippingCity": "Springfield",
  "shippingState": "IL",
  "shippingZipCode": "62701",
  "shippingCountry": "US"
}
```

### Create Payment Intent
```json
POST /api/payments/create-intent
Authorization: Bearer <jwt-token>
{
  "orderId": 1
}
```

## Entity Relationships

```
User (1) ──── (1) Cart
User (1) ──── (*) Order
Cart (1) ──── (*) CartItem
CartItem (*) ──── (1) Product
Order (1) ──── (*) OrderItem
OrderItem (*) ──── (1) Product
```

## Project Structure

```
src/main/java/com/ecommerce/
├── EcommerceApplication.java
├── config/          # Security and Stripe configuration
├── controller/      # REST API controllers
├── dto/             # Request/Response DTOs
├── entity/          # JPA entities
├── enums/           # Role, OrderStatus, PaymentStatus
├── exception/       # Custom exceptions and global handler
├── repository/      # Spring Data JPA repositories
├── security/        # JWT provider, filter, UserDetailsService
└── service/         # Business logic layer
```
