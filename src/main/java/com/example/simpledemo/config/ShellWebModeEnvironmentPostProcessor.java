package com.example.simpledemo.config;

import java.util.Map;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Lets a profile file (such as {@code application-mcp-server.properties}) turn the web server on.
 *
 * <p>Embabel's shell starter sets {@code spring.main.web-application-type} from {@code
 * embabel.agent.shell.web-application-type} (default {@code none}) with top priority. It does this
 * before profile files are read, so a profile cannot change it, and port 8081 never opens. This
 * runs right after profile files are read and applies the profile's value instead.
 */
public class ShellWebModeEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  static final String SHELL_WEB_TYPE = "embabel.agent.shell.web-application-type";

  @Override
  public int getOrder() {
    return ConfigDataEnvironmentPostProcessor.ORDER + 1;
  }

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    String webType = environment.getProperty(SHELL_WEB_TYPE);
    if (webType != null) {
      environment
          .getPropertySources()
          .addFirst(
              new MapPropertySource(
                  "simpleDemoWebMode", Map.of("spring.main.web-application-type", webType)));
    }
  }
}
