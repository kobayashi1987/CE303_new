package SOMSServerJava;

import java.io.IOException;
import java.util.logging.*;

public class SOMSUtils {
    public static Logger setupLogger() {
        Logger logger = Logger.getLogger("SOMSLogger");
        logger.setUseParentHandlers(false); // Disable default console handler

        try {
            // Console Handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

            // File Handler
            FileHandler fileHandler = new FileHandler("soms.log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Failed to initialize logger handlers.");
            e.printStackTrace();
        }

        return logger;
    }
}