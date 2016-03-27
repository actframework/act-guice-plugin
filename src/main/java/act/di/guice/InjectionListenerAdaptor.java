package act.di.guice;

import act.app.App;
import act.db.Dao;
import act.db.di.DaoInjectionListener;
import act.db.di.DaoInjectionListenerCache;
import act.di.DependencyInjectionListener;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.osgl.$;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class InjectionListenerAdaptor implements Module, TypeListener {
    private GuiceDependencyInjector gdi;

    private DaoInjectionListenerCache cache;

    InjectionListenerAdaptor(GuiceDependencyInjector gdi) {
        this.gdi = gdi;
        this.cache = App.instance().service(DaoInjectionListenerCache.class);
        if (null == this.cache) {
            this.cache = new DaoInjectionListenerCache(App.instance());
        }
    }

    @Override
    public void configure(Binder binder) {
        binder.bindListener(Matchers.any(), this);
    }

    @Override
    public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> encounter) {
        final Class rawType = typeLiteral.getRawType();
        if (!gdi.interestedIn(rawType)) {
            return;
        }
        Type[] ta = null;
        Type type = typeLiteral.getType();
        if (type instanceof ParameterizedType) {
            ta = ((ParameterizedType) type).getActualTypeArguments();
        }
        final Type[] typeParameters = ta;
        encounter.register(new InjectionListener<I>() {
            @Override
            public void afterInjection(I injectee) {
                gdi.fireInjectedEvent(injectee, typeParameters);
            }
        });
    }

}
