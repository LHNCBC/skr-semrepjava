# This script will run word sense disambiguation server program
# 
# To run this script, you need to have wsdserver.jar file, centroids.ben.gz 
# and related .gz file for AEC method in the same directory
#
# Available user options are:
# --configfile={path to the configure file}

java -jar wsdserver.jar $@
