/** Custom CHECKED exception: thrown for bad user input (empty name, wrong phone, negative days...). */
public class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}
