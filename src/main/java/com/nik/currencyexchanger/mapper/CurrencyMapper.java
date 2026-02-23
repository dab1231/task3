package com.nik.currencyexchanger.mapper;
import com.nik.currencyexchanger.dto.response.CurrencyResponseDto;
import com.nik.currencyexchanger.entity.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CurrencyMapper {

    @Mapping(source = "fullName", target = "name")
    CurrencyResponseDto toDto(Currency currency);
}
