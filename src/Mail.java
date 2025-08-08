import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Mail implements Runnable{
    private static final Properties properties = System.getProperties();
    private final Config config;
    private final String title;
    private final String body;
    private final String ticketNumber;

    public Mail(Config config, String ticketNumber, String title, String body){
        this.config = config;
        this.title = title;
        this.body = body;
        this.ticketNumber = ticketNumber;
    }

    @Override
    public void run() {
        sendEmail(title, body);
    }

    private void sendEmail(String title, String body){
        try{
            //sets up the mail properties
            properties.put("mail.smtp.auth", true);
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", config.getHost());
            properties.put("mail.smtp.port", config.getPort());
            properties.put("mail.smtp.ssl.trust", config.getHost());

            Session session = Session.getDefaultInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication(){
                    return new PasswordAuthentication(config.getSender(), config.getPassword());
                }
            });


            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getSender()));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(config.getRecipient()));
            message.setSubject(title);
            message.setText(body);
            System.out.println("Sending email...");
            Transport.send(message);
            System.out.println("Mail successfully sent");
            Main.getInstance().deleteFromFile(ticketNumber);
        }catch(Exception e){
            System.out.println("Error for " + title + ": " + e.getMessage());
        }
    }
}
