package momomo.com.db;

import momomo.com.Lambda;
import momomo.com.Reflects;
import momomo.com.exceptions.$DatabaseSQLException;
import momomo.com.exceptions.$RuntimeException;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static momomo.com.db.$Sql.Table.Column.END;
import static momomo.com.db.$Sql.Table.Column.START;


/**
 * @author Joseph S.
 */
public final class $SqlResultSet implements AutoCloseable {
    protected final ResultSet delegate;

    public $SqlResultSet(ResultSet delegate) {
        this.delegate = delegate;
    }

    /////////////////////////////////////////////////////////////////////

    public List<Row> getRows() {
        ArrayList<Row> list = new ArrayList<>();

        eachRow(list::add);

        return list;
    }

    /**
     * Must be called in the beginning and not after subsequent calls to next()
     */
    public Row getFirstRow() {
        if( isEmpty() ) {
            return null;
        }
        return getCurrentRow();
    }

    /**
     * Basically on current position.
     *
     * Note, that this will fail if next has not been called prior.
     *
     * If you wish to retrieve the first, call first() which will move the cursor if it has to.
     */
    public Row getCurrentRow() {
        return new Row(this, null);
    }

    public <E extends Exception> void each(Lambda.VE<E> lambda) throws E {
        while ( next() ) {
            lambda.call();
        }
    }

    public <E extends Exception> void eachRow(Lambda.V1E<Row, E> lambda) throws E {
        while ( next() ) {
            lambda.call( getCurrentRow() );
        }
    }

    public <T, E extends Exception> List<T> eachAs(Class<? extends T> klass) throws E {
        return this.eachAs(klass, null);
    }

    /**
     * Similar to eachRow but allows you to pass the class type of the thing you wish to create instead and it will reflect map the columns to the instance
     *
     * @param lambda can be null
     */

    public <T, E extends Exception> List<T> eachAs(Class<? extends T> klass, Lambda.V1E<T, E> lambda) throws E {
        List<T> list = new ArrayList<>();

        each(() -> {
            T instance = Reflects.newInstance(klass);

            // Iterate each column on the row, and try to set the value on the instance if a match exists
            eachColumn(c -> {
                Field f = Reflects.getField(klass, c.key(false));
                if ( f != null ) {

                    if ( !f.canAccess(instance) ) {
                        f.trySetAccessible();
                    }

                    try {
                        f.set(instance, c.val());
                    }
                    catch(IllegalArgumentException | IllegalAccessException e) {
                        throw new $RuntimeException(e);
                    }
                }
            });

            if ( lambda != null ) {
                lambda.call(instance);
            }

            list.add(instance);
        });

        return list;
    }

    public <E extends Exception> void eachColumn(Lambda.V1E<Row.Column, E> lambda) throws E{
        new Row(this, lambda);
    }

    /////////////////////////////////////////////////////////////////////

    /**
     * Be careful how you use this method. Do not while(next) after call to this.
     *
     * A call to this method moves the cursor forwards if we are at zero.
     *
     * We could call delegate.before but it is not 100% supported in all cases ( our tested case was not with PG JDBC ).
     *
     * For us, it matters not if the cursor move forward once.
     */
    public boolean isEmpty() {
        if ( isBeforeFirst() ){
            return !next();
        }
        return false;
    }

    public boolean previous() {
        try {
            return delegate.previous();
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    /**
     * Note, delegate.isBeforeFirst is not reliable, nor is delegate.isFirst
     */
    public boolean isBeforeFirst() {
        return getCurrentRowIndex() == 0;
    }

    public int getCurrentRowIndex() {
        try {
            return delegate.getRow();
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    /////////////////////////////////////////////////////////////////////

    private ResultSetMetaData metadata;
    public ResultSetMetaData getMetadata() {
        try {
            return metadata == null ? metadata = this.delegate.getMetaData() : metadata;
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    private Integer columnCount;
    public int getColumnCount() {
        try {
            return columnCount == null ? columnCount = getMetadata().getColumnCount() : columnCount;
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    public String getColumnName(int i) {
        try {
            return getMetadata().getColumnName ( i );
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    public boolean next() {
        try {
            return delegate.next();
        }
        catch(SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    public Object getObject(int i) {
        try {
            return delegate.getObject(i);
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    @Override
    public void close() {
        try {
            delegate.close();
        } catch (SQLException e) {
            throw new $DatabaseSQLException(e);
        }
    }

    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    /**
     * Contains several pairs of entries [{columnName:val}]
     */
    public static final class Row {
        public  final $SqlResultSet outer;
        private final List<Column>            lst   = new ArrayList<>();
        private final HashMap<String, Column> map   = new HashMap<>();

        protected Row($SqlResultSet outer) {
            this(outer, null);
        }

        protected <E extends Exception> Row($SqlResultSet outer, Lambda.V1E<Column, E> lambda) throws E {
            this.outer = outer;

            each((column -> {
                // Merges with passed lambda
                add(column);

                if ( lambda != null ) {
                    lambda.call(column);
                }

            }));
        }

        public <E extends Exception> void each(Lambda.V1E<Column, E> lambda) throws E {
            for (int i = 1; i <= outer.getColumnCount(); ++i) {
                Column column = new Column(this, outer.getColumnName(i), outer.getObject(i));

                // If a wish to iterate while build exists, then the user can do so avoiding repeated iterations
                if ( lambda != null ) {
                    lambda.call(column);
                }
            }
        }

        private void add(Column e) {
            lst.add(e); map.put(e.key, e);
        }

        public <R> R val(int i) {
            return key(i).val();
        }
        public <R> R val(CharSequence col) {
            if ( col instanceof $Sql.Table.Column ) {
                col = (($Sql.Table.Column) col).toRead();
            }

            return key(col).val();
        }

        public Column key(int index) {
            return lst.get(index);
        }
        public Column key(CharSequence col) {
            return map.get(col.toString());
        }
        public List<Column> keys() {
            return Collections.unmodifiableList(lst);
        }

        /////////////////////////////////////////////////////////////////////
        public static final class Column {
            public  final Row    outer;
            private final String key;
            private final Object val;

            public Column(Row outer, String key, Object val) {
                this.outer = outer;
                this.key = key;
                this.val = val;
            }

            public <R> R val() {
                return (R) this.val;
            }

            public String key() {
                return key(true);
            }

            public String key(boolean table) {
                String key = this.key;
                if ( !table ) {
                    key = removeTable(key);
                }
                return key;
            }

            private static String removeTable(String key) {
                int EXTRA = 1;  // At least one char is expected as a table name besides START AND END
                if ( key.length() >= START.length() + END.length() + EXTRA) {
                    if ( key.startsWith(START) ) {
                        int i = key.indexOf(END, START.length() + EXTRA);
                        if ( i >= 0 ) {
                            key = key.substring(i + END.length());  // Strip away table name so we only have column name
                        }
                    }
                }
                return key;
            }
        }
        /////////////////////////////////////////////////////////////////////
    }


}
