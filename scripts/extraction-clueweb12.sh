#!/bin/bash
## variables
jar="./java/extraction/target/extraction-1.0-SNAPSHOT-jar-with-dependencies.jar"
data0="data/external/extraction-sources/clueweb12/0013wb-88.warc.gz"
data1="data/bootstrapping/2-patterns" 
data2="data/external/stop-word-lists/enStopWordList.txt"
data3="data/causality-graphs/extraction/clueweb12/clueweb12-extraction.tsv"

## processing
java -jar -Xmx150G -Xms150G $jar $data0 $data1 $data2 $data3 
