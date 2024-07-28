#! /bin/sh
echo '[apache2_workflow] Create a simple webpage...'
filename="index$1.html"
echo '<body><h1>Hello World!</h1></body>' > $filename
echo '[apache2_workflow] Move webpage to Apache HTTP Server user website folder...'
sudo rsync -a $filename /home/kali/public_html/ --remove-source-files