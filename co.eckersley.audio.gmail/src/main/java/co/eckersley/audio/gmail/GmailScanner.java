package co.eckersley.audio.gmail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import co.eckersley.audio.common.GmailFeed;
import co.eckersley.audio.common.SpokenWordFeed;
import co.eckersley.audio.common.SpokenWordItem;
import co.eckersley.audio.repositories.GmailFeedRepository;
import co.eckersley.audio.repositories.SpokenWordItemRepository;

import com.googlecode.gmail4j.GmailConnection;
import com.googlecode.gmail4j.GmailMessage;
import com.googlecode.gmail4j.auth.Credentials;
import com.googlecode.gmail4j.javamail.ImapGmailClient;
import com.googlecode.gmail4j.javamail.ImapGmailConnection;

public class GmailScanner {

    private static Logger logger = LoggerFactory.getLogger(GmailScanner.class);

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
	public static void main(String[] args) {

		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/application-context.xml");

		try {
			GmailFeedRepository repo1 = (GmailFeedRepository) context.getBean("gmailFeedRepository");
//			SpokenWordFeedRepository repo2 = (SpokenWordFeedRepository) context.getBean("spokenWordFeedRepository");
			SpokenWordItemRepository repo3 = (SpokenWordItemRepository) context.getBean("spokenWordItemRepository");
			
			Iterable<GmailFeed> feeds = repo1.findAll();
			
			Map<String, GmailFeed> fromMap = new HashMap<String, GmailFeed>();
			
			for (GmailFeed feed : feeds) {
				if (StringUtils.isNotBlank(feed.getFromName()))
					fromMap.put(feed.getFromName(), feed);
				if (StringUtils.isNotBlank(feed.getFromEmail()))
					fromMap.put(feed.getFromEmail(), feed);
			}
			
			Set<String> fromList = fromMap.keySet();
			
			String uid = context.getEnvironment().getProperty("gmail.uid");
			String pwd = context.getEnvironment().getProperty("gmail.pwd");
			
			ImapGmailClient client = new ImapGmailClient();
			Credentials cred = new Credentials(uid, pwd.toCharArray());
			
			GmailConnection connection = new ImapGmailConnection(cred);
			client.setConnection(connection);
			
			List<Integer> markAsRead = new ArrayList<Integer>();
			
			final List<GmailMessage> messages = client.getUnreadMessages();
			for (GmailMessage message : messages) {
				String fromName = message.getFrom().getName();
				String fromEmail = message.getFrom().getEmail();
				String id = fromName + "-" + sdf.format(message.getSendDate());
				GmailFeed feed = (fromList.contains(fromName) ? fromMap.get(fromName) : fromMap.get(fromEmail));
				if (feed != null) {
//					SpokenWordFeed parentFeed = repo2.findByFromName(message.getFrom().getName());
				    SpokenWordFeed parentFeed = feed.getFeed();
					SpokenWordItem item = repo3.findOne(id);
					if (feed.isSkip()) {
						logger.info("Skipped: " + id + " - " + message.getSubject());
					} else if (parentFeed == null) {
						logger.error("Failed to Parent feed for: " + id + " - " + message.getSubject());
					} else if (item == null) {
						item = new SpokenWordItem(id, parentFeed);
						item.setDate(message.getSendDate());
						item.setFromName(message.getFrom().getName());
						item.setSubject(message.getSubject());
						item.setText(message.getContentText());
						item.setVoice(feed.getVoice());
						repo3.save(item);
						logger.info("Added: " + item.getId() + " - " + item.getSubject());
					} else if (item.getAudio() == null && item.getFileName() == null) {
						item.setText(message.getContentText());
						item.setVoice(item.getFeed().getVoice());
						logger.info("Updated: " + item.getId() + " - " + item.getSubject());
					} else {
						logger.info("Skipped: " + item.getId() + " - " + item.getSubject());
					}
					markAsRead.add(message.getMessageNumber());
				} else {
					logger.info("Skipping " + id + " - " + message.getSubject());
				}
			}
			for (int messageNumber : markAsRead) {
				client.markAsRead(messageNumber);
		//		client.moveTo(ImapGmailLabel.ALL_MAIL, messageNumber);
			}
		} finally {
			context.close();
		}
	}
}
