package manager;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private ArrayList<String> imgFileTypes;
    private ArrayList<File> fileQueue;
    private ArrayList<File> pictureQueue;
    private File rootDirectory;
    private String claimDirectoryPath;

    public FileManager(String rootDirectoryString) { // Remove the parameter from this constructor after completing the comments below
        rootDirectory = new File(rootDirectoryString);
        imgFileTypes = new ArrayList<>();
        fileQueue = new ArrayList<>();
        pictureQueue = new ArrayList<>();

        if(!initializeRootDirectory()) { // This will get the root directory from a text file in the AppData folder.
            // This will execute if the method returns false because the text file was not found.
            setRootDirectory();
        }

        imgFileTypes.add(".png");
        imgFileTypes.add(".jpeg");
        imgFileTypes.add(".jpg");
        imgFileTypes.add(".gif");
        imgFileTypes.add(".tiff");
        imgFileTypes.add(".bmp");
    }


    public boolean initializeRootDirectory() {
        // Use System to get user AppData folder.
        // Does /AppData/AAGFileManager/ exist? If not, create it
        // Does /AppData/AAGFileManager/rootdir.txt exist?
            // If not, this is the first run.
            // Return false.
            return false;

            // If so, read the first line of text and set that to this.rootDirectory
        // return true;

    }

    public void setRootDirectory() {
        // Prompt user to select the root directory, bring up a file explorer to select the folder.
        // Create /AppData/AAGFileManager/rootdir.txt
        // Write the folder path the user selected in the txt file as plain text.
        initializeRootDirectory(); // Call this and let it do its thing
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
        if (rootDirectory.exists() && rootDirectory.isDirectory()) {

            File claimDirectory = new File(rootDirectory.getPath() + "\\" +
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
    public void setRootDirectory(File rootDir) {
        this.rootDirectory = rootDir;
    }

    /**
     * Returns the root directory as a File object.
     * @return
     */
    public File getRootDirectory() {
        return rootDirectory;
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