package act.di.guice;

import act.app.App;
import act.di.DependencyInjector;
import act.plugin.AppServicePlugin;

public class InjectorInitializer extends AppServicePlugin {
    @Override
    protected void applyTo(final App app) {
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
