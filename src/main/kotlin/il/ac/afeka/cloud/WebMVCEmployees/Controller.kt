package il.ac.afeka.cloud.WebMVCEmployees

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = [ "/employees"])
class Controller(
    val employeeService:EmployeeService) {

    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@Valid @RequestBody newEmployee: EmployeeBoundary): ResponseEntity<EmployeeBoundary>{
        return ResponseEntity.ok(employeeService.createEmployee(newEmployee))
    }

    @GetMapping(
        path = ["/{employeeEmail}"],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getByEmailAndPassword(
        @PathVariable("employeeEmail") email:String,
        @RequestParam("password") password: String
    ):EmployeeBoundary{
        if(email.isEmpty() || password.isEmpty())
            throw InvalidInputException("email and password cant be empty")

        return  employeeService.getEmployee(email,password)
    }

    @GetMapping(
        produces = [MediaType.APPLICATION_JSON_VALUE],
        params = ["!criteria", "!value"])
    fun getAll(
        @RequestParam("page", defaultValue = "0") page:Int,
        @RequestParam("size", defaultValue = "5") size:Int
        ):List<EmployeeBoundary>{
        return  employeeService.getAll(page,size)
    }


    @GetMapping(
    produces = [MediaType.APPLICATION_JSON_VALUE],
    params = ["criteria", "value"])
    fun getByCriteria (
        @RequestParam("criteria", required = false) criteria:String,
        @RequestParam("value", required = false) value:String,
        @RequestParam("page", defaultValue = "0") page:Int,
        @RequestParam("size", defaultValue = "5") size:Int
        ):List<EmployeeBoundary>{
        return when (criteria){
            "byEmailDomain" -> employeeService.getByDomain(value,page,size)
            "byRole" -> employeeService.getByRole(value,page,size)
            "byAge" ->  {
                val age = value.toIntOrNull()
                    ?: throw IllegalArgumentException("Age must be a valid number")
                employeeService.getByAge(age, page, size)
            }
            else -> throw InvalidCriteriaException("Criteria option not valid")
        }
    }

    @DeleteMapping(
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun deleteAll(): ResponseEntity<Void> {
        employeeService.deleteAll()
        return ResponseEntity.noContent().build()
    }

    @PutMapping(
        path = ["/{employeeEmail}/manager"],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun updateManagerEmailForEmployee(
        @PathVariable("employeeEmail") employeeEmail: String,
        @Valid @RequestBody managerEmailBoundary: ManagerEmailBoundary) {
        /* PUT /employees/{employeeEmail}/manager  */
        if (employeeEmail.trim().isEmpty()) {
            throw InvalidEmailException("employeeEmail must contain at least 1 char")
        }
        employeeService.updateManagerEmailForEmployee(employeeEmail, managerEmailBoundary)
    }


    @GetMapping(
        path = ["/{employeeEmail}/manager"],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getManagerOfEmployee(
        @PathVariable("employeeEmail") employeeEmail: String) : EmployeeBoundary{
        /* GET /employees/{employeeEmail}/manager */
        return employeeService.getManagerOfEmployee(employeeEmail)
    }

    @GetMapping(
        path = ["/{managerEmail}/subordinates"],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getEmployeesOfManager(
        @PathVariable("managerEmail") managerEmail: String,
        @RequestParam("page", defaultValue = "0") page:Int,
        @RequestParam("size", defaultValue = "5") size:Int): List<EmployeeBoundary>{
        /* GET /employees/{managerEmail}/subordinates?page={page}&size={size} */
        val managerEmailB = ManagerEmailBoundary(managerEmail)
        return employeeService.getAllEmployeesOfManager(managerEmailB,page,size)
    }

    @DeleteMapping(
        path = ["/{employeeEmail}/manager"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun deleteEmployeeManagerConnection(
        @PathVariable("employeeEmail") employeeEmail: String){
        /* DELETE /employees/{employeeEmail}/manager */
        return employeeService.deleteEmployeeManagerConnection(employeeEmail)
    }
}