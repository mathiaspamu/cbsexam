package controllers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import model.Address;
import model.LineItem;
import model.Order;
import model.User;
import utils.Log;

public class OrderController {

  private static DatabaseController dbCon;

  public OrderController() {
    dbCon = new DatabaseController();
  }

  public static Order getOrder(int id) {

    String success = "success";
    String failure = "failure";
    String sql = "SELECT * FROM orders where id = " + id;

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Do the executeQuery in the database and create an empty object for the results
    ResultSet rs = dbCon.executeQuery(sql, success, failure);
    Order order = null;

    try {
      if (rs.next()) {

        // Perhaps we could optimize things a bit here and get rid of nested queries.
        User user = UserController.getUser(rs.getInt("user_id"));
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
        Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));

        // Create an object instance of order from the database data
        order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Returns the build order
        return order;
      } else {
        System.out.println("No order found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Returns null
    return null;
  }

  /**
   * Get all orders in database
   *
   * @return
   */
  public static ArrayList<Order> getOrders() {

    String success = "success";
    String failure = "failure";
    String sql = "SELECT * FROM order";

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    ResultSet rs = dbCon.executeQuery(sql, success, failure);
    ArrayList<Order> orders = new ArrayList<Order>();

    try {
      while(rs.next()) {

        // Perhaps we could optimize things a bit here and get rid of nested queries.
        User user = UserController.getUser(rs.getInt("user_id"));
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
        Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));

        // Create an order from the database data
        Order order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Add order to our list
        orders.add(order);

      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // return the orders
    return orders;
  }

  public static Order createOrder(Order order) {

    String success = "Order created successfully";
    String failure = "Order not created";
    String sql = "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, created_at, updated_at) VALUES("
                    + order.getCustomer().getId()
                    + ", "
                    + order.getBillingAddress().getId()
                    + ", "
                    + order.getShippingAddress().getId()
                    + ", "
                    + order.calculateOrderTotal()
                    + ", "
                    + order.getCreatedAt()
                    + ", "
                    + order.getUpdatedAt()
                    + ")";


    // Write in log that we've reach this step
    Log.writeLog(OrderController.class.getName(), order, "Actually creating an order in DB", 0);

    // Set creation and updated time for order.
    order.setCreatedAt(System.currentTimeMillis() / 1000L);
    order.setUpdatedAt(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Save addresses to database and save them back to initial order instance
    order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
    order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

    // Save the user to the database and save them back to initial order instance
    order.setCustomer(UserController.createUser(order.getCustomer()));

    // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts. (FIXgbb)

    Connection connection = null;

    try {
      connection.setAutoCommit(false);

      dbCon.executeUpdate(sql, success, failure);

      //if (orderID != 0) {
        //Update the productid of the product before returning
        //order.setId(orderID);
      //}

      // Create an empty list in order to go trough items and then save them back with ID
      ArrayList<LineItem> items = new ArrayList<LineItem>();

      // Save line items to database
      for (LineItem item : order.getLineItems()) {
        item = LineItemController.createLineItem(item, order.getId());
        items.add(item);
      }

      order.setLineItems(items);

      // Commit and save SQL updates
      connection.commit();
      System.out.println("Changes committed successfully");

    } catch (SQLException e) {

      try {
        // Current transaction rollback
        connection.rollback();
        System.out.println("Transaction rolled back. Try again");

      } catch (SQLException e1){
        System.out.println("Something went wrong with the rollback" + e1.getMessage());

      } finally {
        try {
          connection.setAutoCommit(true);

        } catch (SQLException e1){
          e1.printStackTrace();
        }
      }
    }

    // Return order
    return order;
  }
}