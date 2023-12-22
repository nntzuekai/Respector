@echo off
"C:\Program Files\Senzing\g2\workbench\resources\app\jre\bin\java" -cp "C:\Program Files\Senzing\g2\workbench\resources\app\dist\g2-webapp-server.jar" -Dbootstrap.script.name=%~0 com.senzing.workbench.bootstrap.BootstrapApiServer %*
