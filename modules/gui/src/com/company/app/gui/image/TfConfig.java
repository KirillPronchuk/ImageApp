/*
 * Copyright (c) ${YEAR} ${PACKAGE_NAME}
 */

package com.company.app.gui.image;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class TfConfig {
    @Bean("tfProcessorService")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TfProcessor getTfProcessorService() {
        return new TfProcessor();
    }
}
