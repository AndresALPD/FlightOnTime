package com.flightontime.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = WeekendValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidWeekend {

    String message() default "El valor de 'es_finde' no coincide con el día de la semana";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
