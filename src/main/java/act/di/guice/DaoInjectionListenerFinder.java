package act.di.guice;

import act.app.App;
import act.app.event.AppEventId;
import act.db.Dao;
import act.util.SubTypeFinder2;
import com.google.inject.spi.InjectionListener;
import org.osgl.$;

public class DaoInjectionListenerFinder extends SubTypeFinder2<DaoInjectionListener> {

    public DaoInjectionListenerFinder() {
        super(DaoInjectionListener.class);
    }

    @Override
    protected void found(final Class<DaoInjectionListener> aClass, final App app) {
        app.jobManager().on(AppEventId.DEPENDENCY_INJECTOR_LOADED, new Runnable() {
            @Override
            public void run() {
                GuiceDependencyInjector gdi = $.cast(app.injector());
                gdi.registerInjectionListenerClass(Dao.class, (Class)aClass);
            }
        });
    }
}
