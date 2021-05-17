package repository;

import model.GridModel;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CampRepositoryTest {
    private final CampRepository cr = new CampRepository();

    @Test
    public void findSitesWithDatesTest() throws IOException {
        // Arrange
        String response = new String(Files.readAllBytes(Paths.get("src/test/resources/campsite_response.txt")));

        // Act
        List<String> result = cr.findSitesWithDates(response, "2021-05-21");

        // Assert
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("Drive-In #64", result.get(0));
    }

    @Test
    public void findSitesWithDates_TwoDates() throws IOException {
        // Arrange
        String response = new String(Files.readAllBytes(Paths.get("src/test/resources/campsite_response.txt")));

        // Act
        List<String> result = cr.findSitesWithDates(response, "2021-05-23", "2021-05-24");

        // Assert
        Assert.assertEquals(7, result.size());
        Assert.assertEquals("Drive-In #30", result.get(0));
        Assert.assertEquals("Drive-In #28", result.get(1));
        Assert.assertEquals("Drive-In #9", result.get(2));
        Assert.assertEquals("Drive-In #5", result.get(3));
        Assert.assertEquals("Drive-In #24", result.get(4));
        Assert.assertEquals("Drive-In #50", result.get(5));
        Assert.assertEquals("Drive-In #46", result.get(6));
    }

    @Test
    public void serializeTest() throws IOException {
        // Arrange
        GridModel gridModel = new GridModel();
        gridModel.FacilityId = "123";
        gridModel.UnitTypeId = 456;
        gridModel.StartDate = "6-16-1986";
        gridModel.InSeasonOnly = true;
        gridModel.WebOnly = true;
        gridModel.IsADA = false;
        gridModel.SleepingUnitId = 0;
        gridModel.MinVehicleLength = 0;
        gridModel.UnitCategoryId = 0;
        gridModel.MinDate = "6-16-1986";
        gridModel.MaxDate = "6-16-2021";

        // Act
        String result = cr.serialize(gridModel);

        // Assert
        Assert.assertEquals("{\"FacilityId\":\"123\",\"UnitTypeId\":456,\"StartDate\":\"6-16-1986\",\"InSeasonOnly\":true,\"WebOnly\":true,\"IsADA\":false,\"SleepingUnitId\":0,\"MinVehicleLength\":0,\"UnitCategoryId\":0,\"MinDate\":\"6-16-1986\",\"MaxDate\":\"6-16-2021\"}", result);
    }
}
