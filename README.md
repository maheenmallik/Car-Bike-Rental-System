Car / Bike Rental System (Java, Swing, file handling)

Run
javac *.java
java Main

Login: admin / admin123 (needs JDK 11+) Data files vehicles.csv and rentals.csv are created automatically in the folder you run from.

Where each OOP concept lives (for your report / viva)
Concept	Where
Abstraction	Vehicle (abstract class with abstract calculateRent, getType, getDetails)
Inheritance	Car, Bike, Scooter extend Vehicle
Polymorphism	Rental.getBaseRent() calls vehicle.calculateRent(days); each subclass answers differently. RentalService stores all types in one List<Vehicle>
Encapsulation	Private fields + getters/setters in Vehicle, Customer, Rental
Interface	Payable (implemented by Rental), OverdueChecker.Listener (implemented by MainFrame)
Exception handling	Custom VehicleNotAvailableException, InvalidInputException; try-catch around all file I/O and GUI input
File handling	FileManager (CSV read/write)
Multithreading	OverdueChecker implements Runnable, updates GUI via SwingUtilities.invokeLater
GUI	LoginScreen, MainFrame (4 tabs)
Pricing rules
Car: days x rate + insurance fee
Bike: days x rate, 10% off for 7+ days, +Rs.20/day if helmet not included
Scooter: days x rate + fuel charge
Late fine: 50% of the daily rate for every day after the due date
Demo tip (shows the thread working)

In "Rent a Vehicle", set the start date to 5 days ago and days = 2. Within 5 seconds the status bar turns red ("Overdue rentals: 1") and the Return tab shows OVERDUE. Returning it today adds a late fine.

