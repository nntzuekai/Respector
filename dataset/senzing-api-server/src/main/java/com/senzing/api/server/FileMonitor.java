package com.senzing.api.server;

import javax.json.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.Collections;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class FileMonitor extends Thread {
  private File file;
  private File tempFile;
  private int pid;
  private Integer port = -1;
  private Integer securePort = -1;
  private JsonWriterFactory writerFactory;
  private boolean ready = false;
  private boolean shutdown = false;

  public FileMonitor(File file)
  {
    this.file = file;
    String processName = ManagementFactory.getRuntimeMXBean().getName();
    String[] parts = processName.split("@");
    this.pid = Integer.parseInt(parts[0]);
    this.writerFactory = Json.createWriterFactory(Collections.emptyMap());
    try {
      this.tempFile = File.createTempFile("senzing-", "-monitor.tmp");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.tempFile.deleteOnExit();
  }

  public synchronized void initialize(Integer port, Integer securePort) {
    if (port != null && port < 0) {
      throw new IllegalArgumentException(
          "The specified port is invalid: " + port);
    }
    if (securePort != null && securePort < 0) {
      throw new IllegalArgumentException(
          "The specified secure port is invalid: " + securePort);

    }
    if (this.port != null && this.port >= 0) return;
    if (this.securePort != null && this.securePort >= 0) return;

    this.port = port;
    this.securePort = securePort;

    while (!this.ready) {
      try {
        this.wait(5000L);
      } catch (InterruptedException ignore) {
        // do nothing
      }
    }

    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("pid", this.pid);
    if (this.port != null) builder.add("port", this.port);
    if (this.securePort != null) builder.add("securePort", this.securePort);
    builder.add("shutdown", false);
    builder.add("heartbeat", System.currentTimeMillis());

    JsonObject jsonObj = builder.build();

    try (FileOutputStream fos = new FileOutputStream(this.file);
         OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
         JsonWriter jw = this.writerFactory.createWriter(osw))
    {
      jw.writeObject(jsonObj);
      osw.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public synchronized void signalReady() {
    // mark as ready
    this.ready = true;

    // check if not yet initialized
    if (this.port != null && this.port < 0) {
      // wait until initialized to write to the file
      return;
    }
    if (this.securePort != null && this.securePort < 0) {
      // wait until initialized to write to the file
      return;
    }
    this.notifyAll();
  }

  public synchronized void signalShutdown() {
    this.shutdown = true;
    this.notifyAll();
  }

  public void run() {
    int readExceptionCount = 0;
    boolean deleteFile = false;
    while (!this.shutdown) {
      // go to sleep for 5 seconds
      try {
        Thread.sleep(5000L);

      } catch (Exception ignore) {
        ignore.printStackTrace();
      }

      // the JSON object to represent the watch file
      JsonObject jsonObj;

      synchronized (this) {
        // get the last-modified time of the file
        long lastModified = this.file.lastModified();

        try (FileInputStream fis = new FileInputStream(this.file);
             InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
             JsonReader jr = Json.createReader(isr)) {

          // read the object
          jsonObj = jr.readObject();

          // clear the read exception count with successful read
          readExceptionCount = 0;

          // check if another process has taken over
          if (jsonObj.getInt("pid") != this.pid) {
            this.shutdown = true;
            continue;
          }

          // check if the shutdown flag has been triggered
          if (jsonObj.getBoolean("shutdown")) {
            this.shutdown = true;
            deleteFile = true;
            continue;
          }

        } catch (Exception e) {
          // allow one exception without aborting -- if 2 consecutive
          // exceptions then give up
          readExceptionCount++;
          if (readExceptionCount > 1) {
            this.shutdown = true;
          }
          e.printStackTrace();
          continue;
        }

        // check if we have shutdown and if so continue loop here
        if (this.shutdown) continue;

        // update the heartbeat
        JsonObjectBuilder builder = Json.createObjectBuilder(jsonObj);
        builder.remove("heartbeat");
        builder.add("heartbeat", System.currentTimeMillis());

        jsonObj = builder.build();

        // write the new JSON to a temp file
        try (FileOutputStream fos = new FileOutputStream(this.tempFile);
             OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
             JsonWriter jw = this.writerFactory.createWriter(osw))
        {
          jw.writeObject(jsonObj);

        } catch (IOException ignore) {
          ignore.printStackTrace();
        }

        // if no process has modified the file, then overwrite it
        if (this.file.lastModified() == lastModified) {
          // copy the temp file over the main file
          try {
            Files.copy(this.tempFile.toPath(),
                       this.file.toPath(),
                       COPY_ATTRIBUTES,
                       REPLACE_EXISTING);

          } catch (IOException ignore) {
            ignore.printStackTrace();
          }
        }
      }
    }

    // delete the file
    if (deleteFile) this.file.delete();

  }
}
