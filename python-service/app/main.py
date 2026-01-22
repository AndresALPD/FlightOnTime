from fastapi import FastAPI, HTTPException, UploadFile, File
from pydantic import BaseModel, Field
from contextlib import asynccontextmanager
from fastapi.middleware.cors import CORSMiddleware
import pandas as pd
import joblib
import os
import io
from datetime import datetime, date
from typing import List

# ========================
# VARIABLES GLOBALES
# ========================
MODEL_PATH = "app/model/flight_delay_model_backend.pkl"
CSV_PATH = "app/data/flights_sample_3m.csv"

model = None
AEROLINEAS_VALIDAS = set()
AIRLINE_MAP = []
AIRLINE_NAME_BY_CODE = {}

# ========================
# LIFESPAN (Carga de recursos al iniciar)
# ========================
@asynccontextmanager
async def lifespan(app: FastAPI):
    global model, AEROLINEAS_VALIDAS, AIRLINE_MAP, AIRLINE_NAME_BY_CODE
    print("🚀 Iniciando FlightOnTime API")

    # 1. Cargar Modelo
    if not os.path.exists(MODEL_PATH):
        print(f"❌ Error: No se encuentra el modelo en {MODEL_PATH}")
        raise RuntimeError("Modelo no encontrado")

    loaded = joblib.load(MODEL_PATH)
    # Si el pkl es un diccionario, buscamos el objeto del modelo
    model = loaded.get("model") if isinstance(loaded, dict) else loaded
    print(f"🧠 Modelo cargado: {type(model).__name__}")

    # 2. Cargar Aerolíneas desde CSV
    if not os.path.exists(CSV_PATH):
        print(f"❌ Error: No se encuentra el CSV en {CSV_PATH}")
        raise RuntimeError("CSV de referencia no encontrado")

    df_ref = pd.read_csv(CSV_PATH, usecols=["AIRLINE_CODE", "AIRLINE"], dtype=str).dropna().drop_duplicates()

    # Normalización para comparaciones seguras
    AEROLINEAS_VALIDAS = set(df_ref["AIRLINE_CODE"].str.upper().str.strip().unique())
    AIRLINE_MAP = df_ref.sort_values("AIRLINE").to_dict(orient="records")
    AIRLINE_NAME_BY_CODE = {row["AIRLINE_CODE"]: row["AIRLINE"] for row in AIRLINE_MAP}

    print(f"✈️ {len(AEROLINEAS_VALIDAS)} aerolíneas cargadas correctamente")
    yield

# ========================
# CONFIGURACIÓN APP
# ========================
app = FastAPI(
    title="FlightOnTime API",
    lifespan=lifespan
)

# CORS corregido para evitar "Failed to fetch"
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # En desarrollo permitimos todo; en producción usa ["http://localhost:8080"]
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ========================
# SCHEMAS
# ========================
class FlightRequest(BaseModel):
    aerolinea: str = Field(..., min_length=2, max_length=3)
    hora_salida: int = Field(..., ge=0, le=23)
    dia_semana: int = Field(..., ge=1, le=7)
    distancia_km: float = Field(..., gt=0)
    taxi_out: float = Field(15, ge=0)
    es_finde: int = Field(0, ge=0, le=1)

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
    return AIRLINE_MAP

@app.post("/predict", response_model=PredictionOutput)
def predict_delay(request: FlightRequest):
    cod = request.aerolinea.upper().strip()

    if cod not in AEROLINEAS_VALIDAS:
        raise HTTPException(status_code=400, detail=f"Aerolínea {cod} no soportada por el modelo")

    input_df = pd.DataFrame([{
        "DEP_HOUR": request.hora_salida,
        "DAY_OF_WEEK": request.dia_semana,
        "IS_WEEKEND": request.es_finde,
        "DISTANCE": request.distancia_km,
        "TAXI_OUT": request.taxi_out,
        "AIRLINE": cod
    }])

    prob = round(float(model.predict_proba(input_df)[0][1]) * 100, 2) if hasattr(model, "predict_proba") else 0.0
    pred = int(model.predict(input_df)[0])

    riesgo = "ALTO" if prob >= 70 else ("MEDIO" if prob >= 40 else "BAJO")

    return PredictionOutput(
        aerolinea_codigo=cod,
        aerolinea_nombre=AIRLINE_NAME_BY_CODE.get(cod, "Desconocida"),
        retrasado="SI" if pred == 1 else "NO",
        probabilidad_retraso=prob,
        nivel_riesgo=riesgo,
        mensaje=f"Probabilidad de retraso {riesgo.lower()}"
    )

@app.post("/predict-batch", response_model=List[PredictionOutput])
async def predict_batch(file: UploadFile = File(...)):
    if not file.filename.endswith('.csv'):
        raise HTTPException(status_code=400, detail="El archivo debe ser un CSV")

    try:
        contents = await file.read()
        df_input = pd.read_csv(io.BytesIO(contents))

        mapping = {
            "aerolinea": "AIRLINE", "hora_salida": "DEP_HOUR",
            "dia_semana": "DAY_OF_WEEK", "distancia_km": "DISTANCE",
            "taxi_out": "TAXI_OUT", "es_finde": "IS_WEEKEND"
        }

        # Validar columnas necesarias
        for col in mapping.keys():
            if col not in df_input.columns:
                raise HTTPException(status_code=400, detail=f"Falta la columna requerida: {col}")

        # Preparación y normalización
        df_model = df_input[list(mapping.keys())].rename(columns=mapping)
        df_model["AIRLINE"] = df_model["AIRLINE"].astype(str).str.upper().str.strip()

        # Separar registros válidos de desconocidos
        mask_validas = df_model["AIRLINE"].isin(AEROLINEAS_VALIDAS)
        preds_final = [0] * len(df_model)
        probs_final = [0.0] * len(df_model)

        if mask_validas.any():
            df_validas = df_model[mask_validas]
            preds_final_validas = model.predict(df_validas)
            probs_final_validas = model.predict_proba(df_validas)[:, 1] if hasattr(model, "predict_proba") else [0.0] * len(preds_final_validas)

            # Mapear resultados a sus índices originales
            indices = df_model.index[mask_validas].tolist()
            for i, idx in enumerate(indices):
                preds_final[idx] = preds_final_validas[i]
                probs_final[idx] = probs_final_validas[i]

        # Construir lista de salida
        res = []
        for i in range(len(df_model)):
            cod = df_model.iloc[i]["AIRLINE"]
            if not mask_validas.iloc[i]:
                res.append(PredictionOutput(
                    aerolinea_codigo=cod, aerolinea_nombre="Aerolinea desconocida",
                    retrasado="N/A", probabilidad_retraso=0.0,
                    nivel_riesgo="DESCONOCIDO", mensaje="Aerolinea no soportada por el modelo"
                ))
            else:
                p_val = round(float(probs_final[i]) * 100, 2)
                r_val = "ALTO" if p_val >= 70 else ("MEDIO" if p_val >= 40 else "BAJO")
                res.append(PredictionOutput(
                    aerolinea_codigo=cod, aerolinea_nombre=AIRLINE_NAME_BY_CODE.get(cod, "Desconocida"),
                    retrasado="SI" if int(preds_final[i]) == 1 else "NO",
                    probabilidad_retraso=p_val, nivel_riesgo=r_val, mensaje=f"Probabilidad {r_val.lower()}"
                ))
        return res

    except Exception as e:
        print(f"❌ Error en Batch: {e}")
        raise HTTPException(status_code=500, detail=f"Error al procesar el lote: {str(e)}")

# ========================
# MAIN
# ========================
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=5000)