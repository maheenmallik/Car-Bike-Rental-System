import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The "brain" of the application (business logic, no GUI code).
 * Methods are synchronized because the GUI thread and the OverdueChecker thread both use it.
 */
public class RentalService {

    private final List<Vehicle> vehicles;
    private final List<Rental> rentals;

    public RentalService() {
        vehicles = FileManager.loadVehicles();
        if (vehicles.isEmpty()) {
            seedDefaultVehicles();
        }
        rentals = FileManager.loadRentals(vehicles);
        // Make sure vehicles that are currently rented show as unavailable
        for (Rental r : rentals) {
            if (r.isActive()) {
                r.getVehicle().setAvailable(false);
            }
        }
    }

    private void seedDefaultVehicles() {
        // POLYMORPHISM: one list, three different kinds of objects
        vehicles.add(new Car("C101", "Maruti", "Swift", 1500, 5, 300));
        vehicles.add(new Car("C102", "Hyundai", "Creta", 2500, 5, 500));
        vehicles.add(new Bike("B201", "Royal Enfield", "Classic 350", 700, true));
        vehicles.add(new Bike("B202", "Honda", "Shine", 400, false));
        vehicles.add(new Scooter("S301", "Honda", "Activa", 300, 50));
        vehicles.add(new Scooter("S302", "TVS", "Jupiter", 280, 50));
        FileManager.saveVehicles(vehicles);
    }

    // ====================== VEHICLES ======================

    public synchronized Vehicle findVehicle(String id) {
        for (Vehicle v : vehicles) {
            if (v.getId().equalsIgnoreCase(id)) return v;
        }
        return null;
    }

    public synchronized void addVehicle(Vehicle v) throws InvalidInputException {
        if (v.getId().isEmpty() || v.getBrand().isEmpty() || v.getModel().isEmpty()) {
            throw new InvalidInputException("ID, brand and model cannot be empty.");
        }
        if (v.getRentPerDay() <= 0) {
            throw new InvalidInputException("Rent per day must be greater than zero.");
        }
        if (findVehicle(v.getId()) != null) {
            throw new InvalidInputException("A vehicle with ID " + v.getId() + " already exists.");
        }
        vehicles.add(v);
        FileManager.saveVehicles(vehicles);
    }

    public synchronized List<Vehicle> getAllVehicles() {
        return new ArrayList<>(vehicles);
    }

    public synchronized List<Vehicle> getAvailableVehicles() {
        List<Vehicle> list = new ArrayList<>();
        for (Vehicle v : vehicles) {
            if (v.isAvailable()) list.add(v);
        }
        return list;
    }

    // ====================== RENTING ======================

    public synchronized Rental rentVehicle(String vehicleId, Customer customer, LocalDate start, int days)
            throws VehicleNotAvailableException, InvalidInputException {

        validateCustomer(customer);
        if (days < 1 || days > 90) {
            throw new InvalidInputException("Number of days must be between 1 and 90.");
        }
        if (start == null) {
            throw new InvalidInputException("Start date is required.");
        }
        Vehicle v = findVehicle(vehicleId);
        if (v == null) {
            throw new InvalidInputException("Vehicle not found: " + vehicleId);
        }
        if (!v.isAvailable()) {
            throw new VehicleNotAvailableException(v + " is already rented out.");
        }

        Rental rental = new Rental(nextRentalId(), v, customer, start, days);
        v.setAvailable(false);
        rentals.add(rental);
        saveAll();
        return rental;
    }

    public synchronized Rental returnVehicle(String rentalId, LocalDate returnDate) throws InvalidInputException {
        Rental rental = findRental(rentalId);
        if (rental == null) {
            throw new InvalidInputException("Rental not found: " + rentalId);
        }
        if (!rental.isActive()) {
            throw new InvalidInputException("Rental " + rentalId + " was already returned.");
        }
        if (returnDate.isBefore(rental.getStartDate())) {
            throw new InvalidInputException("Return date cannot be before the start date.");
        }
        rental.markReturned(returnDate);
        rental.getVehicle().setAvailable(true);
        saveAll();
        return rental;
    }

    // ====================== QUERIES ======================

    public synchronized Rental findRental(String rentalId) {
        for (Rental r : rentals) {
            if (r.getRentalId().equalsIgnoreCase(rentalId)) return r;
        }
        return null;
    }

    public synchronized List<Rental> getAllRentals() {
        return new ArrayList<>(rentals);
    }

    public synchronized List<Rental> getActiveRentals() {
        List<Rental> list = new ArrayList<>();
        for (Rental r : rentals) {
            if (r.isActive()) list.add(r);
        }
        return list;
    }

    public synchronized List<Rental> getOverdue(LocalDate today) {
        List<Rental> list = new ArrayList<>();
        for (Rental r : rentals) {
            if (r.isOverdue(today)) list.add(r);
        }
        return list;
    }

    // ====================== HELPERS ======================

    private String nextRentalId() {
        return String.format("R%03d", rentals.size() + 1);
    }

    private void validateCustomer(Customer c) throws InvalidInputException {
        if (c.getName().isEmpty()) {
            throw new InvalidInputException("Customer name cannot be empty.");
        }
        if (!c.getPhone().matches("\\d{10}")) {
            throw new InvalidInputException("Phone number must be exactly 10 digits.");
        }
        if (c.getLicenseNo().isEmpty()) {
            throw new InvalidInputException("Driving licence number is required.");
        }
    }

    private void saveAll() {
        FileManager.saveVehicles(vehicles);
        FileManager.saveRentals(rentals);
    }
}
