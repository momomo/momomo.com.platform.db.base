package momomo.com.db;

import momomo.com.Lambda;
import momomo.com.Regexes;
import momomo.com.db.delegators.$Connection;
import momomo.com.db.delegators.$PreparedStatement;
import momomo.com.exceptions.$DatabaseSQLException;
import momomo.com.sources.RegexReplacor;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

/**
 * @author Joseph S.
 */
public class $JdbcSql extends $Sql<$JdbcSql> {
    final ArrayList<Object> values = new ArrayList<>();
    
    @Override
    public String get() {
        values.clear();
        
        String query = super.get();
        query        = new RegexReplacor<RuntimeException>(Val.JDBC_PATTERN, new StringBuilder(), query) {
            @Override
            protected void match() throws RuntimeException {
                // Pull out the key
                String key = Val.LEFT + Regexes.group(matcher, 1) + Val.RIGHT;
                
                // Get the val
                Object val = $JdbcSql.super.VALS.get(key).val;
                
                // Add it by index it was discovererd / ran into
                values.add(val);
                
                // Replace with ?
                out("?");
            }
            
            protected String trim(String text) {
                return text;
            }
            
        }.go().toString();
        
        return query;
    }
    
    /**
     * Note, create() has to be called prior to this
     */
    public $PreparedStatement values($PreparedStatement statement) {
        
        // Now, we can walk over the keys which where replaced by index, and kept track of order
        // Get the value by key, and then set by index
        int i = -1; while ( ++i < values.size() ) {
            try {
                Object val = values.get(i);
                
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
                
                // Index starts at zero
                statement.setObject(i+1, val, type);
                
            } catch (SQLException e) {
                throw new $DatabaseSQLException(e);
            }
        }
        
        return statement;
    }
    
    
    public <E extends Exception> void prepared($Connection connection, Lambda.V1E<$PreparedStatement, E> lambda) throws E {
        prepared(connection, lambda.R1E());
    }
    public <R, E extends Exception> R prepared($Connection connection, Lambda.R1E<R, $PreparedStatement, E> lambda) throws E {
        return connection.prepared(this.get(), statement -> {
            $JdbcSql.this.values(statement); return lambda.call(statement);
        });
    }
    
}
