import java.time.LocalDate;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * MULTITHREADING: runs in a background thread and re-checks for overdue rentals every few seconds.
 * The result is handed to the GUI through SwingUtilities.invokeLater so the screen is updated
 * safely on the Swing thread and never freezes.
 */
public class OverdueChecker implements Runnable {

    /** Anything that wants to be told about overdue rentals (our MainFrame) implements this. */
    public interface Listener {
        void onOverdueUpdate(List<Rental> overdueRentals);
    }

    private final RentalService service;
    private final Listener listener;
    private final int intervalSeconds;
    private volatile boolean running = true;

    public OverdueChecker(RentalService service, Listener listener, int intervalSeconds) {
        this.service = service;
        this.listener = listener;
        this.intervalSeconds = intervalSeconds;
    }

    @Override
    public void run() {
        while (running) {
            List<Rental> overdue = service.getOverdue(LocalDate.now());
            SwingUtilities.invokeLater(() -> listener.onOverdueUpdate(overdue));
            try {
                Thread.sleep(intervalSeconds * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void stop() {
        running = false;
    }
}
