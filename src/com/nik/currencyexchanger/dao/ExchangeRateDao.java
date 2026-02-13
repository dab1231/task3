package com.nik.currencyexchanger.dao;

import com.nik.currencyexchanger.entity.ExchangeRate;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ExchangeRateAlreadyExistsException;
import com.nik.currencyexchanger.util.ConnectionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    private static final String INSERT_SQL = """
            INSERT INTO ExchangeRates (base_currency_id, target_currency_id, rate) 
            VALUES (?, ?, ?) 
            """;

    private ExchangeRateDao(){

    }

    public static ExchangeRateDao getInstance(){
        return INSTANCE;
    }

    public static Optional<ExchangeRate> save(int baseCurrencyId, int targetCurrencyId, BigDecimal rate){
        try (var connection = ConnectionManager.get();
            var preparedStatement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, baseCurrencyId);
            preparedStatement.setInt(2,targetCurrencyId);
            preparedStatement.setBigDecimal(3, rate);
            preparedStatement.executeUpdate();

            var resultSet = preparedStatement.getGeneratedKeys();
            if(resultSet.next()){
                var id = resultSet.getInt("id");
                ExchangeRate exchangeRate = new ExchangeRate(
                        id,
                        baseCurrencyId,
                        targetCurrencyId,
                        rate
                );
                return Optional.of(exchangeRate);
            }
            throw new DataBaseException("Fail with generate id");

        }catch (SQLException e){
            int errorCode = e.getErrorCode();
            if(errorCode == 19 || errorCode == 2067){
                throw new ExchangeRateAlreadyExistsException();
            }
            throw new DataBaseException("DB error", e);
        }
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
            preparedStatement.setInt(1,baseId);
            preparedStatement.setInt(2,targetId);
            preparedStatement.setInt(3,targetId);
            preparedStatement.setInt(4,baseId);

            var resultSet = preparedStatement.executeQuery();

            ExchangeRate reverseRate = null;
            if (resultSet.next()) {
                ExchangeRate exchangeRateFromDb = buildExchangeRate(resultSet);
                if (resultSet.getInt("base_currency_id") == baseId
                        && resultSet.getInt("target_currency_id") == targetId) {
                    reverseRate = exchangeRateFromDb;
                } else {
                    reverseRate = getReverseRate(exchangeRateFromDb);
                }
            }
            return Optional.ofNullable(reverseRate);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ExchangeRate getReverseRate(ExchangeRate originalExchangeRate){

        var baseCurrencyId = originalExchangeRate.getBaseCurrencyId();
        var targetCurrencyId = originalExchangeRate.getTargetCurrencyId();
        BigDecimal originalRate = originalExchangeRate.getRate();
        BigDecimal reverseRate = BigDecimal.ONE.divide(originalRate, DECIMAL_SCALE, ROUNDING_MODE);

        ExchangeRate reversedExchangeRate = new ExchangeRate();
        reversedExchangeRate.setRate(reverseRate);
        reversedExchangeRate.setBaseCurrencyId(targetCurrencyId);
        reversedExchangeRate.setTargetCurrencyId(baseCurrencyId);
        reversedExchangeRate.setId(originalExchangeRate.getId());
        return reversedExchangeRate;
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
