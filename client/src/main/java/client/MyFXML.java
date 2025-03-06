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

import com.google.inject.Injector;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class MyFXML {

    private final Injector injector;

    /**
     * Constructs a new instance of MyFXML with the given injector.
     *
     * @param injector the dependency injection framework to be used for controller instantiation
     */
    public MyFXML(Injector injector) {
        this.injector = injector;
    }

    /**
     * Loads an FXML file and returns its controller and root node.
     *
     * @param resourceBundle language bundle
     * @param <T>            the type of the controller
     * @param c              the class of the controller for type inference
     * @param parts          the parts of the path to the FXML file,
     *                       concatenated to form the complete path
     * @return a Pair containing the controller and the root node
     * @throws RuntimeException if an IOException occurs during loading
     */
    public <T> Pair<T, Parent> load(ResourceBundle resourceBundle, Class<T> c, String... parts) {
        try {
            var loader = new FXMLLoader(getLocation(parts),
                    resourceBundle, null, new MyFactory(), StandardCharsets.UTF_8);
            Parent parent = loader.load();
            T ctrl = loader.getController();
            loader.setResources(resourceBundle);
            return new Pair<>(ctrl, parent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Resolves the location of a resource based on the provided path segments.
     * <p>
     * The segments are concatenated into a single path, which is then resolved
     * into a URL using the class loader of MyFXML.
     * </p>
     *
     * @param parts the segments of the resource path
     * @return the URL pointing to the resource
     */
    private URL getLocation(String... parts) {
        var path = Path.of("", parts).toString();
        return MyFXML.class.getClassLoader().getResource(path);
    }

    private class MyFactory implements BuilderFactory, Callback<Class<?>, Object> {

        /**
         * Returns a Builder instance for the specified class type.
         * <p>
         * The builder is used to create instances of the specified type, leveraging
         * the injector for dependency resolution.
         * </p>
         *
         * @param type the class type for which a builder is required
         * @return a Builder that builds instances of the specified type
         */
        @Override
        @SuppressWarnings("rawtypes")
        public Builder<?> getBuilder(Class<?> type) {
            return new Builder() {
                @Override
                public Object build() {
                    return injector.getInstance(type);
                }
            };
        }

        /**
         * Creates an instance of the specified class using the injector.
         * <p>
         * This method is invoked during the FXML loading process to create
         * controller instances with dependency injection.
         * </p>
         *
         * @param type the class to instantiate
         * @return an instance of the specified class
         */
        @Override
        public Object call(Class<?> type) {
            return injector.getInstance(type);
        }
    }
}