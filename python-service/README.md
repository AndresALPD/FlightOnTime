# ✈️ Microservicio de Predicción de Retrasos con Python
Este microservicio expone un modelo de Machine Learning entrenado para predecir si un vuelo tendrá retraso (`delay_prediction = 1`) o no (`delay_prediction = 0`).

Está desarrollado en **Python + FastAPI** y se consume vía HTTP (Swagger / Postman / cualquier backend).

---

## 📁 Estructura del microservicio

Dentro de la carpeta `python-service`:

```
python-service/
├── app/
│ ├── model/ # Aquí va el archivo .pkl del modelo
│ ├── data/ # Aquí va el archivo CSV usado por el modelo
│ ├── batch.py
│ └── main.py
├── venv/ # Entorno virtual (NO se sube al repo)
├── requirements.txt # Dependencias del proyecto
└── README.md
```

---

## 📦 Archivos necesarios (OBLIGATORIO)

Por razones profesionales, **el dataset y el modelo NO se incluyen en el repositorio**.

Debes descargarlos desde el siguiente Google Drive:

🔗 **Drive**  
https://drive.google.com/drive/folders/1x_oxK6i0d5IFrOgKtRk6yf7cIUltL8yC

### Archivos del Drive:
- Modelo entrenado: `*.pkl`
- Modelo entrenado: `*.onnx` **(No utilizado en el proyecto)**
- Dataset: `*.csv`

---

## 📂 Ubicación correcta de los archivos

Una vez descargados:

1. Crear las carpetas dentro de `app`:
```
app/
├── model/
├── data/
```

2. Copiar los archivos:
   

```
app/
├── model/flight_delay_model_backend.pkl
├── data/flights_sample_3m.csv
```

⚠️ **Los nombres y rutas deben coincidir exactamente**, de lo contrario el microservicio no iniciará.

---

## 🐍 Crear y activar el entorno virtual (Windows)

### Requisitos previos
- Python **3.10 o 3.11** instalado  
  👉 Descárgalo desde el sitio oficial: https://www.python.org/downloads/
- `pip` actualizado

Para actualizar `pip`, ejecuta el siguiente comando dentro de la carpeta `python-service`:

```
py -m pip install --upgrade pip
```

De igual manera, desde la carpeta `python-service`, ejecuta los siguientes comandos:

### 1️⃣ Crear el entorno virtual
```
py -3.11 -m venv venv
```

### 2️⃣ Activar el entorno virtual

```
venv\Scripts\activate 
```

Si todo está correcto, verás algo similar a:

```
(venv) C:\ruta\al\proyecto\python-service>
```

---

## 📥 Instalar dependencias

Con el entorno virtual activo, ejecuta:

```
pip install -r requirements.txt
```

---

## 🚀 Iniciar el microservicio (FastAPI + Uvicorn)

Ejecuta el archivo principal:

```
uvicorn app.main:app --port 5000 --reload
```

Si todo está correcto, verás algo similar a:

```
🚀 Iniciando FlightOnTime API

🧠 Modelo cargado: Pipeline

✈️ Aerolíneas válidas: 18

Uvicorn running on http://0.0.0.0:5000
```

---

## 📘 Swagger – Documentación automática

Puedes dejar solo localhost (es suficiente):

Abre tu navegador y accede a:

```
http://localhost:5000/docs
http://127.0.0.1:5000/docs
```

Desde Swagger podrás:

- 🔍 Ver todos los **endpoints disponibles**
- 🧪 Probar el endpoint **POST** directamente desde el navegador
- 📄 Visualizar **ejemplos de JSON válidos** para las peticiones

---

## 📮 Probar el microservicio con Postman

### 🔗 Endpoint
```
POST http://localhost:5000/predict
```

### 🧾 Headers
```
Content-Type: application/json
```

### 📦 Body (JSON)
Seleccionamos raw y copiamos el siguiente json:
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

---

## 📤 Respuesta esperada
```
{
    "aerolinea_codigo": "DL",
    "aerolinea_nombre": "Delta Air Lines Inc.",
    "retrasado": "SI",
    "probabilidad_retraso": 63.08,
    "nivel_riesgo": "MEDIO",
    "mensaje": "Probabilidad de retraso medio"
}
```
---

## ⚠️ Notas importantes

- 🚫 La carpeta `venv/` **NO debe subirse al repositorio**
- 🚫 El **dataset** y el **modelo entrenado** **NO deben subirse a Git**
- 📦 El microservicio depende de las siguientes librerías:
  - `scikit-learn`
  - `numpy`
  - `joblib`
- 🔒 Las versiones de las dependencias están **fijadas en `requirements.txt`** para evitar problemas de compatibilidad

---
