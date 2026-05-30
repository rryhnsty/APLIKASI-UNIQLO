package Program;

public class CartItem {
    private int    cartItemId;   
    private String cartId;      
    private Product product;     
    private int    quantity;    
    private String size;        

   
    public CartItem(int cartItemId, String cartId,
                    Product product, int quantity, String size) {
        this.cartItemId = cartItemId;
        this.cartId     = cartId;
        this.product    = product;
        this.quantity   = quantity;
        this.size       = size;
    }
    
    public int     getCartItemId() { return cartItemId; }
    public String  getCartId()     { return cartId;     }
    public Product getProduct()    { return product;    }
    public int     getQuantity()   { return quantity;   }
    public String  getSize()       { return size;       }

  
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setSize(String size)      { this.size = size;         }

    public long getPriceNumeric() {
        try {
            String raw = product.getPrice()
                    .replace("Rp", "")
                    .replace(".", "")
                    .replace(",", "")
                    .trim();
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

   
    public long getSubtotal() {
        return getPriceNumeric() * quantity;
    }

    public static String formatRupiah(long amount) {
        String s   = String.valueOf(amount);
        StringBuilder sb = new StringBuilder();
        int rem = s.length() % 3;
        if (rem > 0) sb.append(s, 0, rem);
        for (int i = rem; i < s.length(); i += 3) {
            if (sb.length() > 0) sb.append('.');
            sb.append(s, i, i + 3);
        }
        return "Rp " + sb;
    }
}
