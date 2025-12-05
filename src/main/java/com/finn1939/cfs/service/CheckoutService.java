package com.finn1939.cfs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finn1939.cfs.model.Cart;
import com.finn1939.cfs.model.Order;

import java.time.Instant;

@Service
public class CheckoutService {

    // TODO: wire the real repositories (OrderRepository, InventoryService, PaymentService)
    @Autowired
    private OrderArchiveService archiveService;

    public CheckoutService() {}

    /**
     * Perform a fast "quick checkout" from a pre-validated cart. This method is optimized
     * to do the minimal necessary work: validate quantities once, reserve stock in batch,
     * persist the order, and return the created order id.
     *
     * The implementation intentionally keeps DB round-trips low and defers heavy work
     * (notifications, analytics) to async jobs.
     */
    public Order quickCheckout(Cart cart, String paymentMethod) {
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // TODO: 1) validate stock availability in a single batch query
        // TODO: 2) reserve or decrement stock atomically (single update where possible)
        // TODO: 3) create Order entity and save in one persist call
        // TODO: 4) capture payment (synchronously or hand off to a fast payment queue)

        Order o = new Order();
        o.setCreatedAt(Instant.now());
        o.setStatus("PAID");
        o.setTotal(cart.getTotal());
        // TODO: populate order items from cart

        // TODO: persist order via OrderRepository.save(o)

        // Return the created order object (with id after save)
        return o;
    }

    /**
     * Finalize an order (alternate flow) - lightweight wrapper to mark complete and optionally archive.
     */
    public void finalizeOrder(long orderId, boolean autoArchive) {
        // TODO: set order as completed, reduce inventory if not already done
        if (autoArchive) {
            archiveService.archiveOrder(orderId);
        }
    }
}
