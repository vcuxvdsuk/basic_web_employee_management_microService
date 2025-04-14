package il.ac.afeka.cloud.WebMVCEmployees

import jakarta.validation.constraints.*

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