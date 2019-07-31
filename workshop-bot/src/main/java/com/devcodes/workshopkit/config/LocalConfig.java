package com.devcodes.workshopkit.config;

import com.devcodes.workshopkit.util.IHealthReporter;
import com.devcodes.workshopkit.util.ILandingRedirectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalConfig {
    @Bean
    @ConditionalOnMissingBean(IHealthReporter.class)
    public IHealthReporter fallbackHealthReporter() {
        return new IHealthReporter() {
            @Override
            public boolean isHealthy() {
                return true;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(ILandingRedirectProvider.class)
    public ILandingRedirectProvider mattermostRedirectProvider() {
        return new ILandingRedirectProvider() {
            @Override
            public String getRedirectUrl() {
                return "https://www.google.com";
            }
        };
    }
}
