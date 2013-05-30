package co.eckersley.audio.producers;

import static co.eckersley.audio.helpers.FormatHelper.f;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import co.eckersley.audio.common.SpokenWordItem;
import co.eckersley.audio.repositories.SpokenWordItemRepository;

public class AudioProducer {

    private static Logger logger = LoggerFactory.getLogger(AudioProducer.class);

    private String tempDirName = "/Users/david/spoken_audio/tmp";
    private File tempPath = new File(tempDirName);

    // private String baseDirName = "/Users/david/spoken_audio/files";
    private String baseDirName = "/Library/WebServer/Documents/spoken_audio/files/";
    private File basePath = new File(baseDirName);

    public static void main(String[] args) {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/application-context.xml");

        try {
            SpokenWordItemRepository repo3 = (SpokenWordItemRepository) context.getBean("spokenWordItemRepository");

            // List<SpokenWordItem> items = repo3.findByFileNameIsNull();
            List<SpokenWordItem> items = repo3.findByCleanTextIsNullOrderByDateAsc();

            AudioProducer producer = new AudioProducer();

            // int count = 0;
            for (SpokenWordItem item : items) {
                try {
                    producer.produce(item);
                } catch (Exception e) {
                    logger.error("Error producing audio for: " + item.getId() + " - " + item.getSubject(), e);
                }
                repo3.save(item);
                // if (++count == 10)
                //     break;
            }
        } finally {
            context.close();
        }
    }

    public void produce(SpokenWordItem item) throws Exception {

        logger.info("Creating Audio for: " + item.getId() + " - " + item.getSubject());

        // Output the text to convert
        File src = File.createTempFile("spoken_audio_", ".txt", tempPath);

        logger.info("Outputting text for: " + item.getId() + " - " + item.getSubject());

        String text = item.getText();

        if (StringUtils.isNotBlank(item.getFeed().getCleanerClass())) {
            Class<?> clazz = Class.forName(item.getFeed().getCleanerClass());
            TextCleaner cleaner = (TextCleaner) clazz.newInstance();
            text = cleaner.clean(item.getFromName(), item.getSubject(), text);
        }
        String header = item.getFeed().getTile() + ", " + new SimpleDateFormat("EEEE, MMMM d, yyyy").format(item.getDate()) + ". " + item.getSubject() + "."
                + SystemUtils.LINE_SEPARATOR + SystemUtils.LINE_SEPARATOR;
        item.setCleanText(header + text);

        // Need to write the files out as UTF-8 for the Apple 'say' command.
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(src), "UTF8"));
        writer.write(item.getCleanText());
        writer.close();

        // Specific the output file for the spoken audio
        // String checksum = getChecksum(item.getId()) + ".m4a";
        String checksum = src.getName().replace(".txt", ".m4a");
        File o = new File(basePath, checksum);

        String cmd = f("/usr/bin/say -v {0} -o {1} -f {2}", item.getVoice().toLowerCase(), o.getAbsolutePath(), src.getAbsolutePath());
        
        logger.info(f("Invoking audio command ''{0}''  for: {1} - {2}", cmd, item.getId(), item.getSubject()));

        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();

        int exitValue = p.exitValue();

        String msg = f("Audio command exit value: {0} for: {1} - {2}", exitValue, item.getId(), item.getSubject());
        
        if (exitValue == 0)
            logger.info(msg);
        else
            logger.error(msg);

        if (exitValue != 0) {
            int len = 0;
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream in = p.getInputStream();
            do {
                len = in.read(bytes, 0, bytes.length);
                if (len > 0)
                    baos.write(bytes, 0, len);
            } while (len > 0);
            baos.close();
            if (baos.size() > 0)
                logger.error("Audio command output: ", baos.toString());

            baos = new ByteArrayOutputStream();
            in = p.getErrorStream();
            do {
                len = in.read(bytes, 0, bytes.length);
                if (len > 0)
                    baos.write(bytes, 0, len);
            } while (len > 0);
            baos.close();
            if (baos.size() > 0)
                logger.error("Audio command error: ", baos.toString());
        }

        item.setFileName(o.getAbsolutePath());

        logger.info("Finished creating Audio for: " + item.getId() + " - " + item.getSubject() + " with filename: " + o.getAbsolutePath());
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
