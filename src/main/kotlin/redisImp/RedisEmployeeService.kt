package redisImp

import il.ac.afeka.cloud.WebMVCEmployees.EmployeeNotFoundException
import il.ac.afeka.cloud.WebMVCEmployees.InvalidInputException
import il.ac.afeka.cloud.WebMVCEmployees.isAfter
import il.ac.afeka.cloud.WebMVCEmployees.isBefore
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RedisEmployeeService(
    private val repo: RedisEmployeeRepository
) : EmployeeService {

    private val logger = LogFactory.getLog(RedisEmployeeService::class.java)

    override fun createEmployee(employee: EmployeeBoundary): EmployeeBoundary {
        if (repo.existsById(employee.email!!))
            throw InvalidInputException("Email already exists")

        if (!employee.password!!.contains(Regex("[A-Z]")) || !employee.password!!.contains(Regex("[0-9]")))
            throw InvalidInputException("Password must contain at least one uppercase letter and one digit")

        if (employee.birthDate == null || employee.isDateEmpty(employee.birthDate!!))
            throw InvalidInputException("Birth date must not be null or empty")

        if (employee.roles.isNullOrEmpty() || employee.roles!!.any { it.trim().isEmpty() })
            throw InvalidInputException("Roles must not be empty or contain blank entries")

        val saved = repo.save(employee.toRedisEntity())

        return saved.toBoundary()
    }

    override fun getEmployee(email: String, password: String): EmployeeBoundary {
        val employee = repo.findById(email).orElseThrow {
            EmployeeNotFoundException("Employee not found")
        }

        if (employee.password != password)
            throw EmployeeNotFoundException("Invalid credentials")

        return employee.toBoundary()
    }

    override fun getAll(page: Int, size: Int): List<EmployeeBoundary> =
        repo.findAll()
            .sortedByDescending { it.email }
            .drop(page * size)
            .take(size)
            .map { it.toBoundary() }

    override fun getByDomain(email: String, page: Int, size: Int): List<EmployeeBoundary> {
        val normalizedDomain = if (email.contains("@")) email.split("@")[1] else email

        return repo.findAll()
            .filter { it.email.endsWith("@$normalizedDomain") }
            .sortedByDescending { it.email }
            .drop(page * size)
            .take(size)
            .map { it.toBoundary() }
    }

    override fun getByRole(role: String, page: Int, size: Int): List<EmployeeBoundary> {
        val trimmedRole = role.trim()
        if (trimmedRole.isEmpty()) return emptyList()

        return repo.findAll()
            .filter { it.roles.contains(trimmedRole) }
            .sortedByDescending { it.email }
            .drop(page * size)
            .take(size)
            .map { it.toBoundary() }
    }

    override fun getByAge(age: Int, page: Int, size: Int): List<EmployeeBoundary> {
        val now = LocalDateTime.now()
        val lowerBound = now.minusYears(age.toLong())
        val upperBound = lowerBound.plusYears(1).minusNanos(1)

        return repo.findAll()
            .filter {
                val b = it.birthTimestamp
                b != null && b.isAfter(lowerBound) && b.isBefore(upperBound)
            }
            .sortedByDescending { it.email }
            .drop(page * size)
            .take(size)
            .map { it.toBoundary() }
    }

    override fun deleteAll() = repo.deleteAll()
}
