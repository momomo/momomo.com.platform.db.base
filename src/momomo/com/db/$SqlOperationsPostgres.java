package momomo.com.db;

import momomo.com.Is;

/**
 * @author Joseph S.
 */
public interface $SqlOperationsPostgres extends $SqlOperations {
    
    default void indexCreate(String table, String columns, String nameAppend, String where) {
        sqlUpdate(indexSql(table, columns, nameAppend, where));
    }
    
    default void indexCreate(String name, String create) {
        sqlUpdate(indexSql(name, create));
    }
    
    default void indexDrop(String table, String columns, String nameAppend) {
        sqlUpdate( "DROP INDEX IF EXISTS " + indexNameQuoted(table, columns, nameAppend) );
    }
    
    private String indexSql(String table, String columns, String nameAppend, String where) {
        return indexSql(
            indexName(table, columns, nameAppend),
            "CREATE INDEX " + indexNameQuoted(table, columns, nameAppend) + " ON public." + table + " (" + columns + ") " + (Is.Ok(where) ? " WHERE " + where : "")
        );
    }
    
    private String indexSql(String name, String create) {
        return "" +
            "DO $$\n" +
            "BEGIN\n" +
            "\n" +
            "IF NOT EXISTS (\n" +
            "    SELECT 1\n" +
            "    FROM   pg_class c\n" +
            "    JOIN   pg_namespace n ON n.oid = c.relnamespace\n" +
            "    WHERE  c.relname = '" + name + "'\n" +
            "    AND    n.nspname = 'public'\n" +
            "    ) THEN\n" +
            "\n" +
                create + ";\n" +
            "END IF;\n" +
            "\n" +
            "END$$;";
    }
    
    
    private String indexNameQuoted(String table, String columns, String nameAppend) {
        return "\"" + indexName(table, columns, nameAppend) + "\"";
    }
    private String indexName(String table, String columns, String nameAppend) {
        return table + "(" + columns + ") " + ( Is.Or(nameAppend, "") );
    }
    
}
