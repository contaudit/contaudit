#! /bin/sh
echo '[dropbox_workflow] Checking if package dropbox exists...'
if [ $(dpkg -s dropbox | grep -c "installed") -eq 0 ]
then
    echo '[dropbox_workflow] Package not exists! Installing...'
    wget -c  https://www.dropbox.com/download?dl=packages/ubuntu/dropbox_2020.03.04_amd64.deb -O dropbox.deb
    sudo apt install ./dropbox.deb -y
    rm dropbox.deb
else
    echo '[dropbox_workflow] Package exists! Removing...'
    sudo apt purge dropbox -y
fi