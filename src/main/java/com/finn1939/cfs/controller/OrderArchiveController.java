package com.finn1939.cfs.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finn1939.cfs.service.OrderArchiveService;

@RestController
@RequestMapping("/api/orders")
public class OrderArchiveController {

    @Autowired
    private OrderArchiveService archiveService;

    @PostMapping("/{id}/archive")
    public ResponseEntity<?> archive(@PathVariable("id") long id) {
        archiveService.archiveOrder(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unarchive")
    public ResponseEntity<?> unarchive(@PathVariable("id") long id) {
        archiveService.unarchiveOrder(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/archive/export")
    public void exportArchived(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=archived-orders.csv");
        archiveService.exportArchivedOrders(response.getOutputStream());
    }
}