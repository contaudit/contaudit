#! /bin/sh
echo '[gnome_tweaks_workflow] Checking if package gnome-tweaks exists...'
if [ $(dpkg -s gnome-tweaks | grep -c "installed") -eq 0 ]
then
    echo '[gnome_tweaks_workflow] Package not exists! Installing...'
    sudo apt install gnome-tweaks -y
else
    echo '[gnome_tweaks_workflow] Package exists! Removing...'
    sudo apt purge gnome-tweaks -y
fi