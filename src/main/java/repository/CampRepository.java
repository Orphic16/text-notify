package repository;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.GridModel;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CampRepository {
    private final HttpClient client;

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

    public String findCampsitesForDates(String facility, String... dates) throws IOException {
        String[] parts = facility.split(",");

        GridModel gridModel = buildGridModel(parts[1], dates[0]);

        String result = postGrid(gridModel);

        List<String> sites = findSitesWithDates(result, dates);

        if (sites.isEmpty()) {
            return MessageFormat.format("No sites for {0} on {1}", parts[0], Arrays.asList(dates));
        }

        return MessageFormat.format("Sites available for {0} on {1}: {2}", parts[0], Arrays.asList(dates), sites.toString());
    }

    HashMap<String, ArrayList<String>> createSiteMap(String response) throws IOException {
        HashMap<String, ArrayList<String>> siteMap = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);

        Iterator<JsonNode> units = jsonNode.findPath("Units").elements();
        while (units.hasNext()) {
            JsonNode currentUnit = units.next();

            Iterator<JsonNode> slices = currentUnit.findPath("Slices").elements();
            while (slices.hasNext()) {
                JsonNode currentSlice = slices.next();

                JsonNode isFree = currentSlice.findPath("IsFree");
                if (isFree.booleanValue()) {
                    String name = currentUnit.findPath("Name").textValue();
                    String date = currentSlice.findPath("Date").textValue();

                    if (siteMap.containsKey(name)) {
                        siteMap.get(name).add(date);
                    } else {
                        ArrayList<String> dates = new ArrayList<>();
                        dates.add(date);
                        siteMap.put(name, dates);
                    }
                }
            }
        }

        return siteMap;
    }

    List<String> findSitesWithDates(String response, String... dates) throws IOException {
        List<String> sites = new ArrayList<>();
        HashMap<String, ArrayList<String>> siteMap = createSiteMap(response);

        for (HashMap.Entry<String, ArrayList<String>> site : siteMap.entrySet()) {
            List<String> availableDates = site.getValue();
            if (availableDates.containsAll(Arrays.asList(dates))) {
                sites.add(site.getKey());
            }
        }

        return sites;
    }

    private GridModel buildGridModel(String facilityId, String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter json = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        String outputDate = LocalDate.parse(date, formatter).format(json);
        String maxDate = LocalDate.parse(date, formatter).plusDays(20).format(json);

        GridModel gridModel = new GridModel();
        gridModel.FacilityId = facilityId;
        gridModel.UnitTypeId = 0;
        gridModel.StartDate = outputDate;
        gridModel.InSeasonOnly = true;
        gridModel.WebOnly = true;
        gridModel.IsADA = false;
        gridModel.SleepingUnitId = 0;
        gridModel.MinVehicleLength = 0;
        gridModel.UnitCategoryId = 0;
        gridModel.MinDate = outputDate;
        gridModel.MaxDate = maxDate;

        return gridModel;
    }

    private String postGrid(GridModel gridModel) throws IOException {
        String url = "https://mnrdr.usedirect.com/minnesotardr/rdr/search/grid";
        return Post(url, gridModel);
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

    String serialize(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        return mapper.writeValueAsString(obj);
    }
}
