package com.example.appevemsevents.Service.Precio;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long sectorByBatchId) {
        super("Stock insuficiente para el precio " + sectorByBatchId + ".");
    }
}
