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
import javafx.stage.Stage;

import java.sql.*;


public class SignUpController {

    @FXML
    private TextField signupUsernameField;

    @FXML
    private PasswordField signupPasswordField;

    public void createNewAccount(ActionEvent actionEvent) {
        DbConnectivityClass.ensureAccountsTable();

        String username = signupUsernameField.getText().trim();
        String password = signupPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Username and password cannot be empty");
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

}
