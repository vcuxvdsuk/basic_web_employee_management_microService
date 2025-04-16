package redisImp

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.time.LocalDateTime
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.net.Socket

fun main() {
    // Test if Redis is reachable
    try {
        Socket("localhost", 57951).use {
            println("✅ Redis is reachable on port 57951")
        }
    } catch (e: Exception) {
        println("❌ Redis is NOT reachable: ${e.message}")
        return
    }

    // Set up Redis connection
    val lettuceConnectionFactory = LettuceConnectionFactory("localhost", 57951).apply {
        afterPropertiesSet()
    }

    val redisTemplate = RedisTemplate<String, RedisEmployeeEntity>().apply {
        setConnectionFactory(lettuceConnectionFactory)
        afterPropertiesSet()
    }

    val redisStart = System.currentTimeMillis()

    for (i in 0 until 100) {
        val employee = EmployeeBoundary(
            email = "test$i@example.com",
            name = "John",
            password = "Password123",
            birthDate = EmployeeBoundary.dateToDateInfo(LocalDateTime.now()),
            roles = listOf("Admin")
        )
        val redisEntity = employee.toRedisEntity()
        redisTemplate.opsForValue().set(redisEntity.email, redisEntity)
    }

    val redisEnd = System.currentTimeMillis()
    println("⏱️ Redis insertions took ${redisEnd - redisStart} ms")

    // Simulate HTTP API interaction
    val restTemplate = RestTemplate()
    val headers = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
    }
    val objectMapper = jacksonObjectMapper().apply {
        registerKotlinModule()
    }

    // POST a sample employee for testing API POST
    val employeeSample = EmployeeBoundary(
        email = "testPostRedis@example.com",
        name = "CacheTest",
        password = "Secret321",
        birthDate = EmployeeBoundary.dateToDateInfo(LocalDateTime.now()),
        roles = listOf("Viewer")
    )

    val body = objectMapper.writeValueAsString(employeeSample)
    val postEntity = HttpEntity(body, headers)

    val postStart = System.currentTimeMillis()
    try {
        val postResponse = restTemplate.postForEntity(
            "http://localhost:8080/employees",
            postEntity,
            String::class.java
        )
        println("✅ POST Response: ${postResponse.body}")
    } catch (e: Exception) {
        println("❌ POST Error: ${e.message}")
    }
    val postEnd = System.currentTimeMillis()
    println("⏱️ POST API call took ${postEnd - postStart} ms")

    // Repeated GET requests to measure caching effect
    val repeatedEmails = listOf(
        "test0@example.com", "test0@example.com", "test0@example.com",
        "test50@example.com", "test50@example.com",
        "test99@example.com", "test99@example.com",
        "test20@example.com", "test70@example.com",
        "test0@example.com", "test50@example.com"
    )

    val getRepeatedStart = System.currentTimeMillis()
    for (email in repeatedEmails) {
        try {
            val response = restTemplate.getForObject(
                "http://localhost:8080/employees/email/$email",
                String::class.java
            )
            println("📥 GET $email → ${response?.substring(0..minOf(50, response.length - 1))}...")
        } catch (e: Exception) {
            println("❌ GET failed for $email: ${e.message}")
        }
    }
    val getRepeatedEnd = System.currentTimeMillis()
    println("⏱️ Repeated GETs took ${getRepeatedEnd - getRepeatedStart} ms")

    // Final: GET all employees
    val getAllStart = System.currentTimeMillis()
    try {
        val allResponse = restTemplate.getForObject("http://localhost:8080/employees", String::class.java)
        println("✅ GET all employees, size: ${allResponse?.length} chars")
    } catch (e: Exception) {
        println("❌ GET ALL failed: ${e.message}")
    }
    val getAllEnd = System.currentTimeMillis()
    println("⏱️ GET ALL took ${getAllEnd - getAllStart} ms")
}
