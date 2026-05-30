package Program;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * CartDAO.java
 * Data Access Object untuk operasi database tabel Cart, Product, dan Transaction.
 * Mendukung MySQL dan SQL Server melalui DatabaseHelper.
 */
public class CartDataAccess {

    // ================= ADD TO CART =================
    /**
     * Menambahkan produk ke keranjang.
     * Jika produk sudah ada di cart customer, quantity bertambah 1 dan total_harga diperbarui.
     * Jika belum ada, data baru akan ditambahkan ke tabel Cart.
     */
    public static boolean addToCart(String idCustomer, String idProduct) {
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return false;

        PreparedStatement psCheckProduct = null;
        PreparedStatement psCheckCart = null;
        PreparedStatement psInsertCart = null;
        PreparedStatement psUpdateCart = null;
        ResultSet rsCheckProduct = null;
        ResultSet rsCheckCart = null;

        try {
            // 1. Ambil harga produk dari tabel Product
            String sqlCheckProduct = "SELECT harga FROM Product WHERE id_product = ?";
            psCheckProduct = conn.prepareStatement(sqlCheckProduct);
            psCheckProduct.setString(1, idProduct);
            rsCheckProduct = psCheckProduct.executeQuery();

            double hargaProduk = 0;
            if (rsCheckProduct.next()) {
                hargaProduk = rsCheckProduct.getDouble("harga");
            } else {
                System.err.println("Produk tidak ditemukan di database!");
                return false;
            }

            // 2. Cek apakah produk sudah ada di Cart customer tersebut
            String sqlCheckCart = "SELECT id_cart, quantity FROM Cart WHERE id_customer = ? AND id_product = ?";
            psCheckCart = conn.prepareStatement(sqlCheckCart);
            psCheckCart.setString(1, idCustomer);
            psCheckCart.setString(2, idProduct);
            rsCheckCart = psCheckCart.executeQuery();

            if (rsCheckCart.next()) {
                // Jika produk sudah ada, quantity bertambah 1
                int idCart = rsCheckCart.getInt("id_cart");
                int currentQty = rsCheckCart.getInt("quantity");
                int newQty = currentQty + 1;
                double newTotalHarga = newQty * hargaProduk;

                String sqlUpdate = "UPDATE Cart SET quantity = ?, total_harga = ? WHERE id_cart = ?";
                psUpdateCart = conn.prepareStatement(sqlUpdate);
                psUpdateCart.setInt(1, newQty);
                psUpdateCart.setDouble(2, newTotalHarga);
                psUpdateCart.setInt(3, idCart);
                psUpdateCart.executeUpdate();
            } else {
                // Jika produk belum ada, buat data baru di Cart
                String sqlInsert = "INSERT INTO Cart (id_customer, id_product, quantity, total_harga) VALUES (?, ?, 1, ?)";
                psInsertCart = conn.prepareStatement(sqlInsert);
                psInsertCart.setString(1, idCustomer);
                psInsertCart.setString(2, idProduct);
                psInsertCart.setDouble(3, hargaProduk);
                psInsertCart.executeUpdate();
            }
            return true;

        } catch (SQLException e) {
            System.err.println("Error addToCart: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Gagal menyimpan ke database!\nDetail Error: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            closeQuietly(rsCheckProduct);
            closeQuietly(rsCheckCart);
            closeQuietly(psCheckProduct);
            closeQuietly(psCheckCart);
            closeQuietly(psInsertCart);
            closeQuietly(psUpdateCart);
            closeQuietly(conn);
        }
    }

    // ================= GET CART ITEMS =================
    /**
     * Mengambil semua item keranjang milik customer tertentu.
     * Menggunakan JOIN antara tabel Cart dan Product.
     */
    public static List<CartItem> getCartItems(String idCustomer) {
        List<CartItem> items = new ArrayList<>();
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return items;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT c.id_cart, c.id_product, c.quantity, c.total_harga, " +
                         "p.nama_produk, p.harga " +
                         "FROM Cart c " +
                         "JOIN Product p ON c.id_product = p.id_product " +
                         "WHERE c.id_customer = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, idCustomer);
            rs = ps.executeQuery();

            while (rs.next()) {
                int idCart = rs.getInt("id_cart");
                String idProduct = rs.getString("id_product");
                String namaProduk = rs.getString("nama_produk");
                double harga = rs.getDouble("harga");
                int quantity = rs.getInt("quantity");

                // Format harga ke "Rp xx.xxx"
                String priceFormatted = formatRupiah((long) harga);
                String imagePath = "images/" + idProduct.toLowerCase() + ".png";

                Product product = new Product(idProduct, namaProduk, priceFormatted, imagePath);
                CartItem item = new CartItem(idCart, "CART-" + idCustomer, product, quantity, "M");
                items.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Error getCartItems: " + e.getMessage());
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }
        return items;
    }

    // ================= REMOVE CART ITEM =================
    /**
     * Menghapus item keranjang belanja berdasarkan id_cart.
     */
    public static boolean removeCartItem(int idCart) {
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return false;

        PreparedStatement ps = null;
        try {
            String sql = "DELETE FROM Cart WHERE id_cart = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idCart);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error removeCartItem: " + e.getMessage());
            return false;
        } finally {
            closeQuietly(ps);
            closeQuietly(conn);
        }
    }

    // ================= TRANSACTION (Pembayaran) =================
    /**
     * Menjalankan SQL Transaction untuk proses pembayaran.
     * Alur:
     * 1. Tambah subtotal ke saldo Admin 'A001'.
     * 2. Simpan transaksi ke tabel Transaction.
     * 3. Hapus seluruh data Cart customer.
     */
    public static boolean processPaymentTransaction(String idCustomer, double subtotal, double uangDibayar, double kembalian) {
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return false;

        PreparedStatement psUpdateAdmin = null;
        PreparedStatement psInsertTrans = null;
        PreparedStatement psDeleteCart = null;

        try {
            // Mengaktifkan Mode Transaksi
            conn.setAutoCommit(false);

            // 1. Tambahkan total_belanja ke saldo Admin 'A001'
            String sqlUpdateAdmin = "UPDATE Admin SET saldo = COALESCE(saldo, 0) + ? WHERE id_admin = 'A001'";
            psUpdateAdmin = conn.prepareStatement(sqlUpdateAdmin);
            psUpdateAdmin.setDouble(1, subtotal);
            int adminUpdated = psUpdateAdmin.executeUpdate();

            if (adminUpdated == 0) {
                // Fallback jika admin 'A001' tidak ditemukan, cari admin pertama dengan query lintas DB
                closeQuietly(psUpdateAdmin);
                String sqlUpdateFirstAdmin = "UPDATE Admin SET saldo = COALESCE(saldo, 0) + ? WHERE id_admin = (SELECT MIN(id_admin) FROM Admin)";
                psUpdateAdmin = conn.prepareStatement(sqlUpdateFirstAdmin);
                psUpdateAdmin.setDouble(1, subtotal);
                psUpdateAdmin.executeUpdate();
            }

            // 2. Simpan data transaksi ke tabel Transactions
            String sqlInsertTrans = "INSERT INTO Transaksi (id_customer, total_belanja, uang_dibayar, kembalian, tanggal_transaksi) " +
                                    "VALUES (?, ?, ?, ?, ?)";
            psInsertTrans = conn.prepareStatement(sqlInsertTrans);
            psInsertTrans.setString(1, idCustomer);
            psInsertTrans.setDouble(2, subtotal);
            psInsertTrans.setDouble(3, uangDibayar);
            psInsertTrans.setDouble(4, kembalian);
            psInsertTrans.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            psInsertTrans.executeUpdate();

            // 3. Hapus seluruh data Cart customer setelah pembayaran berhasil
            String sqlDeleteCart = "DELETE FROM Cart WHERE id_customer = ?";
            psDeleteCart = conn.prepareStatement(sqlDeleteCart);
            psDeleteCart.setString(1, idCustomer);
            psDeleteCart.executeUpdate();

            // COMMIT jika seluruh proses sukses
            conn.commit();
            return true;

        } catch (SQLException e) {
            // ROLLBACK jika salah satu proses gagal
            System.err.println("Transaksi gagal! Melakukan rollback... Detail: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback gagal: " + ex.getMessage());
            }
            return false;
        } finally {
            closeQuietly(psUpdateAdmin);
            closeQuietly(psInsertTrans);
            closeQuietly(psDeleteCart);
            try {
                conn.setAutoCommit(true); // kembalikan ke default
            } catch (SQLException ignored) {}
            closeQuietly(conn);
        }
    }

    // ================= HELPERS =================
    private static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {}
        }
    }

    private static String formatRupiah(long amount) {
        String s = String.valueOf(amount);
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
