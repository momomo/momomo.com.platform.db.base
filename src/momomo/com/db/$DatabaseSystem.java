package momomo.com.db;

import momomo.com.Is;
import momomo.com.Strings;
import momomo.com.annotations.informative.Protected;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Temporary, needs to be revised. Method names might change in the future. 
 * 
 * @author Joseph S.
 */
public interface $DatabaseSystem extends $DatabaseDrivers, $DatabaseConnection, $DatabaseTransactional, $DatabaseExecute {
    
    /**
     * Example: postgres
     */
    @Protected
    String getSystemDatabase();
    
    @Protected
    default String sqlDropDatabase() {
        return "DROP DATABASE " + name() + ";";
    }
    
    @Protected
    default String sqlCreateDatabase() {
        return "CREATE DATABASE " + name() + ";";
    }
    
    @Protected
    default String sqlHasTable(String table) {
        return sqlListTables() + " AND TABLE_NAME='" + table + "'";
    }
    @Protected
    default String sqlCreateTable(String table, String columns) {
        return "CREATE TABLE IF NOT EXISTS "+table+"\n" +
            "(\n" +
                columns +
            ");\n" +
            "ALTER TABLE "+table + " OWNER TO " + username() + ";"
            ;
    }
    
    default String sqlDropTables() {
        return sqlDropTables(null);
    }
    
    @Protected
    default String sqlDropTables(CharSequence... tables) {
        return "DROP TABLE IF EXISTS " + Strings.join(", ", tables) + ";"; //  " CASCADE;";
    }
    
    @Protected
    default String sqlListTables() {
        return "SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_type='BASE TABLE' AND table_schema='public' AND table_catalog='" + name() + "'";
    }
    
    default String sqlGetColumns(CharSequence table) {
        return "SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_schema = 'public' AND table_catalog = '"+ name() +"' AND table_name = '"+table+"'";
    }
    
    @Protected
    default String sqlGetColumnsConnected() {
        return """
                SELECT A.table_name as fromtable, A.column_name as fromcolumn, B.table_name as toTable, B.column_name toColumn
                FROM information_schema.key_column_usage A
                JOIN information_schema.constraint_column_usage B ON A.constraint_name = B.constraint_name
        """;
    }
    
    /////////////////////////////////////////////////////////////////////
    
    @Protected
    default boolean exists() {
        driverLoad();
        
        try {
            connection().close(); return true;
        } catch (SQLException e) {
            return false;   // Connection failed, probably the database does not exist
        }
    }
    
    @Protected
    default void create() {
        connection(getSystemDatabase(), (connection) -> {
            sql(connection, sqlCreateDatabase());
            return null;
        });
    }
    
    @Protected
    default void drop() {
        connection(getSystemDatabase(), (connection) -> {
            sql(connection, sqlDropDatabase());
            return null;
        });
    }
    
    default boolean isVoid() {
        return !tablesHas();
    }
    
    /////////////////////////////////////////////////////////////////////
    
    default void tableCreate(String table, String columns) {
        sql(sqlCreateTable(table, columns));
    }
    
    /////////////////////////////////////////////////////////////////////
    
    default boolean tableHas(String table) {
        return sql(sqlHasTable(table), rs -> {
            return rs.next();
        });
    }
    
    default boolean tablesHas() {
        return sql(sqlListTables(), rs -> {
            return rs.next();
        });
    }
    
    /////////////////////////////////////////////////////////////////////
    
    default ArrayList<String> tablesList() {
        ArrayList<String> tables = new ArrayList<>();
        this.sql(sqlListTables(), ($SqlResultSet rs) -> {
            List<$SqlResultSet.Row> rows = rs.getRows();
            for ($SqlResultSet.Row row : rows) {
                for ($SqlResultSet.Row.Column column : row.keys()) {
                    tables.add(column.val().toString());
                }
            }
        });
        
        return tables;
    }
    
    /////////////////////////////////////////////////////////////////////
    
    default void tablesDrop() {
        this.tablesDrop(null);
    }
    
    /**
     * Drops all but excluding ( preferably a hashset of some sort)
     */
    default void tablesDrop(Iterable<String> excluding) {
        tablesDrop(tablesList(), excluding);
    }
    
    default void tablesDrop(Collection<String> tables, Iterable<String> excluding) {
        HashSet<String> drop = new HashSet<>(tables);
        
        if ( excluding != null ) {
            for (CharSequence table : excluding) {
                drop.remove(table.toString());
            }
        }
        
        if ( !drop.isEmpty() ) {
            connection((connection) -> {
                sql(connection, sqlDropTables(drop.toArray(new CharSequence[]{})));
            });
        }
    }
    
    /////////////////////////////////////////////////////////////////////
    
    /**
     * DROP SEQUENCE IF EXISTS polkadot_seq, stellar_seq;
     */
    default String sqlSequencesDrop(CharSequence ... sequences) {
        return "DROP SEQUENCE IF EXISTS " + Strings.join(", ", sequences) + ";";  // We assume it looks the same in all databases, otherwise the implementor should override to fit theirs
    }
        
    default void sequencesDrop(CharSequence ... sequences) {
        if ( Is.Ok(sequences) ) {
            connection(connection -> {
                sql(connection, sqlSequencesDrop(sequences));
            });
        }
    }
    
    /////////////////////////////////////////////////////////////////////
    
    @Protected default $DatabaseColumnMapping getColumnMapping() {
        $DatabaseColumnMapping cc = new $DatabaseColumnMapping();
        
        String query = sqlGetColumnsConnected();
        
        sql(query, (rs) -> {
            rs.eachRow(row -> {
                
                String fromTable  = row.val("fromtable");
                String fromColumn = row.val("fromcolumn");
                
                String toTable    = row.val("totable");
                String toColumn   = row.val("tocolumn");
                
                cc.put(fromTable, fromColumn, toTable, toColumn);
            });
        });
        
        return cc;
    }
    
}
