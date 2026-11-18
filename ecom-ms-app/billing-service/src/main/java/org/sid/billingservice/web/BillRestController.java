package org.sid.billingservice.web;

import org.sid.billingservice.entities.Bill;
import org.sid.billingservice.entities.ProductItem;
import org.sid.billingservice.feign.CustomerRestClient;
import org.sid.billingservice.feign.InventoryRestClient;
import org.sid.billingservice.repositories.BillRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class BillRestController {
    private final BillRepository billRepository;
    private final CustomerRestClient customerRestClient;
    private final InventoryRestClient inventoryRestClient;

    public BillRestController(BillRepository billRepository, CustomerRestClient customerRestClient,
                              InventoryRestClient inventoryRestClient) {
        this.billRepository = billRepository;
        this.customerRestClient = customerRestClient;
        this.inventoryRestClient = inventoryRestClient;
    }

    @GetMapping("/bills/{id}")
    public Bill getBill(@PathVariable Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture introuvable"));
        bill.setCustomer(customerRestClient.findCustomerById(bill.getCustomerId()));
        for (ProductItem productItem : bill.getProductItems()) {
            productItem.setProduct(inventoryRestClient.findProductById(productItem.getProductId()));
        }
        return bill;
    }
}
