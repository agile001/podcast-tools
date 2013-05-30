package co.eckersley.audio.producers;


import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import co.eckersley.audio.common.SpokenWordItem;
import co.eckersley.audio.repositories.SpokenWordItemRepository;

public class AudioProducerTest {

    private static Logger logger = LoggerFactory.getLogger(AudioProducer.class);

    @Test
    public void main() {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/application-context.xml");

        try {
            SpokenWordItemRepository repo3 = (SpokenWordItemRepository) context.getBean("spokenWordItemRepository");

            // List<SpokenWordItem> items = repo3.findByFileNameIsNull();
            List<SpokenWordItem> items = repo3.findByCleanTextIsNullOrderByDateAsc();

            AudioProducerTest producer = new AudioProducerTest();

            int count = 0;
            for (SpokenWordItem item : items) {
                try {
                    producer.produce(item);
                } catch (Exception e) {
                    logger.error("Error producing audio for: " + item.getId() + " - " + item.getSubject(), e);
                }
                repo3.save(item);
                if (++count == 1000)
                    break;
            }
        } finally {
            context.close();
        }
    }

    public void produce(SpokenWordItem item) throws Exception {

        logger.info("Outputting clean text for: " + item.getId() + " - " + item.getSubject());

        String text = item.getText();

        if (StringUtils.isNotBlank(item.getFeed().getCleanerClass())) {
            Class<?> clazz = Class.forName(item.getFeed().getCleanerClass());
            TextCleaner cleaner = (TextCleaner) clazz.newInstance();
            text = cleaner.clean(item.getFromName(), item.getSubject(), text);
        }
        String header = item.getFeed().getTile() + ", " + new SimpleDateFormat("EEEE, MMMM d, yyyy").format(item.getDate()) + ". " + item.getSubject() + "."
                + SystemUtils.LINE_SEPARATOR + SystemUtils.LINE_SEPARATOR;
        
        logger.info(header + text);

        logger.info("Finished outputting clean text for: " + item.getId() + " - " + item.getSubject());

    }

//    private String getChecksum(String value) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("SHA-1");
//            md.update(value.getBytes());
//            byte[] checksumBytes = md.digest();
//            return convertToHex(checksumBytes);
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private String convertToHex(byte[] checksumBytes) {
//        BigInteger checksum = new BigInteger(1, checksumBytes);
//        return checksum.toString(16);
//    }
}
