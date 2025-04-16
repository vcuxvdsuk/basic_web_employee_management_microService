package il.ac.afeka.cloud.WebMVCEmployees

import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.time.LocalDateTime
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.net.Socket

fun main() {

    // Test if PostgreSQL is reachable (via port check)
    try {
        Socket("localhost", 57952).use {
            println("✅ PostgreSQL is reachable on port 57952")
        }
    } catch (e: Exception) {
        println("❌ PostgreSQL is NOT reachable: ${e.message}")
        return
    }

    val restTemplate = RestTemplate()
    val headers = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
    }
    val objectMapper = jacksonObjectMapper().apply {
        registerKotlinModule()
    }

    var lastEmail: String? = null
    var lastBody: String? = null

    // Measure POST loop time
    val postStartTime = System.currentTimeMillis()

    for (i in 0 until 100) {
        val email = "test$i@example.com"
        val employeeBoundary = EmployeeBoundary(
            email = email,
            name = "John",
            password = "Password123",
            birthDate = LocalDateTime.now().toDateInfo(),
            roles = listOf("Admin")
        )

        val body = objectMapper.writeValueAsString(employeeBoundary)
        val entity = HttpEntity(body, headers)

        try {
            restTemplate.postForEntity(
                "http://localhost:8080/employees",
                entity,
                String::class.java
            )
        } catch (e: Exception) {
            println("❌ POST failed for $email: ${e.message}")
        }

        lastEmail = email
        lastBody = body
    }

    val postEndTime = System.currentTimeMillis()
    println("⏱️ POST API calls (100 inserts) took ${postEndTime - postStartTime} ms")

    // Print the last posted body for reference
    println("📤 Last inserted JSON:\n$lastBody")

    // Measure GET all employees
    val getStartTime = System.currentTimeMillis()

    try {
        val response = restTemplate.getForObject(
            "http://localhost:8080/employees",
            String::class.java
        )
        println("✅ GET Response:\n$response")
    } catch (e: Exception) {
        println("❌ GET Error: ${e.message}")
    }

    val getEndTime = System.currentTimeMillis()
    println("⏱️ GET API call took ${getEndTime - getStartTime} ms")
}
