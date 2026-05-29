package Program;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CartView extends JFrame {

    Connection conn;

    private String customerId;

    JPanel cartContainer;
    JLabel totalLabel;

    int grandTotal = 0;

    public CartView(String customerId) {

        this.customerId = customerId;

        getConnection();

        setTitle("My Cart");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // ================= HEADER =================

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(20,20,20,20));

        JLabel titleLabel = new JLabel("My Shopping Cart");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

        JButton backButton = new JButton("Back");

        backButton.addActionListener(e -> {
            dispose();
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ================= CART CONTENT =================

        cartContainer = new JPanel();
        cartContainer.setLayout(new BoxLayout(cartContainer, BoxLayout.Y_AXIS));
        cartContainer.setBackground(new Color(245,245,245));

        JScrollPane scrollPane = new JScrollPane(cartContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ================= FOOTER =================

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(20,20,20,20));

        totalLabel = new JLabel("Total : Rp 0");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JButton checkoutButton = new JButton("Checkout");

        checkoutButton.setBackground(Color.BLACK);
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFocusPainted(false);
        checkoutButton.setPreferredSize(new Dimension(180,50));

        checkoutButton.addActionListener(e -> {

            JOptionPane.showMessageDialog(
                    this,
                    "Checkout berhasil!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

        });

        footerPanel.add(totalLabel, BorderLayout.WEST);
        footerPanel.add(checkoutButton, BorderLayout.EAST);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loadCartData();
    }

    // ================= LOAD CART =================

    private void loadCartData() {

        cartContainer.removeAll();

        grandTotal = 0;

        java.util.List<CartItem> items =
                CartManager.getInstance().getItems();

        if(items.isEmpty()) {

            JLabel emptyLabel = new JLabel("Keranjang masih kosong");

            emptyLabel.setFont(new Font("Arial", Font.BOLD, 24));

            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel emptyPanel = new JPanel(new BorderLayout());

            emptyPanel.setBackground(new Color(245,245,245));

            emptyPanel.setBorder(new EmptyBorder(100,0,0,0));

            emptyPanel.add(emptyLabel, BorderLayout.CENTER);

            cartContainer.add(emptyPanel);

        } else {

            for(CartItem item : items) {

                Product p = item.getProduct();

                int subtotal =
                        (int) item.getSubtotal();

                grandTotal += subtotal;

                JPanel itemPanel = createCartItem(
                        p.getCategory(),
                        p.getName(),
                        p.getPrice(),
                        p.getImagePath(),
                        item.getQuantity(),
                        subtotal
                );

                cartContainer.add(itemPanel);

                cartContainer.add(Box.createVerticalStrut(15));
            }
        }

        totalLabel.setText(
                "Total : Rp " + formatPrice(grandTotal)
        );

        cartContainer.revalidate();

        cartContainer.repaint();
    }

    // ================= CART ITEM =================

    private JPanel createCartItem(
            String productId,
            String productName,
            String productPrice,
            String imagePath,
            int quantity,
            int subtotal
    ) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,180));
        panel.setBorder(new EmptyBorder(15,15,15,15));

        // ================= IMAGE =================

        ImageIcon icon = new ImageIcon(imagePath);

        Image img = icon.getImage().getScaledInstance(
                120,
                120,
                Image.SCALE_SMOOTH
        );

        JLabel imageLabel = new JLabel(new ImageIcon(img));

        panel.add(imageLabel, BorderLayout.WEST);

        // ================= INFO =================

        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(0,20,0,0));

        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel priceLabel = new JLabel(productPrice);
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        JLabel qtyLabel = new JLabel("Quantity : " + quantity);
        qtyLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel subtotalLabel = new JLabel(
                "Subtotal : Rp " + formatPrice(subtotal)
        );

        subtotalLabel.setFont(new Font("Arial", Font.BOLD, 18));

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(qtyLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(subtotalLabel);

        panel.add(infoPanel, BorderLayout.CENTER);

        // ================= BUTTON =================

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        JButton deleteButton = new JButton("Remove");

        deleteButton.setBackground(Color.RED);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);

        deleteButton.addActionListener(e -> {

            removeCartItem(productId);

        });

        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    // ================= REMOVE ITEM =================

    private void removeCartItem(String productId) {

        java.util.List<CartItem> items =
                CartManager.getInstance().getItems();

        CartItem target = null;

        for(CartItem item : items) {

            if(item.getProduct().getCategory().equals(productId)) {

                target = item;

                break;
            }
        }

        if(target != null) {

            CartManager.getInstance().removeItem(target);

            JOptionPane.showMessageDialog(
                    this,
                    "Produk dihapus dari cart"
            );

            loadCartData();
        }
    }   

    // ================= PRICE HELPER =================

    private int convertPriceToNumber(String price) {

        price = price.replace("Rp", "");
        price = price.replace(".", "");
        price = price.replace(",", "");
        price = price.trim();

        return Integer.parseInt(price);
    }

    private String formatPrice(int number) {

        return String.format("%,d", number)
                .replace(",", ".");
    }

    // ================= DATABASE =================

    private void getConnection() {

        try {

            String url =
                    "jdbc:sqlserver://localhost:1433;databaseName=Uniqlo;encrypt=true;trustServerCertificate=true";

            conn = DriverManager.getConnection(
                    url,
                    "sa",
                    "revanna16"
            );

        } catch(Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Database Error : " + e.getMessage()
            );

        }
    }
}