package repository;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class BrickSeekRepositoryTest {
    @Test
    public void findStoresBelowAmount() throws IOException {
        // Arrange
        BrickSeekRepository bsr = new BrickSeekRepository();

        String response = new String(Files.readAllBytes(Paths.get("src/test/resources/brickseek_response.txt")));

        // Act
        List<String> result = bsr.findStoresBelowAmount(response, 600.00);

        // Assert
        Assert.assertEquals(7, result.size());
        Assert.assertEquals("4 are available for $599 at 9165 Cahill Ave Inver Grove Heights MN 55076 (2 Miles Away) Google MapsApple Maps", result.get(0));
        Assert.assertEquals("4 are available for $599 at 9300 East Point Douglas Rd S Cottage Grove MN 55016 (7.8 Miles Away) Google MapsApple Maps", result.get(1));
    }
}
