# Extraction and use of arguments in Recommender Systems
![version](https://img.shields.io/badge/version-2.6.4-blue)
![last-update](https://img.shields.io/badge/last_update-5/19/2022-orange)
![license](https://img.shields.io/badge/license-Apache_2.0-brightgreen)

This repository contains a simple but efficient implementation of an argument-based recommender system, which makes use of NLP techniques and a taxonomy and lexicon of connectors to extract argument graphs from the proposals and citizen debates available in the <a href="https://decide.madrid.es/" target="_blank">Decide Madrid</a> e-participation platform.

## Papers
This work (v1.0) was presented as a paper at <a href="http://ceur-ws.org/Vol-2960/">Joint Workshop of the 3rd Edition of Knowledge-aware and Conversational Recommender Systems (KaRS) and the 5th Edition of Recommendation in Complex Environments (ComplexRec)</a> co-located with 15th ACM Conference on Recommender Systems (RecSys 2021). Virtual Event, Amsterdam, The Netherlands, September 25, 2021. Paper and presentation slides are <a href="https://github.com/argrecsys/arg-miner/tree/main/papers/recsys21">here</a>.

This work (v2.0) will be presented as a paper at <a href="https://dgsociety.org/dgo-2022/">23st Annual International Conference on Digital Government Research</a>. Virtual Event, Seoul National University, South-Korea, June 15, 2022. A draft of the paper can be found <a href="https://github.com/argrecsys/arg-miner/tree/main/papers/dgo22">here</a>.

## Solution
This system is composed of 2 main modules, which are:
- Argument Miner: automatic argument extractor based on NLP techniques and a lexicon of connectors.
- Argument-based Recommender System: It makes recommendations of proposals and arguments based on topics and aspects of interest to the user.

## Resources
This project uses the two-level taxonomy of argument relations and the set of linguistic connectors (in English and Spanish) published in the <a href="https://github.com/argrecsys/connectors" target="_blank">argrecsys/connectors</a> repository.

## Algorithm
We propose an heuristic method aimed to automatically identify and extract arguments from textual content, which is evaluated on citizen proposals and comments from the Decide Madrid e-participatory platform. The method follows a simple but effective algorithm to address the three basic tasks of argument mining, namely argument detection, argument constituent identification, and argument relation recognition using the two-level taxonomy.

![arg-algorithm](https://raw.githubusercontent.com/argrecsys/arg-miner/main/images/algorithm.png)

## Preliminary Results
We present some statistics from a preliminary offline test on the automatic identification and extraction of arguments from the citizen proposals available in the Decide Madrid database:
- From the full list of 318 connectors in Spanish, 11,645 arguments are identified and extracted (9,676 from simple sentences and 1,969 from compound sentences).
- Arguments were automatically extracted as follows: 2025 in the proposals and 9620 in the proposal comments.
- Of the 11,645 arguments extracted (some proposals had more than one argument), 5,136 (44.1 %) were identified with connectors from the CONTRAST category, 4,669 (40.1 %) from the CONSEQUENCE category, 1,316 (11.3 %) from the CAUSE category, 505 (4.3 %) from the ELABORATION category, 
and 19 (0.2 %) from the CLARIFICATION category.

## Descriptive and Network Analysis
- The descriptive analysis of the result of the automatic annotation can be seen in the following <a href="https://argrecsys.github.io/arg-miner/analysis/DataAnalysis.html" target="_blank">report</a>.
- Network analysis to find the argumentative threads within the proposals can be seen in the following <a href="https://argrecsys.github.io/arg-miner/analysis/NetworkAnalysis.html" target="_blank">report</a>.

## Outputs
All the results generated by the Argument Miner and Recommender System can be consulted in the following <a href="https://github.com/argrecsys/arg-miner/tree/main/results">folder</a>.

Example in JSON format of an argument extracted from a citizen proposal about public transportation by the Argument Miner.

```jsonc
{
    "5717-0-1-1": {
        "proposalID": 5717,
        "majorClaim": {
            "entities": "[]",
            "text": "Allowing pets on public transport",
            "nouns": "[pets, transport]"
        },
        "sentence": "We are almost forced to use public transport in the city but pets are not allowed in EMT",
        "claim": {
            "entities": "[]",
            "text": "We are almost forced to use public transport in the city",
            "nouns": "[use, transport, city]"
        },
        "linker": {
            "value": "but", "intent": "attack",
            "category": "CONTRAST", "subCategory": "OPPOSITION"
        },
        "premise": {
            "entities": "[EMT]",
            "text": "pets are not allowed in EMT",
            "nouns": "[pets]"
        },
        "mainVerb": "forced",
        "pattern": {
            "value": "[S]-[conj_LNK]-[S]-[PUNCT]",
            "depth": 1
        },
        "syntacticTree": "(sentences 
                            (S (sn (PRP We)) (group.verb (VBP are) ... ))
                            (conj but)
                            (S (sn (NNS pets)) (group.verb (VBP are) (RB not) ... ))
                            (PUNCT .))"
   }
}
```

Example in XML format of recommendations of citizen proposals and arguments about public transportation generated by the Argument-based Recommender System

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<recommendations>
    <proposals quantity="5">
        <proposal id="20307" topics="buses" categories="mobility" date="2017-12-10" districts="Tetu??n">
            Urban buses connecting San Chinarro and Las Tablas with Cuatro Caminos</proposal>
        <proposal id="1432" topics="environment" categories="mobility" date="2015-09-18" districts="city">
            Public transportation in Madrid R??o</proposal>
        <proposal id="5717" topics="pets" categories="mobility" date="2015-11-18" districts="city">
            Allowing pets on public transport</proposal>
        <proposal id="4671" topics="public transport" categories="mobility" date="2015-11-05" districts="city">
            Public transport price</proposal>
        <proposal id="2769" topics="transport pass" categories="mobility" date="2015-10-07" districts="city">
            The Transport Pass should expire in one month</proposal>
    </proposals>
    <topics quantity="1">
        <topic value="transport" aspects="subway,use,price,transports" quantity="4">
            <aspect value="subway" quantity="2">
                <argument id="20307-1">
                    <claim>The PAU of Norte Sanchinarro Las Tablas are poorly served by public transport</claim>
                    <connector category="cause" subcategory="reason" intent="support">due to</connector>
                    <premise>the ineffectiveness of light subway</premise>
                </argument>
                <argument id="1432-1">
                    <claim>The Madrid Rio park was created promising that public transport would reach there</claim>
                    <connector category="contrast" subcategory="opposition" intent="attack">but</connector>
                    <premise>it is false, the Legazpi subway is far away and buses are non-existent</premise>
                </argument>
            </aspect>
            <aspect value="use" quantity="1">
                <argument id="5717-1">
                    <claim>The use of public transport in the city is almost forced</claim>
                    <connector category="contrast" subcategory="opposition" intent="attack">but</connector>
                    <premise>in EMT pets are not allowed</premise>
                </argument>
            </aspect>
            <aspect value="price" quantity="1">
                <argument id="4671-1">
                    <claim>Lower the price of transportation</claim>
                    <connector category="cause" subcategory="reason" intent="support">because</connector>
                    <premise>it is very expensive</premise>
                </argument>
            </aspect>
            <aspect value="transport" quantity="1">
                <argument id="2769-1">
                    <claim>The Madrid Transport Pass expires in 30 days</claim>
                    <connector category="contrast" subcategory="opposition" intent="attack">but</connector>
                    <premise>not all months have 30 days, there are several months that have 31 days</premise>
                </argument>
            </aspect>
        </topic>
    </topics>
</recommendations>
```

The other results (in file form) of the argument extractor and recommender system can be viewed <a href="https://github.com/argrecsys/arg-miner/tree/main/results">here</a>.

## Dependencies
The implemented solutions depend on or make use of the following libraries and .jar files:
- JDK 16
- <a href="https://stanfordnlp.github.io/CoreNLP/" target="_blank">Stanford CoreNLP</a> 4.3.1
- MySQL Connector 8.0.22
- <a href="https://mongodb.github.io/mongo-java-driver/" target="_blank">MongoDB Java Driver</a> 3.12.10
- Snake YAML 1.9
- JSON Java 20210307

## Authors
Created on Apr 10, 2021  
Created by:
- <a href="https://github.com/ansegura7" target="_blank">Andr??s Segura-Tinoco</a>
- <a href="http://arantxa.ii.uam.es/~cantador/" target="_blank">Iv&aacute;n Cantador</a>

## License
This project is licensed under the terms of the <a href="https://github.com/argrecsys/arg-miner/blob/main/LICENSE">Apache License 2.0</a>.

## Acknowledgements
This work was supported by the Spanish Ministry of Science and Innovation (PID2019-108965GB-I00).
