import org.junit.Test;

public class SendMailTest {
    @Test
    public void emailTest() {
        SendMail sm = new SendMail();
        sm.send("Test Message");
    }
}
