package example.grails

import groovy.transform.CompileStatic
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

import java.nio.file.Paths

@CompileStatic
class UploadsWebMvcConfigurer implements WebMvcConfigurer {

    final String cdnFolder

    UploadsWebMvcConfigurer(String cdnFolder) {
        this.cdnFolder = cdnFolder
    }

    @Override
    void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(cdnFolder).toAbsolutePath().normalize().toUri().toString() // <1>
        registry.addResourceHandler('/uploads/**') // <2>
                .addResourceLocations(location)
    }
}
