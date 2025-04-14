package il.ac.afeka.cloud.WebMVCEmployees

interface EmployeeService {
    fun createEmployee (employee:EmployeeBoundary):EmployeeBoundary
    fun getEmployee(email: String, password: String) : EmployeeBoundary
    fun getByDomain(email:String,page: Int,size: Int) : List<EmployeeBoundary>
    fun getByRole(role:String,page:Int,size:Int) : List<EmployeeBoundary>
    fun getByAge(age:Int,page: Int,size: Int)  : List<EmployeeBoundary>
    fun getAll(page:Int,size:Int) : List<EmployeeBoundary>
    fun deleteAll()

    fun updateManagerEmailForEmployee(email: String,managerEmail: ManagerEmailBoundary)
    fun getManagerOfEmployee(email: String): EmployeeBoundary
    fun getAllEmployeesOfManager(managerEmail: ManagerEmailBoundary,page: Int,size: Int): List<EmployeeBoundary>
    fun deleteEmployeeManagerConnection(email: String)
}
