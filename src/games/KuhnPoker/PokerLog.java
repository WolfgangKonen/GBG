package games.KuhnPoker;

import tools.Utils;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class PokerLog {

    static public Logger gameLog;

    private static void setupGameLog() throws IOException {
        String filePath= "Pokerlogs";

        gameLog = Logger.getLogger("pokergame");
        gameLog.setUseParentHandlers(false);

        Utils.checkAndCreateFolder(filePath);

        gameLog.setLevel(Level.INFO);

        FileHandler fileTxt = new FileHandler(filePath+"\\games.log",true);
        fileTxt.setFormatter(new SimpleFormatter() {
            private static final String format = "[%1$tF %1$tT:%1$tL]: %2$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format,
                        new Date(lr.getMillis()),
                        lr.getMessage()
                );
            }
        });

        gameLog.addHandler(fileTxt);
    }

    @SuppressWarnings("unused")
    private static void setupGenericLog() throws IOException {
        String filePath= "Pokerlogs";

        gameLog = Logger.getLogger("Poker");
        gameLog.setUseParentHandlers(false);

        Utils.checkAndCreateFolder(filePath);

        gameLog.setLevel(Level.INFO);

        FileHandler fileTxt = new FileHandler(filePath+"\\logging.txt",true);
        fileTxt.setFormatter(new SimpleFormatter() {
            private static final String format = "[%1$tF %1$tT:%1$tL] [%2$-7s] [%6$d] [%3$s - %4$s]: %5$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format,
                        new Date(lr.getMillis()),
                        lr.getLevel().getLocalizedName(),
                        lr.getSourceClassName(),
                        lr.getSourceMethodName(),
                        lr.getMessage(),
                        lr.getThreadID()
                );
            }
        });

        gameLog.addHandler(fileTxt);
    }

    static public void setup() throws IOException {
        setupGameLog();
    }

}
