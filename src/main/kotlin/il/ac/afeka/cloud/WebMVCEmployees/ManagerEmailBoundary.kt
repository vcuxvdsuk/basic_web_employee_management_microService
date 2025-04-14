package il.ac.afeka.cloud.WebMVCEmployees

import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.time.LocalDateTime

class ManagerEmailBoundary(
        @field:Email(message = "Invalid email format")
        val email: String?){


        constructor(): this(null)

        override fun toString(): String {
            return  "{" +
                    "email:$email" +
                    "}"
        }

}