package demo

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.grails.plugins.excelimport.ExcelImportService
import groovy.transform.CompileStatic
import grails.gorm.transactions.Transactional

@CompileStatic
class StudentService {

    StudentDataService studentDataService

    static final int boundary = 100
    static final BigDecimal A_GRADE = 90
    static final Map CONFIG_STUDENT_COLUMN_MAP = [
            sheet: 'Sheet1',
            startRow: 1,
            columnMap: [
                    'A': 'name',
                    'B': 'grade'
            ]
    ]

    Random random = new Random()

    @Transactional
    void insertStudents(int numberOfStudents) {
        numberOfStudents.times {
            BigDecimal grade = random.nextInt(boundary) as BigDecimal
            studentDataService.save(produceRandomName(), grade)
        }
    }

    //tag::deleteStudents[]
    void deleteStudentsWithGradleLessThanA() {
        studentDataService.deleteByGradleLessThan(A_GRADE)
    }
    //end::deleteStudents[]

    //tag::htmlUnorderedListOfStudents[]
    String htmlUnorderedListOfStudents() {
        List<Student> students = studentDataService.findAll()
        StringBuffer result = new StringBuffer()
        result.append('<ul>')
        for (s in students) {
            result.append('<li>')
            result.append(s.toString())
            result.append('</li>')
        }
        result.append('</ul>')
        result.toString()
    }
    //end::htmlUnorderedListOfStudents[]

    @Transactional
    void saveExcelStudents(String fileName) {
        List<Map> studentData = importStudents(fileName)
        for (Map s in studentData) {
            studentDataService.save(s.name as String, s.grade as BigDecimal)
        }
    }

    //tag::importStudents[]
    protected List<Map> importStudents(String fileName) {
        Workbook workbook = WorkbookFactory.create(new File(fileName))
        ExcelImportService excelImportService = new ExcelImportService()
        excelImportService.convertColumnMapConfigManyRows(workbook, CONFIG_STUDENT_COLUMN_MAP) as List<Map>
    }
    //end::importStudents[]

    protected String produceRandomName() {
        "Name${random.nextInt(2*boundary)}"
    }
}
