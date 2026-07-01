package example.grails

import builders.dsl.spreadsheet.builder.poi.PoiSpreadsheetBuilder
import groovy.transform.CompileStatic

@CompileStatic
class BookExcelService {
    public static final String SHEET_NAME = "Books"
    public static final String HEADER_ISBN = "Isbn"
    public static final String HEADER_NAME = "Name"
    public static final String EXCEL_FILE_SUFIX = ".xlsx"
    public static final String EXCEL_FILE_PREFIX = "books"
    public static final String EXCEL_FILENAME = EXCEL_FILE_PREFIX + EXCEL_FILE_SUFIX

    void exportExcelFromBooks(OutputStream outs, List<Book> bookList) {
        File file = File.createTempFile(EXCEL_FILE_PREFIX, EXCEL_FILE_SUFIX)
        PoiSpreadsheetBuilder.create(outs).build {
            apply BookExcelStylesheet
            sheet(SHEET_NAME) { s ->
                row {
                    [HEADER_ISBN, HEADER_NAME].each { header ->
                        cell {
                            value header
                            style BookExcelStylesheet.STYLE_HEADER
                        }
                    }
                }
                bookList.each { book ->
                    row {
                        cell(book.isbn)
                        cell(book.name)
                    }
                }
            }
        }
        file
    }
}