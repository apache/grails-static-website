package example.grails

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic

import static org.springframework.http.HttpStatus.OK

@CompileStatic
class ExcelController implements GrailsConfigurationAware { // <1>

    BookService bookService
    BookExcelService bookExcelService

    String xlsxMimeType
    String encoding

    @Override
    void setConfiguration(Config co) {  // <1>
        xlsxMimeType = co.getProperty('grails.mime.types.xlsxMimeType',
                String,
                'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
        encoding = co.getProperty('grails.converters.encoding', String, 'UTF-8')
    }

    def index() {
        response.status = OK.value() // <2>
        response.setHeader "Content-disposition", "attachment; filename=${BookExcelService.EXCEL_FILENAME}" // <3>
        response.contentType = "${xlsxMimeType};charset=${encoding}" // <4>
        OutputStream outs = response.outputStream
        bookExcelService.exportExcelFromBooks(outs, bookService.findAll()) // <5>
        outs.flush()
        outs.close()
    }
}
