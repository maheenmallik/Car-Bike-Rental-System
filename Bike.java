/**
 * INHERITANCE: Bike extends Vehicle.
 * Rule: rent = days x rate. 10% discount for 7+ days. Helmet hire (Rs.20/day) if not included.
 */
public class Bike extends Vehicle {

    private final boolean helmetIncluded;

    public Bike(String id, String brand, String model, double rentPerDay, boolean helmetIncluded) {
        super(id, brand, model, rentPerDay);
        this.helmetIncluded = helmetIncluded;
    }

    public boolean isHelmetIncluded() { return helmetIncluded; }

    @Override
    public double calculateRent(int days) {
        double total = getRentPerDay() * days;
        if (days >= 7) {
            total = total * 0.90;          // weekly discount
        }
        if (!helmetIncluded) {
            total += 20.0 * days;          // helmet hire charge
        }
        return total;
    }

    @Override public String getType()      { return "Bike"; }
    @Override public String getDetails()   { return helmetIncluded ? "Helmet included" : "Helmet extra (Rs.20/day)"; }
    @Override protected String getExtraData() { return String.valueOf(helmetIncluded); }
}
