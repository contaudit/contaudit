#! /bin/sh
echo '[apache_workflow] Installing Apache HTTP Server...'
sudo apt-get install apache2 -V -y
echo '[apache_workflow] Enabling Apache per-user web directories...'
sudo a2enmod userdir
echo '[apache_workflow] Restarting Apache HTTP Server...'
sudo systemctl restart apache2 -l

echo '[apache_workflow] Creating an empty webpage...'
touch index.html
echo '[apache_workflow] Moving webpage to Apache HTTP Server website folder...'
sudo rsync -a index.html /var/www/html/ --remove-source-files -v

echo '[apache_workflow] Create a simple webpage...'
echo '<body><h1>Hello World!</h1></body>' > index.html
echo '[apache_workflow] Move webpage to Apache HTTP Server user website folder...'
sudo rsync -a index.html /home/kali/public_html/ --remove-source-files -v