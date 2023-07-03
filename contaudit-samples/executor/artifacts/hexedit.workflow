#! /bin/sh
echo '[hexedit_workflow] Checking if package Hexedit exists...'
if [ $(dpkg -s hexedit | grep -c "installed") -eq 0 ]
then
    echo '[hexedit_workflow] Package not exists! Installing...'
    sudo apt-get install hexedit -y
else
    echo '[hexedit_workflow] Package exists! Removing...'
    sudo apt-get remove hexedit -y
fi