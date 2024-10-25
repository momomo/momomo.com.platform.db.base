package momomo.com.db.delegators;

import momomo.com.exceptions.$DatabaseSQLException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Joseph S.
 */
public final class $Statement {
    public final Statement delegate;

    public $Statement(Statement delegate) {
        this.delegate = delegate;
    }

    public boolean execute(String sql) {
        try {
            return delegate.execute(sql);
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    public ResultSet query(String sql) {
        try {
            return delegate.executeQuery(sql);
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }
}
