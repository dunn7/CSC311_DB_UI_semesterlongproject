package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Person;
import service.MyLogger;

import java.sql.*;
public class DbConnectivityClass {
    public final static String DB_URL = "jdbc:derby:CSC311_DB;create=true";//update this database name

        MyLogger lg= new MyLogger();

        private final ObservableList<Person> data = FXCollections.observableArrayList();

        // Method to retrieve all data from the database and store it into an observable list to use in the GUI tableview.


    public ObservableList<Person> getData() {
        data.clear();
        connectToDatabase();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement preparedStatement = conn.prepareStatement("SELECT * FROM users");
             ResultSet resultSet = preparedStatement.executeQuery()) {

            boolean hasData = false;

            while (resultSet.next()) {
                hasData = true;

                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                String major = resultSet.getString("major");
                String email = resultSet.getString("email");
                String imageURL = resultSet.getString("imageURL");

                data.add(new Person(id, first_name, last_name, department, major, email, imageURL));
            }

            if (!hasData) {
                lg.makeLog("No data");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    public boolean connectToDatabase() {
        boolean hasRegisteredUsers = false;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement statement = conn.createStatement()) {

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
                System.out.println("users table created");
            } catch (SQLException e) {
                System.out.println("users table already exists");
            }

            try {
                String accountsTableSql = "CREATE TABLE accounts ("
                        + "id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
                        + "username VARCHAR(100) NOT NULL UNIQUE,"
                        + "user_password VARCHAR(100) NOT NULL)";
                statement.executeUpdate(accountsTableSql);
                System.out.println("accounts table created");
            } catch (SQLException e) {
                System.out.println("accounts table already exists");
            }

            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");

            if (resultSet.next()) {
                int numUsers = resultSet.getInt(1);
                if (numUsers > 0) {
                    hasRegisteredUsers = true;
                }
            }

            resultSet.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hasRegisteredUsers;
    }

        public static void ensureAccountsTable() {
            try (Connection conn = DriverManager.getConnection(DB_URL);
            Statement statement = conn.createStatement()) {

            statement.executeUpdate(
                    "CREATE TABLE accounts ("
                        + "id INT GENERATED  ALWAYS AS IDENTITY PRIMARY KEY, "
                        + "username VARCHAR(100) NOT NULL UNIQUE, "
                        + "user_password VARCHAR(100) NOT NULL)"
            );

                System.out.println("accounts table created");

            } catch (SQLException e) {
                // X0Y32 = table already exists
                if ("X0Y32".equals(e.getSQLState())) {
                    System.out.println("accounts table already exists");
                } else{
                    e.printStackTrace();
                }
            }
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

    public boolean emailExists(String email) {
        connectToDatabase();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM users WHERE email = ?")) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    }