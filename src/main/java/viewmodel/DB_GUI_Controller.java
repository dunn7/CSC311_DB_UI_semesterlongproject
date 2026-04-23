package viewmodel;

import dao.DbConnectivityClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TableRow;
import model.Major;
import model.Person;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import service.MyLogger;
import service.UserSession;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DB_GUI_Controller implements Initializable {

    private static final String NAME_REGEX = "^(?=.{2,40}$)[A-Z][a-z][a-zA-Z' -]*$";
    private static final String DEPARTMENT_REGEX = "^(?=.{2,50}$)[A-Za-z][A-Za-z &-]*$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String IMAGE_REGEX = "^$|^(?i).+\\.(jpg|jpeg|png|gif|bmp)$";

    @FXML
    private TextField first_name, last_name, department, email, imageURL;

    @FXML
    private ComboBox<Major> majorComboBox;

    @FXML
    private ImageView img_view;

    @FXML
    private MenuBar menuBar;

    @FXML
    private TableView<Person> tv;

    @FXML
    private TableColumn<Person, Integer> tv_id;

    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;

    @FXML
    private Button editBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private MenuItem editItem;

    @FXML
    private MenuItem deleteItem;

    @FXML
    private Button addBtn;

    @FXML
    private Label statusLabel;

    @FXML
    private AnchorPane anchorPane;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();

    /**
     * Temporary visual row used for "click empty row to add".
     * This row exists only in the table until the user clicks Add.
     * It is NOT inserted into the database until a valid add happens.
     */
    private Person draftRow = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

            majorComboBox.setItems(FXCollections.observableArrayList(Major.values()));
            majorComboBox.getSelectionModel().clearSelection();

            tv.setEditable(true);
            tv_fn.setEditable(true);
            tv_ln.setEditable(true);
            tv_department.setEditable(true);
            tv_major.setEditable(true);
            tv_email.setEditable(true);

            // Inline editing for existing rows
            tv_fn.setCellFactory(TextFieldTableCell.forTableColumn());
            tv_ln.setCellFactory(TextFieldTableCell.forTableColumn());
            tv_department.setCellFactory(TextFieldTableCell.forTableColumn());
            tv_email.setCellFactory(TextFieldTableCell.forTableColumn());

            tv_fn.setOnEditCommit(event -> {
                Person person = event.getRowValue();
                if (person == draftRow) {
                    updateStatus("Use the form on the right to enter new student details, then click Add.");
                    tv.refresh();
                    return;
                }

                String newValue = event.getNewValue().trim();
                if (!isValidName(newValue)) {
                    showAlert("Validation Error", "First name is invalid.");
                    tv.refresh();
                    return;
                }

                person.setFirstName(newValue);
                cnUtil.editUser(person.getId(), person);
                updateStatus("First name updated.");
            });

            tv_ln.setOnEditCommit(event -> {
                Person person = event.getRowValue();
                if (person == draftRow) {
                    updateStatus("Use the form on the right to enter new student details, then click Add.");
                    tv.refresh();
                    return;
                }

                String newValue = event.getNewValue().trim();
                if (!isValidName(newValue)) {
                    showAlert("Validation Error", "Last name is invalid.");
                    tv.refresh();
                    return;
                }

                person.setLastName(newValue);
                cnUtil.editUser(person.getId(), person);
                updateStatus("Last name updated.");
            });

            tv_department.setOnEditCommit(event -> {
                Person person = event.getRowValue();
                if (person == draftRow) {
                    updateStatus("Use the form on the right to enter new student details, then click Add.");
                    tv.refresh();
                    return;
                }

                String newValue = event.getNewValue().trim();
                if (!isValidDepartment(newValue)) {
                    showAlert("Validation Error", "Department is invalid.");
                    tv.refresh();
                    return;
                }

                person.setDepartment(newValue);
                cnUtil.editUser(person.getId(), person);
                updateStatus("Department updated.");
            });

            tv_email.setOnEditCommit(event -> {
                Person person = event.getRowValue();
                if (person == draftRow) {
                    updateStatus("Use the form on the right to enter new student details, then click Add.");
                    tv.refresh();
                    return;
                }

                String newValue = event.getNewValue().trim();
                if (!isValidEmail(newValue)) {
                    showAlert("Validation Error", "Email is invalid.");
                    tv.refresh();
                    return;
                }

                person.setEmail(newValue);
                cnUtil.editUser(person.getId(), person);
                updateStatus("Email updated.");
            });

            tv_major.setCellFactory(ComboBoxTableCell.forTableColumn("CS", "CPIS", "ENGLISH"));
            tv_major.setOnEditCommit(event -> {
                Person person = event.getRowValue();
                if (person == draftRow) {
                    updateStatus("Use the form on the right to enter new student details, then click Add.");
                    tv.refresh();
                    return;
                }

                String newValue = event.getNewValue().trim().toUpperCase();
                if (!isValidMajor(newValue)) {
                    showAlert("Validation Error", "Major is invalid.");
                    tv.refresh();
                    return;
                }

                person.setMajor(newValue);
                cnUtil.editUser(person.getId(), person);
                updateStatus("Major updated.");
            });

            // Empty row / empty table area click support
            tv.setRowFactory(table -> {
                TableRow<Person> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 1 && row.isEmpty()) {
                        prepareNewRow();
                        event.consume();
                    }
                });
                return row;
            });

            editItem.setDisable(true);
            deleteItem.setDisable(true);
            editBtn.setDisable(true);
            deleteBtn.setDisable(true);
            addBtn.setDisable(true);

            tv.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                // If a real row is selected while a draft row exists, remove the draft row.
                if (draftRow != null && newSelection != null && newSelection != draftRow) {
                    removeDraftRow(false);
                }

                boolean realSelection = newSelection != null && newSelection != draftRow;
                editItem.setDisable(!realSelection);
                deleteItem.setDisable(!realSelection);
                editBtn.setDisable(!realSelection);
                deleteBtn.setDisable(!realSelection);

                if (newSelection == null) {
                    return;
                }

                if (newSelection == draftRow) {
                    clearInputFieldsOnly();
                    updateFieldStyles();
                    updateAddButton();
                    updateStatus("Enter new student details on the right, then click Add.");
                    Platform.runLater(() -> first_name.requestFocus());
                } else {
                    loadPersonIntoForm(newSelection);
                    updateFieldStyles();
                    updateAddButton();
                }
            });

            first_name.textProperty().addListener((obs, oldVal, newVal) -> updateAddButton());
            last_name.textProperty().addListener((obs, oldVal, newVal) -> updateAddButton());
            department.textProperty().addListener((obs, oldVal, newVal) -> updateAddButton());
            email.textProperty().addListener((obs, oldVal, newVal) -> updateAddButton());
            imageURL.textProperty().addListener((obs, oldVal, newVal) -> updateAddButton());
            majorComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateAddButton());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        anchorPane.setFocusTraversable(true);
        anchorPane.setOnMousePressed(event -> anchorPane.requestFocus());
    }

    @FXML
    protected void addNewRecord() {
        if (!isFormValid()) {
            showAlert("Validation Error",
                    "Please enter valid data.\n"
                            + "First/Last name must start with a capital letter.\n"
                            + "Department must contain only letters/spaces.\n"
                            + "Email must be valid.\n"
                            + "Image must be blank or end in .jpg, .jpeg, .png, .gif, or .bmp.");
            return;
        }

        Person p = new Person(
                first_name.getText().trim(),
                last_name.getText().trim(),
                department.getText().trim(),
                majorComboBox.getValue().name(),
                email.getText().trim(),
                imageURL.getText().trim()
        );

        cnUtil.insertUser(p);
        p.setId(cnUtil.retrieveId(p));

        if (draftRow != null && data.contains(draftRow)) {
            int draftIndex = data.indexOf(draftRow);
            data.set(draftIndex, p);
            draftRow = null;
        } else {
            data.add(p);
        }

        tv.getSelectionModel().select(p);
        loadPersonIntoForm(p);
        updateFieldStyles();
        updateAddButton();
        updateStatus("User added successfully.");
    }

    @FXML
    protected void clearForm() {
        removeDraftRow(true);
        clearInputFieldsOnly();
        updateFieldStyles();
        updateAddButton();
        updateStatus("Form cleared.");
        Platform.runLater(() -> first_name.requestFocus());
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            if (UserSession.getCurrentSession() != null) {
                UserSession.getCurrentSession().cleanUserSession();
            }

            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());

            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p == null || p == draftRow) {
            return;
        }

        if (!isFormValid()) {
            showAlert("Validation Error", "Please enter valid data before editing.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Edit");
        alert.setHeaderText("Update User Record");
        alert.setContentText("Are you sure you want to update this user?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            int index = data.indexOf(p);

            Person p2 = new Person(
                    p.getId(),
                    first_name.getText().trim(),
                    last_name.getText().trim(),
                    department.getText().trim(),
                    majorComboBox.getValue().name(),
                    email.getText().trim(),
                    imageURL.getText().trim()
            );

            cnUtil.editUser(p.getId(), p2);
            data.remove(p);
            data.add(index, p2);
            tv.getSelectionModel().select(index);

            updateStatus("User updated successfully.");
        }
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p == null || p == draftRow) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User Record");
        alert.setContentText("Are you sure you want to delete " + p.getFirstName() + " " + p.getLastName() + "?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            int index = data.indexOf(p);
            cnUtil.deleteRecord(p);
            data.remove(index);

            clearInputFieldsOnly();
            tv.getSelectionModel().clearSelection();
            updateFieldStyles();
            updateAddButton();
            updateStatus("User deleted successfully.");
        }
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p == null) {
            return;
        }

        if (p == draftRow) {
            clearInputFieldsOnly();
            updateFieldStyles();
            updateAddButton();
            updateStatus("Enter new student details on the right, then click Add.");
            Platform.runLater(() -> first_name.requestFocus());
            return;
        }

        loadPersonIntoForm(p);
        updateFieldStyles();
        updateAddButton();
    }

    /**
     * Keep this method in case your FXML uses onMouseClicked="#handleTableClick".
     * Empty row handling is already done by the rowFactory in initialize().
     */
    @FXML
    private void handleTableClick(MouseEvent event) {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            selectedItemTV(event);
        } else {
            prepareNewRow();
        }
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options = FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();

        dialogPane.setContent(new VBox(8, textField1, textField2, textField3, comboBox));
        Platform.runLater(textField1::requestFocus);

        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(), textField2.getText(), comboBox.getValue());
            }
            return null;
        });

        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(results.fname + " " + results.lname + " " + results.major);
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isFormValid() {
        return isValidName(first_name.getText())
                && isValidName(last_name.getText())
                && isValidDepartment(department.getText())
                && majorComboBox.getValue() != null
                && isValidEmail(email.getText())
                && isValidImage(imageURL.getText());
    }

    private boolean isValidName(String text) {
        return text != null && text.trim().matches(NAME_REGEX);
    }

    private boolean isValidDepartment(String text) {
        return text != null && text.trim().matches(DEPARTMENT_REGEX);
    }

    private boolean isValidEmail(String text) {
        return text != null && text.trim().matches(EMAIL_REGEX);
    }

    private boolean isValidImage(String text) {
        return text != null && text.trim().matches(IMAGE_REGEX);
    }

    private boolean isValidMajor(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        try {
            Major.valueOf(text.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void updateAddButton() {
        updateFieldStyles();
        addBtn.setDisable(!isFormValid());
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void updateFieldStyles() {
        String validStyle = "";
        String invalidStyle = "-fx-border-color: red; -fx-border-width: 2;";

        first_name.setStyle(isValidName(first_name.getText()) || first_name.getText().trim().isEmpty() ? validStyle : invalidStyle);
        last_name.setStyle(isValidName(last_name.getText()) || last_name.getText().trim().isEmpty() ? validStyle : invalidStyle);
        department.setStyle(isValidDepartment(department.getText()) || department.getText().trim().isEmpty() ? validStyle : invalidStyle);
        email.setStyle(isValidEmail(email.getText()) || email.getText().trim().isEmpty() ? validStyle : invalidStyle);
        imageURL.setStyle(isValidImage(imageURL.getText()) || imageURL.getText().trim().isEmpty() ? validStyle : invalidStyle);

        if (majorComboBox.getValue() == null) {
            majorComboBox.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        } else {
            majorComboBox.setStyle("");
        }
    }

    private void loadPersonIntoForm(Person p) {
        if (p == null) {
            return;
        }

        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());

        try {
            majorComboBox.setValue(Major.valueOf(p.getMajor().toUpperCase()));
        } catch (Exception e) {
            majorComboBox.getSelectionModel().clearSelection();
        }
    }

    private void clearInputFieldsOnly() {
        first_name.clear();
        last_name.clear();
        department.clear();
        email.clear();
        imageURL.clear();
        majorComboBox.getSelectionModel().clearSelection();
    }

    private void prepareNewRow() {
        if (draftRow == null || !data.contains(draftRow)) {
            draftRow = new Person("", "", "", "", "", "");
            data.add(draftRow);
        }

        tv.getSelectionModel().select(draftRow);
        clearInputFieldsOnly();
        updateFieldStyles();
        updateAddButton();
        updateStatus("Enter new student details on the right, then click Add.");
        Platform.runLater(() -> first_name.requestFocus());
    }

    private void removeDraftRow(boolean clearSelection) {
        if (draftRow != null && data.contains(draftRow)) {
            if (clearSelection && tv.getSelectionModel().getSelectedItem() == draftRow) {
                tv.getSelectionModel().clearSelection();
            }
            data.remove(draftRow);
        }
        draftRow = null;
    }

    private static class Results {
        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    private String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    @FXML
    private void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("users.csv");

        File file = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            writer.write("id,first_name,last_name,department,major,email,imageURL");
            writer.newLine();

            for (Person p : data) {
                // Skip draft row from export
                if (p == draftRow) {
                    continue;
                }

                writer.write(
                        p.getId() + "," +
                                escapeCSV(p.getFirstName()) + "," +
                                escapeCSV(p.getLastName()) + "," +
                                escapeCSV(p.getDepartment()) + "," +
                                escapeCSV(p.getMajor()) + "," +
                                escapeCSV(p.getEmail()) + "," +
                                escapeCSV(p.getImageURL())
                );
                writer.newLine();
            }

            updateStatus("CSV exported successfully.");
            showAlert("Success", "CSV exported successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to export CSV.");
        }
    }

    @FXML
    private void importCSV() {
        int lineNumber = 0;
        List<Integer> badRowLines = new ArrayList<>();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            String line;
            int importedCount = 0;
            int duplicateCount = 0;
            int badRowCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (lineNumber == 1) {
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = parseCSVLine(line);

                if (parts.length < 7) {
                    badRowCount++;
                    badRowLines.add(lineNumber);
                    continue;
                }

                String firstName = parts[1].trim();
                String lastName = parts[2].trim();
                String departmentValue = parts[3].trim();
                String majorValue = parts[4].trim();
                String emailValue = parts[5].trim();
                String imageValue = parts[6].trim();

                if (!isValidName(firstName)
                        || !isValidName(lastName)
                        || !isValidDepartment(departmentValue)
                        || !isValidMajor(majorValue)
                        || !isValidEmail(emailValue)
                        || !isValidImage(imageValue)) {
                    badRowCount++;
                    badRowLines.add(lineNumber);
                    continue;
                }

                if (cnUtil.emailExists(emailValue)) {
                    duplicateCount++;
                    continue;
                }

                Person p = new Person(
                        firstName,
                        lastName,
                        departmentValue,
                        majorValue.toUpperCase(),
                        emailValue,
                        imageValue
                );

                try {
                    cnUtil.insertUser(p);
                    p.setId(cnUtil.retrieveId(p));
                    data.add(p);
                    importedCount++;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    badRowCount++;
                    badRowLines.add(lineNumber);
                }
            }

            String msg = "Import complete:\n"
                    + "- " + importedCount + " record(s) imported\n"
                    + "- " + duplicateCount + " duplicate row(s) skipped\n"
                    + "- " + badRowCount + " invalid row(s) skipped";

            if (!badRowLines.isEmpty()) {
                msg += "\nInvalid rows at lines: " + badRowLines.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
            }

            updateStatus("CSV import complete: " + importedCount + " imported, "
                    + duplicateCount + " duplicates, "
                    + badRowCount + " invalid.");
            showAlert("Import Result", msg);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to import CSV.");
        }
    }

    @FXML
    private void generatePdfReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName("student_major_report.pdf");

        File file = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
        if (file == null) {
            return;
        }

        Map<String, Integer> majorCounts = new TreeMap<>();
        for (Person p : data) {
            if (p == draftRow) {
                continue;
            }

            String major = p.getMajor();
            if (major == null || major.trim().isEmpty()) {
                major = "UNKNOWN";
            }
            majorCounts.put(major, majorCounts.getOrDefault(major, 0) + 1);
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            float y = 720;
            float left = 70;

            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 20);
            content.newLineAtOffset(left, y);
            content.showText("Student Major Report");
            content.endText();

            y -= 30;

            content.beginText();
            content.setFont(PDType1Font.HELVETICA, 12);
            content.newLineAtOffset(left, y);
            content.showText("Generated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            content.endText();

            y -= 40;

            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.newLineAtOffset(left, y);
            content.showText("Major");
            content.endText();

            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.newLineAtOffset(left + 250, y);
            content.showText("Student Count");
            content.endText();

            y -= 20;

            content.moveTo(left, y);
            content.lineTo(left + 350, y);
            content.stroke();

            y -= 20;

            content.setFont(PDType1Font.HELVETICA, 12);
            for (Map.Entry<String, Integer> entry : majorCounts.entrySet()) {
                if (y < 80) {
                    content.close();

                    page = new PDPage(PDRectangle.LETTER);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = 720;
                }

                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(left, y);
                content.showText(entry.getKey());
                content.endText();

                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(left + 250, y);
                content.showText(String.valueOf(entry.getValue()));
                content.endText();

                y -= 20;
            }

            y -= 10;

            int total = majorCounts.values().stream().mapToInt(Integer::intValue).sum();
            content.moveTo(left, y);
            content.lineTo(left + 350, y);
            content.stroke();

            y -= 20;

            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 13);
            content.newLineAtOffset(left, y);
            content.showText("Total Students: " + total);
            content.endText();

            content.close();
            document.save(file);

            updateStatus("PDF report generated successfully.");
            showAlert("Success", "PDF report generated successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to generate PDF report.");
        }
    }
}