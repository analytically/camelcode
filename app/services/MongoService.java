package services;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.mongodb.Mongo;

/**
 * Service that shuts down the MongoDB connection.
 *
 * @author Mathias Bogaert
 */
public class MongoService extends AbstractService {
    private final Mongo mongo;

    @Inject
    public MongoService(Mongo mongo) {
        this.mongo = mongo;
    }

    @Override
    protected void doStart() {
    }

    @Override
    protected void doStop() {
        mongo.close();
    }
}
