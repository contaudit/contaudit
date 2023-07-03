#! /bin/sh
echo '[wordpress_workflow] Checking if package wordpress-desktop exists...'
if [ $(snap list | grep -c "wordpress-desktop") -eq 0 ]
then
    echo '[wordpress_workflow] Package not exists! Installing...'
     sudo snap install wordpress-desktop
else
    echo '[wordpress_workflow] Package exists! Removing...'
    sudo snap remove wordpress-desktop
fi