package com.nikolaichuk.cdr.cdrbillingservice.infrastructure.persistence.converter;

import jakarta.persistence.AttributeConverter;

import java.time.YearMonth;

public class YearMonthAttributeConverter implements AttributeConverter<YearMonth, String> {
    @Override
    public String convertToDatabaseColumn(YearMonth value) {
        return value == null ? null : value.toString();
    }

    @Override
    public YearMonth convertToEntityAttribute(String value) {
        return value == null ? null : YearMonth.parse(value);
    }
}
