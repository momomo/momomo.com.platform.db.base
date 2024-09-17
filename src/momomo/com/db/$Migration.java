package momomo.com.db;

/**
 * @author Joseph S.
 */
public abstract class $Migration {
    private final CharSequence id;

    public $Migration(CharSequence id) {
        this.id = id;
    }

    public final CharSequence id() {
        return id;
    }

    public abstract void perform() throws Exception;

}
