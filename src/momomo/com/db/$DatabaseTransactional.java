package momomo.com.db;

import momomo.com.Lambda;
import momomo.com.db.delegators.$Connection;

/**
 * @author Joseph S.
 */
public interface $DatabaseTransactional extends $DatabaseConnection {

    default <E extends Exception> void withTransaction(Lambda.V1E<$Connection, E> lambda) throws E {
        withTransaction(lambda.R1E());
    }
    default <R, E extends Exception> R withTransaction(Lambda.R1E<R, $Connection, E> lambda) throws E {
        return withTransaction(name(), lambda);
    }
    default <R, E extends Exception> R withTransaction(String name, Lambda.R1E<R, $Connection, E> lambda) throws E {
        return connection(name, connection -> {
            connection.setAutoCommit(false);

            R returns;
            try {
                 returns = lambda.call(connection);
            }
            catch (Throwable e) {
                connection.rollback();

                throw e;
            }

            connection.commit();

            return returns;
        });
    }

    /////////////////////////////////////////////////////////////////////

}
