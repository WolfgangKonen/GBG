package agentIO;

import java.io.IOException;
import java.io.InputStream;


public class ProgressTrackingInputStream extends InputStream implements IGetProgress {
	private final IOProgress progess;
	private final InputStream in;

    public ProgressTrackingInputStream(InputStream in, IOProgress progess) throws IOException {
        this.in = in;
        this.progess = progess;
    }

    public IOProgress getProgess() {
        return progess;
    }

	@Override
	public int read() throws IOException {
		int x = in.read();
		progess.update(1);
		return x;
	}
}
