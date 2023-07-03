#! /bin/sh
echo '[stacer_workflow] Checking if package clamav stacer exists...'
if [ $(dpkg -s clamav stacer | grep -c "installed") -eq 0 ]
then
    echo '[stacer_workflow] Package not exists! Installing...'
    sudo apt install clamav stacer -y
else
    echo '[stacer_workflow] Package exists! Removing...'
    sudo apt purge clamav stacer -y
fi