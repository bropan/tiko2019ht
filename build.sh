#!/bin/bash

DRIVERDIRECTORY="./driver"
DRIVERFILE="$DRIVERDIRECTORY/postgresql-42.2.5.jar"
DRIVERLINK="https://www.sis.uta.fi/~tiko/materiaali/19/jdbcohje/postgresql-42.2.5.jar"

echo "Building tiko2019ht..."
success=true

if [ ! -f "$DRIVERFILE" ]
then
    echo "The PSQL Driver file $DRIVERFILE does not exist"
    echo "Downloading $DRIVERFILE from $DRIVERLINK"
    echo "Enter website username:"
    read username
    echo "Enter website password:"
    read password
    wget --user "$username" --password "$password" -P "$DRIVERDIRECTORY/" "$DRIVERLINK"
    if [ $? -ne 0 ] 
    then 
        echo "Download failed!"
        success=false 
    fi
fi


javac                   \
        src/Main.java   \
        src/Init.java   \
        src/CLI.java    \
        src/Utils.java  \
        src/LoginCredentials.java \
        src/DatabaseStructureHandler.java \

if [ $? -ne 0 ]; 
then 
    echo "Compilation failed!"
    success=false 
fi

if [ "$success" = true ] ; then
    echo "Built tiko2019ht succesfully."
else
    echo "Build failed."
fi
