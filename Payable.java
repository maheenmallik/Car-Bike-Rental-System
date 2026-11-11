/**
 * INTERFACE: any entity that can be paid for must provide these two methods.
 * Rental implements it, so the billing code only depends on this contract.
 */
public interface Payable {
    double getAmountToPay();
    String getPayerId();
}
