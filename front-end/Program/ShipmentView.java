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


public class ShipmentView extends JFrame {

    
    private static final Color COLOR_RED_BRAND    = new Color(200, 30, 40);
    private static final Color COLOR_RED_HOVER    = new Color(160, 20, 30);
    private static final Color COLOR_TEXT_GRAY    = new Color(100, 100, 100);
    private static final Color COLOR_BG_LIGHT     = new Color(248, 249, 250);
    private static final Color COLOR_INPUT_BORDER = new Color(220, 220, 220);
    private static final Color COLOR_GREEN_ON     = new Color(34, 139, 34);
    private static final Color COLOR_GREEN_BG     = new Color(210, 245, 220);
    private static final Color COLOR_YELLOW_ON    = new Color(180, 120, 0);
    private static final Color COLOR_YELLOW_BG    = new Color(255, 243, 205);

    
    private final String customerId;
    private final double grandTotal;
    private final double uangDibayar;
    private final double kembalian;
    private final int    idTransaction;

    
    private JLabel           statusBadge;
    private JLabel           statusIconLabel;
    private JLabel           statusTextLabel;
    private JLabel           etaLabel;
    private DefaultTableModel tableModel;

    
    private Timer countdownTimer;
    private int   secondsLeft = 15;
    private boolean statusUpdatedToDelivery = false;

    
    
    
    public ShipmentView(String customerId, double grandTotal,
                        double uangDibayar, double kembalian) {
        this(customerId, grandTotal, uangDibayar, kembalian, CartDataAccess.getLastTransactionId(customerId));
    }

    public ShipmentView(String customerId, double grandTotal,
                        double uangDibayar, double kembalian, int idTransaction) {
        this.customerId  = customerId;
        this.grandTotal  = grandTotal;
        this.uangDibayar = uangDibayar;
        this.kembalian   = kembalian;
        this.idTransaction = idTransaction;

        
        Object[] shipmentInfo = CartDataAccess.getShipmentInfo(idTransaction);
        if (shipmentInfo != null && "pesanan tiba di tujuan".equalsIgnoreCase((String) shipmentInfo[3])) {
            secondsLeft = 0;
        } else if (shipmentInfo != null && "sedang dalam pengantaran".equalsIgnoreCase((String) shipmentInfo[3])) {
            secondsLeft = 14;
            statusUpdatedToDelivery = true;
        } else {
            secondsLeft = 15;
        }

        setTitle("Status Pengiriman - JEKI Store");
        setSize(1050, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        
        mainPanel.add(buildHeader(), BorderLayout.NORTH);

        
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

        
        loadOrderItems();

        
        startCountdown();
    }

    
    
    
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

        
        table.getColumnModel().getColumn(0).setPreferredWidth(80);  
        table.getColumnModel().getColumn(1).setPreferredWidth(220); 
        table.getColumnModel().getColumn(2).setPreferredWidth(110); 
        table.getColumnModel().getColumn(3).setPreferredWidth(50);  
        table.getColumnModel().getColumn(4).setPreferredWidth(110); 

        
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 13));
        tableHeader.setBackground(COLOR_RED_BRAND);
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setPreferredSize(new Dimension(100, 38));

        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); 
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); 
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); 
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); 

        
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);

        
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 248, 248));
                }
                
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

    
    
    
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_BG_LIGHT);
        panel.setPreferredSize(new Dimension(300, 400));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        
        Object[] shipmentInfo = CartDataAccess.getShipmentInfo(idTransaction);
        String idShipment = "-";
        String alamat = "-";
        String courier = "-";
        String resi = "-";
        double ongkir = 0;
        String statusKirim = "sedang dikirim";

        if (shipmentInfo != null) {
            idShipment = (String) shipmentInfo[0];
            alamat = (String) shipmentInfo[1];
            statusKirim = (String) shipmentInfo[3];
            courier = (String) shipmentInfo[4];
            resi = (String) shipmentInfo[5];
            ongkir = (Double) shipmentInfo[6];
        }

        
        JLabel sectionTitle = new JLabel("Info Pengiriman");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 20));
        sectionTitle.setForeground(COLOR_RED_BRAND);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sectionTitle);
        panel.add(Box.createVerticalStrut(15));

        
        statusBadge = new JLabel();
        statusBadge.setFont(new Font("Arial", Font.BOLD, 13));
        statusBadge.setOpaque(true);
        if ("pesanan tiba di tujuan".equalsIgnoreCase(statusKirim)) {
            statusBadge.setText("PESANAN TIBA DI TUJUAN");
            statusBadge.setBackground(COLOR_GREEN_BG);
            statusBadge.setForeground(COLOR_GREEN_ON);
        } else if ("sedang dalam pengantaran".equalsIgnoreCase(statusKirim)) {
            statusBadge.setText("SEDANG DALAM PENGANTARAN");
            statusBadge.setBackground(COLOR_YELLOW_BG);
            statusBadge.setForeground(COLOR_YELLOW_ON);
        } else {
            statusBadge.setText("SEDANG DIKIRIM");
            statusBadge.setBackground(COLOR_YELLOW_BG);
            statusBadge.setForeground(COLOR_YELLOW_ON);
        }
        statusBadge.setBorder(new EmptyBorder(6, 14, 6, 14));
        statusBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statusBadge);
        panel.add(Box.createVerticalStrut(15));

        
        JPanel iconRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        iconRow.setOpaque(false);
        iconRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusIconLabel = new JLabel();
        statusIconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));

        JPanel statusTextBlock = new JPanel();
        statusTextBlock.setLayout(new BoxLayout(statusTextBlock, BoxLayout.Y_AXIS));
        statusTextBlock.setOpaque(false);

        statusTextLabel = new JLabel();
        statusTextLabel.setFont(new Font("Arial", Font.BOLD, 15));

        etaLabel = new JLabel();
        etaLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        etaLabel.setForeground(COLOR_TEXT_GRAY);

        if ("pesanan tiba di tujuan".equalsIgnoreCase(statusKirim)) {
            statusIconLabel.setText("📦");
            statusTextLabel.setText("Pesanan Tiba");
            statusTextLabel.setForeground(COLOR_GREEN_ON);
            etaLabel.setText("Terima kasih telah berbelanja di JEKI Store");
            etaLabel.setForeground(COLOR_GREEN_ON);
        } else if ("sedang dalam pengantaran".equalsIgnoreCase(statusKirim)) {
            statusIconLabel.setText("🛵");
            statusTextLabel.setText("Sedang Dalam Pengantaran");
            statusTextLabel.setForeground(COLOR_YELLOW_ON);
            etaLabel.setText("Estimasi tiba: " + secondsLeft + " detik lagi...");
        } else {
            statusIconLabel.setText("🚚");
            statusTextLabel.setText("Sedang Dikirim");
            statusTextLabel.setForeground(COLOR_RED_BRAND);
            etaLabel.setText("Estimasi tiba: " + secondsLeft + " detik lagi...");
        }

        statusTextBlock.add(statusTextLabel);
        statusTextBlock.add(Box.createVerticalStrut(4));
        statusTextBlock.add(etaLabel);

        iconRow.add(statusIconLabel);
        iconRow.add(statusTextBlock);
        panel.add(iconRow);
        panel.add(Box.createVerticalStrut(15));

        
        JSeparator sep1 = new JSeparator();
        sep1.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep1);
        panel.add(Box.createVerticalStrut(15));

        
        JLabel shipmentTitle = new JLabel("DETAIL PENGIRIMAN");
        shipmentTitle.setFont(new Font("Arial", Font.BOLD, 12));
        shipmentTitle.setForeground(COLOR_TEXT_GRAY);
        shipmentTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(shipmentTitle);
        panel.add(Box.createVerticalStrut(10));

        JPanel shipGrid = new JPanel(new GridLayout(5, 2, 5, 8));
        shipGrid.setOpaque(false);
        shipGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        addGridRow(shipGrid, "No Shipment :", idShipment);
        addGridRow(shipGrid, "Jasa Kirim :",  courier);
        addGridRow(shipGrid, "No Resi :",     resi);
        addGridRow(shipGrid, "Ongkir :",      formatRupiah((long) ongkir));
        addGridRowHtml(shipGrid, "Alamat Kirim :", alamat);
        panel.add(shipGrid);

        panel.add(Box.createVerticalStrut(15));
        JSeparator sep2 = new JSeparator();
        sep2.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep2);
        panel.add(Box.createVerticalStrut(15));

        
        JLabel ringkasanTitle = new JLabel("RINGKASAN PEMBAYARAN");
        ringkasanTitle.setFont(new Font("Arial", Font.BOLD, 12));
        ringkasanTitle.setForeground(COLOR_TEXT_GRAY);
        ringkasanTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(ringkasanTitle);
        panel.add(Box.createVerticalStrut(10));

        JPanel grid = new JPanel(new GridLayout(4, 2, 5, 8));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        addGridRow(grid, "ID Customer :",    customerId);
        addGridRow(grid, "Total Belanja :",  formatRupiah((long) grandTotal));
        addGridRow(grid, "Uang Dibayar :",   formatRupiah((long) uangDibayar));
        addGridRow(grid, "Kembalian :",      formatRupiah((long) kembalian));

        panel.add(grid);
        panel.add(Box.createVerticalStrut(15));

        JSeparator sep3 = new JSeparator();
        sep3.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sep3);
        panel.add(Box.createVerticalStrut(15));

        
        JLabel totalSmall = new JLabel("TOTAL DIBAYARKAN");
        totalSmall.setFont(new Font("Arial", Font.BOLD, 12));
        totalSmall.setForeground(COLOR_TEXT_GRAY);
        totalSmall.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(totalSmall);

        JLabel totalBig = new JLabel(formatRupiah((long) (grandTotal + ongkir)));
        totalBig.setFont(new Font("Arial", Font.BOLD, 30));
        totalBig.setForeground(COLOR_RED_BRAND);
        totalBig.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(totalBig);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    
    
    
    private void loadOrderItems() {
        tableModel.setRowCount(0);

        
        List<Object[]> rows = CartDataAccess.getLastTransaksiDetail(customerId);

        if (rows.isEmpty()) {
            
            tableModel.addRow(new Object[]{
                "-",
                "Pesanan Customer " + customerId,
                "-",
                "-",
                formatRupiah((long) grandTotal)
            });
        } else {
            for (Object[] row : rows) {
                tableModel.addRow(row);
            }
        }
    }

    
    
    
    private void startCountdown() {
        if (secondsLeft <= 0) {
            updateStatusArrivedUI();
            return;
        }

        
        if (secondsLeft < 15) {
            updateStatusDeliveryUI();
        }

        countdownTimer = new Timer(1000, e -> {
            secondsLeft--;
            if (secondsLeft > 0) {
                etaLabel.setText("Estimasi tiba: " + secondsLeft + " detik lagi...");
                
                
                if (secondsLeft < 15 && !statusUpdatedToDelivery) {
                    statusUpdatedToDelivery = true;
                    updateStatusDelivery();
                }
            } else {
                stopCountdown();
                updateStatusArrived();
            }
        });
        countdownTimer.start();
    }

    private void updateStatusDelivery() {
        
        CartDataAccess.updateShipmentAndTransactionStatus(idTransaction, "sedang dalam pengantaran");

        
        updateStatusDeliveryUI();
    }

    private void updateStatusDeliveryUI() {
        
        statusBadge.setText("SEDANG DALAM PENGANTARAN");
        statusBadge.setBackground(COLOR_YELLOW_BG);
        statusBadge.setForeground(COLOR_YELLOW_ON);

        
        statusIconLabel.setText("🛵");
        statusTextLabel.setText("Sedang Dalam Pengantaran");
        statusTextLabel.setForeground(COLOR_YELLOW_ON);

        etaLabel.setForeground(COLOR_TEXT_GRAY);

        
        statusBadge.revalidate();    statusBadge.repaint();
        statusIconLabel.revalidate(); statusIconLabel.repaint();
        statusTextLabel.revalidate(); statusTextLabel.repaint();
        etaLabel.revalidate();        etaLabel.repaint();
    }

    private void stopCountdown() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
    }

    private void updateStatusArrived() {
        
        CartDataAccess.updateShipmentAndTransactionStatus(idTransaction, "pesanan tiba di tujuan");

        
        updateStatusArrivedUI();

        
        JOptionPane.showMessageDialog(this,
            "Pesanan Anda telah tiba di tujuan!\nTerima kasih telah berbelanja di JEKI Store.",
            "Pesanan Tiba",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void updateStatusArrivedUI() {
        
        statusBadge.setText("PESANAN TIBA DI TUJUAN");
        statusBadge.setBackground(COLOR_GREEN_BG);
        statusBadge.setForeground(COLOR_GREEN_ON);

        
        statusIconLabel.setText("📦");
        statusTextLabel.setText("Pesanan Tiba");
        statusTextLabel.setForeground(COLOR_GREEN_ON);

        etaLabel.setText("Terima kasih telah berbelanja di JEKI Store");
        etaLabel.setForeground(COLOR_GREEN_ON);

        
        statusBadge.revalidate();    statusBadge.repaint();
        statusIconLabel.revalidate(); statusIconLabel.repaint();
        statusTextLabel.revalidate(); statusTextLabel.repaint();
        etaLabel.revalidate();        etaLabel.repaint();
    }

    
    
    
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

    private void addGridRowHtml(JPanel grid, String labelText, String valueText) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Arial", Font.PLAIN, 14));
        lbl.setForeground(COLOR_TEXT_GRAY);

        JLabel val = new JLabel("<html><div style='text-align: right; width: 180px;'>" + valueText + "</div></html>");
        val.setFont(new Font("Arial", Font.BOLD, 13));
        val.setHorizontalAlignment(SwingConstants.RIGHT);

        grid.add(lbl);
        grid.add(val);
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
