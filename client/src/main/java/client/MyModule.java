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
import client.utils.ApplicationState;
import client.utils.ServerUtils;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;


public class MyModule implements Module {

    /**
     * Configures bindings for dependency injection.
     * <p>
     * This method binds specific classes to their corresponding scopes using a
     * Binder. All bindings in this method are set to Scopes#SINGLETON,
     * ensuring that a single instance of each class is shared throughout the application.
     * </p>
     *
     * @param binder the Binder used to configure class bindings for dependency injection
     */
    @Override
    public void configure(Binder binder) {
        binder.bind(MainCtrl.class).in(Scopes.SINGLETON);
        binder.bind(NoteOverviewCtrl.class).in(Scopes.SINGLETON);
        binder.bind(ImageOptionsCtrl.class).in(Scopes.SINGLETON);
        binder.bind(EditCollectionsCtrl.class).in(Scopes.SINGLETON);
        binder.bind(ApplicationState.class).in(Scopes.SINGLETON);
        binder.bind(ServerUtils.class).in(Scopes.SINGLETON);
        binder.bind(Markdown.class).in(Scopes.SINGLETON);
        binder.bind(ImageOptionsCtrl.class).in(Scopes.SINGLETON);
    }
}