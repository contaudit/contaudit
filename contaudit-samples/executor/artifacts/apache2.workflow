#!/bin/sh

check_apache_installed() {
    if command -v httpd > /dev/null 2>&1; then
        return 0 # Apache installed
    elif command -v apache2 > /dev/null 2>&1; then
        return 0 # Apache installed
    else
        return 1 # Apache not installed
    fi
}

if [ "$#" -ne 1 ]; then
    echo "[apache2_workflow] Usage: $0 <file_suffix>"
    exit 1
fi

if [ -f /etc/debian_version ]; then
    DISTRO="debian"
elif [ -f /etc/redhat-release ] || [ -f /etc/amazon-linux-release ]; then
    DISTRO="rpm"
else
    echo "[apache2_workflow] Linux Distro not recognized. Exiting..."
    exit 1
fi

if check_apache_installed; then
    echo "[apache2_workflow] Apache HTTP Server already installed."
else
    echo "[apache2_workflow] Apache HTTP Server not installed. Installing..."

    if [ "$DISTRO" = "debian" ]; then
        sudo apt-get update
        sudo apt-get install apache2 -V -y
    elif [ "$DISTRO" = "rpm" ]; then
        sudo yum install httpd -V -y
    fi

    echo "[apache2_workflow] Apache HTTP Server successfully installed."
fi

user_home="$HOME/public_html"
if [ "$DISTRO" = "debian" ]; then
    sudo a2enmod userdir
else
    httpd_conf="/etc/httpd/conf/httpd.conf"
    
    if ! grep -q "LoadModule userdir_module modules/mod_userdir.so" "$httpd_conf"; then
        echo "[apache2_workflow] Loading the UserDir module..."
        echo "LoadModule userdir_module modules/mod_userdir.so" | sudo tee -a "$httpd_conf" > /dev/null
    else
        echo "[apache2_workflow] UserDir module already loaded."
    fi

    if ! grep -q "<IfModule mod_userdir.c>" "$httpd_conf"; then
        echo "[apache2_workflow] Adding configs to UserDir..."
        sudo tee -a "$httpd_conf" > /dev/null <<EOL

<IfModule mod_userdir.c>
    UserDir public_html
    #AllowOverride FileInfo AuthConfig Limit Indexes
    <Directory /home/*/public_html>
        AllowOverride FileInfo AuthConfig Limit Indexes
        Options MultiViews Indexes SymLinksIfOwnerMatch IncludesNoExec
        Require all granted
    </Directory>
</IfModule>
EOL
    else
        echo "[apache2_workflow] Configs to UserDir already exist."
    fi

    if [ ! -d "$user_home" ]; then
        echo "[apache2_workflow] Creating the public_html directory in $HOME..."
        mkdir "$user_home"
    else
        echo "[apache2_workflow] public_html directory already exists in $HOME."
    fi
fi

echo '[apache2_workflow] Creating a simple webpage...'
suffix="$1"
filename="index$suffix.html"
echo '<body><h1>Hello World!</h1></body>' > "$filename"
if [ $? -ne 0 ]; then
    echo "[apache2_workflow] Failed creating file $filename"
    exit 1
fi

if [ ! -d "$user_home" ]; then
    echo "[apache2_workflow] The directory $user_home doesn't exist. Check if the user is correct."
    exit 1
fi

echo '[apache2_workflow] Moving webpage to Apache HTTP Server user website folder...'
if sudo rsync -a "$filename" "$user_home/" --remove-source-files; then
    echo '[apache2_workflow] Page moved successfully!'
else
    echo "[apache2_workflow] Failed moving file to $user_home/"
    exit 1
fi

#echo "[apache2_workflow] Setting permissions for $user_home..."
#sudo chmod 755 "$user_home"
#sudo chmod 644 "$user_home/$filename"

#echo "[apache2_workflow] Restarting Apache..."
#if [ "$DISTRO" = "debian" ]; then
#    sudo systemctl restart apache2
#elif [ "$DISTRO" = "rpm" ]; then
#    sudo systemctl restart httpd
#fi