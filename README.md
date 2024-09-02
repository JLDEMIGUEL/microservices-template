# Microservices Template Project

## Introduction

This project is a template for a Spring Boot based microservices architecture, designed to provide a starting point for
building scalable
and modular applications. It includes an Authentication Server following OAuth2.0 standard with different grant type
flows, an API Gateway that serves as an entry point to the microservices, multiple microservices, each with its own
dedicated responsibilities, and a Eureka server for service discovery.

## Table of Contents

- [Introduction](#introduction)
- [Table of Contents](#table-of-contents)
- [Microservices Overview](#microservices-overview)
    - [Authentication Server](#authentication-server)
    - [API Gateway](#api-gateway)
    - [Microservice 1 (Product Service)](#microservice-1-product-service)
    - [Microservice 2 (Order Service)](#microservice-2-order-service)
    - [Mail Service](#mail-service)
    - [Eureka Server](#eureka-server)
- [Endpoints](#endpoints)
    - [Auth Endpoints](#auth-endpoints)
    - [Microservice 1 Endpoints](#microservice-1-endpoints)
    - [Microservice 2 Endpoints](#microservice-2-endpoints)
    - [Mail Service Endpoints](#mail-service-endpoints)
    - [API Gateway Endpoints](#api-gateway-endpoints)
- [Key Features and Technologies](#key-features-and-technologies)
    - [Architecture](#architecture)
    - [Authentication and Authorization](#authentication-and-authorization)
    - [REST API](#rest-api)
    - [Database](#database)
    - [Service Discovery](#service-discovery)
    - [Monitoring and Logging](#monitoring-and-logging)
    - [Email Notifications](#email-notifications)
    - [Testing](#testing)

## Microservices Overview

### Authentication Server

The Authentication Server manages user authentication and authorization. It supports:

- User registration and management
- JWT token generation and validation
- OAuth 2.0 authorization with various grant types
- Role-based access control

Accessible at: `http://localhost:9000`

### API Gateway

The API Gateway serves as a unique entrypoint for the system.

Accessible at: `http://localhost:8090`

### Microservice 1 (Product Service)

Handles product-related operations. Features include:

- CRUD operations for products
- Managing product inventory
- Role-based authorization for product creation
- Pagination support for large product listings

Accessible at: `http://localhost:8080`

### Microservice 2 (Order Service)

Handles order-related operations, integrating with the Product Service. Features include:

- Order creation and management
- Integration with the Product Service to retrieve product details
- Caching for efficient communication
- Reactive programming using Spring WebFlux

Accessible at: `http://localhost:8081`

### Mail Service

Handles email notifications across the application. Features include:

- Sending welcome emails upon user registration
- Sending order confirmation emails
- Integration with other microservices via RabbitMQ

Accessible at: `http://localhost:8082`

### Eureka Server

Provides service discovery for the microservices, enabling dynamic scaling and resilience.

Accessible at: `http://localhost:8761`

## Endpoints

### Auth Endpoints

- **Client Credentials Grant**:
    - Request JWT token: `POST http://localhost:9000/oauth2/token`
- **Authorization Code Grant**:
    - Request authorization code: `GET http://localhost:9000/oauth2/authorize`
    - Exchange code for JWT token: `POST http://localhost:9000/oauth2/token`
- **User Registration**:
    - Register a new user: `POST http://localhost:9000/user/register`

### Microservice 1 Endpoints

- **Product Operations**:
    - Get all products: `GET http://localhost:8080/product`
    - Get product by ID: `GET http://localhost:8080/product/{id}`
    - Create product (Admin only): `POST http://localhost:8080/product`

### Microservice 2 Endpoints

- **Order Operations**:
    - Place an order: `POST http://localhost:8081/order/{productId}`
    - Get user orders: `GET http://localhost:8081/order`

### Mail Service Endpoints

- **Email Notifications**:
    - Send account creation email: Automatically triggered on user registration
    - Send order confirmation email: Automatically triggered on order placement

### API Gateway Endpoints

- Routes requests to the appropriate services.
    - **Auth**:
        - Register user: `POST http://localhost:8090/auth-server/user/register`
    - **Product Operations**:
        - Get all products: `GET http://localhost:8090/microservice-1/product`
        - Get product by ID: `GET http://localhost:8090/microservice-1/product/{id}`
        - Create product (Admin only): `POST http://localhost:8090/microservice-1/product`
    - **Order Operations**:
        - Place order: `POST http://localhost:8090/microservice-2/order/{productId}`
        - Get user orders: `GET http://localhost:8090/microservice-2/order`

## Key Features and Technologies

### Architecture

- **API Gateway**: Centralized entry point for all requests, routing them to appropriate services.
- **OAuth 2.0 Authorization Server**: Secure authentication and authorization with JWT tokens.
- **Microservices Architecture**: Decoupled, independently deployable services.
- **RabbitMQ**: Message broker for asynchronous communication between services.

### Authentication and Authorization

- **Spring Security**: Manages authentication and role-based access control.
- **OAuth 2.0 and OpenID Connect**: Ensures secure communication between services.
- **JWT Tokens**: Stateless, secure, and scalable method for authentication.

### REST API

- **REST API**: Design pattern for web services.
- **Spring WebFlux**: Reactive programming model for asynchronous communication.
- **Pagination**: Optimizes performance for large data sets.
- **Caching**: Improves performance by reducing redundant requests.

### Database

- **H2**: Relational database for persistent data storage.
- **Spring Data JPA**: Simplifies database interactions.
- **Flyway**: Manages database migrations for consistency across environments.

### Service Discovery

- **Eureka Server**: Provides service registry for dynamic scaling and resilience.

### Monitoring and Logging

- **Spring Boot Actuator**: Provides application health metrics and monitoring endpoints.
- **Logging**: Configurable logging for debugging and tracing requests.

### Email Notifications

- **JavaMailSender**: Handles email sending.
- **RabbitMQ**: Ensures reliable, asynchronous email communication.

### Testing

- **JUnit**: Unit testing framework.
- **Mockito**: Mocking framework for unit tests.
- **WireMock**: Simulates external services for integration testing.
- **MockMvc**: Tests Spring MVC controllers.
