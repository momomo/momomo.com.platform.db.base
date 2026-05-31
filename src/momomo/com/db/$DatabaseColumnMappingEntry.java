package momomo.com.db;

import java.util.Objects;

/**
 * @author Joseph S.
 */
public class $DatabaseColumnMappingEntry {
    public final String table, column;
    
    public $DatabaseColumnMappingEntry(CharSequence table, CharSequence column) {
        this.table = table.toString();
        this.column = column.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        $DatabaseColumnMappingEntry entry = ($DatabaseColumnMappingEntry) o;
        return table.equals(entry.table) &&
            column.equals(entry.column);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(table, column);
    }
}
