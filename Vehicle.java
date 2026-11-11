/**
 * ABSTRACTION + ENCAPSULATION
 * A generic "Vehicle" is never rented directly, so the class is abstract.
 * All fields are private; they are changed only through controlled getters/setters.
 * Each subclass decides how its own rent is calculated (POLYMORPHISM).
 */
public abstract class Vehicle {

    private final String id;
    private String brand;
    private String model;
    private double rentPerDay;
    private boolean available = true;

    protected Vehicle(String id, String brand, String model, double rentPerDay) {
        this.id = clean(id);
        this.brand = clean(brand);
        this.model = clean(model);
        this.rentPerDay = rentPerDay;
    }

    /** Commas would break our CSV file format, so we replace them. */
    protected static String clean(String s) {
        return s == null ? "" : s.replace(",", " ").trim();
    }

    // ---------- getters / setters (encapsulation) ----------
    public String getId()            { return id; }
    public String getBrand()         { return brand; }
    public String getModel()         { return model; }
    public double getRentPerDay()    { return rentPerDay; }
    public boolean isAvailable()     { return available; }

    public void setBrand(String brand)          { this.brand = clean(brand); }
    public void setModel(String model)          { this.model = clean(model); }
    public void setRentPerDay(double rentPerDay) { this.rentPerDay = rentPerDay; }
    public void setAvailable(boolean available) { this.available = available; }

    // ---------- abstract methods: every subclass MUST implement ----------
    /** Total rent for the given number of days (each subclass has its own rule). */
    public abstract double calculateRent(int days);

    /** "Car", "Bike" or "Scooter". */
    public abstract String getType();

    /** Short human-readable details for the table. */
    public abstract String getDetails();

    /** Type-specific data saved in the CSV file. */
    protected abstract String getExtraData();

    /** One CSV line: TYPE,id,brand,model,rate,available,extra */
    public String toFileString() {
        return String.join(",", getType(), id, brand, model,
                String.valueOf(rentPerDay), String.valueOf(available), getExtraData());
    }

    @Override
    public String toString() {
        return id + " - " + brand + " " + model + " (" + getType() + ", Rs." + rentPerDay + "/day)";
    }
}
