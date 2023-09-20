package momomo.com.db;

import momomo.com.Reflects;
import momomo.com.annotations.informative.Protected;

import java.sql.Driver;

/**
 * @author Joseph S.
 */
public interface $DatabaseDrivers {

    Class<? extends Driver> driverClass();

    @Protected
    default void driverLoad() {
        Reflects.getClass(driverClass().getName());
    }


}
