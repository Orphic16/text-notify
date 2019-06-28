import repository.BrickSeekRepository;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class BrickSeekSearchServerRunner {
    public static void main(String[] args) {
        new Task().run();
    }

    private static Timer timer = new Timer();

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

    private static void runBrickSeekSearches() {
        runBrickSeekSearch("874425416", 599.00);

        sleep();

        runBrickSeekSearch("314572057", 649.00);

        sleep();

        runBrickSeekSearch("115832568", 499.00);
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
        if (searchResult.contains("No")) {
            return;
        }

        SendMail.send(searchResult);
    }

    private static void sleep() {
        try {
            int sleep = 5000 + new Random().nextInt(5000);
            System.out.println(MessageFormat.format("Sleeping {0} seconds", sleep / 1000.0));
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
