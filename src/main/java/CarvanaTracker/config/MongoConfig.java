package CarvanaTracker.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Collection;
import java.util.Collections;

@Configuration
@EnableMongoRepositories(basePackages = "CarvanaTracker.Repository")
@EnableScheduling
@EnableAsync
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${CarvanaTracker.Configuration.MongoUrl}")
    private String mongoURL;
    @Override
    protected String getDatabaseName() {
        return "carvana";
    }

    @Override
    public MongoClient mongoClient() {
        MongoClient mongoClient = MongoClients.create(
                mongoURL);
        MongoDatabase database = mongoClient.getDatabase("admin");

        return mongoClient;
    }

    @Override
    public Collection getMappingBasePackages() {
        return Collections.singleton("carvana");
    }
}