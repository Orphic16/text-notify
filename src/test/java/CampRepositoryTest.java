import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CampRepositoryTest {
    @Test
    public void findSingleDateTest() throws IOException {
        // Arrange
        CampRepository cr = new CampRepository();

        String response = new String(Files.readAllBytes(Paths.get("testData/Response.txt")));

        // Act
        List<String> result = cr.findSingleDate(response, "6/10/2018");

        // Assert
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("14", result.get(0));
        Assert.assertEquals("30", result.get(1));
    }

    @Test
    public void findTwoDatesTest() throws IOException {
        // Arrange
        CampRepository cr = new CampRepository();

        String response = new String(Files.readAllBytes(Paths.get("testData/WeekendResponse.txt")));

        // Act
        List<String> result = cr.findTwoDates(response, "6/15/2018", "6/16/2018");

        // Assert
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("31", result.get(0));
    }

    @Test
    public void serializeTest() throws IOException {
        // Arrange
        UnitGridData ugd = new UnitGridData();
        ugd.FacilityId = "990";
        ugd.PlaceId = "118";
        ugd.MaximumDates = "20";
        ugd.IsTablet = true;
        ugd.MaximumStayforGrid = 14;

        CampRepository cr = new CampRepository();

        // Act
        String result = cr.serialize(ugd, false);

        // Assert
        Assert.assertEquals("{\"FacilityId\":\"990\",\"PlaceId\":\"118\",\"MaximumDates\":\"20\",\"IsTablet\":true,\"MaximumStayforGrid\":14}", result);
    }
}
