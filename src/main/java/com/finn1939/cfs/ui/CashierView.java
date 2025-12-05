package com.finn1939.cfs.ui;

import com.finn1939.cfs.model.Product;

public class CashierView {

    public void displayProductLine(Product p) {
        // Show product line with available stock to the cashier
        String line = String.format("%s - %s (Stock: %d)", p.getCode(), p.getName(), p.getQuantity());
        System.out.println(line);
    }

    // Example helper when adding an item
    public void showAddItemPrompt(Product p) {
        System.out.println("Adding: " + p.getName());
        System.out.println("Available: " + p.getQuantity());
        System.out.println("Enter quantity (or 'all' to add everything):");
    }
}