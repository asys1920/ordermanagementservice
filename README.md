# Ordermanagement Service
## Table of Contents

- [Description](#description)
- [Documentation](#documentation)
- [Features](#features)
- [Requirements](#requirements)
- [API](#api)

## Description
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/asys1920/ordermanagementservice)](https://github.com/asys1920/ordermanagementservice/releases/tag/v1.0.0)

This microservice is part of the car-rental project which was built
by the Asys course 19/20 at the TH Bingen.

It manages orders made by users, including reservations. It keeps track of every order made, even after it ends and is paid.

The Microservice can be monitored by Prometheus.

Logs can be sent to Elasticsearch/Logstash using Filebeat.

## Documentation
See [Management project](https://github.com/asys1920/management) for a documentation of the whole Car-Rental project.
## Features
This microservice can create, delete and finish orders when the car is handed in again. The latter automatically creates a bill as well.
If an order with a start and end date in the future is created, it will count as a reservation for that car.

## Requirements
A JDK with at least Java Version 11.

## API
To see a full documentation view the swagger documentation while running the microservice. You can
find the Swagger Documentation at `http://<host>:<port>/swagger-ui.html` 

Method | Endpoint | Parameters | Request Body | Description
--- | --- | ---  | --- | ---
GET | /orders | N/A | N/A | Gets all orders
GET | /orders | /bycar/{id} | N/A | Gets all orders containing the car specified by ID
GET | /orders | /byuser/{id} | N/A | Gets all orders containing the user specified by ID
GET | /orders | /{id} | N/A | Gets the order with the specified ID
POST | /orders | N/A | Order in JSON Format | Creates an Order based on the Request Body
DELETE | /orders | /{id} | N/A | Deletes the car with the specified ID
PATCH | /orders | /{id} | N/A | Updates the order specified by the ID which either cancels the order or hands the car in