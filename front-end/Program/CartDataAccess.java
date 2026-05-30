package Program;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;


public class CartDataAccess {
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

    public static boolean processPaymentTransaction(
            String idCustomer, double subtotal,
            double uangDibayar, double kembalian,
            String courier, double ongkir) {

        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return false;

        PreparedStatement psUpdateAdmin   = null;
        PreparedStatement psInsertTrans   = null;
        PreparedStatement psGetCart       = null;
        PreparedStatement psInsertDetail  = null;
        PreparedStatement psUpdateProduct = null;
        PreparedStatement psGetCustomerAddress = null;
        PreparedStatement psInsertShipment = null;
        PreparedStatement psDeleteCart    = null;
        ResultSet         rsInsert        = null;
        ResultSet         rsCart          = null;
        ResultSet         rsAddress       = null;

        try {
            conn.setAutoCommit(false);

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
            psInsertTrans = conn.prepareStatement(
                "INSERT INTO Transaksi " +
                "(id_customer, total_belanja, uang_dibayar, kembalian, tanggal_transaksi, status_pengiriman) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            psInsertTrans.setString(1, idCustomer);
            psInsertTrans.setDouble(2, subtotal);
            psInsertTrans.setDouble(3, uangDibayar);
            psInsertTrans.setDouble(4, kembalian);
            psInsertTrans.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            psInsertTrans.setString(6, "sedang dikirim");
            psInsertTrans.executeUpdate();

            rsInsert = psInsertTrans.getGeneratedKeys();
            int idTransaction = -1;
            if (rsInsert.next()) {
                idTransaction = rsInsert.getInt(1);
            }

            psGetCart = conn.prepareStatement(
                "SELECT c.id_product, p.nama_produk, p.harga, c.quantity, c.total_harga " +
                "FROM Cart c " +
                "JOIN Product p ON c.id_product = p.id_product " +
                "WHERE c.id_customer = ?");
            psGetCart.setString(1, idCustomer);
            rsCart = psGetCart.executeQuery();

            if (idTransaction > 0) {
                psInsertDetail = conn.prepareStatement(
                    "INSERT INTO TransaksiDetail " +
                    "(id_transaction, id_product, nama_produk, harga_satuan, quantity, subtotal) " +
                    "VALUES (?, ?, ?, ?, ?, ?)");

                psUpdateProduct = conn.prepareStatement(
                    "UPDATE Product SET stok = stok - ?, terjual = COALESCE(terjual, 0) + ? " +
                    "WHERE id_product = ?");

                while (rsCart.next()) {
                    String idProd = rsCart.getString("id_product");
                    int qty = rsCart.getInt("quantity");
                    
                    // Transaksi Detail
                    psInsertDetail.setInt(1, idTransaction);
                    psInsertDetail.setString(2, idProd);
                    psInsertDetail.setString(3, rsCart.getString("nama_produk"));
                    psInsertDetail.setDouble(4, rsCart.getDouble("harga"));
                    psInsertDetail.setInt(5, qty);
                    psInsertDetail.setDouble(6, rsCart.getDouble("total_harga"));
                    psInsertDetail.addBatch();

                    psUpdateProduct.setInt(1, qty);
                    psUpdateProduct.setInt(2, qty);
                    psUpdateProduct.setString(3, idProd);
                    psUpdateProduct.addBatch();
                }
                psInsertDetail.executeBatch();
                psUpdateProduct.executeBatch();
            }
            String alamatCustomer = "Alamat tidak ditentukan";
            psGetCustomerAddress = conn.prepareStatement("SELECT alamat FROM Customer WHERE id_customer = ?");
            psGetCustomerAddress.setString(1, idCustomer);
            rsAddress = psGetCustomerAddress.executeQuery();
            if (rsAddress.next() && rsAddress.getString("alamat") != null) {
                alamatCustomer = rsAddress.getString("alamat");
            }

            if (idTransaction > 0) {
                String cleanCourier = courier.replace("&", "").trim();
                String prefix = cleanCourier.length() >= 3 ? cleanCourier.substring(0, 3).toUpperCase() : "SHP";
                String resi = prefix + (1000000000L + new java.util.Random().nextLong(9000000000L));
                String idShipment = "SHP-" + String.format("%04d", idTransaction);

                psInsertShipment = conn.prepareStatement(
                    "INSERT INTO Shipment " +
                    "(id_shipment, id_transaction, alamat, tanggal_kirim, status_kirim, nama_jasa_kirim, resi, ongkir) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                psInsertShipment.setString(1, idShipment);
                psInsertShipment.setInt(2, idTransaction);
                psInsertShipment.setString(3, alamatCustomer);
                psInsertShipment.setDate(4, new java.sql.Date(System.currentTimeMillis()));
                psInsertShipment.setString(5, "sedang dikirim");
                psInsertShipment.setString(6, courier);
                psInsertShipment.setString(7, resi);
                psInsertShipment.setDouble(8, ongkir);
                psInsertShipment.executeUpdate();
            }

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
            closeQuietly(rsInsert);              closeQuietly(rsCart);
            closeQuietly(rsAddress);             closeQuietly(psUpdateAdmin); 
            closeQuietly(psInsertTrans);         closeQuietly(psGetCart);     
            closeQuietly(psInsertDetail);        closeQuietly(psUpdateProduct);
            closeQuietly(psGetCustomerAddress);  closeQuietly(psInsertShipment);
            closeQuietly(psDeleteCart);
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            closeQuietly(conn);
        }
    }

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
    public static int getLastTransactionId(String idCustomer) {
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return -1;
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(
                "SELECT TOP 1 id_transaction FROM Transaksi WHERE id_customer = ? ORDER BY tanggal_transaksi DESC"
            );
            ps.setString(1, idCustomer);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_transaction");
            }
        } catch (SQLException e) {
            System.err.println("Error getLastTransactionId: " + e.getMessage());
        } finally {
            closeQuietly(rs); closeQuietly(ps); closeQuietly(conn);
        }
        return -1;
    }

    public static Object[] getShipmentInfo(int idTransaction) {
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return null;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(
                "SELECT id_shipment, alamat, tanggal_kirim, status_kirim, nama_jasa_kirim, resi, ongkir " +
                "FROM Shipment WHERE id_transaction = ?"
            );
            ps.setInt(1, idTransaction);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new Object[] {
                    rs.getString("id_shipment"),
                    rs.getString("alamat"),
                    rs.getDate("tanggal_kirim"),
                    rs.getString("status_kirim"),
                    rs.getString("nama_jasa_kirim"),
                    rs.getString("resi"),
                    rs.getDouble("ongkir")
                };
            }
        } catch (SQLException e) {
            System.err.println("Error getShipmentInfo: " + e.getMessage());
        } finally {
            closeQuietly(rs); closeQuietly(ps); closeQuietly(conn);
        }
        return null;
    }

    public static boolean updateShipmentAndTransactionStatus(int idTransaction, String status) {
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return false;

        PreparedStatement psShip = null;
        PreparedStatement psTrans = null;
        try {
            conn.setAutoCommit(false);

            psShip = conn.prepareStatement("UPDATE Shipment SET status_kirim = ? WHERE id_transaction = ?");
            psShip.setString(1, status);
            psShip.setInt(2, idTransaction);
            psShip.executeUpdate();

            psTrans = conn.prepareStatement("UPDATE Transaksi SET status_pengiriman = ? WHERE id_transaction = ?");
            psTrans.setString(1, status);
            psTrans.setInt(2, idTransaction);
            psTrans.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updateShipmentAndTransactionStatus: " + e.getMessage());
            try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            closeQuietly(psShip); closeQuietly(psTrans);
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
            closeQuietly(conn);
        }
    }

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
