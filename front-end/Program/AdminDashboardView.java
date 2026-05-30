package Program;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * AdminDashboardView.java
 * GUI Dashboard Admin yang modern untuk mengelola data penjualan, menampilkan informasi
 * admin yang aktif, total pendapatan, dan statistik barang terjual per item.
 */
public class AdminDashboardView extends JFrame {

    // ── Konstanta Warna Premium ────────────────────────────────────────────────
    private static final Color COLOR_RED_BRAND    = new Color(200, 30, 40);
    private static final Color COLOR_RED_HOVER    = new Color(160, 20, 30);
    private static final Color COLOR_BG_LIGHT     = new Color(245, 246, 248);
    private static final Color COLOR_TEXT_DARK    = new Color(33, 37, 41);
    private static final Color COLOR_TEXT_MUTED   = new Color(108, 117, 125);
    private static final Color COLOR_BORDER       = new Color(222, 226, 230);
    private static final Color COLOR_CARD_BG      = Color.WHITE;

    // ── Data Admin Aktif ───────────────────────────────────────────────────────
    private String adminId = "-";
    private String adminNama = "-";
    private String adminEmail = "-";
    private String adminNoHp = "-";

    // ── Komponen UI Statistik ──────────────────────────────────────────────────
    private JLabel totalRevenueLabel;
    private JLabel totalSoldLabel;
    private JLabel adminNameLabel;
    private JLabel adminEmailLabel;
    private JLabel adminPhoneLabel;
    private JTable productTable;
    private DefaultTableModel tableModel;

    public AdminDashboardView(String adminEmail) {
        this.adminEmail = adminEmail;

        // Ambil data admin dari DB
        loadAdminData();

        setTitle("Admin Dashboard - Style Connect");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main Container
        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setBackground(COLOR_BG_LIGHT);
        rootPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // 1. Header Panel
        rootPanel.add(buildHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Workspace (Split Pane: Kiri = Info Admin, Kanan = Statistik & Tabel)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(320);
        mainSplit.setDividerSize(10);
        mainSplit.setEnabled(false);
        mainSplit.setBorder(null);
        mainSplit.setOpaque(false);

        mainSplit.setLeftComponent(buildLeftPanel());
        mainSplit.setRightComponent(buildRightPanel());

        rootPanel.add(mainSplit, BorderLayout.CENTER);
        add(rootPanel);

        // Load data statistik & tabel produk
        refreshDashboardData();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  1. HEADER PANEL
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 2, 2));
        titleBlock.setOpaque(false);

        JLabel mainTitle = new JLabel("Admin Control Center");
        mainTitle.setFont(new Font("Arial", Font.BOLD, 28));
        mainTitle.setForeground(COLOR_TEXT_DARK);

        JLabel subTitle = new JLabel("Kelola inventori, penjualan, dan pantau performa toko Anda.");
        subTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subTitle.setForeground(COLOR_TEXT_MUTED);

        titleBlock.add(mainTitle);
        titleBlock.add(subTitle);

        // Tombol Logout
        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 13));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(COLOR_RED_BRAND);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_RED_BRAND),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { logoutBtn.setBackground(COLOR_RED_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { logoutBtn.setBackground(COLOR_RED_BRAND); }
        });
        logoutBtn.addActionListener(e -> {
            new LoginPageView().setVisible(true);
            dispose();
        });

        panel.add(titleBlock, BorderLayout.WEST);
        panel.add(logoutBtn, BorderLayout.EAST);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  2. LEFT PANEL: DATA ADMIN & INFORMASI PRIBADI
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setOpaque(false);
        outerPanel.setBorder(new EmptyBorder(0, 0, 0, 15));

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(COLOR_CARD_BG);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            BorderFactory.createEmptyBorder(30, 20, 30, 20)
        ));

        // Profil Avatar Bulat Sederhana
        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(avatarLabel);
        cardPanel.add(Box.createVerticalStrut(15));

        // Nama Admin
        adminNameLabel = new JLabel(adminNama);
        adminNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        adminNameLabel.setForeground(COLOR_TEXT_DARK);
        adminNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(adminNameLabel);

        // Badge Admin
        JLabel badgeLabel = new JLabel("ADMINISTRATOR");
        badgeLabel.setFont(new Font("Arial", Font.BOLD, 10));
        badgeLabel.setForeground(COLOR_RED_BRAND);
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(new Color(255, 230, 230));
        badgeLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        badgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(badgeLabel);
        cardPanel.add(Box.createVerticalStrut(30));

        // Details Grid
        JPanel detailsGrid = new JPanel(new GridLayout(6, 1, 5, 5));
        detailsGrid.setOpaque(false);
        detailsGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        addDetailRow(detailsGrid, "ID Karyawan:", adminId);
        addDetailRow(detailsGrid, "Alamat Email:", adminEmail);
        addDetailRow(detailsGrid, "Nomor Telepon:", adminNoHp);

        cardPanel.add(detailsGrid);
        cardPanel.add(Box.createVerticalGlue());

        // Tombol Refresh Data
        JButton refreshBtn = new JButton("Refresh Dashboard");
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 13));
        refreshBtn.setForeground(COLOR_RED_BRAND);
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_RED_BRAND),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        refreshBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { refreshBtn.setBackground(new Color(245, 245, 245)); }
            @Override public void mouseExited(MouseEvent e)  { refreshBtn.setBackground(Color.WHITE); }
        });
        refreshBtn.addActionListener(e -> refreshDashboardData());

        // Tombol Reset Data Penjualan
        JButton resetBtn = new JButton("Reset Data Penjualan");
        resetBtn.setFont(new Font("Arial", Font.BOLD, 13));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setBackground(new Color(220, 53, 69)); // Bootstrap Danger Red
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 53, 69)),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { resetBtn.setBackground(new Color(180, 40, 50)); }
            @Override public void mouseExited(MouseEvent e)  { resetBtn.setBackground(new Color(220, 53, 69)); }
        });
        resetBtn.addActionListener(e -> handleResetData());

        // Container Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(resetBtn);

        cardPanel.add(buttonPanel);

        outerPanel.add(cardPanel, BorderLayout.CENTER);
        return outerPanel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  3. RIGHT PANEL: SUMMARY CARDS & PRODUCT TABLES
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // ── A. STATISTIK SUMMARY CARDS ──
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Card 1: Total Pendapatan
        JPanel revCard = createStatCard("TOTAL PENDAPATAN (OMSET)", "Rp 0", "", new Color(40, 167, 69));
        totalRevenueLabel = (JLabel) revCard.getClientProperty("valueLabel");
        statsPanel.add(revCard);

        // Card 2: Total Barang Terjual
        JPanel soldCard = createStatCard("TOTAL ITEM TERJUAL", "0 Pcs", "", COLOR_RED_BRAND);
        totalSoldLabel = (JLabel) soldCard.getClientProperty("valueLabel");
        statsPanel.add(soldCard);

        panel.add(statsPanel);
        panel.add(Box.createVerticalStrut(25));

        // ── B. TABEL PENJUALAN PER ITEM ──
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(COLOR_CARD_BG);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15),
                "Statistik Penjualan Per Item Produk",
                0, 0,
                new Font("Arial", Font.BOLD, 16),
                COLOR_TEXT_DARK
            )
        ));

        String[] columns = {"ID Produk", "Nama Produk", "Harga Satuan", "Sisa Stok", "Kuantitas Terjual"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        productTable = new JTable(tableModel);
        productTable.setFont(new Font("Arial", Font.PLAIN, 13));
        productTable.setRowHeight(38);
        productTable.setShowGrid(false);
        productTable.setIntercellSpacing(new Dimension(0, 0));
        productTable.setSelectionBackground(new Color(250, 242, 242));
        productTable.setSelectionForeground(COLOR_RED_BRAND);

        // Lebar kolom
        productTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(320);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(120);

        // Header styling
        JTableHeader tableHeader = productTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 13));
        tableHeader.setBackground(COLOR_RED_BRAND);
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setPreferredSize(new Dimension(100, 35));

        // Center Align Kolom Tertentu
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        productTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Alternate row colors
        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                if (col == 0 || col == 2 || col == 3 || col == 4) {
                    setHorizontalAlignment(JLabel.CENTER);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(null);

        tableContainer.add(scroll, BorderLayout.CENTER);
        panel.add(tableContainer);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DB DATA OPERATIONS
    // ─────────────────────────────────────────────────────────────────────────
    private void loadAdminData() {
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT id_admin, nama_admin, email, no_hp FROM Admin WHERE email = ?");
            ps.setString(1, adminEmail);
            rs = ps.executeQuery();
            if (rs.next()) {
                adminId = rs.getString("id_admin");
                adminNama = rs.getString("nama_admin");
                adminEmail = rs.getString("email");
                adminNoHp = rs.getString("no_hp");
            }
        } catch (SQLException e) {
            System.err.println("Error loadAdminData: " + e.getMessage());
        } finally {
            closeQuietly(rs); closeQuietly(ps); closeQuietly(conn);
        }
    }

    private void refreshDashboardData() {
        Connection conn = DatabaseHelper.getConnection();
        if (conn == null) return;

        Statement stmt = null;
        ResultSet rsRevenue = null;
        ResultSet rsSold = null;
        ResultSet rsProducts = null;

        try {
            stmt = conn.createStatement();

            // 1. Ambil Total Omset Pendapatan
            double totalRevenue = 0;
            rsRevenue = stmt.executeQuery("SELECT SUM(total_belanja) AS total FROM Transaksi");
            if (rsRevenue.next()) {
                totalRevenue = rsRevenue.getDouble("total");
            }
            totalRevenueLabel.setText(formatRupiah((long) totalRevenue));

            // 2. Ambil Total Item Terjual
            int totalSold = 0;
            rsSold = stmt.executeQuery("SELECT SUM(terjual) AS total FROM Product");
            if (rsSold.next()) {
                totalSold = rsSold.getInt("total");
            }
            totalSoldLabel.setText(totalSold + " Pcs");

            // 3. Muat Data Tabel Produk terurut paling banyak terjual
            tableModel.setRowCount(0);
            rsProducts = stmt.executeQuery(
                "SELECT id_product, nama_produk, harga, stok, COALESCE(terjual, 0) AS jml_terjual " +
                "FROM Product " +
                "ORDER BY jml_terjual DESC, stok ASC"
            );
            while (rsProducts.next()) {
                tableModel.addRow(new Object[] {
                    rsProducts.getString("id_product"),
                    rsProducts.getString("nama_produk"),
                    formatRupiah((long) rsProducts.getDouble("harga")),
                    rsProducts.getInt("stok"),
                    rsProducts.getInt("jml_terjual")
                });
            }

        } catch (SQLException e) {
            System.err.println("Error refreshDashboardData: " + e.getMessage());
        } finally {
            closeQuietly(rsRevenue); closeQuietly(rsSold);
            closeQuietly(rsProducts); closeQuietly(stmt);
            closeQuietly(conn);
        }
    }

    private void handleResetData() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Apakah Anda yakin ingin mereset seluruh data penjualan?\n" +
            "Tindakan ini akan mengosongkan semua riwayat transaksi, pengiriman, saldo admin, dan jumlah terjual produk menjadi 0.",
            "Konfirmasi Reset Data",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = DatabaseHelper.getConnection();
            if (conn == null) return;

            Statement stmt = null;
            try {
                conn.setAutoCommit(false);
                stmt = conn.createStatement();

                // 1. Kosongkan TransaksiDetail
                stmt.executeUpdate("DELETE FROM TransaksiDetail");

                // 2. Kosongkan Shipment
                stmt.executeUpdate("DELETE FROM Shipment");

                // 3. Kosongkan Transaksi
                stmt.executeUpdate("DELETE FROM Transaksi");

                // 4. Reset kolom terjual di Product ke 0
                stmt.executeUpdate("UPDATE Product SET terjual = 0");

                // 5. Reset saldo Admin ke 0
                stmt.executeUpdate("UPDATE Admin SET saldo = 0");

                conn.commit();
                
                JOptionPane.showMessageDialog(
                    this,
                    "Seluruh data penjualan, pendapatan, dan kuantitas produk terjual berhasil direset ke 0!",
                    "Reset Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Refresh tampilan dashboard
                refreshDashboardData();

            } catch (SQLException e) {
                System.err.println("Gagal mereset data: " + e.getMessage());
                try { conn.rollback(); } catch (SQLException ignored) {}
                JOptionPane.showMessageDialog(
                    this,
                    "Gagal mereset data!\nDetail: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                closeQuietly(stmt);
                closeQuietly(conn);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI CREATION HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createStatCard(String title, String initialVal, String iconSymbol, Color themeColor) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Panel Text
        JPanel textBlock = new JPanel(new GridLayout(2, 1, 2, 2));
        textBlock.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 11));
        titleLbl.setForeground(COLOR_TEXT_MUTED);

        JLabel valLbl = new JLabel(initialVal);
        valLbl.setFont(new Font("Arial", Font.BOLD, 24));
        valLbl.setForeground(themeColor);

        textBlock.add(titleLbl);
        textBlock.add(valLbl);

        // Panel Icon
        JLabel iconLbl = new JLabel(iconSymbol);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        iconLbl.setForeground(themeColor.brighter());

        card.add(textBlock, BorderLayout.CENTER);
        card.add(iconLbl, BorderLayout.EAST);

        // Simpan referensi ke valueLabel agar bisa di-update
        card.putClientProperty("valueLabel", valLbl);

        return card;
    }

    private void addDetailRow(JPanel grid, String title, String value) {
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 12));
        titleLbl.setForeground(COLOR_TEXT_MUTED);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        valLbl.setForeground(COLOR_TEXT_DARK);
        valLbl.setBorder(new EmptyBorder(0, 0, 10, 0));

        grid.add(titleLbl);
        grid.add(valLbl);
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

    private static void closeQuietly(AutoCloseable r) {
        if (r != null) try { r.close(); } catch (Exception ignored) {}
    }
}
