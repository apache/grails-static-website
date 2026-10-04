package demo

import groovy.transform.CompileStatic

@CompileStatic
class SerialNumberGeneratorService {

    private static final String LETTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'
    private final Random random = new Random()

    String generate(String bookTitle) {
        StringBuilder randomString = new StringBuilder(8)
        for (int i = 0; i < 8; i++) {
            randomString.append(LETTERS.charAt(random.nextInt(LETTERS.length())))
        }
        String titleChars = "${bookTitle}".take(4) //<1>
        "${titleChars}-${randomString}".toUpperCase()
    }
}
