import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Main dashboard (Swing). Four tabs: Vehicles, Rent, Return, History.
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
    private final JLabel previewLabel = new JLabel(" ");
    private final JTable activeTable = new JTable(activeModel);
    private final JLabel overdueLabel = new JLabel("Overdue rentals: 0");
    private int lastOverdueCount = 0;

    public MainFrame(RentalService service) {
        super("Car / Bike Rental System - Admin");
        this.service = service;

        setSize(900, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Vehicles", buildVehiclePanel());
        tabs.addTab("Rent a Vehicle", buildRentPanel());
        tabs.addTab("Return a Vehicle", buildReturnPanel());
        tabs.addTab("History", buildHistoryPanel());
        add(tabs, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.add(overdueLabel);
        add(statusBar, BorderLayout.SOUTH);

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
    //  TAB 1: VEHICLES
    // =====================================================================
    private JPanel buildVehiclePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Show type:"));
        top.add(typeFilter);
        typeFilter.addActionListener(e -> refreshVehicles());
        panel.add(top, BorderLayout.NORTH);

        panel.add(new JScrollPane(new JTable(vehicleModel)), BorderLayout.CENTER);

        JButton addBtn = new JButton("Add New Vehicle");
        addBtn.addActionListener(e -> showAddVehicleDialog());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(addBtn);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
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
    //  TAB 2: RENT
    // =====================================================================
    private JPanel buildRentPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.add(new JLabel("Select vehicle (available only):")); form.add(vehicleCombo);
        form.add(new JLabel("Customer name:"));                    form.add(nameField);
        form.add(new JLabel("Phone (10 digits):"));                form.add(phoneField);
        form.add(new JLabel("Driving licence no.:"));              form.add(licenseField);
        form.add(new JLabel("Start date (yyyy-MM-dd):"));          form.add(startField);
        form.add(new JLabel("Number of days:"));                   form.add(daysField);
        panel.add(form, BorderLayout.NORTH);

        previewLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(previewLabel, BorderLayout.CENTER);

        JButton previewBtn = new JButton("Preview Bill");
        previewBtn.addActionListener(e -> handlePreview());
        JButton rentBtn = new JButton("Confirm Rental");
        rentBtn.addActionListener(e -> handleRent());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        buttons.add(previewBtn);
        buttons.add(rentBtn);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void handlePreview() {
        try {
            Vehicle v = (Vehicle) vehicleCombo.getSelectedItem();
            if (v == null) throw new InvalidInputException("No vehicle available to rent.");
            int days = Integer.parseInt(daysField.getText().trim());
            if (days < 1) throw new InvalidInputException("Days must be at least 1.");
            // POLYMORPHISM: same call, different result depending on the real vehicle type
            previewLabel.setText(String.format("Estimated bill for %d day(s): Rs. %.2f",
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
            previewLabel.setText(" ");
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
    //  TAB 3: RETURN
    // =====================================================================
    private JPanel buildReturnPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(activeTable), BorderLayout.CENTER);

        JButton returnBtn = new JButton("Return Selected Vehicle");
        returnBtn.addActionListener(e -> handleReturn());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(returnBtn);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
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
    //  TAB 4: HISTORY
    // =====================================================================
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(new JTable(historyModel)), BorderLayout.CENTER);
        return panel;
    }

    // =====================================================================
    //  REFRESH HELPERS
    // =====================================================================
    private void refreshAll() {
        refreshVehicles();
        refreshAvailableCombo();
        refreshActive();
        refreshHistory();
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
        overdueLabel.setForeground(count == 0 ? new Color(0, 120, 0) : Color.RED);
        if (count != lastOverdueCount) {      // only redraw the table when something changed
            lastOverdueCount = count;
            refreshActive();
        }
    }
}
