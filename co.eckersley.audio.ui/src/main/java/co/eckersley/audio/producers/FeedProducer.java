package co.eckersley.audio.producers;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import co.eckersley.audio.data.dao.Episode;
import co.eckersley.audio.data.dao.Feed;
import co.eckersley.audio.data.repositories.PodcastEpisodeRepository;
import co.eckersley.audio.data.repositories.PodcastFeedRepository;

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
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.feed.synd.SyndImageImpl;
import com.sun.syndication.io.SyndFeedOutput;

public class FeedProducer {

    private static Logger logger = LoggerFactory.getLogger(FeedProducer.class);

    // private String baseDirName = "/Users/david/spoken_audio";
    private String baseDirName = "/Library/WebServer/Documents/spoken_audio/";
    private File basePath = new File(baseDirName);

    private String HTTP_BASE = "http://home.eckersley.co/spoken_audio/";

    PodcastEpisodeRepository repository;

    public static void main(String[] args) {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/application-context.xml");

        try {
            PodcastFeedRepository feedRepository = context.getBean(PodcastFeedRepository.class);
            PodcastEpisodeRepository episodeRepository = (PodcastEpisodeRepository) context.getBean(PodcastEpisodeRepository.class);

            FeedProducer producer = new FeedProducer(episodeRepository);

            for (Feed feed : feedRepository.findAll()) {
                try {
                    producer.produce(feed);
                } catch (Exception e) {
                    logger.error("Error producing feed for: " + feed.getTitle(), e);
                }
            }
        } finally {
            context.close();
        }
    }

    public FeedProducer(PodcastEpisodeRepository repository) {
        super();
        this.repository = repository;
    }

    public void produce(Feed feedRef) throws Exception {

        logger.info("Creating Feed for: " + feedRef.getTitle());

        SyndFeed podcastFeed = new SyndFeedImpl();
        podcastFeed.setFeedType("rss_2.0");
        podcastFeed.setTitle(feedRef.getTitle());
        podcastFeed.setLink(HTTP_BASE + c(feedRef.getTitle()));
        podcastFeed.setDescription(feedRef.getDescription());

        SyndImage image = new SyndImageImpl();
        image.setTitle(feedRef.getTitle());
        image.setLink(HTTP_BASE + c(feedRef.getTitle()) + ".xml");
        image.setUrl(HTTP_BASE + "images/" + c(feedRef.getTitle()) + ".png");
        podcastFeed.setImage(image);

        List<SyndCategory> categories = new ArrayList<SyndCategory>();
        SyndCategory category = new SyndCategoryImpl();
        category.setName("Spoken Audio");
        categories.add(category);
        podcastFeed.setCategories(categories);

        List<SyndEntry> entries = new ArrayList<SyndEntry>();

        Calendar limitTime = Calendar.getInstance();
        limitTime.add(Calendar.MONTH, -3);
        Calendar episodeTime = new GregorianCalendar();

        for (Episode item : repository.findByFeedAndPublishedTextIsNotNullAndFileNameIsNotNullOrderByDateDesc(feedRef)) {

            episodeTime.setTime(item.getDate());

            if (limitTime.before(episodeTime)) {
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
                    // description.setValue(StringUtils.abbreviate(item.getCleanText(), 1024 * 5));
                    description.setValue(item.getPublishedText());
                    entry.setDescription(description);

                    entries.add(entry);

                } else {
                    logger.error("Audio file doesn't exist: " + item.getFileName());
                }
            }
        }
        podcastFeed.setEntries(entries);
        podcastFeed.setLink(HTTP_BASE + c(feedRef.getTitle()) + ".xml");

        File feedFile = new File(basePath, f(feedRef.getTitle()) + ".xml");
        logger.debug("Outputting feed '{}' to: {}", feedRef.getTitle(), feedFile.getAbsolutePath());
        Writer writer = new FileWriter(feedFile);
        SyndFeedOutput output = new SyndFeedOutput();
        output.output(podcastFeed, writer);
        writer.close();

        logger.info("Finished creating Feed for: " + feedRef.getTitle());
    }

    private String c(String i) {
        return i.toLowerCase().replace(" ", "_").replace("&", "%26");
    }

    private String f(String i) {
        return i.toLowerCase().replace(" ", "_");
    }
}
