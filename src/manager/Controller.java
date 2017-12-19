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
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Controller {

    @FXML
    private final ObservableList<String> damageTypes = FXCollections.observableArrayList(

            "Water",
            "Fire",
            "Wind"

    );

    private ArrayList<String> imgFileTypes = new ArrayList<>();

    private ArrayList<File> fileQueue = new ArrayList<>();
    private ArrayList<File> pictureQueue = new ArrayList<>();

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

    private final File rootDir = new File("C:\\");
    String claimDirPath;

    final private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM.dd.yyyy");

    @FXML
    void handleCreateButton() throws IOException {

        if(Objects.equals(lastName.getText(), "") || dateOfLoss.getValue() == null || Objects.equals(aagNumber.getText(), "")) {

            status.setText("Please fill in the required fields denoted by '*'.");

        } else {

            if(fileQueue.size() == 0 && pictureQueue.size() == 0) {

                Alert noFilesAlert = new Alert(Alert.AlertType.CONFIRMATION, "No files or pictures have been added. Is " +
                        "this correct? You will have to add them manually later.", ButtonType.OK, ButtonType.CANCEL);
                noFilesAlert.showAndWait();

                if(noFilesAlert.getResult() == ButtonType.OK) {

                    createFilesystem();
                    moveFiles();

                } else if(noFilesAlert.getResult() == ButtonType.CANCEL) {



                }

            } else {

                createFilesystem();
                moveFiles();

            }

        }

    }

    void createFilesystem() throws IOException {

        if (rootDir.exists() && rootDir.isDirectory()) {

            status.setText("Root directory located, creating filesystem...");
            createClientDir(rootDir);

        } else if(!rootDir.canWrite()){

            status.setText("Root directory could not be accessed. Contact Admin.");

        } else {

            status.setText("Something went wrong. Contact Admin.");

        }

    }

    private void createClientDir(File root) {

        String formattedDate = dateFormat.format(dateOfLoss.getValue());

        File claimDir = new File(root.getPath() + "\\" +
                lastName.getText() + " " + firstName.getText() + " " + formattedDate + " AAG " + aagNumber.getText());

        claimDirPath = claimDir.getPath();

        try {
            if(!claimDir.exists()) {
                claimDir.mkdir();
                status.setText("File system created successfully.");
            } else {
                status.setText("File system already available. Proceeding...");
            }

        } catch (Exception e) {

            status.setText("Something went wrong.");

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
        flushQueue();

    }

    @FXML
    void chooseFiles() {

        FileChooser fileChooser = new FileChooser();

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(new Stage());

        if(selectedFiles != null) {

            sortFiles(selectedFiles);
            System.out.println(selectedFiles.size());
            for(File f : selectedFiles) {
                System.out.println(f.getName());
            }

        }

    }

    @FXML
    void handleDragDrop(DragEvent event) throws FileNotFoundException {

        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            sortFiles(db.getFiles());
        }
        event.setDropCompleted(success);
        event.consume();

    }


    /**
     * Puts image files into pictureQueue list and other files into fileQueue list
     * @param files
     */
    private void sortFiles(List<File> files) {

        imgFileTypes.add(".png");
        imgFileTypes.add(".jpeg");
        imgFileTypes.add(".jpg");
        imgFileTypes.add(".gif");
        imgFileTypes.add(".tiff");
        imgFileTypes.add(".bmp");

        // Iterate through each file in the files list
        for(int i = 0; i < files.size(); ++i) {

            File file = files.get(i);

            if(file != null) {

                String fileName = file.getName();

                // Iterate through imgFileTypes list
                // to find picture file extensions
                // then add to appropriate list
                boolean added = false;
                for(String type : imgFileTypes) {
                    if(fileName.endsWith(type)) {
                        pictureQueue.add(file);
                        added = true;
                    }
                }
                if(added == false) {
                    fileQueue.add(file);
                }
            }
        }
        System.out.println("Sorted files. \nfileQueue size: " + fileQueue.size() + "\npictureQueue size: " + pictureQueue.size());
        status.setText("Loaded " + pictureQueue.size() + " pictures and " + fileQueue.size() + " other files successfully.");
    }

    private void moveFiles() throws IOException {

        if(!fileQueue.isEmpty()) {


            for (int i = 0; i < fileQueue.size(); ++i) {

                File file = fileQueue.get(i);
                Path filePath = Paths.get(file.getAbsolutePath());
                Path claimPath = Paths.get(claimDirPath + "\\" + file.getName());

                Path temp = Files.copy(filePath, claimPath);

                if(temp != null) {
                    status.setText("Files migrated successfully.");
                    System.out.println("file migration: success.");
                }
            }
        }

        if(!pictureQueue.isEmpty()) {

            File pictureDir = new File(claimDirPath + "\\Pictures");
            if(!pictureDir.exists()) {
                pictureDir.mkdir();
            }


            for (int i = 0; i < pictureQueue.size(); ++i) {

                File file = pictureQueue.get(i);
                Path filePath = Paths.get(file.getAbsolutePath());
                Path picturePath = Paths.get(pictureDir.getAbsolutePath() + "\\" + file.getName());

                Path temp = Files.copy(filePath, picturePath);

                if(temp != null) {
                    status.setText("Files migrated successfully.");
                    System.out.println("picture migration: success.");
                }
            }

        }

        flushQueue();

    }

    @FXML
    void exit(ActionEvent actionEvent) {

        System.exit(0);

    }

    void flushQueue() {

        fileQueue.clear();
        pictureQueue.clear();

    }

}
