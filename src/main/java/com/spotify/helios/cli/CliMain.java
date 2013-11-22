/**
 * Copyright (C) 2012 Spotify AB
 */

package com.spotify.helios.cli;

import com.spotify.helios.common.LoggingConfig;
import com.spotify.logging.LoggingConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintStream;

import static com.google.common.collect.Iterables.get;
import static com.spotify.logging.LoggingConfigurator.Level.ALL;
import static com.spotify.logging.LoggingConfigurator.Level.DEBUG;
import static com.spotify.logging.LoggingConfigurator.Level.INFO;
import static java.util.Arrays.asList;

/**
 * Instantiates and runs helios CLI.
 */
public class CliMain {

  private static final Logger log = LoggerFactory.getLogger(CliMain.class);

  private final CliParser parser;
  private final PrintStream out;

  public static void main(final String... args) {
    try {
      int exitCode = new CliMain(args).run();
      System.exit(exitCode);
    } catch (Throwable e) {
      // TODO (dano): don't swallow exceptions
      //log.error("Uncaught exception", e);
      System.exit(1);
    }
  }

  public CliMain(final PrintStream out, final PrintStream err, final String... args)
      throws Exception {
    this.parser = new CliParser(args);
    this.out = out;
    setupLogging();
  }

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public CliMain(final String... args) throws Exception {
    this(System.out, System.err, args);
  }

  public int run() {
    try {
      return parser.getCommand().run(parser.getNamespace(), parser.getTargets(), out,
                                     parser.getUsername(), parser.getJson());
    } catch (Exception e) {
      log.error("command failed", e);
      return 1;
    }
  }

  private void setupLogging() {
    final LoggingConfig config = parser.getLoggingConfig();

    if (config.getNoLogSetup()) {
      return;
    }

    final int verbose = config.getVerbosity();
    final LoggingConfigurator.Level level = get(asList(INFO, DEBUG, ALL), verbose, ALL);
    final File logconfig = config.getConfigFile();

    if (logconfig != null) {
      LoggingConfigurator.configure(logconfig);
    } else {
      if (config.isSyslog()) {
        LoggingConfigurator.configureSyslogDefaults("helios", level);
      } else {
        LoggingConfigurator.configureDefaults("helios", level);
      }
    }
  }

}