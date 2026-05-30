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
 * PaymentView.java
 * Halaman Pembayaran modern yang menampilkan detail invoice, total belanja,
 * input jumlah pembayaran, dan eksekusi transaksi database menggunakan SQL Transaction.
 *
 * Alur setelah pembayaran sukses:
 *  1. Popup "Bukti Pembayaran Sukses" muncul seperti biasa.
 *  2. User tutup popup → tombol "Bayar Sekarang" berubah jadi "Lihat Status Pengiriman".
 *  3. User klik tombol → ShipmentView terbuka, PaymentView ditutup.
 */
public class PaymentView extends JFrame {

    private final String customerId;
    private double grandTotal  = 0;
    private double finalUang   = 0;   // disimpan setelah bayar berhasil
    private double finalKembal = 0;   // disimpan setelah bayar berhasil

    private JLabel totalLabel;
    private JLabel customerIdLabel;
    private JLabel totalItemLabel;
    private JTextField cashInputField;
    private JButton payButton;
    private JButton backButton;
    private JTable itemTable;
    private DefaultTableModel tableModel;

    // Flag: true setelah transaksi DB berhasil dan popup ditutup
    private boolean sudahBayar = false;

    private static final Color COLOR_RED_BRAND    = new Color(200, 30, 40);
    private static final Color COLOR_RED_HOVER    = new Color(160, 20, 30);
    private static final Color COLOR_GREEN_BRAND  = new Color(34, 139, 34);
    private static final Color COLOR_GREEN_HOVER  = new Color(20, 100, 20);
    private static final Color COLOR_TEXT_GRAY    = new Color(100, 100, 100);
    private static final Color COLOR_BG_LIGHT     = new Color(248, 249, 250);
    private static final Color COLOR_INPUT_BG     = new Color(240, 240, 240);
    private static final Color COLOR_INPUT_BORDER = new Color(220, 220, 220);

    public PaymentView(String customerId) {
        this.customerId = customerId;

        setTitle("Pembayaran - Style Connect");
        setSize(1050, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // ================= 1. HEADER PANEL =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Checkout & Pembayaran");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.BLACK);

        backButton = new JButton("Kembali ke Keranjang");
        backButton.setFont(new Font("Arial", Font.BOLD, 13));
        backButton.setBackground(Color.WHITE);
        backButton.setForeground(COLOR_TEXT_GRAY);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        backButton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { backButton.setBackground(new Color(245, 245, 245)); }
            @Override public void mouseExited(MouseEvent e)  { backButton.setBackground(Color.WHITE); }
        });
        backButton.addActionListener(e -> {
            new CartView(customerId).setVisible(true);
            dispose();
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ================= 2. LEFT PANEL: DAFTAR BARANG =================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            "Detail Barang Belanjaan",
            0, 0,
            new Font("Arial", Font.BOLD, 15),
            COLOR_RED_BRAND
        ));

        String[] columns = {"ID Produk", "Nama Produk", "Harga Satuan", "Qty", "Total Harga"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
        };

        itemTable = new JTable(tableModel);
        itemTable.setFont(new Font("Arial", Font.PLAIN, 13));
        itemTable.setRowHeight(38);
        itemTable.setSelectionBackground(new Color(250, 242, 242));
        itemTable.setSelectionForeground(COLOR_RED_BRAND);
        itemTable.setShowGrid(false);
        itemTable.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader tableHeader = itemTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 13));
        tableHeader.setBackground(COLOR_RED_BRAND);
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setPreferredSize(new Dimension(100, 35));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        itemTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        itemTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        itemTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        itemTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(itemTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        // ================= 3. RIGHT PANEL: SUMMARY & FORM =================
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(COLOR_BG_LIGHT);
        rightPanel.setPreferredSize(new Dimension(400, 500));
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        // Title Summary
        JLabel summaryTitle = new JLabel("Ringkasan Pembayaran");
        summaryTitle.setFont(new Font("Arial", Font.BOLD, 20));
        summaryTitle.setForeground(COLOR_RED_BRAND);
        summaryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(summaryTitle);
        rightPanel.add(Box.createVerticalStrut(20));

        // Info (ID Customer, Total Item)
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 15));
        infoPanel.setOpaque(false);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel custLabel = new JLabel("ID Customer:");
        custLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        custLabel.setForeground(COLOR_TEXT_GRAY);

        customerIdLabel = new JLabel(customerId);
        customerIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        customerIdLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel itemsCountLabel = new JLabel("Total Barang:");
        itemsCountLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        itemsCountLabel.setForeground(COLOR_TEXT_GRAY);

        totalItemLabel = new JLabel("0 Pcs");
        totalItemLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalItemLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        infoPanel.add(custLabel);
        infoPanel.add(customerIdLabel);
        infoPanel.add(itemsCountLabel);
        infoPanel.add(totalItemLabel);

        rightPanel.add(infoPanel);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(new JSeparator());
        rightPanel.add(Box.createVerticalStrut(20));

        // Total Belanja
        JLabel subtotalLabelText = new JLabel("TOTAL BELANJA");
        subtotalLabelText.setFont(new Font("Arial", Font.BOLD, 12));
        subtotalLabelText.setForeground(COLOR_TEXT_GRAY);
        subtotalLabelText.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(subtotalLabelText);

        totalLabel = new JLabel("Rp 0");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 32));
        totalLabel.setForeground(COLOR_RED_BRAND);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(totalLabel);
        rightPanel.add(Box.createVerticalStrut(30));

        // Input Nominal
        JLabel cashLabelText = new JLabel("MASUKKAN NOMINAL UANG BAYAR (RP)");
        cashLabelText.setFont(new Font("Arial", Font.BOLD, 12));
        cashLabelText.setForeground(Color.BLACK);
        cashLabelText.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(cashLabelText);
        rightPanel.add(Box.createVerticalStrut(8));

        cashInputField = new JTextField();
        cashInputField.setFont(new Font("Arial", Font.BOLD, 20));
        cashInputField.setBackground(Color.WHITE);
        cashInputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        cashInputField.setAlignmentX(Component.LEFT_ALIGNMENT);
        cashInputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_INPUT_BORDER, 2),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        rightPanel.add(cashInputField);
        rightPanel.add(Box.createVerticalStrut(35));

        // ================= TOMBOL UTAMA =================
        // Tombol ini punya DUA fungsi tergantung flag sudahBayar:
        //   false → "Bayar Sekarang"   (merah)
        //   true  → "Lihat Status Pengiriman" (hijau)
        payButton = new JButton("Bayar Sekarang");
        payButton.setFont(new Font("Arial", Font.BOLD, 16));
        payButton.setForeground(Color.WHITE);
        payButton.setBackground(COLOR_RED_BRAND);
        payButton.setFocusPainted(false);
        payButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        payButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        payButton.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        payButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Warna hover menyesuaikan state tombol saat itu
                payButton.setBackground(sudahBayar ? COLOR_GREEN_HOVER : COLOR_RED_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                payButton.setBackground(sudahBayar ? COLOR_GREEN_BRAND : COLOR_RED_BRAND);
            }
        });

        payButton.addActionListener(e -> {
            if (sudahBayar) {
                // State SETELAH bayar: buka ShipmentView
                new ShipmentView(customerId, grandTotal, finalUang, finalKembal).setVisible(true);
                dispose();
            } else {
                // State SEBELUM bayar: proses pembayaran
                handlePayment();
            }
        });

        rightPanel.add(payButton);
        rightPanel.add(Box.createVerticalGlue());

        // Split Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(600);
        splitPane.setDividerSize(5);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel);

        loadInvoiceData();
    }

    // ================= LOAD DATA INVOICE =================
    private void loadInvoiceData() {
        tableModel.setRowCount(0);
        grandTotal = 0;
        int totalQty = 0;

        List<CartItem> cartItems = CartDataAccess.getCartItems(customerId);

        for (CartItem item : cartItems) {
            Product p = item.getProduct();
            double subtotal = item.getSubtotal();
            grandTotal += subtotal;
            totalQty += item.getQuantity();

            tableModel.addRow(new Object[]{
                p.getCategory(),
                p.getName(),
                p.getPrice(),
                item.getQuantity(),
                formatRupiah((long) subtotal)
            });
        }

        totalItemLabel.setText(totalQty + " Pcs");
        totalLabel.setText(formatRupiah((long) grandTotal));
    }

    // ================= PROSES PEMBAYARAN =================
    private void handlePayment() {
        String cashInputText = cashInputField.getText().trim();

        // Validasi: tidak boleh kosong
        if (cashInputText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Masukkan jumlah uang tunai pembayaran terlebih dahulu!",
                "Peringatan",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validasi: harus angka
        double uangDibayar = 0;
        try {
            String cleanInput = cashInputText.replace(".", "").replace(",", "");
            uangDibayar = Double.parseDouble(cleanInput);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Format nominal uang pembayaran tidak valid!\nMasukkan berupa angka saja (contoh: 500000).",
                "Error Validasi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validasi: harus lebih dari 0
        if (uangDibayar <= 0) {
            JOptionPane.showMessageDialog(this,
                "Jumlah uang pembayaran harus lebih besar dari 0!",
                "Error Validasi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validasi: uang harus cukup
        if (uangDibayar < grandTotal) {
            double kurang = grandTotal - uangDibayar;
            JOptionPane.showMessageDialog(this,
                "Uang yang dibayarkan kurang dari total belanja!\nKekurangan: " + formatRupiah((long) kurang),
                "Pembayaran Gagal",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        double kembalian = uangDibayar - grandTotal;

        // Jalankan SQL Transaction secara atomic via DAO
        boolean isSuccess = CartDataAccess.processPaymentTransaction(
                customerId, grandTotal, uangDibayar, kembalian);

        if (isSuccess) {

            // Simpan nilai agar bisa dikirim ke ShipmentView nantinya
            finalUang   = uangDibayar;
            finalKembal = kembalian;

            // ── Popup bukti pembayaran (tetap seperti aslinya) ──────────────
            String message = "BUKTI PEMBAYARAN SUKSES\n"
                    + "-----------------------------\n"
                    + "Customer ID  : " + customerId + "\n"
                    + "Total Belanja: " + formatRupiah((long) grandTotal) + "\n"
                    + "Uang Dibayar : " + formatRupiah((long) uangDibayar) + "\n"
                    + "Kembalian    : " + formatRupiah((long) kembalian) + "\n"
                    + "-----------------------------\n"
                    + "Terima kasih telah berbelanja di JEKI!";

            JOptionPane.showMessageDialog(this,
                message,
                "Transaksi Berhasil",
                JOptionPane.INFORMATION_MESSAGE);

            // ── Setelah popup ditutup: ubah state & tampilan ─────────────────
            sudahBayar = true;

            // Nonaktifkan input uang & tombol kembali agar tidak bisa bayar ulang
            cashInputField.setEnabled(false);
            cashInputField.setBackground(new Color(240, 240, 240));
            cashInputField.setText("Pembayaran telah selesai");
            backButton.setEnabled(false);

            // Ubah tombol merah → tombol hijau "Lihat Status Pengiriman"
            payButton.setText("Lihat Status Pengiriman  \u2192");
            payButton.setBackground(COLOR_GREEN_BRAND);
            payButton.setToolTipText("Klik untuk melihat status pengiriman pesanan Anda");
            payButton.repaint();

        } else {
            // Transaksi DB gagal
            JOptionPane.showMessageDialog(this,
                "Terjadi kesalahan sistem saat memproses transaksi database.\nPembayaran di-rollback.",
                "System Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= FORMAT RUPIAH =================
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