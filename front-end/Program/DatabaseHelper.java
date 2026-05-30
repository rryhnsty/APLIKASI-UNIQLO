package Program;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * DatabaseHelper.java
 * Class Utility terpusat untuk mengelola koneksi database MySQL / SQL Server.
 */
public class DatabaseHelper {

    // ================= CONFIGURATION =================
    // Silakan sesuaikan driver, url, username, dan password database Anda di sini.
    
    // Konfigurasi MySQL (Rekomendasi untuk kebutuhan baru)
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String MYSQL_URL    = "jdbc:sqlserver://localhost:1433;databaseName=Uniqlo;encrypt=true;trustServerCertificate=true";
    private static final String MYSQL_USER   = "sa";
    private static final String MYSQL_PASS   = "revanna16"; // Menggunakan password database lokal Anda

    // Konfigurasi SQL Server (Koneksi bawaan project)
    private static final String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String SQLSERVER_URL    = "jdbc:sqlserver://localhost:1433;databaseName=Uniqlo;encrypt=true;trustServerCertificate=true";
    private static final String SQLSERVER_USER   = "sa";
    private static final String SQLSERVER_PASS   = "revanna16";

    // Set true untuk menggunakan MySQL, false untuk SQL Server
    private static final boolean USE_MYSQL = false; 

    // ================= GET CONNECTION =================
    public static Connection getConnection() {
        Connection conn = null;
        try {
            if (USE_MYSQL) {
                // Load Driver MySQL
                Class.forName(MYSQL_DRIVER);
                conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
            } else {
                // Load Driver SQL Server
                Class.forName(SQLSERVER_DRIVER);
                conn = DriverManager.getConnection(SQLSERVER_URL, SQLSERVER_USER, SQLSERVER_PASS);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver tidak ditemukan: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "JDBC Driver tidak ditemukan! Pastikan library connector sudah ditambahkan di Classpath.\nDetail: " + e.getMessage(),
                "Driver Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            System.err.println("Gagal konek database: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                "Gagal terhubung ke database. Silakan pastikan server database menyala.\nDetail: " + e.getMessage(),
                "Database Connection Error", JOptionPane.ERROR_MESSAGE);
        }
        return conn;
    }
}
