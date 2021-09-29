#!/bin/bash

clear

set echo off

echo "Compilando"

javac Server.java
javac Client.java

echo "Executando"

readonly OUTRO_SCRIPT="server.sh";
chmod +x $OUTRO_SCRIPT;
gnome-terminal -- bash -c "./$OUTRO_SCRIPT; exec $SHELL";

readonly OUTRO_SCRIPT1="client.sh";
chmod +x $OUTRO_SCRIPT1;
gnome-terminal -- bash -c "./$OUTRO_SCRIPT1; exec $SHELL";

gnome-terminal -- bash -c "./$OUTRO_SCRIPT1; exec $SHELL";

gnome-terminal -- bash -c "./$OUTRO_SCRIPT1; exec $SHELL";

exit
