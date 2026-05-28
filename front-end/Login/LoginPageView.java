package Login;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.*;

public class LoginPageView extends JFrame {
    Connection conn;

    private static final Color COLOR_LEFT_PANEL   = new Color(200, 200, 200);
    private static final Color COLOR_RIGHT_PANEL  = Color.WHITE;
    private static final Color COLOR_RED_BRAND    = new Color(200, 30, 40);
    private static final Color COLOR_RED_HOVER    = new Color(160, 20, 30);
    private static final Color COLOR_TEXT_GRAY    = new Color(100, 100, 100);
    private static final Color COLOR_INPUT_BG     = new Color(240, 240, 240);
    private static final Color COLOR_INPUT_BORDER = new Color(220, 220, 220);

    // Warna tombol switcher
    private static final Color COLOR_BTN_ACTIVE       = new Color(200, 30, 40);   // merah saat aktif
    private static final Color COLOR_BTN_ACTIVE_HOVER = new Color(160, 20, 30);   // merah gelap saat hover
    private static final Color COLOR_BTN_INACTIVE      = new Color(240, 240, 240); // abu saat tidak aktif
    private static final Color COLOR_BTN_INACTIVE_HOVER= new Color(210, 210, 210); // abu gelap saat hover

    // State: apakah mode admin atau user?
    private boolean isAdminMode = false;

    // Referensi ke komponen yang dipakai saat login
    private JTextField emailFieldRef;
    private JPasswordField passwordFieldRef;
    private JButton userBtn;
    private JButton adminBtn;
    private JLabel loginModeLabel;

    public LoginPageView() {
        setTitle("Login Page - Style Connect");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // ── Left Panel ──────────────────────────────────────────────────────
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(COLOR_LEFT_PANEL);
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx    = 0;
        gbcLeft.gridy    = 0;
        gbcLeft.anchor   = GridBagConstraints.SOUTHWEST;
        gbcLeft.weightx  = 1.0;
        gbcLeft.weighty  = 1.0;

        JLabel titleLabel = new JLabel("Bismillah BasDat Nilai A");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.BLACK);
        leftPanel.add(titleLabel, gbcLeft);

        gbcLeft.gridy   = 1;
        gbcLeft.weighty = 0;
        gbcLeft.insets  = new Insets(20, 0, 0, 0);

        JTextArea descLabel = new JTextArea("so if you care to find me, look to the western skyyyy");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        descLabel.setForeground(COLOR_TEXT_GRAY);
        descLabel.setLineWrap(true);
        descLabel.setWrapStyleWord(true);
        descLabel.setEditable(false);
        descLabel.setOpaque(false);
        descLabel.setColumns(25);
        leftPanel.add(descLabel, gbcLeft);

        // ── Right Panel ─────────────────────────────────────────────────────
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(COLOR_RIGHT_PANEL);
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(80, 80, 80, 80));

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx  = 0;
        gbcRight.gridy  = 0;
        gbcRight.anchor = GridBagConstraints.NORTH;
        gbcRight.weighty = 0.1;

        JLabel logoLabel = new JLabel("STYLE CONNECT");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        logoLabel.setForeground(COLOR_RED_BRAND);
        rightPanel.add(logoLabel, gbcRight);

        gbcRight.gridy  = 1;
        gbcRight.weighty = 0;
        gbcRight.insets = new Insets(40, 0, 0, 0);

        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 36));
        rightPanel.add(welcomeLabel, gbcRight);

        gbcRight.gridy  = 2;
        gbcRight.insets = new Insets(10, 0, 0, 0);

        JLabel detailsLabel = new JLabel("Enter your details to access your account.");
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        detailsLabel.setForeground(COLOR_TEXT_GRAY);
        rightPanel.add(detailsLabel, gbcRight);

        // ── Switcher User / Admin ────────────────────────────────────────────
        gbcRight.gridy  = 3;
        gbcRight.insets = new Insets(40, 0, 0, 0);

        JPanel switcherPanel = new JPanel(new GridLayout(1, 2));
        switcherPanel.setOpaque(false);
        switcherPanel.setPreferredSize(new Dimension(300, 40));

        userBtn  = createSwitcherButton("User",  true);   // default aktif
        adminBtn = createSwitcherButton("Admin", false);

        // Aksi klik: User
        userBtn.addActionListener(e -> {
            if (!isAdminMode) return; // sudah di mode user
            isAdminMode = false;
            updateSwitcher();
        });

        // Aksi klik: Admin
        adminBtn.addActionListener(e -> {
            if (isAdminMode) return; // sudah di mode admin
            isAdminMode = true;
            updateSwitcher();
        });

        switcherPanel.add(userBtn);
        switcherPanel.add(adminBtn);
        rightPanel.add(switcherPanel, gbcRight);

        // ── Label mode login (berubah saat switch) ───────────────────────────
        gbcRight.gridy  = 4;
        gbcRight.insets = new Insets(12, 0, 0, 0);

        loginModeLabel = new JLabel("Login sebagai: Customer");
        loginModeLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        loginModeLabel.setForeground(COLOR_RED_BRAND);
        rightPanel.add(loginModeLabel, gbcRight);

        // ── Email Field ──────────────────────────────────────────────────────
        gbcRight.gridy  = 5;
        gbcRight.insets = new Insets(20, 0, 0, 0);
        gbcRight.fill   = GridBagConstraints.HORIZONTAL;
        gbcRight.weightx = 1.0;

        JPanel emailFieldPanel = createInputField("Email", "contoh@email.com");
        rightPanel.add(emailFieldPanel, gbcRight);

        // ── Password Field ───────────────────────────────────────────────────
        gbcRight.gridy  = 6;
        gbcRight.insets = new Insets(20, 0, 0, 0);

        JPanel passwordFieldPanel = createPasswordField("Password", "");
        rightPanel.add(passwordFieldPanel, gbcRight);

        // ── Remember Me ──────────────────────────────────────────────────────
        gbcRight.gridy  = 7;
        gbcRight.insets = new Insets(15, 0, 0, 0);
        gbcRight.fill   = GridBagConstraints.NONE;
        gbcRight.anchor = GridBagConstraints.WEST;

        JCheckBox rememberMe = new JCheckBox("Remember me");
        rememberMe.setFont(new Font("Arial", Font.PLAIN, 14));
        rememberMe.setOpaque(false);
        rightPanel.add(rememberMe, gbcRight);

        // ── Sign In Button ───────────────────────────────────────────────────
        gbcRight.gridy  = 8;
        gbcRight.insets = new Insets(40, 0, 0, 0);
        gbcRight.fill   = GridBagConstraints.HORIZONTAL;
        gbcRight.anchor = GridBagConstraints.CENTER;

        JButton signInBtn = new JButton("Sign In");
        signInBtn.setFont(new Font("Arial", Font.BOLD, 18));
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setBackground(COLOR_RED_BRAND);
        signInBtn.setFocusPainted(false);
        signInBtn.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        signInBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { signInBtn.setBackground(COLOR_RED_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { signInBtn.setBackground(COLOR_RED_BRAND); }
        });

        // Logika sign in
        signInBtn.addActionListener(e -> handleSignIn());

        rightPanel.add(signInBtn, gbcRight);

        // ── Sign Up Link ─────────────────────────────────────────────────────
        gbcRight.gridy  = 9;
        gbcRight.insets = new Insets(30, 0, 0, 0);
        gbcRight.fill   = GridBagConstraints.NONE;
        gbcRight.anchor = GridBagConstraints.CENTER;
        gbcRight.weighty = 0.1;

        JLabel signUpLink = new JLabel(
            "<html>Don't have an account? <span style='color: rgb(200,30,40);'>Sign Up</span></html>");
        signUpLink.setFont(new Font("Arial", Font.PLAIN, 14));
        signUpLink.setForeground(COLOR_TEXT_GRAY);
        signUpLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SignUpView signUpView = new SignUpView(LoginPageView.this);
                signUpView.setVisible(true);
            }
        });
        rightPanel.add(signUpLink, gbcRight);

        // ── Split Pane ───────────────────────────────────────────────────────
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(0.5);
        splitPane.setDividerSize(0);
        splitPane.setEnabled(false);
        add(splitPane, BorderLayout.CENTER);

        getConnection();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helper: buat tombol switcher
    // ─────────────────────────────────────────────────────────────────────────
    private JButton createSwitcherButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(COLOR_INPUT_BORDER));

        if (active) {
            btn.setBackground(COLOR_BTN_ACTIVE);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(COLOR_BTN_INACTIVE);
            btn.setForeground(COLOR_TEXT_GRAY);
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boolean isActive = (text.equals("User") && !isAdminMode)
                                || (text.equals("Admin") && isAdminMode);
                btn.setBackground(isActive ? COLOR_BTN_ACTIVE_HOVER : COLOR_BTN_INACTIVE_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                boolean isActive = (text.equals("User") && !isAdminMode)
                                || (text.equals("Admin") && isAdminMode);
                btn.setBackground(isActive ? COLOR_BTN_ACTIVE : COLOR_BTN_INACTIVE);
            }
        });

        return btn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Update tampilan tombol switcher setelah diklik
    // ─────────────────────────────────────────────────────────────────────────
    private void updateSwitcher() {
        if (isAdminMode) {
            adminBtn.setBackground(COLOR_BTN_ACTIVE);
            adminBtn.setForeground(Color.WHITE);
            userBtn.setBackground(COLOR_BTN_INACTIVE);
            userBtn.setForeground(COLOR_TEXT_GRAY);
            loginModeLabel.setText("Login sebagai: Admin");
        } else {
            userBtn.setBackground(COLOR_BTN_ACTIVE);
            userBtn.setForeground(Color.WHITE);
            adminBtn.setBackground(COLOR_BTN_INACTIVE);
            adminBtn.setForeground(COLOR_TEXT_GRAY);
            loginModeLabel.setText("Login sebagai: Customer");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Logika Sign In
    // ─────────────────────────────────────────────────────────────────────────
    private void handleSignIn() {
        if (emailFieldRef == null || passwordFieldRef == null) {
            JOptionPane.showMessageDialog(this, "Form tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String email    = emailFieldRef.getText().trim();
        String password = new String(passwordFieldRef.getPassword());

        // Validasi input kosong
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Email dan password harus diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (conn == null) {
            JOptionPane.showMessageDialog(this,
                "Koneksi database gagal.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (isAdminMode) {
                // ── Cek tabel Admin ──────────────────────────────────────────
                // Sesuaikan nama tabel dan kolom dengan skema database kamu
                String sql = "SELECT * FROM Admin WHERE email = ? AND password = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, email);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this,
                        "Login Admin berhasil!\nSelamat datang, " + email,
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    // TODO: buka halaman dashboard admin
                    // new AdminDashboard().setVisible(true);
                    // dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Email atau password admin salah.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                // ── Cek tabel Customer ───────────────────────────────────────
                // Langkah 1: cek apakah email terdaftar
                String checkEmailSql = "SELECT * FROM Customer WHERE email = ?";
                PreparedStatement psEmail = conn.prepareStatement(checkEmailSql);
                psEmail.setString(1, email);
                ResultSet rsEmail = psEmail.executeQuery();

                if (!rsEmail.next()) {
                    // Email tidak ditemukan
                    JOptionPane.showMessageDialog(this,
                        "Email tidak terdaftar.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Langkah 2: cek password
                String dbPassword = rsEmail.getString("password");
                if (!dbPassword.equals(password)) {
                    JOptionPane.showMessageDialog(this,
                        "Password salah.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Login berhasil
                String customerId = rsEmail.getString("id_customer");
                JOptionPane.showMessageDialog(this,
                    "Login berhasil!\nSelamat datang, " + email + "\nID: " + customerId,
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
                // TODO: buka halaman utama customer
               HomPageView home = new HomPageView();
                home.setVisible(true);
                dispose();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helper: buat input field biasa (menyimpan referensi ke emailFieldRef)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createInputField(String labelText, String placeholder) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        panel.add(label, BorderLayout.NORTH);

        JTextField textField = new JTextField();
        textField.setToolTipText(placeholder);
        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        textField.setBackground(COLOR_INPUT_BG);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.add(textField, BorderLayout.CENTER);

        // Simpan referensi untuk dipakai saat sign in
        if (labelText.equals("Email")) {
            emailFieldRef = textField;
        }

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helper: buat password field (menyimpan referensi ke passwordFieldRef)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createPasswordField(String labelText, String placeholder) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.gridy   = 0;
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.weightx = 1.0;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        panel.add(label, gbc);

        gbc.gridx  = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0;

        JLabel forgotLink = new JLabel("Forgot Password?");
        forgotLink.setFont(new Font("Arial", Font.PLAIN, 12));
        forgotLink.setForeground(COLOR_RED_BRAND);
        forgotLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(forgotLink, gbc);

        gbc.gridx    = 0;
        gbc.gridy    = 1;
        gbc.gridwidth = 2;
        gbc.fill     = GridBagConstraints.HORIZONTAL;
        gbc.weightx  = 1.0;
        gbc.insets   = new Insets(0, 0, 0, 0);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBackground(COLOR_INPUT_BG);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.add(passwordField, gbc);

        // Simpan referensi
        passwordFieldRef = passwordField;

        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Koneksi database
    // ─────────────────────────────────────────────────────────────────────────
    private void getConnection() {
        try {
            String url = "jdbc:sqlserver://localhost:1433;databaseName=Uniqlo;encrypt=true;trustServerCertificate=true";
            conn = DriverManager.getConnection(url, "sa", "revanna16");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Gagal konek ke database: " + e.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginPageView view = new LoginPageView();
            view.setVisible(true);
        });
    }
}
