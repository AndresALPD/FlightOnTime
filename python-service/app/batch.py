import requests
import json
import pandas as pd # Necesitarás instalarlo: pip install pandas


# Configuración
URL_API = "http://127.0.0.1:5000/predict-batch"
ARCHIVO_ENTRADA = "vuelos.csv"
ARCHIVO_SALIDA = "predicciones_vuelos.csv"



def ejecutar_y_guardar():
    try:
        # 1. Enviar el archivo a la API
        print(f"📡 Enviando {ARCHIVO_ENTRADA}...")
        with open(ARCHIVO_ENTRADA, "rb") as f:
            files = {"file": (ARCHIVO_ENTRADA, f, "text/csv")}
            response = requests.post(URL_API, files=files)

        if response.status_code == 200:
            # 2. Convertir la respuesta JSON en un DataFrame de Pandas
            resultados = response.json()
            df_resultados = pd.DataFrame(resultados)

            # 3. Guardar en un nuevo archivo CSV
            df_resultados.to_csv(ARCHIVO_SALIDA, index=False, encoding='utf-8')

            print(f"✅ ¡Proceso completado!")
            print(f"📂 Resultados guardados en: {ARCHIVO_SALIDA}")
            print("\nResumen de las primeras filas:")
            print(df_resultados[['aerolinea_nombre', 'retrasado', 'nivel_riesgo']].head())
        else:
            print(f"❌ Error {response.status_code}: {response.text}")

    except Exception as e:
        print(f"❌ Error: {e}")

if __name__ == "__main__":
    ejecutar_y_guardar()