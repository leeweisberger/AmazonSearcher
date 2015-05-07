
import java.text.NumberFormat;
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
	public static String EMAIL_TO = "amyngilbert@gmail.com";
//	public static String EMAIL_TO2 = "lweisberger5@gmail.com";

	public static String EMAIL_SUBJECT = "An Item Has Dropped Below Your Price Threshold!";

	/**
	 * Send email.
	 *
	 * @param underMap the map containing conditions under the threshold
	 * @param urlMap 
	 * @param asin the asin
	 * @param threshMap the given map with price thresholds
	 */
	public static void sendEmail(Map<String, Map<String, Double>> underMap, Map<String, Map<String, Double>> minPriceMap, Map<String, String> urlMap){
		StringBuilder body = new StringBuilder();
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
		for(String asin : underMap.keySet()){
			body.append("Good news! Item with asin " + asin + " has dropped below your threshold in one or more catagories: \n\n");
			for(String condition : underMap.get(asin).keySet()){
				body.append(ConditionConstants.getReadableFromFile(condition));
				body.append(": " + "Your threshold was " + currencyFormatter.format(minPriceMap.get(asin).get(condition)) + " and the new price is " + currencyFormatter.format(underMap.get(asin).get(condition)));
				body.append("\n\n");
			}
			body.append(urlMap.get(asin) + "\n\n\n");

		}
		body.append("Happy Shopping!");

		sendEmail(EMAIL_FROM, EMAIL_PASSWORD, EMAIL_TO, EMAIL_SUBJECT, body.toString());
//		sendEmail(EMAIL_FROM, EMAIL_PASSWORD, EMAIL_TO2, EMAIL_SUBJECT, body.toString());

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