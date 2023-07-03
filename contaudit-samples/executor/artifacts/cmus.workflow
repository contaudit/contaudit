#! /bin/sh
echo '[cmus_workflow] Checking if package cmus exists...'
if [ $(dpkg -s cmus | grep -c "installed") -eq 0 ]
then
    echo '[cmus_workflow] Package not exists! Installing...'
    sudo apt install cmus -y
else
    echo '[cmus_workflow] Package exists! Removing...'
    sudo apt purge cmus -y
fi