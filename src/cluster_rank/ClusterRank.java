package cluster_rank;

import cluster_rank.ClusterRankGraph.SetLevel;
import cluster_rank.Node.Feature;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * ClusterRank is a graph-clustering algorithm based on PageRank 
 * [Page, L. et al. "The PageRank citation ranking: bringing order to the web." (1999).] 
 * and co-occurrence sets. The algorithm receives a graph as input (adjacency 
 * matrix file) and outputs a list of k disjoint clusters, with each cluster 
 * containing nodes from the original graph.
 *
 * @author Gerardo Figueroa
 * Institute of Information Systems and Applications
 * National Tsing Hua University
 * Hsinchu, Taiwan
 * May 2013
 */
public class ClusterRank {
 
    // Logger
    private static final Logger logger = Logger.getLogger(ClusterRank.class);

    private Graph graph;
    private ClusterRankGraph clusterRankGraph;
    private final ClusterRankProperties clusterRankProperties;
    private final ArrayList<NodeCluster<Node>> nodeClusters;

    protected long start_time = 0L;
    protected long elapsed_time = 0L;

    public ClusterRank(ClusterRankProperties clusterRankProperties)
            throws Exception {

        this.clusterRankGraph = null;
        this.clusterRankGraph = null;
        this.clusterRankProperties = clusterRankProperties;
        this.nodeClusters = new ArrayList<NodeCluster<Node>>();
    }

    /**
     * Re-initialize the timer.
     */
    public void initTime() {
        start_time = System.currentTimeMillis();
    }
    /**
     * Report the elapsed time with a label.
     * @param label
     */
    public void markTime(final String label) {
        elapsed_time = System.currentTimeMillis() - start_time;
        logger.info("ELAPSED_TIME:\t" + elapsed_time + "\t" + label);
    }

    /**
     * Main entry point of ClusterRank.
     * The algorithm reads a graph file in CSV format.
     * The Graph Matrix File must be in adjacency matrix format
     * with the node labels only on the top row (no labels on first column).
     * The rows after the first row contain the edge weights (1 for unweighted).
     * The separator between labels and edge weights is specified in the properties
     * file.
     * Example (separator is space):
     * label1 label2 label3 ...
     * weight_n1n1 weight_n1n2 weight_n1n3 ...
     * weight_n2n1 weight_n2n2 weight n2n3 ...
     * ...
     * @param graphFileDir
     * @return a List of NodeCluster
     */
    public List<NodeCluster<Node>> runClusterRank(String graphFileDir) {

        try {
            logger.info("Loading graph...");
            graph = Graph.loadGraph(graphFileDir, clusterRankProperties.graphFileSeparator,
                    clusterRankProperties.dampingFactor,
                    clusterRankProperties.pageRankStandardErrorThreshold);
            logger.info("Graph loaded successfully!");

            initTime();

            for (int i = 0; i < clusterRankProperties.clusters; i++) {

                logger.info("*** Pass " + i + " ***");

                // 1. Run PageRank
                logger.info("1. Running PageRank...");
                graph.runPageRank();
                if (logger.isDebugEnabled()) {
                    graph.printNodeList();
                }

                // 2. Assign node sets
                logger.info("2. Assigning node sets...");
                clusterRankGraph = new ClusterRankGraph(graph,
                        clusterRankProperties.setDivisionApproach,
                        clusterRankProperties.edgeWeightLowerBound,
                        clusterRankProperties.edgeWeightUpperBound);
                
                // Print Co-Occurrence Sets
                Feature feature;
                feature = Feature.COOCCURRENCE;
                logger.info("**** Co-Occurrence Sets ****");
                if (logger.isDebugEnabled()) {
                    clusterRankGraph.printFeatureSet(SetLevel.LOW, feature);
                    clusterRankGraph.printFeatureSet(SetLevel.MID, feature);
                    clusterRankGraph.printFeatureSet(SetLevel.HIGH, feature);
                }

                // 3. Form cluster
                logger.info("3. Forming cluster " + i +  "...");
                NodeCluster<Node> nodeCluster = new NodeCluster<Node>(String.valueOf(i));
                for (Node node : clusterRankGraph.getFeatureSet(SetLevel.HIGH, feature)) {
                    nodeCluster.add(node);  // Add node to cluster
                }
                nodeClusters.add(nodeCluster);
                if (logger.isDebugEnabled()) {
                    nodeCluster.printNodeCluster(clusterRankProperties.graphFileSeparator);
                }

                // 4. Remove nodes from graph
                logger.info("4. Removing cluster nodes from graph...");
                for (Node node : nodeCluster) {
                    graph.removeNode(node);
                }
            }

            // 5. Add any remaining nodes to the last cluster
            NodeCluster<Node> lastNodeCluster =
                    new NodeCluster<Node>(String.valueOf(clusterRankProperties.clusters));
            for (Node node : graph.values()) {
                lastNodeCluster.add(node);
            }
            nodeClusters.add(lastNodeCluster);

            markTime("rankup");

            return nodeClusters;
        }
        catch (Exception e) {
            logger.error("Exception in runPageRank: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public Graph getGraph() {
        return graph;
    }

    public ClusterRankGraph getClusterRankGraph() {
        return clusterRankGraph;
    }
}
