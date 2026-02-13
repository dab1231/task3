package com.nik.currencyexchanger.dao;

import com.nik.currencyexchanger.entity.Currency;
import com.nik.currencyexchanger.entity.ExchangeRate;
import com.nik.currencyexchanger.util.ConnectionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao {

    private static final int DECIMAL_SCALE = 6;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final ExchangeRateDao INSTANCE = new ExchangeRateDao();

    private static final String SELECT_ALL_SQL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM ExchangeRates
            """;

    private static final String SELECT_BY_CURRENCIES_ID_SQL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM ExchangeRates
            WHERE (base_currency_id = ? AND target_currency_id = ?) OR (target_currency_id = ? AND base_currency_id = ?) 
            """;

    private ExchangeRateDao(){

    }

    public static ExchangeRateDao getInstance(){
        return INSTANCE;
    }

    public static List<ExchangeRate> findAll(){
        try (var connection = ConnectionManager.get();
            var preparedStatement = connection.prepareStatement(SELECT_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();

            List<ExchangeRate> exchangeRates = new ArrayList<>();
            while (resultSet.next()){
                exchangeRates.add(buildExchangeRate(resultSet));
            }
            return exchangeRates;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<ExchangeRate> findByCurrenciesId(int baseId, int targetId){
        try (var connection = ConnectionManager.get();
            var preparedStatement = connection.prepareStatement(SELECT_BY_CURRENCIES_ID_SQL)) {
            var resultSet = preparedStatement.executeQuery();

            ExchangeRate exchangeRate = null;
            if(resultSet.next()){
                if(resultSet.getInt("base_currency_id") == baseId
                    && resultSet.getInt("target_currency_id") == targetId){
                    exchangeRate = buildExchangeRate(resultSet);
                }
                else{
                    var reverseRate = getReverseRate(exchangeRate);
                }
            }
            return Optional.ofNullable(exchangeRate);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ExchangeRate getReverseRate(ExchangeRate exchangeRate){
        var baseCurrencyId = exchangeRate.getBaseCurrencyId();
        var targetCurrencyId = exchangeRate.getTargetCurrencyId();
        BigDecimal originalRate = exchangeRate.getRate();
        BigDecimal reverseRate = originalRate.ONE.divide(originalRate, DECIMAL_SCALE, ROUNDING_MODE);
        exchangeRate.setRate(reverseRate);
        exchangeRate.setBaseCurrencyId(targetCurrencyId);
        exchangeRate.setTargetCurrencyId(baseCurrencyId);
        return exchangeRate;
    }

    private static ExchangeRate buildExchangeRate(ResultSet resultSet) throws SQLException {
        return  new ExchangeRate(
                resultSet.getInt("id"),
                resultSet.getInt("base_currency_id"),
                resultSet.getInt("target_currency_id"),
                resultSet.getBigDecimal("rate")
        );
    }

}
