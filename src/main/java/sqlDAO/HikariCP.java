package main.java.sqlDAO;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class HikariCP {
    private static Logger logger = LoggerFactory.getLogger(HikariCP.class);

    private static DataSource datasource;

    public HikariCP () {}

    public void connectTo(String database) {
        logger.info("Init connection to database '" + database + "'");

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://localhost/" + database + "?serverTimezone=UTC&autoReconnect=true&useSSL=false");
        config.setUsername("root");
        config.setPassword("password");
        config.setMaximumPoolSize(10);
        config.setAutoCommit(true);
        datasource = new HikariDataSource(config);
    }

    // El encargado de dar las conexiones debería estar en la clase que las maneja, y no en las demás
    public Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }

}
