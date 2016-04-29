# ClusterRank - Graph Clustering

ClusterRank is a graph-clustering algorithm based on PageRank [Page, L. et al. "The PageRank citation ranking: bringing order to the web." (1999).] 
and co-occurrence sets using Java.
The algorithm receives a graph as input (adjacency matrix file) and outputs a list of k disjoint clusters, with each
cluster containing nodes from the original graph.

Below is an outline of the algorithm:

	G: input graph
	k: number of clusters
	clusters: {}  // list of clusters

	for i <- 1 to k do
		ranked_nodes <- pagerank(G)
		low_set, mid_set, hi_set <- define_sets(ranked_nodes)
		cluster: {}  // empty cluster
		for each node in hi_set do
			cluster.add(node)
			G.remove(node)
		
		clusters.add(cluster)
		
	// Add any remaining nodes to the last cluster
	cluster: {} // empty cluster
	for each node in G do
		cluster.add
	clusters.add(cluster)
	
	return clusters

## Installation

Requires Java 7 and NetBeans 7.2

## Usage

The Graph Matrix File (args[1]) must be in adjacency matrix format with the node labels only on the top row (no labels on first column). 
The rows after the first row contain the edge weights (1 for unweighted). 
The separator between labels and edge weights is specified in the properties file.

Example (separator is a tab):

`label1	label2	label3 ...`

`weight_n1n1	weight_n1n2	weight_n1n3 ...`

`weight_n2n1	weight_n2n2	weight n2n3 ...`

`...`

To run jar file:

`java -jar ClusterRank.jar properties_file graph_file`

Example:

`java -jar dist\ClusterRank.jar res\default.properties tests\clusterrank_test_data.txt`

## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## History

TODO: Write history

## Credits
Written by Gerardo Figueroa.