#!/bin/sh
"/Applications/Senzing.app/Contents/Resources/app/jre/Contents/Home/bin/java" -cp "/Applications/Senzing.app/Contents/Resources/app/dist/g2-webapp-server.jar" -Dbootstrap.script.name="$0" com.senzing.workbench.bootstrap.BootstrapApiServer "$@"
