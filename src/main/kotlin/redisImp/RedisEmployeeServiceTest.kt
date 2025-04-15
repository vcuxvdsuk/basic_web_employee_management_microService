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
        Socket("localhost", 57983).use {
            println("✅ Redis is reachable on port 57983")
        }
    } catch (e: Exception) {
        println("❌ Redis is NOT reachable: ${e.message}")
    }

    // Create LettuceConnectionFactory and initialize
    val lettuceConnectionFactory = LettuceConnectionFactory("localhost", 57983)
    lettuceConnectionFactory.afterPropertiesSet()

    // Create RedisTemplate with connection factory
    val redisTemplate = RedisTemplate<String, RedisEmployeeEntity>().apply {
        connectionFactory = lettuceConnectionFactory
        afterPropertiesSet()
    }

    // Test data for EmployeeBoundary
    val employeeBoundary = EmployeeBoundary(
        email = "test@example.com",
        name = "John",
        password = "Password123",
        birthDate = EmployeeBoundary.dateToDateInfo(LocalDateTime.now()),
        roles = listOf("Admin")
    )

    // Convert EmployeeBoundary to RedisEmployeeEntity
    val redisEntity = employeeBoundary.toRedisEntity()

    // Measure Redis operation time (set and get)
    val redisStartTime = System.currentTimeMillis()

    // Store the entity in Redis
    redisTemplate.opsForValue().set(redisEntity.email, redisEntity)

    // Retrieve and print the entity from Redis
    val redisResult = redisTemplate.opsForValue().get(redisEntity.email)
    println("Redis result: $redisResult")

    val redisEndTime = System.currentTimeMillis()
    println("Redis operations took ${redisEndTime - redisStartTime} ms")

    // Simulate API call to a controller endpoint (example of using RestTemplate)
    val restTemplate = RestTemplate()

    // Create headers and body for the POST request
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
        // Assuming your controller endpoint is running on localhost:8080
        val postResponse = restTemplate.postForEntity("http://localhost:8080/employees", entityForPost, String::class.java)
        println("Response: ${postResponse.body}")
    } catch (e: Exception) {
        println("Error: ${e.message}")
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
