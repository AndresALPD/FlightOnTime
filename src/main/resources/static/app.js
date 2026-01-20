/**************************************************
 * CARGA INICIAL
 **************************************************/
document.addEventListener("DOMContentLoaded", () => {
    cargarAerolineas();
    renderizarHistorial();

    document.getElementById("aerolinea")
        .addEventListener("change", cargarOrigenes);

    document.getElementById("origen")
        .addEventListener("change", cargarDestinos);

    document.getElementById("destino")
        .addEventListener("change", cargarDistancia);
});

/**************************************************
 * CATÁLOGOS DE VUELOS
 **************************************************/
async function cargarAerolineas() {
    const res = await fetch("http://localhost:8080/api/catalogs/airlines");
    const data = await res.json();

    const select = document.getElementById("aerolinea");
    select.innerHTML = `<option value="">Seleccione aerolínea</option>`;

    data.forEach(a => {
        select.innerHTML += `
            <option value="${a.code}">
                ${a.code} - ${a.name}
            </option>
        `;
    });
}

async function cargarOrigenes() {
    const airline = this.value;
    if (!airline) return;

    const res = await fetch(
        `http://localhost:8080/api/catalogs/origins?airline=${airline}`
    );
    const data = await res.json();

    const select = document.getElementById("origen");
    select.innerHTML = `<option value="">Seleccione origen</option>`;

    data.forEach(o => {
        select.innerHTML += `
            <option value="${o.code}">
                ${o.code} - ${o.city}
            </option>
        `;
    });

    // Reset dependientes
    document.getElementById("destino").innerHTML =
        `<option value="">Seleccione destino</option>`;
    document.getElementById("distancia_km").value = "";
    document.getElementById("taxi_out").value = "";
}

async function cargarDestinos() {
    const airline = document.getElementById("aerolinea").value;
    const origin = this.value;
    if (!origin) return;

    const res = await fetch(
        `http://localhost:8080/api/catalogs/destinations?airline=${airline}&origin=${origin}`
    );
    const data = await res.json();

    const select = document.getElementById("destino");
    select.innerHTML = `<option value="">Seleccione destino</option>`;

    data.forEach(d => {
        select.innerHTML += `
            <option value="${d.code}">
                ${d.code} - ${d.city}
            </option>
        `;
    });

    document.getElementById("distancia_km").value = "";
    document.getElementById("taxi_out").value = "";
}

async function cargarDistancia() {
    const airline = document.getElementById("aerolinea").value;
    const origin = document.getElementById("origen").value;
    const dest = this.value;
    if (!dest) return;

    const res = await fetch(
        `http://localhost:8080/api/catalogs/distance?airline=${airline}&origin=${origin}&dest=${dest}`
    );

    const data = await res.json();

    document.getElementById("distancia_km").value = data.distance;
    document.getElementById("taxi_out").value = data.taxi_out;
}

function predecir() {

    const btn = document.getElementById("btnPredict");

    // Si ya está deshabilitado, no hace nada
    if (btn.disabled) return;

    // Bloqueamos el botón
    btn.disabled = true;

    limpiarErrores();

    const resultadoDiv = document.getElementById("resultado");
    // Añadimos clase para animación suave
    resultadoDiv.classList.remove("result-visible");

    // Spinner moderno temporal
    resultadoDiv.innerHTML = `
        <div style="text-align:center; padding: 20px; color: #666;">
            <i class="fa-solid fa-circle-notch fa-spin"></i> Analizando datos de vuelo...
        </div>
    `;
    resultadoDiv.classList.add("result-visible");

    const data = {
        aerolinea: document.getElementById("aerolinea").value,
        origen: document.getElementById("origen").value,
        destino: document.getElementById("destino").value,
        hora_salida: parseInt(document.getElementById("hora_salida").value),
        fecha_salida: document.getElementById("fecha_salida").value,
        distancia_km: parseFloat(document.getElementById("distancia_km").value),
        taxi_out: parseInt(document.getElementById("taxi_out").value)
    };

    function limpiarErrores() {
        document.querySelectorAll(".error").forEach(e => e.innerText = "");
    }

    fetch("http://localhost:8080/api/flight-delay/predict", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => {
                    throw err;
                });
            }
            return response.json();
        })
        .then(result => {
            mostrarResultado(result);
        })
        .catch(error => {
            mostrarError(error);
        });
}

document.querySelectorAll(
    "#aerolinea, #origen, #destino, #hora_salida, #fecha_salida, #distancia_km, #taxi_out"
).forEach(el => {
    el.addEventListener("change", habilitarBotonPrediccion);
    el.addEventListener("input", habilitarBotonPrediccion);
});

function habilitarBotonPrediccion() {
    const btn = document.getElementById("btnPredict");

    btn.disabled = false;
    btn.innerHTML = `
        Analizar Vuelo <i class="fa-solid fa-bolt"></i>
    `;
}


function mostrarResultado(data) {

    const resultadoDiv = document.getElementById("resultado");

    // Configuración de estilos según riesgo
    let colorFondo;
    let colorTexto;
    let colorBorde;
    let icono;
    let badgeColor;

    if (data.nivel_riesgo === "ALTO") {
        colorFondo = "#ffebee";
        colorTexto = "#c62828";
        colorBorde = "#c62828";
        badgeColor = "#d32f2f";
        icono = "fa-triangle-exclamation";
    } else if (data.nivel_riesgo === "MEDIO") {
        colorFondo = "#fff8e1";
        colorTexto = "#f57f17";
        colorBorde = "#ffb300";
        badgeColor = "#fbc02d";
        icono = "fa-stopwatch";
    } else {
        colorFondo = "#e8f5e9";
        colorTexto = "#2e7d32";
        colorBorde = "#43a047";
        badgeColor = "#388e3c";
        icono = "fa-check-circle";
    }

    // Aplicamos estilos dinámicos al div contenedor
    resultadoDiv.style.backgroundColor = colorFondo;
    resultadoDiv.style.color = "#333";

    resultadoDiv.innerHTML = `
        <div class="card-result" style="border-left-color: ${colorBorde};">
            <div class="card-header-res">
                <h3 style="color:${colorTexto}; margin:0;">
                    <i class="fa-solid ${icono}"></i> Predicción
                </h3>
                <span class="risk-badge" style="background-color: ${badgeColor};">
                    Riesgo ${data.nivel_riesgo}
                </span>
            </div>

            <div class="card-body-res">
                <p><strong><i class="fa-solid fa-plane"></i> Aerolínea: </strong>&nbsp;
                   ${data.aerolinea_nombre} <small>(${data.aerolinea_codigo})</small>
                </p>

                <p><strong><i class="fa-solid fa-chart-line"></i> Probabilidad retraso: </strong>&nbsp;
                   ${data.probabilidad_retraso}%
                </p>

                <p><strong><i class="fa-regular fa-comment-dots"></i> Estado: </strong>&nbsp;
                   ${data.retrasado === 'SI' ? 'Se espera retraso' : 'A tiempo'}
                </p>

                <p style="margin-top: 10px; font-style: italic; color: #555; border-top: 1px dashed #ccc; padding-top: 10px;">
                   "${data.mensaje}"
                </p>
            </div>
        </div>
    `;

    // Aseguramos que se vea
    resultadoDiv.classList.add("result-visible");

// 👇 Muestra el historial SOLO tras la primera predicción
    const historialDiv = document.getElementById("historial");
    historialDiv.classList.remove("hidden");

    guardarEnHistorial(data);
    renderizarHistorial();
}

function mostrarError(error) {

    const resultadoDiv = document.getElementById("resultado");
    resultadoDiv.innerHTML = "";
    resultadoDiv.style.backgroundColor = "transparent";
    resultadoDiv.classList.remove("result-visible");

    // Si vienen errores de validación
    if (error.errors) {
        for (const campo in error.errors) {
            const errorSpan = document.getElementById(`error-${campo}`);

            if (errorSpan) {
                errorSpan.innerText = error.errors[campo];
            } else if (campo === 'aerolinea') {
                const aeroSpan = document.getElementById('aerolineaError');
                if(aeroSpan) aeroSpan.innerText = error.errors[campo];
            }
        }
    } else {
        resultadoDiv.innerHTML = `
            <div style="background-color: #ffebee; color: #c62828; padding: 15px; border-radius: 8px; border: 1px solid #ef9a9a;">
                <strong><i class="fa-solid fa-circle-xmark"></i> Error:</strong> ${error.message || "Error inesperado de conexión"}
            </div>
        `;
        resultadoDiv.classList.add("result-visible");
    }
}

function toggleWeather() {
    const weatherPanel = document.getElementById("weather-panel");
    const grafanaPanel = document.getElementById("grafana-panel");
    const statsPanel = document.getElementById("stats-panel");

    if (weatherPanel.classList.contains("hidden")) {
        // Cerramos los otros dos
        grafanaPanel.classList.add("hidden");
        statsPanel.classList.add("hidden");
        // Abrimos clima
        weatherPanel.classList.remove("hidden");
    } else {
        weatherPanel.classList.add("hidden");
    }
}

function toggleGrafana() {
    const weatherPanel = document.getElementById("weather-panel");
    const grafanaPanel = document.getElementById("grafana-panel");
    const statsPanel = document.getElementById("stats-panel");

    if (grafanaPanel.classList.contains("hidden")) {
        // Cerramos los otros dos
        weatherPanel.classList.add("hidden");
        statsPanel.classList.add("hidden");
        // Abrimos grafana
        grafanaPanel.classList.remove("hidden");
    } else {
        grafanaPanel.classList.add("hidden");
    }
}

function toggleStats() {
    const weatherPanel = document.getElementById("weather-panel");
    const grafanaPanel = document.getElementById("grafana-panel");
    const statsPanel = document.getElementById("stats-panel");

    if (statsPanel.classList.contains("hidden")) {
        // Cerramos los otros dos
        weatherPanel.classList.add("hidden");
        grafanaPanel.classList.add("hidden");
        // Abrimos estadísticas
        statsPanel.classList.remove("hidden");
    } else {
        statsPanel.classList.add("hidden");
    }
}

function consultarClima() {
    const resultDiv = document.getElementById("weather-result");

    if (!selectedCity) {
        resultDiv.innerHTML = "<span class='error'><i class='fa-solid fa-circle-exclamation'></i> Seleccione una ciudad</span>";
        return;
    }

    const { lat, lon } = selectedCity;

    resultDiv.innerHTML = `
        <div style="padding: 20px;">
            <i class="fa-solid fa-circle-notch fa-spin" style="color: var(--primary-color);"></i>
        </div>
    `;

    fetch(`http://localhost:8080/api/weather/by-coordinates?lat=${lat}&lon=${lon}`)
        .then(res => res.json())
        .then(data => {
            // Inyectamos una mini tarjeta de datos con iconos de FontAwesome
            resultDiv.innerHTML = `
                <div class="weather-card-mini">
                    <div class="weather-item">
                        <span>Temp</span>
                        <strong>${data.temperature} °C</strong>
                    </div>
                    <div class="weather-item">
                        <span>Cielo</span>
                        <strong style="text-transform: capitalize;">${data.description}</strong>
                    </div>
                    <div class="weather-item">
                        <span>Humedad</span>
                        <strong>${data.humidity}%</strong>
                    </div>
                    <div class="weather-item">
                        <span>Viento</span>
                        <strong>${data.windSpeed} m/s</strong>
                    </div>
                </div>
                <p style="font-size: 0.7rem; color: #999; margin-top: 10px;">
                    Datos actualizados para ${selectedCity.name}
                </p>
            `;
        })
        .catch(() => {
            resultDiv.innerHTML = "<span class='error'><i class='fa-solid fa-triangle-exclamation'></i> Error de conexión</span>";
        });
}

let selectedCity = null;

const inputCity = document.getElementById("weather-city");
const suggestions = document.getElementById("city-suggestions");

let debounceTimer;

inputCity.addEventListener("input", () => {
    clearTimeout(debounceTimer);

    const query = inputCity.value;
    if (query.length < 3) {
        suggestions.innerHTML = "";
        return;
    }

    debounceTimer = setTimeout(() => {
        fetch(`http://localhost:8080/api/weather/cities?q=${query}`)
            .then(res => res.json())
            .then(data => {
                suggestions.innerHTML = "";
                data.forEach(city => {
                    const li = document.createElement("li");
                    li.textContent = `${city.name}, ${city.country}`;

                    li.onclick = () => {
                        inputCity.value = `${city.name}, ${city.country}`;
                        selectedCity = city;
                        suggestions.innerHTML = "";
                    };

                    suggestions.appendChild(li);
                });

            });
    }, 300);
});

function consultarStats() {
    const fecha = document.getElementById("stats_fecha").value;
    const resultadoDiv = document.getElementById("stats_resultado");

    resultadoDiv.innerHTML = "Cargando estadísticas... ⏳";

    let url = "http://localhost:8080/api/flight-delay/stats";
    if (fecha) {
        url += `?fecha=${fecha}`;
    }

    fetch(url)
        .then(response => {
            if (!response.ok) {
                return response.text().then(err => {
                    throw new Error(err);
                });
            }
            return response.json();
        })
        .then(data => mostrarStats(data))
        .catch(error => {
            resultadoDiv.innerHTML = `
                <p style="color:red;">
                    ${error.message}
                </p>
            `;
        });
}

function mostrarStats(data) {
    const resultadoDiv = document.getElementById("stats_resultado");

    if (data.totalVuelos === 0) {
        resultadoDiv.innerHTML = `
            <div class="stats-card-grid" style="text-align:center;">
                <i class="fa-solid fa-calendar-xmark" style="font-size: 2rem; color: #ccc;"></i>
                <p style="margin-top:10px;">No hay registros para el ${data.fecha}</p>
            </div>
        `;
        return;
    }

    resultadoDiv.innerHTML = `
        <div class="stats-card-grid">
            <div style="border-bottom: 1px solid #eee; padding-bottom: 8px; margin-bottom: 5px; font-weight: 600; color: #673ab7;">
                <i class="fa-solid fa-calendar-day"></i> Reporte: ${data.fecha}
            </div>
            <p><i class="fa-solid fa-plane"></i> Total vuelos: <strong>${data.totalVuelos}</strong></p>

            <p style="color: #d32f2f;">
                <i class="fa-solid fa-clock"></i> Retrasados:
                <strong>${data.vuelosRetrasados} (${data.porcentajeRetrasados}%)</strong>
            </p>

            <p style="color: #2e7d32;">
                <i class="fa-solid fa-check-circle"></i> A tiempo:
                <strong>${data.vuelosATiempo} (${data.porcentajePuntuales}%)</strong>
            </p>
        </div>
    `;
}

/**************************************************
 * HISTORIAL DE CONSULTAS (sessionStorage)
 **************************************************/
function guardarEnHistorial(data) {
    const historial = JSON.parse(sessionStorage.getItem("historialVuelos")) || [];

    historial.unshift({
        fecha: new Date().toLocaleString(),
        aerolinea: data.aerolinea_codigo,
        origen: data.origen,
        destino: data.destino,
        riesgo: data.nivel_riesgo,
        probabilidad: data.probabilidad_retraso
    });

    // Limita a últimas 5 consultas (opcional)
    historial.splice(5);

    sessionStorage.setItem("historialVuelos", JSON.stringify(historial));
}

function renderizarHistorial() {
    const contenedor = document.getElementById("historial-lista");
    if (!contenedor) return;

    const historial = JSON.parse(sessionStorage.getItem("historialVuelos")) || [];

    if (historial.length === 0) {
        contenedor.innerHTML = `<p style="color:#888;">Sin consultas aún</p>`;
        return;
    }

    contenedor.innerHTML = "";

    historial.forEach(h => {
        contenedor.innerHTML += `
            <div class="history-item">
                <div>
                    <strong>${h.aerolinea}</strong> ${h.origen} → ${h.destino}
                </div>
                <div style="font-size:0.85rem; color:#666;">
                    ${h.fecha} · Riesgo ${h.riesgo} (${h.probabilidad}%)
                </div>
            </div>
        `;
    });
}
