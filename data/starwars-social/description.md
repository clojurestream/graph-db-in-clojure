# Star Wars Social Network

## Source
[Star Wars Social Network](https://www.kaggle.com/datasets/ruchi798/star-wars) on Kaggle.

## Context
Star Wars is a science-fiction franchise comprising movies, books, comics, video games, toys, live action shows, and animated shows. It is a fictional universe created by George Lucas. The Star Wars story employs archetypal motifs common to science fiction, political climax and classical mythology, as well as musical motifs of those aspects. As one of the foremost examples of the space opera sub genre of science fiction, Star Wars has become part of mainstream popular culture, as well as being one of the highest-grossing series of all time.

This dataset contains the social network of Star Wars characters extracted from the movie scripts. If two characters speak together within the same scene, they have been connected.

## Content
* `starwars-episode-N-interactions.json` contains the social network extracted from Episode N, where the links between characters are defined by the times the characters speak within the same scene.

* `starwars-episode-N-mentions.json` contains the social network extracted from Episode N, where the links between characters are defined by the times the characters are mentioned within the same scene.

* `starwars-episode-N-interactions-allCharacters.json` is the interactions network with R2-D2 and Chewbacca added in using data from mentions network.

* `starwars-full-…` contain the corresponding social networks for the whole set of 6 episodes.

### Nodes

* `name`: Name of the character
* `value`: Number of scenes the character appeared in
* `colour`: Colour in the visualization

## Links
Represent connections between characters

* `source`: zero-based index of the character that is one end of the link, the order of nodes is the order in which they are listed in the “nodes” element
* `target`: zero-based index of the character that is the the other end of the link.
* `value`: Number of scenes where the “source character” and “target character” of the link appeared together.

The network is undirected and which character represents the source and the target is arbitrary, they correspond only to two ends of the link.

# Metadata

## Authors
Ruchi Bhatia (Owner): [Kaggle Grandmaster](https://www.kaggle.com/ruchi798)

## Provenance
[Star Wars Social Network](https://zenodo.org/records/1411479) on Zenodo.

## License
CC0: Public Domain
