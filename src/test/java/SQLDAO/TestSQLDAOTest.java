package test.java.SQLDAO;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import main.java.sqlDAO.TestSQLDAO;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestSQLDAOTest {

    private TestSQLDAO testSQLDAO = TestSQLDAO.getInstance();

    @Test
    void getMaxUserOrderId() {
        HashMap<Long, Long> maxUserOrderId = testSQLDAO.getLastOrderByShopID(1);
        System.out.println("maxUserOrderId" + maxUserOrderId.toString());
        // Uso optional porque si lo dejará como Long daría un error de ambigüedad con el método assertEquals
        assertEquals(Optional.of(7L), Optional.of(maxUserOrderId.get(1L)));
        assertEquals(Optional.of(17L), Optional.of(maxUserOrderId.get(2L)));
        assertEquals(Optional.empty(), Optional.ofNullable(maxUserOrderId.get(3L)));

        maxUserOrderId = testSQLDAO.getLastOrderByShopID(2);
        System.out.println("maxUserOrderId" + maxUserOrderId.toString());
        assertEquals(Optional.of(9L), Optional.of(maxUserOrderId.get(1L)));
        assertEquals(Optional.of(20L), Optional.of(maxUserOrderId.get(2L)));

        maxUserOrderId = testSQLDAO.getLastOrderByShopID(3);
        System.out.println("maxUserOrderId" + maxUserOrderId.toString());
        assertEquals(Optional.empty(), Optional.ofNullable(maxUserOrderId.get(1L)));
        assertEquals(Optional.empty(), Optional.ofNullable(maxUserOrderId.get(2L)));
    }

    @Test
    void getMostExpensiveUserOrder() {
        Optional<UserOrder> mostExpensiveUserOrder = testSQLDAO.getMostExpensiveUserOrder(1);
        assertTrue(mostExpensiveUserOrder.isPresent());

        UserOrder userOrder = mostExpensiveUserOrder.get();
        assertEquals("Calle Don Pepe", userOrder.getAddress());
        assertEquals(Optional.of(12L), Optional.of(userOrder.getOrderId()));
        assertEquals(Optional.of(2L), Optional.of(userOrder.getUserId()));
        assertEquals(Optional.of(754d), Optional.of(userOrder.getTotal()));
        assertEquals("Manolo", userOrder.getName());

        mostExpensiveUserOrder = testSQLDAO.getMostExpensiveUserOrder(2);
        assertTrue(mostExpensiveUserOrder.isPresent());

        userOrder = mostExpensiveUserOrder.get();
        assertEquals("Calle Don Manolo", userOrder.getAddress());
        assertEquals(Optional.of(10L), Optional.of(userOrder.getOrderId()));
        assertEquals(Optional.of(1L), Optional.of(userOrder.getUserId()));
        assertEquals(Optional.of(523d), Optional.of(userOrder.getTotal()));
        assertEquals("Pepe", userOrder.getName());

        mostExpensiveUserOrder = testSQLDAO.getMostExpensiveUserOrder(3);
        assertFalse(mostExpensiveUserOrder.isPresent());
    }

}
