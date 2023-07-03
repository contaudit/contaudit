#! /bin/sh
echo '[telegram_workflow] Checking if package telegram-desktop exists...'
if [ $(dpkg -s telegram-desktop | grep -c "installed") -eq 0 ]
then
    echo '[telegram_workflow] Package not exists! Installing...'
    sudo apt install telegram-desktop -y
else
    echo '[telegram_workflow] Package exists! Removing...'
    sudo apt purge telegram-desktop -y
fi