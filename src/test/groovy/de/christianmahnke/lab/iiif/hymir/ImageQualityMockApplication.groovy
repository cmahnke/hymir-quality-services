package de.christianmahnke.lab.iiif.hymir

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ConfigurationPropertiesScan
@SpringBootApplication
@TypeChecked
@Slf4j
@CompileStatic
@ComponentScan(basePackages = ['de.christianmahnke.lab.iiif.hymir', 'de.digitalcollections.iiif.hymir.image.business'])
class ImageQualityMockApplication {
    static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(ImageQualityMockApplication.class)
        builder.run(args)
    }

    @Configuration
    static class MockConfig {

    }
}
