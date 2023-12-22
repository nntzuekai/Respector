package com.senzing.datagen;

import com.senzing.cmdline.CommandLineOption;
import com.senzing.cmdline.ParameterProcessor;

import java.io.File;
import java.util.*;

import static java.util.Collections.*;

enum DataGeneratorOption
    implements CommandLineOption<DataGeneratorOption, DataGeneratorOption>
{
  HELP("-help", 0),
  PERSON_COUNT("-personCount", 1),
  ORGANIZATION_COUNT("-orgCount", 1),
  BUSINESS_COUNT("-bizCount", 1),
  PERSON_SOURCES("-personSources", 1, -1),
  ORGANIZATION_SOURCES("-orgSources", 1, -1),
  BUSINESS_SOURCES("-bizSources", 1, -1),
  DEFAULT_NO_FEATURES("-defaultNoFeatures"),
  MAX_NAME_COUNT("-maxNames", 1),
  MAX_BIRTH_DATE_COUNT("-maxBirthDates", 1),
  MAX_ADDRESS_COUNT("-maxAddresses", 1),
  MAX_PHONE_COUNT("-maxPhones", 1),
  MAX_EMAIL_COUNT("-maxEmails", 1),
  NAME_DENSITY("-nameDensity", 1),
  BIRTH_DATE_DENSITY("-birthDateDensity", 1),
  ADDRESS_DENSITY("-addressDensity", 1),
  PHONE_DENSITY("-phoneDensity", 1),
  EMAIL_DENSITY("-emailDensity", 1),
  WITH_RECORD_IDS("-withRecordIds", 0),
  FULL_VALUES("-fullValues", 0),
  FLATTEN("-flatten", 0),
  SEED("-seed", 1),
  CSV_FILE("-csvFile", 1),
  JSON_FILE("-jsonFile", 1),
  JSON_LINES_FILE("-jsonLinesFile", 1),
  OVERWRITE("-overwrite", 0),
  PRETTY_PRINT("-prettyPrint", 0);

  DataGeneratorOption(String commandLineFlag) {
    this(commandLineFlag, false, -1);
  }

  DataGeneratorOption(String commandLineFlag, boolean primary) {
    this(commandLineFlag, primary, -1);
  }

  DataGeneratorOption(String commandLineFlag, int parameterCount) {
    this(commandLineFlag, false, parameterCount);
  }

  DataGeneratorOption(String commandLineFlag,
                      boolean primary,
                      int parameterCount) {
    this(commandLineFlag,
         primary,
         (parameterCount < 0) ? 0 : parameterCount,
         parameterCount);
  }

  DataGeneratorOption(String commandLineFlag,
                      int minParameterCount,
                      int maxParameterCount) {
    this(commandLineFlag, false, minParameterCount, maxParameterCount);
  }

  DataGeneratorOption(String commandLineFlag,
                      boolean primary,
                      int minParameterCount,
                      int maxParameterCount) {
    this.commandLineFlag = commandLineFlag;
    this.primary = primary;
    this.minParamCount = minParameterCount;
    this.maxParamCount = maxParameterCount;
    this.conflicts = new LinkedHashSet<>();
    this.dependencies = null;
  }

  private static Map<String, DataGeneratorOption> OPTIONS_BY_FLAG;

  private String commandLineFlag;
  private int minParamCount;
  private int maxParamCount;
  private boolean primary;
  private Set<CommandLineOption> conflicts;
  private Set<Set<CommandLineOption>> dependencies;

  public String getCommandLineFlag() {
    return this.commandLineFlag;
  }

  public int getMinimumParameterCount() {
    return this.minParamCount;
  }

  public int getMaximumParameterCount() {
    return this.maxParamCount;
  }

  public boolean isPrimary() {
    return this.primary;
  }

  public boolean isDeprecated() {
    return false;
  }

  ;

  public Set<CommandLineOption> getConflicts() {
    return this.conflicts;
  }

  public Set<Set<CommandLineOption>> getDependencies() {
    return this.dependencies;
  }

  public static DataGeneratorOption lookup(String commandLineFlag) {
    return OPTIONS_BY_FLAG.get(commandLineFlag.toLowerCase());
  }

  static {
    try {
      Map<String, DataGeneratorOption> lookupMap = new LinkedHashMap<>();
      for (DataGeneratorOption opt : values()) {
        lookupMap.put(opt.getCommandLineFlag(), opt);
      }
      OPTIONS_BY_FLAG = Collections.unmodifiableMap(lookupMap);

      Set<Set<CommandLineOption>> nodeps = singleton(emptySet());
      HELP.dependencies = nodeps;

      DataGeneratorOption[] exclusiveOptions = {HELP};
      for (DataGeneratorOption option : DataGeneratorOption.values()) {
        for (DataGeneratorOption exclOption : exclusiveOptions) {
          if (option == exclOption) continue;
          exclOption.conflicts.add(option);
          option.conflicts.add(exclOption);
        }
      }

      for (DataGeneratorOption option : DataGeneratorOption.values()) {
        option.conflicts = unmodifiableSet(option.conflicts);
      }

      Set<Set<CommandLineOption>> outputFileDependencies
          = new LinkedHashSet<>();
      outputFileDependencies.add(singleton(CSV_FILE));
      outputFileDependencies.add(singleton(JSON_FILE));
      outputFileDependencies.add(singleton(JSON_LINES_FILE));
      outputFileDependencies
          = Collections.unmodifiableSet(outputFileDependencies);

      OVERWRITE.dependencies = outputFileDependencies;
      PRETTY_PRINT.dependencies = singleton(singleton(JSON_FILE));
      ORGANIZATION_SOURCES.dependencies
          = singleton(singleton(ORGANIZATION_COUNT));
      BUSINESS_SOURCES.dependencies
          = singleton(singleton(BUSINESS_COUNT));
      PERSON_SOURCES.dependencies
          = singleton(singleton(PERSON_COUNT));

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);
    }
  }

  /**
   * The {@link ParameterProcessor} implementation for this class.
   */
  private static class ParamProcessor implements ParameterProcessor {
    /**
     * Processes the parameters for the specified option.
     *
     * @param option The {@link DataGeneratorOption} to process.
     * @param params The {@link List} of parameters for the option.
     * @return The processed value.
     * @throws IllegalArgumentException If the specified {@link
     *                                  CommandLineOption} is not an instance
     *                                  of {@link DataGeneratorOption} or is
     *                                  otherwise unrecognized.
     */
    public Object process(CommandLineOption option,
                          List<String> params) {
      if (!(option instanceof DataGeneratorOption)) {
        throw new IllegalArgumentException(
            "Unhandled command line option: " + option.getCommandLineFlag()
                + " / " + option);
      }

      // down-cast
      DataGeneratorOption dataGenOption = (DataGeneratorOption) option;

      // switch on the option
      switch (dataGenOption) {
        case HELP:
        case FULL_VALUES:
        case DEFAULT_NO_FEATURES:
        case FLATTEN:
        case OVERWRITE:
        case PRETTY_PRINT:
        case WITH_RECORD_IDS:
          return Boolean.TRUE;

        case CSV_FILE:
        case JSON_FILE:
        case JSON_LINES_FILE:
          return new File(params.get(0));

        case PERSON_COUNT:
        case ORGANIZATION_COUNT:
        case BUSINESS_COUNT:
        case MAX_NAME_COUNT:
        case MAX_BIRTH_DATE_COUNT:
        case MAX_ADDRESS_COUNT:
        case MAX_PHONE_COUNT:
        case MAX_EMAIL_COUNT:
          try {
            int count = Integer.parseInt(params.get(0));
            if (count < 0) throw new IllegalArgumentException();
            return count;
          } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "The " + option.getCommandLineFlag() + " option requires "
                    + " a non-negative integer: " + params.get(0));
          }

        case PERSON_SOURCES:
        case ORGANIZATION_SOURCES:
        case BUSINESS_SOURCES:
          return new ArrayList<>(params);

        case NAME_DENSITY:
        case BIRTH_DATE_DENSITY:
        case ADDRESS_DENSITY:
        case PHONE_DENSITY:
        case EMAIL_DENSITY:
          return FeatureDensity.valueOf(
              params.get(0).replace('-','_').toUpperCase());

        case SEED:
          return Long.parseLong(params.get(0));

        default:
          throw new IllegalArgumentException(
              "Unhandled command line option: "
                  + option.getCommandLineFlag()
                  + " / " + option);
      }
    }
  }

  /**
   * The {@link ParameterProcessor} for {@link DataGeneratorOption}.
   * This instance will only handle instances of {@link CommandLineOption}
   * instances of type {@link DataGeneratorOption}.
   */
  public static final ParameterProcessor PARAMETER_PROCESSOR
      = new ParamProcessor();
}
