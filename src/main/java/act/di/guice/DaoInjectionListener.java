package act.di.guice;

import act.db.Dao;
import com.google.inject.spi.InjectionListener;

import java.lang.reflect.Type;

public interface DaoInjectionListener extends InjectionListener<Dao> {

    Class<? extends Dao> targetDaoType();

    /**
     * This allows the implementation to fetch the db service ID from DB annotation
     * @param modelType The component class
     */
    void modelType(Class<?> modelType);
}
