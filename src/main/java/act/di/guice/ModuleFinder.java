package act.di.guice;

import act.app.App;
import act.app.AppByteCodeScanner;
import act.di.DependencyInjector;
import act.util.SubTypeFinder;
import com.google.inject.AbstractModule;
import org.osgl._;
import org.osgl.exception.NotAppliedException;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

public class ModuleFinder extends SubTypeFinder {

    public ModuleFinder() {
        super(true, true, AbstractModule.class, new _.F2<App, String, Map<Class<? extends AppByteCodeScanner>, Set<String>>>() {
            @Override
            public Map<Class<? extends AppByteCodeScanner>, Set<String>> apply(App app, String className) throws NotAppliedException, _.Break {
                Class<? extends AbstractModule> c = _.classForName(className, app.classLoader());
                if (Modifier.isAbstract(c.getModifiers())) {
                    return null;
                }
                DependencyInjector injector = app.injector();
                if (null == injector) {
                    injector = new GuiceDependencyInjector(app);
                    logger.info("Guice injector added to app");
                }
                GuiceDependencyInjector guiceInjector = (GuiceDependencyInjector)injector;
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
