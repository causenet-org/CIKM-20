#!/bin/bash
## variables
jar="./java/extraction/target/extraction-1.0-SNAPSHOT-jar-with-dependencies.jar"
data0="data/external/extraction-sources/wikipedia/enwiki-20181001-pages-articles.xml" 
data1="data/bootstrapping/2-patterns" 
data2="data/causality-graphs/extraction/wikipedia/wikipedia-extraction.tsv"

## processing
java -jar -Xmx150G -Xms150G $jar $data0 $data1 $data2
