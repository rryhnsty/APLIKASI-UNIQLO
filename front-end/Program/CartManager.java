package Program;

import java.util.ArrayList;
import java.util.List;

/**
 * CartManager.java
 * Singleton – menyimpan state keranjang di memori
 * sehingga HomePageView bisa menambah item dan CartView bisa membacanya.
 *
 * Alur:
 *  1. Saat "Tambah ke Keranjang" ditekan di HomePageView  →  CartManager.getInstance().addItem(...)
 *  2. CartView membaca  CartManager.getInstance().getItems()
 *  3. Setelah checkout CartView memanggil  CartManager.getInstance().clear()
 */
public class CartManager {

    // ── Singleton ──────────────────────────────────────────────────────────
    private static CartManager instance;

    public static CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    // ── State ──────────────────────────────────────────────────────────────
    private final List<CartItem> items = new ArrayList<>();

    // customerId diset setelah login; dipakai saat menyimpan ke DB
    private String customerId = "C001";   // default fallback

    private CartManager() {}

    // ── customerId ─────────────────────────────────────────────────────────
    public void setCustomerId(String id) { this.customerId = id; }
    public String getCustomerId()        { return customerId;    }

    // ── Tambah item ────────────────────────────────────────────────────────
    /**
     * Tambahkan produk ke keranjang.
     * Jika produk + size sudah ada, cukup tambah qty.
     */
    public void addItem(Product product, int qty, String size) {
        for (CartItem ci : items) {
            if (ci.getProduct().getCategory().equals(product.getCategory())
                    && ci.getSize().equals(size)) {
                ci.setQuantity(ci.getQuantity() + qty);
                return;
            }
        }
        // id sementara: pakai index list (nanti diganti id dari DB)
        items.add(new CartItem(items.size() + 1, "CART-" + customerId, product, qty, size));
    }

    // ── Getter ─────────────────────────────────────────────────────────────
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

    // ── Hapus / update ─────────────────────────────────────────────────────
    public void removeItem(CartItem ci)           { items.remove(ci); }
    public void clear()                           { items.clear();    }
}
