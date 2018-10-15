# SemRepJava

Semantic Knowledge Representation project in Lister Hill Center for National Institute of Health.

# Configuration

To run source code, you need to either add all of the jar files in lib directory to your building path or compile them through maven and add them as maven dependencies in the pom file.

Maven Compilation E.G.:

mvn install:install-file -Dfile=lib/metamaplite-3.6.jar -DgroupId=gov.nih.nlm.nls -DartifactId=metamaplite -Dversion=3.6 -Dpackaging=jar

mvn install:install-file -Dfile=lib/bioscores-2.0.0.jar -DgroupId=gov.nih.nlm -DartifactId=bioscores -Dversion=2.0.0 -Dpackaging=jar

mvn install:install-file -Dfile=lib/lvg2016dist-0.0.1.jar -DgroupId=gov.nih.nlm.nls.lvg -DartifactId=lvg2016dist -Dversion=0.0.1 -Dpackaging=jar

mvn install:install-file -Dfile=lib/gnormplus-1.0.0.jar -DgroupId=gov.nih.nlm -DartifactId=gnormplus -Dversion=1.0.0 -Dpackaging=jar




# Usage
You have to run it from top level:

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
