package CarvanaTracker.config;

import CarvanaTracker.Model.DailyDataPoint;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import jakarta.persistence.metamodel.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
//@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class DataRestConfig implements RepositoryRestConfigurer {

    @Value("${allowed.origins}")
    private String[] allowedOrgins;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public DataRestConfig(){

    }
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {

        HttpMethod[] invalidActions = {HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PATCH};
        disableHttpMethods(DailyDataPoint.class, config, invalidActions);
        cors.addMapping(config.getBasePath() + "/**").allowedOrigins(allowedOrgins);

    }

    private static void disableHttpMethods(Class classType, RepositoryRestConfiguration config, HttpMethod[] invalidActions) {
        config.getExposureConfiguration().forDomainType(classType)

                .withItemExposure(((metdata, httpMethods) -> httpMethods.disable(invalidActions)))
                .withCollectionExposure((metdata, httpMethods) -> httpMethods.disable(invalidActions));
    }

}
