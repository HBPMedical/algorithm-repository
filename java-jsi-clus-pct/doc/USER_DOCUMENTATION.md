[![JSI](https://img.shields.io/badge/JSI-KT-AF4C64.svg)](http://kt.ijs.si/)
[![DockerHub](https://img.shields.io/badge/docker-hbpmip%2Fjava--jsi--clus--pct-008bb8.svg)](https://hub.docker.com/r/hbpmip/java-jsi-clus-pct/)
[![ImageVersion](https://images.microbadger.com/badges/version/hbpmip/java-jsi-clus-pct.svg)](https://hub.docker.com/r/hbpmip/java-jsi-clus-pct/tags "hbpmip/java-jsi-clus-pct image tags")
[![ImageLayers](https://images.microbadger.com/badges/image/hbpmip/java-jsi-clus-pct.svg)](https://microbadger.com/#/images/hbpmip/java-jsi-clus-pct "hbpmip/java-jsi-clus-pct on microbadger")

# Predictive Clustering Trees (PCTs) for multi-target prediction user reference manual

### Description:

Predictive Clustering Trees (PCTs) are decision trees that can be used for modeling of structured target variables. Decision trees in general, are hierarchical models, where each internal node contains a test on a descriptive variable (attribute) of an example and each branch leaving this node corresponds to an outcome of this test. Terminal nodes (leaves) of a tree contain models defining the values of the target (dependent) variable for all examples falling in a given leaf. Given a new example, for which the value of the target variable should be predicted, the tree is interpreted from the root. In each inner node, the prescribed test is performed, and according to the result, the corresponding sub-tree is selected. When the selected node is a leaf, the value of the target variable for the new example is predicted according to the model in this leaf. If the target variable is numeric, the models in the leaves are typically constant values (regression tree), if the target variable is categorical, the models are categorical values (classification tree). PCTs make it possible for the target variable to be a structured object such as a vector or a time-series. In the former case, the tree (called multi-target tree) basically predicts several target variables simultaneously. The main advantage of multi-target trees (MTTs) over a set of separate trees for each dependent variable is that a single MTT is usually much smaller than the total size of the individual trees for all variables and therefore much easier to interpret. Once a decision tree is learned on the data we can use it for two purposes. First, we can use it for explaining the connections between the variables and determine the variables that are important for grouping (clustering) similar examples according to the dependent target variable. Second, we can use the tree for predicting the target variable of new examples.

### Parameters:

* Descriptive variables: A set of descriptive variables, which are used within tests inside the decision tree that describe how the similar examples are clustered together. They can be numeric or categoric or mix of both types.

* Target variables: A set of one or more (dependent) variables for which we want to learn a predictive model that will describe their values in terms of descriptive variables. They can either be all numeric or all categoric.

* Minimum number of examples in a decision tree leaf: This parameter influences the size of the learned tree (post-pruning parameter), the larger the number, the smaller the learned tree.

* Pruning (Yes/No): The parameter defines whether a post-pruning procedure is applied after the initial decision tree learning. Pruning reduces the size of the tree and such a tree tends to overfit data less.

References:

1. Levatić, J., Kocev, D., Ceci, M., & Džeroski, S. (2018). Semi-supervised trees for multi-target regression. Information Sciences. [URL](https://www.sciencedirect.com/science/article/pii/S002002551830210X)
2. Osojnik, A., Džeroski, S., & Kocev, D. (2016, October). Option predictive clustering trees for multi-target regression. In International Conference on Discovery Science (pp. 118-133). Springer, Cham. [URL](https://link.springer.com/chapter/10.1007/978-3-319-46307-0_8)
