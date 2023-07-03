#! /bin/sh
echo '[virtualbox_workflow] Checking if package virtualbox exists...'
if [ $(dpkg -s virtualbox | grep -c "installed") -eq 0 ]
then
    echo '[virtualbox_workflow] Package not exists! Installing...'
    sudo apt install virtualbox -y
else
    echo '[virtualbox_workflow] Package exists! Removing...'
    sudo apt purge virtualbox -y
fi