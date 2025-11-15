package io.dkakunsi.lab.money;

import java.util.UUID;

import io.dkakunsi.lab.common.Context;
import io.dkakunsi.lab.common.DefaultLogger;
import io.dkakunsi.lab.common.Logger;

public class Main {

  private static final Logger LOGGER = DefaultLogger.getLogger(Main.class);

  public static void main(String[] args) {
    Context.set(Context.builder().requestId(UUID.randomUUID().toString()).requester("SYSTEM").build());
    LOGGER.info("Starting services!");
    try {
      new Launcher().launch(System::getenv);
      LOGGER.info("Service is started!");
    } catch (Exception ex) {
      LOGGER.error("Cannot start the application", ex);
      System.exit(1);
    }
  }
}
