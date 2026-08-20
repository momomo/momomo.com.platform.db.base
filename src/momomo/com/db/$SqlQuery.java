package momomo.com.db;

import momomo.com.Lambda;

import java.util.List;

public interface $SqlQuery {
    
    <R, E extends Exception>R query(Lambda.R1E<R, $SqlResultSet, E> lambda) throws E;
    
    default <E extends Exception> void query(Lambda.V1E<$SqlResultSet, E> lambda) throws E {
        this.query(lambda.R1E());
    }
    
    default <E extends Exception> void each(Lambda.V1E<$SqlResultSet, E> lambda) throws E {
        query((rs)-> {
            rs.each(()-> {
                lambda.call( rs );
            });
        });
    }
    
    default <E extends Exception> void eachRow(Lambda.V1E<$SqlResultSet.Row, E> lambda) throws E {
        query((rs)-> {
            rs.eachRow(lambda);
        });
    }
    
    default <T, E extends Exception> List<T> eachAs(Class<? extends T> klass) throws E {
        return this.eachAs(klass, null);
    }
    
    default <T, E extends Exception> List<T> eachAs(Class<? extends T> klass, Lambda.V1E<T, E> lambda) throws E {
        return query(rs -> {
            return rs.eachAs(klass, lambda);
        });
    }
    
}
