package momomo.com.db.delegators;

import momomo.com.Lambda;
import momomo.com.exceptions.$DatabaseSQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Joseph S.
 */
public final class $Connection {
    public final Connection delegate;
    
    public $Connection(Connection delegate) {
        this.delegate = delegate;
    }
    
    public void setAutoCommit(boolean b) {
        try {
            delegate.setAutoCommit(b);
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }
    
    /////////////////////////////////////////////////////////////////////
    
    public void commit() {
        try {
            delegate.commit();
        } catch (SQLException e) {
            throw  new $DatabaseSQLException(e);
        }
    }
    
    public void rollback() {
        try {
            delegate.rollback();
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }
    
    /////////////////////////////////////////////////////////////////////
    
    public Statement statement() throws SQLException {
        return delegate.createStatement();
    }
    
    public <E extends Exception> void statement(Lambda.V1E<$Statement, E> lambda) throws E {
        statement(lambda.R1E());
    }
    public <R, E extends Exception> R statement(Lambda.R1E<R, $Statement, E> lambda) throws E {
        try(Statement statement = statement()) {
            return lambda.call(new $Statement(statement));
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }
    
    /////////////////////////////////////////////////////////////////////
    
    public PreparedStatement prepared(String sql) throws SQLException {
        return delegate.prepareStatement(sql);
    }
    public <E extends Exception> void prepared(String sql, Lambda.V1E<$PreparedStatement, E> lambda) throws E {
        prepared(sql, lambda.R1E());
    }
    public <R, E extends Exception> R prepared(String sql, Lambda.R1E<R, $PreparedStatement, E> lambda) throws E {
        try (PreparedStatement prepared = prepared(sql)) {
            return lambda.call( new $PreparedStatement(prepared) );
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }
    
}
