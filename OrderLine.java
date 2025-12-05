/**
 * Single line in a customer's cart: item + quantity.
 */
public class OrderLine {
    MenuItem item;
    int qty;

    public OrderLine(MenuItem item, int qty) {
        this.item = item;
        this.qty = qty;
    }

    public double lineTotal() {
        return item.getPrice() * qty;
    }

    @Override
    public String toString() {
        return String.format("%-25s x%2d  %s", item.getName(), qty, Util.formatCurrency(lineTotal()));
    }
}
