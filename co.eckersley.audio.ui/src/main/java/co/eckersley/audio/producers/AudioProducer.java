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

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import co.eckersley.audio.data.dao.Episode;
import co.eckersley.audio.data.repositories.PodcastEpisodeRepository;

public class AudioProducer {

    private static Logger logger = LoggerFactory.getLogger(AudioProducer.class);

    private String tempDirName = "/Users/david/spoken_audio/tmp";
    private File tempPath = new File(tempDirName);

    // private String baseDirName = "/Users/david/spoken_audio/files";
    private String baseDirName = "/Library/WebServer/Documents/spoken_audio/files/";
    private File basePath = new File(baseDirName);

    public static void main(String[] args) {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/applicationContext.xml");

        try {
            PodcastEpisodeRepository episodeRepository = (PodcastEpisodeRepository) context.getBean(PodcastEpisodeRepository.class);

            List<Episode> items = episodeRepository.findByTextIsNotNullAndPublishedTextIsNullOrderByDateAsc();

            AudioProducer producer = new AudioProducer();

            // int count = 0;
            for (Episode item : items) {
                try {
                    producer.produce(item);
                } catch (Exception e) {
                    logger.error("Error producing audio for: " + item.getId() + " - " + item.getSubject(), e);
                }
                episodeRepository.save(item);
                // if (++count == 10)
                //     break;
            }
        } finally {
            context.close();
        }
    }

    public void produce(Episode item) throws Exception {

        logger.info("Creating Audio for: " + item.getId() + " - " + item.getSubject());

        // Output the text to convert
        File src = new File(tempPath, "spoken_audio_" + item.getId() + ".txt");

        logger.info("Outputting text for: " + item.getId() + " - " + item.getSubject());

        String text = item.getText();

        String header = new SimpleDateFormat("EEEE, MMMM d, yyyy").format(item.getDate()) + ". " + item.getSubject() + "."
                + SystemUtils.LINE_SEPARATOR + SystemUtils.LINE_SEPARATOR;
        
        item.setPublishedText(header + text);   

        // Need to write the files out as UTF-8 for the Apple 'say' command.
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(src), "UTF8"));
        writer.write(item.getPublishedText());
        writer.close();

        // Specific the output file for the spoken audio
        // String checksum = getChecksum(item.getId()) + ".m4a";
        String audioFileName = "spoken_audio_" + item.getId() + ".m4a";
        File o = new File(basePath, audioFileName);

//        String cmd = f("/usr/bin/say -v {0} -o {1} --data-format=alac -f {2}", item.getVoice().toLowerCase(), o.getAbsolutePath(), src.getAbsolutePath());
        String cmd = f("/usr/bin/say -v {0} -o {1} --data-format=alac -f {2}", item.getVoice(), o.getAbsolutePath(), src.getAbsolutePath());
        
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
            ProcessTextProducer processText = new ProcessTextProducer(p);
            StringBuilder outputText = new StringBuilder();
            if (processText.getOutputText().length() > 0) {
                String errorText = processText.getOutputText();
                logger.error("Audio command output: ", errorText);
                outputText.append("==============================================").append(SystemUtils.LINE_SEPARATOR);
                outputText.append("        Audio Producer Command Output        ").append(SystemUtils.LINE_SEPARATOR);
                outputText.append("==============================================").append(SystemUtils.LINE_SEPARATOR);
                outputText.append(errorText).append(SystemUtils.LINE_SEPARATOR);
            }
            if (processText.getOutputText().length() > 0) {
                String errorText = processText.getOutputText();
                logger.error("Audio command error: ", errorText);
                outputText.append("==============================================").append(SystemUtils.LINE_SEPARATOR);
                outputText.append("          Audio Producer Error Output         ").append(SystemUtils.LINE_SEPARATOR);
                outputText.append("==============================================").append(SystemUtils.LINE_SEPARATOR);
                outputText.append(errorText).append(SystemUtils.LINE_SEPARATOR);
            }
            if (outputText.length() > 0) {
                item.setErrorText(outputText.toString());
            }
        }
        item.setFileName(o.getAbsolutePath());
        logger.info("Finished creating Audio for: " + item.getId() + " - " + item.getSubject() + " with filename: " + o.getAbsolutePath());
    }
}
