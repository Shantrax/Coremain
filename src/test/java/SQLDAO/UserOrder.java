package test.java.SQLDAO;

// Añado la clase UserOder para devolverla como resultado de getUserMaxOrder()
public class UserOrder {
    private Double total;
    private Long userId;
    private Long orderId;
    private String name;
    private String address;

    public UserOrder() {}

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}