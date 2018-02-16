/*
 * Copyright (c) 2016 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.company.app;

import com.haulmont.cuba.testsupport.TestContainer;

import java.util.Arrays;
import java.util.Collections;

/**
 *  Container for integration testing.
 */
public class AppTestContainer extends TestContainer {

    public AppTestContainer() {
        super();
        appComponents = Collections.singletonList("com.haulmont.cuba");
        appPropertiesFiles = Arrays.asList(
                // List the files defined in your web.xml
                // in appPropertiesConfig context parameter of the core module
                "cuba-app.properties",
                "app.properties",
                // Add this file which is located in CUBA and defines some properties
                // specifically for test environment. You can replace it with your own
                // or add another one in the end.
                "test-app.properties");
        dbDriver = "org.hsqldb.jdbc.JDBCDriver";
        dbUrl = "jdbc:hsqldb:hsql://localhost:9002/app_test";
        dbUser = "sa";
        dbPassword = "";
    }

    public static class Common extends AppTestContainer {

        public static final AppTestContainer.Common INSTANCE = new AppTestContainer.Common();

        private static volatile boolean initialized;

        private Common() {
        }

        @Override
        public void before() throws Throwable {
            if (!initialized) {
                super.before();
                initialized = true;
            }
            setupContext();
        }

        @Override
        public void after() {
            cleanupContext();
            // never stops - do not call super
        }
    }
}