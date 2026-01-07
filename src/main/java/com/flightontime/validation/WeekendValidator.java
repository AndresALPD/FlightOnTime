package com.flightontime.validation;

import com.flightontime.dto.FlightDelayRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class WeekendValidator implements ConstraintValidator<ValidWeekend, FlightDelayRequestDto> {

    @Override
    public boolean isValid(FlightDelayRequestDto dto, ConstraintValidatorContext context) {

        // Si alguno es null, dejamos que @NotNull lo maneje
        if (dto.getDia_semana() == null || dto.getEs_finde() == null) {
            return true;
        }

        boolean isWeekend = dto.getDia_semana() == 6 || dto.getDia_semana() == 7;

        // Coincidencia correcta
        if (isWeekend && dto.getEs_finde() == 1) return true;
        if (!isWeekend && dto.getEs_finde() == 0) return true;

        // Personalizamos el mensaje
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        "Inconsistencia: 'es_finde' no coincide con el día de la semana"
                ).addPropertyNode("es_finde")
                .addConstraintViolation();

        return false;
    }
}
