import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * One rental transaction. Implements the Payable INTERFACE.
 * Bill = vehicle.calculateRent(days)  [polymorphic call]  +  late fine.
 */
public class Rental implements Payable {

    /** Late fine per extra day = 50% of the vehicle's daily rate. */
    public static final double LATE_FINE_RATE = 0.5;

    private final String rentalId;
    private final Vehicle vehicle;
    private final Customer customer;
    private final LocalDate startDate;
    private final int days;
    private LocalDate returnDate;      // null while the rental is still active
    private double amountPaid;

    public Rental(String rentalId, Vehicle vehicle, Customer customer, LocalDate startDate, int days) {
        this.rentalId = rentalId;
        this.vehicle = vehicle;
        this.customer = customer;
        this.startDate = startDate;
        this.days = days;
    }

    // ---------- getters ----------
    public String getRentalId()     { return rentalId; }
    public Vehicle getVehicle()     { return vehicle; }
    public Customer getCustomer()   { return customer; }
    public LocalDate getStartDate() { return startDate; }
    public int getDays()            { return days; }
    public LocalDate getReturnDate(){ return returnDate; }
    public double getAmountPaid()   { return amountPaid; }
    public LocalDate getDueDate()   { return startDate.plusDays(days); }
    public boolean isActive()       { return returnDate == null; }

    // ---------- business logic ----------
    public boolean isOverdue(LocalDate today) {
        return isActive() && today.isAfter(getDueDate());
    }

    public long getLateDays(LocalDate endDate) {
        return Math.max(0, ChronoUnit.DAYS.between(getDueDate(), endDate));
    }

    public double getBaseRent() {
        return vehicle.calculateRent(days);   // POLYMORPHISM: Car/Bike/Scooter each answer differently
    }

    public double getLateFine(LocalDate endDate) {
        return getLateDays(endDate) * vehicle.getRentPerDay() * LATE_FINE_RATE;
    }

    public double getBill(LocalDate endDate) {
        return getBaseRent() + getLateFine(endDate);
    }

    /** Close the rental and lock in the final amount. */
    public void markReturned(LocalDate date) {
        this.returnDate = date;
        this.amountPaid = getBill(date);
    }

    /** Used only when loading a finished rental from the file. */
    public void restoreReturn(LocalDate date, double amountPaid) {
        this.returnDate = date;
        this.amountPaid = amountPaid;
    }

    // ---------- Payable interface ----------
    @Override
    public double getAmountToPay() {
        return isActive() ? getBill(LocalDate.now()) : amountPaid;
    }

    @Override
    public String getPayerId() {
        return customer.getLicenseNo();
    }

    /** One CSV line for rentals.csv */
    public String toFileString() {
        return String.join(",", rentalId, vehicle.getId(), customer.getName(), customer.getPhone(),
                customer.getLicenseNo(), startDate.toString(), String.valueOf(days),
                returnDate == null ? "-" : returnDate.toString(), String.valueOf(amountPaid));
    }
}
