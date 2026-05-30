package Program;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class HomePageView extends JFrame {
    private String customerId;
    
    public HomePageView(String customerId) {
        this.customerId = customerId;
        
        setTitle("JEKI Store");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());


        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(Color.WHITE);
        navbar.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));

        ImageIcon logoIcon = new ImageIcon("images/logo-jeki.png");
        Image logoImg = logoIcon.getImage().getScaledInstance(90, 40, Image.SCALE_SMOOTH);
        JLabel logo = new JLabel(new ImageIcon(logoImg));

        JTextField searchField = new JTextField("Cari produk atau kategori...");
        searchField.setPreferredSize(new Dimension(500,40));

        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230,230,230),1,true),
                BorderFactory.createEmptyBorder(0,15,0,15)
        ));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0,20,0,20));

        centerPanel.add(searchField, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,15,0));
        rightPanel.setBackground(Color.WHITE);

        Dimension buttonSize = new Dimension(40,40);

        ImageIcon cartIcon = new ImageIcon("images/cart.png");
        Image cartImg = cartIcon.getImage().getScaledInstance(24,24,Image.SCALE_SMOOTH);

        JButton cartButton = new JButton(new ImageIcon(cartImg));

        cartButton.setPreferredSize(buttonSize);
        cartButton.setBorderPainted(false);
        cartButton.setContentAreaFilled(false);
        cartButton.setFocusPainted(false);
        cartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon profileIcon = new ImageIcon("images/profile.png");
        Image profileImg = profileIcon.getImage().getScaledInstance(24,24,Image.SCALE_SMOOTH);

        JButton profileButton = new JButton(new ImageIcon(profileImg));

        profileButton.setPreferredSize(buttonSize);
        profileButton.setBorderPainted(false);
        profileButton.setContentAreaFilled(false);
        profileButton.setFocusPainted(false);
        profileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cartButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cartButton.setContentAreaFilled(true);
                cartButton.setBackground(new Color(240,240,240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cartButton.setContentAreaFilled(false);
            }
        });

        profileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                profileButton.setContentAreaFilled(true);
                profileButton.setBackground(new Color(240,240,240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                profileButton.setContentAreaFilled(false);
            }
        });

        cartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                CartView cart = new CartView(customerId);

                cart.setVisible(true);
            }
        });

        profileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ProfileView(customerId).setVisible(true);
            }
        });

        rightPanel.add(cartButton);
        rightPanel.add(profileButton);

        navbar.add(logo, BorderLayout.WEST);
        navbar.add(centerPanel, BorderLayout.CENTER);
        navbar.add(rightPanel, BorderLayout.EAST);

        mainPanel.add(navbar, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

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

        JPanel productGrid = new JPanel(new GridLayout(0,4,20,20));
        productGrid.setBackground(new Color(245,245,245));
        productGrid.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        ArrayList<Product> products = new ArrayList<>();

        products.add(new Product("P001", "HeatTech Ultra Warm Crew Neck T-Shirt", "Rp 299.000", "images/p001.png"));
        products.add(new Product("P002", "Ultra Light Down Jacket", "Rp 799.000", "images/p002.png"));
        products.add(new Product("P003", "AIRism Cotton Crew Neck T-Shirt", "Rp 199.000", "images/p003.png"));
        products.add(new Product("P004", "Slim-Fit Straight Jeans", "Rp 599.000", "images/p004.png"));
        products.add(new Product("P005", "Fleece Full-Zip Jacket", "Rp 499.000", "images/p005.png"));
        products.add(new Product("P006", "Oxford Button-Down Long Sleeve Shirt", "Rp 399.000", "images/p006.png"));
        products.add(new Product("P007", "Dry-EX Crew Neck Short Sleeve T-Shirt", "Rp 249.000", "images/p007.png"));
        products.add(new Product("P008", "Merino Wool V-Neck Sweater", "Rp 699.000", "images/p008.png"));
        products.add(new Product("P009", "Wide-Fit Chino Pants", "Rp 449.000", "images/p009.png"));
        products.add(new Product("P010", "Pocketable UV Protection Parka", "Rp 549.000", "images/p010.png"));
        products.add(new Product("P011", "Baseball Cap", "Rp 199.000", "images/p011.png"));
        products.add(new Product("P012", "Reversible Bucket Hat", "Rp 249.000", "images/p012.png"));
        products.add(new Product("P013", "Knit Beanie", "Rp 149.000", "images/p013.png"));
        products.add(new Product("P014", "Leather Belt", "Rp 349.000", "images/p014.png"));
        products.add(new Product("P015", "Canvas Web Belt", "Rp 199.000", "images/p015.png"));
        products.add(new Product("P016", "Wool Scarf", "Rp 299.000", "images/p016.png"));
        products.add(new Product("P017", "Socks 3-Pack", "Rp 129.000", "images/p017.png"));
        products.add(new Product("P018", "Tote Bag", "Rp 149.000", "images/p018.png"));
        products.add(new Product("P019", "Round Mini Shoulder Bag", "Rp 299.000", "images/p019.png"));
        products.add(new Product("P020", "HeatTech Extra Warm Leggings", "Rp 349.000", "images/p020.png"));

        for(Product p : products) {
            productGrid.add(new ProductCard(p, customerId));

        }

        JScrollPane scrollPane = new JScrollPane(productGrid);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        contentPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }
}