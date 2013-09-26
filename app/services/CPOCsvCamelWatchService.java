package services;

import akka.actor.ActorRef;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import models.csv.CodePointOpenCsvEntry;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;

import java.util.Map;

/**
 * Service that watches the 'cpo.watch' directory for CodePoint-Open CSV files using Apache Camel.
 *
 * @author Mathias Bogaert
 */
public class CPOCsvCamelWatchService extends AbstractService {
    private final CamelContext camelContext;

    @Inject
    public CPOCsvCamelWatchService(@Named("ProcessCPOCsvEntry") final ActorRef actorRef,
                                   CamelContext camelContext,
                                   final @Named("cpo.from") String cpoFrom) throws Exception {
        this.camelContext = camelContext;

        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(cpoFrom)
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
                                        actorRef.tell(entry, ActorRef.noSender());
                                    }
                                } else {
                                    throw new RuntimeException("something went wrong; message body is no map!");
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void doStart() {
        try {
            camelContext.start();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected void doStop() {
        try {
            camelContext.stop();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
