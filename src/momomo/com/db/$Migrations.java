package momomo.com.db;

import lombok.Setter;
import lombok.experimental.Accessors;
import momomo.com.Strings;
import momomo.com.Time;
import momomo.com.annotations.informative.Development;
import momomo.com.collections.$List;
import momomo.com.db.delegators.$Connection;
import momomo.com.log;

import java.sql.Timestamp;
import java.util.Objects;

import static momomo.com.Yarn.$;

/**
 * @author Joseph S.
 */
public class $Migrations implements $MigrationsTable {
    
    public final boolean allow_deleting_of_already_applied_migrations;
    
    private final $JdbcSql sql;
    
    /////////////////////////////////////////////////////////////////////
    public static final class Cons {
        public static final String table = "migrations";
        
        public static final String create = """
                id        character varying(255)      NOT NULL  ,
                start     timestamp without time zone NOT NULL  ,
                finish    timestamp without time zone NOT NULL  ,
                before    character varying(255)                ,               
                
                CONSTRAINT ${table}_pkey PRIMARY KEY (id)       ,
                CONSTRAINT ${table}_ukey UNIQUE      (before)   ,
                                                                
                FOREIGN KEY (before) REFERENCES ${table}(id)    
            """
            ;
    }
    /////////////////////////////////////////////////////////////////////
    
    private final $Database        database;
    private final $Sql.Table       table;
    private final $List<Migration> migrations;
    
    @Accessors(chain = true, fluent = true) @Setter public static final class Params {
        private final $Database database;
        public Params($Database database) {
            this.database = database;
        }
        private String  table                                        = Cons.table;
        private boolean allow_deleting_of_already_applied_migrations = true;
    }
    public $Migrations($Database database) {
        this(new Params(database));
        
    }
    public $Migrations(Params params) {
        this.database   = params.database;
        
        this.sql        = new $JdbcSql();
        this.table      = new $Sql.Table(params.table);
        
        this.allow_deleting_of_already_applied_migrations = params.allow_deleting_of_already_applied_migrations;
        
        this.migrations = new $List<>();
    }
    
    /////////////////////////////////////////////////////////////////////
    
    @Override
    public $Sql.Table table() {
        return table;
    }
    
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    
    public void migrate() throws Exception {
        create();
        alter();
        
        database.withTransaction(connection -> {
            // 1. Totally fresh, meaning last is null
            // 2. Last is not null, this is only used as the previous value in case someting is new, and previous is null
            
            CharSequence last = getLastMigratedId(connection);
            
            // Null first time, then will contain the last one to migrate
            CharSequence previous = null;
            for (Migration migration : migrations) {
                CharSequence id = migration.id();
                
                log.info(getClass(), Strings.NEWLINE, "Attempting to migrate '" + id + "'", Strings.NEWLINE);
                
                // Get from db for same id
                $SqlResultSet.Row row = getId(connection, id);
                
                // Not encountered before. New row.
                if  ( row == null ) {
                    if ( previous == null && allow_deleting_of_already_applied_migrations ) {
                        previous = last;
                    }
                    
                    // When inserting anything new, if previous != last then it is an error
                    if ( !Objects.equals(previous, last) ) {
                        throw new $MigrationsException(id, last);
                    }
                    
                    Timestamp started = Time.stamp();
                    try {
                        migration.perform();
                    }
                    finally {
                        CharSequence p = previous;
                        boolean modified = sql
                            .clear()
                            .insert(table(), $ -> {
                                $.value(id(), id);
                                $.value(start(), started);
                                $.value(finish(), Time.stamp());
                                $.value(before(), p);
                            })
                            .prepared(connection, statement -> {
                                return statement.update() > 0;
                            })
                            ;
                        
                        if ( modified) {
                            log.info(getClass(), "Migration of '" + id + "' was added!");
                        }
                        
                        last = migration.id();
                    }
                }
                else if ( (previous == null && allow_deleting_of_already_applied_migrations) || Objects.equals(previous, row.val(before())) ) {
                    log.info(getClass(), Strings.NEWLINE, "Already migrated '" + id + "' before." , Strings.NEWLINE);
                }
                else {
                    throw new $MigrationsException(id, row.val(before()));
                }
                
                previous = migration.id();;
            }
        });
    }
    
    public $SqlResultSet.Row getId($Connection connection, CharSequence id) {
        $SqlResultSet.Row row = sql
            .clear()
            .select( id(), before() )
            .from  ( table() )
            .where ( id().eq(sql.val(id)) )
            
            .prepared(connection, (statement) -> {
                return statement.query().getFirstRow();
            })
            ;
        return row;
    }
    
    public CharSequence getLastMigratedId($Connection connection) {
        $SqlResultSet.Row row = sql
            .clear()
            .select(id())
            .from(table())
            .limit(1)
            .order(start(), $Sql.Order.DESC)
            
            .prepared(connection, (statement) -> {
                return statement.query().getFirstRow();
            })
            ;
        
        if ( row == null ) {
            return null;
        }
        
        return row.val(id());
    }
    
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    
    /**
     * Create migrations table
     */
    protected void create() {
        database.tableCreate(
            table().toCreate(),
            $.create(Cons.create, "table", Cons.table)
        );
    }
    
    protected void alter() {
        
    }
    
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    
    /**
     * Non static inner class for convinience, adds directly to migrations
     */
    public abstract class Migration extends $Migration {
        public Migration(CharSequence id) {
            super(id); $Migrations.this.migrations.add(this);
        }
    }
    
    
    
    
    
    
    
    @Development private void test() {
        
        database.withTransaction(connection -> {
            
            $JdbcSql sql = new $JdbcSql();
            $Sql.Table table = sql.table(this.table);
            
                /*
                    UPDATE table_name
                    SET column1 = value1, column2 = value2, ...
                    WHERE condition;
                */
            
            // insert(sql, table);
            
            // sql.free( "UPDATE " + table + " SET completed = "+ sql.val(Time.stamp()) );
            
            sql
                .update(table, $ -> {
                    $.value(table.column("completed"), Time.stamp());
                })
                .where(
                    id().eq(sql.val("111"))
                )
            ;
            
            // sql.free("INSERT INTO myway (id, begun, completed) VALUES (" + sql.val("dddddddddddddddddddddddd") + ", " + sql.val(Time.stamp())+ ", " + sql.val(Time.stamp()) + ");");
            
            connection.prepared(sql.get(), statement -> {
                sql.values(statement).update();
            });
            
        });

            /*
            id        character varying(255)      NOT NULL  ,
            start     timestamp without time zone NOT NULL  ,
            finish    timestamp without time zone NOT NULL  ,
            before    character varying(255)                ,
    
            CONSTRAINT ${table}_pkey PRIMARY KEY (id)       ,
            CONSTRAINT ${table}_ukey UNIQUE      (before)   ,
    
            FOREIGN KEY (before) REFERENCES ${table}(id)*/
        
        sql.
            clear().
            create(table, $ -> {
                $.row(id()    , "character varying(255) NOT NULL");
                $.row(start() , "timestamp without time zone NOT NULL");
                $.row(finish(), "timestamp without time zone NOT NULL");
                $.row(before(), "character varying(255)");
                
                $.constraint("PRIMARY KEY", id());
                $.constraint("UNIQUE KEY", before());

                    /*
                    CONSTRAINT ${table}_pkey PRIMARY KEY (id)       ,
                    CONSTRAINT ${table}_ukey UNIQUE      (before)   ,
    
                    FOREIGN KEY (before) REFERENCES ${table}(id)
                    */
                
                // $.row("CONSTRAINT migrations_pkey PRIMARY KEY (id)");
                
                
                
                $.owner(database.username());
            })
        ;
    }
    
}

