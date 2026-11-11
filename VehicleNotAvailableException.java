/** Custom CHECKED exception: thrown when someone tries to rent a vehicle that is already rented. */
public class VehicleNotAvailableException extends Exception {
    public VehicleNotAvailableException(String message) {
        super(message);
    }
}
