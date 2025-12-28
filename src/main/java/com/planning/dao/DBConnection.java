package com.planning.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class DBConnection {
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
            Properties p = new Properties();
            if (in != null) {
                p.load(in);
                url = p.getProperty("db.url");
                user = p.getProperty("db.user");
                password = p.getProperty("db.password");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}