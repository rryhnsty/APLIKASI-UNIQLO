import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.*;

public class LoginPageView extends JFrame {
    Connection conn;

    // Warna-warna yang disesuaikan
    private static final Color COLOR_LEFT_PANEL = new Color(200, 200, 200); // Abu-abu muda
    private static final Color COLOR_RIGHT_PANEL = Color.WHITE;
    private static final Color COLOR_RED_BRAND = new Color(200, 30, 40); // Merah khas
    private static final Color COLOR_RED_HOVER = new Color(160, 20, 30); // Merah lebih gelap untuk hover
    private static final Color COLOR_TEXT_GRAY = new Color(100, 100, 100);
    private static final Color COLOR_INPUT_BG = new Color(240, 240, 240);
    private static final Color COLOR_INPUT_BORDER = new Color(220, 220, 220);

    public LoginPageView() {
        setTitle("Login Page - Style Connect");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // Ukuran default, bisa disesuaikan
        setLocationRelativeTo(null); // Center screen
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen saat start
        setLayout(new BorderLayout());

        // --- PANEL KIRI ---
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(COLOR_LEFT_PANEL);
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); // Padding

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.anchor = GridBagConstraints.SOUTHWEST; // Mulai dari pojok kiri bawah
        gbcLeft.weightx = 1.0;
        gbcLeft.weighty = 1.0; // Ambil sisa ruang vertikal

        // Judul besar "Aesthetics Refined."
        JLabel titleLabel = new JLabel("Bismillah BasDat Nilai A");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.BLACK);
        leftPanel.add(titleLabel, gbcLeft);

        // Deskripsi kecil
        gbcLeft.gridy = 1;
        gbcLeft.weighty = 0; // Tidak mengambil ruang vertikal tambahan
        gbcLeft.insets = new Insets(20, 0, 0, 0); // Margin top
        JTextArea descLabel = new JTextArea("so if you care to find me, look to the western skyyyy");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        descLabel.setForeground(COLOR_TEXT_GRAY);
        descLabel.setLineWrap(true);
        descLabel.setWrapStyleWord(true);
        descLabel.setEditable(false);
        descLabel.setOpaque(false);
        descLabel.setColumns(25); // Atur lebar textarea
        leftPanel.add(descLabel, gbcLeft);

        // --- PANEL KANAN ---
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(COLOR_RIGHT_PANEL);
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(80, 80, 80, 80)); // Padding

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        gbcRight.anchor = GridBagConstraints.NORTH; // Center horizontal, top vertical
        gbcRight.weighty = 0.1; // Ruang kosong di atas

        // Logo (Fiktif)
        JLabel logoLabel = new JLabel("STYLE CONNECT");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        logoLabel.setForeground(COLOR_RED_BRAND);
        rightPanel.add(logoLabel, gbcRight);

        gbcRight.gridy = 1;
        gbcRight.weighty = 0;
        gbcRight.insets = new Insets(40, 0, 0, 0); // Margin top

        // "Welcome Back"
        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        rightPanel.add(welcomeLabel, gbcRight);

        gbcRight.gridy = 2;
        gbcRight.insets = new Insets(10, 0, 0, 0);
        // "Enter your details..."
        JLabel detailsLabel = new JLabel("Enter your details to access your account.");
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        detailsLabel.setForeground(COLOR_TEXT_GRAY);
        rightPanel.add(detailsLabel, gbcRight);

        gbcRight.gridy = 3;
        gbcRight.insets = new Insets(40, 0, 0, 0);
        // --- User/Admin Switcher ---
        JPanel switcherPanel = new JPanel(new GridLayout(1, 2));
        switcherPanel.setOpaque(false);
        switcherPanel.setPreferredSize(new Dimension(300, 40));

        JButton userBtn = new JButton("User");
        userBtn.setFocusPainted(false);
        userBtn.setBackground(COLOR_INPUT_BG);
        userBtn.setForeground(Color.BLACK);
        userBtn.setBorder(BorderFactory.createLineBorder(COLOR_INPUT_BORDER));

        JButton adminBtn = new JButton("Admin");
        adminBtn.setFocusPainted(false);
        adminBtn.setBackground(COLOR_RIGHT_PANEL); // Admin default
        adminBtn.setForeground(COLOR_TEXT_GRAY);
        adminBtn.setBorder(BorderFactory.createLineBorder(COLOR_INPUT_BORDER));

        switcherPanel.add(userBtn);
        switcherPanel.add(adminBtn);
        rightPanel.add(switcherPanel, gbcRight);

        // --- Form Inputs ---
        gbcRight.gridy = 4;
        gbcRight.insets = new Insets(30, 0, 0, 0);
        gbcRight.fill = GridBagConstraints.HORIZONTAL; // Buat input selebar panel
        gbcRight.weightx = 1.0;

        // Email
        JPanel emailFieldPanel = createInputField("Email", "ryanunjukkebolehan@gmail.com");
        rightPanel.add(emailFieldPanel, gbcRight);

        // Password + Forgot Password?
        gbcRight.gridy = 5;
        gbcRight.insets = new Insets(20, 0, 0, 0);
        JPanel passwordFieldPanel = createPasswordField("Password", "mas ryan unuk kebolehan");
        rightPanel.add(passwordFieldPanel, gbcRight);

        // Remember me
        gbcRight.gridy = 6;
        gbcRight.insets = new Insets(15, 0, 0, 0);
        gbcRight.fill = GridBagConstraints.NONE;
        gbcRight.anchor = GridBagConstraints.WEST;
        JCheckBox rememberMe = new JCheckBox("Remember me");
        rememberMe.setFont(new Font("Arial", Font.PLAIN, 14));
        rememberMe.setOpaque(false);
        rightPanel.add(rememberMe, gbcRight);

        // --- Tombol 'Sign In' ---
        gbcRight.gridy = 7;
        gbcRight.insets = new Insets(40, 0, 0, 0);
        gbcRight.fill = GridBagConstraints.HORIZONTAL;
        gbcRight.anchor = GridBagConstraints.CENTER;
        JButton signInBtn = new JButton("Sign In");
        signInBtn.setFont(new Font("Arial", Font.BOLD, 18));
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setBackground(COLOR_RED_BRAND);
        signInBtn.setFocusPainted(false);
        signInBtn.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0)); // Padding dalam tombol

        // **EFEK HOVER** (Menggelapkan tombol saat kursor di atasnya)
        signInBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                signInBtn.setBackground(COLOR_RED_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                signInBtn.setBackground(COLOR_RED_BRAND);
            }
        });
        rightPanel.add(signInBtn, gbcRight);

        // --- Link Sign Up ---
        gbcRight.gridy = 8;
        gbcRight.insets = new Insets(30, 0, 0, 0);
        gbcRight.fill = GridBagConstraints.NONE;
        gbcRight.anchor = GridBagConstraints.CENTER;
        gbcRight.weighty = 0.1; // Ruang kosong di bawah

        JLabel signUpLink = new JLabel("<html>Don't have an account? <span style='color: rgb(200,30,40);'>Sign Up</span></html>");
        signUpLink.setFont(new Font("Arial", Font.PLAIN, 14));
        signUpLink.setForeground(COLOR_TEXT_GRAY);
        signUpLink.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Ubah kursor jadi tangan
        rightPanel.add(signUpLink, gbcRight);

        // Tambahkan kedua panel ke frame utama
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(0.5); // Bagi 50/50
        splitPane.setDividerSize(0); // Sembunyikan garis pembagi
        splitPane.setEnabled(false); // Matikan kemampuan drag pembagi
        add(splitPane, BorderLayout.CENTER);
    }

    // Metode bantuan untuk membuat panel input field yang bersih
    private JPanel createInputField(String labelText, String placeholder) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        panel.add(label, BorderLayout.NORTH);

        JTextField textField = new JTextField(placeholder);
        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        textField.setBackground(COLOR_INPUT_BG);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10) // Padding dalam
        ));
        panel.add(textField, BorderLayout.CENTER);

        return panel;
    }

    // Metode bantuan untuk membuat panel password field dengan link 'Forgot Password?'
    private JPanel createPasswordField(String labelText, String placeholder) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0;
        JLabel forgotLink = new JLabel("Forgot Password?");
        forgotLink.setFont(new Font("Arial", Font.PLAIN, 12));
        forgotLink.setForeground(COLOR_RED_BRAND);
        forgotLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(forgotLink, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2; // Pakai dua kolom
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPasswordField passwordField = new JPasswordField(placeholder);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBackground(COLOR_INPUT_BG);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10) // Padding dalam
        ));
        panel.add(passwordField, gbc);

        return panel;
    }

    private void getCOnnection() {
        try {
            String url = "jdbc:sqlserver://localhost:1433;databaseName=Uniqlo;encrypt=true;trustServerCertificate=true";
            String user = "sa";
            String password = "revanna16";

            conn = DriverManager.getConnection(url, user, password);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    void buatAkun() {
        try {
            String sql = "INSERT INTO Customer VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);

            JOptionPane.showMessageDialog(null, "Data berhasil ditambah");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Jalankan GUI di event dispatch thread
        SwingUtilities.invokeLater(() -> {
            LoginPageView view = new LoginPageView();
            view.setVisible(true);
        });
    }
}
