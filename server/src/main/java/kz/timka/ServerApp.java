package kz.timka;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerApp {
    private static final Logger LOGGER = LogManager.getLogger(ServerApp.class);
    public static void main( String[] args ) {
        LOGGER.error("error");
        new Server(8189);

    }
}