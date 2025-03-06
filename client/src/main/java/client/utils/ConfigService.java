package client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import commons.AppConfig;
import commons.Collection;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class ConfigService {
    private final String configFilePath =
            "./client/src/main/resources/ApplicationConfig.json";
    private final File configFile = new File(configFilePath);
    private final ObjectMapper objectMapper;

    /**
     * Constructor that initializes the objects or values for the fields.
     */
    public ConfigService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Loads the configurations that was saved in the json file.
     *
     * @return the class that holds all the data that needs to be loaded
     * @throws IOException if the file could not be read
     */
    public AppConfig loadConfig() throws IOException {
        File configFile = new File(configFilePath);
        if (configFile.exists()) {
            System.out.println("Reads config file");
            return objectMapper.readValue(configFile, AppConfig.class);
        } else {
            return new AppConfig();
        }
    }

    /**
     * Saves the current state of the application to a json file.
     * since there is no implemented feature to switch to different collections.
     *
     * @param language   the current selected language from the user
     * @param collection the current selected collection from the user
     * @throws IOException if writing to the json file failed
     */
    public void saveConfig(Locale language, Collection collection) throws IOException {
        AppConfig config = new AppConfig();
        config.setSelectedCollection(collection);
        config.setSelectedLanguage(language);
        objectMapper.writeValue(configFile, config);
        System.out.println("Has saved the state of the application to the config file");
    }
}

