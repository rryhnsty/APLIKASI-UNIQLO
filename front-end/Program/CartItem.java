package Program;

/**
 * CartItem.java
 * Menyimpan data satu item di keranjang belanja.
 */
public class CartItem {
    private int    cartItemId;   // id_cart_item (dari DB)
    private String cartId;       // id_cart
    private Product product;     // produk terkait
    private int    quantity;     // jumlah
    private String size;         // ukuran (S / M / L / XL / XXL)

    // ── Constructor lengkap (dari DB) ──────────────────────────────────────
    public CartItem(int cartItemId, String cartId,
                    Product product, int quantity, String size) {
        this.cartItemId = cartItemId;
        this.cartId     = cartId;
        this.product    = product;
        this.quantity   = quantity;
        this.size       = size;
    }

    // ── Getters ────────────────────────────────────────────────────────────
    public int     getCartItemId() { return cartItemId; }
    public String  getCartId()     { return cartId;     }
    public Product getProduct()    { return product;    }
    public int     getQuantity()   { return quantity;   }
    public String  getSize()       { return size;       }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setSize(String size)      { this.size = size;         }

    // ── Harga numerik dari string "Rp 299.000" ─────────────────────────────
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

    // ── Total harga satu baris ─────────────────────────────────────────────
    public long getSubtotal() {
        return getPriceNumeric() * quantity;
    }

    // ── Format Rp ─────────────────────────────────────────────────────────
    public static String formatRupiah(long amount) {
        // Format manual: 299000 → "Rp 299.000"
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
