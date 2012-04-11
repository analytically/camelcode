package modules;

import com.google.inject.AbstractModule;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * @author Mathias Bogaert
 */
public class CamelcodeModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CamelContext.class).to(DefaultCamelContext.class).asEagerSingleton();
    }
}