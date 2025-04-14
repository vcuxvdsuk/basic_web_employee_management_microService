package il.ac.afeka.cloud.WebMVCEmployees

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class DataInitializer : ApplicationRunner {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    override fun run(args: ApplicationArguments?) {
        val manager = EmployeeEntity(
            id = null,
            name = "Alice Manager",
            email = "email1@company.com",
            passwordHash = "hashedPassword123",
            birthTimestamp = LocalDateTime.of(1985, 5, 10, 0, 0),
            roles = "MANAGER",
            managerEmail = null
        )

        val emp1 = EmployeeEntity(
            id = null,
            name = "Bob Worker",
            email = "email2@company.com",
            passwordHash = "hashedPassword456",
            birthTimestamp = LocalDateTime.of(1990, 6, 12, 0, 0),
            roles = "EMPLOYEE",
            managerEmail = manager.email
        )

        val emp2 = EmployeeEntity(
            id = null,
            name = "Carol Developer",
            email = "email3@company.com",
            passwordHash = "hashedPassword789",
            birthTimestamp = LocalDateTime.of(1992, 8, 20, 0, 0),
            roles = "EMPLOYEE",
            managerEmail = manager.email
        )

        entityManager.persist(manager)
        entityManager.persist(emp1)
        entityManager.persist(emp2)

        println("✅ Employees inserted using EntityManager on application startup")
    }
}
