import actors.ProcessCPOCsvEntry;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import com.google.code.morphia.logging.MorphiaLoggerFactory;
import com.google.code.morphia.logging.slf4j.SLF4JLogrImplFactory;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;
import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.yammer.metrics.reporting.ConsoleReporter;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.libs.Akka;
import play.mvc.Controller;
import utils.MoreMatchers;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author Mathias Bogaert
 */
public class Global extends GlobalSettings {
    static {
        MorphiaLoggerFactory.reset();
        MorphiaLoggerFactory.registerLogger(SLF4JLogrImplFactory.class);
    }

    static final class ActorProvider<T extends UntypedActor> implements Provider<ActorRef> {
        private final TypeLiteral<T> uta;
        private final Injector injector;

        @Inject
        public ActorProvider(TypeLiteral<T> uta, Injector injector) {
            this.uta = uta;
            this.injector = injector;
        }

        @Override
        public ActorRef get() {
            return Akka.system().actorOf(new Props(new UntypedActorFactory() {
                public T create() {
                    return injector.getInstance(Key.get(uta));
                }
            }));
        }
    }

    private Injector injector;
    private final List<Module> modules = Lists.newArrayList();

    private final List<OnStartListener> onStartListeners = new CopyOnWriteArrayList<OnStartListener>();
    private final List<OnStopListener> onStopListeners = new CopyOnWriteArrayList<OnStopListener>();

    @Override
    public void beforeStart(final Application application) {
        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder.Exclude(FilterBuilder.prefix("com.google")))
                .addUrls(ClasspathHelper.forClassLoader(application.classloader()))
                .addScanners(
                        new SubTypesScanner()
                ));

        // automatic Guice module detection
        Set<Class<? extends AbstractModule>> guiceModules = reflections.getSubTypesOf(AbstractModule.class);
        for (Class<? extends Module> moduleClass : guiceModules) {
            try {
                if (!moduleClass.isAnonymousClass()) {
                    modules.add(moduleClass.newInstance());
                }
            } catch (InstantiationException e) {
                throw Throwables.propagate(e);
            } catch (IllegalAccessException e) {
                throw Throwables.propagate(e);
            }
        }

        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Application.class).toInstance(application);
                bind(Reflections.class).toInstance(reflections);

                Names.bindProperties(this.binder(), fromKeys(application.configuration().keys(), new Function<String, String>() {
                    @Override
                    public String apply(String key) {
                        // remove after https://play.lighthouseapp.com/projects/82401/tickets/372 is fixed
                        if (key.contains("akka")) return null;

                        return application.configuration().getString(key);
                    }
                }));

                for (Class<? extends Controller> controllerClass : reflections.getSubTypesOf(Controller.class)) {
                    Logger.info("Static injection for " + controllerClass);

                    requestStaticInjection(controllerClass);
                }

                // bind all services
                Multibinder<Service> serviceBinder = Multibinder.newSetBinder(binder(), Service.class);
                for (Class<? extends Service> serviceImplClass : reflections.getSubTypesOf(AbstractService.class)) {
                    serviceBinder.addBinding().to(serviceImplClass).asEagerSingleton();
                }
                for (Class<? extends Service> serviceImplClass : reflections.getSubTypesOf(AbstractIdleService.class)) {
                    serviceBinder.addBinding().to(serviceImplClass).asEagerSingleton();
                }
                for (Class<? extends Service> serviceImplClass : reflections.getSubTypesOf(AbstractExecutionThreadService.class)) {
                    serviceBinder.addBinding().to(serviceImplClass).asEagerSingleton();
                }

                // bind actor - todo use reflections for this
                bind(ActorRef.class).annotatedWith(Names.named("ProcessCPOCsvEntry"))
                        .toProvider(new TypeLiteral<ActorProvider<ProcessCPOCsvEntry>>() {
                        });

                // start/stop services after injection and on shutdown of the Play app
                bindListener(MoreMatchers.subclassesOf(Service.class), new TypeListener() {
                    @Override
                    public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
                        typeEncounter.register(new InjectionListener<I>() {
                            @Override
                            public void afterInjection(final I i) {
                                onStartListeners.add(new OnStartListener() {
                                    @Override
                                    public void onApplicationStart(Application application, Injector injector) {
                                        Logger.info(String.format("Starting %s", i.toString()));
                                        ((Service) i).start();

                                        onStopListeners.add(new OnStopListener() {
                                            @Override
                                            public void onApplicationStop(Application application) {
                                                Logger.info(String.format("Stopping %s", i.toString()));
                                                ((Service) i).stop();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public <A> A getControllerInstance(Class<A> controllerClass) throws Exception {
        return injector.getInstance(controllerClass);
    }

    @Override
    public void onStart(Application app) {
        Logger.info("Creating injector with " + modules.size() + " modules.");
        injector = Guice.createInjector(Stage.PRODUCTION, modules);

        for (OnStartListener listener : onStartListeners) {
            listener.onApplicationStart(app, injector);
        }
    }

    @Override
    public void onStop(Application app) {
        for (OnStopListener listener : onStopListeners) {
            listener.onApplicationStop(app);
        }
    }

    /**
     * Listener that will get invoked after the application is started.
     */
    static interface OnStartListener {
        void onApplicationStart(Application application, Injector injector);
    }

    /**
     * Listener that will get invoked before the application is stopped.
     */
    static interface OnStopListener {
        void onApplicationStop(Application application);
    }

    private static <K, V> ImmutableMap<K, V> fromKeys(Iterable<K> keys, Function<? super K, V> valueFunction) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (K key : keys) {
            V value = valueFunction.apply(key);
            if (value != null) {
                builder.put(key, value);
            }
        }
        return builder.build();
    }
}