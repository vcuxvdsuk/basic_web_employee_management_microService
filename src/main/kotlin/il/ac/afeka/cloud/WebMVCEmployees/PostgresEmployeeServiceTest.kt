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

    // Test if PostgreSQL is reachable
    try {
        Socket("localhost", 57984).use {
            println("✅ PostgreSQL is reachable on port 57984")
        }
    } catch (e: Exception) {
        println("❌ PostgreSQL is NOT reachable: ${e.message}")
    }

    // Test data for EmployeeBoundary
    val employeeBoundary = EmployeeBoundary(
        email = "test@example.com",
        name = "John",
        password = "Password123",
        birthDate = LocalDateTime.now().toDateInfo(),
        roles = listOf("Admin")
    )

    // Initialize RestTemplate
    val restTemplate = RestTemplate()

    // Create headers and JSON body for the POST request
    val headers = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
    }

    val objectMapper = jacksonObjectMapper().apply {
        registerKotlinModule()
    }
    val body = objectMapper.writeValueAsString(employeeBoundary)
    println("Sending JSON body:\n$body")

    val entityForPost = HttpEntity(body, headers)

    // Measure POST API call time
    val postStartTime = System.currentTimeMillis()

    try {
        val postResponse = restTemplate.postForEntity(
            "http://localhost:8080/employees",
            entityForPost,
            String::class.java
        )
        println("POST Response: ${postResponse.body}")
    } catch (e: Exception) {
        println("POST Error: ${e.message}")
    }

    val postEndTime = System.currentTimeMillis()
    println("POST API call took ${postEndTime - postStartTime} ms")

    // Measure GET API call time
    val getStartTime = System.currentTimeMillis()

    try {
        val getResponse = restTemplate.getForObject(
            "http://localhost:8080/employees",
            String::class.java
        )
        println("GET Response: $getResponse")
    } catch (e: Exception) {
        println("GET Error: ${e.message}")
    }

    val getEndTime = System.currentTimeMillis()
    println("GET API call took ${getEndTime - getStartTime} ms")
}
