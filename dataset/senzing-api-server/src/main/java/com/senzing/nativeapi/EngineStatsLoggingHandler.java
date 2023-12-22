package com.senzing.nativeapi;

import com.senzing.g2.engine.G2Engine;
import com.senzing.g2.engine.Result;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Date;

/**
 * Provides an invocation handler for the {@link G2Engine} proxy.
 */
public class EngineStatsLoggingHandler implements InvocationHandler {
  /**
   * The set of methods that add a record.
   */
  private static Set<Method> EXCLUDED_METHOD_SET;

  /**
   * static initializer
   */
  static {
    Class<G2Engine> cls = G2Engine.class;
    Set<Method> methodSet = new LinkedHashSet<>();
    try {
      methodSet.add(cls.getMethod(
          "init",
          String.class, String.class, boolean.class));
      methodSet.add(cls.getMethod(
          "initWithConfigID",
          String.class, String.class, long.class, boolean.class));
      methodSet.add(cls.getMethod("reinit", long.class));
      methodSet.add(cls.getMethod("destroy"));
      methodSet.add(cls.getMethod("primeEngine"));
      methodSet.add(cls.getMethod("purgeRepository"));
      methodSet.add(cls.getMethod("stats"));
      methodSet.add(cls.getMethod(
          "exportConfig", StringBuffer.class));
      methodSet.add(cls.getMethod(
          "exportConfig", StringBuffer.class, Result.class));
      methodSet.add(cls.getMethod(
          "getActiveConfigID", Result.class));
      methodSet.add(cls.getMethod(
          "getRepositoryLastModifiedTime", Result.class));
      methodSet.add(cls.getMethod(
          "getRecord",
          String.class, String.class, StringBuffer.class));
      methodSet.add(cls.getMethod(
          "getRecord",
          String.class, String.class, long.class, StringBuffer.class));
      methodSet.add(cls.getMethod("fetchNext", long.class, StringBuffer.class));
      methodSet.add(cls.getMethod("closeExport", long.class));
      methodSet.add(cls.getMethod(
          "getRedoRecord", StringBuffer.class));
      methodSet.add(cls.getMethod("countRedoRecords"));

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);

    } finally {
      EXCLUDED_METHOD_SET = Collections.unmodifiableSet(methodSet);
    }
  }

  /**
   * The backing {@link G2Engine} instance.
   */
  private G2Engine engineApi;

  /**
   * The time stamp when stats were last logged.
   */
  private long statsLastLogged = 0L;

  /**
   * The period of time between logging of stats (assuming entity scoring is
   * being performed).
   */
  private long statsInterval;

  /**
   * The monitor used for synchronization.
   */
  private final Object monitor = new Object();

  /**
   * The {@link PrintStream} to write to or <tt>null</tt> if not writing to
   * a {@link PrintStream}.
   */
  private PrintStream logStream = null;

  /**
   * The {@link PrintWriter} to write to or <tt>null</tt> if not writing to
   * a {@link PrintWriter}.
   */
  private PrintWriter logWriter = null;

  /**
   * Constructs with the specified {@link G2Engine} instance and the stats
   * interval.
   *
   * @param engineApi The backing {@link G2Engine} instance.
   * @param statsInterval The number of milliseconds between logging of stats
   *                      when actively scoring.
   */
  public EngineStatsLoggingHandler(G2Engine    engineApi,
                                   long        statsInterval)
  {
    this.engineApi        = engineApi;
    this.statsInterval    = statsInterval;
    this.statsLastLogged  = System.currentTimeMillis();
    this.logStream        = null;
    this.logWriter        = null;
  }

  /**
   * Constructs with the specified {@link G2Engine} instance, the stats
   * interval and the {@link PrintStream} to log to.
   *
   * @param engineApi The backing {@link G2Engine} instance.
   * @param statsInterval The number of milliseconds between logging of stats
   *                      when actively scoring.
   * @param logStream The {@link PrintStream} to write to.
   */
  public EngineStatsLoggingHandler(G2Engine     engineApi,
                                   long         statsInterval,
                                   PrintStream  logStream)
  {
    this(engineApi, statsInterval);
    this.logStream = logStream;
  }

  /**
   * Constructs with the specified {@link G2Engine} instance, the stats
   * interval and the {@link PrintWriter} to log to.
   *
   * @param engineApi The backing {@link G2Engine} instance.
   * @param statsInterval The number of milliseconds between logging of stats
   *                      when actively scoring.
   * @param logWriter The {@link PrintWriter} to write to.
   */
  public EngineStatsLoggingHandler(G2Engine    engineApi,
                                   long        statsInterval,
                                   PrintWriter logWriter)
  {
    this(engineApi, statsInterval);
    this.logWriter = logWriter;
  }
  /**
   * Handles invocation of the methods but tracks the number of times a record
   * is added and after every so many records logs stats.
   *
   * @param proxy The proxy on which the method is called.
   * @param method The {@link Method} being called.
   * @param args The arguments to invoke the {@link Method}.
   * @return The result from the invocation.
   * @throws UnsupportedOperationException If the method is explicitly or
   *                                       implicitly unsupported.
   * @throws Throwable If the specified method throws an exception when invoked.
   */
  public Object invoke(Object   proxy,
                       Method   method,
                       Object[] args)
      throws Throwable
  {
    Object result = method.invoke(this.engineApi, args);

    if (EXCLUDED_METHOD_SET.contains(method)) return result;

    synchronized (this.monitor) {
      long now = System.currentTimeMillis();
      if ((now - this.statsLastLogged) >= this.statsInterval) {
        this.statsLastLogged = now;
        String stats = this.engineApi.stats();
        this.log(stats);
      }
    }

    return result;
  }

  /**
   * Logs the stats message.
   */
  private void log(String stats) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    pw.println("=============================================================");
    pw.println("ENGINE STATS @ " + new Date());
    pw.println("-------------------------------------------------");
    pw.println(stats);
    pw.println("=============================================================");
    if (this.logStream != null) {
      this.logStream.println(sw.toString());
    }
    if (this.logWriter != null) {
      this.logWriter.println(sw.toString());
    }
    if (this.logStream == null && this.logWriter == null) {
      System.out.println(sw.toString());
    }
  }
}
