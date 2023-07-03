#! /bin/sh
echo '[audacity_workflow] Checking if package audacity exists...'
if [ $(dpkg -s audacity | grep -c "installed") -eq 0 ]
then
    echo '[audacity_workflow] Package not exists! Installing...'
    sudo apt install audacity -y
else
    echo '[audacity_workflow] Package exists! Removing...'
    sudo apt purge audacity -y
fi