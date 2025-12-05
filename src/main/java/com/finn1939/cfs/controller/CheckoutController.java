package com.finn1939.cfs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finn1939.cfs.model.Cart;
import com.finn1939.cfs.model.Order;
import com.finn1939.cfs.service.CheckoutService;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    // Quick checkout endpoint for cashier to complete orders with minimal steps
    @PostMapping("/quick")
    public ResponseEntity<?> quickCheckout(@RequestBody Cart cart) {
        // For speed we expect cart is pre-validated by the client; server will do a fast sanity check
        Order o = checkoutService.quickCheckout(cart, cart.getPaymentMethod());
        return ResponseEntity.ok(o);
    }
}
