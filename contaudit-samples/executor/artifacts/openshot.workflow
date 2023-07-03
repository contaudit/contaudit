#! /bin/sh
echo '[openshot_workflow] Checking if package openshot-qt exists...'
if [ $(dpkg -s openshot-qt | grep -c "installed") -eq 0 ]
then
    echo '[openshot_workflow] Package not exists! Installing...'
    sudo apt install openshot-qt -y
else
    echo '[openshot_workflow] Package exists! Removing...'
    sudo apt purge openshot-qt -y
fi