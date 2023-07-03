#! /bin/sh
echo '[timeshift_workflow] Checking if package timeshift exists...'
if [ $(dpkg -s timeshift | grep -c "installed") -eq 0 ]
then
    echo '[timeshift_workflow] Package not exists! Installing...'
    sudo apt install timeshift -y
else
    echo '[timeshift_workflow] Package exists! Removing...'
    sudo apt purge timeshift -y
fi