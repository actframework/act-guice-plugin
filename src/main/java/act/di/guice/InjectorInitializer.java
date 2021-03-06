package act.di.guice;

import act.ActComponent;
import act.app.App;
import act.app.event.AppEventId;
import act.di.DependencyInjector;
import act.event.AppEventListenerBase;
import act.plugin.AppServicePlugin;

import java.util.EventObject;

@ActComponent
public class InjectorInitializer extends AppServicePlugin {
    @Override
    protected void applyTo(final App app) {
        app.eventBus().bind(AppEventId.CLASS_LOADED, new AppEventListenerBase() {
            @Override
            public void on(EventObject event) throws Exception {
                synchronized (InjectorInitializer.class) {
                    DependencyInjector injector = app.injector();
                    if (null == injector) {
                        new GuiceDependencyInjector(app);
                    }
                }
            }
        });
    }
}
