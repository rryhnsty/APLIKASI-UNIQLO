package SQLManagement.TugasAkhirBasdat.frontend;


import java.awt.*;
import javax.swing.*;

public class ProductCard extends JPanel {

    public ProductCard(Product product) {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));

        // IMAGE
        JLabel imageLabel;

        if(product.getImagePath() != null && !product.getImagePath().isEmpty()) {

            ImageIcon icon = new ImageIcon(product.getImagePath());

            Image img = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);

            imageLabel = new JLabel(new ImageIcon(img));

        } else {

            imageLabel = new JLabel("NO IMAGE", SwingConstants.CENTER);
            imageLabel.setPreferredSize(new Dimension(180,180));
            imageLabel.setOpaque(true);
            imageLabel.setBackground(new Color(230,230,230));
        }

        add(imageLabel, BorderLayout.NORTH);

        // INFO
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JLabel category = new JLabel(product.getCategory());
        category.setForeground(Color.GRAY);
        category.setFont(new Font("Arial", Font.PLAIN, 11));

        JLabel name = new JLabel(product.getName());
        name.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel price = new JLabel(product.getPrice());
        price.setForeground(new Color(200,30,40));
        price.setFont(new Font("Arial", Font.BOLD, 16));

        JButton cartBtn = new JButton("Tambah ke Keranjang");
        cartBtn.setBackground(new Color(200,30,40));
        cartBtn.setForeground(Color.WHITE);
        cartBtn.setFocusPainted(false);

        infoPanel.add(category);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(name);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(price);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(cartBtn);

        add(infoPanel, BorderLayout.CENTER);
    }
}