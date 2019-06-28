package repository;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class BrickSeekRepository {
    private HttpClient client;

    public BrickSeekRepository() {
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

    public String findStoresWithLowerPriceBySku(String sku, Double amount) throws IOException {
        String result = getPricesBySku(sku);

        List<String> stores = findStoresBelowAmount(result, amount);

        if (stores.isEmpty()) {
            return MessageFormat.format("No stores for {0}", sku);
        }

        return MessageFormat.format("Stores available for {0}: {1}", sku, stores.toString());
    }

    List<String> findStoresBelowAmount(String response, Double amount) {
        List<String> stores = new ArrayList<>();

        Document doc = Jsoup.parse(response);
        Element inventory = doc.getElementsByClass("inventory-checker-table--store-availability-price").first();
        Elements rows = inventory.select(".table__body").select(".table__row");

        for (Element row : rows) {
            Element price = row.getElementsByClass("price-formatted__dollars").first();
            Element availability = row.getElementsByClass("availability-status-indicator__text").first();
            Element store = row.getElementsByTag("address").first();

            Element quantity = null;
            if (!availability.text().equals("Out of Stock") && !availability.text().equals("Limited Stock")) {
                quantity = row.getElementsByClass("table__cell-quantity").first();
            }

            if (quantity != null && Double.parseDouble(price.text()) < amount) {
                String output = MessageFormat.format("{0} are available for ${1} at {2}", quantity.text().replaceAll("[^0-9]", ""), price.text(), store.text());
                stores.add(output);
            }
        }

        return stores;
    }

    private String getPricesBySku(String sku) throws IOException {
        String url = "https://brickseek.com/walmart-inventory-checker/?sku=" + sku;

        String body = MessageFormat.format("method=sku&sku={0}&upc=&zip=55077&sort=recommended", sku);

        return Post(url, body);
    }

    private String Post(String url, String body) throws IOException {
        HttpPost post = new HttpPost(url);

        StringEntity requestEntity = new StringEntity(body, ContentType.APPLICATION_FORM_URLENCODED);

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
}
