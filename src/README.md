# ✈️ Backend - Flight Delay API

Este módulo constituye el núcleo de la aplicación **FlightOnTime**, un sistema inteligente para la predicción de retrasos en vuelos comerciales. Desarrollado con **Java 17** y **Spring Boot**, gestiona la lógica de negocio, la persistencia de datos y la integración con servicios de Machine Learning y APIs meteorológicas.

---

## 🛠️ Tecnologías Utilizadas

El backend ha sido construido siguiendo estándares modernos de desarrollo:

*   **Lenguaje**: Java 17
*   **Framework**: Spring Boot 3.2.1 (Web, Validation, Data JPA)
*   **Base de Datos**: MySQL (con Hibernate)
*   **Cliente HTTP**: RestTemplate
*   **Herramientas**: Maven, Lombok, DevTools
*   **Integraciones**: OpenWeatherMap API, Microservicio Python (FastAPI/Flask)
*   **Frontend**: HTML5, CSS3, JavaScript (Vanilla), servido como recursos estáticos.

---

## 🏛️ Arquitectura del Proyecto

El proyecto sigue una **arquitectura en capas** clásica para asegurar la separación de responsabilidades y la mantenibilidad:

```
📂 `src/main/java/com/flightontime`
├── 🎮 controller: Gestiona las peticiones HTTP (REST endpoints).
├── 🧠 service: Contiene la lógica de negocio y orquesta llamados externos.
├── 💾 repository: Interfaz de acceso a datos (Spring Data JPA).
├── 📦 entity: Modelos que mapean las tablas de la base de datos.
├── 📨 dto: Data Transfer Objects para request/response limpios.
├── 🛡️ exception: Manejo global de errores y excepciones personalizadas.
└── ⚙️ config: Configuraciones de Beans y seguridad.

📂 `src/main/resources/static`
└── 🎨 frontend: Interfaz de usuario simple (HTML/CSS/JS) para interactuar con la API.
```
---

## 🎨 Frontend (Interfaz de Usuario)

El proyecto incluye una interfaz web ligera y moderna ubicada en la carpeta `static`, la cual es servida directamente por Spring Boot.

*   **Tecnologías**: HTML5, CSS3 (Diseño responsivo y Glassmorphism), JavaScript ES6.
*   **Funcionalidad**: Permite a los usuarios seleccionar aerolíneas y aeropuertos desde listas desplegables (cargadas dinámicamente desde la API), ingresar fechas y visualizar la predicción de retraso de forma amigable.
*   **Comunicación**: Utiliza `fetch` para consumir los endpoints REST del backend (`/api/catalog/...` y `/api/flight-delay/predict`).

---

## 🔄 Flujo de Predicción (Core)

El proceso principal de predicción sigue este flujo:

1.  **Recepción**: `FlightDelayController` recibe un `POST` con los datos del vuelo (`FlightDelayRequestDto`).
2.  **Validación**: Se validan los datos de entrada (formato, nulos, rangos) usando Bean Validation.
3.  **Procesamiento**:
    *   `FlightDelayService` calcula datos derivados (día de la semana, si es fin de semana).
    *   Convierte los datos al formato requerido por el modelo (`FlightDelayModelRequestDto`).
4.  **Inferencia (ML)**: El servicio contacta al microservicio Python (`/predict`) para obtener la probabilidad de retraso.
5.  **Persistencia**:
    *   El resultado y los datos de entrada se guardan en MySQL mediante `PrediccionRepository` para análisis histórico.
    *   Se utiliza una transacción (`@Transactional`) para garantizar la integridad.
6.  **Respuesta**: Se devuelve un objeto `FlightDelayResponseDto` al cliente con la predicción y el nivel de riesgo.

---

## 🔌 Integraciones Externas

### 🤖 Microservicio de Machine Learning
El backend actúa como un **Gateway** hacia el modelo de IA. No ejecuta el modelo directamente, sino que delega la inferencia a un servicio Python dedicado.
*   **Comunicación**: REST (HTTP POST).
*   **Endpoint**: `http://127.0.0.1:5000/predict`.

### 🌤️ API de Clima (OpenWeatherMap)
Utilizada para enriquecer la experiencia de usuario con información meteorológica en tiempo real.
*   **Service**: `WeatherService`.
*   **Endpoints**: Búsqueda por ciudad o coordenadas.
*   **Configuración**: `weather.api.key` en `application.properties`.

### 📂 Procesamiento en Lote (CSV)
Funcionalidad para el análisis masivo de vuelos mediante la carga de archivos.
*   **Funcionamiento**: El usuario sube un archivo `.csv` con múltiples registros de vuelos.
*   **Procesamiento**: El sistema procesa el archivo, conecta con el modelo de IA para cada registro y genera los resultados.
*   **Resultado**: Se devuelve un archivo CSV descargable que incluye las predicciones realizadas para cada vuelo.

---

## 💾 Base de Datos (MySQL)

Se utiliza **Spring Data JPA** para interactuar con la base de datos.

*   **Tabla Principal**: `predicciones_vuelos` (Mapeada en `PrediccionEntity`).
*   **Propósito**: Almacena cada consulta realizada por los usuarios junto con el resultado del modelo, permitiendo generar estadísticas de uso y monitorear el rendimiento del modelo a lo largo del tiempo.

---

## 🛡️ Manejo de Errores

Se implementa un manejo de errores robusto y centralizado mediante `@RestControllerAdvice` en `GlobalExceptionHandler`.

*   **Respuestas Estándar**: Todos los errores devuelven un formato JSON consistente (`ApiErrorResponse`).
*   **Tipos Manejados**:
    *   `MethodArgumentNotValidException`: Errores de validación de datos (400 Bad Request).
    *   `ExternalServiceException`: Fallos en la comunicación con Python o API Clima (502 Bad Gateway).
    *   Excepciones genéricas (500 Internal Server Error).

---

## 📡 Endpoints Principales

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/flight-delay/predict` | Envía datos de vuelo y devuelve la predicción de retraso. |
| `GET` | `/api/flight-delay/stats` | Devuelve estadísticas históricas de las predicciones. |
| `GET` | `/api/weather` | Obtiene el clima actual para una ciudad o coordenadas. |
| `GET` | `/api/catalog/...` | Datos estáticos (aerolíneas, aeropuertos) para frontend. |

---

## ✅ Buenas Prácticas Aplicadas

1.  **Uso de DTOs**: Las entidades de la base de datos nunca se exponen directamente en la API; se usan objetos de transferencia de datos.
2.  **Inyección de Dependencias**: Uso de `@Autowired` y constructores para gestionar componentes.
3.  **Variables de Entorno**: Credenciales y URLs configuradas en `application.properties` (o variables del sistema).
4.  **Separación de Responsabilidades**: Lógica de negocio fuera de los controladores.

---

## 🚀 Cómo Ejecutar

Asegúrate de tener configurada la base de datos y las variables de entorno (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `WEATHER_API_KEY`).

### Compilar y Ejecutar

```bash
# Compilar el proyecto en raíz
mvn clean install

# Ejecutar el backend
mvn spring-boot:run
```

---

## 🧪 Pruebas y Ejemplos de Uso

### 🖥️ Interfaz Web (Frontend)
Para probar la aplicación de forma visual:
1.  Abre tu navegador y ve a: `http://localhost:8080`
2.  Completa el formulario con los datos del vuelo.
3.  Haz clic en "Predecir" para ver el resultado y la probabilidad de retraso.

### 🔌 API REST (Postman / cURL)

**Endpoint para pruebas:**
`POST http://localhost:8080/api/flight-delay/predict`

### Caso 1: Baja probabilidad de retraso
Vuelo matutino entre hubs principales.
```json
{
  "aerolinea": "AA",
  "origen": "JFK",
  "destino": "LAX",
  "hora_salida": 8,
  "fecha_salida": "2026-05-20",
  "distancia_km": 3980,
  "taxi_out": 15
}
```

### Caso 2: Alta probabilidad de retraso
Vuelo nocturno en temporada alta con aerolínea de bajo costo.
```json
{
  "aerolinea": "NK",
  "origen": "ORD",
  "destino": "MCO",
  "hora_salida": 20,
  "fecha_salida": "2026-12-24",
  "distancia_km": 1500,
  "taxi_out": 45
}
```

### Caso 3: Prueba de validaciones (Genera error 400)
Datos incompletos o fuera de rango.
```json
{
  "aerolinea": "",
  "origen": "",
  "destino": "MIA",
  "hora_salida": 26,
  "fecha_salida": "2025-01-01",
  "distancia_km": -100,
  "taxi_out": -5
}
```
