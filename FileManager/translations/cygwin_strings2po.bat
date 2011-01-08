set PATH=.;c:\cygwin\bin;%PATH%
bash androidxml2po.bash -e
mkdir translations_filemanager
copy filemanager* translations_filemanager
tar -cvvzf translations_filemanager.tgz translations_filemanager