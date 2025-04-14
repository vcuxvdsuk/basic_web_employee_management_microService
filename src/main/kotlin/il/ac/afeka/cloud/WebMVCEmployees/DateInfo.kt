package il.ac.afeka.cloud.WebMVCEmployees

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import com.fasterxml.jackson.databind.annotation.JsonSerialize

data class DateInfo(
    @field:Min(1, message = "day must be at least 1")
    @field:Max(31, message = "day must be at most 31")
    @JsonSerialize(using = TwoDigitSerializer::class)
    val day: Int,
    @field:Min(1, message = "month must be at least 1")
    @field:Max(12, message = "month must be at most 12")
    @JsonSerialize(using = TwoDigitSerializer::class)
    val month: Int,
    @field:Min(1, message = "year must be at least 1. you did not born before jesus")
    @field:Max(2025, message = "year must be at most 2025, how did you born in the future?")
    @JsonSerialize(using = FourDigitSerializer::class)
    val year: Int
)
