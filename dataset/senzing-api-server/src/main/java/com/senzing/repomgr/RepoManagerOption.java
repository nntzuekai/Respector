package com.senzing.repomgr;

import com.senzing.cmdline.CommandLineOption;
import com.senzing.cmdline.ParameterProcessor;
import com.senzing.util.JsonUtilities;

import javax.json.JsonObject;
import java.io.File;
import java.util.*;

import static java.util.EnumSet.*;
import static java.util.Collections.*;

enum RepoManagerOption
    implements CommandLineOption<RepoManagerOption, RepoManagerOption>
{
  HELP("--help", true, 0),
  CREATE_REPO("--create-repo", true, 1),
  PURGE_REPO("--purge-repo", true, 0),
  LOAD_FILE("--load-file", true, 1),
  ADD_RECORD("--add-record", true, 1),
  CONFIG_SOURCES("--config-sources", true),
  DATA_SOURCE("--data-source", false, 1),
  REPOSITORY("--repo", false, 1),
  VERBOSE("--verbose", false,
          0, "false");

  RepoManagerOption(String commandLineFlag, String... defaultParameters) {
    this(commandLineFlag, false, -1, defaultParameters);
  }

  RepoManagerOption(String commandLineFlag,
                    boolean primary,
                    String... defaultParameters) {
    this(commandLineFlag, primary, -1, defaultParameters);
  }

  RepoManagerOption(String commandLineFlag,
                    int parameterCount,
                    String... defaultParameters) {
    this(commandLineFlag, false, parameterCount, defaultParameters);
  }

  RepoManagerOption(String commandLineFlag,
                    boolean primary,
                    int parameterCount,
                    String... defaultParameters) {
    this(commandLineFlag,
         primary,
         parameterCount < 0 ? 0 : parameterCount,
         parameterCount,
         defaultParameters);
  }

  RepoManagerOption(String commandLineFlag,
                    boolean primary,
                    int minParameterCount,
                    int maxParameterCount,
                    String... defaultParameters) {
    this.commandLineFlag = commandLineFlag;
    this.primary = primary;
    this.minParamCount = minParameterCount;
    this.maxParamCount = maxParameterCount;
    this.conflicts = null;
    this.dependencies = null;
    this.defaultParameters = (defaultParameters == null)
        ? Collections.emptyList() : Arrays.asList(defaultParameters);
  }

  private static Map<String, RepoManagerOption> OPTIONS_BY_FLAG;

  private String commandLineFlag;
  private boolean primary;
  private int minParamCount;
  private int maxParamCount;
  private Set<CommandLineOption> conflicts;
  private Set<Set<CommandLineOption>> dependencies;
  private List<String> defaultParameters;

  @Override
  public String getCommandLineFlag() {
    return this.commandLineFlag;
  }

  @Override
  public int getMinimumParameterCount() {
    return this.minParamCount;
  }

  @Override
  public int getMaximumParameterCount() {
    return this.maxParamCount;
  }

  @Override
  public boolean isPrimary() {
    return this.primary;
  }

  @Override
  public boolean isDeprecated() {
    return false;
  }

  @Override
  public Set<CommandLineOption> getConflicts() {
    return this.conflicts;
  }

  @Override
  public Set<Set<CommandLineOption>> getDependencies() {
    return this.dependencies;
  }

  public static RepoManagerOption lookup(String commandLineFlag) {
    return OPTIONS_BY_FLAG.get(commandLineFlag.toLowerCase());
  }

  static {
    Map<String, RepoManagerOption> lookupMap = new LinkedHashMap<>();
    for (RepoManagerOption opt : values()) {
      lookupMap.put(opt.getCommandLineFlag(), opt);
    }
    OPTIONS_BY_FLAG = Collections.unmodifiableMap(lookupMap);

    HELP.conflicts = unmodifiableSet(
        new LinkedHashSet<>(complementOf(EnumSet.of(HELP))));

    HELP.dependencies = singleton(emptySet());

    CREATE_REPO.conflicts = unmodifiableSet(new LinkedHashSet<>(
        complementOf(of(CREATE_REPO, VERBOSE))));

    CREATE_REPO.dependencies = singleton(emptySet());

    PURGE_REPO.conflicts = unmodifiableSet(new LinkedHashSet<>(
        complementOf(of(PURGE_REPO, REPOSITORY, VERBOSE))));

    PURGE_REPO.dependencies = singleton(singleton(REPOSITORY));
    LOAD_FILE.conflicts = unmodifiableSet(new LinkedHashSet<>(
        complementOf(
            of(LOAD_FILE, REPOSITORY, DATA_SOURCE, VERBOSE))));

    LOAD_FILE.dependencies = singleton(singleton(REPOSITORY));
    ADD_RECORD.conflicts = unmodifiableSet(new LinkedHashSet<>(
        complementOf(
            of(ADD_RECORD, REPOSITORY, DATA_SOURCE, VERBOSE))));

    ADD_RECORD.dependencies = singleton(singleton(REPOSITORY));

    CONFIG_SOURCES.conflicts = unmodifiableSet(new LinkedHashSet<>(
        complementOf(of(CONFIG_SOURCES, REPOSITORY, VERBOSE))));

    CONFIG_SOURCES.dependencies = singleton(singleton(REPOSITORY));

    DATA_SOURCE.conflicts = unmodifiableSet(new LinkedHashSet<>(
        complementOf(
            of(DATA_SOURCE, LOAD_FILE, ADD_RECORD, VERBOSE, REPOSITORY))));

    DATA_SOURCE.dependencies = singleton(emptySet());

    REPOSITORY.conflicts = Set.of(HELP, CREATE_REPO);
    REPOSITORY.dependencies = singleton(emptySet());
  }

  /**
   * The {@link ParameterProcessor} implementation for this class.
   */
  private static class ParamProcessor implements ParameterProcessor {
    /**
     * Processes the parameters for the specified option.
     *
     * @param option The {@link RepoManagerOption} to process.
     * @param params The {@link List} of parameters for the option.
     * @return The processed value.
     * @throws IllegalArgumentException If the specified {@link
     *                                  CommandLineOption} is not an instance
     *                                  of {@link RepoManagerOption} or is
     *                                  otherwise unrecognized.
     */
    public Object process(CommandLineOption option,
                          List<String>      params) {
      if (!(option instanceof RepoManagerOption)) {
        throw new IllegalArgumentException(
            "Unhandled command line option: " + option.getCommandLineFlag()
                + " / " + option);
      }
      // down-cast
      RepoManagerOption repoMgrOption = (RepoManagerOption) option;

      // switch on the option
      switch (repoMgrOption) {
        case HELP:
        case VERBOSE:
        case PURGE_REPO:
          return Boolean.TRUE;

        case CREATE_REPO: {
          File repoDirectory = new File(params.get(0));
          if (repoDirectory.exists()) {
            throw new IllegalArgumentException(
                "Specified repository directory file path "
                    + "already exists: " + repoDirectory);
          }
          return repoDirectory;
        }
        case REPOSITORY: {
          File repoDirectory = new File(params.get(0));
          validateRepositoryDirectory(repoDirectory);
          return repoDirectory;
        }

        case LOAD_FILE:
          File sourceFile = new File(params.get(0));
          validateSourceFile(sourceFile);
          return sourceFile;

        case ADD_RECORD:
          String jsonRecord = params.get(0);
          validateJsonRecord(jsonRecord);
          return jsonRecord;

        case CONFIG_SOURCES:
          Set<String> sources = new LinkedHashSet<>(params);
          if (sources.size() == 0) {
            throw new IllegalArgumentException(
                "No data source names were provided for the "
                    + option.getCommandLineFlag() + " option");
          }
          return sources;

        case DATA_SOURCE:
          String dataSource = params.get(0);
          return dataSource;

        default:
          throw new IllegalArgumentException(
              "Unhandled command line option: "
                  + option.getCommandLineFlag()
                  + " / " + option);
      }
    }
  }

  /**
   * Validates the specified JSON for loading.
   */
  private static void validateJsonRecord(String jsonRecord) {
    JsonObject jsonObject;
    try {
      jsonObject = JsonUtilities.parseJsonObject(jsonRecord);

    } catch (Exception e) {
      String msg = "The provided JSON record is invalid for loading: "
          + jsonRecord;
      System.err.println();
      System.err.println(msg);
      throw new IllegalArgumentException(msg);
    }

    if (jsonObject.size() == 0) {
      String msg = "The provided JSON record has no properties: " + jsonRecord;
      System.err.println();
      System.err.println(msg);
      throw new IllegalArgumentException(msg);
    }
  }

  /**
   * Validates that the specified file exists, is not a directory and appears
   * to be a CSV or a JSON file for loading.
   */
  private static void validateSourceFile(File sourceFile) {
    if (!sourceFile.exists()) {
      String msg = "Specified file does not exist: " + sourceFile;
      System.err.println();
      System.err.println(msg);
      throw new IllegalArgumentException(msg);
    }
    if (sourceFile.isDirectory()) {
      String msg = "Specified file exists, but is a directory: " + sourceFile;
      System.err.println();
      System.err.println(msg);
      throw new IllegalArgumentException(msg);
    }
    String fileName = sourceFile.toString().toUpperCase();
    if (!fileName.endsWith(".JSON") && !fileName.endsWith(".CSV")) {
      String msg = "Specified file must be CSV or JSON: " + fileName;
      System.err.println();
      System.err.println(msg);
      throw new IllegalArgumentException(msg);
    }
  }

  /**
   * Validates a repository directory specified in the command-line arguments.
   *
   */
  private static void validateRepositoryDirectory(File directory) {
    if (!directory.exists()) {
      String msg = "Specified repository directory path does not exist: "
          + directory;

      System.err.println();
      System.err.println(msg);
      throw new IllegalArgumentException(msg);
    }
    if (!directory.isDirectory()) {
      String msg = "Specified repository directory path exists, but is not a "
          + "directory: " + directory;

      System.err.println();
      System.err.println(msg);
      throw new IllegalArgumentException(msg);
    }

    File iniFile = new File(directory, "g2-init.json");
    if (!iniFile.exists() || iniFile.isDirectory()) {
      String msg = "Specified repository directory path exists, but does not "
          + "contain a g2-init.json file: " + directory;

      System.err.println();
      System.err.println(msg);
      throw new IllegalArgumentException(msg);
    }
  }

  /**
   * The {@link ParameterProcessor} for {@link RepoManagerOption}.
   * This instance will only handle instances of {@link CommandLineOption}
   * instances of type {@link RepoManagerOption}.
   */
  public static final ParameterProcessor PARAMETER_PROCESSOR
      = new ParamProcessor();

}
