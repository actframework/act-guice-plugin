package act.di.guice;

import act.app.App;
import act.di.DependencyInjector;
import act.util.SubTypeFinder;
import com.google.inject.AbstractModule;
import org.osgl._;
import org.osgl.exception.NotAppliedException;

public class GuicePlugin extends SubTypeFinder {

    public GuicePlugin() {
        super(AbstractModule.class, new _.F2<App, String, Void>() {
            @Override
            public Void apply(App app, String className) throws NotAppliedException, _.Break {
                DependencyInjector injector = app.injector();
                if (null == injector) {
                    injector = new GuiceDependencyInjector(app);
                    logger.info("Guice injector added to app");
                }
                GuiceDependencyInjector guiceInjector = (GuiceDependencyInjector)injector;
                Class<? extends AbstractModule> c = _.classForName(className, app.classLoader());
                AbstractModule module = _.newInstance(c);
                guiceInjector.addModule(module);
                logger.info("guice module %s added to the injector", className);
                return null;
            }
        });
    }

    @Override
    public boolean load() {
        return true;
    }
}
