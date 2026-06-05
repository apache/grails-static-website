package demo

import grails.validation.Validateable
import org.springframework.web.multipart.MultipartFile

class FeaturedImageCommand implements Validateable {
    MultipartFile featuredImageFile
    Long id
    Long version

    static constraints = {
        id nullable: false
        version nullable: false
       featuredImageFile  validator: { MultipartFile val, FeaturedImageCommand obj ->
            if ( val == null ) {
                return false
            }
            if ( val.empty ) {
                return false
            }

            ['jpeg', 'jpg', 'png'].any { String extension ->
                 val.originalFilename?.toLowerCase()?.endsWith(extension)
            }
        }
    }
}
