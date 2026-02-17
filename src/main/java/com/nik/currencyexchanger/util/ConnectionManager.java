package com.nik.currencyexchanger.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@UtilityClass
public class ConnectionManager {

    private static final int DEFAULT_POOL_SIZE = 5;
    private static final String DB_PATH = "db.path";
    private static final String POOL_SIZE = "db.pool.size";
    private static BlockingQueue<Connection> pool;
    private static List<Connection> sourceConnections;

    static {
        loadDriver();
        initConnectionPool();
    }

    private static void loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }
    }

    private static Connection open(){
        try {
            Connection connection = DriverManager.getConnection(PropertiesUtil.getProperty(DB_PATH));
            try(var statement = connection.createStatement()){
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection get(){
        try {
            return pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void close(){
        for(Connection connection : sourceConnections){
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void initConnectionPool(){
        var poolSize = PropertiesUtil.getProperty(POOL_SIZE);
        var size = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
        pool = new ArrayBlockingQueue<>(size);
        sourceConnections = new ArrayList<>(size);
        for(int i = 0; i < size; i++){
            var connection = open();
            var proxyConnection = (Connection)
                    Proxy.newProxyInstance(PropertiesUtil.class.getClassLoader(), new Class[]{Connection.class},
                    (proxy, method, args) -> method.getName().equals("close")
                        ? pool.add((Connection) proxy)
                        : method.invoke(connection,args));
            pool.add(proxyConnection);
            sourceConnections.add(connection);
        }
    }

}
