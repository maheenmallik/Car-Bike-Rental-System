/** INHERITANCE: Car extends Vehicle. Rule: rent = days x rate + flat insurance fee. */
public class Car extends Vehicle {

    private final int seats;
    private final double insuranceFee;

    public Car(String id, String brand, String model, double rentPerDay, int seats, double insuranceFee) {
        super(id, brand, model, rentPerDay);
        this.seats = seats;
        this.insuranceFee = insuranceFee;
    }

    public int getSeats()           { return seats; }
    public double getInsuranceFee() { return insuranceFee; }

    @Override
    public double calculateRent(int days) {
        return getRentPerDay() * days + insuranceFee;
    }

    @Override public String getType()      { return "Car"; }
    @Override public String getDetails()   { return seats + " seats, insurance Rs." + insuranceFee; }
    @Override protected String getExtraData() { return seats + ";" + insuranceFee; }
}
