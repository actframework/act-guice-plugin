package act.di.guice;

import act.app.DbServiceManager;
import act.db.DB;

public abstract class DaoInjectionListenerBase implements DaoInjectionListener {

    private Class<?> modelType;
    private String dbSvcId = DbServiceManager.DEFAULT;

    @Override
    public void modelType(Class<?> modelType) {
        this.modelType = modelType;
        DB db = modelType.getDeclaredAnnotation(DB.class);
        if (null != db) {
            dbSvcId = db.value();
        }
    }

    protected String svcId() {
        return dbSvcId;
    }

    protected Class<?> modelType() {
        return modelType;
    }
}
