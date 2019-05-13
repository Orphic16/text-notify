import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CampSiteSearchServerRunner {
    public static void main(String[] args) {
        new Task().run();
    }

    private static Timer timer = new Timer();

    static class Task extends TimerTask {
        @Override
        public void run() {
            int delay = (10 * 1000 * 60 + new Random().nextInt(10 * 1000 * 60)); // Every 10 to 20 minutes run
            timer.schedule(new Task(), delay);

            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

            Date current = new Date();

            Calendar nextRun = Calendar.getInstance();
            nextRun.setTime(current);
            nextRun.add(Calendar.MILLISECOND, delay);

            System.out.println(MessageFormat.format("{0}. Delay was {1} ms.  Next run: {2}", df.format(current), delay, df.format(nextRun.getTime())));

            runCampingSearches();
        }
    }

    private static void runCampingSearches() {
        runCampingSearch("6/8/2019", "6/9/2019");

        sleep();

        runCampingSearch("6/15/2019", "6/16/2019");

        sleep();

        runCampingSearch("6/22/2019", "6/23/2019");

        sleep();

        runCampingSearch("6/29/2019", "6/30/2019");

        sleep();

        runCampingSearch("7/6/2019", "7/7/2019");

        sleep();

        runCampingSearch("7/13/2019", "7/14/2019");
    }

    private static void runCampingSearch(String date) {
        try {
            CampRepository cr = new CampRepository();
            String search1 = cr.findCampsitesForDate(date, FACILITY.GOOSEBERRY_FALLS);
            String search2 = cr.findCampsitesForDate(date, FACILITY.SPLIT_ROCK);
            System.out.println(search1);
            System.out.println(search2);

            sendIfNotEmpty(search1);
            sendIfNotEmpty(search2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runCampingSearch(String date, String nextDate) {
        try {
            CampRepository cr = new CampRepository();
            String search1 = cr.findCampsitesForTwoDates(date, nextDate, FACILITY.GOOSEBERRY_FALLS);
            String search2 = cr.findCampsitesForTwoDates(date, nextDate, FACILITY.SPLIT_ROCK);
            System.out.println(search1);
            System.out.println(search2);

            sendIfNotEmpty(search1);
            sendIfNotEmpty(search2);
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
            System.out.println(MessageFormat.format("Sleeping {0} ms", sleep));
            Thread.sleep(5000 + new Random().nextInt(5000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
