package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import model.LineItem;
import model.Product;
import utils.Log;

public class LineItemController {

  private static DatabaseController dbCon;

  public LineItemController() {
    dbCon = new DatabaseController();
  }

  public static ArrayList<LineItem> getLineItemsForOrder(int orderID) {

    String success = "success";
    String failure = "failure";
    String sql = "SELECT * FROM line_item where order_id=" + orderID;

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Do the executeQuery and initialize an empty list for the results
    ResultSet rs = dbCon.executeQuery(sql, success, failure);
    ArrayList<LineItem> items = new ArrayList<>();

    try {

      // Loop through the results from the DB
      while (rs.next()) {

        // Construct a product base on the row data with product_id
        Product product = ProductController.getProduct(rs.getInt("product_id"));

        // Initialize an instance of the line item object
        LineItem lineItem =
            new LineItem(
                rs.getInt("id"),
                product,
                rs.getInt("quantity"),
                rs.getFloat("price"));

        // Add it to our list of items and return it
        items.add(lineItem);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list, which might be empty
    return items;
  }

  public static LineItem createLineItem(LineItem lineItem, int orderID) {

    String success = "Line item created succesfully";
    String failure = "Line item not created";
    String sql = "INSERT INTO line_item(product_id, order_id, price, quantity) VALUES(\"\n" +
            "            + lineItem.getProduct().getId()\n" +
            "            + \", \"\n" +
            "            + orderID\n" +
            "            + \", \"\n" +
            "            + lineItem.getPrice()\n" +
            "            + \", \"\n" +
            "            + lineItem.getQuantity()\n" +
            "            + \")\");";

    // Write in log that we've reach this step
    Log.writeLog(ProductController.class.getName(), lineItem, "Actually creating a line item in DB", 0);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Get the ID of the product, since the user will not send it to us.
    lineItem.getProduct().setId(ProductController.getProductBySku(lineItem.getProduct().getSku()).getId());

    // Update the ID of the product


    // Insert the product in the DB
    dbCon.executeUpdate(sql, success, failure);


    //if (lineItemID != 0) {
      //Update the productid of the product before returning
      //lineItem.setId(lineItemID);
    //} else{

      // Return null if product has not been inserted into database
      //return null;
    //}

    // Return product
    return lineItem;
  }
  
}
