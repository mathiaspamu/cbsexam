package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import model.Product;
import utils.Log;

public class ProductController {

  private static DatabaseController dbCon;

  public ProductController() {
    dbCon = new DatabaseController();
  }

  public static Product getProduct(int id) {

    String success = "success";
    String failure = "failure";
    String sql = "SELECT * FROM product where id=" + id;

    // check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Run the executeQuery in the DB and make an empty object to return
    ResultSet rs = dbCon.executeQuery(sql, success, failure);
    Product product = null;

    try {

      // Get first row and create the object and return it
      if (rs.next()) {
        product =
            new Product(
                rs.getInt("id"),
                rs.getString("product_name"),
                rs.getString("sku"),
                rs.getFloat("price"),
                rs.getString("description"),
                rs.getInt("stock"),
                rs.getLong("created_at"));

        // Return the product
        return product;
      } else {
        System.out.println("No product found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return empty object
    return null;
  }

  public static Product getProductBySku(String sku) {

    String success = "success";
    String failure = "failure";
    String sql = "SELECT * FROM product where sku='" + sku + "'";

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    ResultSet rs = dbCon.executeQuery(sql, success, failure);
    Product product = null;

    try {

      if (rs.next()) {
        product =
            new Product(
                rs.getInt("id"),
                rs.getString("product_name"),
                rs.getString("sku"),
                rs.getFloat("price"),
                rs.getString("description"),
                rs.getInt("stock"),
                rs.getLong("created_at"));

        return product;
      } else {
        System.out.println("No product found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    return null;
  }

  /**
   * Get all products in database
   *
   * @return
   */
  public static ArrayList<Product> getProducts() {

    String success = "success";
    String failure = "failure";
    String sql = "SELECT * FROM product";

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // TODO: Use caching layer.

    ResultSet rs = dbCon.executeQuery(sql, success, failure);
    ArrayList<Product> products = new ArrayList<Product>();

    try {
      while (rs.next()) {
        Product product =
            new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("sku"),
                rs.getFloat("price"),
                rs.getString("description"),
                rs.getInt("stock"),
                rs.getLong("created_at"));

        products.add(product);
      }

      if (products.isEmpty())
        return null;
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    return products;
  }

  public static Product createProduct(Product product) {

    String success = "Product created successfully";
    String failure = "Failed to create address";
    String sql = "INSERT INTO product(product_name, sku, price, description, stock, created_at) VALUES('"
            + product.getName()
            + "', '"
            + product.getSku()
            + "', '"
            + product.getPrice()
            + "', '"
            + product.getDescription()
            + "', "
            + product.getStock()
            + "', "
            + product.getCreatedTime()
            + ")";

    // Write in log that we've reach this step
    Log.writeLog(ProductController.class.getName(), product, "Actually creating a product in DB", 0);

    // Set creation time for product.
    product.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the product in the DB
    boolean state = dbCon.executeUpdate(sql, success, failure);
    if (state)
      return product;
    else
      return null;
  }
}