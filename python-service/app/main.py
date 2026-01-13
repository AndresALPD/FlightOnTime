from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from contextlib import asynccontextmanager
import pandas as pd
import joblib
import os
from datetime import datetime, date

# ========================
# HISTORIAL DE PREDICCIONES (MEMORIA)
# ========================
PREDICTIONS_HISTORY = []

# ========================
# RUTAS
# ========================
MODEL_PATH = "app/model/flight_delay_model_backend.pkl"
CSV_PATH = "app/data/flights_sample_3m.csv"

# ========================
# VARIABLES GLOBALES
# ========================
model = None
AEROLINEAS_VALIDAS = set()     # AIRLINE_CODE
AIRLINE_MAP = []              # [{code, name}]
EXPECTED_COLUMNS = []

# ========================
# LIFESPAN
# ========================
@asynccontextmanager
async def lifespan(app: FastAPI):
    global model, AEROLINEAS_VALIDAS, AIRLINE_MAP, EXPECTED_COLUMNS

    print("🚀 Iniciando FlightOnTime API")

    # ---------
    # Cargar PKL
    # ---------
    if not os.path.exists(MODEL_PATH):
        raise RuntimeError(f"❌ Modelo no encontrado: {MODEL_PATH}")

    loaded = joblib.load(MODEL_PATH)

    if isinstance(loaded, dict):
        print("📦 PKL contiene un diccionario")

        for key, value in loaded.items():
            if hasattr(value, "predict"):
                model = value
                print(f"✅ Modelo encontrado en la clave: '{key}'")
                break
        else:
            raise RuntimeError("❌ No se encontró ningún objeto con predict()")

        EXPECTED_COLUMNS = loaded.get("expected_columns", [])
    else:
        model = loaded

    print(f"🧠 Modelo cargado: {type(model).__name__}")

    # ---------
    # CSV
    # ---------
    if not os.path.exists(CSV_PATH):
        raise RuntimeError(f"❌ CSV no encontrado: {CSV_PATH}")

    df = (
        pd.read_csv(
            CSV_PATH,
            usecols=["AIRLINE_CODE", "AIRLINE"],
            dtype=str
        )
        .dropna()
        .drop_duplicates()
    )

    AEROLINEAS_VALIDAS = set(df["AIRLINE_CODE"].unique())

    AIRLINE_MAP = (
        df[["AIRLINE_CODE", "AIRLINE"]]
        .sort_values("AIRLINE")
        .to_dict(orient="records")
    )

    print(f"✈️ Aerolíneas válidas: {len(AEROLINEAS_VALIDAS)}")
    yield


# ========================
# APP
# ========================
app = FastAPI(
    title="FlightOnTime API",
    description="Predicción de retrasos de vuelos",
    version="1.0.0",
    lifespan=lifespan
)

# ========================
# SCHEMAS
# ========================
class FlightRequest(BaseModel):
    AIRLINE: str = Field(..., alias="aerolinea", min_length=2, max_length=3)
    DEP_HOUR: int = Field(..., alias="hora_salida", ge=0, le=23)
    DAY_OF_WEEK: int = Field(..., alias="dia_semana", ge=1, le=7)
    DISTANCE: float = Field(..., alias="distancia_km", gt=0)
    TAXI_OUT: float = Field(15, alias="taxi_out", ge=0)
    IS_WEEKEND: int = Field(0, alias="es_finde", ge=0, le=1)

    class Config:
        populate_by_name = True


class PredictionOutput(BaseModel):
    airline_code: str
    delay_prediction: int
    will_be_delayed: bool
    delay_probability: float



# ========================
# ENDPOINTS
# ========================
@app.get("/airlines")
def listar_aerolineas():
    """
    Devuelve aerolíneas soportadas (código + nombre)
    """
    return AIRLINE_MAP


@app.post("/predict", response_model=PredictionOutput)
def predict_delay(request: FlightRequest):

    airline_code = request.AIRLINE.upper()

    if airline_code not in AEROLINEAS_VALIDAS:
        raise HTTPException(
            status_code=400,
            detail=f"Aerolínea no soportada: {airline_code}"
        )

    input_df = pd.DataFrame([{
        "DEP_HOUR": request.DEP_HOUR,
        "DAY_OF_WEEK": request.DAY_OF_WEEK,
        "IS_WEEKEND": request.IS_WEEKEND,
        "DISTANCE": request.DISTANCE,
        "TAXI_OUT": request.TAXI_OUT,
        "AIRLINE": airline_code
    }])

    try:
        # Predicción binaria (0 o 1)
        prediction = model.predict(input_df)[0]

        # Probabilidad de retraso (clase 1)
        if hasattr(model, "predict_proba"):
            delay_probability = model.predict_proba(input_df)[0][1]
            delay_probability = round(float(delay_probability) * 100, 2)
        else:
            delay_probability = 0.0

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error interno al predecir: {str(e)}"
        )

    will_be_delayed = int(prediction) > 0

    # Guardar predicción para estadísticas
    PREDICTIONS_HISTORY.append({
        "timestamp": datetime.utcnow(),
        "airline": airline_code,
        "will_be_delayed": will_be_delayed,
        "delay_probability": delay_probability
    })

    return PredictionOutput(
        airline_code=airline_code,
        delay_prediction=int(prediction),
        will_be_delayed=will_be_delayed,
        delay_probability=delay_probability
    )

@app.get("/test")
def test():
    ejemplo = FlightRequest(
        aerolinea=AIRLINE_MAP[0]["AIRLINE_CODE"],
        hora_salida=19,
        dia_semana=4,
        distancia_km=350,
        taxi_out=15,
        es_finde=0
    )
    return predict_delay(ejemplo)

@app.get("/stats")
def get_stats():
    """
    Devuelve estadísticas agregadas de las predicciones del día actual
    """
    today = date.today()

    today_predictions = [
        p for p in PREDICTIONS_HISTORY
        if p["timestamp"].date() == today
    ]

    total = len(today_predictions)

    if total == 0:
        return {
            "fecha": str(today),
            "total_vuelos": 0,
            "porcentaje_retrasados": 0.0,
            "porcentaje_puntuales": 0.0
        }

    delayed = sum(1 for p in today_predictions if p["will_be_delayed"])
    on_time = total - delayed

    return {
        "fecha": str(today),
        "total_vuelos": total,
        "porcentaje_retrasados": round(delayed / total * 100, 2),
        "porcentaje_puntuales": round(on_time / total * 100, 2)
    }


# ========================
# MAIN
# ========================
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=5000,
        log_level="info"
    )
