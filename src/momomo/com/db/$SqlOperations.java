package momomo.com.db;

import java.util.List;

/**
 * @author Joseph S.
 */
public interface $SqlOperations {
    List sqlList  (String query);
    int  sqlUpdate(String query);
}
