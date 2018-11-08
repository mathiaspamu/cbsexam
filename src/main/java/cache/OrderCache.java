package cache;

import controllers.OrderController;
import java.util.ArrayList;
import model.Order;
import utils.Config;

//TODO: Build this cache and use it. (underway)
public class OrderCache {

    // List of orders
    private ArrayList<Order> orders;

    // Time cache should live
    private long ttl;

    // Sets when the cache has been created
    private long created;

    public OrderCache() {
        this.ttl = Config.getOrderTtl();
    }

    public ArrayList<Order> getOrders(boolean forceUpdate) {

        if (forceUpdate
                || ((this.created + this.ttl) >= (System.currentTimeMillis() / 1000L))
                || this.orders.isEmpty()) {

    // Get orders from controller, since we wish to update
            ArrayList<Order> orders = OrderController.getOrders();

    // Set orders for the instance and set created timestamp
            this.orders = orders;
            this.created = System.currentTimeMillis() / 1000L;
        }

        // Return the orders
        return orders;
    }
}
