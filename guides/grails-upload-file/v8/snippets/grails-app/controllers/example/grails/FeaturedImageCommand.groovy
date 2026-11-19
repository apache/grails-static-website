package example.grails

import grails.validation.Validateable
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile

class FeaturedImageCommand implements Validateable {

    static final List<String> ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png']
    static final List<String> ALLOWED_CONTENT_TYPES = ['image/jpeg', 'image/png']
    static final int MAX_FILENAME_LENGTH = 64

    MultipartFile featuredImageFile
    Long id
    Long version

    static constraints = {
        id nullable: false
        version nullable: false
        featuredImageFile validator: { MultipartFile val, FeaturedImageCommand obj ->
            if (val == null || val.empty) { // <1>
                return false
            }
            String extension = StringUtils.getFilenameExtension(val.originalFilename)?.toLowerCase()
            extension in ALLOWED_EXTENSIONS && val.contentType in ALLOWED_CONTENT_TYPES // <2>
        }
    }

    String sanitizedFilename() {
        String filename = StringUtils.getFilename(featuredImageFile.originalFilename) // <3>
        String sanitized = filename.replaceAll(/[^A-Za-z0-9._-]/, '_') // <4>
        if (sanitized.length() <= MAX_FILENAME_LENGTH) {
            return sanitized
        }
        String extension = StringUtils.getFilenameExtension(sanitized)
        String suffix = extension ? ".${extension}" : ''
        sanitized.take(MAX_FILENAME_LENGTH - suffix.length()) + suffix // <5>
    }
}
