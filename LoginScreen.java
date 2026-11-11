import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** GUI (Swing): simple admin login. Demo credentials: admin / admin123 */
public class LoginScreen extends JFrame {

    private final JTextField userField = new JTextField(15);
    private final JPasswordField passField = new JPasswordField(15);

    public LoginScreen() {
        setTitle("Rental System - Login");
        setSize(360, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        form.add(new JLabel("Username:"));
        form.add(userField);
        form.add(new JLabel("Password:"));
        form.add(passField);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> handleLogin());
        form.add(new JLabel());          // empty cell
        form.add(loginBtn);

        getRootPane().setDefaultButton(loginBtn);   // Enter key = click Login
        add(form);
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
