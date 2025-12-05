import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CompletedOrder {
    CustomerOrder order;
    String customerName;
    LocalDateTime time;
    double total;
    double cash;

    public CompletedOrder(CustomerOrder order, String customerName, LocalDateTime time, double total, double cash) {
        this.order = order;
        this.customerName = customerName;
        this.time = time;
        this.total = total;
        this.cash = cash;
    }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("Time: ").append(time.format(fmt)).append("\n");
        sb.append("Customer: ").append(customerName).append("\n");
        sb.append("Total: " + Util.formatCurrency(total) + "  Cash: " + Util.formatCurrency(cash) + "\n");
        sb.append("Items:\n");
        for (OrderLine ol : order.getLines()) {
            sb.append("  ").append(ol.toString()).append("\n");
        }
        return sb.toString();
    }
}
