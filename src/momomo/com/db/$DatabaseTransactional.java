package momomo.com.db;

import momomo.com.Lambda;
import momomo.com.db.delegators.$Connection;

/**
 * This class needs improvement and needs to implemment the momomo.com.db.transactional API eventually so we can get the same features even for JDBC connections.   
 * 
 * @author Joseph S.
 */
public interface $DatabaseTransactional extends $DatabaseConnection {
    
    default <E extends Exception> void newTransaction(Lambda.V1E<$Connection, E> lambda) throws E {
        newTransaction(lambda.R1E());
    }
    
    default <E extends Exception> void newTransaction(CharSequence name, Lambda.V1E<$Connection, E> lambda) throws E {
        newTransaction(name, lambda.R1E());
    }
    
    default <R, E extends Exception> R newTransaction(Lambda.R1E<R, $Connection, E> lambda) throws E {
        return newTransaction(name(), lambda);
    }
    
    default <R, E extends Exception> R newTransaction(CharSequence name, Lambda.R1E<R, $Connection, E> lambda) throws E {
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

}
