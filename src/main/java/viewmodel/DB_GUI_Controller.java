package viewmodel;

import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import service.MyLogger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DB_GUI_Controller implements Initializable {

    @FXML
    Button editButton, deleteButton, uploadFileButton, addBtn;

    String filename;
    @FXML
    MenuItem importCSVbutton, exportCSVbutton,editItem, clearItem, deleteItem;
    @FXML
    private ComboBox<Major> majorDropDown;
    @FXML
    StorageUploader store = new StorageUploader();
    @FXML
    TextField first_name, last_name, department, email, imageURL;
    @FXML
    ImageView img_view;
    private BooleanProperty[] isValid;
    @FXML
    File file;
    @FXML
    ProgressBar progressBar;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();
    private Pattern firstNameRegex = Pattern.compile("^([a-zA-Z]{2,25})");
    private Pattern lastNameRegex = Pattern.compile("^([a-zA-Z]{2,25})");
    private Pattern emailRegex = Pattern.compile("([a-zA-z0-9]+)@([a-zA-z0-9]+).([a-zA-z0-9]+)");
    private Pattern departmentRegex = Pattern.compile("^([a-zA-Z]{2,25})");
    private Pattern majorRegex = Pattern.compile("^([a-zA-Z]{2,25})");

    @Override
    // Initalize adds all the items to the ComboBox, the keywords you can enter for specific commands (Such as
    // Deleting and editing items), and validates that all the Text entered into the TextFields follow the RegEx.
    public void initialize(URL url, ResourceBundle resourceBundle) {
        majorDropDown.getItems().addAll(Major.values());
        majorDropDown.setValue(Major.Undecided);
        deleteItem.setOnAction(e->deleteRecord());
        clearItem.setOnAction(e->clearForm());
        editItem.setOnAction(e->editRecord());
        tv.setOnKeyPressed(e->{
                if(e.isControlDown()&&e.getCode()== KeyCode.D) {
                    deleteRecord();
                }});
        tv.setOnKeyPressed(e->{
            if (e.isControlDown() && e.getCode() == KeyCode.E) {
                editRecord();
            }
        });
        tv.setOnKeyPressed(e->{
            if (e.isControlDown() && e.getCode() == KeyCode.R) {
                clearForm();
            }
        });
        isValid = new BooleanProperty[4];
        for (int i = 0; i < isValid.length; i++) {
            isValid[i] = new SimpleBooleanProperty(false);
        }
        validateText(first_name, firstNameRegex, "Invalid. Must be between 2-25 characters.", isValid, 0);
        validateText(last_name, lastNameRegex, "Invalid. Must be between 2-25 characters.", isValid, 1);
        validateText(email, emailRegex, "Invalid. Must be a valid email address.", isValid, 2);
        validateText(department, departmentRegex, "Invalid. Must be between 2-25 characters.", isValid, 3);

        addBtn.disableProperty().bind(
                isValid[0].not()
                        .or(isValid[1].not())
                        .or(isValid[2].not())
                        .or(isValid[3].not())

        );

        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        editButton.disableProperty().bind(Bindings.isEmpty(tv.getSelectionModel().getSelectedItems()));
        deleteButton.disableProperty().bind(Bindings.isEmpty(tv.getSelectionModel().getSelectedItems()));

    }


    //RegEx validation
    private void validateText(TextField text, Pattern regex, String invalid, BooleanProperty[] b, int index) {
        text.focusedProperty().addListener((observable, notFocused, nowFocused) -> {
            if (!nowFocused) {
                Matcher matcher = regex.matcher(text.getText());
                if (matcher.matches()) {
                    b[index].set(true);
                } else {
                    b[index].set(false);
                }
            }
        });


    }

    //Exports the CSV file. Gets the data from getData() and writes to the file that is going to be named/saved
    //Onto a specific location.
    @FXML
    protected void onExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File f = fileChooser.showSaveDialog(menuBar.getScene().getWindow());

        if (f != null) {
            try {
                String[] data = getData();
                writeFile(f.getAbsolutePath(), data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
    @FXML

    //Imports CSV files into the TableView
    protected void onImportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        File f=fileChooser.showOpenDialog(menuBar.getScene().getWindow());

        if (f != null) {
            try {
                readFile(f.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public  String[] readFile(String fileName) throws IOException {
        FileReader fileReader = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line +"\n");
        }
        bufferedReader.close();
        return lines.toArray(new String[lines.size()]);
    }


    //This method write into a file using String absolutePath, String[] data
    // it is called writeFile
    public void writeFile(String file, String[] data) throws IOException {
        try {
            PrintWriter writer = new PrintWriter(file);
            for(String row:data){
                writer.println(row);
            }
        } catch (IOException ex) {
            System.out.println("Error writing to file '" + file + "'");
        }

    }
    //Returns the data that will be written into the file
    public String[] getData() {
        List<String> tvData = new ArrayList<>();

        for (Person person : data) { // For each person in the TableView
            StringBuilder stuff = new StringBuilder();
            stuff.append(person.getFirstName()).append(",")
                    .append(person.getLastName()).append(",")
                    .append(person.getDepartment()).append(",")
                    .append(person.getMajor().toString()).append(",")
                    .append(person.getEmail()).append(",")
                    .append(person.getImageURL()).append(",");
                    tvData.add(stuff.toString());
        }
        System.out.println(tvData);
        return tvData.toArray(new String[0]);
    }

    // Adds a new record to the TableView and Azure Database
    @FXML
    protected void addNewRecord() {

        Major selectedMajor = majorDropDown.getValue();
        if (selectedMajor != null) {
            Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                    selectedMajor, email.getText(), imageURL.getText());
            System.out.println(p.getMajor());
            cnUtil.insertUser(p);
            cnUtil.retrieveId(p);
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            clearForm();
        } else {
            throw new IllegalArgumentException("Please select a major");
        }

    }

    // Clears the form on the right so you can start from scratch
    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        email.setText("");
        imageURL.setText("");
    }

    // Logs you out of the database so you can either create a new account or log into an existing one
    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Closes the program
    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    //Displays the About, which explains the program and certain functionalities
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

    //Edits the selected record
    @FXML
    protected void editRecord() {
        Major selectedMajor = majorDropDown.getValue();
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                selectedMajor, email.getText(), imageURL.getText());
        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);
    }

    //Deletes the selected record
    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
    }

    //Shows the image that is currently uploaded to a specific User. Sets the image by getting the URL from the
    //StorageServer (Utilizes a SAS key so that you can actually view it).
    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());

        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
            Person selectedPerson = tv.getSelectionModel().getSelectedItem();
            if (selectedPerson != null) {
                Task<Void> uploadTask = createUploadTask(file, progressBar, selectedPerson);
                progressBar.progressProperty().bind(uploadTask.progressProperty());
                new Thread(uploadTask).start();
            } else {
                System.out.println("Please select a person to upload the image");
            }

        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

//When an item from the TableView is selected, this is the method that identifies it. The reasoning for the "defaultImage" is because
    //if you create a new user, the program freaks out and doesn't recognize it as an "Image". So I had to include that.
    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        String defaultImage = "/images/profile.png";
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            majorDropDown.setValue(p.getMajor());
            email.setText(p.getEmail());
            imageURL.setText(p.getImageURL());
            if (p.getImageURL() != null && !p.getImageURL().isEmpty()) {
                try {
                    img_view.setImage(new Image(p.getImageURL()));
                } catch (IllegalArgumentException e) {
                    img_view.setImage(new Image(getClass().getResource(defaultImage).toExternalForm()));
                }

            } else {
                img_view.setImage(new Image(getClass().getResource(defaultImage).toExternalForm()));
            }
        } else {
            System.out.println("Please choose a valid option!");
        }
    }

// Light theme for the GUI
    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   // Dark theme for the GUI
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

//For if you want to upload a new user VIA the menu
    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2, textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }

    //For creating an upload task. This is a thread. Utilizes a SAS key so that we can view the images without the
    //Database blocking access.
    private Task<Void> createUploadTask(File file, ProgressBar progressBar, Person selectedPerson) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                String SAS = "sp=racwdli&st=2024-11-20T06:49:28Z&se=2025-11-20T14:49:28Z&sv=2022-11-02&sr=c&sig=ezp0WOXIK4xN93tSrTGoOHS%2FuI%2FBL58MvpPohDjKqYg%3D";
                String blobName = file.getName();
                long fileSize = Files.size(file.toPath());
                long uploadedBytes = 0;
                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());

                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    blobClient.upload(fileInputStream, fileSize, true);
                    byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer size
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//                        blobOutputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;

                        // Calculate and update progress as a percentage
                        int progress = (int) ((double) uploadedBytes / fileSize * 100);
                        updateProgress(progress, 100);
                        wait(100);
                        updateProgress(progress, 0);

                    }

                    updateProgress(fileSize, fileSize);
                } catch (IOException e) {
                    e.printStackTrace();
                    updateMessage("Upload failed.");
                    return null;
                }
                String fileURL = (blobClient.getContainerClient().getBlobClient(blobName).getBlobUrl() + "?" + SAS);
                if (selectedPerson != null) {
                    selectedPerson.setImageURL(fileURL);
                    imageURL.setText(fileURL);
                    img_view.setImage(new Image(fileURL));
                    System.out.println(selectedPerson);
                    cnUtil.editUser(selectedPerson.getId(), selectedPerson);
                }


                return null;
            }

        };
    }

// Enum class for the ComboBox values
    public static enum Major {
        Undecided, BUS, CS, CPIS, PSY, MTH, EGL, ECO, SOC, IT, SE


        }

//Compiles the results for showSomeone() method
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


}