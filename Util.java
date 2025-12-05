import java.text.DecimalFormat;

/**
 * Small utility helpers shared across classes.
 */
public class Util {
    /** Format a numeric amount as a currency string for display. Uses ASCII "PHP" prefix. */
    public static String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return "PHP " + df.format(amount);
    }
}
