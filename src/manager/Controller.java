package manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Controller {

    @FXML
    private final ObservableList<String> damageTypes = FXCollections.observableArrayList(

            "Water",
            "Fire",
            "Wind"

    );

    @FXML
    private TextField firstName;

    @FXML
    private TextField lastName;

    @FXML
    private DatePicker dateOfLoss;

    @FXML
    private ComboBox damageType = new ComboBox();

    @FXML
    private TextField aagNumber;

    @FXML
    private TextField status;

    private FileManager fileManager = new FileManager();

    final private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM.dd.yyyy");

    @FXML
    void handleCreateButton() throws IOException {

        if(!lastName.getText().equals("") || !(dateOfLoss.getValue() == null) || !aagNumber.getText().equals("")) {

            if(fileManager.getFileQueue().size() == 0 && fileManager.getPictureQueue().size() == 0) {

                Alert noFilesAlert = new Alert(Alert.AlertType.CONFIRMATION,
                        "No files or pictures have been added. Is "
                                + "this correct? You will have to add them manually later.",
                        ButtonType.OK,
                        ButtonType.CANCEL);
                noFilesAlert.showAndWait();

                if(noFilesAlert.getResult() == ButtonType.OK) {

                    // if this method returns false that means it was not able to create the directory
                    if(!fileManager.createFileDirectory(firstName.getText(),
                            lastName.getText(),
                            dateFormat.format(dateOfLoss.getValue()),
                            aagNumber.getText())) {
                        status.setText("Something went wrong while making the directory.");
                    }
                }
            } else {
                if(!fileManager.createFileDirectory(firstName.getText(),
                        lastName.getText(),
                        dateFormat.format(dateOfLoss.getValue()),
                        aagNumber.getText())) {
                    status.setText("Something went wrong while making the directory");
                }

                // Move the files and update the status text.
                status.setText(fileManager.moveFiles());
            }
        } else {
            Alert requiredFieldsEmptyAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "You must fill in each field containing a '*' symbol before proceeding.",
                    ButtonType.OK);
            requiredFieldsEmptyAlert.showAndWait();
        }
    }

    @FXML
    void populateDamageTypes(MouseEvent mouseEvent) {
        damageType.setItems(damageTypes);
    }

    @FXML
    void clearForm(ActionEvent actionEvent) {
        firstName.setText(null);
        lastName.setText(null);
        dateOfLoss.setValue(null);
        damageType.setValue(null);
        aagNumber.setText(null);
        status.setText(null);
        fileManager.clearLists();
    }

    @FXML
    void chooseFiles() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select claim files");
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(new Stage());

        if(selectedFiles != null) {
            status.setText(fileManager.sortFiles(selectedFiles));
        }
    }

    @FXML
    void handleDragDrop(DragEvent event) throws FileNotFoundException {

        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            success = true;
            status.setText(fileManager.sortFiles(db.getFiles()));
        }

        event.setDropCompleted(success);
        event.consume();

    }

    @FXML
    /**
     * Called when the user clicks on "Set Claim Directory" under the edit menu.
     * Will call initializeClaimDirectory() in the FileManager class to set a new
     * working directory for claim files.
     */
    void setWorkingDirectory() {
        fileManager.initializeClaimDirectory();
        status.setText("Set claim directory to " + fileManager.getClaimDirectory());
    }

    @FXML
    void exit(ActionEvent actionEvent) {
        System.exit(0);
    }
}