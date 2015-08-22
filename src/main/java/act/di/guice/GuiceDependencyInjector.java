package act.di.guice;

import act.app.ActionContext;
import act.app.App;
import act.app.event.AppEventId;
import act.app.util.AppCrypto;
import act.conf.AppConfig;
import act.di.DependencyInjector;
import act.di.DependencyInjectorBase;
import act.di.DiBinder;
import act.event.EventBus;
import act.mail.MailerContext;
import com.google.inject.*;
import org.osgl._;
import org.osgl.util.C;
import org.osgl.util.E;

import java.util.List;
import java.util.Map;

/**
 * Implement {@link DependencyInjector}
 */
public class GuiceDependencyInjector extends DependencyInjectorBase<GuiceDependencyInjector> {

    volatile Injector injector;
    List<Module> modules = C.newList();
    private Map<Class, DiBinder> binders = C.newMap();

    public GuiceDependencyInjector(App app) {
        super(app);
        app.eventBus().emit(AppEventId.DEPENDENCY_INJECTOR_LOADED);
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
    public DependencyInjector<GuiceDependencyInjector> createContextAwareInjector(ActionContext appContext) {
        // Now appContext local is always stored appContext.saveLocal();
        return this;
    }

    void registerDiBinder(DiBinder binder) {
        binders.put(binder.targetClass(), binder);
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
                        bind(AppCrypto.class).toProvider(new Provider<AppCrypto>() {
                            @Override
                            public AppCrypto get() {
                                return app().crypto();
                            }
                        });
                        bind(ActionContext.class).toProvider(new Provider<ActionContext>() {
                            @Override
                            public ActionContext get() {
                                return ActionContext.current();
                            }
                        });
                        bind(MailerContext.class).toProvider(new Provider<MailerContext>() {
                            @Override
                            public MailerContext get() {
                                return MailerContext.current();
                            }
                        });
                        bind(EventBus.class).toProvider(new Provider<EventBus>() {
                            @Override
                            public EventBus get() {
                                return app().eventBus();
                            }
                        });
                        for (final Class key: binders.keySet()) {
                            bind(key).toProvider(new Provider() {
                                @Override
                                public Object get() {
                                    return binders.get(key).resolve(app());
                                }
                            });
                        }
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
