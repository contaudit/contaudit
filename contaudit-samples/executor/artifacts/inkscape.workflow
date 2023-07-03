#! /bin/sh
echo '[inkscape_workflow] Checking if package inkscape exists...'
if [ $(dpkg -s inkscape | grep -c "installed") -eq 0 ]
then
    echo '[inkscape_workflow] Package not exists! Installing...'
    sudo apt install inkscape -y
else
    echo '[inkscape_workflow] Package exists! Removing...'
    sudo apt purge inkscape -y
fi