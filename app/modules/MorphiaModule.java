package modules;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.mapping.DefaultCreator;
import com.google.code.morphia.mapping.MappedField;
import com.google.code.morphia.mapping.Mapper;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import models.Model;
import play.Application;
import play.Logger;

import java.net.UnknownHostException;

/**
 * @author Mathias Bogaert
 */
public class MorphiaModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(Application.class);
        requestStaticInjection(Model.class);
    }

    @Provides
    Datastore create(final Application application, final Injector injector) {
        Morphia morphia = new Morphia();
        morphia.getMapper().getOptions().objectFactory = new DefaultCreator() {
            @Override
            protected ClassLoader getClassLoaderForClass(String clazz, DBObject object) {
                return application.classloader();
            }
        };
        morphia.mapPackage("models");

        try {
            final Mongo mongo = new Mongo(new MongoURI(application.configuration().getString("mongodb.uri")));

            Datastore datastore = morphia.createDatastore(mongo, application.configuration().getString("mongodb.db"));
            datastore.ensureIndexes();

            Logger.info("Connected to MongoDB [" + mongo.debugString() + "] database [" + datastore.getDB().getName() + "]");

            return datastore;
        } catch (UnknownHostException e) {
            addError(e);
            return null;
        }
    }
}
