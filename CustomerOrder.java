import java.util.ArrayList;
import java.util.List;

public class CustomerOrder {
    private final List<OrderLine> lines = new ArrayList<>();

    /** Add an item to the cart. If the same item id is already present, increment the quantity. */
    public void addItem(MenuItem item, int qty) {
        for (OrderLine ol : lines) {
            if (ol.item.getId() == item.getId()) {
                ol.qty += qty;
                return;
            }
        }
        lines.add(new OrderLine(item, qty));
    }

    /** Remove the line at 1-based index from the cart. Returns true if removed. */
    public boolean removeItem(int index) {
        if (index < 1 || index > lines.size()) return false;
        lines.remove(index - 1);
        return true;
    }

    /** Update the quantity of a cart line (1-based index). */
    public boolean updateQuantity(int index, int newQty) {
        if (index < 1 || index > lines.size()) return false;
        if (newQty <= 0) return false;
        lines.get(index - 1).qty = newQty;
        return true;
    }

    /** Calculate the total price for the current cart (sum of line totals). */
    public double getTotal() {
        double sum = 0;
        for (OrderLine ol : lines) sum += ol.lineTotal();
        return sum;
    }

    /** @return true when the cart contains no lines */
    public boolean isEmpty() {
        return lines.isEmpty();
    }

    /** Remove all lines from the cart. */
    public void clear() {
        lines.clear();
    }

    /** Print the cart contents and total to stdout. */
    public void view() {
        if (lines.isEmpty()) {
            System.out.println("\nYour cart is empty.\n");
            return;
        }
        System.out.println("\n--- Your Cart ---");
        int i = 1;
        for (OrderLine ol : lines) {
            System.out.printf("%2d. %s%n", i++, ol.toString());
        }
        System.out.println("Total: " + Util.formatCurrency(getTotal()));
        System.out.println();
    }

    /** Return the live list of order lines. */
    public List<OrderLine> getLines() {
        return lines;
    }

    /** Create a snapshot copy of this order. */
    public CustomerOrder copy() {
        CustomerOrder c = new CustomerOrder();
        for (OrderLine ol : lines) {
            c.lines.add(new OrderLine(ol.item, ol.qty));
        }
        return c;
    }
}
