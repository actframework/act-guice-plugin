package act.di.guice;

import act.app.App;
import act.di.DependencyInjector;
import act.di.DiBinder;
import act.event.ActEventListener;
import act.event.ActEventListenerBase;
import act.plugin.AppServicePlugin;

import java.util.EventObject;

public class InjectorInitializer extends AppServicePlugin {
    @Override
    protected void applyTo(final App app) {
        app.eventBus().bind(DiBinder.class, new ActEventListenerBase<DiBinder>() {
            @Override
            public void on(DiBinder event) throws Exception {
                GuiceDependencyInjector injector = app.injector();
                if (null == injector) {
                    injector = new GuiceDependencyInjector(app);
                }
                injector.registerDiBinder(event);
            }
        });
        app.jobManager().beforeAppStart(new Runnable() {
            @Override
            public void run() {
                DependencyInjector injector = app.injector();
                if (null == injector) {
                    new GuiceDependencyInjector(app);
                }
            }
        });
    }
}
