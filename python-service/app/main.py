from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from contextlib import asynccontextmanager
from fastapi.middleware.cors import CORSMiddleware

import pandas as pd
import joblib
import os
from datetime import datetime, date
from typing import List
from fastapi import UploadFile, File
import io

# ========================
# HISTORIAL DE PREDICCIONES (MEMORIA)
# ========================
PREDICTIONS_HISTORY = []

# ========================
# RUTAS
# ========================
MODEL_PATH = "model/flight_delay_model_backend.pkl"
CSV_PATH = "data/flights_sample_3m.csv"

# ========================
# VARIABLES GLOBALES
# ========================
model = None
AEROLINEAS_VALIDAS = set()
AIRLINE_MAP = []
AIRLINE_NAME_BY_CODE = {}

# ========================
# LIFESPAN
# ========================
@asynccontextmanager
async def lifespan(app: FastAPI):
    global model, AEROLINEAS_VALIDAS, AIRLINE_MAP, AIRLINE_NAME_BY_CODE

    print("🚀 Iniciando FlightOnTime API")

    # ---------
    # Cargar modelo
    # ---------
    if not os.path.exists(MODEL_PATH):
        raise RuntimeError(f"❌ Modelo no encontrado: {MODEL_PATH}")

    loaded = joblib.load(MODEL_PATH)

    if isinstance(loaded, dict):
        for value in loaded.values():
            if hasattr(value, "predict"):
                model = value
                break
        if model is None:
            raise RuntimeError("❌ No se encontró un modelo válido en el PKL")
    else:
        model = loaded

    print(f"🧠 Modelo cargado: {type(model).__name__}")

    # ---------
    # Cargar CSV de aerolíneas
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

    AIRLINE_NAME_BY_CODE = {
        row["AIRLINE_CODE"]: row["AIRLINE"]
        for row in AIRLINE_MAP
    }

    print(f"✈️ Aerolíneas cargadas: {len(AEROLINEAS_VALIDAS)}")
    yield


# ========================
# APP
# ========================
app = FastAPI(
    title="FlightOnTime API",
    description="Predicción de retrasos de vuelos",
    version="1.2.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ========================
# SCHEMA DE ENTRADA
# ========================
class FlightRequest(BaseModel):
    aerolinea: str = Field(..., min_length=2, max_length=3)
    hora_salida: int = Field(..., ge=0, le=23)
    dia_semana: int = Field(..., ge=1, le=7)
    distancia_km: float = Field(..., gt=0)
    taxi_out: float = Field(15, ge=0)
    es_finde: int = Field(0, ge=0, le=1)

# ========================
# SCHEMA DE SALIDA (NORMALIZADO)
# ========================
class PredictionOutput(BaseModel):
    aerolinea_codigo: str
    aerolinea_nombre: str
    retrasado: str
    probabilidad_retraso: float
    nivel_riesgo: str
    mensaje: str

# ========================
# ENDPOINTS
# ========================
@app.get("/airlines")
def listar_aerolineas():
    """Lista aerolíneas soportadas"""
    return AIRLINE_MAP


@app.post("/predict", response_model=PredictionOutput)
def predict_delay(request: FlightRequest):

    aerolinea_codigo = request.aerolinea.upper()


    if aerolinea_codigo not in AEROLINEAS_VALIDAS:
        raise HTTPException(
            status_code=400,
            detail=f"Aerolínea no soportada: {aerolinea_codigo}"
        )

    input_df = pd.DataFrame([{
        "DEP_HOUR": request.hora_salida,
        "DAY_OF_WEEK": request.dia_semana,
        "IS_WEEKEND": request.es_finde,
        "DISTANCE": request.distancia_km,
        "TAXI_OUT": request.taxi_out,
        "AIRLINE": aerolinea_codigo
    }])

    try:
        aerolinea_nombre = AIRLINE_NAME_BY_CODE.get(aerolinea_codigo, "Desconocida")
        prediccion = int(model.predict(input_df)[0])

        if hasattr(model, "predict_proba"):
            probabilidad_retraso = round(
                float(model.predict_proba(input_df)[0][1]) * 100, 2
            )
        else:
            probabilidad_retraso = 0.0

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error interno al predecir: {str(e)}"
        )

    retrasado = "SI" if prediccion == 1 else "NO"


    # ========================
    # INTERPRETACIÓN
    # ========================

    RIESGO_ALTO = 70
    RIESGO_MEDIO = 40


    if probabilidad_retraso >= RIESGO_ALTO:
        nivel_riesgo = "ALTO"
        mensaje = "Alta probabilidad de retraso. Se recomienda prever demoras"
    elif probabilidad_retraso >= RIESGO_MEDIO:
        nivel_riesgo = "MEDIO"
        mensaje = "Probabilidad media de retraso"
    else:
        nivel_riesgo = "BAJO"
        mensaje = "Baja probabilidad de retraso."

    return PredictionOutput(
        aerolinea_codigo=aerolinea_codigo,
        aerolinea_nombre=aerolinea_nombre,
        retrasado=retrasado,
        probabilidad_retraso=probabilidad_retraso,
        nivel_riesgo=nivel_riesgo,
        mensaje=mensaje
    )


@app.post("/predict-batch", response_model=List[PredictionOutput])
async def predict_batch(file: UploadFile = File(...)):
    print(f"\n--- 📥 Nueva petición Batch: {file.filename} ---")

    if not file.filename.endswith('.csv'):
        raise HTTPException(status_code=400, detail="No es un CSV")

    try:
        # Leer archivo
        contents = await file.read()
        df_input = pd.read_csv(io.BytesIO(contents))
        print(f"✅ CSV leido. Filas: {len(df_input)}")

        # Definir mapeo
        mapping = {
            "aerolinea": "AIRLINE",
            "hora_salida": "DEP_HOUR",
            "dia_semana": "DAY_OF_WEEK",
            "distancia_km": "DISTANCE",
            "taxi_out": "TAXI_OUT",
            "es_finde": "IS_WEEKEND"
        }

        # VALIDACIÓN MANUAL DE COLUMNAS
        for col in mapping.keys():
            if col not in df_input.columns:
                print(f"❌ ERROR: Falta columna '{col}'")
                raise HTTPException(status_code=400, detail=f"Falta columna: {col}")

        # Preparar DataFrame para el modelo
        df_model = df_input[list(mapping.keys())].rename(columns=mapping)
        df_model["AIRLINE"] = df_model["AIRLINE"].str.upper().str.strip()
        print("✅ Columnas mapeadas y limpias")

        # INFERENCIA
        print("🧠 Iniciando predicción con el modelo...")
        preds = model.predict(df_model)

        if hasattr(model, "predict_proba"):
            probs = model.predict_proba(df_model)[:, 1]
        else:
            probs = [0.0] * len(preds)
        print("✅ Predicción completada")

        # Formatear respuesta
        results = []
        for i in range(len(df_model)):
            codigo = df_model.iloc[i]["AIRLINE"]
            prob = round(float(probs[i]) * 100, 2)

            # Lógica de riesgo
            riesgo = "ALTO" if prob >= 70 else ("MEDIO" if prob >= 40 else "BAJO")
            mensaje = "Probabilidad de retraso " + riesgo.lower()

            results.append(PredictionOutput(
                aerolinea_codigo=codigo,
                aerolinea_nombre=AIRLINE_NAME_BY_CODE.get(codigo, "Desconocida"),
                retrasado="SI" if int(preds[i]) == 1 else "NO",
                probabilidad_retraso=prob,
                nivel_riesgo=riesgo,
                mensaje=mensaje
            ))

        print(f"🚀 Enviando {len(results)} resultados")
        return results

    except Exception as e:
            import traceback
            # Esto forzará a que el error aparezca en la terminal negra de Uvicorn
            print("\n" + "="*50)
            print("❌ ERROR DETECTADO:")
            print(traceback.format_exc())
            print("="*50 + "\n")

            # Esto enviará el detalle al script batch.py
            raise HTTPException(status_code=500, detail=str(e))

@app.get("/stats")
def get_stats(fecha: str = None): # Agregamos el parámetro opcional
    """
    Devuelve estadísticas de una fecha específica (formato YYYY-MM-DD)
    o del día actual si no se envía nada.
    """
    if fecha:
        try:
            target_date = datetime.strptime(fecha, "%Y-%m-%d").date()
        except ValueError:
            return {"error": "Formato de fecha inválido. Use YYYY-MM-DD"}
    else:
        target_date = date.today()

    today_predictions = [
        p for p in PREDICTIONS_HISTORY
        if p["timestamp"].date() == target_date
    ]

    total = len(today_predictions)

    if total == 0:
        return {
            "fecha": str(target_date),
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
# INFO DEL MODELO
# ========================
@app.get("/model/info")
def model_info():
    return {
        "modelo": type(model).__name__,
        "tiene_predict": hasattr(model, "predict"),
        "tiene_predict_proba": hasattr(model, "predict_proba")
    }

# ========================
# MAIN
# ========================
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        app,
        host="127.0.0.1",
        port=5000,
        log_level="info"
    )
