package example.grails

import builders.dsl.spreadsheet.query.api.SpreadsheetCriteria
import builders.dsl.spreadsheet.query.api.SpreadsheetCriteriaResult
import builders.dsl.spreadsheet.query.poi.PoiSpreadsheetCriteria
import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration
import spock.lang.IgnoreIf
import spock.util.concurrent.PollingConditions

@Integration
class DownloadExcelSpec extends GebSpec {

    @IgnoreIf({ !sys['download.folder'] || sys['geb.env'] != 'chrome' })
    def "books can be downloaded as an excel file"() {
        given:
        PollingConditions conditions = new PollingConditions(timeout: 5)

        when:
        browser.to HomePage

        then:
        browser.at HomePage

        when: 'clicking excel button'
        String expectedPath = System.getProperty('download.folder') + "/" + BookExcelService.EXCEL_FILENAME
        File outputFile = new File(expectedPath)
        browser.page(HomePage).downloadExcel()

        then: 'an excel file is downloaded'
        conditions.eventually { outputFile.exists() }

        when: 'if we search for a row with a particular value (Building Microservices)'
        SpreadsheetCriteria query = PoiSpreadsheetCriteria.FACTORY.forFile(outputFile)
        SpreadsheetCriteriaResult result = query.query {
            sheet(BookExcelService.SHEET_NAME) {
                row {
                    cell {
                        value 'Building Microservices'
                    }
                }
            }
        }

        then: 'a row is found'
        result.cells.size() == 1

        cleanup:
        outputFile?.delete()
    }
}
