package com.erp.moveis.sales.mapper;

import com.erp.moveis.sales.dto.*;
import com.erp.moveis.sales.entity.*;

import java.util.Collections;
import java.util.stream.Collectors;

public class QuoteMapper {

    private QuoteMapper() {}

    // ── Entity → Response ──────────────────────────────────────

    public static QuoteResponse toResponse(Quote quote) {
        QuoteResponse dto = new QuoteResponse();
        dto.setId(quote.getId());
        dto.setQuoteNumber(quote.getQuoteNumber());
        dto.setCompanyId(quote.getCompanyId());
        dto.setClientId(quote.getClientId());
        dto.setSellerId(quote.getSellerId());
        dto.setStatus(quote.getStatus());
        dto.setTotalAmount(quote.getTotalAmount());
        dto.setDiscountAmount(quote.getDiscountAmount());
        dto.setTaxAmount(quote.getTaxAmount());
        dto.setFinalAmount(quote.getFinalAmount());
        dto.setValidUntil(quote.getValidUntil());
        dto.setNotes(quote.getNotes());
        dto.setCreatedAt(quote.getCreatedAt());
        dto.setUpdatedAt(quote.getUpdatedAt());
        dto.setCreatedBy(quote.getCreatedBy());

        if (quote.getItems() != null) {
            dto.setItems(
                    quote.getItems().stream()
                            .map(QuoteMapper::toItemResponse)
                            .collect(Collectors.toList())
            );
        } else {
            dto.setItems(Collections.emptyList());
        }

        return dto;
    }

    public static QuoteItemResponse toItemResponse(QuoteItem item) {
        QuoteItemResponse dto = new QuoteItemResponse();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscountPercentage(item.getDiscountPercentage());
        dto.setDiscountAmount(item.getDiscountAmount());
        dto.setSubtotal(item.getSubtotal());
        dto.setNotes(item.getNotes());
        dto.setCreatedAt(item.getCreatedAt());
        return dto;
    }

    // ── Request → Entity ──────────────────────────────────────

    public static Quote toEntity(QuoteRequest request) {
        Quote quote = new Quote();
        quote.setCompanyId(request.getCompanyId());
        quote.setClientId(request.getClientId());
        quote.setSellerId(request.getSellerId());
        quote.setValidUntil(request.getValidUntil());
        quote.setDiscountAmount(request.getDiscountAmount());
        quote.setTaxAmount(request.getTaxAmount());
        quote.setNotes(request.getNotes());

        if (request.getItems() != null) {
            for (QuoteItemRequest itemReq : request.getItems()) {
                QuoteItem item = toItemEntity(itemReq);
                quote.addItem(item);
            }
        }

        return quote;
    }

    public static QuoteItem toItemEntity(QuoteItemRequest request) {
        QuoteItem item = new QuoteItem();
        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());
        item.setDiscountPercentage(request.getDiscountPercentage());
        item.setDiscountAmount(request.getDiscountAmount());
        item.setNotes(request.getNotes());
        return item;
    }
}
