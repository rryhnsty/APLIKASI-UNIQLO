package Program;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;


public class DatabaseHelper {

    
    
    
    
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String MYSQL_URL    = "jdbc:sqlserver://localhost:1433;databaseName=Uniqlo;encrypt=true;trustServerCertificate=true";
    private static final String MYSQL_USER   = "sa";
    private static final String MYSQL_PASS   = "revanna16"; 

    
    private static final String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String SQLSERVER_URL    = "jdbc:sqlserver://localhost:1433;databaseName=Uniqlo;encrypt=true;trustServerCertificate=true";
    private static final String SQLSERVER_USER   = "sa";
    private static final String SQLSERVER_PASS   = "revanna16";

    
    private static final boolean USE_MYSQL = false; 

    private static boolean schemaInitialized = false;

    
    public static Connection getConnection() {
        Connection conn = null;
        try {
            if (USE_MYSQL) {
                
                Class.forName(MYSQL_DRIVER);
                conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
            } else {
                
                Class.forName(SQLSERVER_DRIVER);
                conn = DriverManager.getConnection(SQLSERVER_URL, SQLSERVER_USER, SQLSERVER_PASS);
            }

            if (conn != null && !schemaInitialized) {
                initializeDatabaseSchema(conn);
                schemaInitialized = true;
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

    private static void initializeDatabaseSchema(Connection conn) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            
            
            stmt.execute(
                "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Product') AND name = 'terjual') " +
                "BEGIN " +
                "    ALTER TABLE Product ADD terjual INT DEFAULT 0; " +
                "END"
            );

            
            stmt.execute(
                "IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Transaksi') AND name = 'status_pengiriman') " +
                "BEGIN " +
                "    ALTER TABLE Transaksi ADD status_pengiriman VARCHAR(100) DEFAULT 'sedang dikirim'; " +
                "END"
            );

            
            stmt.execute(
                "IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Shipment') " +
                "BEGIN " +
                "    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Shipment') AND name = 'id_transaction') " +
                "    BEGIN " +
                "        DROP TABLE Shipment; " +
                "    END " +
                "END"
            );

            
            stmt.execute(
                "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Shipment') " +
                "BEGIN " +
                "    CREATE TABLE Shipment ( " +
                "        id_shipment VARCHAR(50) PRIMARY KEY, " +
                "        id_transaction INT, " +
                "        alamat TEXT, " +
                "        tanggal_kirim DATE, " +
                "        status_kirim VARCHAR(100) DEFAULT 'sedang dikirim', " +
                "        nama_jasa_kirim VARCHAR(100), " +
                "        resi VARCHAR(100), " +
                "        ongkir DECIMAL(12,2), " +
                "        FOREIGN KEY (id_transaction) REFERENCES Transaksi(id_transaction) ON DELETE CASCADE " +
                "    ); " +
                "END"
            );
        } catch (SQLException e) {
            System.err.println("Error saat inisialisasi skema database: " + e.getMessage());
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException ignored) {}
            }
        }
    }
}

