# Microservices Template Project

## Introduction

This project is a template for a microservices architecture, designed to provide a starting point for building scalable
and modular applications. It includes an Authentication Server following OAuth2.0 standard with different grant type
flows, an API Gateway that serves as an entrypoint to the microservices, multiple microservices, each with its own
dedicated responsibilities, and Eureka server for service discovery.

## Table of Contents

- [Introduction](#introduction)
- [Table of Contents](#table-of-contents)
- [Microservices Overview](#microservices-overview)
- [Endpoints](#endpoints)
- [Key Features and Technologies](#key-features-and-technologies)

## Microservices Overview

### Authentication Server

The authentication server handles user authentication and authorization. It includes functionalities such as:

- User registration and management
- JWT token generation and validation
- Role-based access control
  It can be accessed at `http://localhost:8080`.

### Microservice 1

Microservice 1 handles product-related operations. Main functionalities include:

- CRUD operations for products
- Managing product inventory
  It can be accessed at `http://localhost:8081`.

### Microservice 2

Microservice 2 handles order-related operations. Main functionalities include:

- Creating and managing orders
- Integrating with the product service to retrieve product details
  It can be accessed at `http://localhost:8082`.

### Eureka Server

The Eureka server provides service discovery for the microservices. It can be accessed at `http://localhost:8761`.

### Endpoints

The endpoints to interact with the microservices:

- **Auth**:
    - **Client Credentials**:
        - JWT token request: `POST http://localhost:9000/oauth2/token`
    - **Authorization Code**:
        - Authorization code request: `GET http://localhost:9000/oauth2/authorize`
        - JWT token request: `POST http://localhost:9000/oauth2/token`
- **Microservice-1**:
    - **Product Operations**:
        - Get all products: `GET http://localhost:8080/product`
        - Get product by ID: `GET http://localhost:8080/product/{id}`
- **Microservice-2**:
    - **Order Operations**:
        - Place order: `POST http://localhost:8081/order/{productId}`
        - Get user orders: `GET http://localhost:8081/order`
- **API Gateway**:
    - **Product Operations**:
        - Get all products: `GET http://localhost:8090/microservice-1/product`
        - Get product by ID: `GET http://localhost:8090/microservice-1/product/{id}`
    - **Order Operations**:
        - Place order: `POST http://localhost:8090/microservice-2/order/{productId}`
        - Get user orders: `GET http://localhost:8090/microservice-2/order`

## Key Features and Technologies

- **Authentication and Authorization**:
    - Secure user management and access control with the authentication server.
    - **Spring Security**: Framework for authentication and authorization.
    - **OAuth 2.0 and OpenID Connect**: Protocols for authorization.

- **API Gateway**:
    - Provides a single entry point for clients.
    - **API Gateway**: Single entry point for clients.

- **Scalability**:
    - Each microservice can be scaled independently.
    - **Microservices**: Architecture style that structures an application as a collection of services.

- **Modularity**:
    - Clear separation of concerns for each microservice.
    - **SpringBoot**: Framework for building Java-based applications.
    - **REST API**: Design pattern for web services.
    - **Lombok**: Java library to reduce boilerplate code.
    - **Gradle**: Build tool for project automation.

- **Product Management**:
    - Comprehensive product handling by microservice 1.
    - **Spring Data JPA**: JPA data access abstraction.
    - **MySQL**: Relational database management system.

- **Order Processing**:
    - Efficient order handling and payment processing by microservice 2.
    - **Spring Data JPA**: JPA data access abstraction.
    - **MySQL**: Relational database management system.

- **Inter-Service Communication**:
    - Simplified inter-service communication using Feign Clients.
    - **Feign Clients**: Simplifies inter-service communication.

- **Service Discovery**:
    - Centralized service registry using Eureka server.
    - **Spring Eureka Server**: Service registry for locating microservices.

- **Monitoring & Logging**:
    - Provides endpoints for monitoring and managing the application.
    - **Spring Boot Actuator**: Application health monitoring.
