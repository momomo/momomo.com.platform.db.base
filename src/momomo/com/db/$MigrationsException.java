package momomo.com.db;

import momomo.com.exceptions.$DatabaseException;

/**
 * @author Joseph S.
 */
public final class $MigrationsException extends $DatabaseException {
    public $MigrationsException(CharSequence is, CharSequence before) {
        super(
            "Aborting all migrations since this migration with id '" + is + "' does not follow what should be '" + before + "'. \n" + """
            This is not allowed by default since it could cause more damage than good. 
                                            
            Migrations should come in sequence, one after each other and once applied it would be dangerous to put something in betweeen past ones or rename past migrations.
                                    
            All migrations will now be rolled back.
            """);
    }
}
