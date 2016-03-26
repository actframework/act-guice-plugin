package act.di.guice;

import act.app.App;
import act.db.Dao;
import act.db.di.DaoInjectionListener;
import act.db.di.DaoInjectionListenerCache;
import act.di.DiListener;
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

public class DaoInjectionHelper implements Module, TypeListener {

    private GuiceDependencyInjector gdi;

    private DaoInjectionListenerCache cache;

    DaoInjectionHelper(GuiceDependencyInjector gdi) {
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
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        Class rawType = type.getRawType();
        if (Dao.class.isAssignableFrom(rawType)) {
            App app = App.instance();
            Type ttype = type.getType();
            if (ttype instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType)ttype;
                Type[] ta = ptype.getActualTypeArguments();
                Type componentType = ta[0];
                if (ta.length > 1) {
                    componentType = ta[1];
                }
                Class componentClass = $.cast(componentType);
                $.T2<Class, Class> key = $.T2(rawType, componentClass);
                DaoInjectionListener listener = cache.get(key);
                if (null == listener) {
                    List<Class<? extends DiListener>> lcs = gdi.injectionListeners(Dao.class);
                    if (null != lcs) {
                        for (Class<? extends DiListener> c: lcs) {
                            DiListener l = app.newInstance(c);
                            if (l instanceof DaoInjectionListener) {
                                DaoInjectionListener dil = $.cast(l);
                                if (dil.targetDaoType().isAssignableFrom(rawType)) {
                                    dil.modelType(componentClass);
                                    cache.put(key, dil);
                                    final DaoInjectionListener daoInjectionListener = dil;
                                    InjectionListener<I> listener1 = new InjectionListener<I>() {
                                        @Override
                                        public void afterInjection(I injectee) {
                                            daoInjectionListener.afterInjection((Dao)injectee);
                                        }
                                    };
                                    encounter.register(listener1);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
