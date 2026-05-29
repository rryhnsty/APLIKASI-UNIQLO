package Program;

import java.awt.*;
import java.io.File;
import javax.swing.*;

public class ProductCard extends JPanel {

    public ProductCard(Product product, String customerId) {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        setPreferredSize(new Dimension(250, 420));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        // ================= IMAGE SECTION =================

        JPanel imagePanel = new JPanel(new BorderLayout());

        imagePanel.setPreferredSize(new Dimension(220,220));

        imagePanel.setBackground(Color.WHITE);

        JLabel imageLabel;

        if(product.getImagePath() != null &&
           !product.getImagePath().isEmpty()) {

            File file = new File(product.getImagePath());

            if(file.exists()) {

                ImageIcon icon = new ImageIcon(product.getImagePath());

                Image img = icon.getImage().getScaledInstance(
                        220,
                        220,
                        Image.SCALE_SMOOTH
                );

                imageLabel = new JLabel(new ImageIcon(img));

            } else {

                imageLabel = new JLabel(
                        "IMAGE NOT FOUND",
                        SwingConstants.CENTER
                );

                imageLabel.setOpaque(true);

                imageLabel.setBackground(new Color(230,230,230));
            }

        } else {

            imageLabel = new JLabel(
                    "NO IMAGE",
                    SwingConstants.CENTER
            );

            imageLabel.setOpaque(true);

            imageLabel.setBackground(new Color(230,230,230));
        }

        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        imagePanel.add(imageLabel, BorderLayout.CENTER);

        add(imagePanel, BorderLayout.NORTH);

        // ================= INFO SECTION =================

        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(
                new BoxLayout(infoPanel, BoxLayout.Y_AXIS)
        );

        infoPanel.setBackground(Color.WHITE);

        infoPanel.setBorder(
                BorderFactory.createEmptyBorder(10,5,5,5)
        );

        JLabel category = new JLabel(product.getCategory());

        category.setForeground(Color.GRAY);

        category.setFont(new Font("Arial", Font.PLAIN, 11));

        JLabel name = new JLabel(
                "<html><body style='width:200px'>" +
                        product.getName() +
                        "</body></html>"
        );

        name.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel price = new JLabel(product.getPrice());

        price.setForeground(new Color(200,30,40));

        price.setFont(new Font("Arial", Font.BOLD, 16));

        JButton cartBtn = new JButton("Tambah ke Keranjang");

        cartBtn.setBackground(new Color(200,30,40));

        cartBtn.setForeground(Color.WHITE);

        cartBtn.setFocusPainted(false);

        cartBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cartBtn.setMaximumSize(
                new Dimension(Integer.MAX_VALUE,40)
        );

        // ================= ADD TO CART =================

        cartBtn.addActionListener(e -> {

            CartManager.getInstance().setCustomerId(customerId);

            CartManager.getInstance().addItem(
                    product,
                    1,
                    "M"
            );
        });

        // tambah component
        infoPanel.add(category);

        infoPanel.add(Box.createVerticalStrut(5));

        infoPanel.add(name);

        infoPanel.add(Box.createVerticalStrut(10));

        infoPanel.add(price);

        infoPanel.add(Box.createVerticalGlue());

        infoPanel.add(Box.createVerticalStrut(10));

        infoPanel.add(cartBtn);

        add(infoPanel, BorderLayout.CENTER);
    }
}