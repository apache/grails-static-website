package demo

import groovy.transform.CompileStatic
import org.apache.commons.lang.RandomStringUtils

@CompileStatic
class SerialNumberGeneratorService {

    String generate(String bookTitle) {
        String randomString = RandomStringUtils.random(8, true, false)
        String titleChars = "${bookTitle}".take(4) //<1>
        "${titleChars}-${randomString}".toUpperCase()
    }
}