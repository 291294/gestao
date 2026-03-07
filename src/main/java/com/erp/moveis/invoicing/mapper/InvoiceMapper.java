package com.erp.moveis.invoicing.mapper;

import com.erp.moveis.invoicing.dto.*;
import com.erp.moveis.invoicing.entity.Invoice;
import com.erp.moveis.invoicing.entity.InvoiceItem;

import java.util.Collections;
import java.util.stream.Collectors;

public class InvoiceMapper {

    private InvoiceMapper() {}

    public static InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse dto = new InvoiceResponse();
        dto.setId(invoice.getId());
        dto.setCompanyId(invoice.getCompanyId());
        dto.setClientId(invoice.getClientId());
        dto.setOrderId(invoice.getOrderId());
        dto.setDeliveryId(invoice.getDeliveryId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setStatus(invoice.getStatus());
        dto.setIssueDate(invoice.getIssueDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setDiscountAmount(invoice.getDiscountAmount());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setAmountPaid(invoice.getAmountPaid());
        dto.setAmountDue(invoice.getAmountDue());
        dto.setNotes(invoice.getNotes());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());

        if (invoice.getItems() != null) {
            dto.setItems(invoice.getItems().stream()
                    .map(InvoiceMapper::toItemResponse)
                    .collect(Collectors.toList()));
        } else {
            dto.setItems(Collections.emptyList());
        }
        return dto;
    }

    public static InvoiceItemResponse toItemResponse(InvoiceItem item) {
        InvoiceItemResponse dto = new InvoiceItemResponse();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setDescription(item.getDescription());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscountAmount(item.getDiscountAmount());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setSubtotal(item.getSubtotal());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    public static Invoice toEntity(InvoiceRequest request) {
        Invoice invoice = new Invoice();
        invoice.setCompanyId(request.getCompanyId());
        invoice.setClientId(request.getClientId());
        invoice.setOrderId(request.getOrderId());
        invoice.setDeliveryId(request.getDeliveryId());
        invoice.setDueDate(request.getDueDate());
        invoice.setDiscountAmount(request.getDiscountAmount());
        invoice.setTaxAmount(request.getTaxAmount());
        invoice.setNotes(request.getNotes());

        if (request.getItems() != null) {
            for (InvoiceItemRequest itemReq : request.getItems()) {
                InvoiceItem item = toItemEntity(itemReq);
                invoice.addItem(item);
            }
        }
        return invoice;
    }

    public static InvoiceItem toItemEntity(InvoiceItemRequest request) {
        InvoiceItem item = new InvoiceItem();
        item.setProductId(request.getProductId());
        item.setDescription(request.getDescription());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setDiscountAmount(request.getDiscountAmount());
        item.setTaxAmount(request.getTaxAmount());
        return item;
    }
}
