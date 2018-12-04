package com.paypal.desk;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbHelper {

    private static final Connection connection = getConnection();

    private static Connection getConnection() {
        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/paypal",
                    "root",
                    "root"
            );

            System.out.println("Connection successful");
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    static int createUser(String firstName, String lastName) {
        String sql = "insert into users " +
                "(first_name, last_name)" +
                " values (" +
                "?" +
                ", " +
                "?" +
                ")";

        try {

            executeStatement(sql,firstName,lastName);

            String idSql = "select max(id) from users";
            ResultSet resultSet = executeQuery(idSql);

            resultSet.next();

            return resultSet.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Updates the user balance in database
     * Sets balance = balance + amount
     *
     * @param userId id of the user in users table
     * @param amount double value of the amount to insert
     */
    static void cashFlow(int userId,double amount) {

        Double currentCash = null;

        try{
            currentCash = getUserCash(userId);
        }catch (SQLException e){
            e.printStackTrace();
            System.out.println("Wrong user id");
            return;
        }

        Double newCash = currentCash + amount;

        String cashInSql = "UPDATE users" + " "
                + "SET balance=" + newCash + " "
                +"where id=" + userId;

        try{
            executeStatement(cashInSql);
        }catch (SQLException e){
            e.printStackTrace();
        }

        System.out.println("Cash in successful");
    }

    private static void executeStatement(String statement,Object... arr) throws SQLException{
        PreparedStatement preparedStatement = connection.prepareStatement(statement);

        for(int i=0;i<arr.length;++i){
            preparedStatement.setObject(i + 1,arr[i]);
        }

        preparedStatement.execute();
    }

    private static ResultSet executeQuery(String query,Object... arr) throws SQLException{

        PreparedStatement executor = connection.prepareStatement(query);

        for(int i=0;i<arr.length;++i){
            executor.setObject(i+1,arr[i]);
        }


       return  executor.executeQuery();

    }

    private static Double getUserCash(int id) throws SQLException {
        String getCash = "SELECT balance FROM users WHERE id = " + "?";

        ResultSet resultSet = null;

        resultSet = executeQuery(getCash,id);
        resultSet.next();
        return resultSet.getDouble("balance");

    }

    /**
     * Emulates a transaction between 2 users
     * Takes money from one account and adds to another account
     *
     * @param userFrom source user id
     * @param userTo   target user id
     * @param amount   transaction amount
     */
    static void transaction(int userFrom, int userTo, double amount) {
      Double userFromCash = null;
      Double userToCash = null;

      if(amount < 0){
          System.out.println("Funds can't be negative");
          return;
      }

      try{
          userFromCash = getUserCash(userFrom);
      }catch (SQLException e){
          System.out.println("Wrong userFrom id");
          return;
      }

      try{
          userToCash = getUserCash(userTo);
      }catch (SQLException e){
          System.out.println("Wrong userTo id");
      }

      if(userFromCash < amount){
          System.out.println("Insufficient funds fro transaction");
          return;
      }

      cashFlow(userFrom,-amount);
      cashFlow(userTo,amount);

    }

    static List<User> listUsers() {
        String sql = "select * from users";

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            List<User> userList = new ArrayList<>();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                double balance = resultSet.getDouble("balance");

                userList.add(new User(
                        id, firstName, lastName, balance
                ));
            }
            return userList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
