package test.java.SQLDAO;

import main.java.sqlDAO.HikariCP;
import main.java.sqlDAO.TestSQLDAO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class TestSQLDAOTest {

    private static TestSQLDAO testSQLDAO = TestSQLDAO.getInstance();

    private static HikariCP hikariCP;

    @BeforeAll
    static void setUp() throws SQLException {
        /*
            Normalmente o mockearía la base de datos o utilizaría una base de datos en memoria
            en caso de necesitar un testeo de las queries más real, en este caso para pruebas
            simplemente utilizo una base de datos de testing.
         */

        hikariCP = new HikariCP();
        hikariCP.connectTo("test-coremain");

        testSQLDAO.setHikariCP(hikariCP);

        try (Connection conn = hikariCP.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS PEDIDOS\n" +
                    "(" +
                    "  ID_USUARIO INT        NULL,\n" +
                    "  ID_TIENDA  INT        NULL,\n" +
                    "  ID_PEDIDO  INT        AUTO_INCREMENT PRIMARY KEY,\n" +
                    "  TOTAL      DOUBLE     NULL,\n" +
                    "  SUBTOTAL   DOUBLE     NULL,\n" +
                    "  DIRECCION  TEXT       NULL,\n" +
                    "  FECHA      TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP\n" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS USUARIOS\n" +
                    "(" +
                    "  ID_USUARIO INT        AUTO_INCREMENT PRIMARY KEY,\n" +
                    "  NOMBRE     TEXT       NULL,\n" +
                    "  DIRECCION  TEXT       NULL\n" +
                    ")");

            // Esto lo añado por si petaron los tests en algún punto y no se vació la base de datos
            stmt.execute("TRUNCATE TABLE PEDIDOS;");
            stmt.execute("TRUNCATE TABLE USUARIOS;");

            File users = new File("./src/test/resources/usuarios.csv");
            stmt.executeQuery("LOAD DATA INFILE '" + users.getAbsolutePath() + "' INTO TABLE USUARIOS " +
                    " FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' IGNORE 1 LINES;");

            File orders = new File("./src/test/resources/pedidos.csv");
            stmt.executeQuery("LOAD DATA INFILE '" + orders.getAbsolutePath() + "' INTO TABLE PEDIDOS " +
                    "FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n' IGNORE 1 LINES\n" +
                    "(ID_PEDIDO,ID_USUARIO,ID_TIENDA,TOTAL,SUBTOTAL,DIRECCION); ");
        }

    }

    @Test
    void getMaxUserOrderId() {
        HashMap<Integer, Integer> maxUserOrderId = testSQLDAO.getLastOrderByShopID(1);
        System.out.println("maxUserOrderId" + maxUserOrderId.toString());
        // Uso optional porque si lo dejará como Long daría un error de ambigüedad con el método assertEquals
        assertEquals(Optional.of(7), Optional.ofNullable(maxUserOrderId.get(1)));
        assertEquals(Optional.of(17), Optional.ofNullable(maxUserOrderId.get(2)));
        assertEquals(Optional.empty(), Optional.ofNullable(maxUserOrderId.get(5)));

        maxUserOrderId = testSQLDAO.getLastOrderByShopID(2);
        System.out.println("maxUserOrderId" + maxUserOrderId.toString());
        assertEquals(Optional.of(10), Optional.ofNullable(maxUserOrderId.get(1)));
        assertEquals(Optional.of(20), Optional.ofNullable(maxUserOrderId.get(2)));

        maxUserOrderId = testSQLDAO.getLastOrderByShopID(3);
        System.out.println("maxUserOrderId" + maxUserOrderId.toString());
        assertEquals(Optional.empty(), Optional.ofNullable(maxUserOrderId.get(1)));
        assertEquals(Optional.empty(), Optional.ofNullable(maxUserOrderId.get(2)));
    }

    @Test
    @DisplayName("Para la tienda 1 el valor más caro debería ser 754")
    void getMostExpensiveUserOrderOne() {
        Optional<UserOrder> mostExpensiveUserOrder = testSQLDAO.getMostExpensiveUserOrder(1);
        assertTrue(mostExpensiveUserOrder.isPresent());

        UserOrder userOrder = mostExpensiveUserOrder.get();
        assertEquals("Calle Most Expensive", userOrder.getAddress());
        assertEquals(Optional.of(30), Optional.ofNullable(userOrder.getOrderId()));
        assertEquals(Optional.of(6), Optional.ofNullable(userOrder.getUserId()));
        assertEquals(Optional.of(1000d), Optional.ofNullable(userOrder.getTotal()));
        assertEquals("MostExpensiveUser", userOrder.getName());
    }

    @Test
    @DisplayName("Para la tienda 2 el valor más caro debería ser 523")
    void getMostExpensiveUserOrderTwo() {
        Optional<UserOrder> mostExpensiveUserOrder = testSQLDAO.getMostExpensiveUserOrder(2);
        assertTrue(mostExpensiveUserOrder.isPresent());

        UserOrder userOrder = mostExpensiveUserOrder.get();
        assertEquals("Calle Don Manolo", userOrder.getAddress());
        assertEquals(Optional.of(10), Optional.ofNullable(userOrder.getOrderId()));
        assertEquals(Optional.of(1), Optional.ofNullable(userOrder.getUserId()));
        assertEquals(Optional.of(523d), Optional.ofNullable(userOrder.getTotal()));
        assertEquals("Pepe", userOrder.getName());
    }

    @Test
    @DisplayName("Para la tienda 5 no tiene valor máximo, devuelve Optional.empty()")
    void getMostExpensiveUserOrderThree() {
        Optional<UserOrder> mostExpensiveUserOrder = testSQLDAO.getMostExpensiveUserOrder(5);
        assertFalse(mostExpensiveUserOrder.isPresent());
    }

    @Test
    @DisplayName("Probando a copiar ordenes del usuario 3 al 4")
    void copyOrdersBetweenUsersOne() throws SQLException {
        try (Connection conn = hikariCP.getConnection();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM PEDIDOS WHERE ID_USUARIO = 4")) {
                assertTrue(!rs.next());
            }

            List<Order> ordersFromUserWithOrders;
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM PEDIDOS WHERE ID_USUARIO = 3")) {
                assertTrue(rs.next());
                ordersFromUserWithOrders = saveOrders(rs);
                assertEquals(9, ordersFromUserWithOrders.size());
            }

            testSQLDAO.copyUserOrders(3, 4);

            try (ResultSet rs = stmt.executeQuery("SELECT * FROM PEDIDOS WHERE ID_USUARIO = 4")) {
                assertTrue(rs.next());
                List<Order> copiedOrders = saveOrders(rs);

                AtomicBoolean containsAll = new AtomicBoolean(true);
                for (Order ordersFromUserWithOrder : ordersFromUserWithOrders) {
                    boolean containsIt = copiedOrders.stream().anyMatch(copiedOrder ->
                            copiedOrder.getAddress().equals(ordersFromUserWithOrder.getAddress()) &&
                                    copiedOrder.getTimestamp().equals(ordersFromUserWithOrder.getTimestamp()) &&
                                    copiedOrder.getShopId() == ordersFromUserWithOrder.getShopId() &&
                                    Double.compare(copiedOrder.getTotal(), ordersFromUserWithOrder.getTotal()) > -1 &&
                                    Double.compare(copiedOrder.getSubtotal(), ordersFromUserWithOrder.getSubtotal()) > -1);
                    System.out.println("Contains it " + containsIt);
                    if (!containsIt) {
                        containsAll.set(false);
                        break;
                    }
                }

                assertTrue(containsAll.get());
                assertEquals(ordersFromUserWithOrders.size(), copiedOrders.size());
            }
        }
    }

    @Test
    @DisplayName("Probando a copiar ordenes desde un usuario que no tiene")
    void copyOrdersBetweenUsersTwo() throws SQLException {
        try (Connection conn = hikariCP.getConnection();
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM PEDIDOS WHERE ID_USUARIO = 5")) {
                assertTrue(!rs.next());
            }

            List<Order> ordersFromUserWithOrders;
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM PEDIDOS WHERE ID_USUARIO = 3")) {
                assertTrue(rs.next());
                ordersFromUserWithOrders = saveOrders(rs);
                assertEquals(9, ordersFromUserWithOrders.size());
            }

            testSQLDAO.copyUserOrders(5, 3);

            try (ResultSet rs = stmt.executeQuery("SELECT * FROM PEDIDOS WHERE ID_USUARIO = 3")) {
                assertTrue(rs.next());
                ordersFromUserWithOrders = saveOrders(rs);
                assertEquals(9, ordersFromUserWithOrders.size());
            }

            try (ResultSet rs = stmt.executeQuery("SELECT * FROM PEDIDOS WHERE ID_USUARIO = 5")) {
                assertTrue(!rs.next());
            }
        }
    }

    private List<Order> saveOrders(ResultSet rs) throws SQLException {
        List<Order> orders = new ArrayList<>();
        do {
            Order order = new Order();
            order.setTimestamp(rs.getTimestamp("FECHA"));
            order.setAddress(rs.getString("DIRECCION"));
            order.setSubtotal(rs.getLong("SUBTOTAL"));
            order.setOrderId(rs.getLong("ID_PEDIDO"));
            order.setUserId(rs.getLong("ID_USUARIO"));
            order.setShopId(rs.getLong("ID_TIENDA"));
            orders.add(order);
        } while (rs.next());

        return orders;
    }

    @AfterAll
    static void tearDown() throws SQLException {
        try (Connection conn = hikariCP.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE USUARIOS;");
            stmt.execute("DROP TABLE PEDIDOS;");
        }
    }

}
