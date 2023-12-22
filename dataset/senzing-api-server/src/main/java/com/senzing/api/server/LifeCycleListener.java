package com.senzing.api.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.component.LifeCycle;

import java.net.InetAddress;

class LifeCycleListener implements LifeCycle.Listener {
  private String serverDescription;
  private Integer httpPort;
  private Integer httpsPort = null;
  private InetAddress ipAddr;
  private String basePath;
  private Server jettyServer;
  private FileMonitor fileMonitor;

  public LifeCycleListener(String       serverDesription,
                           Server       jettyServer,
                           Integer      httpPort,
                           Integer      httpsPort,
                           String       basePath,
                           InetAddress  ipAddress,
                           FileMonitor  fileMonitor)
  {
    this.serverDescription  = serverDesription;
    this.httpPort           = httpPort;
    this.httpsPort          = httpsPort;
    this.ipAddr             = ipAddress;
    this.basePath           = basePath;
    this.jettyServer        = jettyServer;
    this.fileMonitor        = fileMonitor;
  }

  public void lifeCycleStarting(LifeCycle event) {
    System.out.println();
    System.out.println("Starting " + this.serverDescription + " on ports:");
    if (this.httpPort != null) {
      if (this.httpPort != 0) {
        System.out.println("    - " + this.httpPort + " (HTTP)");
      } else {
        System.out.println("    - [rotating port] (HTTP)");
      }
    }
    if (this.httpsPort != null) {
      if (this.httpsPort != 0) {
        System.out.println("    - " + this.httpsPort + " (HTTPS / SSL)");
      } else {
        System.out.println("    - [rotating port] (HTTPS)");
      }
    }
    System.out.println();
  }

  public void lifeCycleStarted(LifeCycle event) {
    Integer port = this.httpPort;
    Integer securePort = this.httpsPort;
    if (port != null && port == 0) {
      port = ((ServerConnector)(jettyServer.getConnectors()[0])).getLocalPort();
    }
    if (securePort != null && securePort == 0) {
      securePort = ((ServerConnector)(jettyServer.getConnectors()[1])).getLocalPort();
    }
    System.out.println("Started " + this.serverDescription + " on ports:");
    if (port != null) {
      System.out.println("    - " + port + " (HTTP)");
    }
    if (securePort != null) {
      System.out.println("    - " + securePort + " (HTTPS / SSL)");
    }
    System.out.println();
    System.out.println("Server running at:");
    if (port != null) {
      System.out.println(" - http://" + this.ipAddr.getHostAddress()
                             + ":" + port + this.basePath);
    }
    if (securePort != null) {
      System.out.println(" - https://" + this.ipAddr.getHostAddress()
                             + ":" + securePort + this.basePath);
    }
    System.out.println();
    if (this.fileMonitor != null) {
      this.fileMonitor.signalReady();
    }
  }

  public void lifeCycleFailure(LifeCycle event, Throwable cause) {
    System.err.println("Failed to start " + this.serverDescription
                           + " on ports:");
    if (this.httpPort == null) {
      if (this.httpPort == 0) {
        System.out.println("    - [rotating port] (HTTP)");
      } else {
        System.out.println("    - " + this.httpPort + " (HTTP)");
      }
    }
    if (this.httpsPort == null) {
      if (this.httpsPort == 0) {
        System.out.println("    - [rotating port] (HTTPS / SSL)");
      } else {
        System.out.println("    - " + this.httpsPort + " (HTTPS / SSL)");
      }
    }
    System.err.println();
    System.err.println(cause);
  }

  public void lifeCycleStopping(LifeCycle event) {
    System.out.println("Stopping " + this.serverDescription + "....");
    System.out.println();
  }

  public void lifeCycleStopped(LifeCycle event) {
    System.out.println("Stopped " + this.serverDescription + ".");
    System.out.println();
  }
}
