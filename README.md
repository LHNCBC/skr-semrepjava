# SemRepJava

Semantic Knowledge Representation project in Lister Hill Center for National Institute of Health.

# Usage
sh bin/semrepjava.sh <options>

Please specify the following options:

--inputformat=? (either "dir" or "singlefile")
--inputtextformat=? (either "plaintext" or "medline")
--inputpath=? (the input directory path or single file path)
--outputpath=? (the output directory name or a file name)

e.g. to test with plaintext:

sh bin/semrepjava.sh --inputformat=singlefile --inputtextformat=plaintext --inputpath=TestFiles/test.plain --outputpath=TestFiles/out.plain

e.g. to test with medline:

sh bin/semrepjava.sh --inputformat=singlefile --inputtextformat=medline --inputpath=TestFiles/test.ml --outputpath=TestFiles/out.ml
