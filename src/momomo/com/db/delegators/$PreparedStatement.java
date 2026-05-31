package momomo.com.db.delegators;

import momomo.com.Lambda;
import momomo.com.db.$Sql;
import momomo.com.db.$SqlResultSet;
import momomo.com.exceptions.$DatabaseSQLException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Set;

/**
 * @author Joseph S.
 */
public final class $PreparedStatement {
    public final PreparedStatement delegate;

    public $PreparedStatement(PreparedStatement delegate) {
        this.delegate = delegate;
    }

    public void setObject(int i, Object val) throws SQLException {
        this.setObject(i, val, null);
    }

    public void setObject(int i, Object val, Integer type) throws SQLException {
        if ( type == null ) {
            delegate.setObject(i, val);
        }
        else {
            delegate.setObject(i, val, type);
        }
    }

    public void setString(int parameterIndex, String x) {
        try {
            delegate.setString(parameterIndex, x);
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    public void setLong(int parameterIndex, long x) {
        try {
            delegate.setLong(parameterIndex, x);
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    public int update() {
        try {
            return delegate.executeUpdate();
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    public $SqlResultSet query() {
        try {
            return new $SqlResultSet(delegate.executeQuery());
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    public <R, E extends Exception> R query(Lambda.R1E<R, $SqlResultSet, E> lambda) throws E {
        try( $SqlResultSet rs = query() ) {
            return lambda.call(rs);
        }
    }

    public $PreparedStatement setQuestionParameters(Set<Map.Entry<String, $Sql.Val>> values) {
        int i = 0; for (Map.Entry<String, $Sql.Val> entry : values) {
            try {
                Object val   = entry.getValue().val;
                Integer type = null;

                if ( false ) {
                    if ( val instanceof Number ) {
                        if ( val instanceof Long ) {
                            type = Types.NUMERIC;
                        }
                    }
                    else if ( val instanceof String ) {
                        type = Types.VARCHAR;
                    }
                }

                setObject(++i, val, type);

            } catch (SQLException e) {
                throw new $DatabaseSQLException(e);
            }
        }

        return this;
    }
}
