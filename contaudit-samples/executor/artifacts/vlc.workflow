#! /bin/sh
echo '[vlc_workflow] Checking if package vlc exists...'
if [ $(dpkg -s vlc | grep -c "installed") -eq 0 ]
then
    echo '[vlc_workflow] Package not exists! Installing...'
    sudo apt install vlc -y
else
    echo '[vlc_workflow] Package exists! Removing...'
    sudo apt purge vlc -y
fi