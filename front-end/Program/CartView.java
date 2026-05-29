package Program;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * CartView.java
 * Halaman keranjang belanja JEKI Store.
 *
 * Layout sesuai desain:
 *   ┌─────────────────────────────────────────────┐
 *   │  NAVBAR  (logo · search · cart · profile)   │
 *   ├─────────────────────────────────────────────┤
 *   │  KERANJANG BELANJA  (judul + tombol kembali) │
 *   ├─────────────────────┬───────────────────────┤
 *   │  DAFTAR ITEM        │  ORDER SUMMARY         │
 *   │  (scroll)           │  promo · total · pay  │
 *   └─────────────────────┴───────────────────────┘
 */
public class CartView extends JFrame {

    // ── Brand colors (konsisten dengan file lain) ──────────────────────────
    private static final Color COLOR_RED_BRAND  = new Color(200, 30, 40);
    private static final Color COLOR_RED_HOVER  = new Color(160, 20, 30);
    private static final Color COLOR_BG         = new Color(248, 248, 248);
    private static final Color COLOR_WHITE      = Color.WHITE;
    private static final Color COLOR_BORDER     = new Color(220, 220, 220);
    private static final Color COLOR_TEXT_GRAY  = new Color(130, 130, 130);
    private static final Color COLOR_TEXT_DARK  = new Color(30,  30,  30);

    // ── DB ─────────────────────────────────────────────────────────────────
    private Connection conn;

    // ── State ──────────────────────────────────────────────────────────────
    private final String customerId;
    private       String activePromoCode = "";
    private       long   discountAmount  = 0L;

    // ── Panel referensi (untuk refresh) ───────────────────────────────────
    private JPanel     itemListPanel;   // panel daftar item (kiri)
    private JLabel     subtotalValueLabel;
    private JLabel     totalItemsLabel;
    private JLabel     totalPayLabel;
    private JLabel     discountLabel;
    private JLabel     discountValueLabel;
    private JTextField promoField;

    // ── Constructor ────────────────────────────────────────────────────────
    public CartView() {
        this(CartManager.getInstance().getCustomerId());
    }

    public CartView(String customerId) {
        this.customerId = customerId;
        CartManager.getInstance().setCustomerId(customerId);

        getConnection();

        setTitle("Keranjang Belanja – JEKI Store");
        setSize(1000, 700);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_BG);

        root.add(buildNavbar(),  BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);

        add(root);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  NAVBAR  (sama persis dengan HomePageView)
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildNavbar() {
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(COLOR_WHITE);
        navbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // ── Logo ─────────────────────────────────────────────────────────
        ImageIcon logoIcon = new ImageIcon("images/logo-jeki.png");
        Image logoImg = logoIcon.getImage().getScaledInstance(90, 40, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(logoImg));

        // ── Search ───────────────────────────────────────────────────────
        JTextField searchField = new JTextField("Cari produk atau kategori...");
        searchField.setPreferredSize(new Dimension(500, 40));
        searchField.setForeground(COLOR_TEXT_GRAY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)
        ));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(COLOR_WHITE);
        centerPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        centerPanel.add(searchField, BorderLayout.CENTER);

        // ── Tombol kanan ─────────────────────────────────────────────────
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(COLOR_WHITE);

        Dimension btnSize = new Dimension(40, 40);

        // Cart button (dengan badge)
        JPanel cartWrapper = new JPanel(null);
        cartWrapper.setBackground(COLOR_WHITE);
        cartWrapper.setPreferredSize(new Dimension(46, 40));

        JButton cartButton = makeIconButton("images/cart.png", btnSize);
        cartButton.setBounds(0, 0, 40, 40);
        cartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cartButton.addActionListener(e -> {
            // sudah di halaman cart, tidak perlu buka lagi
        });
        cartWrapper.add(cartButton);

        int totalItems = CartManager.getInstance().getTotalItems();
        if (totalItems > 0) {
            JLabel badge = new JLabel(String.valueOf(totalItems));
            badge.setBounds(26, -2, 20, 16);
            badge.setOpaque(true);
            badge.setBackground(COLOR_RED_BRAND);
            badge.setForeground(COLOR_WHITE);
            badge.setFont(new Font("Arial", Font.BOLD, 10));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setBorder(BorderFactory.createLineBorder(COLOR_WHITE, 1));
            cartWrapper.add(badge);
        }

        // Profile button
        JButton profileButton = makeIconButton("images/profile.png", btnSize);
        profileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileButton.addActionListener(e -> new ProfileView(customerId).setVisible(true));

        // Menu / hamburger
        JButton menuButton = makeIconButton("images/menu.png", btnSize);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        rightPanel.add(cartWrapper);
        rightPanel.add(profileButton);

        navbar.add(logo,        BorderLayout.WEST);
        navbar.add(centerPanel, BorderLayout.CENTER);
        navbar.add(rightPanel,  BorderLayout.EAST);

        return navbar;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CONTENT  (judul + dua kolom)
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(COLOR_BG);
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ── Judul + tombol kembali ────────────────────────────────────────
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(COLOR_BG);
        headerRow.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("KERANJANG BELANJA");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(COLOR_TEXT_DARK);

        JButton backBtn = new JButton("← Lanjut Belanja");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 13));
        backBtn.setForeground(COLOR_TEXT_GRAY);
        backBtn.setBackground(COLOR_WHITE);
        backBtn.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(hoverEffect(backBtn, COLOR_WHITE, new Color(240, 240, 240)));
        backBtn.addActionListener(e -> dispose());

        headerRow.add(title,  BorderLayout.WEST);
        headerRow.add(backBtn, BorderLayout.EAST);

        content.add(headerRow, BorderLayout.NORTH);

        // ── Dua kolom: item list (kiri) + order summary (kanan) ───────────
        JPanel twoCol = new JPanel(new BorderLayout(20, 0));
        twoCol.setBackground(COLOR_BG);

        twoCol.add(buildItemListPanel(),    BorderLayout.CENTER);
        twoCol.add(buildOrderSummaryPanel(), BorderLayout.EAST);

        content.add(twoCol, BorderLayout.CENTER);
        return content;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PANEL KIRI – Daftar item
    // ══════════════════════════════════════════════════════════════════════
    private JScrollPane buildItemListPanel() {
        itemListPanel = new JPanel();
        itemListPanel.setLayout(new BoxLayout(itemListPanel, BoxLayout.Y_AXIS));
        itemListPanel.setBackground(COLOR_BG);

        refreshItemList();

        JScrollPane scroll = new JScrollPane(itemListPanel);
        scroll.setBorder(null);
        scroll.setBackground(COLOR_BG);
        scroll.getViewport().setBackground(COLOR_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** Rebuild isi daftar item dari CartManager */
    private void refreshItemList() {
        itemListPanel.removeAll();

        List<CartItem> items = CartManager.getInstance().getItems();

        if (items.isEmpty()) {
            JLabel empty = new JLabel("Keranjang kamu masih kosong.");
            empty.setFont(new Font("Arial", Font.ITALIC, 15));
            empty.setForeground(COLOR_TEXT_GRAY);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setBorder(new EmptyBorder(20, 0, 0, 0));
            itemListPanel.add(empty);
        } else {
            for (CartItem ci : items) {
                itemListPanel.add(buildItemRow(ci));
                itemListPanel.add(Box.createVerticalStrut(12));
            }
        }

        itemListPanel.revalidate();
        itemListPanel.repaint();
        refreshSummary();
    }

    /** Satu baris item (kartu putih) */
    private JPanel buildItemRow(CartItem ci) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(COLOR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                new EmptyBorder(14, 14, 14, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Foto produk ───────────────────────────────────────────────────
        JLabel imgLabel = buildProductImage(ci.getProduct().getImagePath(), 80, 80);
        card.add(imgLabel, BorderLayout.WEST);

        // ── Info produk ───────────────────────────────────────────────────
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(COLOR_WHITE);

        // Nama produk
        JLabel nameLabel = new JLabel(ci.getProduct().getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        nameLabel.setForeground(COLOR_TEXT_DARK);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Kategori
        JLabel catLabel = new JLabel("Kategori: " + ci.getProduct().getCategory());
        catLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        catLabel.setForeground(COLOR_TEXT_GRAY);
        catLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Size
        JLabel sizeLabel = new JLabel("Size: " + ci.getSize());
        sizeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        sizeLabel.setForeground(COLOR_TEXT_GRAY);
        sizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Qty control  [−] qty [+]
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        qtyPanel.setBackground(COLOR_WHITE);
        qtyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton minusBtn = buildQtyButton("−");
        JLabel  qtyLabel = new JLabel(String.valueOf(ci.getQuantity()));
        qtyLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        qtyLabel.setPreferredSize(new Dimension(34, 28));
        qtyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        qtyLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, COLOR_BORDER));
        JButton plusBtn  = buildQtyButton("+");

        minusBtn.addActionListener(e -> {
            if (ci.getQuantity() > 1) {
                ci.setQuantity(ci.getQuantity() - 1);
                qtyLabel.setText(String.valueOf(ci.getQuantity()));
                refreshSummary();
            }
        });
        plusBtn.addActionListener(e -> {
            ci.setQuantity(ci.getQuantity() + 1);
            qtyLabel.setText(String.valueOf(ci.getQuantity()));
            refreshSummary();
        });

        qtyPanel.add(minusBtn);
        qtyPanel.add(qtyLabel);
        qtyPanel.add(plusBtn);

        info.add(nameLabel);
        info.add(Box.createVerticalStrut(3));
        info.add(catLabel);
        info.add(Box.createVerticalStrut(2));
        info.add(sizeLabel);
        info.add(Box.createVerticalStrut(8));
        info.add(qtyPanel);

        card.add(info, BorderLayout.CENTER);

        // ── Harga + tombol hapus (kanan) ──────────────────────────────────
        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setBackground(COLOR_WHITE);

        JLabel priceLabel = new JLabel(ci.getProduct().getPrice());
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(COLOR_RED_BRAND);
        priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton deleteBtn = new JButton("×");
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 18));
        deleteBtn.setForeground(COLOR_TEXT_GRAY);
        deleteBtn.setBackground(COLOR_WHITE);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { deleteBtn.setForeground(COLOR_RED_BRAND); }
            @Override public void mouseExited(MouseEvent e)  { deleteBtn.setForeground(COLOR_TEXT_GRAY); }
        });
        deleteBtn.addActionListener(e -> {
            CartManager.getInstance().removeItem(ci);
            refreshItemList();
        });

        rightSide.add(priceLabel, BorderLayout.NORTH);
        rightSide.add(deleteBtn,  BorderLayout.SOUTH);

        card.add(rightSide, BorderLayout.EAST);

        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PANEL KANAN – Order Summary
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildOrderSummaryPanel() {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(COLOR_BG);
        wrap.setPreferredSize(new Dimension(340, 0));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(COLOR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(340, Integer.MAX_VALUE));

        // ── Judul ─────────────────────────────────────────────────────────
        JLabel summaryTitle = new JLabel("ORDER SUMMARY");
        summaryTitle.setFont(new Font("Arial", Font.BOLD, 14));
        summaryTitle.setForeground(COLOR_TEXT_DARK);
        summaryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(summaryTitle);
        card.add(Box.createVerticalStrut(16));
        card.add(makeDivider());
        card.add(Box.createVerticalStrut(16));

        // ── Promo Code ────────────────────────────────────────────────────
        JPanel promoRow = new JPanel(new BorderLayout(8, 0));
        promoRow.setBackground(COLOR_WHITE);
        promoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        promoRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        promoField = new JTextField();
        promoField.setFont(new Font("Arial", Font.PLAIN, 13));
        promoField.setForeground(COLOR_TEXT_GRAY);
        promoField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                new EmptyBorder(6, 10, 6, 10)
        ));
        promoField.setToolTipText("Promo Code (UNIQLO10)");

        JButton applyBtn = new JButton("Apply");
        applyBtn.setFont(new Font("Arial", Font.BOLD, 12));
        applyBtn.setBackground(COLOR_WHITE);
        applyBtn.setForeground(COLOR_TEXT_DARK);
        applyBtn.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        applyBtn.setFocusPainted(false);
        applyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyBtn.setPreferredSize(new Dimension(70, 36));
        applyBtn.addMouseListener(hoverEffect(applyBtn, COLOR_WHITE, new Color(240, 240, 240)));
        applyBtn.addActionListener(e -> applyPromo());

        promoRow.add(promoField, BorderLayout.CENTER);
        promoRow.add(applyBtn,   BorderLayout.EAST);

        card.add(promoRow);
        card.add(Box.createVerticalStrut(16));
        card.add(makeDivider());
        card.add(Box.createVerticalStrut(14));

        // ── Total Items ───────────────────────────────────────────────────
        totalItemsLabel = new JLabel();
        card.add(makeSummaryRow("Total Items", totalItemsLabel, false));
        card.add(Box.createVerticalStrut(8));

        // ── Subtotal ──────────────────────────────────────────────────────
        subtotalValueLabel = new JLabel();
        card.add(makeSummaryRow("Subtotal", subtotalValueLabel, false));
        card.add(Box.createVerticalStrut(8));

        // ── Diskon (disembunyikan sampai promo aktif) ─────────────────────
        discountLabel      = new JLabel("Diskon");
        discountLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        discountLabel.setForeground(new Color(40, 140, 60));
        discountValueLabel = new JLabel();
        discountValueLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        discountValueLabel.setForeground(new Color(40, 140, 60));
        JPanel discountRow = makeSummaryRowCustom(discountLabel, discountValueLabel);
        discountRow.setVisible(false);
        card.add(discountRow);
        card.add(Box.createVerticalStrut(8));

        // Simpan referensi discount row untuk toggle visibility
        // (gunakan nama field agar bisa diakses di refreshSummary)
        // Wrap dalam panel supaya mudah toggle
        final JPanel discountRowRef = discountRow;

        card.add(makeDivider());
        card.add(Box.createVerticalStrut(14));

        // ── Total Payment ─────────────────────────────────────────────────
        totalPayLabel = new JLabel();
        totalPayLabel.setFont(new Font("Arial", Font.BOLD, 17));
        totalPayLabel.setForeground(COLOR_RED_BRAND);

        JLabel totalPayTitle = new JLabel("Total Payment");
        totalPayTitle.setFont(new Font("Arial", Font.BOLD, 14));
        totalPayTitle.setForeground(COLOR_TEXT_DARK);

        JPanel totalPayRow = new JPanel(new BorderLayout());
        totalPayRow.setBackground(COLOR_WHITE);
        totalPayRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        totalPayRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalPayRow.add(totalPayTitle,  BorderLayout.WEST);
        totalPayRow.add(totalPayLabel,  BorderLayout.EAST);

        card.add(totalPayRow);
        card.add(Box.createVerticalStrut(20));

        // ── Payment Method ────────────────────────────────────────────────
        JLabel pmLabel = new JLabel("PAYMENT METHOD");
        pmLabel.setFont(new Font("Arial", Font.BOLD, 11));
        pmLabel.setForeground(COLOR_TEXT_GRAY);
        pmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(pmLabel);
        card.add(Box.createVerticalStrut(8));

        String[] methods = {"QRIS", "Transfer Bank", "Kartu Kredit / Debit", "COD (Bayar di Tempat)"};
        JComboBox<String> payCombo = new JComboBox<>(methods);
        payCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        payCombo.setBackground(COLOR_WHITE);
        payCombo.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        payCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        payCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(payCombo);
        card.add(Box.createVerticalStrut(20));

        // ── Checkout Button ───────────────────────────────────────────────
        JButton checkoutBtn = new JButton("CHECKOUT NOW");
        checkoutBtn.setFont(new Font("Arial", Font.BOLD, 14));
        checkoutBtn.setForeground(COLOR_WHITE);
        checkoutBtn.setBackground(COLOR_RED_BRAND);
        checkoutBtn.setFocusPainted(false);
        checkoutBtn.setBorder(new EmptyBorder(14, 0, 14, 0));
        checkoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        checkoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkoutBtn.addMouseListener(hoverEffect(checkoutBtn, COLOR_RED_BRAND, COLOR_RED_HOVER));
        checkoutBtn.addActionListener(e -> handleCheckout(
                (String) payCombo.getSelectedItem(),
                discountRowRef));

        card.add(checkoutBtn);

        wrap.add(card);
        wrap.add(Box.createVerticalGlue());

        // Isi nilai awal
        refreshSummary();

        return wrap;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REFRESH SUMMARY  (dipanggil setiap qty/item berubah)
    // ══════════════════════════════════════════════════════════════════════
    private void refreshSummary() {
        long subtotal = CartManager.getInstance().getSubtotal();
        long total    = subtotal - discountAmount;
        if (total < 0) total = 0;

        totalItemsLabel.setText(String.valueOf(CartManager.getInstance().getTotalItems()));
        subtotalValueLabel.setText(CartItem.formatRupiah(subtotal));
        totalPayLabel.setText(CartItem.formatRupiah(total));

        if (discountAmount > 0) {
            discountValueLabel.setText("- " + CartItem.formatRupiah(discountAmount));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PROMO CODE
    // ══════════════════════════════════════════════════════════════════════
    private void applyPromo() {
        String code = promoField.getText().trim().toUpperCase();
        if (code.equals("UNIQLO10")) {
            long subtotal    = CartManager.getInstance().getSubtotal();
            discountAmount   = subtotal / 10;   // diskon 10%
            activePromoCode  = code;
            refreshSummary();
            // tampilkan baris diskon
            findDiscountRow(true);
            JOptionPane.showMessageDialog(this,
                    "Promo UNIQLO10 berhasil diterapkan! Diskon 10%.",
                    "Promo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            discountAmount  = 0;
            activePromoCode = "";
            refreshSummary();
            findDiscountRow(false);
            JOptionPane.showMessageDialog(this,
                    "Kode promo tidak valid.",
                    "Promo", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Toggle visibility baris diskon (cari dari parent card) */
    private void findDiscountRow(boolean visible) {
        // Cari dari discountLabel parent
        if (discountLabel.getParent() != null) {
            discountLabel.getParent().setVisible(visible);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CHECKOUT – simpan ke database
    // ══════════════════════════════════════════════════════════════════════
    private void handleCheckout(String paymentMethod, JPanel discountRow) {
        List<CartItem> items = CartManager.getInstance().getItems();

        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Keranjang kamu masih kosong!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (conn == null) {
            JOptionPane.showMessageDialog(this,
                    "Koneksi database gagal.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            conn.setAutoCommit(false);  // mulai transaksi

            // 1. Buat Cart baru
            String cartSql =
                "INSERT INTO Cart (id_customer, tanggal_buat, kode_promo, total_harga) " +
                "VALUES (?, GETDATE(), ?, ?)";
            PreparedStatement cartPs =
                conn.prepareStatement(cartSql, Statement.RETURN_GENERATED_KEYS);

            long total = CartManager.getInstance().getSubtotal() - discountAmount;
            if (total < 0) total = 0;

            cartPs.setString(1, customerId);
            cartPs.setString(2, activePromoCode.isEmpty() ? null : activePromoCode);
            cartPs.setLong(3, total);
            cartPs.executeUpdate();

            ResultSet generatedKeys = cartPs.getGeneratedKeys();
            int cartId = -1;
            if (generatedKeys.next()) cartId = generatedKeys.getInt(1);

            // 2. Masukkan setiap item
            String itemSql =
                "INSERT INTO CartItem " +
                "(id_cart, id_produk, nama_produk, harga_satuan, jumlah, ukuran) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement itemPs = conn.prepareStatement(itemSql);

            for (CartItem ci : items) {
                itemPs.setInt(1, cartId);
                itemPs.setString(2, ci.getProduct().getCategory());   // P001 dst.
                itemPs.setString(3, ci.getProduct().getName());
                itemPs.setLong(4, ci.getPriceNumeric());
                itemPs.setInt(5, ci.getQuantity());
                itemPs.setString(6, ci.getSize());
                itemPs.addBatch();
            }
            itemPs.executeBatch();

            // 3. Buat Order
            String orderSql =
                "INSERT INTO Orders " +
                "(id_cart, id_customer, metode_pembayaran, status_order, tanggal_order) " +
                "VALUES (?, ?, ?, 'Pending', GETDATE())";
            PreparedStatement orderPs = conn.prepareStatement(orderSql);
            orderPs.setInt(1, cartId);
            orderPs.setString(2, customerId);
            orderPs.setString(3, paymentMethod);
            orderPs.executeUpdate();

            conn.commit();

            CartManager.getInstance().clear();
            discountAmount  = 0;
            activePromoCode = "";

            JOptionPane.showMessageDialog(this,
                    "Pesanan berhasil dibuat!\nMetode pembayaran: " + paymentMethod,
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (SQLException ex) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            JOptionPane.showMessageDialog(this,
                    "Gagal menyimpan pesanan:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPER – UI
    // ══════════════════════════════════════════════════════════════════════

    /** Tombol ikon navbar (cart / profile) */
    private JButton makeIconButton(String path, Dimension size) {
        JButton btn;
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            btn = new JButton(new ImageIcon(img));
        } catch (Exception ex) {
            btn = new JButton("?");
        }
        btn.setPreferredSize(size);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(new Color(240, 240, 240));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
            }
        });
        return btn;
    }

    /** Tombol qty [−] / [+] */
    private JButton buildQtyButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(30, 28));
        btn.setBackground(new Color(245, 245, 245));
        btn.setForeground(COLOR_TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(230, 230, 230)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(245, 245, 245)); }
        });
        return btn;
    }

    /** Foto produk dengan fallback */
    private JLabel buildProductImage(String path, int w, int h) {
        JLabel lbl;
        if (path != null && !path.isEmpty() && new java.io.File(path).exists()) {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            lbl = new JLabel(new ImageIcon(img));
        } else {
            lbl = new JLabel();
            lbl.setOpaque(true);
            lbl.setBackground(new Color(230, 230, 230));
        }
        lbl.setPreferredSize(new Dimension(w, h));
        lbl.setMinimumSize(new Dimension(w, h));
        lbl.setMaximumSize(new Dimension(w, h));
        return lbl;
    }

    /** Baris summary dua kolom teks */
    private JPanel makeSummaryRow(String leftText, JLabel rightLabel, boolean bold) {
        JLabel left = new JLabel(leftText);
        left.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, 13));
        left.setForeground(COLOR_TEXT_DARK);
        rightLabel.setFont(new Font("Arial", bold ? Font.BOLD : Font.PLAIN, 13));
        rightLabel.setForeground(COLOR_TEXT_DARK);
        return makeSummaryRowCustom(left, rightLabel);
    }

    private JPanel makeSummaryRowCustom(JLabel left, JLabel right) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(COLOR_WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(left,  BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    /** Garis pemisah tipis */
    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    /** MouseAdapter hover generic untuk JButton */
    private MouseAdapter hoverEffect(JButton btn, Color normal, Color hover) {
        return new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover);  }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        };
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DATABASE CONNECTION
    // ══════════════════════════════════════════════════════════════════════
    private void getConnection() {
        try {
            String url =
                "jdbc:sqlserver://localhost:1433;" +
                "databaseName=Uniqlo;" +
                "encrypt=true;" +
                "trustServerCertificate=true";
            conn = DriverManager.getConnection(url, "sa", "revanna16");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Gagal konek ke database: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
