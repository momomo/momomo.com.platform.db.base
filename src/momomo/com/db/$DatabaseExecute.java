package momomo.com.db;

import momomo.com.Lambda;
import momomo.com.annotations.informative.Private;
import momomo.com.db.delegators.$Connection;
import momomo.com.db.delegators.$Statement;
import momomo.com.exceptions.$DatabaseSQLException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Joseph S.
 */
public interface $DatabaseExecute extends $DatabaseTransactional {
    
    /////////////////////////////////////////////////////////////////////
    // sqll
    /////////////////////////////////////////////////////////////////////

    default void sql(String sql) {
        connection((connection) -> {
            sql(connection, sql); return null;
        });
    }

    default <E extends Exception> void sql(String sql, Lambda.V1E<$SqlResultSet, E> lambda) throws E {
        sql(sql, lambda.R1E());
    }

    /**
     * This only delegates
     */
    default <R, E extends Exception> R sql(String sql, Lambda.R1E<R, $SqlResultSet, E> lambda) throws E {
        return connection(connection -> {
            return $DatabaseExecute.this.sql(connection, sql, lambda);
        });
    }

    default <R, E extends Exception> R sql($Connection connection, String sql, Lambda.R1E<R, $SqlResultSet, E> lambda) throws E {
        return connection.statement(statement -> {
            return query(() -> {
                return statement.query(sql);
            }, lambda);
        });
    }

    @Private default boolean sql($Connection connection, String sql) {
        return connection.statement(($Statement statement) -> {
            return statement.execute(sql);
        });
    }

    /////////////////////////////////////////////////////////////////////
    // query
    /////////////////////////////////////////////////////////////////////

    @Private default <R, E extends Exception> R query(Lambda.RE<ResultSet, SQLException> lambdaExecute, Lambda.R1E<R, $SqlResultSet, E> lambda) throws E {
        ResultSet rs;
        try {
             rs = lambdaExecute.call();
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }

        return query(rs, lambda);
    }

    @Private default <R, E extends Exception> R query(ResultSet rs, Lambda.R1E<R, $SqlResultSet, E> lambda) throws E {
        try (rs) {
            if (lambda != null) {
                return lambda.call(new $SqlResultSet(rs));
            }
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
        return null;
    }

}
