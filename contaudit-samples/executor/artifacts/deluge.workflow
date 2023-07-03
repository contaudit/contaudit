#! /bin/sh
echo '[deluge_workflow] Checking if package deluge exists...'
if [ $(dpkg -s deluge | grep -c "installed") -eq 0 ]
then
    echo '[deluge_workflow] Package not exists! Installing...'
    sudo apt install deluge -y
else
    echo '[deluge_workflow] Package exists! Removing...'
    sudo apt purge deluge -y
fi