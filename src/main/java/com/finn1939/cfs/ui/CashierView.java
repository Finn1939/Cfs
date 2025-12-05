package com.finn1939.cfs.ui;

import com.finn1939.cfs.model.Product;
import com.finn1939.cfs.model.Cart;
import com.finn1939.cfs.service.CheckoutService;

public class CashierView {

    private CheckoutService checkoutService;

    public CashierView(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    public void displayProductLine(Product p) {
        String line = String.format("%s - %s (Stock: %d)", p.getCode(), p.getName(), p.getQuantity());
        System.out.println(line);
    }

    public void showAddItemPrompt(Product p) {
        System.out.println("Adding: " + p.getName());
        System.out.println("Available: " + p.getQuantity());
        System.out.println("Enter quantity (or 'all' to add everything):");
    }

    /**
     * Quick checkout helper that collects minimal inputs and performs a fast checkout.
     * Designed to be called from UI flow where cashier has already built a cart.
     */
    public void quickCheckout(Cart cart) {
        // Minimal confirmation
        System.out.println("Confirm quick checkout for total: " + cart.getTotal() + " (y/N)");
        // TODO: read confirmation from UI; for now assume yes for fast flow

        // Hand off to CheckoutService which performs efficient persistence and payment capture
        checkoutService.quickCheckout(cart, cart.getPaymentMethod());

        System.out.println("Checkout completed.");
    }
}