package act.di.guice;

import act.ActComponent;
import act.app.App;
import act.di.DependencyInjector;
import act.util.SubTypeFinder2;
import com.google.inject.AbstractModule;
import org.osgl.$;

@ActComponent
public class ModuleFinder extends SubTypeFinder2<AbstractModule> {

    public ModuleFinder() {
        super(AbstractModule.class);
    }

    @Override
    protected void found(final Class<AbstractModule> target, final App app) {
        DependencyInjector injector = app.injector();
        if (null == injector) {
            injector = new GuiceDependencyInjector(app);
            logger.info("Guice injector added to app");
        }
        GuiceDependencyInjector guiceInjector = (GuiceDependencyInjector) injector;
        AbstractModule module = $.newInstance(target);
        guiceInjector.addModule(module);
        logger.info("guice module %s added to the injector", target.getName());
    }
}
