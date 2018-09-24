package main.java.sqlDAO;

import test.java.SQLDAO.UserOrder;

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
    private HikariCP hikariCP;

    private TestSQLDAO() {
    }

    /*
        Cambié la manera de crear el singleton a una inicialización lazy, para que no se cree al cargar la clase,
        otra manera sería usar el patrón de SingletonHolder que es Thread-safe.

        De todas maneras en está clase no veo la necesidad de que sea singleton, ya que perfiero usar injección
        para meter el pool de conexiones.
     */
    public static TestSQLDAO getInstance() {
        if (testSQLDAO == null) {
            testSQLDAO = new TestSQLDAO();
        }

        return testSQLDAO;
    }

    public void setHikariCP(HikariCP hikariCP) {
        this.hikariCP = hikariCP;
    }

    /**
     * Obtiene el ID del último pedido para cada usuario.
     *
     * @param shopID Tienda de la que se quiere obtener el listado de usuarios.
     * @return HashMap<Long               ,                               Long> Devuelve el mapa con las ids de usuario como key y el maxOrder como value.
     */
    public HashMap<Integer, Integer> getLastOrderByShopID(long shopID) {
       /*
           Hashtable es más recomendable para aplicaciones multi-thread, si es solo un hilo
            HashMap es más rápido y eficiente.
        */
        HashMap<Integer, Integer> maxOrderUser = new HashMap<>();

       /*
           Es mejor utilizar las funciones del preparedStatment para insertar los valores.
           A parte de eso, he cambiado la query para que devuelva directamente lo que queremos
           en vez de calcularlo despues con todos los valores.
        */
        String query = "SELECT ID_USUARIO, MAX(ID_PEDIDO) AS MaxOrder FROM PEDIDOS WHERE ID_TIENDA = ? GROUP BY ID_USUARIO";

        /*
            Añado el try-with-resources para que cierre el resultSet y el preparedStmt y devuelva la conexión al pool.
            La conexión siempre hay que devolverla al pool y para cerrar la conexión hay librerías que obligan a
            cerrar el stmt y el resultSet primero ya que no lo hacen automáticamente.
         */
        try (Connection connection = hikariCP.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, shopID);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        // Cambio las ids de int a long
                        maxOrderUser.put(rs.getInt("ID_USUARIO"), rs.getInt("MaxOrder"));
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
        // En vez de hacerlo programaticamente lo hago con query
        String insertQuery = "INSERT INTO PEDIDOS(ID_USUARIO, ID_TIENDA, TOTAL, SUBTOTAL, DIRECCION, FECHA)\n" +
                "SELECT " + destUserID + ", ID_TIENDA, TOTAL, SUBTOTAL, DIRECCION, FECHA FROM PEDIDOS WHERE ID_USUARIO = ?";

        // try-with-resources
        try (Connection selectConnection = hikariCP.getConnection();
             PreparedStatement insertStatmente = selectConnection.prepareStatement(insertQuery)) {
            insertStatmente.setLong(1, oriUserID);
            insertStatmente.execute();
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
            Pensé en está query para no tener que calcular el máximo a mano, el problema es que el sub-select no es muy óptimo.

            String query = "SELECT U.ID_USUARIO, P.ID_PEDIDO, P.TOTAL, U.NOMBRE, U.DIRECCION" +
                " FROM PEDIDOS AS P" +
                " INNER JOIN USUARIOS AS U ON P.ID_USUARIO = U.ID_USUARIO" +
                " WHERE P.ID_TIENDA = ?" +
                " AND P.TOTAL = (SELECT MAX(TOTAL) FROM PEDIDOS WHERE ID_TIENDA = P.ID_TIENDA ORDER BY FECHA DESC LIMIT 1)";
         */

        String query = "SELECT U.ID_USUARIO, P.ID_PEDIDO, P.TOTAL, U.NOMBRE, U.DIRECCION" +
                " FROM PEDIDOS AS P" +
                " INNER JOIN USUARIOS AS U ON P.ID_USUARIO = U.ID_USUARIO" +
                " WHERE P.ID_TIENDA = ?";

        // try-with-resources
        try (Connection connection = hikariCP.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, idTienda);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (userOrder == null) {
                        userOrder = new UserOrder();
                        userOrder.setAddress(rs.getString("DIRECCION"));
                        userOrder.setUserId(rs.getInt("ID_USUARIO"));
                        userOrder.setOrderId(rs.getInt("ID_PEDIDO"));
                        userOrder.setName(rs.getString("NOMBRE"));
                        userOrder.setTotal(rs.getDouble("TOTAL"));
                    } else {
                        double total = rs.getDouble("TOTAL");
                        if (total > userOrder.getTotal()) {
                            userOrder.setAddress(rs.getString("DIRECCION"));
                            userOrder.setUserId(rs.getInt("ID_USUARIO"));
                            userOrder.setOrderId(rs.getInt("ID_PEDIDO"));
                            userOrder.setName(rs.getString("NOMBRE"));
                            userOrder.setTotal(total);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Devuelvo Optional porque es cómodo para comprobar si hay resultado no y evitar comprobaciones de null/empty
        return Optional.ofNullable(userOrder);
    }
}