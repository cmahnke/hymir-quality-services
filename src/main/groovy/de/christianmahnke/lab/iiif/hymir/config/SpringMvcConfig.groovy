package de.christianmahnke.lab.iiif.hymir.config


import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class SpringMvcConfig implements WebMvcConfigurer {


/*
        @Override
        public void configurePathMatch(PathMatchConfigurer configurer) {
            // Needed for escaped slashes in identifiers
            UrlPathHelper urlPathHelper = new UrlPathHelper();
            urlPathHelper.setUrlDecode(false);
            configurer.setUrlPathHelper(urlPathHelper);
        }
    */
}
