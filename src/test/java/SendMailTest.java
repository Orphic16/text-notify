import org.junit.Test;

public class SendMailTest {
    @Test
    public void emailTest() {
        SendMail sm = new SendMail();
        sm.send("Sites available for Gooseberry Falls on 1/1/1111 and 2/2/2222: [59]");
    }
}
