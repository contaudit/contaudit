#! /bin/sh
echo '[libreoffice_workflow] Checking if package libreoffice exists...'
if [ $(dpkg -s libreoffice | grep -c "installed") -eq 0 ]
then
    echo '[libreoffice_workflow] Package not exists! Installing...'
    sudo apt install libreoffice -y
else
    echo '[libreoffice_workflow] Package exists! Removing...'
    sudo apt purge libreoffice -y
fi