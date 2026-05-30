package Program;

import java.util.ArrayList;
import java.util.List;


public class CartManager {

   
    private static CartManager instance;

    public static CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    
    private final List<CartItem> items = new ArrayList<>();
    private String customerId = "C001";

    private CartManager() {}
    public void setCustomerId(String id) { this.customerId = id; }
    public String getCustomerId()        { return customerId;    }
    public void addItem(Product product, int qty, String size) {
        for (CartItem ci : items) {
            if (ci.getProduct().getCategory().equals(product.getCategory())
                    && ci.getSize().equals(size)) {
                ci.setQuantity(ci.getQuantity() + qty);
                return;
            }
        }
        items.add(new CartItem(items.size() + 1, "CART-" + customerId, product, qty, size));
    }

  
    public List<CartItem> getItems() { return items; }

    public int getTotalItems() {
        int n = 0;
        for (CartItem ci : items) n += ci.getQuantity();
        return n;
    }

    public long getSubtotal() {
        long total = 0;
        for (CartItem ci : items) total += ci.getSubtotal();
        return total;
    }

    public void removeItem(CartItem ci)           { items.remove(ci); }
    public void clear()                           { items.clear();    }
}
