package co.eckersley.audio.producers;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import co.eckersley.audio.common.SpokenWordFeed;
import co.eckersley.audio.common.SpokenWordItem;
import co.eckersley.audio.repositories.SpokenWordFeedRepository;
import co.eckersley.audio.repositories.SpokenWordItemRepository;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

public class FeedProducer {

    private static Logger logger = LoggerFactory.getLogger(FeedProducer.class);

//	private String baseDirName = "/Users/david/spoken_audio";
	private String baseDirName = "/Library/WebServer/Documents/spoken_audio/";
	private File basePath = new File(baseDirName);
	
	private String HTTP_BASE = "http://atlas-i.local/spoken_audio/";
	
	SpokenWordItemRepository repository;
	
	public static void main(String[] args) {
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/application-context.xml");

		try {
			SpokenWordFeedRepository repo2 = (SpokenWordFeedRepository) context.getBean("spokenWordFeedRepository");
			SpokenWordItemRepository repo3 = (SpokenWordItemRepository) context.getBean("spokenWordItemRepository");
	
//			List<SpokenWordFeed> items = repo2.findAll();
//			List<SpokenWordItem> items = repo3.findByFileNameIsNull();
//			List<SpokenWordItem> items = repo3.findByCleanTextIsNullOrderByDateAsc();
			
			FeedProducer producer = new FeedProducer(repo3);
			
			for (SpokenWordFeed feed : repo2.findAll()) {
				try {
					producer.produce(feed);
				} catch (Exception e) {
					logger.error("Error producing feed for: " + feed.getTile(), e);
				}
			}
		} finally {
			context.close();
		}
	}
	
	public FeedProducer(SpokenWordItemRepository repository) {
		super();
		this.repository = repository;
	}

	public void produce(SpokenWordFeed feedRef) throws Exception {

		logger.info("Creating Feed for: " + feedRef.getTile());

		SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");
        feed.setTitle(feedRef.getTile());
        feed.setLink(HTTP_BASE + c(feedRef.getTile()));
        feed.setDescription(feedRef.getDescription()); 
        
        List<SyndCategory> categories = new ArrayList<SyndCategory>();
        SyndCategory category = new SyndCategoryImpl();
        category.setName("Spoken Audio");
        categories.add(category);
        feed.setCategories(categories);
        
        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        
        for (SpokenWordItem item : repository.findByFeedAndCleanTextIsNotNullAndFileNameIsNotNullOrderByDateAsc(feedRef)) {

        	File f = new File(item.getFileName());
        	
        	if (f.exists()) {
	        	SyndEnclosure enclosure = new SyndEnclosureImpl();
	            enclosure.setLength(f.length());
	            enclosure.setType("audio/m4a");
	            enclosure.setUrl(HTTP_BASE + "files/" + f.getName());
	            
	        	SyndEntry entry = new SyndEntryImpl();
	            entry.setTitle(item.getSubject());
	            entry.setPublishedDate(item.getDate());
	            entry.setCategories(categories);
	            entry.setLink(HTTP_BASE + "files/" + f.getName());
	            entry.setEnclosures(Arrays.asList(enclosure));
	            
	            SyndContent description = new SyndContentImpl();
	            description.setType("text/plain");
	            description.setValue(item.getCleanText());
	            entry.setDescription(description);
	            
	            entries.add(entry);
        	} else {
        		logger.error("Audio file doesn't exist: " + item.getFileName());
        	}
        }
        feed.setEntries(entries);
        feed.setLink(HTTP_BASE + c(feedRef.getTile()) + ".xml");
        
        File feedFile = new File(basePath, f(feedRef.getTile()) + ".xml");
        Writer writer = new FileWriter(feedFile);
        SyndFeedOutput output = new SyndFeedOutput();
        output.output(feed, writer);
        writer.close();
        
		logger.info("Finished creating Feed for: " + feedRef.getTile());
	}
	
	private String c(String i) {
		return i.toLowerCase().replace(" ", "_").replace("&", "%26");
	}
	
	private String f(String i) {
		return i.toLowerCase().replace(" ", "_");
	}
	
	private String getChecksum(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
	        md.update(value.getBytes());
	        byte[] checksumBytes = md.digest();
	        return convertToHex(checksumBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
    
    private String convertToHex(byte[] checksumBytes) {
        BigInteger checksum = new BigInteger(1, checksumBytes);
        return checksum.toString(16);
    }
}
