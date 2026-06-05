package demo

import grails.gorm.services.Query
import grails.gorm.services.Service
import groovy.transform.CompileStatic

@CompileStatic
@Service(Student)
interface StudentDataService {
    List<Student> findAll()
    List<Student> findByGradeLessThan(BigDecimal grade)
    int count()
    Student save(String name, BigDecimal grade)
    //tag::deleteByGradleLessThan[]
    @Query("delete ${Student student} where $student.grade <= $grade")
    void deleteByGradleLessThan(BigDecimal grade)
    //tag::deleteByGradleLessThan[]
}
