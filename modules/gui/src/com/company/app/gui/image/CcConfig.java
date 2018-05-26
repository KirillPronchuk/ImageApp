package com.company.app.gui.image;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class CcConfig {
    @Bean("ccProcessorService")
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public CcProcessor getCcProcessorService() {
        return new CcProcessor();
    }
}