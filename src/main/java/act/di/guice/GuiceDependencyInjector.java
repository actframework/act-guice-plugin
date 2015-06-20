package act.di.guice;

import act.app.App;
import act.app.AppContext;
import act.conf.AppConfig;
import act.di.DependencyInjectorBase;
import com.google.inject.*;
import org.osgl._;
import act.di.DependencyInjector;
import org.osgl.util.C;
import org.osgl.util.E;

import java.util.List;

/**
 * Implement {@link DependencyInjector}
 */
public class GuiceDependencyInjector extends DependencyInjectorBase<GuiceDependencyInjector> {

    volatile Injector injector;
    List<Module> modules = C.newList();

    public GuiceDependencyInjector(App app) {
        super(app);
    }

    @Override
    protected void releaseResources() {
        modules.clear();
        injector = null;
    }

    public void addModule(Module module) {
        E.NPE(module);
        modules.add(module);
    }

    @Override
    public <T> T create(Class<T> clazz) {
        Injector injector = injector();
        if (null == injector) {
            return _.newInstance(clazz);
        } else {
            return injector.getInstance(clazz);
        }
    }

    @Override
    public DependencyInjector<GuiceDependencyInjector> createContextAwareInjector(AppContext appContext) {
        // Now appContext local is always stored appContext.saveLocal();
        return this;
    }

    private Injector injector() {
        if (null == injector) {
            synchronized (this) {
                modules.add(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(App.class).toProvider(new Provider<App>() {
                            @Override
                            public App get() {
                                return app();
                            }
                        });
                        bind(AppConfig.class).toProvider(new Provider<AppConfig>() {
                            @Override
                            public AppConfig get() {
                                return app().config();
                            }
                        });
                        bind(AppContext.class).toProvider(new Provider<AppContext>() {
                            @Override
                            public AppContext get() {
                                return AppContext.current();
                            }
                        });
                    }
                });
                if (null == injector) {
                    injector = Guice.createInjector(modules);
                }
            }
        }
        return injector;
    }
}
