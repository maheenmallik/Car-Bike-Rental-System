import javax.swing.SwingUtilities;

/** Program entry point: opens the login screen on the Swing thread. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}
