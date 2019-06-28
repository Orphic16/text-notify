package repository;

import model.ArrivalDate;
import org.apache.http.NameValuePair;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CampRepositoryTest {
    private CampRepository cr = new CampRepository();

    @Test
    public void findSingleDateTest() throws IOException {
        // Arrange
        String response = new String(Files.readAllBytes(Paths.get("src/test/resources/campsite_response.txt")));

        // Act
        List<String> result = cr.findSingleDate(response, "08/15/2019");

        // Assert
        Assert.assertEquals(5, result.size());
        Assert.assertEquals("1", result.get(0));
        Assert.assertEquals("14", result.get(1));
        Assert.assertEquals("25", result.get(2));
        Assert.assertEquals("31", result.get(3));
        Assert.assertEquals("43", result.get(4));
    }

    @Test
    public void findTwoDatesTest() throws IOException {
        // Arrange
        String response = new String(Files.readAllBytes(Paths.get("src/test/resources/campsite_response.txt")));

        // Act
        List<String> result = cr.findTwoDates(response, "08/14/2019", "08/15/2019");

        // Assert
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("14", result.get(0));
        Assert.assertEquals("31", result.get(1));
        Assert.assertEquals("43", result.get(2));
    }

    @Test
    public void serializeTest() throws IOException {
        // Arrange
        ArrivalDate arrivalDate = new ArrivalDate();
        arrivalDate.arrivaldate = "6/16/1986";

        // Act
        String result = cr.serialize(arrivalDate);

        // Assert
        Assert.assertEquals("{\"arrivaldate\":\"6/16/1986\"}", result);
    }

    @Test
    public void breakIntoKeyValuePairsTest() {
        // Arrange
        String formData = "abc=123&def=&ghi=456";

        // Act
        List<NameValuePair> pairList = cr.breakIntoKeyValuePairs(formData);

        // Assert
        Assert.assertEquals(3, pairList.size());

        Assert.assertEquals("abc", pairList.get(0).getName());
        Assert.assertEquals("123", pairList.get(0).getValue());

        Assert.assertEquals("def", pairList.get(1).getName());
        Assert.assertEquals("", pairList.get(1).getValue());

        Assert.assertEquals("ghi", pairList.get(2).getName());
        Assert.assertEquals("456", pairList.get(2).getValue());
    }
}
