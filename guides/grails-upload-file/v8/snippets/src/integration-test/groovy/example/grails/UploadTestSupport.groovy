package example.grails

import org.apache.grails.testing.http.client.MultipartBody

class UploadTestSupport {

    static final byte[] PNG_SIGNATURE = [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A] as byte[]

    static byte[] pngBytes(int size) {
        byte[] bytes = new byte[size]
        System.arraycopy(PNG_SIGNATURE, 0, bytes, 0, PNG_SIGNATURE.length)
        bytes
    }

    static MultipartBody featuredImageBody(Long id, Long version, String filename, String contentType, byte[] bytes) {
        MultipartBody.builder()
                .addPart('id', id.toString())
                .addPart('version', version.toString())
                .addPart('featuredImageFile', filename, contentType, bytes)
                .build()
    }
}
