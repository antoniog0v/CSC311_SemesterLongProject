package dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Person;
import service.MyLogger;
import service.UserSession;
import viewmodel.DB_GUI_Controller;

import java.sql.*;
public class DbConnectivityClass {
    final static String DB_NAME = "CSC311_BD_TEMP";
    MyLogger lg = new MyLogger();
    final static String SQL_SERVER_URL = "jdbc:mysql://villanicsc311server.mysql.database.azure.com";//update this server name
    final static String DB_URL = SQL_SERVER_URL + "/" + DB_NAME;//update this database name
    final static String USERNAME = "villaniadmin";// update this username
    final static String PASSWORD = "farmingdale25!";// update this password


    private final ObservableList<Person> data = FXCollections.observableArrayList();

    // Method to retrieve all data from the database and store it into an observable list to use in the GUI tableview.
    public ObservableList<Person> getData() {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users ";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                lg.makeLog("No data");
            }
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String department = resultSet.getString("department");
                String majorString = resultSet.getString("major");
                DB_GUI_Controller.Major major = null;
                //Tests if user has chosen a value, otherwise Undecided
                try {
                    major = DB_GUI_Controller.Major.valueOf(majorString.toUpperCase());
                }catch(Exception e){
                    major = DB_GUI_Controller.Major.Undecided;
                }
                String email = resultSet.getString("email");
                String imageURL = resultSet.getString("imageURL");
                data.add(new Person(id, first_name, last_name, department, major, email, imageURL));
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    //This is for registering a new account. It would've made a bit more sense to make it named
    //registerAccount(), but it works just the same
    public void registerUser(UserSession s) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            Statement statement = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS accounts (" + "username VARCHAR(200) NOT NULL PRIMARY KEY,"
                    + "password VARCHAR(200) NOT NULL,"
                    + "privileges VARCHAR(200))";
            statement.executeUpdate(sql);


            String sql2 = "INSERT INTO accounts (username, password, privileges) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql2);
            preparedStatement.setString(1, s.getUserName());
            preparedStatement.setString(2, s.getPassword());
            preparedStatement.setString(3, s.getPrivileges());

            int row = preparedStatement.executeUpdate();
            statement.close();
            conn.close();
            if (row > 0) {
                System.out.println("Account created successfully");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Connects to the database to insert/retrieve/delete/etc data
    public boolean connectToDatabase() {
        boolean hasRegistredUsers = false;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            //First, connect to MYSQL server and create the database if not created
            Connection conn = DriverManager.getConnection(SQL_SERVER_URL, USERNAME, PASSWORD);
            Statement statement = conn.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + "");
            statement.close();
            conn.close();

            //Second, connect to the database and create the table "users" if cot created
            conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            statement = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS users (" + "id INT( 10 ) NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                    + "first_name VARCHAR(200) NOT NULL," + "last_name VARCHAR(200) NOT NULL,"
                    + "department VARCHAR(200),"
                    + "major VARCHAR(200),"
                    + "email VARCHAR(200) NOT NULL UNIQUE,"
                    + "imageURL VARCHAR(500))";
            statement.executeUpdate(sql);

            //check if we have users in the table users
            statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");

            if (resultSet.next()) {
                int numUsers = resultSet.getInt(1);
                if (numUsers > 0) {
                    hasRegistredUsers = true;
                }
            }

            statement.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hasRegistredUsers;
    }

    //The ability to look up a user by their last name
    public void queryUserByLastName(String name) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM users WHERE last_name = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, name);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String major = resultSet.getString("major");
                String department = resultSet.getString("department");

                lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + " "
                        + ", Major: " + major + ", Department: " + department);
            }
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //This gets an account from the inputed username.
    public UserSession getAccount(String username) {
        connectToDatabase();
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            String sql = "SELECT * FROM accounts WHERE username = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                UserSession s = new UserSession(resultSet.getString("username"), resultSet.getString("password"), resultSet.getString("privileges"));
                return s;
            } else {
                UserSession s = new UserSession("", "", "NONE");
                return s;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
// Lists all users in the database
        public void listAllUsers() {
            connectToDatabase();
            try {
                Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                String sql = "SELECT * FROM users ";
                PreparedStatement preparedStatement = conn.prepareStatement(sql);

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String first_name = resultSet.getString("first_name");
                    String last_name = resultSet.getString("last_name");
                    String department = resultSet.getString("department");
                    String major = resultSet.getString("major");
                    String email = resultSet.getString("email");

                    lg.makeLog("ID: " + id + ", Name: " + first_name + " " + last_name + " "
                            + ", Department: " + department + ", Major: " + major + ", Email: " + email);
                }

                preparedStatement.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

       //Inserts a new user (Person) into the database. Again, with major, it sets it as the ComboBox value,
        // And if there isnt a usable one, it just sets to Undecided
        public void insertUser(Person person) {
            connectToDatabase();
            try {
                Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                String sql = "INSERT INTO users (first_name, last_name, department, major, email, imageURL) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, person.getFirstName());
                preparedStatement.setString(2, person.getLastName());
                preparedStatement.setString(3, person.getDepartment());
                if(person.getMajor()!=null) {
                    preparedStatement.setString(4, person.getMajor().toString());
                }else{
                    preparedStatement.setString(4, "Undecided");
                }
                preparedStatement.setString(5, person.getEmail());
                preparedStatement.setString(6, person.getImageURL());
                int row = preparedStatement.executeUpdate();
                if (row > 0) {
                    lg.makeLog("A new user was inserted successfully.");
                }
                preparedStatement.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
// Edits a user. Again, similarly to the above method, sets the major to Undecided if there isn't one chosen.
        public void editUser(int id, Person p) {
            connectToDatabase();
            try {
                Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                String sql = "UPDATE users SET first_name=?, last_name=?, department=?, major=?, email=?, imageURL=? WHERE id=?";
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, p.getFirstName());
                preparedStatement.setString(2, p.getLastName());
                preparedStatement.setString(3, p.getDepartment());
                String major = (p.getMajor()!=null?p.getMajor().toString():DB_GUI_Controller.Major.Undecided.toString());
                preparedStatement.setString(4, major);
                preparedStatement.setString(5, p.getEmail());
                preparedStatement.setString(6, p.getImageURL());
                preparedStatement.setInt(7, id);
                preparedStatement.executeUpdate();
                preparedStatement.close();
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        //Deletes a user where the ID is a certain number. User is chosen from selection in the TableView
        public void deleteRecord(Person person) {
            int id = person.getId();
            connectToDatabase();
            try {
                Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                String sql = "DELETE FROM users WHERE id=?";
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();
                preparedStatement.close();
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        //Method to retrieve id from database where it is auto-incremented.
        public int retrieveId(Person p) {
            connectToDatabase();
            int id;
            try {
                Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                String sql = "SELECT id FROM users WHERE email=?";
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, p.getEmail());

                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                id = resultSet.getInt("id");
                preparedStatement.close();
                conn.close();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            lg.makeLog(String.valueOf(id));
            return id;
        }
    }