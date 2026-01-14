DATE=`date +'%b %d, %Y'`
echo "  - version: $1" >> conf/releases.yml
echo "    publicationDate: $DATE" >> conf/releases.yml
echo ""
