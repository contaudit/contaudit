#! /bin/sh
echo '[atom_workflow] Checking if package atom exists...'
if [ $(dpkg -s atom | grep -c "installed") -eq 0 ]
then
    echo '[atom_workflow] Package not exists! Installing...'
    sudo dpkg --configure -a
    sudo apt update -y
    sudo apt install software-properties-common apt-transport-https wget -y
    wget -q https://packagecloud.io/AtomEditor/atom/gpgkey -O- | sudo apt-key add -
    sudo apt-key export DE9E3B09 | sudo gpg --dearmour -o /etc/apt/trusted.gpg.d/atom.gpg --yes
    sudo add-apt-repository "deb [arch=amd64] https://packagecloud.io/AtomEditor/atom/any/ any main" -y
    sudo apt install atom -y
else
    echo '[atom_workflow] Package exists! Removing...'
    sudo apt purge atom -y
fi