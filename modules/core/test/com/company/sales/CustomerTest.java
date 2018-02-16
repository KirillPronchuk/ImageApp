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

import com.company.app.entity.Customer;
import com.haulmont.cuba.core.global.*;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Sample integration test.
 */
public class CustomerTest {

    /**
     * Single-instance test container
     */
    @ClassRule
    public static AppTestContainer cont = AppTestContainer.Common.INSTANCE;

    private Metadata metadata;

    @Before
    public void setUp() throws Exception {
        metadata = cont.metadata();
    }

    @Test
    public void testCreateCustomer() throws Exception {
        // Get a managed bean (or service) from container
        DataManager dataManager = AppBeans.get(DataManager.class);

        // Create new Customer
        Customer customer = metadata.create(Customer.class);
        customer.setName("Test customer");

        // Save the customer to the database
        dataManager.commit(customer);

        // Load the customer by ID
        Customer loaded = dataManager.load(
                LoadContext.create(Customer.class).setId(customer.getId()).setView(View.LOCAL));

        assertNotNull(loaded);
        assertEquals(customer.getName(), loaded.getName());

        // Remove the customer
        dataManager.remove(loaded);
    }
}
