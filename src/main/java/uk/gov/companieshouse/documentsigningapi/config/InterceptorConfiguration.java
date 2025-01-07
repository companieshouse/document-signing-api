package uk.gov.companieshouse.documentsigningapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.documentsigningapi.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.documentsigningapi.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.documentsigningapi.interceptor.UserAuthorisationInterceptor;

@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer {

    private final UserAuthorisationInterceptor userAuthenticationInterceptor;
    private final UserAuthenticationInterceptor userAuthorisationInterceptor;
    private final LoggingInterceptor loggingInterceptor;

    @Autowired
    public InterceptorConfiguration(UserAuthorisationInterceptor userAuthenticationInterceptor,
            UserAuthenticationInterceptor userAuthorisationInterceptor, LoggingInterceptor loggingInterceptor) {
        this.userAuthenticationInterceptor = userAuthenticationInterceptor;
        this.userAuthorisationInterceptor = userAuthorisationInterceptor;
        this.loggingInterceptor = loggingInterceptor;

    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(userAuthenticationInterceptor);
        registry.addInterceptor(userAuthorisationInterceptor);
        registry.addInterceptor(loggingInterceptor);
    }
}
