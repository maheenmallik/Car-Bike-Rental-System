import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * GUI (Swing): admin login. Demo credentials: admin / admin123
 * Same layout idea as the sample project's Sign Up page: coloured panel with a title
 * and a picture on the left, form on the right.
 */
public class LoginScreen extends JFrame {

    private final JTextField userField = new JTextField();
    private final JPasswordField passField = new JPasswordField();

    public LoginScreen() {
        setTitle(Theme.BRAND + " - Login");
        setIconImage(new ImageIcon("images/icon.png").getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        buildUI();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JPanel root = new JPanel(null);                 // null layout: we place things with setBounds
        root.setPreferredSize(new Dimension(800, 500));

        // ----- left panel: red, title, picture -----
        JPanel left = new JPanel(null);
        left.setBackground(Theme.RED);
        left.setBounds(0, 0, 300, 500);

        JLabel brand = new JLabel(Theme.BRAND);
        brand.setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 36));
        brand.setForeground(Color.WHITE);
        brand.setBounds(24, 26, 270, 50);
        left.add(brand);

        JLabel tagline = new JLabel("Cars, bikes and scooters by the day");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(new Color(255, 222, 218));
        tagline.setBounds(26, 76, 270, 22);
        left.add(tagline);

        JLabel picture = new JLabel(new ImageIcon("images/login_art.png"));
        picture.setBounds(0, 100, 300, 400);
        left.add(picture);
        root.add(left);

        // ----- right panel: the login form -----
        JPanel right = new JPanel(null);
        right.setBackground(Color.WHITE);
        right.setBounds(300, 0, 500, 500);

        JLabel title = new JLabel("Admin Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(Theme.RED);
        title.setBounds(60, 70, 380, 46);
        right.add(title);

        JLabel sub = Theme.label("Sign in to manage cars, bikes and scooters");
        sub.setForeground(Color.GRAY);
        sub.setBounds(62, 118, 380, 22);
        right.add(sub);

        JLabel userLabel = Theme.label("Username");
        userLabel.setBounds(60, 176, 200, 22);
        right.add(userLabel);
        Theme.styleField(userField);
        userField.setBounds(60, 202, 380, 40);
        right.add(userField);

        JLabel passLabel = Theme.label("Password");
        passLabel.setBounds(60, 262, 200, 22);
        right.add(passLabel);
        Theme.styleField(passField);
        passField.setBounds(60, 288, 380, 40);
        right.add(passField);

        JButton loginBtn = new JButton("Login");
        Theme.styleButton(loginBtn, Theme.RED);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setBounds(60, 356, 380, 44);
        loginBtn.addActionListener(e -> handleLogin());
        right.add(loginBtn);
        root.add(right);

        getRootPane().setDefaultButton(loginBtn);   // Enter key = click Login
        setContentPane(root);
    }

    private void handleLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if (user.equals("admin") && pass.equals("admin123")) {
            dispose();
            new MainFrame(new RentalService()).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Login failed", JOptionPane.ERROR_MESSAGE);
            passField.setText("");
        }
    }
}
