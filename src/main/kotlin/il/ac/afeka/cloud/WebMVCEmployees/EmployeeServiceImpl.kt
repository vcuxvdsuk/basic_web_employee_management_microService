package il.ac.afeka.cloud.WebMVCEmployees

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EmployeeServiceImpl(
    val EmployeeCrud: EmployeeCrud,
    private val employeeCrud: EmployeeCrud,
) : EmployeeService {
    val logger:Log = LogFactory.getLog(EmployeeServiceImpl::class.java)


    @org.springframework.transaction.annotation.Transactional(readOnly = false)
    override fun createEmployee(employee: EmployeeBoundary): EmployeeBoundary{
        /*
        פעולה שמקבלת פרטים של עובדת בחברה, שיש לאחסן בשירות.
       פעולה זו מחזירה את הפרטים, לאחר שמירתם, למפעיל השירות
       במידה וכבר קיימים פרטים עם הדואל שהועבר לשירות, הפעולה תכשל
        */

        if (this.EmployeeCrud.existsByEmail(employee.email!!))
            throw (InvalidInputException("info already exist"))

        if(!employee.password!!.trim().contains(Regex("[A-Z]+")) ||
            !employee.password!!.trim().contains(Regex("[0-9]+")))
            throw (InvalidInputException("password must contain at least one digit and one upper letter"))

        if(employee.birthDate == null ||
            employee.isDateEmpty(employee.birthDate!!))
            throw (InvalidInputException("birth Date must be of the following format {\"day\":\"**\", \"month\":\"**\", \"year\":\"****\"}"))

        if(employee.roles == null ||
            employee.roles!!.isEmpty() ||
           !employee.roles!!.all { s: String ->  s.trim().isNotEmpty()}
            )
            throw (InvalidInputException("roles must contain at least one entry"))

        return EmployeeBoundary(this.EmployeeCrud.save(employee.toEntity()))
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    override fun getEmployee(email: String, password: String) : EmployeeBoundary{
        /*
        פעולה שמחזירה פרטי עובדת ספציפית.
        פעולה זו מקבלת כפרמטרים את הדואל של העובדת ואת הסיסמא שלה, כפי שנשמרה בשירות.
        במידה וקיימים בשירות נתונים על עובדים עם ה-email והסיסמא שהועברו, הפעולה תחזיר למפעיל השירות את הפרטים.
        במידה והנתונים לא קיימים בשירות, בדיוק כפי שהועברו בפרמטרים, הפעולה תחזיר שגיאה מתאימה
        שימו לב כי פעולה זו לא חושפת סיסמאות, אלא מחזירה את כל הפרטים השמורים בשירות, פרט לסיסמא
         */
        val employeeE = this.EmployeeCrud.findByEmail(email)
            .orElseThrow { EmployeeNotFoundException("Employee with given credentials not found") }

        val employeeB = EmployeeBoundary(employeeE)
        if (password != employeeB.password!!){
            throw(EmployeeNotFoundException("Employee with given credential not found"))
        }
        employeeB.password = null
        return employeeB
    }

    override fun getAll(page:Int,size:Int) : List<EmployeeBoundary>{
        /*
        פעולה שמחזירה את פרטי העובדים שנשמרו בשירות, ושתומכת ב-pagination
        שימו לב כי גם פעולה זו לא חושפת את הסיסמאות של העובדים
         */
        val pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"id"))
        return employeeCrud.findAll(pageable)
            .stream()
            .map {EmployeeBoundary(it)}
            .toList()
    }

    override fun getByDomain(email:String ,page: Int ,size: Int) : List<EmployeeBoundary>{
        /*
        פעולה שמחזירה את פרטי העובדים, שה-domain ב-email שלהם מתאים בדיוק לקלט שהפעולה מקבלת במשתנה domain.
        פעולה זו תומכת ב-pagination. שימו לב כי גם פעולה זו לא חושפת סיסמאות
        אם לא קיימים בשירות עובדים עם ה-domain המבוקש, הפעולה תחזיר מערך ריק
         */
        val domain = if (email.matches(regex = Regex("^.*@.+$"))){
            getDomain(email)
        }else{
            email
        }
        val pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"id"))

        val employeesOfDomain = EmployeeCrud.findByEmailDomain(domain,pageable)

        return employeesOfDomain
            .map {EmployeeBoundary(it)}
            .toList()
    }

    override fun getByRole(role:String,page:Int,size:Int) : List<EmployeeBoundary>{
        /*
        פעולה שמחזירה את פרטי העובדים, שאחד התפקידים שלהם, מתאים בדיוק למשתנה role.
        פעולה זו תומכת ב-pagination. שימו לב כי גם פעולה זו לא חושפת סיסמאות.
        אם לא קיימים בשירות עובדים עם התפקיד המבוקש, הפעולה תחזיר מערך ריק
         */
        if(role.trim().isEmpty())
            return emptyList()

        val pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"id"))

        return employeeCrud.findAllByRolesContains(EmployeeBoundary.rolesToString(listOf(role.trim()))!!,pageable)
            .stream()
            .map {EmployeeBoundary(it)}
            .toList()
    }

    override fun getByAge(age:Int,page: Int,size: Int) : List<EmployeeBoundary>{
        /*פעולה שמחזירה את פרטי העובדים, שהגיל שלהם בשנים, הועבר כפרמטר ageInYears.
        למשל, אם פעולה זו הופעלה, כדי לחפש עובדים בני 30 ב-1 באפריל 2025, היא תחזיר את כל העובדים שיום הולדתם ה-30 חל בין 1 באפריל 2024 ל-1 באפריל 2025
        פעולה זו תומכת ב-pagination. שימו לב כי גם פעולה זו לא חושפת סיסמאות.
        אם לא קיימים בשירות עובדים בגיל המבוקש, הפעולה תחזיר מערך ריק*/
        val pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"id"))

        val currentDateTime: LocalDateTime = LocalDateTime.now()
        val minForAge = currentDateTime.minusYears((age+1).toLong())
        val maxForAge = minForAge.plusYears(1).minusDays(1)

        return employeeCrud
            .findAllByBirthTimestampBetween(minForAge,maxForAge,pageable)
            .stream()
            .map {EmployeeBoundary(it)}
            .toList()
    }

    override fun deleteAll() {
        /*
        פעולה שתעזור לך לבדוק את השרות, שמוחקת את כל המידע בשירות, כולל קשרים בין נתונים, אם הגדרת כאלה
         */
        return employeeCrud.deleteAll()
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = false)
    override fun updateManagerEmailForEmployee(email: String,managerEmail: ManagerEmailBoundary){
        /*פעולה שמקבלת בקלט JSON במבנה של ManagerEmailBoundary, שהמבנה שלו מפורט בהמשך.
        פעולה זו תיצור קשר בין העובדת, שכתובת הדואל שלה הועברה בפרמטר employeeEmail ב-Path, לבין המנהלת שלה, שכתובת הדואל שלה מועברת ב-JSON שנשלח לפעולה.
        אם כבר קיים קשר כזה בין עובדת מסוימת למנהלת מסוימת, הפעולה לא תשנה דבר בשירות
        במידה ובשירות מוגדרת מנהלת אחרת לעובדת, השירות יקשר את העובדת למנהלת החדשה, שהמזהה שלה הועבר ב-JSON לשירות
        בכל מקרה, לכל עובדת, שמנוהלת בשירות שלך, תהיה לכל היותר מנהלת אחת.
        אם אחת מכתובות הדואל של העובדים לא קיימת בשירות, הפעולה תחזיר סטטוס שגיאה מתאים*/

        var manager = managerEmail.email
            ?.let { employeeCrud.findByEmail(it).orElse(null) }
            ?: throw InvalidEmailException("there are no employee with given manager email")

        val employee = employeeCrud.findByEmail(email).orElseThrow {
            InvalidEmailException("Employee with email $email not found")
        }

        employee.managerEmail = managerEmail.email
        employeeCrud.save(employee)
    }


    override fun getManagerOfEmployee(email: String): EmployeeBoundary{
        /*
        פעולה שמחזירה JSON עם פרטי המנהלת של עובדת, שהדואל שלה הועבר כפרמטר employeeEmail ב-Path
        פרטי המנהלת אמורים לכלול את המידע עליה, כולל דואל, שם, תפקידים ותאריך לידה, באותו מבנה JSON שמוחזר בפעולות אחרות שמחזירות פרטי עובדים
        במידה וכתובת הדואל לא מתאימה לעובדים שהוגדרו בשירות, או במידה ולא מוגדרת מנהלת לעובדת המבוקשת, הפעולה תחזיר שגיאה מתאימה
        שימו לב כי גם פעולה זו לא חושפת את הסיסמה של המנהלת, אלא מחזירה את פרטי המנהלת, השמורים בשירות, פרט לסיסמא
         */
        val employee = email
            .let { employeeCrud.findByEmail(it).orElse(null) }
            ?: throw EmployeeNotFoundException("there are no employee with given email")

        val manager = employee.managerEmail
            ?.let { employeeCrud.findByEmail(it).orElse(null) }
            ?: throw EmployeeNotFoundException("the employee manager doesnt exist")

        return EmployeeBoundary(manager)
    }

    override fun getAllEmployeesOfManager(managerEmail: ManagerEmailBoundary,page: Int,size: Int): List<EmployeeBoundary>{
        /*
    פעולה שמחזירה מערך של עובדים, שכפופים למנהלת, שהדואל שלה מוגדר בפרמטר managerEmail ב-Path
    פרטי העובדים שמוחזרים, אמורים לכלול את המידע שלהם, כולל דואל, שם, תפקידים ותאריך לידה, באותו מבנה JSON שמוחזר בפעולות אחרות שמחזירות פרטי עובדים
    פעולה זו תומכת ב-pagination.
    שימו לב כי גם פעולה זו לא חושפת סיסמאות.
    אם לא קיימת בשירות מנהלת, שהדואל שלה מוגדר כפרמטר, או אם לא קיימים בשירות עובדים שכפופים למנהלת זו, הפעולה תחזיר מערך ריק
     */
        val pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"id"))
        if (managerEmail.email == null){
            throw  InvalidEmailException("invalid email")
        }
        return employeeCrud
            .findAllByManagerEmail(managerEmail.email,pageable)
            .stream()
            .map { EmployeeBoundary(it) }
            .toList()
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = false)
    override fun deleteEmployeeManagerConnection(email: String){
        /*
        DELETE /employees/{employeeEmail}/manager

    פעולה שמנתקת את הקשר בין עובדת מסוימת, שהדואל שלה מוגדר ב-Path, לבין המנהלת שלה, במידה ויש קשר כזה בינהן
         */

        val employee = employeeCrud.findByEmail(email).orElseThrow {
            InvalidEmailException("Employee not found")
        }

        employee.managerEmail = null
        employeeCrud.save(employee)
    }

    //////////////////////////
    //  utils
    /////////////////////////
    fun getDomain(email: String): String{
        if(!email.matches(Regex("^[A-Za-z0-9]+@.+$")))
            throw(InvalidEmailException("invalid email format"))
        val domain = email.split("@")[1]
        return domain
    }

}