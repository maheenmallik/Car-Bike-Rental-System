/** INHERITANCE: Scooter extends Vehicle. Rule: rent = days x rate + flat fuel charge. */
public class Scooter extends Vehicle {

    private final double fuelCharge;

    public Scooter(String id, String brand, String model, double rentPerDay, double fuelCharge) {
        super(id, brand, model, rentPerDay);
        this.fuelCharge = fuelCharge;
    }

    public double getFuelCharge() { return fuelCharge; }

    @Override
    public double calculateRent(int days) {
        return getRentPerDay() * days + fuelCharge;
    }

    @Override public String getType()      { return "Scooter"; }
    @Override public String getDetails()   { return "Fuel charge Rs." + fuelCharge; }
    @Override protected String getExtraData() { return String.valueOf(fuelCharge); }
}
