import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class Emailer {

	public static String EMAIL_FROM = "amazon.updator@gmail.com";
	public static String EMAIL_PASSWORD = "emailerpassword";
	public static String EMAIL_TO = "lweisberger5@gmail.com";
	public static String EMAIL_SUBJECT = "An Item Has Dropped Below Your Price Threshold!";
	
	/**
	 * Send email.
	 *
	 * @param underMap the map containing conditions under the threshold
	 * @param asin the asin
	 * @param threshMap the given map with price thresholds
	 */
	public static void sendEmail(Map<String, Double> underMap, String asin, Map<String,Double> threshMap){
		StringBuilder body = new StringBuilder();
		body.append("Good news! Item with asin " + asin + " has dropped below your threshold in one or more catagories: \n\n");
		for(String condition : underMap.keySet()){
			body.append(ConditionConstants.getReadableFromFile(condition));
			body.append(": " + "Your threshold was " + threshMap.get(condition) + " and the new price is " + underMap.get(condition));
			body.append("\n\n");
		}
		body.append("Happy Shopping!");
		
		sendEmail(EMAIL_FROM, EMAIL_PASSWORD, EMAIL_TO, EMAIL_SUBJECT, body.toString());
	}
	
	private static void sendEmail(final String from, final String password, final String to,
			String subject, String body) {
		String host = "smtp.gmail.com";

		Properties properties = System.getProperties();
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.auth", "true");

		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, password);
			}
		});

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to));

			message.setSubject(subject);
			message.setText(body);
			Transport.send(message);
			System.out.println("Sent message successfully....");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

}