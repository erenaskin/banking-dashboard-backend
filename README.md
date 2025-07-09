# Banking Dashboard Backend

Banking Dashboard is a secure, enterprise-grade banking backend application developed with modern technologies. The project simulates real-world financial transactions through a Java Spring Boot backend architecture and RESTful API design.

---

## Table of Contents

* [Overview](#overview)
* [Features](#features)
* [Technologies](#technologies)
* [Project Architecture](#project-architecture)
* [Setup and Running](#setup-and-running)
* [API Documentation](#api-documentation)
* [Tests](#tests)
* [Security](#security)
* [Development and Contribution](#development-and-contribution)
* [Contact](#contact)

---

## Overview

This project enables bank customers to securely create accounts, transfer money, view transaction histories, and perform many other banking operations.

The backend layer is developed using **Spring Boot**, featuring JWT-based security, data validation, error handling, and automated testing.

---

## Features

* **User Registration & Login (JWT Authentication)**
* **Account Management:** Create accounts, view account details, list user accounts.
* **Money Transfers:** Transfer funds between accounts, deposit, withdraw.
* **Transaction History:** List all account transactions.
* **Enterprise-Level Security:** JWT authorization and password encryption.
* **Layered Design with DTOs and MapStruct:** Data transfer objects and mapping.
* **Global Exception Handling:** Centralized error capture and consistent API error responses.
* **Integration Tests:** Comprehensive end-to-end tests for controllers and services.
* **Unit Tests:** Detailed unit tests for service layer.
* **OpenAPI/Swagger (optional):** API documentation.

---

## Technologies

* Java 17
* Spring Boot 3.5.x
* Spring Security (JWT)
* Spring Data JPA
* PostgreSQL
* Maven
* MapStruct
* JUnit 5 & Mockito
* Spring Test (MockMvc)
* Jackson (JSON processing)
* Lombok
* Testcontainers (Optional, for integration testing)

---

## Project Architecture

The project follows a classic **3-layer architecture**:

* **Controller:** Handles API endpoints and HTTP requests, calls services.
* **Service:** Business logic and operations are managed here.
* **Repository:** Database operations via JPA repositories.

**DTOs (Data Transfer Objects)** abstract the data transfer between API and entity layers. MapStruct is used for automatic mapping.

---

## Setup and Running

1. **Clone the project:**

   ```bash
   git clone https://github.com/erenaskin/banking-dashboard.git
   cd banking-dashboard
   ```

2. **Configure PostgreSQL Database:**

   * Install PostgreSQL 17 or compatible version.
   * Create database and user credentials.
   * Update database connection details in `application.properties` or `application.yml`.

3. **Build and run the project:**

   ```bash
   ./mvnw clean spring-boot:run
   ```

4. **Run tests:**

   ```bash
   ./mvnw test
   ```

---

## API Documentation

* All API endpoints follow REST standards.
* JWT Token authorization is required.
* Main endpoints include:

| Method | URL                               | Description             |
| ------ | --------------------------------- | ----------------------- |
| POST   | /api/auth/register                | User registration       |
| POST   | /api/auth/login                   | User login (get token)  |
| POST   | /api/accounts                     | Create account          |
| GET    | /api/accounts                     | List user accounts      |
| GET    | /api/accounts/{iban}/currency     | Get account currency    |
| GET    | /api/accounts/{iban}/details      | Get account details     |
| POST   | /api/accounts/{iban}/transactions | Perform transactions    |
| POST   | /api/transactions                 | Transfer operation      |
| GET    | /api/transactions/{iban}          | Get transaction history |

---

## Tests

The project includes comprehensive testing:

* **Unit Tests:** Test business logic at the service layer.
* **Integration Tests:** End-to-end testing of controllers and services to verify API endpoints.
* Tests use MockMvc, JUnit 5, Mockito, and Spring Boot testing framework.
* Integrating tests into CI pipelines is recommended for early error detection.

---

## Security

* User authentication is performed with JWT (JSON Web Tokens).
* Passwords are encrypted using BCrypt.
* All API endpoints are protected with authorization mechanisms.
* Global Exception Handler provides consistent and meaningful error messages.

---

## Development and Contribution

* Coding standards and clean code principles are followed.
* Pull requests are welcome to add features or fix bugs.
* The modular project structure facilitates easy development.
* Use of MapStruct and DTOs keeps entity and API layers separate.

---

## Contact

Developer: **Eren AŞKIN**
GitHub: [https://github.com/erenaskin](https://github.com/erenaskin)
Email: [eren.askin@hotmail.com](mailto:eren.askin@hotmail.com)


