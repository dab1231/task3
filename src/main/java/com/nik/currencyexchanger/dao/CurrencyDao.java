package com.nik.currencyexchanger.dao;

import com.nik.currencyexchanger.entity.Currency;
import com.nik.currencyexchanger.exception.CurrencyAlreadyExistsException;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.util.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao {

    private static final CurrencyDao INSTANCE = new CurrencyDao();

    private static final String INSERT_SQL = """
            INSERT INTO Currency (code, full_name, sign) 
            VALUES
            (?,?,?) 
            """;

    private static final String FIND_ALL_SQL = """
            SELECT id, code, full_name, sign FROM Currency
            """;

    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + " WHERE id = ?";

    private static final String UPDATE_SQL = """
            UPDATE Currency
            SET code = ?,
                full_name = ?,
                sign = ?
            WHERE id = ?
            """;

    private static final String SELECT_BY_CODE_SQL = """
            SELECT id, code, full_name, sign FROM Currency
            WHERE code = ?
            """;

    private CurrencyDao(){

    }

    public static CurrencyDao getInstance(){
        return INSTANCE;
    }

    public Optional<Currency> findByCode(String code){
        try (var connection = ConnectionManager.get();
            var preparedStatement = connection.prepareStatement(SELECT_BY_CODE_SQL)) {
            preparedStatement.setString(1, code);
            var resultSet = preparedStatement.executeQuery();

            Currency currency = null;
            if(resultSet.next()){
                currency = buildCurrency(resultSet);
            }
            return Optional.ofNullable(currency);
        } catch (SQLException e) {
            throw new DataBaseException("DB error",e);
        }
    }

    public Currency create(Currency currency){
        try (var connection = ConnectionManager.get();
            var preparedStatement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFull_name());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.executeUpdate();

            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()){
                currency.setId(generatedKeys.getInt(1));
            }
            return currency;
        } catch (SQLException e) {
            int errorCode = e.getErrorCode();
            if(errorCode == 19 || errorCode == 2067){
                throw new CurrencyAlreadyExistsException(currency.getCode());
            }
            throw new DataBaseException("Failed to insert currency",e);
        }
    }

    public List<Currency> findAll(){
        try (var connection = ConnectionManager.get();
            var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();

            List<Currency> currencies = new ArrayList<>();
            while(resultSet.next()){
                currencies.add(buildCurrency(resultSet));
            }
            return currencies;
        } catch (SQLException e) {
            throw new DataBaseException("DB error",e);
        }
    }

    private Currency buildCurrency(ResultSet resultSet) throws SQLException {
        return  new Currency(
                resultSet.getInt("id"),
                resultSet.getString("code"),
                resultSet.getString("full_name"),
                resultSet.getString("sign")
        );
    }

    public Optional<Currency> findById(int id){
        try (var connection = ConnectionManager.get();
            var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setInt(1,id);
            var resultSet = preparedStatement.executeQuery();

            Currency currency = null;
            if(resultSet.next()){
                currency = buildCurrency(resultSet);
            }
            return Optional.ofNullable(currency);
        } catch (SQLException e) {
            throw new DataBaseException("DB error",e);
        }
    }

    public void update(Currency currency){
        try (var connection = ConnectionManager.get();
             var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFull_name());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.setInt(4, currency.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataBaseException("DB error",e);
        }
    }
}
