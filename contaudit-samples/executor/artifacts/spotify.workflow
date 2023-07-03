#! /bin/sh
echo '[spotify_workflow] Checking if package spotify-client exists...'
if [ $(dpkg -s spotify-client | grep -c "installed") -eq 0 ]
then
    echo '[spotify_workflow] Package not exists! Installing...'
    curl -sS https://download.spotify.com/debian/pubkey_7A3A762FAFD4A51F.gpg | sudo gpg --dearmor --yes -o /etc/apt/trusted.gpg.d/spotify.gpg
    echo "deb http://repository.spotify.com stable non-free" | sudo tee /etc/apt/sources.list.d/spotify.list
    sudo apt-get update -y
    sudo apt-get install spotify-client -y
else
    echo '[spotify_workflow] Package exists! Removing...'
    sudo apt purge spotify-client -y
fi