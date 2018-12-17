package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    String success = "success";
    String failure = "failure";
    String sql = "SELECT * FROM user where id=" + id;
    User user;

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Actually do the executeQuery
    ResultSet rs = dbCon.executeQuery(sql, success, failure);

    try {

      // Get first object, since we only have one
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("token"),
                        rs.getLong("created_at"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return null;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    String success = "success";
    String failure = "failure";
    String sql = "SELECT * FROM user";
    ArrayList<User> users = new ArrayList<User>();

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Do the executeQuery and initialize an empty list for use if we don't get results
    ResultSet rs = dbCon.executeQuery(sql, success, failure);

    try {

      // Loop through DB Data
      while (rs.next()) {
        User user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("token"),
                        rs.getLong("created_at"));

        // Add element to list
        users.add(user);
      }

      if (users.isEmpty())
        return null;

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    String success = "User created successfully";
    String failure = "User not created";
    String sql = "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
            + user.getFirstname()
            + "', '"
            + user.getLastname()
            + "', '"
            + user.getPassword()
            + "', '"
            + user.getEmail()
            + "', "
            + user.getCreatedTime()
            + ")";
    boolean status;

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it. (FIX, created time instead of salt)
    dbCon.executeUpdate(sql, success, failure);

    try {
      // byte[] salt = Hashing.generateSalt();

      user.setPassword(Hashing.sha(user.getPassword(), user.getCreatedTime()));

      status = dbCon.executeUpdate(sql, success, failure);

      if (status)
        return user;

    } catch (Exception e) {
      System.out.println("Exception occurred. Try again.");
    }

    // Return user
    return null;
  }

  public static boolean deleteUser(int userId) {

    String success = "User was successfully deleted";
    String failure = "The system failed to delete user";
    String sql = "DELETE FROM user WHERE id=" + userId;
    boolean status;

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    status = dbCon.executeUpdate(sql, success, failure);

    return status;
  }

  public static User updateUser(User user) {

    String success = "User successfully updated";
    String failure = "Failed to update user";
    String sql = "UPDATE user SET first_name='"
            + user.getFirstname()
            + "',"
            + "last_name='"
            + user.getLastname()
            + "',"
            + "password='"
            + user.getPassword()
            + "',"
            + "email='"
            + user.getEmail()
            + "' WHERE id="
            + user.getId();
    boolean status;

    // Check for connection
    if (dbCon == null)
      dbCon = new DatabaseController();

      status = dbCon.executeUpdate(sql, success, failure);

      if (status)
        return user;
      else
        return null;
  }

  public static User authorizeUser(User user) {

    User userLogin = null;
    String success = "success";
    String failure = "failure";
    String sql = "SELECT user.id, user.first_name, user.last_name, user.password, user.email, user.token"
            + " FROM user " + "WHERE user.email = '" + user.getEmail() + "' AND " + "user.password = '" + user.getPassword() + "'";
    ResultSet resultSet;
    User loginUser;
    String token;
    boolean status;

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    try {
      // Actually do the query
      ResultSet rs = dbCon.executeQuery(sql, success, failure);

      // Get first and only object
      if (rs.next()) {

        userLogin = new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("token"),
                rs.getLong("created_at"));

        if (userLogin.getToken() == null) {
          try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            token = JWT.create()
                    .withIssuer("auth0")
                    .sign(algorithm);

            user.setToken(token);

            String tokenSuccess = "Token created.";
            String tokenFailure = "Token not created";
            String sqlToken = "UPDATE user SET token = '" + user.getToken() + "' WHERE email = '" + user.getEmail()
                    + "' AND password = '" + user.getPassword() + "'";

            status = dbCon.executeUpdate(sqlToken, tokenSuccess, tokenFailure);

            if (status)
              return userLogin;
          } catch (JWTCreationException exception){
            //Invalid Signing configuration / Couldn't convert Claims.
            System.out.println("Something went wrong" + exception.getMessage());
          }
        } else {
          System.out.println("JWT not created. Try again.");
        }

      } else {
        System.out.println("No user found");
      }

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return null;
  }
}