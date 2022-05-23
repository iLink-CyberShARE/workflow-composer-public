package com.utep.ilink.swim;

import com.utep.ilink.swim.config.MainConfig;
import com.utep.ilink.swim.health.WorkflowComposerHealthCheck;
import com.utep.ilink.swim.resources.MainResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.*;

public class App extends Application<MainConfig> {

  public static void main(String[] args) throws Exception {
    new App().run(args);
  }
  
  @Override
  public void initialize(Bootstrap<MainConfig> bootstrap) {
    bootstrap.addBundle(
      new SwaggerBundle<MainConfig>() {
        @Override
        protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
          MainConfig configuration
        ) {
          return configuration.swaggerBundleConfiguration;
        }
      }
    );
  }

  @Override
  public void run(MainConfig configuration, Environment environment) {
    final MainResource resource = new MainResource();
    final WorkflowComposerHealthCheck healthCheck = new WorkflowComposerHealthCheck();
    environment.healthChecks().register("workflow-composer", healthCheck);
    environment.jersey().register(resource);
  }
}
