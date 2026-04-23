package viewmodel;

import dao.DbConnectivityClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;


public class SignUpController {

    private static final String USERNAME_REGEX = "^(?=.{5,20}$)[A-Za-z][A-Za-z0-9_]*$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,20}$";

    @FXML
    private TextField signupUsernameField;

    @FXML
    private PasswordField signupPasswordField;

    @FXML
    private GridPane rootpane;

    @FXML
    public void initialize() {
        rootpane.setFocusTraversable(true);

        rootpane.setOnMousePressed(event -> {
            rootpane.requestFocus();
        });
    }

    public void createNewAccount(ActionEvent actionEvent) {
        DbConnectivityClass.ensureAccountsTable();

        String username = signupUsernameField.getText().trim();
        String password = signupPasswordField.getText().trim();

        if (!isValidUsername(username)) {
            showAlert("Error",
                    "Username must be 5-20 characters,\nstart with a letter,\nand contain only letters, numbers, or underscores.");
            return;
        }

        if (!isValidPassword(password)) {
            showAlert("Error",
                    "Password must be 8-20 characters\nand include at least:\n1 uppercase letter,\n1 lowercase letter,\n1 number,\nand 1 special character.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DbConnectivityClass.DB_URL)) {

            String checkSQL = "SELECT * FROM accounts WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
            checkStmt.setString(1, username);

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                showAlert("Error", "Username already exists");
                rs.close();
                checkStmt.close();
                return;
            }

            rs.close();
            checkStmt.close();

            String insertSQL = "INSERT INTO accounts (username, user_password) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.executeUpdate();
            insertStmt.close();

            showAlert("Success", "Account created successfully!");
            signupUsernameField.clear();
            signupPasswordField.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Something went wrong creating the account.");
        }
    }

    public void goBack(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.setTitle("Student Profile Manager - Login");
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert (String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidUsername(String text) {
        return text != null && text.trim().matches(USERNAME_REGEX);
    }

    private boolean isValidPassword(String text) {
        return text != null && text.trim().matches(PASSWORD_REGEX);
    }

}
