package de.christianmahnke.lab.iiif.hymir.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

@Configuration
class PluginPropertySpringConfig {
    @Bean
    static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer()
        propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true)
        return propertySourcesPlaceholderConfigurer
    }
}
