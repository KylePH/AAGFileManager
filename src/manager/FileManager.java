package manager;


import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

class FileManager {

    private ArrayList<String> imgFileTypes;
    private ArrayList<File> fileQueue;
    private ArrayList<File> pictureQueue;
    private File claimDirectory;
    private File aagDataFolder;
    private String claimDirectoryPath;
    private String slash;
    private boolean isWindows;

    FileManager() { // Remove the parameter from this constructor after completing the comments below
        imgFileTypes = new ArrayList<>();
        fileQueue = new ArrayList<>();
        pictureQueue = new ArrayList<>();

        // incorporate multi-platform support for testing on Windows and MacOS/Linux.
        String os = System.getProperty("os.name").toLowerCase();

        if(os.startsWith("windows")) {
            isWindows = true;
            slash = "\\";
        } else {
            isWindows = false;
            slash = "/";
        }

        parseClaimDirectory();

        imgFileTypes.add(".png");
        imgFileTypes.add(".jpeg");
        imgFileTypes.add(".jpg");
        imgFileTypes.add(".gif");
        imgFileTypes.add(".tiff");
        imgFileTypes.add(".bmp");
    }

    /**
     * Reads the path to the root working directory for AAG claims from
     * a text file located in the AppData/Roaming/AAGFileManager folder.
     * If the folder does not exist, this method creates it.
     * If the text file does not exist, this method calls setClaimDirectory(),
     * which then makes the text file and prompts the user to select the
     * root working directory.
     */
    private void parseClaimDirectory() {
        // Use System to get user AppData folder.
        // Made compatible with MacOS for testing on my MacBook.
        if(isWindows) {
            aagDataFolder = new File(System.getenv("APPDATA") + "\\AAGFileManager");
        } else {
            aagDataFolder = new File(System.getProperty("user.home") + "/Library/Application Support/AAGFileManager");
        }

        // Does /AppData/Roaming/AAGFileManager/rootdir.txt exist? If yes, read it.
        Path rootDirTextFile = Paths.get(aagDataFolder.getAbsolutePath() + slash + "rootdir.txt");
        if(rootDirTextFile.toFile().exists() && rootDirTextFile.toFile().isFile() && !rootDirTextFile.toFile().isDirectory()) {
            try {
                List<String> lines = Files.readAllLines(rootDirTextFile);
                this.claimDirectory = new File(lines.get(0)); // set this.claimDirectory
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else { // Doesn't exist? Set it.
            initializeClaimDirectory();
        }
    }

    /**
     * This method is only called on the first time this program is executed.
     * This will prompt the user to select the folder of the root working
     * directory that AAG claims are stored. It will then make a text file in
     * the AppData/Local/Roaming
     */
    void initializeClaimDirectory() {
        // Prompt user to select the root directory, bring up a file explorer to select the folder.
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select working folder for AAG claims");
        if(isWindows) {
            directoryChooser.setInitialDirectory(new File("C:\\"));
        } else {
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }

        File directory = directoryChooser.showDialog(new Stage());

        if(directory == null) {
            Alert claimDirectoryMissingAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "You have not specified the folder for your claim files.\nPress OK to choose the folder.\n"
                            + "Press Cancel to do it later.",
                    ButtonType.OK,
                    ButtonType.CANCEL);
            claimDirectoryMissingAlert.showAndWait();
            if (claimDirectoryMissingAlert.getResult() == ButtonType.OK) {
                initializeClaimDirectory();
            }
            return;
        }

        // Create /AppData/AAGFileManager/rootdir.txt
        Path rootDirTextFile = Paths.get(aagDataFolder.getAbsolutePath() + slash + "rootdir.txt");

        // Write the folder path the user selected in the txt file as plain text.
        Path writtenFile = null;
        try {
           writtenFile = Files.write(rootDirTextFile, directory.getAbsolutePath().getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(writtenFile != null) {
            parseClaimDirectory(); // Call this and let it do its thing
        } else {
            if(isWindows) {
                System.out.println("File write failed... Defaulting claim directory to C:\\");
                this.claimDirectory = new File("C:\\");
            } else {
                System.out.println("File write failed... Defaulting claim directory to ~/user/home");
                this.claimDirectory = new File(System.getProperty("user.home"));
            }
        }
    }


    /**
     * Creates the special directory for the claim files to be added to.
     * True if the directory was created.
     * False if the directory already exists.
     * @param firstName
     * @param lastName
     * @param date
     * @param aagNumber
     * @return
     * @throws IOException
     */
    boolean createFileDirectory(String firstName, String lastName, String date, String aagNumber) throws IOException {
        if (claimDirectory != null && claimDirectory.exists() && claimDirectory.isDirectory()) {

            File claimDirectory = new File(this.claimDirectory.getPath() + "\\" +
                    lastName + " " + firstName + " " + date + " AAG " + aagNumber);

            claimDirectoryPath = claimDirectory.getPath();

            if(!claimDirectory.exists()) {
                claimDirectory.mkdir();
                return true;
            }
        } else {
            Alert claimDirectoryMissingAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "You have not specified the folder for your claim files. Press OK to choose the folder.",
                    ButtonType.OK,
                    ButtonType.CANCEL);
            claimDirectoryMissingAlert.showAndWait();
            if(claimDirectoryMissingAlert.getResult() == ButtonType.OK) {
                initializeClaimDirectory();
                createFileDirectory(firstName, lastName, date, aagNumber);
            }
        }
        return false;
    }

    /**
     * Puts image files into pictureQueue list and other files into fileQueue list.
     * Returns a string to be used with the status text.
     * @param files
     * @return
     */
    public String sortFiles(List<File> files) {
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
                if(!added) {
                    fileQueue.add(file);
                }
            }
        }

        // Create a proper status text.
        String statusText;
        if(pictureQueue.size() > 0 && fileQueue.size() > 0) {
            statusText = "Loaded "
                    + pictureQueue.size()
                    + " picture(s) and "
                    + fileQueue.size()
                    + " other file(s) successfully.";
        } else if (pictureQueue.size() > 0) {
            statusText = "Loaded "
                    + pictureQueue.size()
                    + " picture(s) successfully.";
        } else if(fileQueue.size() > 0) {
            statusText = "Loaded "
                    + fileQueue.size()
                    + " file(s) successfully.";
        } else {
            statusText = "No files loaded. Something went wrong.";
        }
        return statusText;
    }

    /**
     * Takes all files from the pictureQueue and fileQueue lists and loads them into the -
     * claimDirectoryPath and claimDirectoryPath + /Pictures/ respectively.
     * @return
     * @throws IOException
     */
    public String moveFiles() throws IOException {

        String statusText = null;

        if(!fileQueue.isEmpty()) {


            for (int i = 0; i < fileQueue.size(); ++i) {

                File file = fileQueue.get(i);
                Path filePath = Paths.get(file.getAbsolutePath());
                Path claimPath = Paths.get(claimDirectoryPath + "\\" + file.getName());

                Path temp = Files.copy(filePath, claimPath);

                if(temp != null) {
                    statusText = "Files migrated successfully.";
                }
            }
        }

        if(!pictureQueue.isEmpty()) {

            File pictureDir = new File(claimDirectoryPath + "\\Pictures");
            if(!pictureDir.exists()) {
                pictureDir.mkdir();
            }

            for (int i = 0; i < pictureQueue.size(); ++i) {

                File file = pictureQueue.get(i);
                Path filePath = Paths.get(file.getAbsolutePath());
                Path picturePath = Paths.get(pictureDir.getAbsolutePath() + "\\" + file.getName());

                Path temp = Files.copy(filePath, picturePath);

                if(temp != null) {
                    statusText = "Files migrated successfully.";
                }
            }
        }

        if(fileQueue.isEmpty() && pictureQueue.isEmpty()) {
            statusText = "No files moved";
        }

        clearLists();
        return statusText;
    }

    /**
     * Clears the pictureQueue and fileQueue lists.
     */
    public void clearLists() {
        pictureQueue.clear();
        fileQueue.clear();
    }

    /**
     * Sets the rootDir, must be a File object.
     * @param rootDir
     */
    public void setClaimDirectory(File rootDir) {
        this.claimDirectory = rootDir;
    }

    /**
     * @return whether or not the user is on Windows.
     */
    public boolean getIsWindows() {
        return isWindows;
    }

    /**
     * Returns the root directory as a File object.
     * @return
     */
    public File getClaimDirectory() {
        return claimDirectory;
    }

    /**
     * Returns the pictureQueue list that is loaded via the sortFiles method.
     * @return
     */
    public List getPictureQueue() {
        return pictureQueue;
    }

    /**
     * Returns the pictureQueue list that is loaded via the sortFiles method.
     * @return
     */
    public List getFileQueue() {
        return fileQueue;
    }
}