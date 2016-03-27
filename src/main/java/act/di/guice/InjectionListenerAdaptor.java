package act.di.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class InjectionListenerAdaptor implements Module, TypeListener {
    private GuiceDependencyInjector gdi;

    InjectionListenerAdaptor(GuiceDependencyInjector gdi) {
        this.gdi = gdi;
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
