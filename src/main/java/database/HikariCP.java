package main.java.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class HikariCP {
    private static DataSource datasource;

    private String databaseName;

    public HikariCP (String databaseName) {
        this.databaseName = databaseName;
    }

    public DataSource getDataSource() {
        if (datasource == null) {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl("jdbc:mysql://localhost/" + databaseName + "?serverTimezone=UTC&autoReconnect=true&useSSL=false");
            config.setUsername("root");
            config.setPassword("C0nv3rf1t@1nf0");
            config.setMaximumPoolSize(10);
            config.setAutoCommit(true);
            datasource = new HikariDataSource(config);
        }

        return datasource;
    }

}
