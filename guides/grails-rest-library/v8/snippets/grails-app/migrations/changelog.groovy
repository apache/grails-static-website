databaseChangeLog = {

    changeSet(author: 'guide', id: 'author-table') {
        createTable(tableName: 'author') {
            column(autoIncrement: 'true', name: 'id', type: 'BIGINT') {
                constraints(nullable: 'false', primaryKey: 'true', primaryKeyName: 'authorPK')
            }

            column(name: 'version', type: 'BIGINT') {
                constraints(nullable: 'false')
            }

            column(name: 'name', type: 'VARCHAR(255)') {
                constraints(nullable: 'false')
            }

            column(name: 'biography', type: 'VARCHAR(4000)')
            column(name: 'date_of_birth', type: 'DATE')
        }
    }

    changeSet(author: 'guide', id: 'book-table') {
        createTable(tableName: 'book') {
            column(autoIncrement: 'true', name: 'id', type: 'BIGINT') {
                constraints(nullable: 'false', primaryKey: 'true', primaryKeyName: 'bookPK')
            }

            column(name: 'version', type: 'BIGINT') {
                constraints(nullable: 'false')
            }

            column(name: 'author_id', type: 'BIGINT') {
                constraints(nullable: 'false')
            }

            column(name: 'title', type: 'VARCHAR(255)') {
                constraints(nullable: 'false')
            }

            column(name: 'isbn', type: 'VARCHAR(20)') {
                constraints(nullable: 'false')
            }

            column(name: 'page_count', type: 'INTEGER')
            column(name: 'published_on', type: 'DATE')
        }
    }

    changeSet(author: 'guide', id: 'book-isbn-unique') {
        addUniqueConstraint(columnNames: 'isbn', tableName: 'book', constraintName: 'uk_book_isbn')
    }

    changeSet(author: 'guide', id: 'book-author-fk') {
        addForeignKeyConstraint(baseColumnNames: 'author_id', baseTableName: 'book', constraintName: 'fk_book_author', referencedColumnNames: 'id', referencedTableName: 'author')
    }

    changeSet(author: 'guide', id: 'book-author-index') {
        createIndex(indexName: 'idx_book_author', tableName: 'book') {
            column(name: 'author_id')
        }
    }
}
