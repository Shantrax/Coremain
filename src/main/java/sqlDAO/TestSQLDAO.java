package main.java.sqlDAO;

import main.java.database.HikariCP;
import test.java.SQLDAO.UserOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

/**
 * Mejorar cada uno de los métodos a nivel SQL y código cuando sea necesario
 * Razonar cada una de las mejoras que se han implementado
 * No es necesario que el código implementado funcione
 */
public class TestSQLDAO {

    private static TestSQLDAO testSQLDAO = null;
    private static DataSource dataSource;

    private TestSQLDAO() {
    }

    /*
        Cambié la manera de crear el singleton a una inicialización lazy, para que no se cree al cargar la clase,
        otra manera sería usar el patrón de SingletonHolder que es Thread-safe
     */
    public static TestSQLDAO getInstance() {
        if (testSQLDAO == null) {
            testSQLDAO = new TestSQLDAO();

            HikariCP hikariCP = new HikariCP("coremain");
            dataSource = hikariCP.getDataSource();
        }

        return testSQLDAO;
    }


    /**
     * Obtiene el ID del último pedido para cada usuario.
     *
     * @param shopID Tienda de la que se quiere obtener el listado de usuarios.
     * @return HashMap<Long   ,       Long> Devuelve el mapa con las ids de usuario como key y el maxOrder como value.
     */
    public HashMap<Long, Long> getLastOrderByShopID(long shopID) {
       /*
           Hashtable es más recomendable para aplicaciones multi-thread, si es solo un hilo
            HashMap es más rápido y eficiente.
        */
        HashMap<Long, Long> maxOrderUser = new HashMap<>();

       /*
           Es mejor utilizar las funciones del preparedStatment para insertar los valores.
           A parte de eso, he cambiado la query para que devuelva directamente lo que queremos
           en vez de calcularlo despues con todos los valores.
        */
        String query = "SELECT ID_USUARIO, MAX(ID_PEDIDO) AS MaxOrder FROM pedidos WHERE ID_TIENDA = ? GROUP BY ID_USUARIO";

        /*
            Añado el try-with-resources para que cierre el resultSet y el preparedStmt y devuelva la conexión al pool.
            La conexión siempre hay que devolverla al pool y para cerrar la conexión hay librerias que obligan a
            cerrar el stmt y el resultSet primero ya que no lo hacen automáticamente.
         */
        try (Connection connection = getDBConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, shopID);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        // Cambio las ids de int a long
                        maxOrderUser.put(rs.getLong("ID_USUARIO"), rs.getLong("MaxOrder"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return maxOrderUser;
    }

    /**
     * Copia todos los pedidos de un usuario a otro
     *
     * @param oriUserID  ID del usuario al que copiar los pedidos.
     * @param destUserID ID del usuario al que añadirle los pedidos.
     * @throws SQLException Si falla al mover los pedidos.
     */
    public void copyUserOrders(long oriUserID, long destUserID) throws SQLException {
        // String.format a stmt.set
        String selectQuery = "SELECT FECHA, TOTAL, SUBTOTAL, DIRECCION FROM PEDIDOS WHERE ID_USUARIO = ?;";

        // try-with-resources
        try (Connection selectConnection = getDBConnection();
             PreparedStatement selectStatement = selectConnection.prepareStatement(selectQuery)) {
            selectStatement.setLong(1, oriUserID);

            try (ResultSet selectResultSet = selectStatement.executeQuery()) {
                if (selectResultSet.next()) {
                    moveOrders(selectResultSet, destUserID);
                }
            }
        }
    }

    /**
     * Mueve los pedidos al usuario de destino.
     *
     * @param selectResultSet Pedidos del usuario de origen.
     * @param destUserID      Usuario de destiono.
     */
    private void moveOrders(ResultSet selectResultSet, long destUserID) throws SQLException {
        try  (Connection updateConnection = getDBConnection()) {
            updateConnection.setAutoCommit(false);

            // En vez de insertar uno a uno inserte en batchs ya que es más rápido.
            String insertQuery = "INSERT INTO PEDIDOS (ID_PEDIDO, ID_USUARIO, ID_TIENDA, FECHA, TOTAL, SUBTOTAL, DIRECCION) VALUES (?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement insertStatement = updateConnection.prepareStatement(insertQuery)) {

                while (selectResultSet.next()) {
                    insertStatement.setLong(1, selectResultSet.getLong("ID_PEDIDO"));
                    insertStatement.setLong(2, destUserID);
                    insertStatement.setLong(3, selectResultSet.getLong("ID_TIENDA"));
                    insertStatement.setTimestamp(2, selectResultSet.getTimestamp("FECHA"));
                    insertStatement.setDouble(3, selectResultSet.getDouble("TOTAL"));
                    insertStatement.setDouble(4, selectResultSet.getDouble("SUBTOTAL"));
                    insertStatement.setString(5, selectResultSet.getString("DIRECCION"));
                    insertStatement.addBatch();
                }

                insertStatement.executeBatch();
            } catch (SQLException e) {
                // Hacemos rollback por si fallo en medio de la inserción de pedidos
                updateConnection.rollback();
                updateConnection.setAutoCommit(true);
                throw e;
            }
        }
    }

    /**
     * Obtiene los datos del usuario y pedido con el pedido de mayor importe por tienda
     *
     * @param idTienda Tienda en la que se quiere buscar el pedido con mayor importe.
     * @return Optional<UserOrder> con los datos de la orden y del usuario.
     */
    public Optional<UserOrder> getMostExpensiveUserOrder(long idTienda) {
        UserOrder userOrder = null;

        /*
            String.format a stmt.set
            Cambié la query para traer de base de datos el dato que buscamos directamente
         */
        String query = "SELECT U.ID_USUARIO, P.ID_PEDIDO, P.TOTAL, U.NOMBRE, U.DIRECCION" +
                " FROM PEDIDOS AS P" +
                " INNER JOIN USUARIOS AS U ON P.ID_USUARIO = U.ID_USUARIO" +
                " WHERE P.ID_TIENDA = ?" +
                " AND P.TOTAL IN (SELECT MAX(TOTAL) FROM PEDIDOS GROUP BY ID_USUARIO)";

        // try-with-resources
        try (Connection connection = getDBConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, idTienda);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    userOrder = new UserOrder();
                    userOrder.setAddress(rs.getString("DIRECCION"));
                    userOrder.setUserId(rs.getLong("ID_USUARIO"));
                    userOrder.setOrderId(rs.getLong("ID_PEDIDO"));
                    userOrder.setName(rs.getString("NOMBRE"));
                    userOrder.setTotal(rs.getDouble("TOTAL"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(userOrder);
    }

    /**
     * @return Devuelve una conexión del pool de conexiones
     */
    private Connection getDBConnection() throws SQLException {
        return dataSource.getConnection();
    }
}