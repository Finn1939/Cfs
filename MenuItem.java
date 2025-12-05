public class MenuItem {
    private final int id;
    private final String name;
    private double price;
    private int quantity;
    private final String category;

    /**
     * Create a menu item with unspecified initial stock (defaults to 0).
     *
     * @param id numeric identifier for the item
     * @param name human-readable name
     * @param price item price (decimal)
     * @param category category such as "Coffee", "Pastry", "Addon"
     */
    public MenuItem(int id, String name, double price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.quantity = 0;
    }

    /**
     * Create a menu item and set an initial stock quantity.
     * Useful for bootstrapping inventory.
     */
    public MenuItem(int id, String name, double price, String category, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    /** @return current price */
    public double getPrice() { return price; }

    /** Set a new price for this item. */
    public void setPrice(double price) { this.price = price; }

    /** @return current stock quantity for this item */
    public int getQuantity() { return quantity; }

    /** Set the stock quantity for this item. */
    public void setQuantity(int q) { this.quantity = q; }

    /** @return category string for grouping (e.g. "Coffee", "Addon") */
    public String getCategory() { return category; }

    @Override
    public String toString() {
        String priceStr = String.format("%,.2f", price);
        String displayName = name;
        if (category != null) {
            String c = category.trim().toLowerCase();
            if (c.equals("drink") || c.equals("coffee")) {
                displayName = name + " (16oz)";
            }
        }
        return String.format("%2d. %-25s PHP %6s", id, displayName, priceStr);
    }

    /**
     * Human-readable representation intended for inventory displays (includes quantity).
     */
    public String toInventoryString() {
        String priceStr = String.format("%,.2f", price);
        return String.format("%2d. %-20s PHP %6s   Qty: %3d", id, name, priceStr, quantity);
    }
}
