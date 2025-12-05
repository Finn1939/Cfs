import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.File;

public class MenuManager {
    private final List<MenuItem> items = new ArrayList<>();
    // Explicit list of known categories (allows creating a category without adding an item)
    private final List<String> categories = new ArrayList<>();
    private int nextId = 1;
    // When true, suppress automatic persistence while loading from disk
    private boolean loading = false;

    public MenuManager() {
        // Try loading menu from disk first; fall back to defaults when unavailable
        File f = new File("menu.txt");
        if (!loadMenuFromFile(f)) {
            loadDefaultMenu();
        }
    }

    /**
     * Populate the menu with default items and initial stock quantities.
     * This is a convenience for the sample app and can be modified later.
     */
    private void loadDefaultMenu() {
        // Initialize known categories and default items with sensible stock quantities
        addCategory("Coffee");
        addCategory("Drink");
        addCategory("Pastry");
        addCategory("Cookie");
        addCategory("Snack");
        addCategory("Addon");
        // Coffee
        items.add(new MenuItem(nextId++, "Americano", 120, "Coffee", 50));
        items.add(new MenuItem(nextId++, "Latte", 140, "Coffee", 50));
        items.add(new MenuItem(nextId++, "Cappuccino", 150, "Coffee", 50));
        items.add(new MenuItem(nextId++, "Espresso", 100, "Coffee", 60));
        items.add(new MenuItem(nextId++, "Mocha", 160, "Coffee", 50));

        // Drinks
        items.add(new MenuItem(nextId++, "Hot Chocolate", 95, "Drink", 40));
        items.add(new MenuItem(nextId++, "Milk Tea", 95, "Drink", 40));
        items.add(new MenuItem(nextId++, "Iced Tea", 70, "Drink", 40));
        items.add(new MenuItem(nextId++, "Orange Juice", 85, "Drink", 40));

        // Pastries / Bread
        items.add(new MenuItem(nextId++, "Croissant", 80, "Pastry", 30));
        items.add(new MenuItem(nextId++, "Banana Bread", 85, "Pastry", 30));
        items.add(new MenuItem(nextId++, "Cinnamon Roll", 90, "Pastry", 30));

        // Cookies / Snacks
        items.add(new MenuItem(nextId++, "Chocolate Chip Cookie", 60, "Cookie", 50));
        items.add(new MenuItem(nextId++, "Oatmeal Cookie", 55, "Cookie", 50));
        items.add(new MenuItem(nextId++, "Ham Sandwich", 85, "Snack", 30));

        // Add-ons
        items.add(new MenuItem(nextId++, "Vanilla Syrup", 20, "Addon", 100));
        items.add(new MenuItem(nextId++, "Caramel Syrup", 20, "Addon", 100));
        items.add(new MenuItem(nextId++, "Chocolate Sauce", 25, "Addon", 100));
        items.add(new MenuItem(nextId++, "Whipped Cream", 30, "Addon", 100));
    }

    /**
     * Add a new category to the catalog. Returns false when category is empty or exists.
     */
    public boolean addCategory(String category) {
        if (category == null) return false;
        String c = category.trim();
        if (c.isEmpty()) return false;
        for (String ex : categories) {
            if (ex.equalsIgnoreCase(c)) return false;
        }
        // Ensure 'Addon' category stays at the end. If 'Addon' exists, insert new
        // categories before it so Addon remains last in the list.
        if (containsIgnoreCase(categories, "Addon") && !c.equalsIgnoreCase("Addon")) {
            // find index of existing Addon
            int idx = -1;
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).equalsIgnoreCase("Addon")) { idx = i; break; }
            }
            if (idx >= 0) {
                categories.add(idx, c);
            } else {
                categories.add(c);
            }
        } else {
            // add normally (this places Addon at end when adding it)
            categories.add(c);
        }
        return true;
    }

    /** Return the known categories in insertion order. */
    public List<String> getCategories() {
        return new ArrayList<>(categories);
    }

    public List<MenuItem> getItems() {
        return items;
    }

    /**
     * Add a new item to the menu and inventory.
     * @return the created MenuItem
     */
    public MenuItem addNewItem(String name, double price, String category, int quantity) {
        // ensure category is known
        if (category != null) {
            String catTrim = category.trim();
            boolean found = false;
            for (String c : categories) if (c.equalsIgnoreCase(catTrim)) { found = true; break; }
            if (!found) {
                // reuse addCategory logic to preserve Addon ordering and uniqueness
                addCategory(catTrim);
            }
        }
        MenuItem mi = new MenuItem(nextId++, name, price, category, quantity);
        items.add(mi);
        if (!loading) persist();
        return mi;
    }

    /** Update the price of the item with given id. */
    public boolean updatePrice(int id, double newPrice) {
        MenuItem mi = findById(id);
        if (mi == null) return false;
        mi.setPrice(newPrice);
        if (!loading) persist();
        return true;
    }

    /** Increase stock for an item. */
    public boolean restock(int id, int qty) {
        MenuItem mi = findById(id);
        if (mi == null) return false;
        mi.setQuantity(mi.getQuantity() + qty);
        if (!loading) persist();
        return true;
    }

    /** Decrement stock when items are sold. Returns false if not enough stock. */
    public boolean decrementStock(int id, int qty) {
        MenuItem mi = findById(id);
        if (mi == null) return false;
        if (mi.getQuantity() < qty) return false;
        mi.setQuantity(mi.getQuantity() - qty);
        if (!loading) persist();
        return true;
    }

    /** Return a snapshot of all menu items for inventory display. */
    public List<MenuItem> getInventory() {
        return new ArrayList<>(items);
    }

    /**
     * Remove an item by id from the menu and inventory. Returns true when removed.
     */
    public boolean removeItemById(int id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == id) {
                items.remove(i);
                if (!loading) persist();
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all items belonging to the given category. Returns number of items removed.
     */
    public int removeItemsByCategory(String category) {
        if (category == null) return 0;
        String c = category.trim();
        if (c.isEmpty()) return 0;
        int removed = 0;
        for (int i = items.size() - 1; i >= 0; i--) {
            MenuItem mi = items.get(i);
            if (mi.getCategory() != null && mi.getCategory().equalsIgnoreCase(c)) {
                items.remove(i);
                removed++;
            }
        }
        if (removed > 0 && !loading) persist();
        return removed;
    }

    public void displayMenu() {
        if (items.isEmpty()) {
            System.out.println("Menu is empty.");
            return;
        }
        System.out.println("\n========== MENU ==========");
        // Print categories in known order, then any stray categories from items
        Map<String, List<MenuItem>> groups = new LinkedHashMap<>();
        for (MenuItem item : items) {
            groups.computeIfAbsent(item.getCategory(), k -> new ArrayList<>()).add(item);
        }
        // Print known categories first
        for (String cat : categories) {
            List<MenuItem> list = groups.get(cat);
            if (list == null || list.isEmpty()) continue;
            System.out.println();
            System.out.println("-- " + cat + " --");
            for (MenuItem mi : list) System.out.println(mi.toInventoryString());
            groups.remove(cat);
        }
        // Print any remaining categories discovered from items
        for (Map.Entry<String, List<MenuItem>> e : groups.entrySet()) {
            System.out.println();
            System.out.println("-- " + e.getKey() + " --");
            for (MenuItem mi : e.getValue()) System.out.println(mi.toInventoryString());
        }
        System.out.println("==========================\n");
    }

    /**
     * Interactive display: show categories as selectable options, then display chosen group or all.
     */
    public void displayMenu(Scanner sc) {
        CoffeeShopSystem.clearConsole();
        if (items.isEmpty()) {
            System.out.println("Menu is empty.");
            return;
        }

        // Group items by category preserving insertion order; start from known categories
        Map<String, List<MenuItem>> groups = new LinkedHashMap<>();
        for (MenuItem item : items) {
            groups.computeIfAbsent(item.getCategory(), k -> new ArrayList<>()).add(item);
        }
        List<String> categories = new ArrayList<>(this.categories);
        // append any discovered categories not in the known list
        for (String k : groups.keySet()) if (!containsIgnoreCase(categories, k)) categories.add(k);

        System.out.println();
        System.out.println();
        System.out.println("\n========== MENU ==========");
        System.out.println("0. All");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, categories.get(i));
        }
        System.out.print("Select category (0=All, S=Search): ");

        String selLine;
        while (true) {
            String raw = sc.nextLine();
            if (raw == null) {
                System.out.print("Please enter a valid number or 'S' to search: ");
                continue;
            }
            if (!raw.equals(raw.trim()) || raw.matches(".*\\s.*")) {
                System.out.print("Input must not contain whitespace. Enter a number or 'S' to search: ");
                continue;
            }
            selLine = raw;
            if (selLine.equalsIgnoreCase("s")) {
                System.out.print("Enter keyword (or 'b' to cancel): ");
                String kwRaw = sc.nextLine();
                if (kwRaw == null || kwRaw.equalsIgnoreCase("b") || !kwRaw.equals(kwRaw.trim()) || kwRaw.matches(".*\\s.*")) {
                    System.out.println("Search cancelled or invalid keyword.\n");
                    return;
                }
                String kw = kwRaw;
                List<MenuItem> results = search(kw);
                if (results.isEmpty()) {
                    System.out.println("No results for: " + kw + "\n");
                } else {
                    System.out.println("\n-- Search Results --");
                    for (MenuItem mi : results) System.out.println(mi.toInventoryString());
                    System.out.println();
                }
                return;
            }
            try {
                Integer.parseInt(selLine);
                break;
            } catch (Exception e) {
                System.out.print("Please enter a valid number or 'S' to search: ");
            }
        }

        int sel = Integer.parseInt(selLine);

        System.out.println();
        if (sel == 0) {
            for (String cat : categories) {
                System.out.println();
                System.out.println("-- " + cat + " --");
                List<MenuItem> list = groups.get(cat);
                if (list != null) for (MenuItem mi : list) System.out.println(mi.toInventoryString());
            }
        } else if (sel >= 1 && sel <= categories.size()) {
            String cat = categories.get(sel - 1);
            System.out.println("\n-- " + cat + " --");
            List<MenuItem> list = groups.get(cat);
            if (list != null) for (MenuItem mi : list) System.out.println(mi.toInventoryString());
        } else {
            System.out.println("Invalid selection.");
        }

        System.out.println("==========================\n");
    }

    /**
     * Let user browse categories and then pick an item ID to return (0 = cancel).
     */
    public int selectItem(Scanner sc) {
        CoffeeShopSystem.clearConsole();
        if (items.isEmpty()) return 0;

        Map<String, List<MenuItem>> groups = new LinkedHashMap<>();
        for (MenuItem item : items) {
            groups.computeIfAbsent(item.getCategory(), k -> new ArrayList<>()).add(item);
        }
        List<String> categories = new ArrayList<>(groups.keySet());

        while (true) {
            System.out.println("\n========== MENU ==========");
            System.out.println("0. All");
            for (int i = 0; i < categories.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, categories.get(i));
            }
            System.out.print("Select category (0=All, S=Search, b=cancel): ");

            String selLine = sc.nextLine();
            if (selLine == null) { System.out.println("Please enter a valid selection."); continue; }
            if (!selLine.equals(selLine.trim()) || selLine.matches(".*\\s.*")) { System.out.println("Input must not contain whitespace."); continue; }
            if (selLine.equalsIgnoreCase("b")) return 0;

            if (selLine.equalsIgnoreCase("s")) {
                System.out.print("Enter keyword (or 'b' to cancel): ");
                String kwRaw = sc.nextLine();
                if (kwRaw == null || kwRaw.equalsIgnoreCase("b") || !kwRaw.equals(kwRaw.trim()) || kwRaw.matches(".*\\s.*")) {
                    System.out.println("Search cancelled or invalid keyword.\n");
                    continue;
                }
                List<MenuItem> results = search(kwRaw);
                if (results.isEmpty()) {
                    System.out.println("No results for: " + kwRaw + "\n");
                    continue;
                }
                System.out.println("\n-- Search Results --");
                for (MenuItem mi : results) System.out.println(mi.toInventoryString());
                System.out.println();
                System.out.print("Enter item number to Order (0 to cancel, b to go back): ");
                while (true) {
                    String lineRaw = sc.nextLine();
                    if (lineRaw == null) { System.out.print("Please enter a valid item number (0 to cancel, b to go back): "); continue; }
                    if (!lineRaw.equals(lineRaw.trim()) || lineRaw.matches(".*\\s.*")) { System.out.print("Input must not contain whitespace. Enter item number or 'b': "); continue; }
                    if (lineRaw.equalsIgnoreCase("b")) break; // go back to category selection
                    try {
                        int id = Integer.parseInt(lineRaw);
                        return id;
                    } catch (Exception e) {
                        System.out.print("Please enter a valid item number (0 to cancel, b to go back): ");
                    }
                }
                continue;
            }

            int sel;
            try {
                sel = Integer.parseInt(selLine);
            } catch (Exception e) {
                System.out.println("Please enter a valid number or 'b' to cancel.");
                continue;
            }

            System.out.println();
            if (sel == 0) {
                for (String cat : categories) {
                    System.out.println();
                    System.out.println("-- " + cat + " --");
                    List<MenuItem> list = groups.get(cat);
                    if (list != null) for (MenuItem mi : list) System.out.println(mi.toInventoryString());
                }
            } else if (sel >= 1 && sel <= categories.size()) {
                String cat = categories.get(sel - 1);
                System.out.println("\n-- " + cat + " --");
                List<MenuItem> list = groups.get(cat);
                if (list != null) for (MenuItem mi : list) System.out.println(mi.toInventoryString());
            } else {
                System.out.println("Invalid selection.");
                continue; // re-show categories
            }

            // Prompt for item id. Allow 'b' to go back to category selection and 0 to cancel.
            System.out.print("Enter item number to Order (0 to cancel, b to go back): ");
            while (true) {
                String rawLine = sc.nextLine();
                if (rawLine == null) { System.out.print("Please enter a valid item number (0 to cancel, b to go back): "); continue; }
                if (!rawLine.equals(rawLine.trim()) || rawLine.matches(".*\\s.*")) { System.out.print("Input must not contain whitespace. Enter item number or 'b': "); continue; }
                if (rawLine.equalsIgnoreCase("b")) break;
                try {
                    int id = Integer.parseInt(rawLine);
                    return id;
                } catch (Exception e) {
                    System.out.print("Please enter a valid item number (0 to cancel, b to go back): ");
                }
            }
            // if we reach here, user requested 'b' to go back — loop to show categories again
        }
    }

    /**
     * Let user select add-ons (syrups, sauces, etc.). Returns list of [id, qty] pairs.
     */
    public List<int[]> selectAddons(Scanner sc) {
        CoffeeShopSystem.clearConsole();
        List<int[]> res = new ArrayList<>();
        List<MenuItem> addons = new ArrayList<>();
        for (MenuItem it : items) {
            if ("Addon".equalsIgnoreCase(it.getCategory())) addons.add(it);
        }
        if (addons.isEmpty()) {
            System.out.println("No add-ons available.");
            return res;
        }

        System.out.println("\n-- Add-Ons --");
        for (MenuItem mi : addons) System.out.println(mi.toString());

        while (true) {
            System.out.print("Enter add-on ID to add (0 = done): ");
            String line = sc.nextLine();
            String trimmed = line.trim();
            if (!trimmed.matches("^[0-9]+$")) {
                System.out.println("Please enter a valid add-on ID (digits only).");
                continue;
            }
            int id = Integer.parseInt(trimmed);
            if (id == 0) break;
            MenuItem chosen = findById(id);
            if (chosen == null || !"Addon".equalsIgnoreCase(chosen.getCategory())) {
                System.out.println("Invalid add-on ID.");
                continue;
            }
            System.out.print("Enter quantity: ");
            int qty;
            String qlineRaw = sc.nextLine();
            if (qlineRaw == null || !qlineRaw.equals(qlineRaw.trim()) || qlineRaw.matches(".*\\s.*") || !qlineRaw.matches("^[0-9]+$")) {
                System.out.println("Please enter a valid quantity (digits only).");
                continue;
            }
            qty = Integer.parseInt(qlineRaw);
            if (qty <= 0) {
                System.out.println("Quantity must be at least 1.");
                continue;
            }
            res.add(new int[] { id, qty });
        }
        return res;
    }

    public MenuItem findById(int id) {
        for (MenuItem item : items) {
            if (item.getId() == id) return item;
        }
        return null;
    }

    public List<MenuItem> search(String keyword) {
        keyword = keyword.toLowerCase();
        List<MenuItem> results = new ArrayList<>();
        for (MenuItem item : items) {
            if (item.getName().toLowerCase().contains(keyword) ||
                item.getCategory().toLowerCase().contains(keyword)) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * Remove a category by name. Returns true when removed.
     * If any items belong to the category, the removal is refused.
     */
    public boolean removeCategory(String category) {
        if (category == null) return false;
        String c = category.trim();
        if (c.isEmpty()) return false;
        // refuse to remove when items exist in this category
        for (MenuItem mi : items) {
            if (mi.getCategory() != null && mi.getCategory().equalsIgnoreCase(c)) return false;
        }
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).equalsIgnoreCase(c)) {
                categories.remove(i);
                persist();
                return true;
            }
        }
        return false;
    }

    /** Persist the current menu to the default `menu.txt` file (best-effort). */
    private void persist() {
        try {
            saveMenuToFile(new File("menu.txt"));
        } catch (Exception e) {
            // best-effort persistence: ignore failures
        }
    }

    /** Save menu/categories to a text file. Format:
     *  CATEGORIES:cat1|cat2|...
     *  ITEM:id|name|price|category|quantity
     */
    public boolean saveMenuToFile(File f) throws Exception {
        List<String> lines = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("CATEGORIES:");
        for (int i = 0; i < categories.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(categories.get(i));
        }
        lines.add(sb.toString());
        for (MenuItem mi : items) {
            String nm = mi.getName().replace("|", "/");
            String cat = mi.getCategory() == null ? "" : mi.getCategory();
            String itemLine = String.format("ITEM:%d|%s|%s|%s|%d", mi.getId(), nm, Double.toString(mi.getPrice()), cat, mi.getQuantity());
            lines.add(itemLine);
        }
        TextFileDatabase.saveLines(f, lines);
        return true;
    }

    /** Load menu and categories from a previously-saved file. Returns false when file missing or parse failed. */
    public boolean loadMenuFromFile(File f) {
        if (f == null || !f.exists()) return false;
        try {
            loading = true;
            List<String> lines = TextFileDatabase.loadLines(f);
            items.clear();
            categories.clear();
            int maxId = 0;
            for (String l : lines) {
                if (l.startsWith("CATEGORIES:")) {
                    String rest = l.substring("CATEGORIES:".length());
                    if (!rest.isEmpty()) {
                        String[] cats = rest.split("\\|");
                        for (String c : cats) addCategory(c);
                    }
                } else if (l.startsWith("ITEM:")) {
                    String rest = l.substring("ITEM:".length());
                    String[] parts = rest.split("\\|", 5);
                    if (parts.length >= 5) {
                        int id = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        double price = Double.parseDouble(parts[2]);
                        String category = parts[3].isEmpty() ? null : parts[3];
                        int qty = Integer.parseInt(parts[4]);
                        items.add(new MenuItem(id, name, price, category, qty));
                        if (id > maxId) maxId = id;
                    }
                }
            }
            nextId = Math.max(nextId, maxId + 1);
            loading = false;
            return true;
        } catch (Exception e) {
            items.clear();
            categories.clear();
            loading = false;
            return false;
        }
    }

    // Helper: case-insensitive contains for a list of strings
    private boolean containsIgnoreCase(List<String> list, String s) {
        if (s == null) return false;
        for (String e : list) {
            if (e.equalsIgnoreCase(s)) return true;
        }
        return false;
    }
}

