package momomo.com.db;

import momomo.com.Strings;
import momomo.com.Yarn;

/**
 * @author Joseph S.
 */
public class $SqlColumns {
    private final String   alias;
    private final String[] columns;
    
    public $SqlColumns(String alias, String ... columns ) {
        this.alias   = alias;
        this.columns = columns;
    }
    
    @Override
    public String toString() {
        return Strings.join(Yarn.$(alias, "."), ", ", columns);
    }
    
    public static String toString( $SqlColumns... on ) {
        return Strings.join(", ", on);
    }
}
