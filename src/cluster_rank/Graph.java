package cluster_rank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;

/**
 * Graph class for running the PageRank algorithm
 *
 * @author Gerardo Figueroa
 * Institute of Information Systems and Applications
 * National Tsing Hua University
 * Hsinchu, Taiwan
 * October 2013
 */
public class Graph extends TreeMap<String, Node> {
    
    // logging
    private static final Logger logger = Logger.getLogger(Graph.class);
    
    private final double dampingFactor;
    private final double standardErrorThreshold;
    
    /**
     * Public definitions.
     */
    public final static double INCLUSIVE_COEFF = 0.25D;

    /**
     * Public members.
     */
    public SummaryStatistics dist_stats = new SummaryStatistics();
    /**
     * Protected members.
     */
    protected Node[] node_list = null;

    /**
     * Private constructor
     * @param dampingFactor
     * @param standardErrorThreshold 
     */
    private Graph(double dampingFactor,
            double standardErrorThreshold) {
        super();
        this.dampingFactor = dampingFactor;
        this.standardErrorThreshold = standardErrorThreshold;
    }

    /**
     * Factory method for creating Graph instance from a CSV file representing
     * the graph with a matrix.
     * @param graphFileDir
     * @param separator
     * @param dampingFactor
     * @param standardErrorThreshold
     * @return
     * @throws IOException 
     */
    public static Graph loadGraph(String graphFileDir, String separator,
            double dampingFactor, double standardErrorThreshold)
            throws IOException {

        Graph graph = new Graph(dampingFactor, standardErrorThreshold);

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(graphFileDir));

            // First line contains node data (column headers)
            String line = br.readLine();
            String data[] = line.split(separator);
            for (int i = 0; i < data.length ; i++) {
                Node.buildNode(graph, String.valueOf(i), data[i]);
            }

            // After that come the edge weights
            int nodeKey = 0;
            while ((line = br.readLine()) != null) {
                Node node = graph.get(String.valueOf(nodeKey));
                String edgeWeights[] = line.split(separator);
                for (int i = 0; i < edgeWeights.length; i++) {
                    double weight = Double.parseDouble(edgeWeights[i]);
                    if (weight != 0) {
                        Node otherNode = graph.get(String.valueOf(i));
                        node.connect(otherNode, weight);
                    }
                }

                nodeKey++;
            }
        }
        finally {
            br.close();
        }

        return graph;
    }

    /**
     * Removes a node from the graph
     * @param node 
     */
    public void removeNode(Node node) {
        ArrayList<Node> outgoingNodes =
                new ArrayList<Node>(node.getOutgoingEdges().keySet());
        ArrayList<Node> incomingNodes =
                new ArrayList<Node>(node.getIncomingEdges().keySet());
        
        // First, remove all outgoing and incoming edges
        for (Node outgoingNode : outgoingNodes) {
            node.disconnect(outgoingNode);
        }
        for (Node incomingNode : incomingNodes) {
            incomingNode.disconnect(node);
        }

        // Then, remove node from graph
        this.remove(node.getKey());
    }
    
    /**
     * Run through N iterations of the TreeRank algorithm, or until
     * the standard error converges below a given threshold.
     */
    public void runPageRank() {
        final int max_iterations = this.size();
        node_list = new Node[this.size()];

        // load the node list

        int j = 0;

        for (Node n1 : this.values()) {
            node_list[j++] = n1;
        }

        // iterate, then sort and mark the top results
        iterateGraph(max_iterations);
        sortResults(this.size());
    }

    /**
     * Iterate through the graph, calculating the PageRank for each node.
     */
    private void iterateGraph(final int max_iterations) {
        final double[] rank_list = new double[node_list.length];

        // either run through N iterations, or until the standard
        // error converges below a threshold

        for (int k = 0; k < max_iterations; k++) {
            dist_stats.clear();

            // calculate the next rank for each node
            for (int i = 0; i < node_list.length; i++) {
                final Node n1 = node_list[i];
                double rank = 0.0D;

                /**
                 * New weighted formula
                 */
                for (Node n2 : n1.getIncomingEdges().keySet()) {

                    double numerator = n2.getOutgoingEdges().get(n1);
                    
                    double denominator = 0.0D;
                    for (Double d : n2.getOutgoingEdges().values()) {
                        denominator += d;
                    }

                    double normalizedWeight = numerator / denominator;

                    rank += normalizedWeight * n2.getRank();
                }

                rank *= dampingFactor;
                rank += 1.0D - dampingFactor;

                rank_list[i] = rank;
                dist_stats.addValue(Math.abs(n1.getRank() - rank));
            }

            final double standard_error =
                    dist_stats.getStandardDeviation() / Math.sqrt((double) dist_stats.getN());

            logger.trace("iteration: " + k + " error: " + standard_error);

            // swap in new rank values

            for (int i = 0; i < node_list.length; i++) {
                node_list[i].setRank(rank_list[i]);
            }

            if (standard_error < standardErrorThreshold) {
                break;
            }
        }
    }

    /**
     * Sort results to identify potential keywords.
     */
    public void sortResults(final long max_results) {
        Arrays.sort(node_list,
                new Comparator<Node>() {

                    public int compare(Node n1, Node n2) {
                        if (n1.getRank() > n2.getRank()) {
                            return -1;
                        }
                        else if (n1.getRank() < n2.getRank()) {
                            return 1;
                        }
                        else {
                            return 0;
                        }
                    }
                });
    }

    /**
     * Calculate a threshold for the ranked results.
     */
    public double getRankThreshold() {
        return dist_stats.getMean()
                + (dist_stats.getStandardDeviation() * INCLUSIVE_COEFF);
    }

    /**
     * Print the graph's values.
     */
    public void printGraphValues() {
        System.out.println("Graph size: " + this.size());
        for (Node node : this.values()) {
            System.out.println(node.toString());
        }
    }
    
    /**
     * Print the list of nodes in the graph.
     */
    public void printNodeList() {
        System.out.println("Graph size: " + this.size());
        for (Node node : this.node_list) {
            System.out.println(node.toString());
        }
    }

    public double getDampingFactor() {
        return dampingFactor;
    }
}
