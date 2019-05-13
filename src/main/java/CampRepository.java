import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.ArrivalDate;
import model.AvailabilitySearchParams;
import model.SearchParams;
import model.UnitGridData;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CampRepository {
    HttpClient client;

    BasicCookieStore httpCookieStore;

    public CampRepository() throws IOException {
        httpCookieStore = new BasicCookieStore();
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setDefaultCookieStore(httpCookieStore)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36")
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build());

        // For Fiddler
        //builder.setProxy(new HttpHost("localhost", 8888));
        //System.setProperty("javax.net.ssl.trustStore", "C:\\Windows\\System32\\FiddlerKeystore");
        //System.setProperty("javax.net.ssl.trustStorePassword", "password");

        client = builder.build();

        getSessionCookies();
    }

    public String findCampsitesForDate(String date, String facility) throws IOException {
        UnitGridData ugd = new UnitGridData();
        String[] parts = facility.split(",");
        ugd.FacilityId = parts[1];
        ugd.PlaceId = parts[2];
        ugd.MaximumDates = "20";
        ugd.IsTablet = true;
        ugd.MaximumStayforGrid = 14;

        sessionClears(date);
        setSessionValue(date);

        String result = getUnitGridDataHtmlString(ugd);

        List<String> sites = findSingleDate(result, date);

        if (sites.isEmpty()) {
            return MessageFormat.format("No sites for {0} on {1}", parts[0], date);
        }

        return MessageFormat.format("Sites available for {0} on {1}: {2}", parts[0], date, sites.toString());
    }

    public String findCampsitesForTwoDates(String date, String nextDate, String facility) throws IOException {
        UnitGridData ugd = new UnitGridData();
        String[] parts = facility.split(",");
        ugd.FacilityId = parts[1];
        ugd.PlaceId = parts[2];
        ugd.MaximumDates = "20";
        ugd.IsTablet = true;
        ugd.MaximumStayforGrid = 14;

        sessionClears(date);
        setSessionValue(date);

        String result = getUnitGridDataHtmlString(ugd);

        List<String> sites = findTwoDates(result, date, nextDate);

        if (sites.isEmpty()) {
            return MessageFormat.format("No sites for {0} on {1} and {2}", parts[0], date, nextDate);
        }

        return MessageFormat.format("Sites available for {0} on {1} and {2}: {3}", parts[0], date, nextDate, sites.toString());
    }

    public List<String> findSingleDate(String response, String dateToMatch) {
        List<String> sites = new ArrayList<>();

        Matcher matcher = Pattern.compile("\\d+  is available on " + dateToMatch).matcher(response);
        while (matcher.find()) {
            sites.add(matcher.group().split(" ")[0]);
        }

        return sites;
    }

    public List<String> findTwoDates(String response, String dateToMatch, String secondDateToMatch) {
        List<String> sites = new ArrayList<>();
        List<String> secondSites = new ArrayList<>();

        Matcher matcher = Pattern.compile("\\d+  is available on " + dateToMatch).matcher(response);
        while (matcher.find()) {
            sites.add(matcher.group().split(" ")[0]);
        }

        Matcher secondMatcher = Pattern.compile("\\d+  is available on " + secondDateToMatch).matcher(response);
        while (secondMatcher.find()) {
            secondSites.add(secondMatcher.group().split(" ")[0]);
        }

        sites.retainAll(secondSites);

        return sites;
    }

    public String getUnitGridDataHtmlString(UnitGridData unitGridData) throws IOException {
        String url = "https://reservemn.usedirect.com/MinnesotaWeb/Facilities/AdvanceSearch.aspx/GetUnitGridDataHtmlString";
        return Post(url, unitGridData);
    }

    public String sessionClears(String date) throws IOException {
        String url = "https://reservemn.usedirect.com/MinnesotaWeb/Facilities/AdvanceSearch.aspx/SessionClears";
        ArrivalDate arrivalDate = new ArrivalDate();
        arrivalDate.arrivaldate = date;
        return Post(url, arrivalDate);
    }

    public String setSessionValue(String date) throws IOException {
        String url = "https://reservemn.usedirect.com/MinnesotaWeb/Facilities/AdvanceSearch.aspx/SetSessionvalue";
        AvailabilitySearchParams availabilitySearchParams = new AvailabilitySearchParams();
        availabilitySearchParams.StartDate = date;
        availabilitySearchParams.Nights = "1";
        availabilitySearchParams.CategoryId = "0";
        availabilitySearchParams.ShowOnlyAdaUnits = false;
        availabilitySearchParams.ShowOnlyTentSiteUnits = "false";
        availabilitySearchParams.ShowOnlyRvSiteUnits = "false";
        availabilitySearchParams.MinimumVehicleLength = "0";
        availabilitySearchParams.ShowSiteUnitsName = "0";
        availabilitySearchParams.chooseActivity = "1";
        availabilitySearchParams.IsPremium = false;

        SearchParams searchParams = new SearchParams();
        searchParams.availabilitySearchParams = availabilitySearchParams;

        return Post(url, searchParams);
    }

    public String Post(String url, Object obj) throws IOException {
        HttpPost post = new HttpPost(url);

        StringEntity requestEntity = new StringEntity(serialize(obj, false), ContentType.APPLICATION_JSON);

        post.setEntity(requestEntity);

        HttpResponse response = client.execute(post);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    public List<Cookie> getSessionCookies() throws IOException {
        String url = "https://reservemn.usedirect.com/MinnesotaWeb/Facilities/AdvanceSearch.aspx";

        HttpGet get = new HttpGet(url);

        client.execute(get);

        List<Cookie> cookies = httpCookieStore.getCookies();

        return cookies;
    }

    public String serialize(Object obj, boolean pretty) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        if (pretty) {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        }

        return mapper.writeValueAsString(obj);
    }
}
