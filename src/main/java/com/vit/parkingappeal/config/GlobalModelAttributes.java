package com.vit.parkingappeal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Exposes environment/version to every page footer (deployment evidence in screenshots). */
@ControllerAdvice
public class GlobalModelAttributes {

    @Value("${app.env:dev}")
    private String appEnv;

    @Value("${app.version:dev}")
    private String appVersion;

    @Value("${app.build:local}")
    private String appBuild;

    @ModelAttribute("appEnv")
    public String appEnv() {
        return appEnv;
    }

    @ModelAttribute("appVersion")
    public String appVersion() {
        return appVersion;
    }

    @ModelAttribute("appBuild")
    public String appBuild() {
        return appBuild;
    }
}
