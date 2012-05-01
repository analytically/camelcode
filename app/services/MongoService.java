package services;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;

/**
 * Service that shuts down the MongoDB connection.
 *
 * @author Mathias Bogaert
 */
public class MongoService extends AbstractService {
    public static final String HEALTH_CHECK_NAME = "mongo.connection";

    private final Mongo mongo;

    @Inject
    public MongoService(Mongo mongo) {
        this.mongo = mongo;
    }

    @Override
    protected void doStart() {
        HealthChecks.register(new HealthCheck(HEALTH_CHECK_NAME) {
            @Override
            protected Result check() throws Exception {
                try {
                    mongo.getDatabaseNames();
                    return Result.healthy(mongo.debugString());
                } catch (MongoException e) {
                    return Result.unhealthy(e);
                }
            }
        });
    }

    @Override
    protected void doStop() {
        HealthChecks.defaultRegistry().unregister(HEALTH_CHECK_NAME);

        mongo.close();
    }
}
