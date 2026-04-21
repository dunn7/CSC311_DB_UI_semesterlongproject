package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Person;
import service.MyLogger;

import java.sql.*;
public class DbConnectivityClass {
    final static String DB_URL = "jdbc:derby:CSC311_DB;create=true";//update this database name

        MyLogger lg= new MyLogger();

        private final ObservableList<Person> data = FXCollections.observableArrayList();

        // Method to retrieve all data from the database and store it into an observable list to use in the GUI tableview.


        public ObservableList<Person> getData() {
            data.clear();
            connectToDatabase();
            try {
                Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM users");
                ResultSet resultSet = preparedStatement.executeQuery();

                if (!resultSet.isBeforeFirst()) {
                    lg.makeLog("No data");
                }

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String first_name = resultSet.getString("first_name");
                    String last_name = resultSet.getString("last_name");
                    String department = resultSet.getString("department");
                    String major = resultSet.getString("major");
                    String email = resultSet.getString("email");
                    String imageURL = resultSet.getString("imageURL");

                    data.add(new Person(id, first_name, last_name, department, major, email, imageURL));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return data;
        }

        public boolean connectToDatabase() {
            boolean hasRegistredUsers = false;

            try {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

                try (Connection conn = DriverManager.getConnection(DB_URL)) {
                    Statement statement = conn.createStatement();

                    try {
                        String sql = "CREATE TABLE users ("
                                + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                                + "first_name VARCHAR(200) NOT NULL,"
                                + "last_name VARCHAR(200) NOT NULL,"
                                + "department VARCHAR(200),"
                                + "major VARCHAR(200),"
                                + "email VARCHAR(200) NOT NULL UNIQUE,"
                                + "imageURL VARCHAR(200))";
                        statement.executeUpdate(sql);
                    } catch (SQLException e) {
                        //table probably already exists
                    }

                    //check if we have users in the table users
                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");

                    if (resultSet.next()) {
                        int numUsers = resultSet.getInt(1);
                        if (numUsers > 0) {
                            hasRegistredUsers = true;
                        }

                        resultSet.close();
                        statement.close();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return hasRegistredUsers;
        }

        public void queryUserByLastName(String name) {
            connectToDatabase();
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement preparedStatement = conn.prepareStatement(
                         "SELECT * FROM users WHERE last_name = ?")) {


                preparedStatement.setString(1, name);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String first_name = resultSet.getString("first_name");
                    String last_name = resultSet.getString("last_name");
                    String major = resultSet.getString("major");
                    String department = resultSet.getString("department");

                    lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + " "
                            + ", Major: " + major + ", Department: " + department);
                }

                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void listAllUsers() {
            connectToDatabase();
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM users ");
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String first_name = resultSet.getString("first_name");
                    String last_name = resultSet.getString("last_name");
                    String department = resultSet.getString("department");
                    String major = resultSet.getString("major");
                    String email = resultSet.getString("email");

                    lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + " "
                            + ", Department: " + department + ", Major: " + major + ", Email: " + email);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void insertUser(Person person) {
            connectToDatabase();
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement preparedStatement = conn.prepareStatement(
                         "INSERT INTO users (first_name, last_name, department, major, email, imageURL) VALUES (?, ?, ?, ?, ?, ?)")) {

                preparedStatement.setString(1, person.getFirstName());
                preparedStatement.setString(2, person.getLastName());
                preparedStatement.setString(3, person.getDepartment());
                preparedStatement.setString(4, person.getMajor());
                preparedStatement.setString(5, person.getEmail());
                preparedStatement.setString(6, person.getImageURL());

                int row = preparedStatement.executeUpdate();
                if (row > 0) {
                    lg.makeLog("A new user was inserted successfully.");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void editUser(int id, Person p) {
            connectToDatabase();
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement preparedStatement = conn.prepareStatement(
                         "UPDATE users SET first_name=?, last_name=?, department=?, major=?, email=?, imageURL=? WHERE id=?")) {

                preparedStatement.setString(1, p.getFirstName());
                preparedStatement.setString(2, p.getLastName());
                preparedStatement.setString(3, p.getDepartment());
                preparedStatement.setString(4, p.getMajor());
                preparedStatement.setString(5, p.getEmail());
                preparedStatement.setString(6, p.getImageURL());
                preparedStatement.setInt(7, id);

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public void deleteRecord(Person person) {
            int id = person.getId();
            connectToDatabase();
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement preparedStatement = conn.prepareStatement(
                         "DELETE FROM users WHERE id=?")) {

                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        //Method to retrieve id from database where it is auto-incremented.
        public int retrieveId(Person p) {
            connectToDatabase();
            int id;
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement preparedStatement = conn.prepareStatement(
                         "SELECT id FROM users WHERE email=?")) {

                preparedStatement.setString(1, p.getEmail());

                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                id = resultSet.getInt("id");
                resultSet.close();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            lg.makeLog(String.valueOf(id));
            return id;
        }
    }