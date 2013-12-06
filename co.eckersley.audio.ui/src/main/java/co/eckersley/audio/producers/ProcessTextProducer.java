package co.eckersley.audio.producers;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProcessTextProducer {

    // private Process p;
    private String outputText;
    private String errorText;
    
    public ProcessTextProducer(Process p) throws Exception {
        // this.p = p;
        this.outputText = extract(p.getInputStream());
        this.errorText = extract(p.getErrorStream());
    }

    private String extract(InputStream in) throws Exception {
        int len = 0;
        byte[] bytes = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        do {
            len = in.read(bytes, 0, bytes.length);
            if (len > 0) {
                baos.write(bytes, 0, len);
            }
        } while (len > 0);
        baos.close();
        return baos.toString();
    }

    public String getOutputText() {
        return outputText;
    }

    public String getErrorText() {
        return errorText;
    }
}
