package agentIO;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class ProgressTrackingOutputStream extends ObjectOutputStream implements IGetProgress {
    private final IOProgress progess;

    public ProgressTrackingOutputStream(OutputStream out, IOProgress progess) throws IOException {
        super(out);
        this.progess = progess;
    }

    public IOProgress getProgess() {
        return progess;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        this.progess.update(len);
    }
}