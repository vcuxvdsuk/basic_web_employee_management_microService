package il.ac.afeka.cloud.WebMVCEmployees

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDate

class ValidDateValidator : ConstraintValidator<ValidDate, DateInfo> {
    override fun isValid(value: DateInfo?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true  // leave null-check to @NotNull if needed
        return try {
            LocalDate.of(value.year, value.month, value.day)  // could throw DateTimeException
            true
        } catch (e: Exception) {
            throw InvalidInputException("date is impossible")
        }
    }
}
