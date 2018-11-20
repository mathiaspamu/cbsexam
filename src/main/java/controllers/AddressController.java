package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import model.Address;
import utils.Log;

public class AddressController {

  private static DatabaseController dbCon;

  public AddressController() {
    dbCon = new DatabaseController();
  }

  public static Address getAddress(int id) {

    String success = "success";
    String failure = "failure";
    String sql = "SELECT * FROM address where id=" + id;

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Do the executeQuery and set the initial value to null
    ResultSet rs = dbCon.executeQuery(sql, success, failure);
    Address address = null;

    try {
      // Get the first row and build an address object
      if (rs.next()) {
        address =
            new Address(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("street_address"),
                rs.getString("city"),
                rs.getString("zipcode")
                );

        // Return our newly added object
        return address;
      } else {
        System.out.println("No address found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Returns null if we can't find anything.
    return address;
  }

  public static Address createAddress(Address address) {

    String success = "Address created successfully";
    String failure = "Failed to create address";
    String sql = "INSERT INTO address(name, city, zipcode, street_address) VALUES('\"\n" +
            "            + address.getName()\n" +
            "            + \"', '\"\n" +
            "            + address.getCity()\n" +
            "            + \"', '\"\n" +
            "            + address.getZipCode()\n" +
            "            + \"', '\"\n" +
            "            + address.getStreetAddress()\n" +
            "            + \"')\");";

    // Write in log that we've reach this step
    Log.writeLog(ProductController.class.getName(), address, "Actually creating a line item in DB", 0);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the product in the DB
    dbCon.executeUpdate(sql, success, failure);

    //if (addressID != 0) {
      //Update the productid of the product before returning
      //address.setId(addressID);
    //} else{
      // Return null if product has not been inserted into database
      //return null;
    //}

    // Return product, will be null at this point
    return address;
  }
  
}
