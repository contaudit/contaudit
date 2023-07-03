#! /bin/sh
echo '[simple_screen_recorder_workflow] Checking if package simplescreenrecorder exists...'
if [ $(dpkg -s simplescreenrecorder | grep -c "installed") -eq 0 ]
then
    echo '[simple_screen_recorder_workflow] Package not exists! Installing...'
    sudo apt install simplescreenrecorder -y
else
    echo '[simple_screen_recorder_workflow] Package exists! Removing...'
    sudo apt purge simplescreenrecorder -y
fi