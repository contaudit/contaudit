#! /bin/sh
echo '[gimp_workflow] Checking if package gimp exists...'
if [ $(dpkg -s gimp | grep -c "installed") -eq 0 ]
then
    echo '[gimp_workflow] Package not exists! Installing...'
    sudo apt install gimp -y
else
    echo '[gimp_workflow] Package exists! Removing...'
    sudo apt purge gimp -y
fi