package agentIO;

import java.io.OutputStream;
import java.io.PrintStream;

public class ProgressTrackingPrintStream extends PrintStream implements IGetProgress {
	 private final IOProgress progess;
	
	public ProgressTrackingPrintStream(OutputStream out, IOProgress progress) {
		super(out);
		this.progess = progress;
	}

	@Override
	public IOProgress getProgess() {
		return progess;
	}
	
	@Override
	public void print(String str) {
		super.print(str);
		this.progess.update(str.length());
	}
	
	@Override
	public void print(float f) {
		super.print(f);
		this.progess.update(Float.SIZE/8);
	}

}
