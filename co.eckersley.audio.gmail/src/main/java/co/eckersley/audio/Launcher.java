package co.eckersley.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.eckersley.audio.gmail.GmailScanner;
import co.eckersley.audio.producers.AudioProducer;
import co.eckersley.audio.producers.FeedProducer;

public class Launcher {

    private static Logger logger = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {

        logger.info("Checking launch command line parameters");

        boolean skipGmail = false;
        boolean skipAudio = false;
        boolean skipFeed = false;

        for (String arg : args) {
            if ("-gmail".equalsIgnoreCase(arg))
                skipGmail = true;
            if ("-audio".equalsIgnoreCase(arg))
                skipAudio = true;
            if ("-feed".equalsIgnoreCase(arg))
                skipFeed = true;
        }

        if (skipGmail) {
            logger.info("Skipping Spoken Word Gmail scanner");
        } else {
            logger.info("Launching Spoken Word Gmail scanner");
            try {
                GmailScanner.main(args);

            } catch (Exception e) {
                logger.error("Error encountered while scanning mail.google.com", e);
            }
            logger.info("Finished Spoken Word Gmail scanner");
        }
        if (skipAudio) {
            logger.info("Skipping Spoken Word Audio generation");
        } else {
            logger.info("Launching Spoken Word Audio generation");
            try {
                AudioProducer.main(args);

            } catch (Exception e) {
                logger.error("Error encountered while creating audio files", e);
            }
            logger.info("Finished Spoken Word Audio generation");
        }
        if (skipFeed) {
            logger.info("Skipping Spoken Word feed generation");
        } else {
            logger.info("Launching Spoken Word feed generation");
            try {
                FeedProducer.main(args);

            } catch (Exception e) {
                logger.error("Error encountered while creating RSS feeds", e);
            }
            logger.info("Finished Spoken Word feed generation");
        }
    }

}
