package demo

import gorm.logical.delete.LogicalDelete
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class Book implements LogicalDelete<Book> { // <1>
    String image
    String title
    String author
    String about
    String href
    static mapping = {
        about type: 'text'
    }
}