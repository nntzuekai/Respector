package com.senzing.nativeapi;

import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.senzing.io.IOUtilities.readTextFileAsString;
import static com.senzing.util.OperatingSystemFamily.MAC_OS;
import static com.senzing.util.OperatingSystemFamily.RUNTIME_OS_FAMILY;

/**
 * Describes the directories on disk used to find the Senzing product
 * installation and the support directories.
 */
public class InstallLocations {
  /**
   * The installation location.
   */
  private File installDir;

  /**
   * The location of the configuration files for the config directory.
   */
  private File configDir;

  /**
   * The location of the resource files for the resource directory.
   */
  private File resourceDir;

  /**
   * The location of the support files for the support directory.
   */
  private File supportDir;

  /**
   * The location of the template files for the template directory.
   */
  private File templatesDir;

  /**
   * Default constructor.
   */
  private InstallLocations() {
    this.installDir   = null;
    this.configDir    = null;
    this.resourceDir  = null;
    this.supportDir   = null;
    this.templatesDir = null;
  }

  /**
   * Gets the primary installation directory.
   *
   * @return The primary installation directory.
   */
  public File getInstallDirectory() {
    return this.installDir;
  }

  /**
   * Gets the configuration directory.
   *
   * @return The configuration directory.
   */
  public File getConfigDirectory() {
    return this.configDir;
  }

  /**
   * Gets the resource directory.
   *
   * @return The resource directory.
   */
  public File getResourceDirectory() {
    return this.resourceDir;
  }

  /**
   * Gets the support directory.
   *
   * @return The support directory.
   */
  public File getSupportDirectory() {
    return this.supportDir;
  }

  /**
   * Gets the templates directory.
   *
   * @return The templates directory.
   */
  public File getTemplatesDirectory() {
    return this.templatesDir;
  }

  /**
   * Finds the install directories and returns the {@link InstallLocations}
   * instance describing those locations.
   *
   * @return The {@link InstallLocations} instance describing the install
   *         locations.
   */
  public static InstallLocations findLocations() {
    File installDir   = null;
    File configDir    = null;
    File resourceDir  = null;
    File supportDir   = null;
    File templatesDir = null;
    try {
      String defaultInstallPath;
      String defaultConfigPath = null;

      switch (RUNTIME_OS_FAMILY) {
        case WINDOWS:
          defaultInstallPath = "C:\\Program Files\\Senzing\\g2";
          break;
        case MAC_OS:
          defaultInstallPath = "/Applications/Senzing.app/Contents/Resources/app/g2";
          break;
        case UNIX:
          defaultInstallPath = "/opt/senzing/g2";
          defaultConfigPath = "/etc/opt/senzing";
          break;
        default:
          throw new IllegalStateException(
              "Unrecognized Operating System: " + RUNTIME_OS_FAMILY);
      }

      // check for senzing system properties
      String installPath  = System.getProperty("senzing.install.dir");
      String configPath   = System.getProperty("senzing.config.dir");
      String supportPath = System.getProperty("senzing.support.dir");
      String resourcePath = System.getProperty("senzing.resource.dir");

      // try environment variables if system properties don't work
      if (installPath == null || installPath.trim().length() == 0) {
        installPath = System.getenv("SENZING_G2_DIR");
      }
      if (configPath == null || configPath.trim().length() == 0) {
        configPath = System.getenv("SENZING_ETC_DIR");
      }
      if (supportPath == null || supportPath.trim().length() == 0) {
        supportPath = System.getenv("SENZING_DATA_DIR");
      }

      // normalize empty strings as null
      if (installPath != null && installPath.trim().length() == 0) {
        installPath = null;
      }
      if (configPath != null && configPath.trim().length() == 0) {
        configPath = null;
      }
      if (supportPath != null && supportPath.trim().length() == 0) {
        supportPath = null;
      }
      if (resourcePath != null && resourcePath.trim().length() == 0) {
        resourcePath = null;
      }

      // check the senzing directory
      installDir = new File(installPath == null ? defaultInstallPath : installPath);
      if (!installDir.exists()) {
        System.err.println("Could not find Senzing installation directory:");
        System.err.println("     " + installDir);
        System.err.println();
        if (installPath != null) {
          System.err.println(
              "Check the -Dsenzing.install.dir=[path] command line option.");
        } else {
          System.err.println(
              "Use the -Dsenzing.install.dir=[path] command line option to "
                  + "specify a path");
        }

        return null;
      }

      // normalize the senzing directory
      String dirName = installDir.getName();
      if (installDir.isDirectory()
          && !dirName.equalsIgnoreCase("g2")) {
        if (RUNTIME_OS_FAMILY == MAC_OS) {
          // for macOS be tolerant of Senzing.app or the electron app dir
          if (dirName.equalsIgnoreCase("Senzing.app")) {
            File contents = new File(installDir, "Contents");
            File resources = new File(contents, "Resources");
            installDir = new File(resources, "app");
            dirName = installDir.getName();
          }
          if (dirName.equalsIgnoreCase("app")) {
            installDir = new File(installDir, "g2");
          }
        } else if (dirName.equalsIgnoreCase("senzing")) {
          // for windows or linux allow the "Senzing" dir as well
          installDir = new File(installDir, "g2");
        }
      }

      if (!installDir.isDirectory()) {
        System.err.println("Senzing installation directory appears invalid:");
        System.err.println("     " + installDir);
        System.err.println();
        if (installPath != null) {
          System.err.println(
              "Check the -Dsenzing.install.dir=[path] command line option.");
        } else {
          System.err.println(
              "Use the -Dsenzing.install.dir=[path] command line option to "
                  + "specify a path");
        }

        return null;
      }

      if (supportPath == null || supportPath.trim().length() == 0) {
        // try to determine the support path
        File installParent = installDir.getParentFile();
        File dataRoot = new File(installParent, "data");
        if (dataRoot.exists() && dataRoot.isDirectory()) {
          File versionFile = new File(installDir, "g2BuildVersion.json");
          String dataVersion = null;
          if (versionFile.exists()) {
            String text = readTextFileAsString(versionFile, "UTF-8");
            JsonObject jsonObject = JsonUtilities.parseJsonObject(text);
            dataVersion = JsonUtilities.getString(jsonObject, "DATA_VERSION");
          }

          // try the data version directory
          supportDir = (dataVersion == null)
              ? null : new File(dataRoot, dataVersion.trim());

          // check if data version was not found
          if (supportDir == null || !supportDir.exists()) {
            // look to see if we only have one data version installed
            File[] versionDirs = dataRoot.listFiles( f -> {
              return f.getName().matches("\\d+\\.\\d+\\.\\d+");
            });
            if (versionDirs.length == 1 && supportDir == null) {
              // use the single data version found
              supportDir = versionDirs[0];

            } else if (versionDirs.length > 1) {
              System.err.println(
                  "Could not infer support directory.  Multiple data "
                      + "directory versions at: ");
              System.err.println("     " + dataRoot);
              if (supportDir != null) {
                System.err.println();
                System.err.println("Expected to find: " + supportDir);
              }
              throw new InvalidInstallationException(
                  ((supportDir == null)
                     ? "Could not infer support directory."
                     : "Could not find support directory (" + supportDir + ").")
                   + "  Multiple data directory versions found at: "
                   + dataRoot);
            } else {
              // no version directories were found, maybe the data root is
              // the actual support directory (mapped in a docker image)
              File[] ibmFiles = dataRoot.listFiles(f -> {
                return f.getName().toLowerCase().endsWith(".ibm");
              });
              File libPostalDir = new File(dataRoot, "libpostal");

              // require the .ibm files and libpostal to exist
              if (ibmFiles.length > 0 && libPostalDir.exists()) {
                supportDir = dataRoot;
              }
            }
          }

        }
        if (supportDir == null) {
          // use the default path
          supportDir = new File(installDir, "data");
        }

      } else {
        // use the specified explicit path
        supportDir = new File(supportPath);
      }

      if (!supportDir.exists()) {
        System.err.println("The support directory does not exist:");
        System.err.println("         " + supportDir);
        if (supportPath != null) {
          System.err.println(
              "Check the -Dsenzing.support.dir=[path] command line option.");
        } else {
          System.err.println(
              "Use the -Dsenzing.support.dir=[path] command line option to "
                  + "specify a path");
        }

        throw new InvalidInstallationException(
            "The support directory does not exist: " + supportDir);
      }

      if (!supportDir.isDirectory()) {
        System.err.println("The support directory is invalid:");
        System.err.println("         " + supportDir);
        if (supportPath != null) {
          System.err.println(
              "Check the -Dsenzing.support.dir=[path] command line option.");
        } else {
          System.err.println(
              "Use the -Dsenzing.support.dir=[path] command line option to "
                  + "specify a path");
        }
        throw new InvalidInstallationException(
            "The support directory is invalid: " + supportDir);

      }

      // check the config path
      if (configPath != null) {
        configDir = new File(configPath);
      }
      if (configDir == null && defaultConfigPath != null) {
        configDir = new File(defaultConfigPath);
        if (!configDir.exists()) configDir = null;
      }
      if (configPath != null && !configDir.exists()) {
        System.err.println(
            "The -Dsenzing.config.dir=[path] option specifies a path that does not exist:");
        System.err.println(
            "         " + configPath);

        throw new InvalidInstallationException(
            "Explicit config path does not exist: " + configPath);
      }
      if (configDir != null && configDir.exists()) {
        if (!configDir.isDirectory()) {
          System.err.println(
              "The -Dsenzing.config.dir=[path] option specifies a file, not a directory:");
          System.err.println(
              "         " + configPath);

          throw new InvalidInstallationException(
              "Explicit config path is not directory: " + configPath);
        }

        String[] requiredFiles = { "cfgVariant.json" };
        List<String> missingFiles = new ArrayList<>(requiredFiles.length);

        for (String fileName : requiredFiles) {
          File configFile   = new File(configDir, fileName);
          File supportFile  = new File(supportDir, fileName);
          if (!configFile.exists() && !supportFile.exists()) {
            missingFiles.add(fileName);
          }
        }
        if (missingFiles.size() > 0 && configPath != null) {
          System.err.println(
              "The -Dsenzing.config.dir=[path] option specifies an invalid config directory:");
          for (String missing : missingFiles) {
            System.err.println(
                "         " + missing + " was not found");
          }
          throw new InvalidInstallationException(
              "Explicit config path missing required files: " + missingFiles);
        }
      }

      // now determine the resource path
      resourceDir = (resourcePath == null) ? null : new File(resourcePath);
      if (resourceDir == null) {
        resourceDir = new File(installDir, "resources");
      }
      if (resourceDir != null && resourceDir.exists()
          && resourceDir.isDirectory())
      {
        templatesDir = new File(resourceDir, "templates");
      }

      if (resourcePath != null) {
        if (!resourceDir.exists()) {
          System.err.println(
              "The -Dsenzing.resource.dir=[path] option specifies a path that does not exist:");
          System.err.println(
              "         " + resourcePath);

          throw new InvalidInstallationException(
              "Explicit resource path does not exist: " + resourcePath);
        }

        if (!resourceDir.isDirectory()
            || !templatesDir.exists()
            || !templatesDir.isDirectory())
        {
          System.err.println(
              "The -Dsenzing.resource.dir=[path] option specifies an invalid "
                  + "resource directory:");
          System.err.println("         " + resourcePath);

          throw new InvalidInstallationException(
              "Explicit resource path is not valid: " + resourcePath);
        }

      } else if (!resourceDir.exists() || !resourceDir.isDirectory()
          || !templatesDir.exists() || !templatesDir.isDirectory())
      {
        resourceDir   = null;
        templatesDir  = null;
      }

      // construct and initialize the result
      InstallLocations result = new InstallLocations();
      result.installDir       = installDir;
      result.configDir        = configDir;
      result.supportDir       = supportDir;
      result.resourceDir      = resourceDir;
      result.templatesDir     = templatesDir;

      // return the result
      return result;

    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
