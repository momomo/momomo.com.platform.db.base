package momomo.com.db;

import momomo.com.Ex;
import momomo.com.Globals;
import momomo.com.Lambda;
import momomo.com.annotations.informative.Overridable;
import momomo.com.annotations.informative.Protected;
import momomo.com.db.delegators.$Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Joseph S.
 */
public interface $DatabaseConnection {
    
    @Protected abstract String name();
    
    @Overridable
    @Protected default String protocol() {
        return Globals.Configurable.DATABASE_SERVER_PROTOCOL.get();
    }
    @Overridable
    @Protected default String host() {
        return Globals.Configurable.DATABASE_SERVER_HOST.get();
    }
    
    @Overridable
    @Protected default String port() {
        return Globals.Configurable.DATABASE_SERVER_PORT.get();
    }
    
    @Overridable
    @Protected default String username() {
        return Globals.Configurable.DATABASE_SERVER_PASSWORD.get();
    }
    
    @Overridable
    @Protected default String password() {
        return Globals.Configurable.DATABASE_SERVER_PASSWORD.get();
    }

    @Overridable
    @Protected default String url() {
        return url(name());
    }
    
    @Overridable
    @Protected default String url(CharSequence db) {
        return protocol() + host() + ":" + port() + "/" + db;
    }

    /////////////////////////////////////////////////////////////////////

    default java.sql.Connection connection() {
        return connection(name());
    }
    default java.sql.Connection connection(CharSequence db) {
        try {
            return DriverManager.getConnection(url(db), username(), password());
        } catch (SQLException e) {
            throw Ex.runtime(e);
        }
    }
    default <E extends Exception> void connection(Lambda.V1E<$Connection, E> lambda) throws E {
        connection(lambda.R1E());
    }
    default <R, E extends Exception> R connection(Lambda.R1E<R, $Connection, E> lambda) throws E {
        return connection(name(), lambda);
    }
    default <E extends Exception> void connection(CharSequence db, Lambda.V1E<$Connection, E> lambda) throws E {
        connection(db, lambda.R1E());
    }
    default <R, E extends Exception> R connection(CharSequence db, Lambda.R1E<R, $Connection, E> lambda) throws E {
        try (java.sql.Connection connection = connection(db)) {
            return lambda.call( new $Connection(connection) );
        } catch (SQLException e) {
            throw Ex.runtime(e);
        }
    }

}
