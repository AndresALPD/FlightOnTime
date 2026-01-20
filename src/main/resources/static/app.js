/**************************************************
 * VARIABLES GLOBALES
 **************************************************/
let selectedCity = null;   // Ciudad seleccionada para clima
let debounceTimer;         // Timer para debounce del autocomplete


/**************************************************
 * CARGA INICIAL
 **************************************************/
document.addEventListener("DOMContentLoaded", () => {

    // Carga inicial de catálogos
    cargarAerolineas();
    renderizarHistorial();

    // Cascada de selects
    document.getElementById("aerolinea")
        ?.addEventListener("change", cargarOrigenes);

    document.getElementById("origen")
        ?.addEventListener("change", cargarDestinos);

    document.getElementById("destino")
        ?.addEventListener("change", cargarDistancia);

    // Habilitar botón de predicción al cambiar cualquier input
    document.querySelectorAll(
        "#aerolinea, #origen, #destino, #hora_salida, #fecha_salida, #distancia_km, #taxi_out"
    ).forEach(el => {
        el.addEventListener("change", habilitarBotonPrediccion);
        el.addEventListener("input", habilitarBotonPrediccion);
    });

    /**************************************************
     * AUTOCOMPLETE DE CIUDADES (CLIMA)
     **************************************************/
    const inputCity = document.getElementById("weather-city");
    const suggestions = document.getElementById("city-suggestions");

    if (inputCity) {
        inputCity.addEventListener("input", () => {
            clearTimeout(debounceTimer);

            const query = inputCity.value;
            if (query.length < 3) {
                suggestions.innerHTML = "";
                return;
            }

            // Debounce para no saturar el backend
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
    }
});


/**************************************************
 * CATÁLOGOS DE VUELOS
 **************************************************/

// Carga aerolíneas
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

// Carga orígenes según aerolínea
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

    // Reset de campos dependientes
    document.getElementById("destino").innerHTML =
        `<option value="">Seleccione destino</option>`;
    document.getElementById("distancia_km").value = "";
    document.getElementById("taxi_out").value = "";
}

// Carga destinos según aerolínea + origen
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

// Carga distancia y taxi_out según ruta
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


/**************************************************
 * PREDICCIÓN DE RETRASO
 **************************************************/

// Limpia mensajes de error previos
function limpiarErrores() {
    document.querySelectorAll(".error").forEach(e => e.innerText = "");
}

// Ejecuta la predicción
function predecir() {

    const btn = document.getElementById("btnPredict");

    // Evita doble click
    if (btn.disabled) return;
    btn.disabled = true;

    limpiarErrores();

    const resultadoDiv = document.getElementById("resultado");

    // Spinner de carga
    resultadoDiv.innerHTML = `
        <div style="text-align:center; padding:20px;">
            <i class="fa-solid fa-circle-notch fa-spin"></i>
            Analizando datos de vuelo...
        </div>
    `;
    resultadoDiv.classList.add("result-visible");

    // Payload al backend
    const data = {
        aerolinea: document.getElementById("aerolinea").value,
        origen: document.getElementById("origen").value,
        destino: document.getElementById("destino").value,
        hora_salida: parseInt(document.getElementById("hora_salida").value),
        fecha_salida: document.getElementById("fecha_salida").value,
        distancia_km: parseFloat(document.getElementById("distancia_km").value),
        taxi_out: parseInt(document.getElementById("taxi_out").value)
    };

    fetch("http://localhost:8080/api/flight-delay/predict", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
        .then(res =>
            res.ok ? res.json() : res.json().then(err => Promise.reject(err))
        )
        .then(mostrarResultado)
        .catch(mostrarError);
}

// Reactiva el botón cuando se modifica cualquier campo
function habilitarBotonPrediccion() {
    const btn = document.getElementById("btnPredict");
    btn.disabled = false;
    btn.innerHTML = `Analizar Vuelo <i class="fa-solid fa-bolt"></i>`;
}


/**************************************************
 * RESULTADOS Y ERRORES
 **************************************************/

function mostrarResultado(data) {
    const resultadoDiv = document.getElementById("resultado");

    let colorFondo, colorTexto, colorBorde, icono, badgeColor;

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

    resultadoDiv.style.backgroundColor = colorFondo;

    resultadoDiv.innerHTML = `
        <div class="card-result" style="border-left-color:${colorBorde};">
            <h3 style="color:${colorTexto};">
                <i class="fa-solid ${icono}"></i> Predicción
            </h3>
            <p><strong>Probabilidad retraso:</strong> ${data.probabilidad_retraso}%</p>
            <p><strong>Estado:</strong> ${data.retrasado === "SI" ? "Se espera retraso" : "A tiempo"}</p>
            <p style="font-style:italic;">"${data.mensaje}"</p>
        </div>
    `;

    resultadoDiv.classList.add("result-visible");

    // Mostrar historial luego de la primera predicción
    document.getElementById("historial")?.classList.remove("hidden");

    guardarEnHistorial(data);
    renderizarHistorial();
}

function mostrarError(error) {
    const resultadoDiv = document.getElementById("resultado");
    resultadoDiv.innerHTML = "";
    resultadoDiv.classList.remove("result-visible");

    if (error.errors) {
        for (const campo in error.errors) {
            const span = document.getElementById(`error-${campo}`);
            if (span) span.innerText = error.errors[campo];
        }
    } else {
        resultadoDiv.innerHTML = `
            <div style="color:#c62828;">
                <i class="fa-solid fa-circle-xmark"></i>
                ${error.message || "Error inesperado"}
            </div>
        `;
        resultadoDiv.classList.add("result-visible");
    }
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
                <strong>${h.aerolinea}</strong> ${h.origen} → ${h.destino}
                <div style="font-size:0.85rem; color:#666;">
                    ${h.fecha} · Riesgo ${h.riesgo} (${h.probabilidad}%)
                </div>
            </div>
        `;
    });
}
