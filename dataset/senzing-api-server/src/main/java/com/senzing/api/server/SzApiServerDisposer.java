package com.senzing.api.server;

import com.senzing.util.JsonUtilities;

import javax.json.*;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class SzApiServerDisposer {

  /**
   *
   */
  public static void main(String[] args) {
    try {
      int argIndex = 0;
      if (argIndex >= args.length) {
        System.err.println("No monitoring file path was specified.");
        System.exit(1);
      }
      String arg = args[argIndex++];
      File file = new File(arg);

      if (argIndex >= args.length) {
        System.err.println("No process ID specified.");
        System.exit(1);
      }

      arg = args[argIndex++];
      int pid= 0;
      try {
        pid = Integer.parseInt(arg);

      } catch (Exception e) {
        String message = "The specified PID was not an integer: " + arg;
        System.err.println(message);
        throw new IllegalArgumentException(message);
      }

      long wait = 5000L;
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
          throw new IllegalArgumentException(message);
        }
      }

      long delay = 0L;
      if (argIndex < args.length) {
        arg = args[argIndex++];
        try {
          delay = Long.parseLong(arg);

          if (delay < 0L) {
            throw new IllegalArgumentException();
          }
        } catch (Exception e) {
          String message = ("The specified delay time is not a positive integer: "
              + arg);
          System.err.println(message);
          throw new IllegalArgumentException(message);
        }
      }

      final String utf8 = "UTF-8";
      int readExceptionCount = 0;
      boolean done = false;

      log("Attempting to shutdown process: " + pid);
      while (!done && file.exists()) {
        // get the last modified time for the monitoring file
        long lastModified = file.lastModified();

        // read the monitoring file
        Integer port = null;
        Long heartbeat = null;
        log("Opening file for read: " + file);
        try (FileInputStream    fis = new FileInputStream(file);
             InputStreamReader  isr = new InputStreamReader(fis, utf8);
             JsonReader         jr  = Json.createReader(isr))
        {
          // read the object
          JsonObject jsonObj = jr.readObject();

          // set the read exception count back to zero
          readExceptionCount = 0;

          // check if another process has taken over
          if (jsonObj.getInt("pid") != pid) {
            log("Found different PID in file: " + jsonObj.getInt("pid"));
            done = true;
            continue;
          }
          log("Found expected PID in file: " + pid);

          // check if the shutdown flag has been triggered
          if (jsonObj.getBoolean("shutdown")) {
            log("Found shutdown flag set to true");
            done = true;
            continue;
          }

          // get the port
          port = JsonUtilities.getInteger(jsonObj, "port");
          heartbeat = JsonUtilities.getLong(jsonObj, "heartbeat");

        } catch (Exception e) {
          log("Error reading file: " + file);
          readExceptionCount++;
          if (readExceptionCount > 2) {
            done = true;
            continue;
          }
        } finally {
          log("Closing file: " + file);
        }


        // write out a temp file representing the new content with the shutdown
        // flag set to true
        File tempFile = File.createTempFile("senzing-monintor-", ".tmp");
        tempFile.deleteOnExit();

        log("Opening temp file for write: " + tempFile);
        try (FileOutputStream   fos = new FileOutputStream(tempFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, utf8);
             JsonWriter         jw  = Json.createWriter(osw))
        {
          JsonObjectBuilder builder = Json.createObjectBuilder();
          JsonUtilities.add(builder, "pid", pid);
          JsonUtilities.add(builder, "port", port);
          JsonUtilities.add(builder, "shutdown", true);
          JsonUtilities.add(builder, "heartbeat", heartbeat);

          jw.writeObject(builder.build());

          log("Wrote new content to temp file: " + tempFile);

        } catch (Exception e) {
          log("FAILED TO WRITE OUT MONITOR FILE FOR SHUTDOWN");
          done = true;
          continue;
        } finally {
          log("Closed temp file: " + file);
        }

        // copy the temp monitoring file over the actual monitoring file
        if (file.lastModified() == lastModified) {
          log("Determined file has not changed since read: " + file);
          try {
            log("Copying temp file to file: " + file);
            Files.copy(tempFile.toPath(),
                       file.toPath(),
                       COPY_ATTRIBUTES,
                       REPLACE_EXISTING);

            log("Copied temp file to file: " + file);
            done = true;
            continue;

          } catch (IOException ignore) {
            ignore.printStackTrace();

          } finally {
            tempFile.delete();
          }
        }

        try {
          log("Sleeping for 2 seconds before retry");
          Thread.sleep(2000L);
        } catch (InterruptedException ignore) {
          // ignore exception
        }
      }

      long start = System.currentTimeMillis();
      done = false;

      log("Waiting for file deletion: " + file);
      while (!done && file.exists() && (System.currentTimeMillis()-start) < wait)
      {
        log("Checking PID in file: " + file);
        try (FileInputStream    fis = new FileInputStream(file);
             InputStreamReader  isr = new InputStreamReader(fis, utf8);
             JsonReader         jr  = Json.createReader(isr))
        {
          // read the object
          JsonObject jsonObj = jr.readObject();

          if (jsonObj.getInt("pid") != pid) {
            log("PID changed to: " + jsonObj.getInt("pid"));
            done = true;
            continue;
          }
          log("PID still unchanged: " + pid);

        } catch (Exception e) {
          // ignore I/O exception
        } finally {
          log("Closing file: " + file);
        }

        log("Sleeping before checking file again.");
        try {
          Thread.sleep(2000L);
        } catch (InterruptedException ignore) {
          // ignore exception
        }
      }

      log("File was " + (file.exists()?" NEVER ":"") + "deleted: " + file);

      if (delay > 0L) {
        log("Delaying before exit: " + delay);
        try {
          Thread.sleep(delay);
        } catch (InterruptedException ignore) {
          // ignore exception
        }
      }

      log("Completed for PID: " + pid);

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void log(String msg) {
    msg = LocalDateTime.now().toString() + " [Disposer] " + msg;
    System.out.println(msg);
  }

}
