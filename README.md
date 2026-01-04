# ✈️ FlightOnTime – Flight Delay Prediction API

Microservicio backend desarrollado en **Java + Spring Boot** para predecir si un vuelo despegará **a tiempo o con retraso**, a partir de datos históricos de vuelos.

Este proyecto forma parte del desafío **FlightOnTime**, orientado a Aviación Civil, Logística y Transporte Aéreo.

---

## 📌 Objetivo del Proyecto

Desarrollar una **API REST** capaz de:
- Recibir información de un vuelo
- Procesar variables relevantes (aerolínea, fecha, horario, etc.)
- Devolver una **estimación de retraso** o **on-time**

---

## 🧠 Descripción General

El microservicio expone endpoints REST que permiten:
- Consultar aerolíneas disponibles
- Enviar datos de un vuelo para su evaluación
- Obtener una predicción basada en el modelo entrenado y entragado por el equipo de Data Science 
 
Arquitectura basada en capas:
- Controller
- Service
- Model / DTO

---

## 🛠️ Tecnologías Utilizadas

- **Java 17**
- **Spring Boot**
- **Spring Web (REST)**
- **Maven**
- **IntelliJ IDEA**
- **Git / GitHub**

---

## 📂 Estructura del Proyecto

src/main/java/com/flightontime
│
├── controller
│ └── PredictionController.java
│
├── service
│ └── PredictionService.java
│
├── model
│ └── PredictionRequest.java
│
└── FlightDelayApiApplication.java

