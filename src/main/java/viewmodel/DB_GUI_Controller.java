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
    StorageUploader store = new StorageUploader();
    @FXML
    TextField first_name, last_name, department, major, email, imageURL;
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
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isValid = new BooleanProperty[5];
        for (int i = 0; i < isValid.length; i++) {
            isValid[i] = new SimpleBooleanProperty(false);
        }
        validateText(first_name, firstNameRegex, "Invalid. Must be between 2-25 characters.", isValid, 0);
        validateText(last_name, lastNameRegex, "Invalid. Must be between 2-25 characters.", isValid, 1);
        validateText(email, emailRegex, "Invalid. Must be a valid email address.", isValid, 2);
        validateText(department, departmentRegex, "Invalid. Must be between 2-25 characters.", isValid, 3);
        validateText(major, majorRegex, "Invalid. Must be between 2-25 characters.", isValid, 4);

        addBtn.disableProperty().bind(
                isValid[0].not()
                        .or(isValid[1].not())
                        .or(isValid[2].not())
                        .or(isValid[3].not())
                        .or(isValid[4].not())

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
    private void validateText(TextField text, Pattern regex, String invalid, BooleanProperty[] b, int index){
        text.focusedProperty().addListener((observable, notFocused, nowFocused)->{
            if(!nowFocused){
                Matcher matcher = regex.matcher(text.getText());
                if (matcher.matches()) {
                    b[index].set(true);
                }else{
                    b[index].set(false);
                }
            }
        });



    }
//    @FXML
//    protected void onImportCSV() {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Open CSV File");
//        fileChooser.getExtensionFilters().addAll(
//                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
//        );
//        File f=fileChooser.showOpenDialog(welcomeText.getScene().getWindow());
//
//        if (f != null) {
//
//
//            try {
//                areaText.setText(Arrays.toString(readFile(f.getAbsolutePath())));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//    public  String[] readFile(String fileName) throws IOException {
//        FileReader fileReader = new FileReader(fileName);
//        BufferedReader bufferedReader = new BufferedReader(fileReader);
//        List<String> lines = new ArrayList<String>();
//        String line = null;
//        while ((line = bufferedReader.readLine()) != null) {
//            lines.add(line +"\n");
//        }
//        bufferedReader.close();
//        return lines.toArray(new String[lines.size()]);
//    }
//
//    //ths method return the data to be written in a file
//    public String[] getData(){
//        return areaText.getText().split(",");
//    }


    @FXML
    protected void addNewRecord() {

            Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                    major.getText(), email.getText(), imageURL.getText());
            cnUtil.insertUser(p);
            cnUtil.retrieveId(p);
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            clearForm();

    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        major.setText("");
        email.setText("");
        imageURL.setText("");
    }

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
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                major.getText(), email.getText(),  imageURL.getText());
        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());

        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
            Person selectedPerson = tv.getSelectionModel().getSelectedItem();
            if(selectedPerson!=null) {
                Task<Void> uploadTask = createUploadTask(file, progressBar, selectedPerson);
                progressBar.progressProperty().bind(uploadTask.progressProperty());
                new Thread(uploadTask).start();
            }else{
                System.out.println("Please select a person to upload the image");
            }

        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }


    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        String defaultImage = "/images/profile.png";
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            first_name.setText(p.getFirstName());
            last_name.setText(p.getLastName());
            department.setText(p.getDepartment());
            major.setText(p.getMajor());
            email.setText(p.getEmail());
            imageURL.setText(p.getImageURL());
            if (p.getImageURL()!=null&&!p.getImageURL().isEmpty()){
                try {
                    img_view.setImage(new Image(p.getImageURL()));
                }catch (IllegalArgumentException e){
                    img_view.setImage(new Image(getClass().getResource(defaultImage).toExternalForm()));
                }

                }else{
                img_view.setImage(new Image(getClass().getResource(defaultImage).toExternalForm()));
            }
        } else {
            System.out.println("Please choose a valid option!");
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
            System.out.println("light " + scene.getStylesheets());

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
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2,textField3, comboBox));
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
    private Task<Void> createUploadTask(File file, ProgressBar progressBar, Person selectedPerson) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                String SAS = "sp=racwdli&st=2024-11-20T06:49:28Z&se=2025-11-20T14:49:28Z&sv=2022-11-02&sr=c&sig=ezp0WOXIK4xN93tSrTGoOHS%2FuI%2FBL58MvpPohDjKqYg%3D";
                String blobName = file.getName();
                long fileSize = Files.size(file.toPath());
                long uploadedBytes = 0;
                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());

                try (FileInputStream fileInputStream = new FileInputStream(file)){
                    blobClient.upload(fileInputStream, fileSize, true);
                    byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer size
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//                        blobOutputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;

                        // Calculate and update progress as a percentage
                        int progress = (int) ((double) uploadedBytes / fileSize * 100);
                        updateProgress(progress, 100);

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
                    System.out.println(selectedPerson.getImageURL() + " This is the image URL from createUploadTask");
                    imageURL.setText(fileURL);
                    img_view.setImage(new Image(fileURL));

                }


                return null;
            }

        };
    }


    private static enum Major {Business, CSC, CPIS}

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