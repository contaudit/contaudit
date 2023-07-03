#! /bin/sh
echo '[steam_workflow] Checking if package steam exists...'
if [ $(dpkg -s steam-launcher | grep -c "installed") -eq 0 ]
then
    echo '[steam_workflow] Package not exists! Installing...'
    sudo dpkg --add-architecture i386
    sudo apt update -y
    sudo apt install wget gdebi-core libgl1-mesa-dri:i386 libgl1-mesa-glx:i386 -y
    cd /tmp && wget http://media.steampowered.com/client/installer/steam.deb
    sudo gdebi steam.deb -n
else
    echo '[steam_workflow] Package exists! Removing...'
    sudo apt purge steam-launcher -y
fi