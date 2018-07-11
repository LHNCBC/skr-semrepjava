# This script will run SemRep program with the providing input and output file
# 
# To run this script, you need to have semrep.jar file in the ../dist directory
#
# You need to specify these options:
# --inputformat=? (either "dir" or "singlefile")
# --inputtextformat=? (either "plaintext" or "medline")
# --inputpath=? (the input directory path or single file path)
# --outputpath=? (the output directory name or a file name)
#
# e.g. sh semrepjava.sh --inputformat=singlefile --inputtextformat=plaintext --inputpath=test.txt --outputpath=test

java -jar ../dist/semrep.jar $@
