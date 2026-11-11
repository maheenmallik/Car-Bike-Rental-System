/** Simple data class for the person renting a vehicle (fields private, read through getters). */
public class Customer {

    private final String name;
    private final String phone;
    private final String licenseNo;

    public Customer(String name, String phone, String licenseNo) {
        this.name = clean(name);
        this.phone = clean(phone);
        this.licenseNo = clean(licenseNo);
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace(",", " ").trim();
    }

    public String getName()      { return name; }
    public String getPhone()     { return phone; }
    public String getLicenseNo() { return licenseNo; }

    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}
