package momomo.com.db;

import java.util.HashMap;

/**
 * Used to build a mapping from a column to another
 * 
 * @author Joseph S.
 */
public final class $DatabaseColumnMapping {
    final HashMap<From, To> columns = new HashMap<>();

    protected $DatabaseColumnMapping() {}

    public void put(CharSequence fromTable, CharSequence fromColumn, CharSequence toTable, CharSequence toColumn) {
        this.columns.put( new From(fromTable, fromColumn), new To(toTable, toColumn) );
    }

    public To from(CharSequence table, CharSequence column) {
        return columns.get( new From(table, column) );
    }

    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    
    
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////


    public static final class From extends $DatabaseColumnMappingEntry {
        public From(CharSequence table, CharSequence column) {
            super(table, column);
        }
    }

    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public static final class To extends $DatabaseColumnMappingEntry {
        public To(CharSequence table, CharSequence column) {
            super(table, column);
        }
    }
}
