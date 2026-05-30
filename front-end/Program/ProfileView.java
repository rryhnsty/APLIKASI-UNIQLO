package Program;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProfileView extends JFrame {

    Connection conn;

    private String customerId;

    JTextField namaField;
    JTextField teleponField;
    JTextArea alamatArea;
    JLabel emailLabel;
    JLabel avatarLabel;
    JLabel usernameLabel;

    public ProfileView(String customerId) {

        this.customerId = customerId;

        getConnection();

        setTitle("Profile");
        setSize(900,650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(20,30,20,30));

        JLabel title = new JLabel("PROFIL");
        title.setFont(new Font("Arial", Font.BOLD, 32));

        JButton backButton = new JButton("Kembali");

        backButton.addActionListener(e -> dispose());

        header.add(title, BorderLayout.WEST);
        header.add(backButton, BorderLayout.EAST);

        mainPanel.add(header, BorderLayout.NORTH);

      

        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20,40,20,40));

       

        JPanel profileCard = new JPanel(new FlowLayout(FlowLayout.LEFT,20,20));
        profileCard.setBackground(Color.WHITE);

        avatarLabel = new JLabel("U");
        avatarLabel.setPreferredSize(new Dimension(80,80));
        avatarLabel.setOpaque(true);
        avatarLabel.setBackground(new Color(245,235,235));
        avatarLabel.setForeground(new Color(180,60,60));
        avatarLabel.setFont(new Font("Arial", Font.BOLD, 36));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel userInfo = new JPanel();
        userInfo.setBackground(Color.WHITE);
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));

        usernameLabel = new JLabel("User");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel statusLabel = new JLabel("PENGGUNA AKTIF");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(210,245,220));
        statusLabel.setForeground(new Color(40,120,60));
        statusLabel.setBorder(new EmptyBorder(5,10,5,10));

        userInfo.add(usernameLabel);
        userInfo.add(Box.createVerticalStrut(10));
        userInfo.add(statusLabel);

        profileCard.add(avatarLabel);
        profileCard.add(userInfo);

        content.add(profileCard);

        content.add(Box.createVerticalStrut(30));

       

        JLabel infoTitle = new JLabel("INFORMASI AKUN");
        infoTitle.setFont(new Font("Arial", Font.BOLD, 24));

        content.add(infoTitle);
        content.add(Box.createVerticalStrut(20));

        

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("NAMA"), gbc);

        
        gbc.gridx = 1;
        formPanel.add(new JLabel("NOMOR TELEPON"), gbc);

        
        gbc.gridx = 0;
        gbc.gridy = 1;
        namaField = new JTextField();
        namaField.setPreferredSize(new Dimension(250,35));
        formPanel.add(namaField, gbc);

       
        gbc.gridx = 1;
        teleponField = new JTextField();
        teleponField.setPreferredSize(new Dimension(250,35));
        formPanel.add(teleponField, gbc);

        

        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("EMAIL"), gbc);

        
        gbc.gridx = 1;
        formPanel.add(new JLabel("ALAMAT"), gbc);

        

        
        gbc.gridx = 0;
        gbc.gridy = 3;

        emailLabel = new JLabel();
        emailLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        emailLabel.setPreferredSize(new Dimension(250,35));
        formPanel.add(emailLabel, gbc);

        
        gbc.gridx = 1;

        alamatArea = new JTextArea(5,20);
        alamatArea.setLineWrap(true);
        alamatArea.setWrapStyleWord(true);

        JScrollPane alamatScroll = new JScrollPane(alamatArea);
        alamatScroll.setPreferredSize(new Dimension(250,120));

        formPanel.add(alamatScroll, gbc);

        content.add(formPanel);

        content.add(Box.createVerticalStrut(30));

        

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Simpan Perubahan");

        saveButton.setBackground(new Color(190,60,60));
        saveButton.setForeground(Color.WHITE);

        saveButton.addActionListener(e -> saveProfile());

        buttonPanel.add(saveButton);

        content.add(buttonPanel);

        mainPanel.add(content, BorderLayout.CENTER);

        add(mainPanel);

        loadProfile();
    }

    

    private void loadProfile() {

        try {

            String sql =
                "SELECT * FROM Customer WHERE id_customer = ?";

            PreparedStatement ps =
                conn.prepareStatement(sql);

            ps.setString(1, customerId);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {

                String nama = rs.getString("nama");
                String email = rs.getString("email");
                String alamat = rs.getString("alamat");
                String telepon = rs.getString("no_telepon");

                
                if(nama == null) nama = "";
                if(alamat == null) alamat = "";
                if(telepon == null) telepon = "";

                namaField.setText(nama);
                alamatArea.setText(alamat);
                teleponField.setText(telepon);

                emailLabel.setText(email);

                if(!nama.isEmpty()) {

                    usernameLabel.setText(nama);

                    avatarLabel.setText(
                        String.valueOf(
                            Character.toUpperCase(
                                nama.charAt(0)
                            )
                        )
                    );

                } else {

                    usernameLabel.setText(email);

                    avatarLabel.setText(
                        String.valueOf(
                            Character.toUpperCase(
                                email.charAt(0)
                            )
                        )
                    );
                }
            }

        } catch(Exception e) {

            JOptionPane.showMessageDialog(this,
                e.getMessage());

        }
    }

    

    private void saveProfile() {

        try {

            String sql =
                "UPDATE Customer " +
                "SET nama = ?, alamat = ?, no_telepon = ? " +
                "WHERE id_customer = ?";

            PreparedStatement ps =
                conn.prepareStatement(sql);

            ps.setString(1, namaField.getText());
            ps.setString(2, alamatArea.getText());
            ps.setString(3, teleponField.getText());
            ps.setString(4, customerId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                "Profile berhasil diperbarui!");

            loadProfile();

        } catch(Exception e) {

            JOptionPane.showMessageDialog(this,
                e.getMessage());

        }
    }

    

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

            JOptionPane.showMessageDialog(null,
                "Gagal konek database");

        }
    }
}

