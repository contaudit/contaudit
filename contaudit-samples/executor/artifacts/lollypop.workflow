#! /bin/sh
echo '[lollypop_workflow] Checking if package lollypop exists...'
if [ $(dpkg -s lollypop | grep -c "installed") -eq 0 ]
then
    echo '[lollypop_workflow] Package not exists! Installing...'
    sudo apt install lollypop -y
else
    echo '[lollypop_workflow] Package exists! Removing...'
    sudo apt purge lollypop -y
fi