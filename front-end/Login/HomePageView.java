package Login;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class HomePageView extends JFrame {

    public HomePageView() {

        setTitle("JEKI Store");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // ================= NAVBAR =================

        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(Color.WHITE);
        navbar.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));

        JLabel logo = new JLabel("JEKI");
        logo.setFont(new Font("Arial", Font.BOLD, 26));
        logo.setForeground(new Color(200,30,40));

        JTextField searchField = new JTextField("Cari produk...");
        searchField.setPreferredSize(new Dimension(300,40));

        navbar.add(logo, BorderLayout.WEST);
        navbar.add(searchField, BorderLayout.CENTER);

        mainPanel.add(navbar, BorderLayout.NORTH);

        // ================= CONTENT =================

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

        // HERO SECTION
        JPanel heroPanel = new JPanel();
        heroPanel.setPreferredSize(new Dimension(1200,300));
        heroPanel.setBackground(new Color(40,40,40));
        heroPanel.setLayout(new BorderLayout());

        JLabel heroText = new JLabel(
                "<html><div style='padding:40px;'>"
                + "<h1 style='color:white;'>Esensial Modern.</h1>"
                + "<p style='color:white;'>Temukan koleksi terbaru kami.</p>"
                + "</div></html>"
        );

        heroPanel.add(heroText);

        contentPanel.add(heroPanel, BorderLayout.NORTH);

        // ================= PRODUCT GRID =================

        JPanel productGrid = new JPanel(new GridLayout(0,4,20,20));
        productGrid.setBackground(new Color(245,245,245));
        productGrid.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        ArrayList<Product> products = new ArrayList<>();

        // ===== EDIT PRODUK MANUAL DISINI =====

        products.add(new Product(
                "PAKAIAN KASUAL",
                "HeatTech Crew Neck",
                "Rp 299.000",
                "images/baju1.jpg"
        ));

        products.add(new Product(
                "AKSESORIS",
                "Bucket Hat",
                "Rp 199.000",
                "images/topi.jpg"
        ));

        products.add(new Product(
                "PAKAIAN",
                "Oxford Shirt",
                "Rp 399.000",
                "images/shirt.jpg"
        ));

        // ====================================

        for(Product p : products) {

            productGrid.add(new ProductCard(p));

        }

        JScrollPane scrollPane = new JScrollPane(productGrid);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }
}