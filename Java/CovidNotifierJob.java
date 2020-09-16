import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

public class CovidNotifierJob implements Job {

    private static final DateTimeFormatter LONG_DATE_FORMAT = DateTimeFormatter.ofPattern("E, d MMM yyyy H:mm:ss z");
    private static final String TO_ADDRESS = "to address goes here";
    public static final String FROM_ADDRESS = "from address goes here";
    public static final String PASSWORD = "password goes here";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        System.out.printf("Running job at %s", LocalDateTime.now().toString());
        try {
            URL url = new URL("https://api.coronavirus.data.gov.uk/v1/data?" + getQueryParameters());
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            System.out.printf("Received response from server %s", httpsURLConnection.getResponseCode());
            processResponse(httpsURLConnection);
        } catch (Exception e) {
            System.out.printf("Error occurred during Quartz job: %s", e.getMessage());
            e.printStackTrace();
        }
    }

    private void processResponse(HttpsURLConnection connection) throws InterruptedException {
        String lastModifiedHeader = connection.getHeaderField("Last-Modified");
        LocalDate lastModifiedDate = LocalDate.parse(lastModifiedHeader, LONG_DATE_FORMAT);
        if (lastModifiedDate.isBefore(LocalDate.now())) {
            System.out.printf("Data out of date, but latest is not available at the usual time. Current check time is %s; recursively checking until published", lastModifiedDate.toString());
            Thread.sleep(1800000);
            execute(null);
        } else {
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(connection.getInputStream());
                 BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String input;
                while ((input = reader.readLine()) != null) {
                    builder.append(input);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                CovidResponse covidData = objectMapper.readValue(builder.toString(), CovidResponse.class);
                int cases = covidData.getData().stream().mapToInt(CovidData::getNewCasesByPublishDate).sum();
                sendMail(cases);
            } catch (Exception e) {
                System.out.printf("Error occurred during Quartz job: %s", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String getQueryParameters() {
        return "filters=areaType=nation&structure={\"date\":\"date\",\"newCasesByPublishDate\":\"newCasesByPublishDate\"}&latestBy=newCasesByPublishDate";
    }

    private void sendMail(int cases) {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_ADDRESS, PASSWORD);
            }
        });
        session.setDebug(true);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(FROM_ADDRESS);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(TO_ADDRESS));
            message.setSubject("Corona virus daily cases");
            message.setText(String.format("The daily cases are %s", cases));
            Transport.send(message);
        } catch (Exception e) {
            System.out.printf("Error when sending email %s", e.getMessage());
            e.printStackTrace();
        }
    }
}
