#! /bin/sh
echo '[vim_workflow] Checking if package vim exists...'
if [ $(dpkg -s vim | grep -c "installed") -eq 0 ]
then
    echo '[vim_workflow] Package not exists! Installing...'
    sudo apt install vim -y
else
    echo '[vim_workflow] Package exists! Removing...'
    sudo apt purge vim -y
fi