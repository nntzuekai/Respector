package com.senzing.api.server;

import javax.json.*;
import java.io.*;

public class SzApiServerWaiter {
  public static final int SUCCESS = 0;
  public static final int WRONG_PID = 1;
  public static final int BAD_PARSE = 2;
  public static final int TIMEOUT = 3;
  public static final int BAD_ARGS = 4;
  public static final int UNKNOWN_ERROR = 5;
  /**
   *
   */
  public static void main(String[] args) {
    try {
      int argIndex = 0;
      if (argIndex >= args.length) {
        System.err.println("No monitoring file path was specified.");
        System.exit(BAD_ARGS);
      }
      String arg = args[argIndex++];
      File file = new File(arg);

      if (argIndex >= args.length) {
        System.err.println("No process ID specified.");
        System.exit(BAD_ARGS);
      }

      arg = args[argIndex++];
      int pid= 0;
      try {
        pid = Integer.parseInt(arg);

      } catch (Exception e) {
        String message = "The specified PID was not an integer: " + arg;
        System.err.println(message);
        System.exit(BAD_ARGS);
        throw new IllegalArgumentException(message);
      }

      long wait = 10000L;
      if (argIndex < args.length) {
        arg = args[argIndex++];

        try {
          wait = Long.parseLong(arg);

          if (wait < 0L) {
            throw new IllegalArgumentException();
          }

        } catch (Exception e) {
          String message = ("The specified wait time is not a positive integer: "
              + arg);
          System.err.println(message);
          System.exit(BAD_ARGS);
          throw new IllegalArgumentException(message);
        }
      }

      final String utf8 = "UTF-8";
      int readExceptionCount = 0;
      boolean done = false;
      long start = System.currentTimeMillis();
      long now   = start;
      boolean wrongPID = false;

      // wait for the file to be created
      while (((now-start) < wait) && (!file.exists())) {
        try {
          Thread.sleep(Math.min(1000L, (wait / 5L)));
        } catch (InterruptedException ignore) {
          // ignore
        }
        now = System.currentTimeMillis();
      }

      while (!done && ((now - start) < wait) && file.exists()) {
        // read the monitoring file
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, utf8);
             JsonReader jr = Json.createReader(isr)) {
          // read the object
          JsonObject jsonObj = jr.readObject();

          // set the read exception count back to zero
          readExceptionCount = 0;

          // check if another process has taken over or if the previous
          // one has not yet relinquished the file
          wrongPID = (jsonObj.getInt("pid") != pid);
          if (wrongPID) {
            try {
              Thread.sleep(Math.min(1000L, (wait / 5L)));
            } catch (InterruptedException ignore) {
              // do nothing
            }
            now = System.currentTimeMillis();
            continue;
          }

          // check if the port has been set (and the PID is expected)
          if (jsonObj.getInt("port") > 0) {
            done = true;
            now = System.currentTimeMillis();
            continue;
          }

        } catch (Exception e) {
          readExceptionCount++;
          if (readExceptionCount > 2) {
            System.exit(BAD_PARSE);
            done = true;
            now = System.currentTimeMillis();
            continue;
          }
        }
        now = System.currentTimeMillis();
      }

      if (wrongPID) {
        System.exit(WRONG_PID);
      }

      if (!done && file.exists() && ((now - start) > wait)) {
        // assume time must have run out
        System.exit(TIMEOUT);
      }

      // if we get here then all went well
      if (done) {
        System.exit(SUCCESS);
      }

      // signal an unreognized error
      System.exit(UNKNOWN_ERROR);

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
