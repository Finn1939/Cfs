import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.File;

public class CoffeeShopSystem {
    private static final Map<String,String> ADMINS = new HashMap<>();
    static {
        try {
            File aFile = new File("admins.txt");
            if (aFile.exists()) {
                List<String> lines = TextFileDatabase.loadLines(aFile);
                for (String ln : lines) {
                    if (ln == null || ln.trim().isEmpty()) continue;
                    String[] p = ln.split("\\|", 2);
                    if (p.length == 2) {
                        ADMINS.put(p[0], p[1]);
                    }
                }
            }
        } catch (Exception ex) {
            
        }
        if (ADMINS.isEmpty()) {
            ADMINS.put("admin", "12345");
        }
    }

    private static final List<CompletedOrder> completedOrders = new ArrayList<>();

    private static double bankTotal = 0.00;
   
    private static boolean clearMainOnReturn = false;

    private static void printReceipt(CustomerOrder order, String customerName, double cash) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        double total = order.getTotal();
        double change = cash - total;

        System.out.println("\n===== RECEIPT =====");
        System.out.println("Café Minute");
        System.out.println("Date: " + now.format(fmt));
        System.out.println("Customer: " + customerName);
        System.out.println("---------------------------");
        for (OrderLine ol : order.getLines()) {
            System.out.printf("%-20s x%2d  %s%n", ol.item.getName(), ol.qty, Util.formatCurrency(ol.lineTotal()));
        }
        System.out.println("---------------------------");
        System.out.println("TOTAL    : " + Util.formatCurrency(total));
        System.out.println("CASH     : " + Util.formatCurrency(cash));
        System.out.println("CHANGE   : " + Util.formatCurrency(change));
        System.out.println("=====================\n");
    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        MenuManager menu = new MenuManager();
        try {
            File bankFile = new File("bank.txt");
            if (bankFile.exists()) {
                List<String> lines = TextFileDatabase.loadLines(bankFile);
                if (!lines.isEmpty()) {
                    try { bankTotal = Double.parseDouble(lines.get(0)); } catch (Exception ex) { /* ignore parse errors */ }
                }
            }
        } catch (Exception e) {
            
        }
        
        try {
            List<String> ordLines = TextFileDatabase.loadLines(new File("orders.txt"));
            for (String ol : ordLines) {
                try {
                    CompletedOrder co = parseCompletedOrderLine(ol, menu);
                    if (co != null) completedOrders.add(co);
                } catch (Exception ex) {
            
                }
            }
        } catch (Exception e) {
           
        }
        CustomerOrder order = new CustomerOrder();
        
        clearConsole();

        while (true) {
            clearMainIfRequested();
            System.out.println("=== Café Minute ===");
            System.out.println("1. Customer Menu");
            System.out.println("2. Admin Menu");
            System.out.println("0. Exit");
            System.out.print("Select: ");
            int mainChoice = readInt(sc);

            switch (mainChoice) {
                case 1:
                    customerMenu(sc, menu, order);
                    break;
                case 2:
                        String adminUser = adminLogin(sc);
                        if (adminUser != null) {
                            adminMenu(sc, menu, adminUser);
                        }
                    break;
                case 0:
                    System.out.println("Goodbye!");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid selection.\n");
            }
        }
    }

    
    private static void adminMenu(Scanner sc, MenuManager menu, String adminUser) {
        clearConsole();
        while (true) {
            System.out.println("\n--- ADMIN ---");
            System.out.println("1. View Menu");
            System.out.println("2. View Completed Orders");
            System.out.println("3. Manage Bank");
            System.out.println("4. Inventory Management");
            System.out.println("5. Manage Admins");
            System.out.println("0. Back");
            System.out.print("Select: ");
            int choice = readInt(sc);

            if (choice == 1) {
                
                menu.displayMenu(sc);

            } else if (choice == 2) {
                if (completedOrders.isEmpty()) {
                    System.out.println("\nNo completed orders yet.\n");
                } else {
                    System.out.println("\n--- Completed Orders ---");
                    System.out.println("Search options: 1=By customer name  2=By item id  0=Show all");
                    System.out.print("Select: ");
                    int sOpt = readInt(sc);
                    List<CompletedOrder> results = new ArrayList<>();
                    if (sOpt == 0) {
                        results.addAll(completedOrders);
                    } else if (sOpt == 1) {
                        String cName = readNameAllowBack(sc);
                        if (cName == null) { System.out.println("Cancelled.\n"); continue; }
                        for (CompletedOrder co : completedOrders) {
                            if (co.customerName != null && co.customerName.equalsIgnoreCase(cName)) results.add(co);
                        }
                    } else if (sOpt == 2) {
                        System.out.print("Enter item id to search for: ");
                        Integer idObj = readIntAllowBack(sc);
                        if (idObj == null) { System.out.println("Cancelled.\n"); continue; }
                        int searchId = idObj;
                        for (CompletedOrder co : completedOrders) {
                            for (OrderLine ol : co.order.getLines()) {
                                if (ol.item.getId() == searchId) { results.add(co); break; }
                            }
                        }
                    } else {
                        System.out.println("Invalid selection. Showing all.");
                        results.addAll(completedOrders);
                    }

                    if (results.isEmpty()) {
                        System.out.println("\nNo completed orders match the search.\n");
                    } else {
                        int i = 1;
                        for (CompletedOrder co : results) {
                            System.out.println("Order #" + (i++) + ":");
                            System.out.println(co.toString());
                        }
                        System.out.println();
                    }
                }

            } else if (choice == 3) {
                bankMenu(sc, adminUser);

            } else if (choice == 4) {
                inventoryMenu(sc, menu);

            } else if (choice == 5) {
                if (requireAdminReauth(sc, adminUser)) {
                    adminManagementMenu(sc);
                } else {
                    System.out.println("Access denied. Admin management requires re-authentication.\n");
                }

            } else if (choice == 0) {
                requestMainClear();
                return;

            } else {
                System.out.println("Invalid selection.\n");
            }
        }
    }


    private static void bankMenu(Scanner sc, String adminUser) {
        clearConsole();
        while (true) {
            System.out.println("\n--- MANAGE BANK ---");
            System.out.println("1. View Bank Total");
            System.out.println("2. Withdraw");
            System.out.println("0. Back");
            System.out.print("Select: ");
            int bch = readInt(sc);
            if (bch == 1) {
                System.out.println("\n-- Bank --");
                System.out.println("Total cash collected: " + Util.formatCurrency(bankTotal) + "\n");
            } else if (bch == 2) {
                if (!requireAdminReauth(sc, adminUser)) {
                    System.out.println("Access denied. Withdrawal requires re-authentication.\n");
                    continue;
                }
                System.out.println("\n--- WITHDRAW FROM BANK ---");
                System.out.println("Available: " + Util.formatCurrency(bankTotal));
                System.out.print("Enter amount to withdraw (or 'b' to cancel): PHP ");
                Double wObj = readDoubleAllowBack(sc);
                if (wObj == null) { System.out.println("Cancelled.\n"); continue; }
                double w = wObj;
                if (w <= 0) {
                    System.out.println("Amount must be greater than zero.\n");
                } else if (w > bankTotal) {
                    System.out.println("Insufficient funds in bank.\n");
                } else {
                    System.out.print("Confirm withdraw " + Util.formatCurrency(w) + " from bank? (y/n): ");
                    String conf = sc.nextLine().trim();
                    if (conf.equalsIgnoreCase("y")) {
                        bankTotal -= w;
                        
                        try {
                            TextFileDatabase.saveLines(new File("bank.txt"), java.util.Arrays.asList(Double.toString(bankTotal)));
                        } catch (Exception e) {
                        
                        }
                        System.out.println("Withdrawal successful. New bank total: " + Util.formatCurrency(bankTotal) + "\n");
                    } else {
                        System.out.println("Cancelled.\n");
                    }
                }
            } else if (bch == 0) {
                return;
            } else {
                System.out.println("Invalid selection.\n");
            }
        }
    }

    private static void inventoryMenu(Scanner sc, MenuManager menu) {
        clearConsole();
        while (true) {
            System.out.println("\n--- INVENTORY ---");
            System.out.println("1. View Inventory");
            System.out.println("2. Add New Category");
            System.out.println("3. Remove Category");
            System.out.println("4. Add New Item");
            System.out.println("5. Remove Item");
            System.out.println("6. Restock Item");
            System.out.println("7. Update Price");
            System.out.println("0. Back");
            System.out.print("Select (or 'b' to go back): ");
            Integer cObj = readIntAllowBack(sc);
            if (cObj == null) { System.out.println(); return; }
            int c = cObj;
            if (c == 1) {
                System.out.println("\n-- Inventory --");
                for (MenuItem mi : menu.getInventory()) {
                    System.out.println(mi.toInventoryString());
                }
                System.out.println();
            } else if (c == 2) {
                System.out.print("Enter new category name (or 'b' to cancel): ");
                String catRaw = readLineAllowBack(sc);
                if (catRaw == null) { System.out.println("Cancelled.\n"); continue; }
                if (!catRaw.equals(catRaw.trim()) || catRaw.matches(".*\\s.*")) { System.out.println("Category name cannot contain whitespace. Cancelled.\n"); continue; }
                String cat = catRaw;
                if (cat.isEmpty()) {
                    System.out.println("Invalid category name.\n");
                } else if (menu.addCategory(cat)) {
                    System.out.println("Category added: " + cat + "\n");
                } else {
                    System.out.println("Category already exists.\n");
                }
            } else if (c == 3) {
                Integer sel = promptCategorySelection(sc, menu);
                if (sel == null) continue;
                String rem = menu.getCategories().get(sel - 1);
                if (menu.removeCategory(rem)) {
                    System.out.println("Category removed: " + rem + "\n");
                } else {
                    System.out.println("Cannot remove category '" + rem + "'. Make sure no items belong to it first.\n");
                }
            } else if (c == 4) {
                System.out.print("Enter item name: ");
                String nameRaw = readLineAllowBack(sc);
                if (nameRaw == null) { System.out.println("Cancelled.\n"); continue; }
                if (!nameRaw.equals(nameRaw.trim()) || nameRaw.matches(".*\\s.*")) { System.out.println("Item name cannot contain whitespace. Cancelled.\n"); continue; }
                String name = nameRaw;
                Integer catSel = promptCategorySelection(sc, menu);
                if (catSel == null) continue;
                String cat = menu.getCategories().get(catSel - 1);
                System.out.print("Enter price: ");
                Double priceObj = readDoubleAllowBack(sc);
                if (priceObj == null) { System.out.println("Cancelled.\n"); continue; }
                double price = priceObj;
                System.out.print("Enter initial quantity (or 'b' to cancel): ");
                Integer qtyObj = readIntAllowBack(sc);
                if (qtyObj == null) { System.out.println("Cancelled.\n"); continue; }
                int qty = qtyObj;
                MenuItem created = menu.addNewItem(name, price, cat, qty);
                System.out.println("Added item: " + created.toInventoryString() + "\n");
            } else if (c == 5) {
                // Remove items: choose a category first, then remove single item or clear category
                Integer catSelObj = promptCategorySelection(sc, menu);
                if (catSelObj == null) continue;
                int catSel = catSelObj;
                String selectedCat = menu.getCategories().get(catSel - 1);
                // list items in selected category
                List<MenuItem> itemsInCat = new ArrayList<>();
                for (MenuItem mi : menu.getInventory()) {
                    if (mi.getCategory() != null && mi.getCategory().equalsIgnoreCase(selectedCat)) itemsInCat.add(mi);
                }
                if (itemsInCat.isEmpty()) { System.out.println("No items in this category.\n"); continue; }
                System.out.println("\n-- Items in " + selectedCat + " --");
                for (MenuItem mi : itemsInCat) System.out.println(mi.toInventoryString());
                System.out.println("A. Remove ALL items in this category");
                System.out.print("Enter item ID to remove, 'A' to remove all, or 'b' to cancel: ");
                String choiceRaw = readLineAllowBack(sc);
                if (choiceRaw == null) { System.out.println("Cancelled.\n"); continue; }
                if (!choiceRaw.equals(choiceRaw.trim()) || choiceRaw.matches(".*\\s.*")) { System.out.println("Input cannot contain whitespace. Cancelled.\n"); continue; }
                String choice = choiceRaw;
                if (choice.equalsIgnoreCase("a")) {
                    Boolean conf = readYesNoStrict(sc, "Confirm: remove ALL items in category '" + selectedCat + "'?");
                    if (conf != null && conf) {
                        int removed = menu.removeItemsByCategory(selectedCat);
                        System.out.println("Removed " + removed + " items from category '" + selectedCat + "'.\n");
                    } else {
                        System.out.println("Cancelled.\n");
                    }
                    continue;
                }
                // try parse as id and ask for quantity to remove
                try {
                    int idRem = Integer.parseInt(choice);
                    MenuItem toRem = menu.findById(idRem);
                    if (toRem == null) { System.out.println("Invalid item ID.\n"); continue; }
                    if (toRem.getCategory() == null || !toRem.getCategory().equalsIgnoreCase(selectedCat)) {
                        System.out.println("Item does not belong to the selected category.\n"); continue;
                    }
                    System.out.printf("Current stock for '%s' is %d. Enter quantity to remove (or 'b' to cancel): ", toRem.getName(), toRem.getQuantity());
                    Integer remQtyObj = readIntAllowBack(sc);
                    if (remQtyObj == null) { System.out.println("Cancelled.\n"); continue; }
                    int remQty = remQtyObj;
                    if (remQty <= 0) { System.out.println("Quantity must be at least 1.\n"); continue; }
                    if (remQty >= toRem.getQuantity()) {
                        // removing all or more -> confirm full deletion
                        Boolean confRem = readYesNoStrict(sc, "Requested quantity >= current stock; remove entire item from inventory?");
                        if (confRem != null && confRem) {
                            if (menu.removeItemById(idRem)) {
                                System.out.println("Item removed.\n");
                            } else {
                                System.out.println("Failed to remove item.\n");
                            }
                        } else {
                            System.out.println("Cancelled.\n");
                        }
                    } else {
                        // partial decrement
                        if (menu.decrementStock(idRem, remQty)) {
                            System.out.println("Reduced stock by " + remQty + ". New stock: " + menu.findById(idRem).getQuantity() + "\n");
                        } else {
                            System.out.println("Failed to reduce stock (not enough stock?).\n");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Please enter a valid item ID, 'A' or 'b'.\n");
                    continue;
                }
            } else if (c == 6) {
                // Restock: choose a category first for a cleaner UI
                Integer catObj = promptCategorySelection(sc, menu);
                if (catObj == null) continue;
                String selCat = menu.getCategories().get(catObj - 1);
                List<MenuItem> itemsInCat = new ArrayList<>();
                for (MenuItem mi : menu.getInventory()) if (mi.getCategory() != null && mi.getCategory().equalsIgnoreCase(selCat)) itemsInCat.add(mi);
                if (itemsInCat.isEmpty()) { System.out.println("No items in this category.\n"); continue; }
                System.out.println("\n-- Items in " + selCat + " --");
                for (MenuItem mi : itemsInCat) System.out.println(mi.toInventoryString());
                System.out.print("Enter item ID to restock (or 'b' to cancel): ");
                Integer idObj = readIntAllowBack(sc);
                if (idObj == null) { System.out.println("Cancelled.\n"); continue; }
                int id = idObj;
                MenuItem chosen = menu.findById(id);
                if (chosen == null || chosen.getCategory() == null || !chosen.getCategory().equalsIgnoreCase(selCat)) { System.out.println("Invalid item ID for this category.\n"); continue; }
                System.out.print("Enter quantity to add (or 'b' to cancel): ");
                Integer qObj = readIntAllowBack(sc);
                if (qObj == null) { System.out.println("Cancelled.\n"); continue; }
                int q = qObj;
                if (menu.restock(id, q)) {
                    System.out.println("Restocked. New quantity: " + menu.findById(id).getQuantity() + "\n");
                } else {
                    System.out.println("Invalid item ID.\n");
                }
            } else if (c == 7) {
                // Update price: choose category first
                Integer catUObj = promptCategorySelection(sc, menu);
                if (catUObj == null) continue;
                String selCatU = menu.getCategories().get(catUObj - 1);
                List<MenuItem> itemsU = new ArrayList<>();
                for (MenuItem mi : menu.getInventory()) if (mi.getCategory() != null && mi.getCategory().equalsIgnoreCase(selCatU)) itemsU.add(mi);
                if (itemsU.isEmpty()) { System.out.println("No items in this category.\n"); continue; }
                System.out.println("\n-- Items in " + selCatU + " --");
                for (MenuItem mi : itemsU) System.out.println(mi.toInventoryString());
                System.out.print("Enter item ID to update price (or 'b' to cancel): ");
                Integer idpObj = readIntAllowBack(sc);
                if (idpObj == null) { System.out.println("Cancelled.\n"); continue; }
                int idp = idpObj;
                MenuItem chosenP = menu.findById(idp);
                if (chosenP == null || chosenP.getCategory() == null || !chosenP.getCategory().equalsIgnoreCase(selCatU)) { System.out.println("Invalid item ID for this category.\n"); continue; }
                System.out.print("Enter new price: ");
                Double npObj = readDoubleAllowBack(sc);
                if (npObj == null) { System.out.println("Cancelled.\n"); continue; }
                double np = npObj;
                if (menu.updatePrice(idp, np)) {
                    System.out.println("Price updated. New price: " + Util.formatCurrency(menu.findById(idp).getPrice()) + "\n");
                } else {
                    System.out.println("Invalid item ID.\n");
                }
            
            } else if (c == 0) {
                return;
            } else {
                System.out.println("Invalid selection.\n");
            }
        }
    }

    /**
     * Prompt for admin credentials. Returns true if supplied credentials match
     * the configured admin username/password constants.
     */
    private static String adminLogin(Scanner sc) {
        System.out.print("Admin username (or 'b' to cancel): ");
        String userRaw = readLineAllowBack(sc);
        if (userRaw == null) { System.out.println("Admin login cancelled.\n"); return null; }
        if (!userRaw.equals(userRaw.trim()) || userRaw.matches(".*\\s.*")) {
            System.out.println("Username cannot contain whitespace. Login cancelled.\n");
            return null;
        }
        String user = userRaw;
        System.out.print("Admin password (or 'b' to cancel): ");
        String passRaw = readLineAllowBack(sc);
        if (passRaw == null) { System.out.println("Admin login cancelled.\n"); return null; }
        if (!passRaw.equals(passRaw.trim()) || passRaw.matches(".*\\s.*")) {
            System.out.println("Password cannot contain whitespace. Login cancelled.\n");
            return null;
        }
        String pass = passRaw;
        String expected = ADMINS.get(user);
        if (expected != null && expected.equals(pass)) {
            return user;
        }
        System.out.println("Access denied.\n");
        return null;
    }

    private static void requestMainClear() {
        clearMainOnReturn = true;
    }

    private static void clearMainIfRequested() {
        if (clearMainOnReturn) {
            clearConsole();
            clearMainOnReturn = false;
        }
    }

    private static boolean requireAdminReauth(Scanner sc, String adminUser) {
        if (adminUser == null || !ADMINS.containsKey(adminUser)) return false;
        final int MAX_TRIES = 3;
        for (int attempt = 1; attempt <= MAX_TRIES; attempt++) {
            System.out.print("Re-enter password for '" + adminUser + "' (attempt " + attempt + "/" + MAX_TRIES + "): ");
            String raw = readLineAllowBack(sc);
            if (raw == null) { System.out.println("Re-authentication cancelled."); return false; }
            if (!raw.equals(raw.trim()) || raw.matches(".*\\s.*")) { System.out.println("Password must not contain whitespace."); continue; }
            String p = raw;
            String expected = ADMINS.get(adminUser);
            if (expected != null && expected.equals(p)) {
                return true;
            }
            System.out.println("Incorrect password.");
        }
        System.out.println("Re-authentication failed.");
        return false;
    }

    public static void clearConsole() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Use cmd to run CLS on Windows (cls is internal to cmd.exe)
                Process p = new ProcessBuilder("cmd", "/c", "cls").inheritIO().start();
                p.waitFor();
            } else {
                // ANSI escape sequence for POSIX terminals
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback: print many new lines to approximate a clear
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }


    private static void adminManagementMenu(Scanner sc) {
        clearConsole();
        while (true) {
            System.out.println("\n--- ADMIN MANAGEMENT ---");
            System.out.println("1. List admins");
            System.out.println("2. Add admin");
            System.out.println("3. Remove admin");
            System.out.println("0. Back");
            System.out.print("Select: ");
            int c = readInt(sc);
            if (c == 1) {
                System.out.println("\nAdmins:");
                for (String u : ADMINS.keySet()) System.out.println("- " + u);
                System.out.println();
            } else if (c == 2) {
                System.out.print("Enter new admin username: ");
                String uRaw = readLineAllowBack(sc);
                if (uRaw == null || !uRaw.equals(uRaw.trim()) || uRaw.matches(".*\\s.*") || uRaw.isEmpty()) { System.out.println("Invalid username.\n"); continue; }
                String u = uRaw;
                if (ADMINS.containsKey(u)) { System.out.println("User already exists.\n"); continue; }
                System.out.print("Enter password for " + u + ": ");
                String pRaw = readLineAllowBack(sc);
                if (pRaw == null || !pRaw.equals(pRaw.trim()) || pRaw.matches(".*\\s.*")) { System.out.println("Invalid password.\n"); continue; }
                String p = pRaw;
                ADMINS.put(u, p);
                saveAdmins();
                System.out.println("Admin added.\n");
            } else if (c == 3) {
                System.out.print("Enter admin username to remove: ");
                String u = readLineAllowBack(sc);
                if (u == null || !u.equals(u.trim()) || u.matches(".*\\s.*")) { System.out.println("No such admin.\n"); continue; }
                if (!ADMINS.containsKey(u)) { System.out.println("No such admin.\n"); continue; }
                if (ADMINS.size() <= 1) { System.out.println("Cannot remove last admin.\n"); continue; }
                ADMINS.remove(u);
                saveAdmins();
                System.out.println("Admin removed.\n");
            } else if (c == 0) {
                return;
            } else {
                System.out.println("Invalid selection.\n");
            }
        }
    }

    private static void customerMenu(Scanner sc, MenuManager menu, CustomerOrder order) {
        clearConsole();
        while (true) {
            System.out.println("\n--- WELCOME TO Café Minute ---");
            System.out.println("1. View Menu and Order");
            System.out.println("2. View Cart");
            System.out.println("3. Cancel Order");
            System.out.println("0. Back");
            System.out.print("Select: ");
            int choice = readInt(sc);

            switch (choice) {
                case 1:
                    handleAddItem(sc, menu, order);
                    break;

                case 2:
                    viewCartInteractive(sc, menu, order);
                    break;

                case 3:
                    Boolean confCancel = readYesNoStrict(sc, "Confirm cancel order");
                    if (confCancel != null && confCancel) {
                        order.clear();
                        System.out.println("Order cleared.\n");
                    } else {
                        System.out.println("Cancel aborted.\n");
                    }
                    break;

                

                case 0:
                    requestMainClear();
                    return;

                default:
                    System.out.println("Invalid selection.\n");
            }
        }
    }

    private static int readInt(Scanner sc) {
        while (true) {
            String lineRaw = sc.nextLine();
            if (lineRaw == null) { System.out.print("Please enter a valid whole number (digits only): "); continue; }
            if (!lineRaw.equals(lineRaw.trim()) || lineRaw.matches(".*\\s.*")) {
                System.out.print("Input must not contain whitespace. Enter digits only: ");
                continue;
            }
            String line = lineRaw;
            if (line.matches("^[0-9]+$")) {
                return Integer.parseInt(line);
            }
            System.out.print("Please enter a valid whole number (digits only): ");
        }
    }

    private static Integer readIntAllowBack(Scanner sc) {
        while (true) {
            try {
                String raw = sc.nextLine();
                if (raw == null) { System.out.print("Please enter a valid whole number (or 'b' to go back): "); continue; }
                if (!raw.equals(raw.trim()) || raw.matches(".*\\s.*")) {
                    System.out.print("Input must not contain whitespace. Enter digits only or 'b' to go back: ");
                    continue;
                }
                String line = raw;
                if (line.equalsIgnoreCase("b")) return null;
                if (line.matches("^[0-9]+$")) return Integer.parseInt(line);
                System.out.print("Please enter a valid whole number (or 'b' to go back): ");
            } catch (Exception e) {
                System.out.print("Please enter a valid whole number (or 'b' to go back): ");
            }
        }
    }

    private static Integer promptCategorySelection(Scanner sc, MenuManager menu) {
        List<String> cats = menu.getCategories();
        if (cats.isEmpty()) {
            System.out.println("No categories defined.\n");
            return null;
        }
        System.out.println("Select category:");
        for (int i = 0; i < cats.size(); i++) System.out.printf("%d. %s%n", i + 1, cats.get(i));
        System.out.print("Enter category number (or 'b' to cancel): ");
        Integer sel = readIntAllowBack(sc);
        if (sel == null) { System.out.println("Cancelled.\n"); return null; }
        if (sel < 1 || sel > cats.size()) { System.out.println("Invalid category selection.\n"); return null; }
        return sel;
    }

    private static void handleAddItem(Scanner sc, MenuManager menu, CustomerOrder order) {
        while (true) {
            int id = menu.selectItem(sc);
            if (id <= 0) {
                // user cancelled ordering
                System.out.println();
                break;
            }
            MenuItem item = menu.findById(id);
            if (item == null) {
                System.out.println("Invalid item ID.\n");
                continue;
            }
            System.out.print("Enter quantity (or 'b' to cancel): ");
            Integer qtyObj = readIntAllowBack(sc);
            if (qtyObj == null) { System.out.println("Cancelled.\n"); continue; }
            int qty = qtyObj;
            if (qty <= 0) { System.out.println("Quantity must be at least 1.\n"); continue; }
            if (item.getQuantity() < qty) { System.out.println("Not enough stock available.\n"); continue; }

            // Ask about add-ons before adding the main item to the cart
            List<int[]> addons = new ArrayList<>();
            Boolean wantAddons = readYesNoStrict(sc, "Would you like to add syrup/sauce/add-ons?");
            if (wantAddons != null && wantAddons) {
                addons = menu.selectAddons(sc);
            }

            // Add main item first (after confirming quantity) and then add valid add-ons
            order.addItem(item, qty);
            boolean anyAddonsAdded = false;
            if (!addons.isEmpty()) {
                for (int[] pair : addons) {
                    MenuItem addon = menu.findById(pair[0]);
                    if (addon != null) {
                        if (addon.getQuantity() < pair[1]) {
                            System.out.printf("Not enough stock for add-on %s; skipped.\n", addon.getName());
                        } else {
                            order.addItem(addon, pair[1]);
                            anyAddonsAdded = true;
                        }
                    }
                }
            }

            System.out.printf("Added %s x%d to cart.%n", item.getName(), qty);
            System.out.println("Item added to cart.");
            if (anyAddonsAdded) System.out.println("Add-ons added to cart.");
            System.out.println();
            // loop back to menu to allow more orders
        }
    }

    // Helper: read double, but allow user to enter 'b' to go back (returns null when back)
    private static Double readDoubleAllowBack(Scanner sc) {
        while (true) {
            try {
                String raw = sc.nextLine();
                if (raw == null) { System.out.print("Please enter a valid amount (xx.00) or 'b' to go back: "); continue; }
                if (!raw.equals(raw.trim()) || raw.matches(".*\\s.*")) {
                    System.out.print("Input must not contain whitespace. Enter amount or 'b' to go back: ");
                    continue;
                }
                String line = raw;
                if (line.equalsIgnoreCase("b")) return null;
                if (line.matches("^[0-9]+(\\.[0-9]{1,2})?$") ) {
                    return Double.parseDouble(line);
                }
                System.out.print("Please enter a valid amount (xx.00) or 'b' to go back: ");
            } catch (Exception e) {
                System.out.print("Please enter a valid amount (xx.00) or 'b' to go back: ");
            }
        }
    }

    private static String readNameAllowBack(Scanner sc) {
        while (true) {
            System.out.print("Enter customer name (letters and single spaces only, no leading/trailing spaces, or 'b' to go back): ");
            String raw = sc.nextLine();
            if (raw == null) return null;
            if (raw.equalsIgnoreCase("b")) return null;
            if (!raw.equals(raw.trim())) {
                System.out.println("Do not include leading or trailing spaces. Try again or enter 'b' to cancel.");
                continue;
            }
            if (raw.matches("^[A-Za-z]+( [A-Za-z]+)*$")) return raw;
            System.out.println("Name must contain only letters and single spaces between words. Try again or enter 'b' to cancel.");
        }
    }

    private static Boolean readYesNoStrict(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String raw = sc.nextLine();
            if (raw == null) { System.out.println("Invalid input."); continue; }
            if (raw.equalsIgnoreCase("b")) return null;
            if (!raw.equals(raw.trim())) { System.out.println("Do not include leading/trailing spaces."); continue; }
            if (raw.matches(".*\\d.*")) { System.out.println("Numbers are not allowed."); continue; }
            String v = raw.toLowerCase();
            if (v.equals("y") || v.equals("yes")) return true;
            if (v.equals("n") || v.equals("no")) return false;
            System.out.println("Please answer 'y' or 'n' (no spaces or numbers).");
        }
    }

    private static String readLineAllowBack(Scanner sc) {
        String raw = sc.nextLine();
        if (raw == null) return null;
        if (raw.equalsIgnoreCase("b")) return null;
        return raw;
    }

    private static void viewCartInteractive(Scanner sc, MenuManager menu, CustomerOrder order) {
        clearConsole();
        while (true) {
            order.view();
            if (order.isEmpty()) {
                return;
            }
            System.out.println("Options: (u) Update quantity  (r) Remove item  (c) Change item  (p) Checkout  (b) Back");
            System.out.print("Choose: ");
            String optRaw = sc.nextLine();
            if (optRaw == null) { System.out.println("Invalid input.\n"); continue; }
            if (!optRaw.equals(optRaw.trim()) || optRaw.matches(".*\\s.*")) { System.out.println("Input must not contain whitespace.\n"); continue; }
            String opt = optRaw.toLowerCase();
            if (opt.equals("b")) return;
            if (opt.equals("u")) {
                System.out.print("Enter cart item number to update (or 'b' to cancel): ");
                Integer idxObj = readIntAllowBack(sc);
                if (idxObj == null) { System.out.println("Cancelled.\n"); continue; }
                int idx = idxObj;
                System.out.print("Enter new quantity (or 'b' to cancel): ");
                Integer nqObj = readIntAllowBack(sc);
                if (nqObj == null) { System.out.println("Cancelled.\n"); continue; }
                int newQty = nqObj;
                if (order.updateQuantity(idx, newQty)) {
                    System.out.println("Quantity updated.\n");
                } else {
                    System.out.println("Invalid input.\n");
                }
                continue;
            }
            if (opt.equals("c")) {
                System.out.print("Enter cart item number to change (or 'b' to cancel): ");
                Integer idxObj = readIntAllowBack(sc);
                if (idxObj == null) { System.out.println("Cancelled.\n"); continue; }
                int idx = idxObj;
                if (idx < 1 || idx > order.getLines().size()) { System.out.println("Invalid cart item number.\n"); continue; }
                System.out.println("Select replacement item (or 'b' to cancel):");
                int newId = menu.selectItem(sc);
                if (newId == 0) { System.out.println("Cancelled.\n"); continue; }
                MenuItem newItem = menu.findById(newId);
                if (newItem == null) { System.out.println("Invalid item selected.\n"); continue; }
                System.out.print("Enter quantity for replacement item (or 'b' to cancel): ");
                Integer qObj = readIntAllowBack(sc);
                if (qObj == null) { System.out.println("Cancelled.\n"); continue; }
                int q = qObj;
                if (q <= 0) { System.out.println("Quantity must be at least 1.\n"); continue; }
                if (newItem.getQuantity() < q) { System.out.println("Not enough stock available for selected item.\n"); continue; }
                MenuItem old = order.getLines().get(idx-1).item;
                Boolean confChange = readYesNoStrict(sc, "Confirm change from '" + old.getName() + "' to '" + newItem.getName() + "' x" + q + "?");
                if (confChange == null || !confChange) { System.out.println("Change cancelled.\n"); continue; }
                // replace the line at index with the new item/quantity
                order.getLines().set(idx - 1, new OrderLine(newItem, q));
                System.out.println("Item changed in cart.\n");
                continue;
            }
            if (opt.equals("p")) {
                checkoutFlow(sc, menu, order);
                continue;
            }
            if (opt.equals("r")) {
                System.out.print("Enter cart item number to remove (or 'b' to cancel): ");
                Integer idxObj = readIntAllowBack(sc);
                if (idxObj == null) { System.out.println("Cancelled.\n"); continue; }
                int idx = idxObj;
                if (idx < 1 || idx > order.getLines().size()) { System.out.println("Invalid cart item number.\n"); continue; }
                MenuItem toRemove = order.getLines().get(idx-1).item;
                Boolean confRem = readYesNoStrict(sc, "Confirm remove '" + toRemove.getName() + "' from cart");
                if (confRem != null && confRem) {
                    if (order.removeItem(idx)) {
                        System.out.println("Item removed.\n");
                    } else {
                        System.out.println("Invalid input.\n");
                    }
                } else {
                    System.out.println("Removal cancelled.\n");
                }
                continue;
            }
            System.out.println("Unknown option. Use 'u', 'r' or 'b'.\n");
        }
    }

    private static void checkoutFlow(Scanner sc, MenuManager menu, CustomerOrder order) {
        if (order.isEmpty()) {
            System.out.println("Your cart is empty.\n");
            return;
        }
        order.view();
        String name = readNameAllowBack(sc);
        if (name == null) {
            System.out.println("Checkout cancelled.\n");
            return;
        }
        double total = order.getTotal();
        System.out.println("Total: " + Util.formatCurrency(total));
        System.out.print("Enter cash amount (or 'b' to go back): PHP ");
        Double cashObj = readDoubleAllowBack(sc);
        if (cashObj == null) {
            System.out.println("Checkout cancelled.\n");
            return;
        }
        double cash = cashObj;
        while (cash < total) {
            System.out.printf("Insufficient cash. Need at least %s. Enter again (or 'b' to go back): PHP ", Util.formatCurrency(total));
            cashObj = readDoubleAllowBack(sc);
            if (cashObj == null) {
                System.out.println("Checkout cancelled.\n");
                return;
            }
            cash = cashObj;
        }
        // Ask for final confirmation now that the payment amount is known
        System.out.println("Payment: Total " + Util.formatCurrency(total) + "  Cash " + Util.formatCurrency(cash));
        Boolean confirm = readYesNoStrict(sc, "Confirm checkout");
        if (confirm == null || !confirm) { System.out.println("Checkout cancelled.\n"); return; }
        // Check inventory availability before finalizing
        boolean ok = true;
        for (OrderLine ol : order.getLines()) {
            MenuItem mi = menu.findById(ol.item.getId());
            if (mi == null || mi.getQuantity() < ol.qty) {
                System.out.printf("Insufficient stock for %s. Checkout cancelled.\n\n", ol.item.getName());
                ok = false;
                break;
            }
        }
        if (!ok) {
            return;
        }

        // Deduct stock and record payment
        for (OrderLine ol : order.getLines()) {
            menu.decrementStock(ol.item.getId(), ol.qty);
        }
        bankTotal += total;
        // persist bank total
        try {
            TextFileDatabase.saveLines(new File("bank.txt"), java.util.Arrays.asList(Double.toString(bankTotal)));
        } catch (Exception e) {
            // ignore persistence errors
        }

        LocalDateTime completedTime = LocalDateTime.now();
        printReceipt(order, name.isEmpty() ? "Guest" : name, cash);
        CompletedOrder rec = new CompletedOrder(order.copy(), name.isEmpty() ? "Guest" : name, completedTime, total, cash);
        completedOrders.add(rec);
        // append to orders.txt
        try {
            TextFileDatabase.appendLine(new File("orders.txt"), completedOrderToLine(rec));
        } catch (Exception e) {
            // ignore
        }
        System.out.println("Order recorded as completed.\n");
        order.clear();
    }

    private static String completedOrderToLine(CompletedOrder co) {
        // Format: timeISO|customer|total|cash|id:qty,id:qty
        StringBuilder sb = new StringBuilder();
        sb.append(co.time.toString().replace("|", "/"));
        sb.append("|");
        sb.append(co.customerName.replace("|", "/"));
        sb.append("|");
        sb.append(Double.toString(co.total));
        sb.append("|");
        sb.append(Double.toString(co.cash));
        sb.append("|");
        boolean first = true;
        for (OrderLine ol : co.order.getLines()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(ol.item.getId()).append(":").append(ol.qty);
        }
        return sb.toString();
    }

    private static CompletedOrder parseCompletedOrderLine(String line, MenuManager menu) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split("\\|", 5);
        if (parts.length < 5) return null;
        LocalDateTime t = LocalDateTime.parse(parts[0]);
        String customer = parts[1];
        double total = Double.parseDouble(parts[2]);
        double cash = Double.parseDouble(parts[3]);
        String itemsPart = parts[4];
        CustomerOrder co = new CustomerOrder();
        if (!itemsPart.trim().isEmpty()) {
            String[] pairs = itemsPart.split(",");
            for (String p : pairs) {
                String[] kv = p.split(":");
                if (kv.length != 2) continue;
                try {
                    int id = Integer.parseInt(kv[0]);
                    int qty = Integer.parseInt(kv[1]);
                    MenuItem mi = menu.findById(id);
                    if (mi != null) co.addItem(mi, qty);
                } catch (Exception e) { /* skip malformed pair */ }
            }
        }
        return new CompletedOrder(co, customer, t, total, cash);
    }

    private static void saveAdmins() {
        try {
            List<String> out = new ArrayList<>();
            for (Map.Entry<String, String> e : ADMINS.entrySet()) {
                out.add(e.getKey() + "|" + e.getValue());
            }
            TextFileDatabase.saveLines(new File("admins.txt"), out);
        } catch (Exception ex) {
            System.out.println("Warning: failed to save admins to admins.txt");
        }
    }
}
