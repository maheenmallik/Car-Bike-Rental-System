import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FILE HANDLING: saves/loads vehicles and rentals as simple CSV text files.
 * Every I/O operation is wrapped in try-catch (EXCEPTION HANDLING) so a missing or
 * damaged file never crashes the program.
 */
public class FileManager {

    public static final String VEHICLE_FILE = "vehicles.csv";
    public static final String RENTAL_FILE  = "rentals.csv";

    // ====================== VEHICLES ======================

    public static List<Vehicle> loadVehicles() {
        List<Vehicle> list = new ArrayList<>();
        Path path = Paths.get(VEHICLE_FILE);
        if (!Files.exists(path)) {
            return list;                      // first run: nothing saved yet
        }
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    list.add(parseVehicle(line));
                } catch (InvalidInputException | RuntimeException e) {
                    System.err.println("Skipping bad vehicle line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read " + VEHICLE_FILE + ": " + e.getMessage());
        }
        return list;
    }

    public static void saveVehicles(List<Vehicle> vehicles) {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(VEHICLE_FILE))) {
            for (Vehicle v : vehicles) {
                bw.write(v.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not write " + VEHICLE_FILE + ": " + e.getMessage());
        }
    }

    /** Line format: TYPE,id,brand,model,rate,available,extra */
    private static Vehicle parseVehicle(String line) throws InvalidInputException {
        String[] p = line.split(",", -1);
        if (p.length < 7) throw new InvalidInputException("Incomplete vehicle line");

        String type = p[0];
        double rate = Double.parseDouble(p[4]);
        boolean available = Boolean.parseBoolean(p[5]);
        String extra = p[6];

        Vehicle v;
        switch (type) {
            case "Car": {
                String[] e = extra.split(";");
                v = new Car(p[1], p[2], p[3], rate, Integer.parseInt(e[0]), Double.parseDouble(e[1]));
                break;
            }
            case "Bike":
                v = new Bike(p[1], p[2], p[3], rate, Boolean.parseBoolean(extra));
                break;
            case "Scooter":
                v = new Scooter(p[1], p[2], p[3], rate, Double.parseDouble(extra));
                break;
            default:
                throw new InvalidInputException("Unknown vehicle type: " + type);
        }
        v.setAvailable(available);
        return v;
    }

    // ====================== RENTALS ======================

    public static List<Rental> loadRentals(List<Vehicle> vehicles) {
        Map<String, Vehicle> byId = new HashMap<>();
        for (Vehicle v : vehicles) byId.put(v.getId(), v);

        List<Rental> list = new ArrayList<>();
        Path path = Paths.get(RENTAL_FILE);
        if (!Files.exists(path)) {
            return list;
        }
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    // rentalId,vehicleId,name,phone,license,start,days,returnDate,amountPaid
                    String[] p = line.split(",", -1);
                    Vehicle v = byId.get(p[1]);
                    if (v == null) continue;      // vehicle was deleted; ignore this rental
                    Customer c = new Customer(p[2], p[3], p[4]);
                    Rental r = new Rental(p[0], v, c, LocalDate.parse(p[5]), Integer.parseInt(p[6]));
                    if (!p[7].equals("-")) {
                        r.restoreReturn(LocalDate.parse(p[7]), Double.parseDouble(p[8]));
                    }
                    list.add(r);
                } catch (RuntimeException e) {
                    System.err.println("Skipping bad rental line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read " + RENTAL_FILE + ": " + e.getMessage());
        }
        return list;
    }

    public static void saveRentals(List<Rental> rentals) {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(RENTAL_FILE))) {
            for (Rental r : rentals) {
                bw.write(r.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not write " + RENTAL_FILE + ": " + e.getMessage());
        }
    }
}
