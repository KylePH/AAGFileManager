package manager;


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

public class FileManager {

    private ArrayList<String> imgFileTypes;
    private ArrayList<File> fileQueue;
    private ArrayList<File> pictureQueue;
    private File caseDirectory;
    private File aagDataFolder;
    private String claimDirectoryPath;
    private String os;
    private String slash;

    public FileManager() { // Remove the parameter from this constructor after completing the comments below
        imgFileTypes = new ArrayList<>();
        fileQueue = new ArrayList<>();
        pictureQueue = new ArrayList<>();
        String rootDirectoryString;

        os = System.getProperty("os.name").toLowerCase();

        switch(os) { // This is temporary until setCaseDirectory() is fully working.
            case "windows 10":
            case "windows 8.1":
            case "windows 8":
            case "windows 7":
            case "windows xp":
                rootDirectoryString = "C:\\";
                slash = "\\";
                break;
            default:
                rootDirectoryString = System.getProperty("user.home");
                slash = "/";
                break;
        }

        caseDirectory = new File(rootDirectoryString); // Temporary until setCaseDirectory() is fully working.

        parseCaseDirectory();

        imgFileTypes.add(".png");
        imgFileTypes.add(".jpeg");
        imgFileTypes.add(".jpg");
        imgFileTypes.add(".gif");
        imgFileTypes.add(".tiff");
        imgFileTypes.add(".bmp");
    }

    /**
     * Reads the path to the root working directory for AAG cases from
     * a text file located in the AppData/Roaming/AAGFileManager folder.
     * If the folder does not exist, this method creates it.
     * If the text file does not exist, this method calls setCaseDirectory(),
     * which then makes the text file and prompts the user to select the
     * root working directory.
     */
    public void parseCaseDirectory() {
        // Use System to get user AppData folder.
        // Added os specific folders so I can test on my MacBook.

        if(os.startsWith("windows")) {
            aagDataFolder = new File(System.getenv("APPDATA") + "\\AAGFileManager");
        } else {
            aagDataFolder = new File(System.getProperty("user.home") + "/Library/Application Support/AAGFileManager");
        }

        // Does /AppData/Roaming/AAGFileManager/ exist? If not, create it
        if(!aagDataFolder.exists()) {
            aagDataFolder.mkdir();
        }

        // Does /AppData/Roaming/AAGFileManager/rootdir.txt exist? If yes, read it.
        Path rootDirTextFile = Paths.get(aagDataFolder.getAbsolutePath() + slash + "rootdir.txt");
        if(rootDirTextFile.toFile().exists() && rootDirTextFile.toFile().isFile() && !rootDirTextFile.toFile().isDirectory()) {
            try {
                List<String> lines = Files.readAllLines(rootDirTextFile);
                this.caseDirectory = new File(lines.get(0)); // set this.caseDirectory
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else { // Doesn't exist? Set it.
            initializeCaseDirectory();
        }
    }

    /**
     * This method is only called on the first time this program is executed.
     * This will prompt the user to select the folder of the root working
     * directory that AAG cases are stored. It will then make a text file in
     * the AppData/Local/Roaming
     */
    public void initializeCaseDirectory() {
        // Prompt user to select the root directory, bring up a file explorer to select the folder.
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select working folder for AAG cases");
        directoryChooser.setInitialDirectory(new File("C:\\"));
        File directory = directoryChooser.showDialog(new Stage());

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
            parseCaseDirectory(); // Call this and let it do its thing
        } else {
            System.out.println("File write failed... Defaulting case directory to C://");
            this.caseDirectory = new File("C:\\");
        }
    }


    /**
     * Creates the special directory for the case files to be added to.
     * True if the directory was created.
     * False if the directory already exists.
     * @param firstName
     * @param lastName
     * @param date
     * @param aagNumber
     * @return
     * @throws IOException
     */
    public boolean createFileDirectory(String firstName, String lastName, String date, String aagNumber) throws IOException {
        if (caseDirectory.exists() && caseDirectory.isDirectory()) {

            File claimDirectory = new File(caseDirectory.getPath() + "\\" +
                    lastName + " " + firstName + " " + date + " AAG " + aagNumber);

            claimDirectoryPath = claimDirectory.getPath();

            if(!claimDirectory.exists()) {
                claimDirectory.mkdir();
                return true;
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
                if(added == false) {
                    fileQueue.add(file);
                }
            }
        }

        // Create a proper status text.
        String statusText;
        if(pictureQueue.size() > 0 && fileQueue.size() > 0) {
            statusText = "Loaded "
                    + pictureQueue.size()
                    + " pictures and "
                    + fileQueue.size()
                    + " other files successfully.";
        } else if (pictureQueue.size() > 0) {
            statusText = "Loaded "
                    + pictureQueue.size()
                    + " pictures successfully.";
        } else if(fileQueue.size() > 0) {
            statusText = "Loaded "
                    + fileQueue.size()
                    + " file successfully.";
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
    public void setCaseDirectory(File rootDir) {
        this.caseDirectory = rootDir;
    }

    /**
     * Returns the root directory as a File object.
     * @return
     */
    public File getCaseDirectory() {
        return caseDirectory;
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