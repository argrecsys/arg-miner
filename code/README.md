# Solution
The two main modules of the solution were implemented in Java within the ArgumentMiner project.

### Argument Miner
Java solution that using NLP techniques and a lexicon of connectors, automatically identifies and extracts arguments from textual content.

### Argument-based Recommender Systems
Java solution that from previously extracted arguments on a topic (for and against), makes simple recommendations on proposals of interest leveraged by arguments.

### Solution parameters
The 2 modules of the solution (the argument extractor and the recommender system) receive the input parameters directly from the <a href="https://github.com/argrecsys/arg-miner/blob/main/code/ArgumentMiner/Resources/config/params.json">params.json</a> file.

```json
{
    "customProposalID": [7, 19, 50, 61, 72, 86, 89, 109, 152, 417],
    "extraction": {
        "annotateComments": true
    },
    "language": "es",
    "linkers": {
        "en": {
            "invalidAspects": ["also", "thing", "mine", "sometimes", "too", "other"],
            "invalidLinkers": ["and", "or"],
            "validLinkers": []
        },
        "es": {
            "invalidAspects": ["tambien", "cosa", "mia", "veces", "ademas", "demas"],
            "invalidLinkers": ["o", "y"],
            "validLinkers": []
        }
    },
    "recommendation": {
        "maxTreeLevel": 2,
        "minAspectOccur": 1,
        "topic": "-"
    }
}
```

## License
This project is licensed under the terms of the <a href="https://github.com/argrecsys/arg-miner/blob/main/LICENSE">Apache License 2.0</a>.

## Acknowledgements
This work was supported by the Spanish Ministry of Science and Innovation (PID2019-108965GB-I00).
