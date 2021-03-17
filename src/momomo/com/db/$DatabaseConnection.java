package momomo.com.db;

import momomo.com.annotations.informative.Protected;
import momomo.com.Lambda;
import momomo.com.exceptions.$DatabaseSQLException;
import momomo.com.db.delegators.$Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Joseph S.
 */
public interface $DatabaseConnection {

    @Protected
    String protocol();

    @Protected
    String port();

    @Protected
    String name();

    @Protected
    String username();

    @Protected
    String password();

    @Protected
    default String host() {
        return "localhost";
    }

    @Protected
    default String url() {
        return url(name());
    }

    @Protected
    default String url(String db) {
        return protocol() + host() + ":" + port() + "/" + db;
    }

    /////////////////////////////////////////////////////////////////////

    default java.sql.Connection connection() throws SQLException {
        return connection(name());
    }
    default java.sql.Connection connection(String db) throws SQLException {
        return DriverManager.getConnection(url(db), username(), password());
    }
    default <E extends Exception> void connection(Lambda.V1E<$Connection, E> lambda) throws E {
        connection(lambda.R1E());
    }
    default <R, E extends Exception> R connection(Lambda.R1E<R, $Connection, E> lambda) throws E {
        return connection(name(), lambda);
    }
    default <E extends Exception> void connection(String db, Lambda.V1E<$Connection, E> lambda) throws E {
        connection(db, lambda.R1E());
    }
    default <R, E extends Exception> R connection(String db, Lambda.R1E<R, $Connection, E> lambda) throws E {
        try (java.sql.Connection connection = connection(db)) {
            return lambda.call( new $Connection(connection) );
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

}
