package redisImp


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class redisApplication

fun main(args: Array<String>) {
    runApplication<redisApplication>(*args)
}
