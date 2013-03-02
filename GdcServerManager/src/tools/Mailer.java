package tools;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.Popup;

public class Mailer {
	private static String HOST =  "smtp.gmail.com";
	private static String FROM = "gdcservtest@gmail.com";
	private static String USER = "gdcservtest@gmail.com";
	private static String EncryptedPass = "gdct%123456m";
	private String host;
	private String from;
	private String to;
	private String user;
	
	public Mailer(){
		this.host = Mailer.HOST;
		this.from = Mailer.FROM;
	}
	public Mailer(String to){
		this.host = Mailer.HOST;
		this.from = Mailer.FROM;
		this.to = to;
		this.user = USER;
	}
	public Mailer(String from,String to){
		this.host = Mailer.HOST;
		this.from = from;
		this.to = to;
		this.user = USER;
	}
	public Mailer(String host,String from,String to){
		this.host = host;
		this.from = from;
		this.to = to;
		this.user = USER;
	}
	
	public boolean sendMail(String subject, String content){		
		if(this.to == null)
			return false;

		// Set properties
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", "587");
		// Get session
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, Mailer.EncryptedPass.substring(0, 3)+Mailer.EncryptedPass.substring(5, 11));
			}
		  });
 
		try {
 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(content);
 
			Transport.send(message);
 
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
			
		}
	}
	 
}
