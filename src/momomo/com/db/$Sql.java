package momomo.com.db;

import momomo.com.Is;
import momomo.com.Lambda;
import momomo.com.annotations.informative.Development;
import momomo.com.sources.$CharSeq;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import static momomo.com.Strings.NEWLINE;

/**
 * @author Joseph S.
 */
public class $Sql<THIS extends $Sql<THIS>> {
    protected final THIS THIS() {  return (THIS) this; }

    private   static final AtomicLong NEXT       = new AtomicLong(0);
    protected static final String     N          = NEWLINE;
    public    static final String     TAB        = "\t";
    public    static final String     SPACE      = " ";
    public    static final String     COMMASPACE = ","  + SPACE;
    public    static final String     SEMI       = ";";
    public    static final String     SEMISPACE  = SEMI + SPACE;
    public    static final String     AND        = " AND ";
    public    static final String     EMPTY      = "";

    private final ArrayList<CharSequence> FREE          = new ArrayList<>();
    private final ArrayList<CharSequence> CREATE        = new ArrayList<>();

    private final ArrayList<CharSequence> UPDATE        = new ArrayList<>();
    private final ArrayList<CharSequence> INSERT        = new ArrayList<>();
    private final ArrayList<CharSequence> INSERT_VALUES = new ArrayList<>();

    private final ArrayList<CharSequence> SELECT        = new ArrayList<>();
    private final ArrayList<CharSequence> DISTINCT      = new ArrayList<>();
    private final ArrayList<CharSequence> FROM          = new ArrayList<>();
    private final ArrayList<CharSequence> WHERE         = new ArrayList<>();
    private final ArrayList<CharSequence> JOIN          = new ArrayList<>();
    private final ArrayList<CharSequence> GROUP         = new ArrayList<>();
    private final ArrayList<CharSequence> ORDER         = new ArrayList<>();

    private         Long                               LIMIT;
    private         Long                               OFFSET;
    protected final Map<String, Val>                   VALS   = new HashMap<>();
    protected final LinkedHashMap<CharSequence, Table> TABLES = new LinkedHashMap<>();

    // Can be called prior to create to reuse an instance
    public THIS clear() {
        FREE.clear();
        CREATE.clear();
        UPDATE.clear();
        INSERT.clear();
        INSERT_VALUES.clear();
        SELECT.clear();
        DISTINCT.clear();
        FROM.clear();
        WHERE.clear();
        JOIN.clear();
        GROUP.clear();
        ORDER.clear();
        LIMIT = null;
        OFFSET = null;
        VALS.clear();
        
        // TABLES is intentionally not cleared!

        return THIS();
    }

    public THIS limit(int limit) {
        return this.limit( (long) limit );
    }
    public THIS limit(Long limit) {
        this.LIMIT = limit; return THIS();
    }

    public THIS offset(Long offset) {
        this.OFFSET = offset; return THIS();
    }

    public Val val(Object val) {
        Val v;

        if ( val instanceof Val ) {
            v = (Val) val;
        }
        else {
            v = new Val(val);
        }

        // We afford to reinsert it if already inserted, no biggie
        VALS.put(v.key, v);

        return v;
    }

    /////////////////////////////////////////////////////////////////////

    public THIS free(CharSequence free) {
        this.FREE.add(free); return THIS();
    }

    /////////////////////////////////////////////////////////////////////

    public final zelect select = new zelect(); public final class zelect { private zelect(){}
        public THIS distinct(Distinct select) {
            $Sql.this.DISTINCT.add(select); return THIS();
        }
        public THIS distinct(CharSequence... columns) {
            return distinct(new Distinct(columns));
        }
    }
    public THIS select(Select select) {
        this.SELECT.add (select); return THIS();
    }
    public THIS select(CharSequence... columns) {
        return select(new Select(columns));
    }

    /////////////////////////////////////////////////////////////////////

    public THIS from(From from) {
        this.FROM.add(from); return THIS();
    }
    /**
     * grom(table1, table2)
     */
    public THIS from(CharSequence... tables) {
        return from(new From(tables));
    }

    /////////////////////////////////////////////////////////////////////

    public THIS join(Join e) {
        this.JOIN.add(e); return THIS();
    }

    public THIS join(CharSequence table, CharSequence on1, CharSequence on2) {
        return join(new Join(table, on1, on2));
    }
    public THIS join(CharSequence columnA, Table.Column columnB) {
        return join(new Join(columnB, columnA));
    }

    /////////////////////////////////////////////////////////////////////

    public THIS values(Object ... values) {
        Val[] vals = new Val[values.length];

        int i = -1; for (Object val : values) {
            vals[++i] = this.val(val);
        }

        this.INSERT_VALUES.add(new Insert.Values(vals));

        return THIS();
    }

    /////////////////////////////////////////////////////////////////////

    public <E extends Exception> THIS insert(CharSequence table, CharSequence ... columns) throws E {
        return insert(table, $-> {
            $.columns(columns);
        });
    }

    public <E extends Exception> THIS insert(CharSequence table, Lambda.V1E<Insert, E> lambda) throws E {
        Insert insert = new Insert(this, table);
        if ( lambda != null ) lambda.call(insert);

        this.INSERT.add(insert);
        return THIS();
    }

    /////////////////////////////////////////////////////////////////////

    public <E extends Exception> THIS update(CharSequence table, Lambda.V1E<Update, E> lambda) throws E {
        Update update = new Update(this, table);
        if ( lambda != null ) lambda.call(update);

        this.UPDATE.add(update);

        return THIS();
    }

    /////////////////////////////////////////////////////////////////////

    public <E extends Exception> THIS create(CharSequence table, Lambda.V1E<Create, E> lambda) throws E {
        Create create = new Create(this, table);

        if ( lambda != null ) lambda.call(create);

        this.CREATE.add(create);

        return THIS();
    }

    /////////////////////////////////////////////////////////////////////

    /**
     * where(column, "=", "val")
     */
    public THIS where(CharSequence ... seqs) {
        WHERE.add(new Where(this, seqs)); return THIS();
    }

    /////////////////////////////////////////////////////////////////////

    public THIS group(Group group) {
        GROUP.add(group); return THIS();
    }

    /**
     * group(column, column, "something")
     */
    public THIS group(CharSequence... seqs) {
        return group(new Group(seqs));
    }

    /////////////////////////////////////////////////////////////////////

    public THIS order(Order group) {
        ORDER.add(group); return THIS();
    }

    public THIS order(CharSequence column) {
        return order(new Order(column));
    }
    /**
     * order(column, "ASC")
     */
    public THIS order(CharSequence column, CharSequence asc) {
        return order(new Order(column, asc));
    }

    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public static class Table implements $CharSeq {
        private final CharSequence                  name;
        private final String                        alias;

        public Table(CharSequence name) {
            this(name, NEXT.incrementAndGet());
        }

        public Table(CharSequence name, long count) {
            this(name, name + "" + count);
        }

        public Table(CharSequence name, String alias) {
            this.name  = name;
            this.alias = alias;
        }

        private final HashMap<CharSequence, Column> columns = new HashMap<>();
        public Column column(CharSequence column) {
            return columns.compute(column.toString(), (k, v) -> {
                if ( v == null ) {
                    v = new Column(Table.this, column, false);
                }

                return v;
            });
        }

        public String toFrom() {
            return this.name + " AS " + this.alias;
        }

        public String toJoin() {
            return toFrom();
        }

        /**
         * You can only insert into a single table only normally but using AS
         * might make sense in context of INSERT...FROM so we include AS since it does not throw an error
         */
        public String toInsert() {
            return toFrom();
        }

        public String toUpdate() {
            return toFrom();
        }

        public String toCreate() {
            return toString();
        }

        public String alias() {
            return alias;
        }

        public String aliased(String append) {
            return alias() + "." + append;
        }

        public final Column[] all() {
            return columns.values().toArray(new Column[]{});
        }

        @Override
        public String toString() {
            return name.toString();
        }

        /////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////

        public static class Column implements $CharSeq {
            protected final Table        outer;
            protected final CharSequence name;

            public static final String START = "__", END = START;

            protected Column(Table outer, CharSequence name) {
                this(outer, name, true);
            }

            public Column(Table outer, CharSequence name, boolean put) {
                this.outer = outer;
                this.name  = name;

                if ( put ) {
                    outer.columns.put(name.toString(), this);
                }
            }

            public String eq(Val val) {
                return toWhere()  + " = " + val;
            }

            public String gt(Val val) {
                return toWhere()  + " > " + val;
            }
            public String ge(Val val) {
                return toWhere()  + " >= " + val;
            }
            public String lt(Val val) {
                return toWhere()  + " < " + val;
            }
            public String le(Val val) {
                return toWhere()  + " <= " + val;
            }

            protected String aliased() {
                return outer.alias + "." + toString();
            }

            protected String ass() {
                return START + outer.alias + END + toString();
            }

            protected String assed() {
                return aliased() + " AS " + ass();
            }

            public Table table() {
                return outer;
            }

            public String toSelect() {
                return assed();
            }

            public String toDistinct() {
                return aliased();
            }

            public String toWhere() {
                return aliased();
            }

            public String toGroup() {
                return aliased();
            }

            public String toOrder() {
                return aliased();
            }

            public String toJoin() {
                return aliased();
            }

            public String toInsert() {
                return toString();
            }

            public String toCreate() {
                return toString();
            }

            public String toUpdate() {
                return toString();
            }

            public String toRead() {
                return ass();
            }

            @Override
            public String toString() {
                return name.toString();
            }

        }
    }

    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public static class Val implements $CharSeq {
        public static final String  WHERED       = ":";
        public static final String  LEFT         = "A";
        public static final String  RIGHT        = "BtiZQe587edf14C";
        public static final String  JDBC_REGEX   = "(?:" + WHERED + LEFT + ")" + "(.*?)" + "(?:" + RIGHT + ")";
        public static final Pattern JDBC_PATTERN = Pattern.compile(JDBC_REGEX);

        public final String key;
        public final Object val;

        private Val(Object val) {
            this(NEXT.incrementAndGet(), val);
        }

        private Val(long key, Object val) {
            this(LEFT + key + RIGHT, val);
        }

        private Val(String key, Object val) {
            this.key = key;
            this.val = val;
        }

        @Override
        public String toString() {
            return toWhere();
        }

        public String toWhere() {
            return WHERED + key;
        }

        public String toInsert() {
            return toWhere();
        }
    }

    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public Table table(CharSequence table) {
        return TABLES.compute(table, (k, v) -> {
            if ( v == null ) { v = new Table(k); } return v;
        });
    }

    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public String get() {
        final StringBuilder
                FREE            = new StringBuilder(),
                CREATE          = new StringBuilder(),
                SELECT          = new StringBuilder(),
                DISTINCT        = new StringBuilder(),
                UPDATE          = new StringBuilder(),
                INSERT          = new StringBuilder(),
                INSERT_VALUES   = new StringBuilder(),
                FROM            = new StringBuilder(),
                JOIN            = new StringBuilder(),
                LATERAL         = new StringBuilder(),
                WHERE           = new StringBuilder(),
                ORDER           = new StringBuilder(),
                GROUP           = new StringBuilder(),
                LIMIT           = new StringBuilder(),
                OFFSET          = new StringBuilder()
        ;

        for ( CharSequence seq : this.FREE) {
            FREE.append( seq.toString() );
        }

        for (CharSequence seq : this.SELECT) {
            if (SELECT.length() > 0) {
                SELECT.append(COMMASPACE);
            }

            SELECT.append(seq.toString());
        }

        for (CharSequence seq : this.DISTINCT) {
            if (DISTINCT.length() > 0) {
                DISTINCT.append(COMMASPACE);
            }

            DISTINCT.append(seq.toString());
        }

        for (CharSequence seq : this.CREATE) {
            if (CREATE.length() > 0) {
                CREATE.append(COMMASPACE);
            }

            CREATE.append(seq.toString());
        }

        for (CharSequence seq : this.UPDATE) {
            if (UPDATE.length() > 0) {
                UPDATE.append(COMMASPACE);
            }

            UPDATE.append(seq.toString());
        }

        for (CharSequence seq : this.INSERT) {
            if (INSERT.length() > 0) {
                INSERT.append(COMMASPACE);
            }

            INSERT.append(seq.toString());
        }

        for (CharSequence seq : this.INSERT_VALUES) {
            if (INSERT_VALUES.length() > 0) {
                INSERT_VALUES.append(COMMASPACE);
            }

            INSERT_VALUES.append(seq.toString());
        }

        for (CharSequence seq : this.FROM) {
            if (FROM.length() > 0) {
                FROM.append(COMMASPACE);
            }

            FROM.append(seq.toString());
        }

        for (CharSequence seq : this.JOIN) {
            if (JOIN.length() > 0) {
                JOIN.append(NEWLINE);
            }

            JOIN.append(seq.toString());
        }

        for (CharSequence seq : this.WHERE) {
            if (WHERE.length() > 0) {
                WHERE.append(AND);
            }

            WHERE.append(seq.toString());
        }

        for (CharSequence seq : this.GROUP) {
            if (GROUP.length() > 0) {
                GROUP.append(COMMASPACE);
            }

            GROUP.append(seq.toString());
        }

        for (CharSequence seq : this.ORDER) {
            if (ORDER.length() > 0) {
                ORDER.append(COMMASPACE);
            }

            ORDER.append(seq.toString());
        }

        if ( this.OFFSET != null ) {
            OFFSET.append(this.OFFSET);
        }
        if ( this.LIMIT != null ) {
            LIMIT.append(this.LIMIT);
        }

        StringBuilder QUERY = new StringBuilder();
        if ( Is.Ok(FREE) ) {
            QUERY.append(FREE);
        }

        if ( Is.Ok(SELECT) ) {
            QUERY.append("SELECT ");

            if ( Is.Ok(DISTINCT) ) {
                QUERY.append("DISTINCT ON (").append( DISTINCT ).append(") ");
            }

            QUERY.append(SELECT).append(N);
        }

        if ( Is.Ok(CREATE) ) {
            QUERY.append( CREATE ).append(N);
        }

        if ( Is.Ok(INSERT) ) {
            QUERY.append("INSERT INTO ").append(INSERT).append(" VALUES ").append(INSERT_VALUES);
        }

        if ( Is.Ok(UPDATE) ) {
            QUERY.append("UPDATE " + UPDATE).append(N);
        }

        if ( Is.Ok(FROM) ) {
            QUERY.append("FROM ").append(FROM).append(N);
        }

        if ( Is.Ok(JOIN) ) {
            QUERY.append(JOIN).append(N);
        }

        if ( Is.Ok(WHERE) ) {
            QUERY.append("WHERE ").append(WHERE).append(N);
        }

        if ( Is.Ok(GROUP) ) {
            QUERY.append("GROUP BY ").append(GROUP).append(N);
        }

        if ( Is.Ok(ORDER) ) {
            QUERY.append("ORDER BY ").append(ORDER).append(N);
        }

        if ( Is.Ok(OFFSET) ) {
            QUERY.append("OFFSET ").append(OFFSET).append(N);
        }

        if ( Is.Ok(LIMIT) ) {
            QUERY.append("LIMIT ").append(LIMIT).append(N);
        }

        return QUERY.toString().trim();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public interface Params extends $CharSeq {
        String toSQL(CharSequence seq);

        default String toSQL(CharSequence ... seqs) {
            return this.toSQL("", seqs);
        }

        default String toSQL(CharSequence separator, CharSequence... seqs) {
            StringBuilder sb = new StringBuilder();
            if (Is.Ok(seqs) ) {
                for (CharSequence seq : seqs) {
                    if ( !sb.isEmpty() ) {
                        sb.append(separator);
                    }
                    sb.append(toSQL(seq));
                }
            }
            return sb.toString();
        }
    }

    public static class Select implements Params {
        private final String select;

        public Select(CharSequence[] columns) {
            this.select = toSQL(COMMASPACE, columns);
        }

        @Override
        public String toSQL(CharSequence seq) {
            if (seq instanceof Table.Column) {
                return toSQL((Table.Column) seq);
            }

            return seq.toString();
        }

        protected String toSQL(Table.Column column) {
            return column.toSelect();
        }

        @Override
        public String toString() {
            return select;
        }
    }

    public static class Distinct extends Select {
        public Distinct(CharSequence[] columns) {
            super(columns);
        }

        @Override
        protected String toSQL(Table.Column column) {
            return column.toDistinct();
        }
    }


    public static class Join implements Params {
        private final String join;

        public Join(CharSequence table, CharSequence columnA, CharSequence columnB) {
            this.join = "JOIN " + toSQL(table) + " ON " + toSQL(columnA) + " = " + toSQL(columnB);
        }
        public Join(Table.Column columnB, CharSequence columnA) {
            this(columnB.table(), columnA, columnB);
        }

        @Override
        public String toSQL(CharSequence seq) {
            if (seq instanceof Table) {
                return toSQL((Table) seq);
            }
            if (seq instanceof Table.Column) {
                return toSQL((Table.Column) seq);
            }

            return seq.toString();
        }

        protected String toSQL(Table seq) {
            return seq.toJoin();
        }

        protected String toSQL(Table.Column seq) {
            return seq.toJoin();
        }

        @Override
        public String toString() {
            return join;
        }
    }

    public static class Update implements Params {
        private final $Sql<?> outer;

        private final String       table;
        private final List<String> coleqval = new ArrayList<>();

        public Update($Sql<?> outer, CharSequence table) {
            this.outer = outer;
            this.table = toSQL(table);
        }

        public Update value(CharSequence column, Object value) {
            this.coleqval.add( toSQL(column) + "=" + outer.val(value) ); return this;
        }

        @Override
        public String toSQL(CharSequence seq) {
            if (seq instanceof Table) {
                return toSQL((Table) seq);
            }
            if (seq instanceof Table.Column) {
                return toSQL((Table.Column) seq);
            }

            return seq.toString();
        }

        protected String toSQL(Table seq) {
            return seq.toUpdate();
        }

        protected String toSQL(Table.Column seq) {
            return seq.toUpdate();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (String column : coleqval) {
                if ( !sb.isEmpty() ) {
                    sb.append(COMMASPACE);
                }

                sb.append(column);
            }

            return table + " SET " + sb.toString();
        }
    }

    public static class Create implements Params {
        private final $Sql<?> outer;

        private final String       table;
        private final List<String> rows  = new ArrayList<>();
        private       CharSequence owner;

        public Create($Sql<?> outer, CharSequence table) {
            this.outer = outer;
            this.table = toSQL(table);
        }

        public Create row(CharSequence column, CharSequence ... things) {
            StringBuilder sb = new StringBuilder( toSQL(column) );

            if ( Is.Ok(things) ) {
                for (CharSequence thing : things) {
                    if ( !sb.isEmpty() ) {
                        sb.append(TAB);
                    }
                    sb.append( toSQL(thing) );
                }
            }

            this.rows.add(sb.toString());

            return this;
        }

        public Create constraint(CharSequence type, Table.Column ... columns) {
            StringBuilder cols = new StringBuilder();
            for (Table.Column column : columns) {
                if ( !cols.isEmpty() ) {
                    cols.append(COMMASPACE);
                }

                cols.append( toSQL(column) );
            }
            row("CONSTRAINT", (table + SPACE + type).replaceAll("\s", "_").toLowerCase()+ SPACE + type + SPACE + "(" + cols + ")" );

            return this;
        }

        public Create foreign(Table.Column here, Table.Column there) {
            // TODO
            // row("FOREIGN KEY", "(" + toSQL(here) + ")" + "REFERENCES " + toSQL(table) +);
            return this;
        }


        public void owner(CharSequence owner) {
            this.owner = owner;
        }

        @Override
        public String toSQL(CharSequence seq) {
            if (seq instanceof Table) {
                return toSQL((Table) seq);
            }
            if (seq instanceof Table.Column) {
                return toSQL((Table.Column) seq);
            }
            if (seq instanceof Row) {
                return toSQL((Row) seq);
            }

            return seq.toString();
        }

        protected String toSQL(Table seq) {
            return seq.toCreate();
        }

        protected String toSQL(Table.Column seq) {
            return seq.toCreate();
        }

        protected String toSQL(Row seq) {
            return seq.toCreate();
        }

        @Override
        public String toString() {
            StringBuilder rows = new StringBuilder();
            for (String row : this.rows) {
                if ( !rows.isEmpty() ) {
                    rows.append(COMMASPACE).append(NEWLINE);
                }

                rows.append(TAB).append(row);
            }

            StringBuilder finals = new StringBuilder().append("CREATE TABLE IF NOT EXISTS ").append(table).append(SPACE).append("(").append(N).append(rows.toString()).append(N).append(")");
            if  ( Is.Ok(owner) ) {
                finals.append(SEMISPACE).append(N).append("ALTER TABLE ").append(table).append(" OWNER TO ").append(owner).append(SEMI);
            }
            return finals.toString();
        }

        public static final class Row implements Params {

            @Override
            public String toSQL(CharSequence seq) {
                return null;
            }

            public String toCreate() {
                return null;
            }
        }
    }

    public static class Where implements Params {
        private final $Sql<?> outer;
        private final String seqs;

        public Where($Sql<?> outer, CharSequence ... seqs) {
            this.outer = outer;

            this.seqs = toSQL(seqs);
        }

        @Override
        public String toSQL(CharSequence seq) {
            if (seq instanceof Table.Column) {
                return toSQL((Table.Column) seq);
            }

            if (seq instanceof Val) {
                return toSQL((Val) seq);
            }

            return seq.toString();
        }

        protected String toSQL(Table.Column seq) {
            return seq.toWhere();
        }

        protected String toSQL(Val seq) {
            return seq.toWhere();
        }

        @Override
        public String toString() {
            return seqs;
        }
    }

    public static class Insert implements Params {
        private final $Sql<?> outer;

        private final String       table;
        private final List<String> columns = new ArrayList<>();
        private final List<Object> values  = new ArrayList<>();

        public Insert($Sql<?> outer, CharSequence table) {
            this.outer = outer;
            this.table = toSQL(table);
        }

        public Insert columns(CharSequence ... columns) {
            for (CharSequence column : columns) {
                this.columns.add(toSQL(column));
            }
            return this;
        }

        public Insert values(Object ... values) {
            outer.values(values); return this;
        }

        public Insert value(CharSequence column, Object value) {
            this.columns(column);

            this.values.add(value);

            return this;
        }

        @Override
        public String toSQL(CharSequence seq) {
            if (seq instanceof Table) {
                return toSQL((Table) seq);
            }
            if (seq instanceof Table.Column) {
                return toSQL((Table.Column) seq);
            }

            return seq.toString();
        }

        protected String toSQL(Table seq) {
            return seq.toInsert();
        }

        protected String toSQL(Table.Column seq) {
            return seq.toInsert();
        }

        @Override
        public String toString() {
            String insert = table;

            if ( Is.Ok(columns) ) {
                insert += " (" + toSQL(COMMASPACE, columns.toArray(new CharSequence[]{})) + ")";
            }

            if ( Is.Ok( values ) ) {
                outer.values(values.toArray(new Object[]{}));
            }

            return insert;
        }

        public static final class Values implements Params {
            private final String vals;

            public Values(Val ... vals) {
                this.vals = toSQL(COMMASPACE, vals);
            }

            @Override
            public String toSQL(CharSequence seq) {
                return seq.toString();
            }

            @Override
            public String toString() {
                return "(" + vals + ")";
            }
        }
    }

    public static class From implements Params {
        private final String from;

        public From(CharSequence[] tables) {
            this.from = toSQL(tables);
        }

        @Override
        public String toSQL(CharSequence seq) {
            if (seq instanceof Table) {
                return toSQL((Table) seq);
            }

            return seq.toString();
        }

        protected String toSQL(Table seq) {
            return seq.toFrom();
        }

        @Override
        public String toString() {
            return from;
        }
    }

    public static class Group implements Params {
        private final String group;

        public Group(CharSequence[] seqs) {
            this.group = toSQL(COMMASPACE, seqs);
        }

        @Override
        public String toSQL(CharSequence seq) {
            if (seq instanceof Table.Column) {
                return toSQL((Table.Column) seq);
            }
            return seq.toString();
        }

        protected String toSQL(Table.Column seq) {
            return seq.toGroup();
        }

        @Override
        public String toString() {
            return group;
        }
    }

    public static class Order implements Params {
        private final String order;

        public static final Direction ASC = new Direction("ASC"), DESC = new Direction("DESC");

        public Order(CharSequence column) {
            this(column, null);
        }
        public Order(CharSequence column, Direction ascOrDesc) {
            this(column, (CharSequence) ascOrDesc);
        }
        public Order(CharSequence column, CharSequence ascOrDesc) {
            this.order = toSQL(column) + (Is.Ok(ascOrDesc) ? " " + ascOrDesc : "");
        }

        @Override
        public String toSQL(CharSequence seq) {
            if (seq instanceof Table.Column) {
                return toSQL((Table.Column) seq);
            }
            return seq.toString();
        }

        protected String toSQL(Table.Column seq) {
            return seq.toOrder();
        }

        @Override
        public String toString() {
            return order;
        }

        private static class Direction implements $CharSeq {
            private final CharSequence val;

            private Direction(CharSequence seq) {
                this.val = seq;
            }

            @Override
            public String toString() {
                return val.toString();
            }
        }
    }
    
    @Development private static void exampleInsert($Sql<?> sql) {
        Table table = sql.table("myway");

        // Insert example 1. Multiple row insertions
        if ( false ) {
            sql.insert(table, "id", "begun", "completed")
                    .values("zzzzzzzzz", timestamp(), timestamp())
                    .values("xxxxxxxxx", timestamp(), timestamp())
            ;
        }

        // Insert example 2. Multiple row insertions
        if ( false ) {
            sql.insert(table, $-> {
                $
                    .columns("id", "begun", "completed")
                    .values("aaaa", timestamp(), timestamp())
                    .values("bbbb", timestamp(), timestamp())
                ;

            });
        }

        // Insert example 3
        if ( false ) {
            sql.insert(table, $ -> {
                $.value(table.column("id"), "eeessssssssssssssssssssssssssssssssssssssss");
                $.value(table.column("begun"), timestamp());
                $.value(table.column("completed"), timestamp());
            });
        }
    }
    
    @Development private static void exampleUpdate($Sql<?> sql, Table table) {
        sql.update(table, $-> {
            $.value("completed", timestamp());

        }).where("id", "=", sql.val("111"));
    }
    
    
    private static Timestamp timestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
