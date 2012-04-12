import actors.ProcessCPOCsvEntry;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;
import com.google.code.morphia.logging.MorphiaLoggerFactory;
import com.google.code.morphia.logging.slf4j.SLF4JLogrImplFactory;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.yammer.metrics.reporting.ConsoleReporter;
import models.csv.CodePointOpenCsvEntry;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Akka;
import play.mvc.Controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Mathias Bogaert
 */
public class Global extends GlobalSettings {
    private final List<Module> modules = Lists.newArrayList();

    private Injector injector;
    private CamelContext camelContext;

    static {
        MorphiaLoggerFactory.reset();
        MorphiaLoggerFactory.registerLogger(SLF4JLogrImplFactory.class);
    }

    @Override
    public void beforeStart(final Application application) {
        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .addUrls(ClasspathHelper.forPackage("modules", application.classloader()))
                .addScanners(
                        new SubTypesScanner(),
                        new TypeAnnotationsScanner()
                ));

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

                for (Class<? extends Controller> controller : reflections.getSubTypesOf(Controller.class)) {
                    requestStaticInjection(controller);
                }
            }
        });
    }

    @Override
    public void onStart(Application app) {
        Logger.info("Creating injector with " + modules.size() + " modules.");
        injector = Guice.createInjector(Stage.PRODUCTION, modules);

        ConsoleReporter.enable(1, TimeUnit.MINUTES);

        final ActorRef processActorRef = Akka.system().actorOf(new Props(new UntypedActorFactory() {
            public ProcessCPOCsvEntry create() {
                return injector.getInstance(ProcessCPOCsvEntry.class);
            }
        }), "process-codepoint-open-csv-entry");

        camelContext = injector.getInstance(CamelContext.class);
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {

                    from("file://codepointopen/?move=done")
                            .unmarshal().bindy(BindyType.Csv, "models.csv")
                            .split(body())
                            .process(new Processor() {
                                @SuppressWarnings("unchecked")
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    Object body = exchange.getIn().getBody();

                                    if (body instanceof Map) {
                                        Map<String, CodePointOpenCsvEntry> csvEntryMap = (Map<String, CodePointOpenCsvEntry>) body;

                                        for (CodePointOpenCsvEntry entry : csvEntryMap.values()) {
                                            processActorRef.tell(entry);
                                        }
                                    } else {
                                        throw new RuntimeException("something went wrong; message body is no map!");
                                    }
                                }
                            });
                }
            });
        } catch (Exception e) {
            Logger.error(e.getMessage(), e);
        }

        try {
            camelContext.start();
        } catch (Exception e) {
            Logger.error("Exception starting Apache Camel context: " + e.getMessage(), e);

            throw Throwables.propagate(e);
        }
    }

    @Override
    public void onStop(Application app) {
        if (camelContext != null) {
            try {
                camelContext.stop();
            } catch (Exception e) {
                Logger.error("Exception stopping Apache Camel context: " + e.getMessage(), e);

                throw Throwables.propagate(e);
            }
        }
    }
}