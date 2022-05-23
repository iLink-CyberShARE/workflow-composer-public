package com.utep.ilink.swim.config;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotEmpty;
import io.federecio.dropwizard.swagger.*;

public class MainConfig extends Configuration {
    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;
    
    @NotEmpty
    String version;

    public String getVersion(){
        return version;
    }
    public void setVersion(String version){
        this.version = version;
    }
}
