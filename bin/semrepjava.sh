# This script will run SemRep program with the providing input and output file
# 
# To run this script, you need to have semrep.jar file in the dist directory
#
# You need to specify these options:
# --inputformat=? (either "dir" or "singlefile")
# --inputtextformat=? (either "plaintext" or "medline")
# --inputpath=? (the input directory path or single file path)
# --outputpath=? (the output directory name or a file name)
#
# e.g. to test with plaintext:
# 
# sh bin/semrepjava.sh --inputformat=singlefile --inputtextformat=plaintext --inputpath=TestFiles/test.plain --outputpath=TestFiles/out.plain
#
#
# e.g. to test with medline:
# sh bin/semrepjava.sh --inputformat=singlefile --inputtextformat=medline --inputpath=TestFiles/test.ml --outputpath=TestFiles/out.ml

java -jar dist/semrep.jar $@
