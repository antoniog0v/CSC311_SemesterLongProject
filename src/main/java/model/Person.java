package model;

import viewmodel.DB_GUI_Controller;


//Person class. This is the blueprint for the data that is inserted into the database.
//Major is of type Major from the DB_GUI_Controller class, which allows the user to utilize the ComboBox for
//Major selection

public class Person {
    private Integer id;
    private String firstName;
    private String lastName;
    private String department;
    private DB_GUI_Controller.Major major;
    private String email;
    private String imageURL;

    public Person() {
    }


   //All the constructors. Keep in mind that major is a type from the combobox

    public Person(String firstName, String lastName, String department, DB_GUI_Controller.Major major, String email, String imageURL) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.major = major;
        this.email = email;
        this.imageURL = imageURL;
    }

    public Person(Integer id, String firstName, String lastName, String department, DB_GUI_Controller.Major major, String email, String imageURL) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.major = major;
        this.email = email;
        this.imageURL = imageURL;
    }


    //All the setters and getters. Keep in mind that major is a type from the ComboBox

    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public DB_GUI_Controller.Major getMajor() {
        return major;
    }

    public void setMajor(DB_GUI_Controller.Major major) {
        this.major = major;
    }


    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

   //toString override for displaying all person information
    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", department='" + department + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", major='" + major + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}