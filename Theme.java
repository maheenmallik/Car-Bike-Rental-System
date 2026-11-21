import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.JTableHeader;

/**
 * Colours, fonts and small helper methods used by the screens, so the look of the
 * whole app can be changed from one place.
 * Colour scheme: red (instead of the teal of the sample project) + the same blue buttons.
 */
public class Theme {

    public static final String BRAND = "OOPS Rentals";

    // ----- colours -----
    public static final Color RED      = new Color(186, 28, 40);    // header bar, main red
    public static final Color DARK_RED = new Color(128, 16, 28);    // hover / selected / table header
    public static final Color CORAL    = new Color(214, 48, 52);    // page title bands
    public static final Color PINK     = new Color(253, 240, 237);  // page background
    public static final Color LINE     = new Color(230, 190, 188);  // borders
    public static final Color BLUE     = new Color(55, 98, 217);    // action buttons
    public static final Color TEXT     = new Color(48, 30, 33);

    // ----- fonts -----
    public static final Font TITLE_FONT = new Font("Times New Roman", Font.BOLD | Font.ITALIC, 26);
    public static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BOLD   = new Font("Segoe UI", Font.BOLD, 14);

    // ----- helper methods -----

    /** Coloured button with white bold text (like the blue buttons in the sample project). */
    public static void styleButton(JButton button, Color background) {
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFont(BOLD);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Text field with a light red border and some padding. */
    public static void styleField(JTextField field) {
        field.setFont(NORMAL);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
    }

    /** Normal label for forms. */
    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(NORMAL);
        label.setForeground(TEXT);
        return label;
    }

    /** Table with a dark red header and taller rows. */
    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(NORMAL);
        table.setGridColor(LINE);
        table.setSelectionBackground(new Color(255, 205, 200));
        table.setSelectionForeground(Color.BLACK);
        JTableHeader header = table.getTableHeader();
        header.setBackground(DARK_RED);
        header.setForeground(Color.WHITE);
        header.setFont(BOLD);
    }

    /** Coloured title band at the top of each page, with a small car + bike picture on the right. */
    public static JPanel pageTitle(String title, String subtitle) {
        JPanel band = new JPanel(new BorderLayout());
        band.setBackground(CORAL);
        band.setPreferredSize(new Dimension(100, 76));

        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        text.setBorder(BorderFactory.createEmptyBorder(10, 24, 8, 0));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 24));
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        s.setForeground(Color.WHITE);
        text.add(t);
        text.add(s);

        band.add(text, BorderLayout.WEST);
        band.add(new JLabel(new ImageIcon("images/banner_art.png")), BorderLayout.EAST);
        return band;
    }

    /** Coloured number box (like the "Current Balance" boxes in the sample project). */
    public static JPanel statBox(String title, JLabel value, Color background) {
        JPanel box = new JPanel(new GridLayout(2, 1));
        box.setBackground(background);
        box.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel t = new JLabel(title);
        t.setFont(NORMAL);
        t.setForeground(Color.WHITE);
        value.setFont(new Font("Segoe UI", Font.BOLD, 30));
        value.setForeground(Color.WHITE);
        box.add(t);
        box.add(value);
        return box;
    }
}
