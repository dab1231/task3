package com.nik.currencyexchanger.dao;

import com.nik.currencyexchanger.entity.ExchangeRate;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ExchangeRateAlreadyExistsException;
import com.nik.currencyexchanger.exception.ExchangeRateNotFoundException;
import com.nik.currencyexchanger.util.ConnectionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDao {

    private static final ExchangeRateDao INSTANCE = new ExchangeRateDao();

    private static final String SELECT_ALL_SQL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM ExchangeRates
            """;

    private static final String SELECT_BY_CURRENCIES_ID_SQL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM ExchangeRates
            WHERE base_currency_id = ? AND target_currency_id = ? 
            """;

    private static final String INSERT_SQL = """
            INSERT INTO ExchangeRates (base_currency_id, target_currency_id, rate) 
            VALUES (?, ?, ?) 
            """;

    private static final String UPDATE_SQL = """
            UPDATE ExchangeRates
            SET rate = ?
            WHERE base_currency_id = ? AND target_currency_id = ?
            """;

    private ExchangeRateDao(){

    }

    public static ExchangeRateDao getInstance(){
        return INSTANCE;
    }

    public Optional<ExchangeRate> update(int baseCurrencyId, int targetCurrencyId, BigDecimal rate){
        try (var connection = ConnectionManager.get();
            var  preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setBigDecimal(1, rate);
            preparedStatement.setInt(2, baseCurrencyId);
            preparedStatement.setInt(3, targetCurrencyId);

            var executed = preparedStatement.executeUpdate();
            if(executed == 0){
                throw new ExchangeRateNotFoundException();
            }
            return findByCurrenciesId(baseCurrencyId, targetCurrencyId);
        } catch (SQLException e){
            throw new DataBaseException("Failed to update exchange rate",e);
        }
    }

    public ExchangeRate save(int baseCurrencyId, int targetCurrencyId, BigDecimal rate){
        try (var connection = ConnectionManager.get();
            var preparedStatement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, baseCurrencyId);
            preparedStatement.setInt(2,targetCurrencyId);
            preparedStatement.setBigDecimal(3, rate);
            preparedStatement.executeUpdate();

            var resultSet = preparedStatement.getGeneratedKeys();
            if(resultSet.next()){
                var id = resultSet.getInt(1);
                return new ExchangeRate(
                        id,
                        baseCurrencyId,
                        targetCurrencyId,
                        rate
                );
            }
            throw new DataBaseException("Fail with generate id");

        }catch (SQLException e){
            int errorCode = e.getErrorCode();
            if(errorCode == 19 || errorCode == 2067){
                throw new ExchangeRateAlreadyExistsException(baseCurrencyId, targetCurrencyId);
            }
            throw new DataBaseException("DB error", e);
        }
    }

    public List<ExchangeRate> findAll(){
        try (var connection = ConnectionManager.get();
            var preparedStatement = connection.prepareStatement(SELECT_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();

            List<ExchangeRate> exchangeRates = new ArrayList<>();
            while (resultSet.next()){
                exchangeRates.add(buildExchangeRate(resultSet));
            }
            return exchangeRates;
        } catch (SQLException e) {
            throw new DataBaseException("DB error",e);
        }
    }

    public Optional<ExchangeRate> findByCurrenciesId(int baseId, int targetId){
        try (var connection = ConnectionManager.get();
            var preparedStatement = connection.prepareStatement(SELECT_BY_CURRENCIES_ID_SQL)) {
            preparedStatement.setInt(1,baseId);
            preparedStatement.setInt(2,targetId);

            var resultSet = preparedStatement.executeQuery();

            ExchangeRate exchangeRateFromDb = null;
            if (resultSet.next()) {
                exchangeRateFromDb = buildExchangeRate(resultSet);
            }
            return Optional.ofNullable(exchangeRateFromDb);
        } catch (SQLException e) {
            throw new DataBaseException("DB error",e);
        }
    }

    private ExchangeRate buildExchangeRate(ResultSet resultSet) throws SQLException {
        return  new ExchangeRate(
                resultSet.getInt("id"),
                resultSet.getInt("base_currency_id"),
                resultSet.getInt("target_currency_id"),
                resultSet.getBigDecimal("rate")
        );
    }

}
