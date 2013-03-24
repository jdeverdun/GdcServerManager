package tools;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.Popup;

public class Mailer {
	private final String HOST =  "120.40.30.100";
	private final String FROM = "GDC Server";
	private final String USER = "gdcservtest@gmail.com";
	private final String EncryptedPass = "gdct%123456m";
	private String host;
	private String from;
	private String to;
	private String user;
	
	public Mailer(){
		this.host = HOST;
		this.from = FROM;
	}
	public Mailer(String to){
		this.host = HOST;
		this.from = FROM;
		this.to = to;
		this.user = USER;
	}
	public Mailer(String from,String to){
		this.host = HOST;
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

		Properties properties = new Properties();
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "1025");
		properties.put("mail.smtp.auth", "true");

		final String username = user;
		final String password = EncryptedPass.substring(0, 3)+EncryptedPass.substring(5, 11);
		Authenticator authenticator = new Authenticator() {
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication(username, password);
		    }
		};

		Transport transport = null;

		try {
		    Session session = Session.getDefaultInstance(properties, authenticator);
		    transport = session.getTransport();
		    transport.connect(username, password);
		    Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(content);
		    transport.sendMessage(message, message.getAllRecipients());
		    return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
		    if (transport != null) try { transport.close(); } catch (MessagingException logOrIgnore) {}
		}
		
	}
	 
}
