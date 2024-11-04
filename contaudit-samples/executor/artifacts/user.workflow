#!/bin/sh

# Checks if argument was provided
if [ -z "$1" ]; then
    echo "[useradd_workflow] Please provide a number for the creation of the contaudit user."
    exit 1
fi

# Sets the username
USER="contaudit$1"

# Checks if user already exists
if getent passwd "$USER" > /dev/null 2>&1; then
    echo "[useradd_workflow] User $USER already exists. Removing..."
    sudo userdel -r "$USER"
    if [ $? -eq 0 ]; then
        echo "[useradd_workflow] User $USER successfully removed."
    else
        echo "[useradd_workflow] Error removing user $USER."
        exit 1
    fi
else
    echo "[useradd_workflow] User $USER does not exist. Creating..."
fi

# Create the user again
sudo useradd "$USER"
if [ $? -eq 0 ]; then
    echo "[useradd_workflow] User $USER created successfully."
else
    echo "[useradd_workflow] Error creating user $USER."
    exit 1
fi