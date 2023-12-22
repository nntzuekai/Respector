package com.senzing.api;

import java.io.*;

import static com.senzing.util.OperatingSystemFamily.*;

public class GenerateTestJVMScript {
  public static void main(String[] args) {
    String targetFileName = "target/java-wrapper/bin/java-wrapper.bat";
    if (args.length > 0) {
      targetFileName = args[0];
    }
    File targetFile = new File(targetFileName);
    File targetDir = targetFile.getParentFile();
    if (targetDir != null && !targetDir.exists()) {
      targetDir.mkdirs();
    }
    String  senzingDirPath  = System.getProperty("senzing.install.dir");
    String  javaHome        = System.getProperty("java.home");
    File    javaHomeDir     = new File(javaHome);
    File    javaBinDir      = new File(javaHomeDir, "bin");
    String  javaExeName     = (RUNTIME_OS_FAMILY == WINDOWS) ? "javaw.exe" : "java";
    File    javaExecutable  = new File(javaBinDir, javaExeName);
    File    senzingDir      = (senzingDirPath != null && senzingDirPath.trim().length() > 0)
                              ? new File(senzingDirPath) : null;

    // normalize the senzing directory path
    if (senzingDir != null) {
      String dirName = senzingDir.getName();
      if (senzingDir.exists() && senzingDir.isDirectory()
          && !dirName.equalsIgnoreCase("g2")) {
        if (RUNTIME_OS_FAMILY == MAC_OS) {
          // for macOS be tolerant of Senzing.app or the electron app dir
          if (dirName.equalsIgnoreCase("Senzing.app")) {
            File contents = new File(senzingDir, "Contents");
            File resources = new File(contents, "Resources");
            senzingDir = new File(resources, "app");
            dirName = senzingDir.getName();
          }
          if (dirName.equalsIgnoreCase("app")) {
            senzingDir = new File(senzingDir, "g2");
          }

        } else if (dirName.equalsIgnoreCase("senzing")) {
          // for windows or linux allow the "Senzing" dir as well
          senzingDir = new File(senzingDir, "g2");
        }
        senzingDirPath = senzingDir.toString();
        senzingDir = null;
      }
    }

    System.out.println("*********************************************");
    System.out.println();
    System.out.println("senzing.install.dir = " + senzingDirPath);
    System.out.println("java.home = " + javaHome);
    System.out.println();
    System.out.println("*********************************************");

    File libDir = null;
    File platformLibDir = null;
    String pathSep = ":";
    String quote = "\"";
    if (senzingDirPath != null && senzingDirPath.trim().length() > 0) {
      senzingDir = new File(senzingDirPath);
    }
    switch (RUNTIME_OS_FAMILY) {
      case WINDOWS:
        if (senzingDir == null) {
          senzingDir = new File("C:\\Program Files\\Senzing\\g2");
        }
        libDir = new File(senzingDir, "lib");
        pathSep = ";";
        quote = "";
        break;
      case MAC_OS:
        if (senzingDir == null) {
          senzingDir = new File("/Applications/Senzing.app/Contents/Resources/app/g2/");
        }
        libDir = new File(senzingDir, "lib");
        platformLibDir = new File(libDir, "macos");
        break;
      case UNIX:
        if (senzingDir == null) {
          senzingDir = new File("/opt/senzing/g2");
        }
        libDir = new File(senzingDir, "lib");
        platformLibDir = new File(libDir, "debian");
        break;
    }

    String libraryPath = quote + libDir.toString() + quote
        + ((platformLibDir != null)
           ? pathSep + quote + platformLibDir + quote : "");

    try {
      try (FileOutputStream fos = new FileOutputStream(targetFile);
           OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
           PrintWriter pw = new PrintWriter(osw))
      {
        if (RUNTIME_OS_FAMILY == WINDOWS) {
          pw.println("@echo off");
          pw.println("set Path=" + libraryPath + ";%Path%");
          pw.println("\"" + javaExecutable.toString() + "\" %*");

        } else {
          pw.println("#!/bin/sh");
          if (RUNTIME_OS_FAMILY == MAC_OS) {
            pw.println("export DYLD_LIBRARY_PATH=" + libraryPath
                           + ":$DYLD_LIBRARY_PATH");
          }
          pw.println("export LD_LIBRARY_PATH=" + libraryPath
                     + ":$LD_LIBRARY_PATH");
          pw.println("\"" + javaExecutable.toString() + "\" \"$@\"");
        }
      }
      targetFile.setExecutable(true);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
