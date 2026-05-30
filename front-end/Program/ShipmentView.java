package Program;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * ShipmentView.java
 * Halaman status pengiriman yang muncul setelah pembayaran berhasil.
 *
 * PERUBAHAN:
 *  - loadOrderItems() kini membaca dari tabel TransaksiDetail (via CartDataAccess)
 *    sehingga setiap produk yang dibeli ditampilkan sebagai baris terpisah
 *    lengkap dengan ID Produk, Nama, Harga Satuan, Qty, dan Subtotal.
 *  - Tombol "Lacak Pesanan" dihapus.
 *  - Alur logika Info Pengiriman (countdown, badge, status) tidak berubah.
 */
public class ShipmentView extends JFrame {

    // ── Konstanta Warna ───────────────────────────────────────────────────────
    private static final Color COLOR_RED_BRAND    = new Color(200, 30, 40);
    private static final Color COLOR_RED_HOVER    = new Color(160, 20, 30);
    private static final Color COLOR_TEXT_GRAY    = new Color(100, 100, 100);
    private static final Color COLOR_BG_LIGHT     = new Color(248, 249, 250);
    private static final Color COLOR_INPUT_BORDER = new Color(220, 220, 220);
    private static final Color COLOR_GREEN_ON     = new Color(34, 139, 34);
    private static final Color COLOR_GREEN_BG     = new Color(210, 245, 220);
    private static final Color COLOR_YELLOW_ON    = new Color(180, 120, 0);
    private static final Color COLOR_YELLOW_BG    = new Color(255, 243, 205);

    // ── Data Transaksi ────────────────────────────────────────────────────────
    private final String customerId;
    private final double grandTotal;
    private final double uangDibayar;
    private final double kembalian;

    // ── Komponen UI ───────────────────────────────────────────────────────────
    private JLabel           statusBadge;
    private JLabel           statusIconLabel;
    private JLabel           statusTextLabel;
    private JLabel           etaLabel;
    private DefaultTableModel tableModel;

    // ── Timer ─────────────────────────────────────────────────────────────────
    private Timer countdownTimer;
    private int   secondsLeft = 15;

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────────────────
    public ShipmentView(String customerId, double grandTotal,
                        double uangDibayar, double kembalian) {
        this.customerId  = customerId;
        this.grandTotal  = grandTotal;
        this.uangDibayar = uangDibayar;
        this.kembalian   = kembalian;

        setTitle("Status Pengiriman - JEKI Store");
        setSize(1050, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ── Root Panel ────────────────────────────────────────────────────────
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // ── 1. HEADER ─────────────────────────────────────────────────────────
        mainPanel.add(buildHeader(), BorderLayout.NORTH);

        // ── 2. SPLIT: kiri (tabel barang) | kanan (status + ringkasan) ────────
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(),
                buildRightPanel()
        );
        split.setDividerLocation(620);
        split.setDividerSize(5);
        split.setEnabled(false);
        split.setBorder(null);
        mainPanel.add(split, BorderLayout.CENTER);

        add(mainPanel);

        // ── Load data dari DB ─────────────────────────────────────────────────
        loadOrderItems();

        // ── Mulai countdown 15 detik ──────────────────────────────────────────
        startCountdown();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HEADER
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Status Pengiriman");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.BLACK);

        JButton homeButton = new JButton("Kembali ke Beranda");
        homeButton.setFont(new Font("Arial", Font.BOLD, 13));
        homeButton.setBackground(Color.WHITE);
        homeButton.setForeground(COLOR_TEXT_GRAY);
        homeButton.setFocusPainted(false);
        homeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        homeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        homeButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                homeButton.setBackground(new Color(245, 245, 245));
            }
            @Override public void mouseExited(MouseEvent e) {
                homeButton.setBackground(Color.WHITE);
            }
        });
        homeButton.addActionListener(e -> {
            stopCountdown();
            new HomePageView(customerId).setVisible(true);
            dispose();
        });

        header.add(title,      BorderLayout.WEST);
        header.add(homeButton, BorderLayout.EAST);
        return header;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PANEL KIRI – Tabel Barang yang Dipesan
    //  Setiap produk ditampilkan dalam baris terpisah dengan kolom lengkap.
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                "Daftar Barang Pesanan",
                0, 0,
                new Font("Arial", Font.BOLD, 15),
                COLOR_RED_BRAND
        ));

        // Kolom tabel
        String[] cols = {"ID Produk", "Nama Produk", "Harga Satuan", "Qty", "Subtotal"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(250, 242, 242));
        table.setSelectionForeground(COLOR_RED_BRAND);

        // Lebar kolom
        table.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID Produk
        table.getColumnModel().getColumn(1).setPreferredWidth(220); // Nama Produk
        table.getColumnModel().getColumn(2).setPreferredWidth(110); // Harga Satuan
        table.getColumnModel().getColumn(3).setPreferredWidth(50);  // Qty
        table.getColumnModel().getColumn(4).setPreferredWidth(110); // Subtotal

        // Header styling
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 13));
        tableHeader.setBackground(COLOR_RED_BRAND);
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setPreferredSize(new Dimension(100, 38));

        // Renderer tengah untuk kolom tertentu
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // ID Produk
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Harga
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Qty
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Subtotal

        // Renderer kiri untuk Nama Produk agar teks panjang tetap terbaca
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);

        // Striped rows (alternating background)
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 248, 248));
                }
                // Alignment per kolom
                if (col == 0 || col == 2 || col == 3 || col == 4) {
                    setHorizontalAlignment(JLabel.CENTER);
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                    setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PANEL KANAN – Status Pengiriman + Ringkasan Transaksi
    //  (Alur logika tidak berubah; tombol Lacak Pesanan dihapus)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_BG_LIGHT);
        panel.setPreferredSize(new Dimension(400, 500));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // ── A. Judul ──────────────────────────────────────────────────────────
        JLabel sectionTitle = new JLabel("Info Pengiriman");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 20));
        sectionTitle.setForeground(COLOR_RED_BRAND);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sectionTitle);
        panel.add(Box.createVerticalStrut(18));

        // ── B. Badge Status ───────────────────────────────────────────────────
        statusBadge = new JLabel("● SEDANG DIKIRIM");
        statusBadge.setFont(new Font("Arial", Font.BOLD, 13));
        statusBadge.setOpaque(true);
        statusBadge.setBackground(COLOR_YELLOW_BG);
        statusBadge.setForeground(COLOR_YELLOW_ON);
        statusBadge.setBorder(new EmptyBorder(6, 14, 6, 14));
        statusBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statusBadge);
        panel.add(Box.createVerticalStrut(20));

        // ── C. Ikon + Teks Status Utama ───────────────────────────────────────
        JPanel iconRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        iconRow.setOpaque(false);
        iconRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusIconLabel = new JLabel("🚚");
        statusIconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));

        JPanel statusTextBlock = new JPanel();
        statusTextBlock.setLayout(new BoxLayout(statusTextBlock, BoxLayout.Y_AXIS));
        statusTextBlock.setOpaque(false);

        statusTextLabel = new JLabel("Pesanan Sedang Dalam Perjalanan");
        statusTextLabel.setFont(new Font("Arial", Font.BOLD, 15));

        etaLabel = new JLabel("Estimasi tiba: " + secondsLeft + " detik lagi...");
        etaLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        etaLabel.setForeground(COLOR_TEXT_GRAY);

        statusTextBlock.add(statusTextLabel);
        statusTextBlock.add(Box.createVerticalStrut(4));
        statusTextBlock.add(etaLabel);

        iconRow.add(statusIconLabel);
        iconRow.add(statusTextBlock);
        panel.add(iconRow);
        panel.add(Box.createVerticalStrut(25));

        // ── D. Garis Pemisah ──────────────────────────────────────────────────
        JSeparator sep1 = new JSeparator();
        sep1.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep1);
        panel.add(Box.createVerticalStrut(20));

        // ── E. Ringkasan Pembayaran ───────────────────────────────────────────
        JLabel ringkasanTitle = new JLabel("RINGKASAN PEMBAYARAN");
        ringkasanTitle.setFont(new Font("Arial", Font.BOLD, 12));
        ringkasanTitle.setForeground(COLOR_TEXT_GRAY);
        ringkasanTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(ringkasanTitle);
        panel.add(Box.createVerticalStrut(12));

        JPanel grid = new JPanel(new GridLayout(4, 2, 10, 12));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        addGridRow(grid, "ID Customer :",    customerId);
        addGridRow(grid, "Total Belanja :",  formatRupiah((long) grandTotal));
        addGridRow(grid, "Uang Dibayar :",   formatRupiah((long) uangDibayar));
        addGridRow(grid, "Kembalian :",      formatRupiah((long) kembalian));

        panel.add(grid);
        panel.add(Box.createVerticalStrut(20));

        JSeparator sep2 = new JSeparator();
        sep2.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep2);
        panel.add(Box.createVerticalStrut(20));

        // ── F. Total Besar ────────────────────────────────────────────────────
        JLabel totalSmall = new JLabel("TOTAL DIBAYARKAN");
        totalSmall.setFont(new Font("Arial", Font.BOLD, 12));
        totalSmall.setForeground(COLOR_TEXT_GRAY);
        totalSmall.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(totalSmall);

        JLabel totalBig = new JLabel(formatRupiah((long) grandTotal));
        totalBig.setFont(new Font("Arial", Font.BOLD, 32));
        totalBig.setForeground(COLOR_RED_BRAND);
        totalBig.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(totalBig);

        // ── Tombol Lacak Pesanan DIHAPUS sesuai permintaan ───────────────────
        // (tidak ada panel.add(trackBtn) di sini)

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LOAD DATA BARANG – dari TransaksiDetail (via CartDataAccess)
    //
    //  Setelah processPaymentTransaction() dijalankan, snapshot CartItem
    //  sudah tersimpan di tabel TransaksiDetail. Method ini membacanya
    //  dan menampilkan setiap produk sebagai baris terpisah di tabel.
    // ─────────────────────────────────────────────────────────────────────────
    private void loadOrderItems() {
        tableModel.setRowCount(0);

        // Ambil detail item transaksi terakhir customer dari TransaksiDetail
        List<Object[]> rows = CartDataAccess.getLastTransaksiDetail(customerId);

        if (rows.isEmpty()) {
            // Fallback: jika TransaksiDetail masih kosong (misal DB belum di-update),
            // tampilkan satu baris ringkasan dari parameter constructor.
            tableModel.addRow(new Object[]{
                "-",
                "Pesanan Customer " + customerId,
                "-",
                "-",
                formatRupiah((long) grandTotal)
            });
        } else {
            // Tampilkan setiap produk sebagai baris terpisah
            for (Object[] row : rows) {
                tableModel.addRow(row);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  COUNTDOWN TIMER – 15 detik → update status
    // ─────────────────────────────────────────────────────────────────────────
    private void startCountdown() {
        countdownTimer = new Timer(1000, e -> {
            secondsLeft--;
            if (secondsLeft > 0) {
                etaLabel.setText("Estimasi tiba: " + secondsLeft + " detik lagi...");
            } else {
                stopCountdown();
                updateStatusArrived();
            }
        });
        countdownTimer.start();
    }

    private void stopCountdown() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
    }

    /**
     * Mengubah semua elemen UI status menjadi "Pesanan Tiba Di Tujuan".
     * Dipanggil di Event Dispatch Thread (aman karena Timer Swing).
     */
    private void updateStatusArrived() {
        // Badge
        statusBadge.setText("✓ PESANAN TIBA DI TUJUAN");
        statusBadge.setBackground(COLOR_GREEN_BG);
        statusBadge.setForeground(COLOR_GREEN_ON);

        // Ikon & teks
        statusIconLabel.setText("📦");
        statusTextLabel.setText("Pesanan Telah Tiba di Tujuan!");
        statusTextLabel.setForeground(COLOR_GREEN_ON);

        etaLabel.setText("Terima kasih telah berbelanja di JEKI Store ✨");
        etaLabel.setForeground(COLOR_GREEN_ON);

        // Repaint
        statusBadge.revalidate();    statusBadge.repaint();
        statusIconLabel.revalidate(); statusIconLabel.repaint();
        statusTextLabel.revalidate(); statusTextLabel.repaint();
        etaLabel.revalidate();        etaLabel.repaint();

        // Notifikasi popup
        JOptionPane.showMessageDialog(this,
            "🎉 Pesanan Anda telah tiba di tujuan!\nTerima kasih telah berbelanja di JEKI Store.",
            "Pesanan Tiba",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER – Baris label + nilai untuk grid ringkasan
    // ─────────────────────────────────────────────────────────────────────────
    private void addGridRow(JPanel grid, String labelText, String valueText) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl.setForeground(COLOR_TEXT_GRAY);

        JLabel val = new JLabel(valueText);
        val.setFont(new Font("Arial", Font.BOLD, 14));
        val.setHorizontalAlignment(SwingConstants.RIGHT);

        grid.add(lbl);
        grid.add(val);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER – Format Rupiah
    // ─────────────────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPER – Tutup resource JDBC
    // ─────────────────────────────────────────────────────────────────────────
    private static void closeQuietly(AutoCloseable r) {
        if (r != null) try { r.close(); } catch (Exception ignored) {}
    }
}