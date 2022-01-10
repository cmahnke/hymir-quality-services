package de.christianmahnke.lab.iiif.hymir.config

import de.digitalcollections.commons.springmvc.interceptors.CurrentUrlAsModelAttributeHandlerInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.util.UrlPathHelper

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
