package util;

import java.text.MessageFormat;
import java.util.Random;

public class Utils {
    public static void sleep() {
        try {
            int sleep = 5000 + new Random().nextInt(5000);
            System.out.println(MessageFormat.format("Sleeping {0} seconds", sleep / 1000.0));
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
