package act.di.guice;

import act.ActComponent;
import act.app.ActionContext;
import act.app.App;
import act.app.CliContext;
import act.app.event.AppEventId;
import act.app.event.AppEventListener;
import act.app.util.AppCrypto;
import act.conf.AppConfig;
import act.di.DependencyInjector;
import act.di.DependencyInjectorBase;
import act.di.DiBinder;
import act.event.AppEventListenerBase;
import act.event.EventBus;
import act.mail.MailerContext;
import act.util.ActContext;
import com.google.inject.*;
import com.google.inject.spi.InjectionListener;
import org.osgl.$;
import org.osgl.util.C;
import org.osgl.util.E;

import java.util.EventObject;
import java.util.List;
import java.util.Map;

/**
 * Implement {@link DependencyInjector}
 */
@ActComponent
public class GuiceDependencyInjector extends DependencyInjectorBase<GuiceDependencyInjector> {

    volatile Injector injector;
    List<Module> modules = C.newList();
    private Map<Class, DiBinder> binders = C.newMap();
    // store binder mapping after the injector has been created
    private Map<Class, DiBinder> additionalBinders = C.newMap();
    private Map<Class, List<Class<? extends InjectionListener>>> injectionListenerClasses = C.newMap();

    public GuiceDependencyInjector(App app) {
        super(app);
        app.eventBus().bind(AppEventId.SINGLETON_PROVISIONED, new AppEventListenerBase() {
            @Override
            public void on(EventObject event) throws Exception {
                processAdditionalBinders();
            }
        });
    }

    @Override
    protected void releaseResources() {
        modules.clear();
        injector = null;
        binders.clear();
        additionalBinders.clear();
        injectionListenerClasses.clear();
    }

    public void addModule(Module module) {
        E.NPE(module);
        modules.add(module);
    }

    @Override
    public <T> T create(Class<T> clazz) {
        Injector injector = injector();
        if (null == injector) {
            return $.newInstance(clazz);
        } else {
            return injector.getInstance(clazz);
        }
    }

    synchronized <T> void registerInjectionListenerClass(Class<T> type, Class<InjectionListener<T>> listenerClass) {
        List<Class<? extends InjectionListener>> l = injectionListenerClasses.get(type);
        if (null == l) {
            l = C.newList();
            injectionListenerClasses.put(type, l);
        }
        l.add(listenerClass);
    }

    synchronized List<Class<? extends InjectionListener>> injectionListeners(Class<?> keyClass) {
        return injectionListenerClasses.get(keyClass);
    }

    @Override
    public DependencyInjector<GuiceDependencyInjector> createContextAwareInjector(ActContext appContext) {
        // Now appContext local is always stored appContext.saveLocal();
        return this;
    }

    synchronized void registerDiBinder(DiBinder binder) {
        if (null == injector) {
            binders.put(binder.targetClass(), binder);
        } else {
            additionalBinders.put(binder.targetClass(), binder);
        }
    }

    private synchronized void processAdditionalBinders() {
        if (additionalBinders.isEmpty()) {
            return;
        }
        AbstractModule module = new AbstractModule() {
            @Override
            protected void configure() {
                for (final Class key: additionalBinders.keySet()) {
                    bind(key).toProvider(new Provider() {
                        @Override
                        public Object get() {
                            return additionalBinders.get(key).resolve(app());
                        }
                    });
                }
            }
        };
        injector = injector.createChildInjector(module);
    }

    private Injector injector() {
        if (null == injector) {
            synchronized (this) {
                if (null == injector) {
                    final Module daoHelper = new DaoInjectionHelper(GuiceDependencyInjector.this);
                    AbstractModule tempModule = new AbstractModule() {
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
                            bind(CliContext.class).toProvider(new Provider<CliContext>() {
                                @Override
                                public CliContext get() {
                                    return CliContext.current();
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
                    };
                    modules.add(tempModule);
                    modules.add(new AbstractModule() {
                        @Override
                        protected void configure() {
                            install(daoHelper);
                        }
                    });
                    injector = Guice.createInjector(modules);
                }
            }
        }
        return injector;
    }
}
