package com.tankWar.server;


import com.tankWar.game.server.Config;

import java.sql.*;

public class DBOperator {
    Connection connection = null;

    public DBOperator() {
        try {
            connection = DriverManager.getConnection(Config.getDbURL(), Config.getDbUserName() , Config.getDbPassword());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DBOperator(String dbURL, String userName, String passWord) {
        try {
            connection = DriverManager.getConnection(dbURL, userName , passWord);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 判断注册用户的昵称是否重复
    public boolean isExistUserName(String name) {
        String query = "SELECT * FROM users WHERE nickname = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            return false;
        }
    }

    // 判断注册的用户账号是否重复
    public boolean isExistUser(String account) {
        String query = "SELECT * FROM users WHERE account = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, account);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            return false;
        }
    }

    //判断用户名和密码是否正确
    public boolean checkLogin(String account, String password) {
        String query = "SELECT * FROM users WHERE account = ? AND password = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, account);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean createPlayer(String name, String account, String password) {
        try {
            String insertQuery = "INSERT INTO users (nickname, account,password) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(insertQuery);
            statement.setString(1, name);
            statement.setString(2, account);
            statement.setString(3, password);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getNickName(String account) {
        String insertQuery = "SELECT nickname FROM users WHERE account = ? ";

        try {
            PreparedStatement statement = connection.prepareStatement(insertQuery);
            statement.setString(1, account);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}