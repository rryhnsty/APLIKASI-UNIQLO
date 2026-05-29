package Program;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.*;

public class SignUpView extends JDialog {

    private static final Color COLOR_RED_BRAND    = new Color(200, 30, 40);
    private static final Color COLOR_RED_HOVER    = new Color(160, 20, 30);
    private static final Color COLOR_TEXT_GRAY    = new Color(100, 100, 100);
    private static final Color COLOR_INPUT_BG     = new Color(240, 240, 240);
    private static final Color COLOR_INPUT_BORDER = new Color(220, 220, 220);

    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private Connection conn;

    public SignUpView(JFrame parent) {
        super(parent, "Sign Up", true);
        setSize(500, 550);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        gbc.insets = new Insets(40, 50, 5, 50);
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 50, 30, 50);
        JLabel subLabel = new JLabel("Fill in the details below to register.");
        subLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subLabel.setForeground(COLOR_TEXT_GRAY);
        add(subLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 50, 15, 50);
        add(buildLabeledField("Email", false), gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 50, 15, 50);
        add(buildLabeledField("Password", true), gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 50, 30, 50);
        add(buildLabeledField("Confirm Password", true), gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 50, 15, 50);
        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Arial", Font.BOLD, 16));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(COLOR_RED_BRAND);
        registerBtn.setFocusPainted(false);
        registerBtn.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        registerBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { registerBtn.setBackground(COLOR_RED_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { registerBtn.setBackground(COLOR_RED_BRAND); }
        });
        registerBtn.addActionListener(e -> handleRegister());
        add(registerBtn, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 50, 40, 50);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel backLabel = new JLabel("<html>Already have an account? <span style='color: rgb(200,30,40);'>Sign In</span></html>");
        backLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        backLabel.setForeground(COLOR_TEXT_GRAY);
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dispose(); }
        });
        add(backLabel, gbc);

        getConnection();
    }

    private JPanel buildLabeledField(String labelText, boolean isPassword) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        panel.add(label, BorderLayout.NORTH);

        JComponent field;
        if (isPassword) {
            JPasswordField pf = new JPasswordField();
            pf.setFont(new Font("Arial", Font.PLAIN, 15));
            pf.setBackground(COLOR_INPUT_BG);
            pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            if (labelText.equals("Password")) passwordField = pf;
            else if (labelText.equals("Confirm Password")) confirmPasswordField = pf;
            field = pf;
        } else {
            JTextField tf = new JTextField();
            tf.setFont(new Font("Arial", Font.PLAIN, 15));
            tf.setBackground(COLOR_INPUT_BG);
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_INPUT_BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            emailField = tf;
            field = tf;
        }
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void handleRegister() {

    String email = emailField.getText().trim();
    String password = new String(passwordField.getPassword());
    String confirm = new String(confirmPasswordField.getPassword());

    if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Semua field harus diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (!password.equals(confirm)) {
        JOptionPane.showMessageDialog(this, "Password dan Confirm Password tidak cocok.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (conn == null) {
        JOptionPane.showMessageDialog(this, "Koneksi database gagal.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        String lastId = "C000";

        String getIdSql = "SELECT TOP 1 id_customer FROM Customer ORDER BY id_customer DESC";

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(getIdSql);

            if (rs.next()) {
                lastId = rs.getString("id_customer");
            }

            int angka = Integer.parseInt(lastId.substring(1));

            angka++;

            String newId = String.format("C%03d", angka);

            String sql = "INSERT INTO Customer VALUES (?, NULL, ?, ?, NULL, NULL)";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, newId);
            ps.setString(2, email);
            ps.setString(3, password);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this,
                "Akun berhasil dibuat!\nID Customer: " + newId,
                "Sukses",
                JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void getConnection() {
        try {
            String url = "jdbc:sqlserver://localhost:1433;databaseName=Uniqlo;encrypt=true;trustServerCertificate=true";
            conn = DriverManager.getConnection(url, "sa", "revanna16");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}