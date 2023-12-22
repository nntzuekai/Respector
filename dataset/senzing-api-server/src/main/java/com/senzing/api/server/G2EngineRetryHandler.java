package com.senzing.api.server;

import com.senzing.g2.engine.G2Engine;
import com.senzing.g2.engine.Result;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.senzing.util.LoggingUtilities.multilineFormat;

/**
 * Provides an invocation handler for the {@link G2Engine} proxy.
 */
class G2EngineRetryHandler implements InvocationHandler {
  /**
   * The set of methods that should be retried.
   */
  private static Set<Method> RETRY_METHODS;

  /**
   * The set of methods that are directly called without retry.
   */
  private static Set<Method> DIRECT_METHODS;

  /**
   * The set of methods that are explicitly unsupported.
   */
  private static Set<Method> UNSUPPORTED_METHODS;

  /**
   * Utility method to get an optional method that may not exist on the version
   * of g2.jar that we are building with.
   */
  private static void addMethodIfExists(Set<Method> set,
                                        String      name,
                                        Class...    argTypes)
  {
    Method method = null;
    try {
      method = G2Engine.class.getMethod(name, argTypes);
    } catch (NoSuchMethodException ignore) {
      return;
    }
    set.add(method);
  }

  /**
   * static initializer
   */
  static {
    Class<G2Engine> cls = G2Engine.class;
    Set<Method> retrySet        = new LinkedHashSet<>();
    Set<Method> directSet       = new LinkedHashSet<>();
    Set<Method> unsupportedSet  = new LinkedHashSet<>();
    try {
      unsupportedSet.add(cls.getMethod(
          "init", String.class, String.class, boolean.class));
      unsupportedSet.add(cls.getMethod(
          "initWithConfigID",
          String.class, String.class, long.class, boolean.class));
      unsupportedSet.add(cls.getMethod("reinit", long.class));
      unsupportedSet.add(cls.getMethod("destroy"));
      unsupportedSet.add(cls.getMethod("exportJSONEntityReport",
                                       long.class, Result.class));
      unsupportedSet.add(cls.getMethod("exportCSVEntityReport",
                                       String.class, long.class, Result.class));
      unsupportedSet.add(cls.getMethod("closeExport", long.class));

      directSet.add(cls.getMethod("primeEngine"));
      directSet.add(cls.getMethod("purgeRepository"));
      directSet.add(cls.getMethod("stats"));
      directSet.add(cls.getMethod("exportConfig", StringBuffer.class));
      directSet.add(cls.getMethod(
          "exportConfig", StringBuffer.class, Result.class));
      directSet.add(cls.getMethod("getActiveConfigID", Result.class));
      directSet.add(cls.getMethod("getRepositoryLastModifiedTime", Result.class));
      directSet.add(cls.getMethod(
          "exportJSONEntityReport", long.class, Result.class));
      directSet.add(cls.getMethod(
          "exportCSVEntityReport",
          String.class, long.class, Result.class));
      directSet.add(cls.getMethod(
          "fetchNext", long.class, StringBuffer.class));
      directSet.add(cls.getMethod("closeExport", long.class));
      directSet.add(cls.getMethod("getRedoRecord", StringBuffer.class));
      directSet.add(cls.getMethod("countRedoRecords"));
      directSet.add(cls.getMethod("getLastException"));
      directSet.add(cls.getMethod("getLastExceptionCode"));
      directSet.add(cls.getMethod("clearLastException"));

      retrySet.add(cls.getMethod(
          "addRecord",
          String.class, String.class, String.class, String.class));
      retrySet.add(cls.getMethod(
          "replaceRecord",
          String.class, String.class, String.class, String.class));
      retrySet.add(cls.getMethod(
          "replaceRecordWithInfo",
                String.class, String.class, String.class,
                String.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "addRecordWithReturnedRecordID",
          String.class, StringBuffer.class, String.class, String.class));
      retrySet.add(cls.getMethod(
          "addRecordWithInfo",
                String.class, String.class, String.class, String.class,
                long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "addRecordWithInfoWithReturnedRecordID",
          String.class, String.class, String.class, long.class,
          StringBuffer.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "deleteRecord", String.class, String.class, String.class));
      retrySet.add(cls.getMethod(
          "deleteRecordWithInfo", String.class, String.class,
          String.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "reevaluateRecord", String.class, String.class, long.class));
      retrySet.add(cls.getMethod(
          "reevaluateRecordWithInfo",
          String.class, String.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "reevaluateEntity", long.class, long.class));
      retrySet.add(cls.getMethod(
          "reevaluateEntityWithInfo",
          long.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "searchByAttributes", String.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "searchByAttributes",
          String.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "getEntityByEntityID", long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "getEntityByEntityID",
          long.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "getEntityByRecordID",
          String.class, String.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "getEntityByRecordID",
          String.class, String.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "getVirtualEntityByRecordID",
          String.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "getVirtualEntityByRecordID",
          String.class, StringBuffer.class));

      retrySet.add(cls.getMethod(
          "findPathByEntityID",
          long.class, long.class, int.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findPathByEntityID",
          long.class, long.class, int.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findPathByRecordID",
          String.class, String.class, String.class, String.class,
          int.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findPathByRecordID",
          String.class, String.class, String.class, String.class, int.class,
          long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findPathExcludingByEntityID",
          long.class, long.class, int.class, String.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findPathExcludingByEntityID",
          long.class, long.class, int.class, String.class, long.class,
          StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findPathExcludingByRecordID",
          String.class, String.class, String.class, String.class, int.class,
          String.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findPathExcludingByRecordID",
          String.class, String.class, String.class, String.class, int.class,
          String.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findPathIncludingSourceByEntityID",
          long.class, long.class, int.class, String.class, String.class,
          StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findPathIncludingSourceByEntityID",
          long.class, long.class, int.class, String.class, String.class,
          long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findPathIncludingSourceByRecordID",
          String.class, String.class, String.class, String.class, int.class,
          String.class, String.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findPathIncludingSourceByRecordID",
          String.class, String.class, String.class, String.class, int.class,
          String.class, String.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findNetworkByEntityID",
          String.class, int.class, int.class, int.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findNetworkByEntityID",
          String.class, int.class, int.class, int.class, long.class,
          StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findNetworkByRecordID", String.class, int.class, int.class,
          int.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findNetworkByRecordID", String.class, int.class, int.class,
          int.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "getRecord", String.class, String.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "getRecord", String.class, String.class, long.class,
          StringBuffer.class));
      retrySet.add(cls.getMethod(
          "processRedoRecord", StringBuffer.class));
      retrySet.add(cls.getMethod(
          "processRedoRecordWithInfo",
                long.class, StringBuffer.class, StringBuffer.class));
      retrySet.add(cls.getMethod("process", String.class));
      retrySet.add(cls.getMethod(
          "process", String.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "processWithInfo",
                String.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findInterestingEntitiesByEntityID",
          long.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "findInterestingEntitiesByRecordID",
          String.class, String.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "whyEntityByRecordID", String.class, String.class,
          StringBuffer.class));
      retrySet.add(cls.getMethod(
          "whyEntityByRecordID",String.class, String.class, long.class,
          StringBuffer.class));
      retrySet.add(cls.getMethod(
          "whyEntityByEntityID", long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "whyEntityByEntityID", long.class, long.class,
          StringBuffer.class));
      retrySet.add(cls.getMethod(
          "whyRecords",String.class, String.class,
          String.class, String.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "whyRecords", String.class, String.class, String.class,
          String.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "whyEntities", long.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "whyEntities", long.class, long.class, long.class,
          StringBuffer.class));
      retrySet.add(cls.getMethod(
          "howEntityByEntityID",
          long.class, long.class, StringBuffer.class));
      retrySet.add(cls.getMethod(
          "howEntityByEntityID", long.class, StringBuffer.class));

      // check what we did not catch
      Method[] methods = cls.getMethods();
      boolean first = true;
      for (Method method : methods) {
       if (!directSet.contains(method)
           && !unsupportedSet.contains(method)
           && !retrySet.contains(method))
       {
         if (first) {
           System.out.println(
               multilineFormat(
               "Senzing API Server is running with a version of g2.jar "
               + "that is newer than the",
               "minimum required version.  The following G2Engine methods "
               + "will not be used:"));
           first = false;
         }
         System.out.println("    - " + method);
       }
      }

      // check for methods that cannot be retried
      for (Method method : retrySet) {
        Class returnType = method.getReturnType();
        if (returnType != int.class && returnType != long.class) {
          throw new ExceptionInInitializerError(
              multilineFormat(
                  "Cannot retry a method that does not have an int or "
                      + "long return type:",
                  method.toString()));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);

    } finally {
      UNSUPPORTED_METHODS = Collections.unmodifiableSet(unsupportedSet);
      DIRECT_METHODS = Collections.unmodifiableSet(directSet);
      RETRY_METHODS = Collections.unmodifiableSet(retrySet);
    }
  }

  /**
   * The backing {@link G2Engine} instance.
   */
  private G2Engine engineApi;

  /**
   * The {@link SzApiServer} that owns this instance.
   */
  private SzApiServer apiServer;

  /**
   * Constructs with the specified {@link G2Engine} instance and {@link
   * SzApiServer} instance.
   * @param engineApi The backing {@link G2Engine} instance.
   * @param apiServer The {@link SzApiServer} that owns this instance.
   */
  G2EngineRetryHandler(G2Engine engineApi, SzApiServer apiServer)
  {
    this.engineApi    = engineApi;
    this.apiServer    = apiServer;
  }

  /**
   * Handles invocation to retry the methods that should be retried, directly
   * call the methods that should be directly called without retry and throw
   * an UnsupportedOperatedException for all methods explicitly unsupported and
   * for those that are implicitly unsupported because they are newer than the
   * minimum required version of {@link G2Engine}.
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
    // check if explicitly unsupported
    if (UNSUPPORTED_METHODS.contains(method)) {
      throw new UnsupportedOperationException(
          multilineFormat(
              "The specified method is explicitly not supported through "
              + "this interface:",
              method.toString()));
    }

    // check if it should be directly called
    if (DIRECT_METHODS.contains(method)) {
      return method.invoke(this.engineApi, args);
    }

    // check if it should be retried if failure
    if (RETRY_METHODS.contains(method)) {
      Number  returnCode;
      boolean retried = false;
      do {
        returnCode = (Number) method.invoke(this.engineApi, args);
      } while (returnCode.intValue() != 0
               && (retried = this.checkRetryNeeded(retried)));

      // return the result
      return returnCode;
    }

    // if we get here then the method is not recognized
    throw new UnsupportedOperationException(
        multilineFormat(
            "The specified method is implicitly not supported through "
                + "this interface:",
            method.toString()));
  }

  /**
   * Checks if a retry is needed by comparing the active config ID to the
   * default config ID.  If they are the same then <tt>false</tt> is returned,
   * but if they differ then the {@link G2Engine} is reinitialized and
   * <tt>true</tt> is returned.
   *
   * @param retried Indicates if we have already retried once.
   * @return <tt>true</tt> if the last operation should be retried, otherwise
   *         <tt>false</tt>.
   */
  private boolean checkRetryNeeded(boolean retried) {
    if (retried) return false;

    Boolean result = this.apiServer.ensureConfigCurrent(false);
    if (result == null) return false;
    return result;
  }

}
