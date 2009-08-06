#
# This Script is for building the Zehnkampf Server
#

mvn clean assembly:assembly

echo ""
echo "*****************************************************************"
echo "*****************************************************************"
while true; do
    read -p "*** Do you wish to immediately startup the Zehnkampf Server?  *** " yn
    case $yn in
        [Yy]* ) ./startup.sh; break;;
        [Nn]* ) exit;;
        * ) echo "Please answer yes or no.";;
    esac
done

