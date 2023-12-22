# Senzing&reg; App Integration Scripts

## Overview

The Senzing&reg; REST API Server requires an entity repository and INI file describing how to
connect to that repository.  One of the easiest ways to create an entity repository is to use
the [Senzing&reg; app](https://senzing.com/#download).  However, the app maintains its data
internally which can be difficult to get to.  That is why scripts are being provided to make
accessing this data through the Senzing REST API Server easier.  The scripts are:

* `run-rest-api-server.bat` (Windows)
* `run-rest-api-server.sh` (macOS)

### Availability

Senzing app version 1.6 and later includes a `"Developer"` menu.  Within that menu is the 
option to `"Get REST API Server Script"`.  This will generate a Bourne/Bash shell (.sh) 
script on macOS and a Batch (.bat) script on Windows and prompt you to save that script.  
This release will also include the logic required to support the generated script.

Attempting to use the script with an older version of the Senzing app will lead to an error
indicating that the REST API Server did not start.

### Default Scripts

When generated from the Senzing app, the scripts will automatically include the proper paths
to whatever location that the Senzing app was installed.  However, versions of the scripts
are provided here using the default installation locations:

* Windows: `C:\Program Files\Senzing\`
* macOS: `/Applications/Senzing` (actually `/Applications/Senzing.app`)

### Projects

By default the Senzing app provides you with a single project (i.e.: entity repository) called
`"My Project"`.  This can be renamed as you see fit.  The project is assigned an internal numeric
identifier as well.  In the case of the default project that identifier is `1`.  This project
has an INI file and a backing SQLite database.

Through the Senzing app's `"Preferences"` menu you can enable multiple projects.  Doing so allows
you to create multiple entity repositories (each with their own INI file and backing SQLite
database).

The app integration scripts let you choose which project you want to use when launching
the Senzing API Server.  Only one project can be used per running instance of the Senzing REST
API Server.

### Warnings

By default the app integration script will not start the Senzing REST API Server if it detects
that the Senzing app is currently running.  You can force startup using the `--force` option
on the script's command line.

The reason for this is that some operations in the Senzing app operations can cause the
Senzing REST API Server to exhibit errors.  When these operations occur in the Senzing app,
the Senzing REST API Server needs to be restarted.  Specifically, the operations that can
cause problems are:

* Any operation in the Senzing app that triggers a "purge" of the repository, for example:
  * Deleting a data source that has been loaded
  * Changing the mapping on a data source that has been loaded
* An operation that modifies the configuration of the repository, for example:
  * Deleting a "Ready to Load" data source
  * Adding a new data source

However, if you know that your repository is not changing in any way that can cause a problem and
you want to be able to simultaneously review the repository through the Senzing app while running
the Senzing REST API Server for development then feel free to use the `--force` option on startup.

**NOTE:** If you start the Senzing app **after** running the app integrations script to start the
Senzing REST API Server, you will **not** receive any warning.

### Running

Running the script is quite simple as it sets up the environment all by itself
and even makes use of the Java version embedded with the Senzing app to execute
the Senzing REST API Server.

To run the script you will need to provide the following on the command line:

* Optionally specify the `--force` option as described above
* Identify the project you want to use by its name or numeric identifier.
* Provide the path to the `senzing-api-server.jar` file.
* Provide any additional options for `senzing-api-server.jar` after the JAR file path.

**NOTE:** You generally identify a project to start with as that is the point of using
these scripts.  However, you can specify a project identifier of zero (0) and still
provide the `-iniFile` option to the `senzing-api-server.jar`.  If you try to specify
an actual project **and** also specify the `-iniFile` option then you will get an error
message from the script telling you that this is not allowed.

**ALSO NOTE:** Specifying no command line arguments from the script will generate a usage message (as
will any malformed or incorrect command-line options).  If you need to know the available project
names and project identifiers, this is a good way to get a list of them as the usage message will
list them for you.

```console
USAGE: run-rest-api-server.sh [--force] <project_name|project_id> <senzing-api-server-jar-path> [api-server-options*]

EXAMPLE: run-rest-api-server.sh --force "My Project" ./senzing-api-server.jar -httpPort 8080

   --force  Specify this to force the API Server to start even if
            the Senzing app is currently running.

   <project_name|project_id>
            Either a project name or project ID.  Specify zero (0)
            if you intend on providing the -iniFile option to the API
            Server.  Otherwise, the available project names and
            associated project IDs are:
              - My Project (1)

   <senzing-api-server-jar-path>
            The path to the senzing-api-server-[version].jar file
            that was built from the associated github project.
            See https://github.com/Senzing/senzing-api-server


   [api-server-options*]
            Options to pass to the senzing-api-server-[version].jar.
            Any option may be specified except for "-iniFile" which
            is already passed in by this bootstrapper.

```

### Terminating

The script will run and wait for the Senzing REST API Server to terminate before exiting.
If you want to terminate the script (and the Senzing REST API Server) early, simply
press `CTRL-C` in the shell.