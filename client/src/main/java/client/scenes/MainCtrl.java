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
package client.scenes;

import client.utils.ApplicationState;
import client.utils.ConfigService;
import com.google.inject.Inject;
import commons.AppConfig;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ResourceBundle;

public class MainCtrl {
    private final ApplicationState state;
    private Stage primaryStage;
    private Stage secondaryStage;

    private EditCollectionsCtrl editCollectionsCtrl;
    private ImageOptionsCtrl imageOptionsCtrl;
    private Scene overview;
    private Scene editCollections;
    private Scene imageOptions;
    private AppConfig appConfig;
    private ResourceBundle resourceBundle;

    /**
     * Constructor method for the class.
     *
     * @param state state an instance of ApplicationState
     *              that manages the application's current state
     *              and provides context for navigation and data handling
     */
    @Inject
    public MainCtrl(ApplicationState state) {
        this.state = state;
    }

    /**
     * Initializes the main controller by setting up the primary stage, scenes, and controllers
     * for the overview and edit collections views.
     *
     * @param primaryStage    the primary Stage for the application
     * @param overview        a Pair containing the controller
     *                        and parent node for the overview scene
     * @param editCollections a Pair containing the controller
     *                        and parent node for the edit collections scene
     * @param imageOptions    a Pair containing the controller
     *                        and parent node for the image options scene
     * @param resourceBundle  the resource bundle for internationalization
     */
    public void initialize(Stage primaryStage, Pair<NoteOverviewCtrl, Parent> overview,
                           Pair<EditCollectionsCtrl, Parent> editCollections,
                           Pair<ImageOptionsCtrl, Parent> imageOptions,
                           ResourceBundle resourceBundle) {

        this.primaryStage = primaryStage;
        this.secondaryStage = new Stage();

        NoteOverviewCtrl overviewCtrl = overview.getKey();
        this.editCollectionsCtrl = editCollections.getKey();
        this.imageOptionsCtrl = imageOptions.getKey();
        this.overview = new Scene(overview.getValue());
        this.editCollections = new Scene(editCollections.getValue());
        this.imageOptions = new Scene(imageOptions.getValue());

        overviewCtrl.setScene(this.overview);
        state.setResourceBundle(resourceBundle);
        state.setSelectedCollectionFromIndex(
                state.getIndexFromCollection(appConfig.getSelectedCollection())
        );
        state.refresh();
        overviewCtrl.initializeCollectionSelection();
        overviewCtrl.setResourceBundle(resourceBundle);
        overviewCtrl.updateWebView(null);
        overviewCtrl.setAppConfig(appConfig);
        editCollectionsCtrl.setResourceBundle(resourceBundle);
        imageOptionsCtrl.setResourceBundle(resourceBundle);

        showOverview();
        primaryStage.show();

        // remove log from importing spring dependencies
        // https://stackoverflow.com/a/78063825
        System.setProperty("slf4j.internal.verbosity", "WARN");
        state.connectWebsocket();
    }

    /**
     * Displays the overview scene on the primary stage.
     * Sets the title of the primary stage to "NetNote" and assigns
     * the overview scene as the active scene.
     */
    public void showOverview() {
        primaryStage.setTitle("NetNote");
        primaryStage.setScene(overview);

        editCollectionsCtrl.closeCollection();

        primaryStage.setOnCloseRequest(_ -> {
            ConfigService configService = new ConfigService();
            System.out.println("Application is closing," +
                    " saving the current state");

            // make sure note content is saved before exiting
            state.handleAutoContentSyncTimer();

            try {
                configService.saveConfig(
                        appConfig.getSelectedLanguage(), state.getSelectedCollection()
                );
            } catch (IOException e) {
                System.out.println("Config save problem.");
            }
            this.secondaryStage.close();
        });
    }

    /**
     * Displays the edit collections scene on the secondary stage.
     * Sets the title of the secondary stage to "Edit Collections" and assigns
     * the edit collections scene as the active scene.
     */
    public void showCollections() {
        this.resourceBundle = ResourceBundle.getBundle(
                "Internationalization.Text", appConfig.getSelectedLanguage());
        secondaryStage.setTitle(resourceBundle.getString("EditCollection.name"));
        secondaryStage.setScene(editCollections);

        editCollectionsCtrl.setScene(editCollections);

        secondaryStage.show();
        secondaryStage.requestFocus(); // Ensure the secondary stage has focus

        editCollectionsCtrl.updateLanguage(ResourceBundle.getBundle(
                "Internationalization.Text", appConfig.getSelectedLanguage()
        ));
    }

    /**
     * Displays the image options scene on the secondary stage.
     * Sets the title of the secondary stage to "Image Options".
     * And makes this scene the active scene.
     */
    public void showImageOptions() {
        this.resourceBundle = ResourceBundle.getBundle(
                "Internationalization.Text", appConfig.getSelectedLanguage());
        secondaryStage.setTitle(resourceBundle.getString("Image.imageOptions"));
        secondaryStage.setScene(imageOptions);

        imageOptionsCtrl.setScene(imageOptions);

        secondaryStage.show();
        secondaryStage.requestFocus();

        imageOptionsCtrl.updateLanguage(ResourceBundle.getBundle(
                "Internationalization.Text", appConfig.getSelectedLanguage()
        ));
    }

    /**
     * Sets the new language so that it can be passed to other scenes.
     *
     * @param resourceBundle the new language that has been chosen
     */
    public void setResourceBundle(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        this.appConfig.setSelectedLanguage(resourceBundle.getLocale());
        editCollectionsCtrl.setResourceBundle(resourceBundle);
        imageOptionsCtrl.setResourceBundle(resourceBundle);
    }


    /**
     * Set application data.
     * Since it cannot be done through initialize().
     *
     * @param appConfig the config of the user
     */
    public void setData(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Returns the application config class.
     *
     * @return returns the object AppConfig of the application from the user
     */
    public AppConfig getAppConfig() {
        return this.appConfig;
    }
}