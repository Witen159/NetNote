/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client;

import client.scenes.EditCollectionsCtrl;
import client.scenes.ImageOptionsCtrl;
import client.scenes.MainCtrl;
import client.scenes.NoteOverviewCtrl;
import client.utils.ConfigService;
import client.utils.ServerUtils;
import com.google.inject.Injector;
import commons.AppConfig;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.google.inject.Guice.createInjector;

public class Main extends Application {

    private final Injector injector = createInjector(new MyModule());
    private final MyFXML fxml = new MyFXML(injector);

    /**
     * The main entry point for the application.
     * <p>
     * This method launches the JavaFX application by invoking launch(),
     * which triggers the start(Stage primaryStage) method.
     * </p>
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Initializes and starts the JavaFX application.
     * This method is invoked after the JavaFX runtime is initialized.
     *
     * @param primaryStage the primary stage for the application
     * @throws Exception if an error occurs during application initialization or scene loading
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Locale locale = new Locale("En");
        String internationalizationPath = "Internationalization.Text";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(internationalizationPath, locale);
        ConfigService configService = new ConfigService();

        var serverUtils = injector.getInstance(ServerUtils.class);
        if (!serverUtils.isServerAvailable()) {
            var msg = "Server is not available.";
            System.err.println(msg);

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setTitle(resourceBundle.getString("Alert.warningDialog"));
            alert.setContentText(resourceBundle.getString("Alert.serverNotAvailable"));
            alert.showAndWait();
        }

        AppConfig appConfig;
        if (new File("./client/src/main/resources/ApplicationConfig.json").exists()) {
            appConfig = configService.loadConfig();
            resourceBundle = ResourceBundle.getBundle(
                    internationalizationPath, appConfig.getSelectedLanguage()
            );
        } else {
            //Null means nothing is selected.
            appConfig = new AppConfig(null, resourceBundle.getLocale());
        }

        //PLEASE DO NOT CHANGE THE ORDER OF THE LINES BELOW
        //IF THIS IS CHANGED THE FXML.load() would not be able to initialize
        //the NoteOverviewCtrl class
        var mainCtrl = injector.getInstance(MainCtrl.class);
        mainCtrl.setData(appConfig);
        var overview = fxml
                .load(resourceBundle, NoteOverviewCtrl.class,
                        "client", "scenes", "NoteOverview.fxml");
        var editCollections = fxml
                .load(resourceBundle, EditCollectionsCtrl.class,
                        "client", "scenes", "EditCollections.fxml");
        var imageOptions = fxml.load(resourceBundle, ImageOptionsCtrl.class,
                "client", "scenes", "ImageOptions.fxml");

        mainCtrl.initialize(primaryStage, overview, editCollections, imageOptions, resourceBundle);
    }
}