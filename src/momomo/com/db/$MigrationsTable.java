package momomo.com.db;

import momomo.com.db.$Sql;

/**
 * @author Joseph S.
 */
public interface $MigrationsTable {

    $Sql.Table table();

    default $Sql.Table.Column id() {
        return table().column("id");
    }

    default $Sql.Table.Column start() {
        return table().column("start");
    }

    default $Sql.Table.Column finish() {
        return table().column("finish");
    }

    default $Sql.Table.Column before() {
        return table().column("before");
    }

}

