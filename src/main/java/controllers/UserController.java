package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
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

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Actually do the executeQuery
    ResultSet rs = dbCon.executeQuery(sql, success, failure);
    User user = null;

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
                        rs.getString("token"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
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

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Do the executeQuery and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.executeQuery(sql, success, failure);
    ArrayList<User> users = new ArrayList<User>();

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
                        rs.getString("token"));

        // Add element to list
        users.add(user);
      }
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

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it.
    dbCon.executeUpdate(sql, success, failure);

    // Return user
    return user;
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
    boolean status;

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
    boolean status;
    String success = "success";
    String failure = "failure";
    String sql = "SELECT user.id, user.first_name, user.last_name, user.password, user.email, user.token"
            + " FROM user " + "WHERE user.email = '" + user.getEmail() + "' AND " + "user.password = '" + user.getPassword() + "'";

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
                rs.getString("token"));

        if (userLogin.getToken() == null) {
          try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            String token = JWT.create()
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

        /*
        // Verifying the token
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXUyJ9.eyJpc3MiOiJhdXRoMCJ9.AbIJTDMFc7yUa5MhvcP03nJPyCPzZtQcGEp-zWfOkEE";

        try {
          Algorithm algorithm = Algorithm.HMAC256("secret");
          JWTVerifier verifier = JWT.require(algorithm)
                  .withIssuer("auth0")
                  .build(); //Reusable verifier instance
          DecodedJWT jwt = verifier.verify(token);
        } catch (JWTVerificationException exception1){
          // Invalid signature/claims
          System.out.println("Something went wrong with verifying the token" + exception1.getMessage());
        }
        */

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