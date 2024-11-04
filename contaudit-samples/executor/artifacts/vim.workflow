#!/bin/sh
echo '[vim_workflow] Checking operating system...'

if [ -f /etc/debian_version ]; then
    echo '[vim_workflow] Detected Debian-based system.'
    echo '[vim_workflow] Checking if package vim exists...'
    if [ $(dpkg -s vim | grep -c "installed") -eq 0 ]; then
        echo '[vim_workflow] Package not exists! Installing...'
        sudo apt install vim -y
    else
        echo '[vim_workflow] Package exists! Removing...'
        sudo apt purge vim -y
    fi

elif [ -f /etc/os-release ] && grep -q 'Amazon Linux' /etc/os-release; then
    echo '[vim_workflow] Detected Amazon Linux.'
    echo '[vim_workflow] Checking if vim is installed...'
    if ! command -v vim >/dev/null 2>&1; then
        echo '[vim_workflow] Vim not found! Installing...'
        sudo dnf install vim -y
    else
        echo '[vim_workflow] Vim found! Removing...'
        sudo dnf remove vim -y
    fi

else
    echo '[vim_workflow] Unsupported OS.'
    exit 1
fi
