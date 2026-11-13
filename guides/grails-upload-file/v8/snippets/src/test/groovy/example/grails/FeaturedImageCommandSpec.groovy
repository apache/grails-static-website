package example.grails

import org.springframework.mock.web.MockMultipartFile
import spock.lang.Specification
import spock.lang.Unroll

class FeaturedImageCommandSpec extends Specification {

    @Unroll
    void "featuredImageFile #filename (#contentType) is #description"() {
        given:
        MockMultipartFile file = content == null ? null :
                new MockMultipartFile('featuredImageFile', filename, contentType, content as byte[])
        FeaturedImageCommand cmd = new FeaturedImageCommand(id: 1L, version: 0L, featuredImageFile: file)

        expect:
        cmd.validate() == valid

        where:
        filename      | contentType  | content        || valid
        'photo.png'   | 'image/png'  | [1, 2, 3]      || true
        'photo.jpg'   | 'image/jpeg' | [1, 2, 3]      || true
        'PHOTO.JPEG'  | 'image/jpeg' | [1, 2, 3]      || true
        'photo.png'   | 'image/png'  | []             || false
        'notes.txt'   | 'text/plain' | [1, 2, 3]      || false
        'photo.png'   | 'text/html'  | [1, 2, 3]      || false
        'photo.gif'   | 'image/gif'  | [1, 2, 3]      || false
        'photo'       | 'image/png'  | [1, 2, 3]      || false
        null          | null         | null           || false

        description = valid ? 'valid' : 'rejected'
    }

    void "id and version are required"() {
        given:
        def file = new MockMultipartFile('featuredImageFile', 'photo.png', 'image/png', [1] as byte[])

        when:
        FeaturedImageCommand cmd = new FeaturedImageCommand(featuredImageFile: file)

        then:
        !cmd.validate()
        cmd.errors['id'].code == 'nullable'
        cmd.errors['version'].code == 'nullable'
    }

    @Unroll
    void "sanitizedFilename turns '#original' into '#expected'"() {
        given:
        def file = new MockMultipartFile('featuredImageFile', original, 'image/png', [1] as byte[])

        expect:
        new FeaturedImageCommand(featuredImageFile: file).sanitizedFilename() == expected

        where:
        original               || expected
        'photo.png'            || 'photo.png'
        'my photo (1).png'     || 'my_photo__1_.png'
        '../../etc/evil.png'   || 'evil.png'
        '/var/tmp/evil.png'    || 'evil.png'
        '..\\..\\evil.png'     || '.._.._evil.png'
    }
}
