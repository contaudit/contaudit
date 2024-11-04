#!/bin/sh

echo '[hexedit_workflow] Checking operating system...'

if [ -f /etc/debian_version ]; then
    echo '[hexedit_workflow] Detected Debian-based system.'
    echo '[hexedit_workflow] Checking if package Hexedit exists...'
    if [ $(dpkg -s hexedit | grep -c "installed") -eq 0 ]; then
        echo '[hexedit_workflow] Package not exists! Installing...'
        sudo apt-get install hexedit -y
    else
        echo '[hexedit_workflow] Package exists! Removing...'
        sudo apt-get remove hexedit -y
    fi

elif [ -f /etc/os-release ] && grep -q 'Amazon Linux' /etc/os-release; then
    echo '[hexedit_workflow] Detected Amazon Linux.'
    echo '[hexedit_workflow] Checking if package Hexedit exists...'
    if ! command -v hexedit >/dev/null 2>&1; then
        echo '[hexedit_workflow] Package not exists! Installing from source...'

        sudo dnf groupinstall "Development Tools" -y
        sudo dnf install ncurses-devel wget -y

        wget https://github.com/pixel/hexedit/archive/refs/heads/master.zip
        unzip master.zip
        cd hexedit-master
        gcc -o hexedit hexedit.c -lncurses
        
        sudo mv hexedit /usr/local/bin/

        echo '[hexedit_workflow] Hexedit installed successfully from source.'
    else
        echo '[hexedit_workflow] Package exists! Removing...'
        sudo rm /usr/local/bin/hexedit
    fi

else
    echo '[hexedit_workflow] Unsupported OS.'
    exit 1
fi
