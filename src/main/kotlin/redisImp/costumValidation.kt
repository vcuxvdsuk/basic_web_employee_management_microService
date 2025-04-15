package redisImp

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class Role(
    @field:NotBlank(message = "Role must not be blank")
    val value: String){

}