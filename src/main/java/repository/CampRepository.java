package repository;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.*;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CampRepository {
    private HttpClient client;

    public CampRepository() {
        BasicCookieStore httpCookieStore = new BasicCookieStore();
        HttpClientBuilder builder = HttpClientBuilder.create()
                .setDefaultCookieStore(httpCookieStore)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36")
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build());

        // For Fiddler
        // keytool.exe -import -file C:\Users\smurf\Desktop\FiddlerRoot.cer -keystore C:\Windows\System32\FiddlerKeystore -alias Fiddler -storepass password
        // builder.setProxy(new HttpHost("localhost", 8888));
        // System.setProperty("javax.net.ssl.trustStore", "C:\\Windows\\System32\\FiddlerKeystore");
        // System.setProperty("javax.net.ssl.trustStorePassword", "password");

        client = builder.build();
    }

    public String findCampsitesForDate(String date, String facility) throws IOException {
        String[] parts = facility.split(",");

        PlaceIdAndFacilityId placeIdAndFacilityId = new PlaceIdAndFacilityId();
        placeIdAndFacilityId.placeId = Integer.parseInt(parts[2]);
        placeIdAndFacilityId.facilityId = 0;

        String advanceSearchBody = getSessionCookies(parts[1], parts[2]);

        sessionClears(date);
        setSessionValue(date);
        setPlaceIdAndFacilityId(placeIdAndFacilityId);

        String result = advanceSearch(advanceSearchBody);

        List<String> sites = findSingleDate(result, date);

        if (sites.isEmpty()) {
            return MessageFormat.format("No sites for {0} on {1}", parts[0], date);
        }

        return MessageFormat.format("Sites available for {0} on {1}: {2}", parts[0], date, sites.toString());
    }

    public String findCampsitesForTwoDates(String date, String nextDate, String facility) throws IOException {
        String[] parts = facility.split(",");

        PlaceIdAndFacilityId placeIdAndFacilityId = new PlaceIdAndFacilityId();
        placeIdAndFacilityId.placeId = Integer.parseInt(parts[2]);
        placeIdAndFacilityId.facilityId = 0;

        String advanceSearchBody = getSessionCookies(parts[1], parts[2]);

        sessionClears(date);
        setSessionValue(date);
        setPlaceIdAndFacilityId(placeIdAndFacilityId);

        String result = advanceSearch(advanceSearchBody);

        List<String> sites = findTwoDates(result, date, nextDate);

        if (sites.isEmpty()) {
            return MessageFormat.format("No sites for {0} on {1} and {2}", parts[0], date, nextDate);
        }

        return MessageFormat.format("Sites available for {0} on {1} and {2}: {3}", parts[0], date, nextDate, sites.toString());
    }

    List<String> findSingleDate(String response, String dateToMatch) {
        List<String> sites = new ArrayList<>();

        Matcher matcher = Pattern.compile("\\d+ {2}is available on {2}" + dateToMatch).matcher(response);
        while (matcher.find()) {
            sites.add(matcher.group().split(" ")[0]);
        }

        return sites;
    }

    List<String> findTwoDates(String response, String dateToMatch, String secondDateToMatch) {
        List<String> sites = new ArrayList<>();
        List<String> secondSites = new ArrayList<>();

        Matcher matcher = Pattern.compile("\\d+ {2}is available on {2}" + dateToMatch).matcher(response);
        while (matcher.find()) {
            sites.add(matcher.group().split(" ")[0]);
        }

        Matcher secondMatcher = Pattern.compile("\\d+ {2}is available on {2}" + secondDateToMatch).matcher(response);
        while (secondMatcher.find()) {
            secondSites.add(secondMatcher.group().split(" ")[0]);
        }

        sites.retainAll(secondSites);

        return sites;
    }

    private void setPlaceIdAndFacilityId(PlaceIdAndFacilityId placeIdAndFacilityId) throws IOException {
        String url = "https://reservemn.usedirect.com/MinnesotaWeb/Facilities/AdvanceSearch.aspx/SetNightByPlaceIdAndFacilityIdOnUnitGrid";
        Post(url, placeIdAndFacilityId);
    }

    private String advanceSearch(String advanceSearchBody) throws IOException {
        String url = "https://reservemn.usedirect.com/MinnesotaWeb/Facilities/AdvanceSearch.aspx";
        return PostForm(url, advanceSearchBody);
    }

    private void sessionClears(String date) throws IOException {
        String url = "https://reservemn.usedirect.com/MinnesotaWeb/Facilities/AdvanceSearch.aspx/SessionClears";
        ArrivalDate arrivalDate = new ArrivalDate();
        arrivalDate.arrivaldate = date;
        Post(url, arrivalDate);
    }

    private void setSessionValue(String date) throws IOException {
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

        Post(url, searchParams);
    }

    private String Get(String url) throws IOException {
        HttpGet get = new HttpGet(url);

        HttpResponse response = client.execute(get);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    private String Post(String url, Object obj) throws IOException {
        HttpPost post = new HttpPost(url);

        StringEntity requestEntity = new StringEntity(serialize(obj), ContentType.APPLICATION_JSON);

        post.setEntity(requestEntity);

        HttpResponse response = client.execute(post);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    private String PostForm(String url, String formData) throws IOException {
        HttpPost post = new HttpPost(url);

        UrlEncodedFormEntity requestEntity = new UrlEncodedFormEntity(breakIntoKeyValuePairs(formData));

        post.setEntity(requestEntity);

        HttpResponse response = client.execute(post);

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    private String getSessionCookies(String facilityId, String placeId) throws IOException {
        String url = "https://reservemn.usedirect.com/MinnesotaWeb/Facilities/AdvanceSearch.aspx";
        String html = Get(url);

        Document doc = Jsoup.parse(html);
        String viewState = URLEncoder.encode(doc.getElementById("__VIEWSTATE").val(), StandardCharsets.UTF_8);
        String viewStateGenerator = URLEncoder.encode(doc.getElementById("__VIEWSTATEGENERATOR").val(), StandardCharsets.UTF_8);

        String body = new String(Files.readAllBytes(Paths.get("src/main/resources/advance_search_body.txt")));
        body = body.replace("{__VIEWSTATE}", viewState)
                .replace("{__VIEWSTATEGENERATOR}", viewStateGenerator)
                .replace("{FacilityId}", facilityId)
                .replace("{placeId}", placeId);

        return body;
    }

    String serialize(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        return mapper.writeValueAsString(obj);
    }

    List<NameValuePair> breakIntoKeyValuePairs(String formData) {
        List<NameValuePair> params = new ArrayList<>();
        String[] parts = formData.split("=");
        for (int i = 0; i < parts.length; i += 2) {
            NameValuePair nameValuePair = new BasicNameValuePair(parts[i], parts[i + 1]);
            params.add(nameValuePair);
        }

        try {
            params = URLEncodedUtils.parse(new URI("?" + formData), Charset.forName("UTF-8"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return params;
    }
}
