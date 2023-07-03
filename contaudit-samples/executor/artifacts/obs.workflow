#! /bin/sh
echo '[obs_workflow] Checking if package obs-studio exists...'
if [ $(dpkg -s obs-studio | grep -c "installed") -eq 0 ]
then
    echo '[obs_workflow] Package not exists! Installing...'
    sudo apt install obs-studio -y
else
    echo '[obs_workflow] Package exists! Removing...'
    sudo apt purge obs-studio -y
fi