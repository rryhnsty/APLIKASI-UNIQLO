package Program;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * CartDataAccess.java
 * Data Access Object untuk operasi database tabel Cart, Product, dan Transaksi.
 *
 * PERUBAHAN:
 *  - processPaymentTransaction() menyimpan snapshot Cart ke TransaksiDetail
 *    SEBELUM Cart dihapus → ShipmentView bisa tampilkan detail produk per baris.
 *  - Ditambahkan getLastTransaksiDetail() untuk ShipmentView.
 */
public class CartDataAccess {

    // ================= ADD TO CART =================
    public static boolean addToCart(String idCustomer, String idProduct) {
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return false;

        PreparedStatement psCheckProduct = null;
        PreparedStatement psCheckCart    = null;
        PreparedStatement psInsertCart   = null;
        PreparedStatement psUpdateCart   = null;
        ResultSet rsCheckProduct = null;
        ResultSet rsCheckCart    = null;

        try {
            // 1. Ambil harga produk
            psCheckProduct = conn.prepareStatement(
                "SELECT harga FROM Product WHERE id_product = ?");
            psCheckProduct.setString(1, idProduct);
            rsCheckProduct = psCheckProduct.executeQuery();

            double hargaProduk = 0;
            if (rsCheckProduct.next()) {
                hargaProduk = rsCheckProduct.getDouble("harga");
            } else {
                System.err.println("Produk tidak ditemukan: " + idProduct);
                return false;
            }

            // 2. Cek apakah produk sudah ada di Cart
            psCheckCart = conn.prepareStatement(
                "SELECT id_cart, quantity FROM Cart WHERE id_customer = ? AND id_product = ?");
            psCheckCart.setString(1, idCustomer);
            psCheckCart.setString(2, idProduct);
            rsCheckCart = psCheckCart.executeQuery();

            if (rsCheckCart.next()) {
                int idCart     = rsCheckCart.getInt("id_cart");
                int newQty     = rsCheckCart.getInt("quantity") + 1;
                double newTotal = newQty * hargaProduk;

                psUpdateCart = conn.prepareStatement(
                    "UPDATE Cart SET quantity = ?, total_harga = ? WHERE id_cart = ?");
                psUpdateCart.setInt(1, newQty);
                psUpdateCart.setDouble(2, newTotal);
                psUpdateCart.setInt(3, idCart);
                psUpdateCart.executeUpdate();
            } else {
                psInsertCart = conn.prepareStatement(
                    "INSERT INTO Cart (id_customer, id_product, quantity, total_harga) VALUES (?, ?, 1, ?)");
                psInsertCart.setString(1, idCustomer);
                psInsertCart.setString(2, idProduct);
                psInsertCart.setDouble(3, hargaProduk);
                psInsertCart.executeUpdate();
            }
            return true;

        } catch (SQLException e) {
            System.err.println("Error addToCart: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                "Gagal menyimpan ke database!\nDetail: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            closeQuietly(rsCheckProduct); closeQuietly(rsCheckCart);
            closeQuietly(psCheckProduct); closeQuietly(psCheckCart);
            closeQuietly(psInsertCart);   closeQuietly(psUpdateCart);
            closeQuietly(conn);
        }
    }

    // ================= GET CART ITEMS =================
    public static List<CartItem> getCartItems(String idCustomer) {
        List<CartItem> items = new ArrayList<>();
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return items;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                "SELECT c.id_cart, c.id_product, c.quantity, c.total_harga, " +
                "p.nama_produk, p.harga " +
                "FROM Cart c " +
                "JOIN Product p ON c.id_product = p.id_product " +
                "WHERE c.id_customer = ?");
            ps.setString(1, idCustomer);
            rs = ps.executeQuery();

            while (rs.next()) {
                int    idCart    = rs.getInt("id_cart");
                String idProduct = rs.getString("id_product");
                String namaProd  = rs.getString("nama_produk");
                double harga     = rs.getDouble("harga");
                int    quantity  = rs.getInt("quantity");

                String priceFormatted = formatRupiah((long) harga);
                String imagePath      = "images/" + idProduct.toLowerCase() + ".png";

                Product  product = new Product(idProduct, namaProd, priceFormatted, imagePath);
                CartItem item    = new CartItem(idCart, "CART-" + idCustomer, product, quantity, "M");
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getCartItems: " + e.getMessage());
        } finally {
            closeQuietly(rs); closeQuietly(ps); closeQuietly(conn);
        }
        return items;
    }

    // ================= REMOVE CART ITEM =================
    public static boolean removeCartItem(int idCart) {
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return false;

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("DELETE FROM Cart WHERE id_cart = ?");
            ps.setInt(1, idCart);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error removeCartItem: " + e.getMessage());
            return false;
        } finally {
            closeQuietly(ps); closeQuietly(conn);
        }
    }

    // ================= PROCESS PAYMENT TRANSACTION =================
    /**
     * Alur atomik (semua dalam satu SQL Transaction):
     *  1. Update saldo Admin.
     *  2. INSERT ke Transaksi → ambil id_transaction baru.
     *  3. Ambil snapshot Cart (JOIN Product).
     *  4. Batch INSERT snapshot ke TransaksiDetail.
     *  5. DELETE Cart customer.
     */
    public static boolean processPaymentTransaction(
            String idCustomer, double subtotal,
            double uangDibayar, double kembalian) {

        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return false;

        PreparedStatement psUpdateAdmin  = null;
        PreparedStatement psInsertTrans  = null;
        PreparedStatement psGetCart      = null;
        PreparedStatement psInsertDetail = null;
        PreparedStatement psDeleteCart   = null;
        ResultSet         rsInsert       = null;
        ResultSet         rsCart         = null;

        try {
            conn.setAutoCommit(false);

            // ── 1. Update saldo Admin A001 ───────────────────────────────────
            psUpdateAdmin = conn.prepareStatement(
                "UPDATE Admin SET saldo = COALESCE(saldo, 0) + ? WHERE id_admin = 'A001'");
            psUpdateAdmin.setDouble(1, subtotal);
            int adminUpdated = psUpdateAdmin.executeUpdate();

            if (adminUpdated == 0) {
                closeQuietly(psUpdateAdmin);
                psUpdateAdmin = conn.prepareStatement(
                    "UPDATE Admin SET saldo = COALESCE(saldo, 0) + ? " +
                    "WHERE id_admin = (SELECT MIN(id_admin) FROM Admin)");
                psUpdateAdmin.setDouble(1, subtotal);
                psUpdateAdmin.executeUpdate();
            }

            // ── 2. INSERT ke Transaksi, ambil id_transaction baru ────────────
            psInsertTrans = conn.prepareStatement(
                "INSERT INTO Transaksi " +
                "(id_customer, total_belanja, uang_dibayar, kembalian, tanggal_transaksi) " +
                "VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            psInsertTrans.setString(1, idCustomer);
            psInsertTrans.setDouble(2, subtotal);
            psInsertTrans.setDouble(3, uangDibayar);
            psInsertTrans.setDouble(4, kembalian);
            psInsertTrans.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            psInsertTrans.executeUpdate();

            rsInsert = psInsertTrans.getGeneratedKeys();
            int idTransaction = -1;
            if (rsInsert.next()) {
                idTransaction = rsInsert.getInt(1);
            }

            // ── 3. Snapshot Cart sebelum dihapus ─────────────────────────────
            psGetCart = conn.prepareStatement(
                "SELECT c.id_product, p.nama_produk, p.harga, c.quantity, c.total_harga " +
                "FROM Cart c " +
                "JOIN Product p ON c.id_product = p.id_product " +
                "WHERE c.id_customer = ?");
            psGetCart.setString(1, idCustomer);
            rsCart = psGetCart.executeQuery();

            // ── 4. Batch INSERT ke TransaksiDetail ───────────────────────────
            if (idTransaction > 0) {
                psInsertDetail = conn.prepareStatement(
                    "INSERT INTO TransaksiDetail " +
                    "(id_transaction, id_product, nama_produk, harga_satuan, quantity, subtotal) " +
                    "VALUES (?, ?, ?, ?, ?, ?)");

                while (rsCart.next()) {
                    psInsertDetail.setInt(1, idTransaction);
                    psInsertDetail.setString(2, rsCart.getString("id_product"));
                    psInsertDetail.setString(3, rsCart.getString("nama_produk"));
                    psInsertDetail.setDouble(4, rsCart.getDouble("harga"));
                    psInsertDetail.setInt(5, rsCart.getInt("quantity"));
                    psInsertDetail.setDouble(6, rsCart.getDouble("total_harga"));
                    psInsertDetail.addBatch();
                }
                psInsertDetail.executeBatch();
            }

            // ── 5. Hapus seluruh Cart customer ───────────────────────────────
            psDeleteCart = conn.prepareStatement(
                "DELETE FROM Cart WHERE id_customer = ?");
            psDeleteCart.setString(1, idCustomer);
            psDeleteCart.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Transaksi gagal! Rollback... Detail: " + e.getMessage());
            try { conn.rollback(); } catch (SQLException ex) {
                System.err.println("Rollback gagal: " + ex.getMessage());
            }
            return false;
        } finally {
            closeQuietly(rsInsert);      closeQuietly(rsCart);
            closeQuietly(psUpdateAdmin); closeQuietly(psInsertTrans);
            closeQuietly(psGetCart);     closeQuietly(psInsertDetail);
            closeQuietly(psDeleteCart);
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            closeQuietly(conn);
        }
    }

    // ================= GET LAST TRANSAKSI DETAIL (untuk ShipmentView) =================
    /**
     * Ambil detail item dari transaksi terakhir customer.
     * Return: List<Object[]> = { id_product, nama_produk, harga_satuan_fmt, qty, subtotal_fmt }
     */
    public static List<Object[]> getLastTransaksiDetail(String idCustomer) {
        List<Object[]> rows = new ArrayList<>();
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return rows;

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(
                "SELECT td.id_product, td.nama_produk, td.harga_satuan, " +
                "       td.quantity, td.subtotal " +
                "FROM TransaksiDetail td " +
                "JOIN Transaksi t ON td.id_transaction = t.id_transaction " +
                "WHERE t.id_customer = ? " +
                "  AND t.id_transaction = (" +
                "       SELECT TOP 1 id_transaction FROM Transaksi " +
                "       WHERE id_customer = ? " +
                "       ORDER BY tanggal_transaksi DESC" +
                "  )");
            ps.setString(1, idCustomer);
            ps.setString(2, idCustomer);
            rs = ps.executeQuery();

            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString("id_product"),
                    rs.getString("nama_produk"),
                    formatRupiah((long) rs.getDouble("harga_satuan")),
                    rs.getInt("quantity"),
                    formatRupiah((long) rs.getDouble("subtotal"))
                });
            }
        } catch (SQLException e) {
            System.err.println("Error getLastTransaksiDetail: " + e.getMessage());
        } finally {
            closeQuietly(rs); closeQuietly(ps); closeQuietly(conn);
        }
        return rows;
    }

    // ================= HELPERS =================
    private static void closeQuietly(AutoCloseable resource) {
        if (resource != null) try { resource.close(); } catch (Exception ignored) {}
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