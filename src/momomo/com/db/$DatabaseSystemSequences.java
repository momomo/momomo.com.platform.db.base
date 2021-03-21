package momomo.com.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Temporary, needs to be revised. Method names might change in the future. 
 * 
 * @author Joseph S.
 */
public interface $DatabaseSystemSequences extends $DatabaseSystem {
    
    String sqlListSequences();
    
    default ArrayList<String> sequencesList() {
        ArrayList<String> sequences = new ArrayList<>();
        this.sql(sqlListSequences(), ($SqlResultSet rs) -> {
            List<$SqlResultSet.Row> rows = rs.getRows();
            for ($SqlResultSet.Row row : rows) {
                for ($SqlResultSet.Row.Column column : row.keys()) {
                    sequences.add(column.val().toString());
                }
            }
        });
        
        return sequences;
    }
    
    
    default void sequencesDrop() {
        sequencesDrop( sequencesList().toArray(new CharSequence[]{}) );
    }
    
}
