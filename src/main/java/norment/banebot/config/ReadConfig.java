package norment.banebot.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

public class ReadConfig {

    public Properties properties;

    public ReadConfig() {
        properties = new Properties();
        try {
            //Load properties from file
            FileInputStream inputStream = new FileInputStream("resources/config.properties");
            properties.load(inputStream);
            inputStream.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println("config.properties file not found");
            fnfe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
