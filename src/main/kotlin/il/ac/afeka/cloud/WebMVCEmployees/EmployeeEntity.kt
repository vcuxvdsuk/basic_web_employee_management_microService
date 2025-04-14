package il.ac.afeka.cloud.WebMVCEmployees

import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "employees")
class EmployeeEntity (
    @Id @GeneratedValue
    var id: Long?,

    var name:String?,
    var email:String?,

    var passwordHash: String?,
    @Temporal(TemporalType.TIMESTAMP)
    var birthTimestamp: LocalDateTime?,
    var roles: String?,

    var managerEmail: String?){

    constructor():this(null,null,null,null,null,null,null)

    override fun toString(): String {
        return "{" +
                "email:$email, " +
                "name:$name, " +
                "birthdate:$birthTimestamp" +
                "roles:$roles" +
                "}"
    }

}