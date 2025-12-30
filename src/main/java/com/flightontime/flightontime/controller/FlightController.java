package com.flightontime.flightontime.controller;

@RestController
@RequestMapping("/api/v1")
public class FlightController {

    @PostMapping("/predict")
    public Map<String, Object> predictFlight(@RequestBody Flight flight) {
        // Aquí podrías integrar el modelo predictivo, por ejemplo, llamando a un servicio de Python
        // o cargando el modelo directamente si se convierte a ONNX para Java. Aquí vamos a simular la respuesta.

        // Simulación de predicción}
Map<String, Object> response = new HashMap<>();
response.put("prevision", "Retrasado"); // "Puntual" o "Retrasado"
response.put("probabilidad", 0.78); // Probabilidad del retraso

return response;
}

// Puedes agregar más endpoints como /stats o cualquier otro que necesites.
}
