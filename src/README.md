## Pruebas del sistema de predicción de retrasos

### Probar consultas válidas

Caso: Baja probabilidad de retraso (0)
```
{
  "aerolinea": "AA",
  "hora_salida": 10,
  "dia_semana": 3,
  "distancia_km": 400,
  "taxi_out": 8,
  "es_finde": 0
}
```

Caso: Alta probabilidad de retraso (1)
```
{
  "aerolinea": "DL",
  "hora_salida": 19,
  "dia_semana": 6,
  "distancia_km": 850,
  "taxi_out": 20,
  "es_finde": 1
}
```

Prueba de validaciones (errores)
```
{
  "aerolinea": "",
  "hora_salida": 30,
  "dia_semana": 8,
  "distancia_km": -50,
  "es_finde": 2
}
```
---
### Activar los servicios

Iniciar microservicio Python (FastAPI)
```
venv\Scripts\activate
uvicorn app.main:app --port 5000 --reload
```

Iniciar backend Spring Boot
```
mvn spring-boot:run
```

