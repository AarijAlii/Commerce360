# Multi-Store Inventory Management System

A scalable inventory management system designed to handle product stock tracking, store-specific inventory,
user access control, audit logs, and reporting for 500+ stores. Built using Spring Boot, PostgreSQL,
Redis, and secured using JWT authentication with role-based access.

---

## ✅ Evolution Rationale (v1 → v2 → v3)

### Version 1: Single Store MVP 
- **Purpose**: Bootstrap a minimal working system to manage inventory for a single store.
- **Storage**: Local SQLite .
- **Access**: CLI.
- **Features**:
  - Basic CRUD on products.
  - Manual stock-in and stock-out tracking.
  - Single store, no multi-tenancy.
- **Goal**: Validate inventory logic, product structure, and stock movements before scaling.

---

### Version 2: Multi-Store System (Current Implementation)
- **Purpose**: Scale the system to support 500+ stores with centralized data and secure APIs.
- **Storage**: PostgreSQL (normalized schema, indexed by `store_id`).
- **Features**:
  - Central product catalog with store-specific inventory.
  - REST API endpoints with pagination and filtering.
  - Database hosted on Aiven to avoid downtime.
  - Indexing for improved database fetching.
  - Role-based JWT authentication and authorization.
  - Admin endpoints for user approval workflows.
  - Full audit logging system to track all activities(user actions, product/store edits, etc).
  - Robust reporting (sales, purchases, profit/loss, stock status).
  - Rate limiting via leaky bucket implementation.
- **Goal**: Provide performance and control at scale with secure API-based interaction.

---

### Version 3: Large-Scale Enterprise (To be Implemented)
- **Purpose**: Support thousands of stores and concurrent users with real-time stock sync.
- **Implemented Features**:
  - Implemented Redis for in-memory caching to reduce the time for data fetching.
  - Dockerize for container orchestration via Kubernetes to host on Render as a Web Service.
- **Planned Enhancements**:
  - Switch to **event-driven architecture** (Apache Kafka).
  - **Horizontal scalability** with microservices architecture.
  - Use **read/write DB separation** with replica reads via Aiven.
  - Improve caching strategy (multi-level cache or CDN edge).
  - Fine-grained **rate limiting per API key/user/role**.
  - Real-time analytics dashboards & webhooks.

---

## 🧠 Key Design Decisions

- **Multi-Tenancy via `store_id`**: All inventory and reporting APIs are scoped to stores using a consistent `store_id` pattern.
- **Centralized Product Catalog**: Enables consistent product data across stores while tracking inventory separately per store.
- **Audit Trail**: All critical actions (like inventory changes) are tracked via an `audit_log` table for compliance and traceability.

### Architecture

- **Microservices-ready**: Designed with scalability in mind, though currently monolithic.
- **Layered Architecture**: Clear separation between controllers, services, and repositories.
- **Dependency Injection**: Leveraging Spring's DI for loose coupling and testability.

### Security

- **JWT Authentication**: Stateless authentication using JWT tokens.
- **Role-based Access Control**: Different roles (ADMIN, STORE_MANAGER) with appropriate permissions.
- **Password Encryption**: BCrypt for secure password hashing.
- **Token Refresh**: Implemented refresh token mechanism for better security.

### Data Management

- **PostgreSQL**: Chosen for its reliability and ACID compliance.
- **Redis Caching**: Implemented for frequently accessed data to improve performance.
- **JPA/Hibernate**: Used for ORM to simplify database operations.
- **Soft Delete**: Implemented for data retention and audit purposes.

## 🔐 Authentication

- **JWT**: Access tokens are issued via `/api/auth/login` and refreshed via `/api/auth/refresh`.
- **Roles**: 
  - `ADMIN`: Full access including user approval, audit logs and managing stores, products and user inventories.
  - `STORE_MANAGER`: Access limited to stores they manage, personal profile and reports/summaries of owned stores.

## 📊 API Design

- **RESTful Principles**: Following REST conventions for resource management.
- **Pagination**: Implemented for large datasets.
- **Filtering & Sorting**: Support for query parameters.
- **Standardized Responses**: Consistent response structure.
---

## 📉 Trade-offs

### Performance vs Consistency
- **Scenario**: Redis caching improves performance but requires cache invalidation  
- **Trade-off**: Potential stale data vs faster response times  
- **Mitigation**: Implemented cache invalidation strategies (e.g., TTL, write-through, or event-driven invalidation)  

###  Scalability vs Complexity  
- **Scenario**: Monolithic architecture is simpler but harder to scale  
- **Trade-off**: Easier development vs future scaling challenges  
- **Mitigation**: Designed with clear service boundaries to facilitate future decomposition  

###  Security vs Usability  
- **Scenario**: JWT tokens provide stateless auth but can't be revoked  
- **Trade-off**: Better performance vs security control  
- **Mitigation**: Short token expiration + refresh token mechanism  

###  Data Integrity vs Performance  
- **Scenario**: PostgreSQL ensures data integrity but may be slower than NoSQL  
- **Trade-off**: Strong consistency vs raw performance  
- **Mitigation**: Redis caching for read-heavy operations  

---

## 📌 Assumptions

1. **User Management**
   - Admins can manage users, audit logs, and system-wide configurations.
   - Users are manually approved by an admin before gaining access.
   - Users have unique email addresses
   - Passwords are at least 8 characters long

2. **Inventory Management**
   - A product can be sold, restocked, or removed manually or automatically upon expiry.
   - Each store has its own inventory but uses the global product catalog.
   - Products have unique SKUs
   - Stock levels cannot go negative
   - Price changes are tracked historically

3. **Security**
   - JWT tokens are secure enough for the application's needs
   - Redis is secure and properly configured
   - Database credentials are properly managed

4. **Performance**
   - Report endpoints are read-only and query-heavy — designed for performance.
   - Rate limiting prevents abusive behavior from API consumers.
   - Redis caching will significantly improve performance
   - Database indexes are sufficient for query performance
   - Pagination will handle large datasets effectively

---

## 📊 API Overview

### Inventory Management Endpoints (`/api/inventory`)

- `POST /api/inventory/stock-in`  
  Records stock intake (**Store Manager only**)

- `POST /api/inventory/sale`  
  Records product sale (**Store Manager only**)

- `POST /api/inventory/remove`  
  Removes stock (**Store Manager only**)

- `GET /api/inventory/store/{storeId}`  
  Gets store inventory (**Store Manager only**)

- `GET /api/inventory/store/{storeId}/category/{categoryId}`  
  Gets store inventory by category (**Store Manager only**)

- `GET /api/inventory/store/{storeId}/expiring`  
  Gets expiring stock (**Store Manager only**)

- `GET /api/inventory/reports/stock`  
  Gets stock summary report (**Store Manager only**)

- `GET /api/inventory/reports/sales`  
  Gets sales summary report (**Store Manager only**)

- `GET /api/inventory/reports/purchases`  
  Gets purchase summary report (**Store Manager only**)

- `GET /api/inventory/reports/profit-loss`  
  Gets profit-loss analysis (**Store Manager only**)

---

### User Management Endpoints (`/api/users`)

- `GET /api/users/me`  
  Gets current user's information

- `POST /api/users/register`  
  Registers a new user

- `GET /api/users/pending`  
  Gets list of pending users (**Admin only**)

- `GET /api/users/approved`  
  Gets list of approved users (**Admin only**)

- `GET /api/users/rejected`  
  Gets list of rejected users (**Admin only**)

- `POST /api/users/{userId}/approve`  
  Approves a pending user (**Admin only**)

- `POST /api/users/{userId}/reject`  
  Rejects a pending user (**Admin only**)

- `PUT /api/users/{userId}`  
  Updates user information

- `DELETE /api/users/{userId}`  
  Deletes a user

- `DELETE /api/users/me`  
  Deletes current user's account

---

### Product Endpoints (`/api/products`)

- `GET /api/products/search`  
  Search a specific product

- `GET /api/products/supplier/{supplierId}`  
  Get products by supplier

- `GET /api/products`  
  Gets all products with pagination

- `GET /api/products/category/{category}`  
  Gets products by category

- `GET /api/products/filter`  
  Filters products by category, supplier, or search query

- `GET /api/products/{id}`  
  Gets product by ID

- `POST /api/products`  
  Creates a new product (**Admin only**)

- `PUT /api/products/{id}`  
  Updates a product (**Admin only**)

- `DELETE /api/products/{id}`  
  Deletes a product (**Admin only**)

- `GET /api/products/test-metrics`  
  Test endpoint for rate limiter metrics

---

### Audit Log Endpoints (`/api/audit-logs`)

- `GET /api/audit-logs`  
  Gets all audit logs (**Admin only**)

- `GET /api/audit-logs/store/{storeId}`  
  Gets store audit logs (**Store Manager only**)

- `GET /api/audit-logs/product/{productId}`  
  Gets product audit logs (**Store Manager only**)

- `GET /api/audit-logs/date-range`  
  Gets audit logs by date range (**Admin only**)

- `GET /api/audit-logs/user/{userId}`  
  Gets audit logs by user (**Admin only**)

---

### Supplier Management Endpoints (`/api/suppliers`)

- `GET /api/suppliers`  
  Gets all suppliers with pagination

- `GET /api/suppliers/{id}`  
  Gets supplier by ID

- `POST /api/suppliers`  
  Creates a new supplier (**Admin only**)

- `PUT /api/suppliers/{id}`  
  Updates a supplier (**Admin only**)

- `DELETE /api/suppliers/{id}`  
  Deletes a supplier (**Admin only**)

- `GET /api/suppliers/search`  
  Searches suppliers

---

### Store Management Endpoints (`/api/stores`)

- `GET /api/stores`  
  Gets all stores

- `GET /api/stores/{id}`  
  Gets store by ID

- `POST /api/stores`  
  Creates a new store

- `PUT /api/stores/{id}`  
  Updates a store

- `DELETE /api/stores/{id}`  
  Deletes a store

- `PUT /api/stores/{id}/assign-owner`  
  Assigns store owner

- `GET /api/stores/owned/{ownerId}`  
  Gets stores by owner

- `GET /api/stores/my-stores`  
  Gets stores for current user

---

### Authentication Endpoints (`/api/auth`)

- `POST /api/auth/login`  
  Authenticates users and returns JWT tokens
- `POST /api/auth/refresh`    
  Returns access and refresh tokens

---

## 🚀 Technologies Used

| Component          | Technology               |
|--------------------|--------------------------|
| Backend            | Spring Boot              |
| API Authentication | Spring Security + JWT    |
| Database           | PostgreSQL               |
| Caching            | Redis                    |
| Rate Limiting      | Spring Boot + Leaky Bucket |
| API Testing        | Postman / Swagger UI     |

---

## 📈 Future Improvements (v3)

- Migrate to microservices using Spring Cloud.
- Add Kafka for async stock syncing.
- Implement read replicas and write queues.
- Build real-time reporting dashboard using WebSockets.

---

## 💬 Developer Information

**Developer**: Aarij Ali  
**Email**: aarij.ali04@gmail.com  
**GitHub**: https://github.com/AarijAlii
