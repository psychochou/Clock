package euphoria.psycho.clock;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        ;
    }

    public static Integer toInteger(String value) {
        final Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return -1;
    }

    public static Float toFloat(String value) {
        final Pattern pattern = Pattern.compile("[0-9\\.]+");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            return Float.parseFloat(matcher.group());
        }
        return -1f;
    }

    public static String getNumberFormattedQuantityString(Context context, int id, int quantity) {
        final String localizedQuantity = NumberFormat.getInstance().format(quantity);
        return context.getResources().getQuantityString(id, quantity, localizedQuantity);
    }

    public static long getTimeNow() {
        return SystemClock.elapsedRealtime();
    }

    public static boolean isMOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
