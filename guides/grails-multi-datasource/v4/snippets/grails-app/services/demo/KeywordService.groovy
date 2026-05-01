package demo

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.ReadOnly
import groovy.transform.CompileStatic

@CompileStatic
class KeywordService {

    @ReadOnly('books')
    List<Keyword> findAllBooksKeywords() {
        booksQuery().list()
    }

    @ReadOnly
    List<Keyword> findAllDefaultDataSourceKeywords() {
        defaultDataSourceQuery().list()
    }

    private DetachedCriteria<Keyword> booksQuery() {
        Keyword.where {}.withConnection('books') // <1>
    }

    private DetachedCriteria<Keyword> defaultDataSourceQuery() {
        Keyword.where {}
    }
}