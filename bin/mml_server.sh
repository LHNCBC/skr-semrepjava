# This script will run metamaplite server program
#
# To run this script, you have to have metamapliteserver.jar file 
# and data folder(which contains opennlp models and metamap files) in the same directory
#
# Available user options are:
# --configfile={path to the configure file}
# --indexdir={path to the metamap index directory}
# --modelsdir={path to the opennlp model directory}

java -jar metamapliteserver.jar $@
