import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Main dashboard (Swing). Four pages: Vehicles, Rent, Return, History.
 * A red header bar with buttons switches between the pages (CardLayout).
 * Implements OverdueChecker.Listener so the background thread can update the status bar.
 * This class only handles screens; all real logic lives in RentalService.
 */
public class MainFrame extends JFrame implements OverdueChecker.Listener {

    private final RentalService service;
    private final OverdueChecker checker;

    // ----- table models -----
    private final DefaultTableModel vehicleModel =
            newModel("ID", "Type", "Brand", "Model", "Rate/Day (Rs.)", "Status", "Details");
    private final DefaultTableModel activeModel =
            newModel("Rental ID", "Vehicle", "Customer", "Start", "Due", "Status");
    private final DefaultTableModel historyModel =
            newModel("Rental ID", "Vehicle", "Customer", "Start", "Returned", "Amount (Rs.)");

    // ----- widgets we need to read/update later -----
    private final JComboBox<String> typeFilter =
            new JComboBox<>(new String[]{"All", "Car", "Bike", "Scooter"});
    private final DefaultComboBoxModel<Vehicle> vehicleChoice = new DefaultComboBoxModel<>();
    private final JComboBox<Vehicle> vehicleCombo = new JComboBox<>(vehicleChoice);
    private final JTextField nameField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField licenseField = new JTextField();
    private final JTextField startField = new JTextField(LocalDate.now().toString());
    private final JTextField daysField = new JTextField("1");
    private final JLabel previewLabel = new JLabel();
    private final JTable activeTable = new JTable(activeModel);
    private final JLabel overdueLabel = new JLabel("Overdue rentals: 0");
    private int lastOverdueCount = 0;

    // numbers shown in the coloured boxes on the Vehicles page
    private final JLabel totalValue = new JLabel("0");
    private final JLabel availableValue = new JLabel("0");
    private final JLabel rentedValue = new JLabel("0");
    private final JLabel overdueValue = new JLabel("0");

    // header buttons + page switching
    private final String[] pageNames = {"Vehicles", "Rent a Vehicle", "Return a Vehicle", "History"};
    private final JButton[] navButtons = new JButton[4];
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private String currentPage = "Vehicles";

    public MainFrame(RentalService service) {
        super(Theme.BRAND + " - Admin");
        this.service = service;

        setIconImage(new ImageIcon("images/icon.png").getImage());
        setSize(1020, 690);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardPanel.add(buildVehiclePanel(), pageNames[0]);
        cardPanel.add(buildRentPanel(), pageNames[1]);
        cardPanel.add(buildReturnPanel(), pageNames[2]);
        cardPanel.add(buildHistoryPanel(), pageNames[3]);

        add(buildHeader(), BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        // status bar at the bottom
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(Theme.DARK_RED);
        overdueLabel.setFont(Theme.BOLD);
        overdueLabel.setForeground(Color.WHITE);
        statusBar.add(overdueLabel);
        add(statusBar, BorderLayout.SOUTH);

        showPage(pageNames[0]);
        clearPreview();
        refreshAll();

        // Start the background overdue-checking thread (checks every 5 seconds)
        checker = new OverdueChecker(service, this, 5);
        Thread t = new Thread(checker, "OverdueChecker");
        t.setDaemon(true);
        t.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                checker.stop();
            }
        });
    }

    // =====================================================================
    //  HEADER (red bar with the name and the page buttons)
    // =====================================================================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.RED);
        header.setPreferredSize(new Dimension(100, 62));

        JLabel brand = new JLabel(Theme.BRAND, new ImageIcon("images/brand_car.png"), JLabel.LEFT);
        brand.setFont(Theme.TITLE_FONT);
        brand.setForeground(Color.WHITE);
        brand.setIconTextGap(10);
        brand.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 24));
        header.add(brand, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setBackground(Theme.RED);
        for (int i = 0; i < pageNames.length; i++) {
            final JButton b = new JButton(pageNames[i]);
            final String page = pageNames[i];
            b.setBackground(Theme.RED);
            b.setForeground(Color.WHITE);
            b.setFont(Theme.BOLD);
            b.setBorder(null);
            b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(138, 62));
            b.addActionListener(e -> showPage(page));
            // darker red while the mouse is over the button
            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    b.setBackground(Theme.DARK_RED);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    updateHeaderColours();
                }
            });
            navButtons[i] = b;
            buttons.add(b);
        }
        header.add(buttons, BorderLayout.CENTER);

        JButton logout = new JButton("Logout");
        logout.setBackground(Theme.RED);
        logout.setForeground(Color.WHITE);
        logout.setFont(Theme.BOLD);
        logout.setFocusPainted(false);
        logout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(6, 18, 6, 18)));
        logout.addActionListener(e -> handleLogout());
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 13));
        logoutPanel.setBackground(Theme.RED);
        logoutPanel.add(logout);
        header.add(logoutPanel, BorderLayout.EAST);
        return header;
    }

    private void showPage(String page) {
        currentPage = page;
        cards.show(cardPanel, page);
        updateHeaderColours();
    }

    /** The button of the page that is open is dark red, the others are normal red. */
    private void updateHeaderColours() {
        for (JButton b : navButtons) {
            b.setBackground(b.getText().equals(currentPage) ? Theme.DARK_RED : Theme.RED);
        }
    }

    private void handleLogout() {
        checker.stop();
        dispose();
        new LoginScreen().setVisible(true);
    }

    /** One page = coloured title band on top + content below, on a light pink background. */
    private JPanel newPage(String title, String subtitle, JPanel content) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Theme.PINK);
        page.add(Theme.pageTitle(title, subtitle), BorderLayout.NORTH);
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(18, 26, 18, 26));
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JScrollPane tableScroll(JTable table) {
        Theme.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        return scroll;
    }

    // =====================================================================
    //  PAGE 1: VEHICLES
    // =====================================================================
    private JPanel buildVehiclePanel() {
        JPanel content = new JPanel(new BorderLayout(0, 12));

        // four coloured number boxes
        JPanel boxes = new JPanel(new GridLayout(1, 4, 14, 0));
        boxes.setOpaque(false);
        boxes.add(Theme.statBox("Total fleet", totalValue, new Color(186, 28, 40)));
        boxes.add(Theme.statBox("Available now", availableValue, new Color(226, 70, 66)));
        boxes.add(Theme.statBox("On rent", rentedValue, new Color(140, 20, 34)));
        boxes.add(Theme.statBox("Overdue", overdueValue, new Color(70, 50, 56)));

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filter.setOpaque(false);
        JLabel show = Theme.label("Show type:");
        show.setFont(Theme.BOLD);
        typeFilter.setFont(Theme.NORMAL);
        typeFilter.setBackground(Color.WHITE);
        typeFilter.addActionListener(e -> refreshVehicles());
        filter.add(show);
        filter.add(typeFilter);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(boxes, BorderLayout.NORTH);
        top.add(filter, BorderLayout.SOUTH);
        content.add(top, BorderLayout.NORTH);

        content.add(tableScroll(new JTable(vehicleModel)), BorderLayout.CENTER);

        JButton addBtn = new JButton("Add New Vehicle");
        Theme.styleButton(addBtn, Theme.BLUE);
        addBtn.addActionListener(e -> showAddVehicleDialog());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(addBtn);
        content.add(bottom, BorderLayout.SOUTH);

        return newPage("Fleet", "All cars, bikes and scooters and whether they are available", content);
    }

    private void showAddVehicleDialog() {
        JComboBox<String> type = new JComboBox<>(new String[]{"Car", "Bike", "Scooter"});
        JTextField id = new JTextField();
        JTextField brand = new JTextField();
        JTextField model = new JTextField();
        JTextField rate = new JTextField();
        JLabel label1 = new JLabel();
        JLabel label2 = new JLabel();
        JTextField extra1 = new JTextField();
        JTextField extra2 = new JTextField();

        // Changing the type changes which extra fields are needed
        Runnable updateLabels = () -> {
            String t = (String) type.getSelectedItem();
            if ("Car".equals(t)) {
                label1.setText("Seats:");
                label2.setText("Insurance fee (Rs.):");
                extra2.setEnabled(true);
            } else if ("Bike".equals(t)) {
                label1.setText("Helmet included (yes/no):");
                label2.setText("-");
                extra2.setEnabled(false);
            } else {
                label1.setText("Fuel charge (Rs.):");
                label2.setText("-");
                extra2.setEnabled(false);
            }
        };
        type.addActionListener(e -> updateLabels.run());
        updateLabels.run();

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Type:"));        form.add(type);
        form.add(new JLabel("Vehicle ID:"));  form.add(id);
        form.add(new JLabel("Brand:"));       form.add(brand);
        form.add(new JLabel("Model:"));       form.add(model);
        form.add(new JLabel("Rent per day (Rs.):")); form.add(rate);
        form.add(label1);                     form.add(extra1);
        form.add(label2);                     form.add(extra2);

        int choice = JOptionPane.showConfirmDialog(this, form, "Add Vehicle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) return;

        try {
            double r = Double.parseDouble(rate.getText().trim());
            Vehicle v;
            switch ((String) type.getSelectedItem()) {
                case "Car":
                    v = new Car(id.getText(), brand.getText(), model.getText(), r,
                            Integer.parseInt(extra1.getText().trim()),
                            Double.parseDouble(extra2.getText().trim()));
                    break;
                case "Bike":
                    v = new Bike(id.getText(), brand.getText(), model.getText(), r,
                            extra1.getText().trim().equalsIgnoreCase("yes"));
                    break;
                default:
                    v = new Scooter(id.getText(), brand.getText(), model.getText(), r,
                            Double.parseDouble(extra1.getText().trim()));
            }
            service.addVehicle(v);
            refreshAll();
            JOptionPane.showMessageDialog(this, "Vehicle added: " + v);
        } catch (NumberFormatException ex) {
            showError("Please enter valid numbers for rate, seats and charges.");
        } catch (InvalidInputException ex) {
            showError(ex.getMessage());
        }
    }

    // =====================================================================
    //  PAGE 2: RENT
    // =====================================================================
    private JPanel buildRentPanel() {
        // white card with the form
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 14));
        form.setOpaque(false);
        form.add(Theme.label("Select vehicle (available only):")); form.add(vehicleCombo);
        form.add(Theme.label("Customer name:"));                    form.add(nameField);
        form.add(Theme.label("Phone (10 digits):"));                form.add(phoneField);
        form.add(Theme.label("Driving licence no.:"));              form.add(licenseField);
        form.add(Theme.label("Start date (yyyy-MM-dd):"));          form.add(startField);
        form.add(Theme.label("Number of days:"));                   form.add(daysField);
        vehicleCombo.setFont(Theme.NORMAL);
        vehicleCombo.setBackground(Color.WHITE);
        Theme.styleField(nameField);
        Theme.styleField(phoneField);
        Theme.styleField(licenseField);
        Theme.styleField(startField);
        Theme.styleField(daysField);

        previewLabel.setHorizontalAlignment(JLabel.CENTER);
        previewLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)));

        JButton previewBtn = new JButton("Preview Bill");
        Theme.styleButton(previewBtn, Theme.BLUE);
        previewBtn.addActionListener(e -> handlePreview());
        JButton rentBtn = new JButton("Confirm Rental");
        Theme.styleButton(rentBtn, Theme.BLUE);
        rentBtn.addActionListener(e -> handleRent());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttons.setOpaque(false);
        buttons.add(previewBtn);
        buttons.add(rentBtn);

        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                BorderFactory.createEmptyBorder(20, 24, 16, 24)));
        card.add(form, BorderLayout.NORTH);
        card.add(previewLabel, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(card, BorderLayout.NORTH);

        // big faint picture on the free space at the right
        JLabel art = new JLabel(new ImageIcon("images/rent_art.png"));
        art.setVerticalAlignment(JLabel.BOTTOM);

        JPanel content = new JPanel(new BorderLayout(20, 0));
        content.add(left, BorderLayout.CENTER);
        content.add(art, BorderLayout.EAST);

        return newPage("Rent a vehicle", "Choose an available vehicle and enter the customer's details", content);
    }

    private void clearPreview() {
        previewLabel.setFont(Theme.NORMAL);
        previewLabel.setForeground(Color.GRAY);
        previewLabel.setText("The estimated bill will be shown here");
    }

    private void showPreview(String text) {
        previewLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        previewLabel.setForeground(Theme.RED);
        previewLabel.setText(text);
    }

    private void handlePreview() {
        try {
            Vehicle v = (Vehicle) vehicleCombo.getSelectedItem();
            if (v == null) throw new InvalidInputException("No vehicle available to rent.");
            int days = Integer.parseInt(daysField.getText().trim());
            if (days < 1) throw new InvalidInputException("Days must be at least 1.");
            // POLYMORPHISM: same call, different result depending on the real vehicle type
            showPreview(String.format("Estimated bill for %d day(s): Rs. %.2f",
                    days, v.calculateRent(days)));
        } catch (NumberFormatException ex) {
            showError("Number of days must be a whole number.");
        } catch (InvalidInputException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleRent() {
        try {
            Vehicle v = (Vehicle) vehicleCombo.getSelectedItem();
            if (v == null) throw new InvalidInputException("No vehicle available to rent.");

            int days = Integer.parseInt(daysField.getText().trim());
            LocalDate start = LocalDate.parse(startField.getText().trim());
            Customer c = new Customer(nameField.getText(), phoneField.getText(), licenseField.getText());

            Rental r = service.rentVehicle(v.getId(), c, start, days);

            JOptionPane.showMessageDialog(this, String.format(
                    "Rental confirmed!%nRental ID: %s%nVehicle: %s%nDue date: %s%nEstimated bill: Rs. %.2f",
                    r.getRentalId(), r.getVehicle(), r.getDueDate(), r.getBaseRent()));

            nameField.setText("");
            phoneField.setText("");
            licenseField.setText("");
            daysField.setText("1");
            startField.setText(LocalDate.now().toString());
            clearPreview();
            refreshAll();
        } catch (VehicleNotAvailableException | InvalidInputException ex) {
            showError(ex.getMessage());
        } catch (NumberFormatException ex) {
            showError("Number of days must be a whole number.");
        } catch (DateTimeParseException ex) {
            showError("Start date must be in the format yyyy-MM-dd (e.g. 2026-11-07).");
        }
    }

    // =====================================================================
    //  PAGE 3: RETURN
    // =====================================================================
    private JPanel buildReturnPanel() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.add(tableScroll(activeTable), BorderLayout.CENTER);

        JLabel hint = Theme.label("Select a rental in the table, then press the button.");
        hint.setForeground(Color.GRAY);
        JButton returnBtn = new JButton("Return Selected Vehicle");
        Theme.styleButton(returnBtn, Theme.BLUE);
        returnBtn.addActionListener(e -> handleReturn());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(hint, BorderLayout.WEST);
        bottom.add(returnBtn, BorderLayout.EAST);
        content.add(bottom, BorderLayout.SOUTH);

        return newPage("Return a vehicle", "Close a rental and see the final bill", content);
    }

    private void handleReturn() {
        int row = activeTable.getSelectedRow();
        if (row < 0) {
            showError("Please select a rental from the table first.");
            return;
        }
        String rentalId = (String) activeModel.getValueAt(row, 0);
        try {
            LocalDate today = LocalDate.now();
            Rental r = service.returnVehicle(rentalId, today);

            JOptionPane.showMessageDialog(this, String.format(
                    "Vehicle returned: %s%n%nBase rent:  Rs. %.2f%nLate days:  %d%nLate fine:  Rs. %.2f%n"
                            + "-----------------------------%nTOTAL:      Rs. %.2f",
                    r.getVehicle(), r.getBaseRent(), r.getLateDays(today),
                    r.getLateFine(today), r.getAmountPaid()),
                    "Final Bill - " + r.getRentalId(), JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        } catch (InvalidInputException ex) {
            showError(ex.getMessage());
        }
    }

    // =====================================================================
    //  PAGE 4: HISTORY
    // =====================================================================
    private JPanel buildHistoryPanel() {
        JPanel content = new JPanel(new BorderLayout());
        content.add(tableScroll(new JTable(historyModel)), BorderLayout.CENTER);
        return newPage("Rental history", "All completed rentals and the amount paid", content);
    }

    // =====================================================================
    //  REFRESH HELPERS
    // =====================================================================
    private void refreshAll() {
        refreshVehicles();
        refreshAvailableCombo();
        refreshActive();
        refreshHistory();
        refreshNumbers();
    }

    /** Updates the four coloured boxes on the Vehicles page. */
    private void refreshNumbers() {
        List<Vehicle> all = service.getAllVehicles();
        int available = service.getAvailableVehicles().size();
        totalValue.setText(String.valueOf(all.size()));
        availableValue.setText(String.valueOf(available));
        rentedValue.setText(String.valueOf(all.size() - available));
        overdueValue.setText(String.valueOf(service.getOverdue(LocalDate.now()).size()));
    }

    private void refreshVehicles() {
        vehicleModel.setRowCount(0);
        String filter = (String) typeFilter.getSelectedItem();
        for (Vehicle v : service.getAllVehicles()) {
            if (filter == null || filter.equals("All") || filter.equals(v.getType())) {
                vehicleModel.addRow(new Object[]{v.getId(), v.getType(), v.getBrand(), v.getModel(),
                        v.getRentPerDay(), v.isAvailable() ? "Available" : "Rented", v.getDetails()});
            }
        }
    }

    private void refreshAvailableCombo() {
        vehicleChoice.removeAllElements();
        for (Vehicle v : service.getAvailableVehicles()) {
            vehicleChoice.addElement(v);
        }
    }

    private void refreshActive() {
        activeModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (Rental r : service.getActiveRentals()) {
            activeModel.addRow(new Object[]{r.getRentalId(), r.getVehicle(), r.getCustomer(),
                    r.getStartDate(), r.getDueDate(), r.isOverdue(today) ? "OVERDUE" : "On time"});
        }
    }

    private void refreshHistory() {
        historyModel.setRowCount(0);
        for (Rental r : service.getAllRentals()) {
            if (!r.isActive()) {
                historyModel.addRow(new Object[]{r.getRentalId(), r.getVehicle(), r.getCustomer(),
                        r.getStartDate(), r.getReturnDate(), String.format("%.2f", r.getAmountPaid())});
            }
        }
    }

    private static DefaultTableModel newModel(String... columns) {
        // Cells are read-only so users cannot edit data by typing in the table
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // =====================================================================
    //  CALLED BY THE BACKGROUND THREAD (already on the Swing thread)
    // =====================================================================
    @Override
    public void onOverdueUpdate(List<Rental> overdueRentals) {
        int count = overdueRentals.size();
        overdueLabel.setText("Overdue rentals: " + count);
        // white when there are none, yellow when something is overdue
        overdueLabel.setForeground(count == 0 ? Color.WHITE : new Color(255, 205, 70));
        overdueValue.setText(String.valueOf(count));
        if (count != lastOverdueCount) {      // only redraw the table when something changed
            lastOverdueCount = count;
            refreshActive();
        }
    }
}
