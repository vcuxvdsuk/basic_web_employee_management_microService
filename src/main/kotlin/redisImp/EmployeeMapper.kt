package redisImp

fun RedisEmployeeEntity.toBoundary(): redisImp.EmployeeBoundary =
    EmployeeBoundary(this)
