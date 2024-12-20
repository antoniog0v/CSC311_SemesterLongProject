package viewmodel;

import dao.DbConnectivityClass;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.UserSession;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SignUpController {
    @FXML
    private TextField usernameField, passwordField, reenterField;
    private static DbConnectivityClass cd;


    private Pattern usernameRegex = Pattern.compile("^([a-zA-Z]{2,25})");
    private Pattern passwordRegex = Pattern.compile("^([a-zA-Z]{2,25})");
    private BooleanProperty[] isValid;

    @FXML
    Button newAccountBtn;

// This initializes all the regex for the username and password. Must be between 2-25 characters, and if the password
    //reenter field doesn't match the normal password field, it won't let you create an account.
    public void initialize() {
        isValid = new BooleanProperty[2];
        for (int i = 0; i < isValid.length; i++) {
            isValid[i] = new SimpleBooleanProperty(false);
        }
        validateText(usernameField, usernameRegex, "Invalid. Must be between 2-25 characters.", isValid, 0);
        validateText(passwordField, passwordRegex, "Invalid. Must be between 2-25 characters.", isValid, 1);

        BooleanBinding match = Bindings.createBooleanBinding(
                ()-> passwordField.getText().equals(reenterField.getText()),
                passwordField.textProperty(),
                reenterField.textProperty());

        newAccountBtn.disableProperty().bind(
                isValid[0].not()
                        .or(isValid[1].not())
                        .or(match.not()));

    }

    //Passes throguh the String to see if it works with the regex.
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

// Creates a new account in the database. If it exists already, it will say the username is taken.
    // Doesn't check password because there's no point in that
    public void createNewAccount(ActionEvent actionEvent) {
        cd = new DbConnectivityClass();
        String priv = "NONE";
        String username = usernameField.getText();
        String password = passwordField.getText();
        UserSession s = new UserSession(usernameField.getText(), passwordField.getText(),priv);

        try{
            UserSession existingUser = cd.getAccount(s.getUserName());
        if(existingUser==null) {
            cd.registerUser(s);
            System.out.println("Account created!");
        }else{
            System.out.println("Username is taken! Please choose another");
        }
        } catch (Exception e) {
            System.out.println("Unable to create account");
            e.printStackTrace();
        }

        }


// Goes back to login screen
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
}
