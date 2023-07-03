#! /bin/sh
echo '[nomacs_workflow] Checking if package nomacs exists...'
if [ $(dpkg -s nomacs | grep -c "installed") -eq 0 ]
then
    echo '[nomacs_workflow] Package not exists! Installing...'
    sudo apt install python3-launchpadlib -y
    sudo apt install aptitude -y
    sudo aptitude install nomacs -y
else
    echo '[nomacs_workflow] Package exists! Removing...'
    sudo apt purge nomacs -y
fi