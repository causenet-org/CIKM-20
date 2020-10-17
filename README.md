CauseNet Source Code for Analysis & Extraction
==============================================

This source code forms the basis for our CIKM 2020 paper [CauseNet: Towards a Causality Graph Extracted from the Web](https://papers.dice-research.org/2020/CIKM-20/heindorf_2020a_public.pdf). The code is divided into two components: one component for [analyzing the graph](#analysis) and another component for [extracting the graph from the web](#graph_extraction). The final graph can be downloaded from [causenet.org](https://causenet.org/). When using the code, please make sure to refer to it as follows:

```TeX
@inproceedings{heindorf2020causenet,
  author    = {Stefan Heindorf and
               Yan Scholten and
               Henning Wachsmuth and
               Axel-Cyrille Ngonga Ngomo and
               Martin Potthast},
  title     = {CauseNet: Towards a Causality Graph Extracted from the Web},
  booktitle = {{CIKM}},
  publisher = {{ACM}},
  year      = {2020}
}
```

Overview 
=========

Project structure 
-----------------

We assume the following project structure:
```
CIKM-20/
├── java
│    ├── bootstrapping
│    └── extraction
├── notebooks
│   ├── 01-concept-spotting
│   │   ├── 01-texts-training.ipynb
│   │   ├── 02-texts-spotting-wikipedia.ipynb
│   │   ├── 03-texts-spotting-clueweb.ipynb
│   │   ├── 04-infoboxes-training.ipynb
│   │   ├── 05-infoboxes-spotting.ipynb
│   │   ├── 06-lists-training.ipynb
│   │   └── 07-lists-spotting.ipynb
│   ├── 02-graph-construction
│   │   └── 01-graph-construction.ipynb
│   ├── 03-graph-analysis
│   │   ├── 01-knowledge-bases-overview.ipynb
│   │   └── 02-graph-statistics.ipynb
│   └── 04-graph-evaluation
│       ├── 01-graph-evaluation-precision.ipynb
│       ├── 02-qa-corpus-construction.ipynb
│       └── 03-graph-evaluation-recall.ipynb
└── data/
    ├── bootstrapping
    │   ├── 0-instances
    │   ├── 0-patterns
    │   ├── 1-instances
    │   ├── 1-patterns
    │   ├── 2-instances
    │   ├── 2-patterns
    │   └── seeds.csv
    ├── question-answering/
    ├── causality-graphs/
    │   ├── extraction
    │   │   ├── clueweb
    │   │   └── wikipedia
    │   ├── spotting
    │   │   ├── clueweb
    │   │   └── wikipedia
    │   ├── integration
    │   ├── causenet-full.jsonl.bz2
    │   ├── causenet-precision.jsonl.bz2
    │   └── causenet-sample.json
    ├── categorization
    ├── random
    ├── concept-spotting
    │   ├── infoboxes
    │   ├── lists
    │   └── texts
    ├── flair-models
    │   ├── infoboxes
    │   ├── lists/
    │   └── texts/
    ├── lucene-index/
    └── external
        ├── extraction-sources
        │   ├── clueweb12
        │   └── wikipedia
        ├── knowledge-bases
        │   ├── conceptnet-assertions-5.6.0.csv
        │   ├── freebase-rdf-latest.gz
        │   └── wikidata-20181001-all.json.bz2
        ├── msmarco
        ├── nltk
        ├── stop-word-lists
        ├── spacy
        └── stanfordnlp
```


Prerequisites
------------

We recommend [Miniconda](http://conda.pydata.org/miniconda.html) for easy installation on many platforms.

1. Create new environment:  
   `conda env create -f environment.yml`
2. Activate environment:  
   `conda activate cikm20-causenet`
3. Install Kernel:  
   `python -m ipykernel install --user --name cikm20-causenet --display-name cikm20-causenet`
3. Start Jupyter:  
   `jupyter notebook`


CauseNet: Analysis<a name="analysis"></a>
==================

The code was tested with Python 3.7.3, under Linux 4.9.0-8-amd64 with 16 cores and 256 GB RAM.
 
Overview of causal relations in knowledge bases
---------------------------------------------------------

Overview of causal relations in knowledge bases as provided by Table 1.

### Required Input Data

- CauseNet-Full (output of the [extraction component](#graph_extraction))  
    [`data/causality-graphs/causenet-full.jsonl.bz2`](https://groups.uni-paderborn.de/wdqa/causenet/causality-graphs/causenet-full.jsonl.bz2)
- Freebase  
    [`data/external/knowledge-bases/freebase-rdf-latest.gz`](https://developers.google.com/freebase)
- ConceptNet (version 5.6.0)  
    [`data/external/knowledge-bases/conceptnet-assertions-5.6.0.csv`](https://s3.amazonaws.com/conceptnet/downloads/2018/edges/conceptnet-assertions-5.6.0.csv.gz)
- Wikidata  
    [`data/external/knowledge-bases/wikidata-20181001-all.json.bz2`](https://groups.uni-paderborn.de/wdqa/causenet/external/knowledge-bases/wikidata-20181001-all.json.bz2)  

### Execution

Execute the following notebook:

```
notebooks/03-graph-analysis/
└── 01-knowledge-bases-overview.ipynb
```
    
CauseNet: Graph Analysis
-----------------

### Required Input Data

- CauseNet-Full (output of the [extraction component](#graph_extraction))  
    [`data/causality-graphs/integration/causenet-full.jsonl.bz2`](https://groups.uni-paderborn.de/wdqa/causenet/causality-graphs/causenet-full.jsonl.bz2)
- Manual categorization  
    [`/data/categorization/manual_categorization.csv`](https://groups.uni-paderborn.de/wdqa/causenet/categorization/manual_categorization.csv)
- Wikipedia extraction (Output of [Wikipedia extraction](#graph_extraction_text_extraction_wikipedia))  
    [`data/causality-graphs/extraction/wikipedia/wikipedia-extraction.tsv`](https://groups.uni-paderborn.de/wdqa/causenet/causality-graphs/extraction/wikipedia/wikipedia-extraction.tsv)

### Execution

Execute the following notebook:
```
notebooks/03-graph-analysis/
└── 02-graph-statistics.ipynb
```

CauseNet: Graph Evaluation
-----------------

### Required Software

- DBpedia Spotlight
    - Installation Instructions: [https://github.com/dbpedia-spotlight/dbpedia-spotlight-model](https://github.com/dbpedia-spotlight/dbpedia-spotlight-model)
    - Required files:
      - `https://sourceforge.net/projects/dbpedia-spotlight/files/spotlight/dbpedia-spotlight-1.0.0.jar`  
      - `https://sourceforge.net/projects/dbpedia-spotlight/files/2016-10/en/model/en.tar.gz`

### Required Input Data

- CauseNet-Full (output of the [extraction component](#graph_extraction))  
    [`data/causality-graphs/integration/causenet-full.jsonl.bz2`](https://groups.uni-paderborn.de/wdqa/causenet/causality-graphs/causenet-full.jsonl.bz2)
- Random numbers for reproducibility:  
    [`data/random/generated_random_numbers.bz2`](https://groups.uni-paderborn.de/wdqa/causenet/random/generated_random_numbers.bz2)
- [MSMARCO (version: 2.1)](https://microsoft.github.io/msmarco/):  
    `data/external/msmarco/train_v2.1.json`  
    `data/external/msmarco/dev_v2.1.json`  
- ConceptNet (version 5.6.0)  
    [`data/external/knowledge-bases/conceptnet-assertions-5.6.0.csv`](https://s3.amazonaws.com/conceptnet/downloads/2018/edges/conceptnet-assertions-5.6.0.csv.gz)  
- Wikidata  
    [`data/external/knowledge-bases/wikidata-20181001-all.json.bz2`](https://groups.uni-paderborn.de/wdqa/causenet/external/knowledge-bases/wikidata-20181001-all.json.bz2)

### Execution

Execute the following notebooks:
```
notebooks/04-graph-evaluation/
├── 01-graph-evaluation-precision.ipynb
├── 02-qa-corpus-construction.ipynb
└── 03-graph-evaluation-recall.ipynb
```

### Computed Output Data

`02-qa-corpus-construction.ipynb` will extract simple causal questions from MSMARCO:

```
question-answering/
├── causality-qa-training.json
└── causality-qa-validation.json
```

CauseNet: Graph Extraction <a name="graph_extraction"></a>
=================================

The graph extraction is structured as follows:

1. [Bootstrapping Component (Java)](#graph_extraction_bootstrapping):
    - generates linguistic patterns from Wikipedia sentences using a bootstrapping approach
2. Extraction Component (Java): 
    - uses linguistic patterns to extract causal relations from the following sources:
        - [Extracting from Wikipedia](#graph_extraction_text_extraction_wikipedia)
        - [Extracting from ClueWeb12](#graph_extraction_text_extraction_clueweb12)
3. [Causal Concept Spotting (Python)](#graph_extraction_spotting):
    - training sequence taggers for sentences, infoboxes and lists
    - spotting causal concepts in extractions of previous step
3. [Graph construction (Python)](#graph_extraction_construction):
    - final construction and reconciliation steps

The code was tested with Java 8 and Python 3.7.3, under Linux 4.9.0-8-amd64 with 16 cores and 256 GB RAM.

Bootstrapping Component  <a name="graph_extraction_bootstrapping"></a>
--------------------------

### Required Input Data 

1. Bootstrapping seeds:  
   [`data/bootstrapping/seeds.csv`](https://groups.uni-paderborn.de/wdqa/causenet/bootstrapping/seeds.csv)
2. Lucene index with preprocessed Wikipedia sentences:  
   [`data/lucene-index/`](https://groups.uni-paderborn.de/wdqa/causenet/lucene-index/)

### Execution

1. Compile:  
   `mvn package -f ./java/bootstrapping/pom.xml`
2. Execute:  
   `./scripts/bootstrapping.sh`

### Computed Output Data

The bootstrapping component will compute the following files: 

```
data/bootstrapping/
├── 0-instances
├── 0-patterns
├── 1-instances
├── 1-patterns
├── 2-instances
└── 2-patterns
```
The following components will use the patterns after the second iteration: [`data/bootstrapping/2-patterns`](https://groups.uni-paderborn.de/wdqa/causenet/bootstrapping/2-patterns).

Extraction Component: Wikipedia <a name="graph_extraction_text_extraction_wikipedia"></a>
--------------------------------

### Input Data 

- Wikipedia XML dump:  
    [`data/external/extraction-sources/wikipedia/enwiki-20181001-pages-articles.xml`](https://groups.uni-paderborn.de/wdqa/causenet/external/extraction-sources/wikipedia/enwiki-20181001-pages-articles.xml)
- Patterns of the second bootstrapping iteration:  
    [`data/bootstrapping/2-patterns`](https://groups.uni-paderborn.de/wdqa/causenet/bootstrapping/2-patterns)

### Execution

1. Compile:  
   `mvn package -f ./java/extraction/pom.xml`
2. Execute:  
   `./scripts/extraction-wikipedia.sh`

### Computed Output Data

- Causal relations extracted from texts, infoboxes and lists:  
    ```
    data/causality-graphs/extraction/
    └── wikipedia
        └── wikipedia-extraction.tsv
    ```
  
Extraction Component: ClueWeb12 <a name="graph_extraction_text_extraction_clueweb12"></a>  
--------------------------------  

We provide code to parse one ClueWeb12 file. To parse the entire ClueWeb12 corpus, you can integrate this code into your cluster software.

### Input Data 

- ClueWeb12 file in WARC format:  
    [`data/external/extraction-sources/clueweb12/0013wb-88.warc.gz`](http://lemurproject.org/clueweb12/0013wb-88.warc.gz)
- Patterns of the second bootstrapping iteration:  
    [`data/bootstrapping/2-patterns`](https://groups.uni-paderborn.de/wdqa/causenet/bootstrapping/2-patterns)
- Stop word list for parsing webpages:  
    [`data/external/stop-word-lists/enStopWordList.txt`](https://raw.githubusercontent.com/chatnoir-eu/aitools3-ie-stopwords/master/src/de/aitools/ie/stopwords/stopwordlists/enStopWordList.txt)

### Execution

1. Compile:  
   `mvn package -f ./java/extraction/pom.xml`
2. Execute:  
   `./scripts/extraction-clueweb12.sh`

### Computed Output Data

- Causal relations extracted from webpage texts:  
    ```
    data/causality-graphs/extraction/
    └── clueweb12
        └── clueweb12-extraction.tsv
    ```
 
Causal Concept Spotting <a name="graph_extraction_spotting"></a>
--------------------------

Models were trained on a NVIDIA GeForce GTX 1080 Ti (11 GByte). To reproduce the results, we recommend to use a similar GPU architecture. If you do not want to retrain the models, you can use our models: [`/data/flair-models/`](https://groups.uni-paderborn.de/wdqa/causenet/flair-models/)  

### Required Software

No manual steps required. The correct versions will be automatically installed if you use the provided `environment.yml`.  
For completeness:

  - [Flair (version: 0.4.2)](https://github.com/flairNLP/flair/tree/v0.4.2) 
  - [Stanford Parser (version: 0.2.0)](https://github.com/stanfordnlp/stanza/tree/v0.2.0) (The following bug should be fixed: https://github.com/stanfordnlp/stanza/issues/135)
  - [Spacy (version: 2.1.8)](https://github.com/explosion/spaCy/tree/v2.1.8)  
    - Model version: 2.1.0 `pip install https://github.com/explosion/spacy-models/releases/download/en_core_web_sm-2.1.0/en_core_web_sm-2.1.0.tar.gz`


### Required Input Data 

- Concept Spotting datasets:  
    [`/data/concept-spotting/`](https://groups.uni-paderborn.de/wdqa/causenet/concept-spotting/): This folder contains the manually annotated training and evaluation data for the concept spotting.
- Output data of the extraction components:  
    ```
    data/causality-graphs/extraction/
    ├── clueweb12
    │   └── clueweb12-extraction.tsv
    └── wikipedia
        └── wikipedia-extraction.tsv
    ```

### Execution

Execute the following notebooks:  
```
notebooks/01-spotting/
├── 01-texts-training.ipynb
├── 02-texts-spotting-wikipedia.ipynb
├── 03-texts-spotting-clueweb.ipynb
├── 04-infoboxes-training.ipynb
├── 05-infoboxes-spotting.ipynb
├── 06-lists-training.ipynb
└── 07-lists-spotting.ipynb
```

### Computed Output Data

- Flair models for sequence labeling:  
    [`/data/flair-models/`](https://groups.uni-paderborn.de/wdqa/causenet/flair-models/)  
- Separate causality graphs:  
    ```
    data/causality-graphs/spotting/
    ├── clueweb12
    │   └── clueweb-graph.json
    └── wikipedia
        ├── infobox-graph.json
        ├── list-graph.json
        └── text-graph.json
    ```

Graph Construction <a name="graph_extraction_construction"></a>
--------------------------

### Required Input Data 

```
data/causality-graphs/spotting/
├── clueweb12
│   └── clueweb-graph.json
└── wikipedia
    ├── infobox-graph.json
    ├── list-graph.json
    └── text-graph.json
```

### Execution

Execute the following notebook:  
```
notebooks/02-graph-construction/
└── 01-graph-construction.ipynb
```

### Computed Output Data

```
data/causality-graphs/integration/
└── causenet-full.jsonl.bz2
```


Contact
-------

For questions and feedback please contact:

Stefan Heindorf, Paderborn University  
Yan Scholten, Technical University of Munich  
Henning Wachsmuth, Paderborn University  
Axel-Cyrille Ngonga Ngomo, Paderborn University  
Martin Potthast, Leipzig University  

License
-------

The code by Stefan Heindorf, Yan Scholten, Henning Wachsmuth, Axel-Cyrille Ngonga Ngomo, Martin Potthast is licensed under a MIT license.
