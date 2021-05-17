import repository.CampRepository;

import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CampSiteSearchServerRunner {
    private static final Timer timer = new Timer();

    public static void main(String[] args) {
        new Task().run();
    }

    private static void runCampingSearches() {
        runCampingSearch("2021-05-21", "2021-05-22");

        sleep();

        runCampingSearch("2021-05-28", "2021-05-29");

        sleep();

        runCampingSearch("2021-06-04", "2021-06-05");
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
            System.out.println(search1);
            sendIfNotEmpty(search1);

            CampRepository cr2 = new CampRepository();
            String search2 = cr2.findCampsitesForTwoDates(date, nextDate, FACILITY.SPLIT_ROCK);
            System.out.println(search2);
            sendIfNotEmpty(search2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendIfNotEmpty(String searchResult) {
        if (searchResult.contains("No sites for")) {
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

            System.out.println(MessageFormat.format("{0}. Delay was {1} seconds.  Next run: {2}", df.format(current), delay / 1000.0, df.format(nextRun.getTime())));

            runCampingSearches();
        }
    }
}
