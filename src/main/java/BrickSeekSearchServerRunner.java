import repository.BrickSeekRepository;
import util.Utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class BrickSeekSearchServerRunner {
    private static Timer timer = new Timer();

    public static void main(String[] args) {
        new Task().run();
    }

    private static void runBrickSeekSearches() {
        runBrickSeekSearch("446346201", 95.00);

        Utils.sleep();

        runBrickSeekSearch("979038361", 269.00);

        Utils.sleep();

        runBrickSeekSearch("734447693", 154.00);
    }

    private static void runBrickSeekSearch(String sku, Double amount) {
        try {
            BrickSeekRepository bsr = new BrickSeekRepository();
            String search = bsr.findStoresWithLowerPriceBySku(sku, amount);
            System.out.println(search);

            sendIfNotEmpty(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendIfNotEmpty(String searchResult) {
        if (searchResult.contains("No stores for")) {
            return;
        }

        SendMail.send(searchResult);
    }

    static class Task extends TimerTask {
        @Override
        public void run() {
            int delay = (60 * 1000 * 60 + new Random().nextInt(60 * 1000 * 60)); // Every 60 to 120 minutes run
            timer.schedule(new Task(), delay);

            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            Date current = new Date();

            Calendar nextRun = Calendar.getInstance();
            nextRun.setTime(current);
            nextRun.add(Calendar.MILLISECOND, delay);

            System.out.println(MessageFormat.format("{0}. Delay was {1} seconds.  Next run: {2}", df.format(current), delay / 1000.0, df.format(nextRun.getTime())));

            runBrickSeekSearches();
        }
    }
}
