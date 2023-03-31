package uk.gov.companieshouse.documentsigningapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.documentsigningapi.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.documentsigningapi.interceptor.UserAuthorisationInterceptor;

@Configuration
public class InterceptorConfiguration  implements WebMvcConfigurer {
    @Autowired
    UserAuthorisationInterceptor userAuthenticationInterceptor;
    @Autowired
    UserAuthenticationInterceptor userAuthorisationInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(userAuthenticationInterceptor);
        registry.addInterceptor(userAuthorisationInterceptor);
    }
}
