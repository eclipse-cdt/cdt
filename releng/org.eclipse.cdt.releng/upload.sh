# This script must be run from the directory you want to upload

# Arguments
#  $1 - username
#  $2 - password
#  $3 - target URL

for i in *
do
	curl --ftp-create-dirs -u$1:$2 -T $i $3
done
