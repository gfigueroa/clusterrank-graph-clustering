package cluster_rank;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * ClusterRank is a graph clustering algorithm based on PageRank and
 * co-occurrence sets.
 *
 * @author Gerardo Figueroa
 * Institute of Information Systems and Applications
 * National Tsing Hua University
 * Hsinchu, Taiwan
 * October 2013
 */
public class Main {

    // Logger
    private static final Logger logger = Logger.getLogger(Main.class);

    /**
     * Print the clustering results in CSV format
     * @param nodeClusters
     * @param separator 
     */
    private static void printResultsCSV(List<NodeCluster<Node>> nodeClusters,
            String separator) {
        
        for (NodeCluster<Node> nodeCluster : nodeClusters) {
            nodeCluster.printNodeCluster();
        }
    }

    /**
     * Run the main application for ClusterRank.
     * @param args 
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
     */
    public static void main(String[] args) {
        /*
         * Arg 0: Properties file
         * Arg 1: Graph matrix file
         */
        try {

            // Load ClusterRank Properties
            FileInputStream fis = new FileInputStream(args[0]);
            Properties props = new Properties();
            props.load(fis);
            ClusterRankProperties clusterRankProperties = new ClusterRankProperties(props);

            // Load graph matrix file
            String graphFileDir = args[1];

            logger.info("");
            logger.info("Starting PageRank...");
            logger.info("PageRank Properties:\n" + clusterRankProperties.toString());
            logger.info("");

            final ClusterRank clusterRank = new ClusterRank(clusterRankProperties);

            // Run ClusterRank
            List<NodeCluster<Node>> nodeClusters = clusterRank.runClusterRank(graphFileDir);

            printResultsCSV(nodeClusters, clusterRankProperties.graphFileSeparator);

            logger.info("");
            logger.info("ClusterRank completed!");
            logger.info("");

        }
        catch (FileNotFoundException fnfe) {
            logger.error("Properties file not found! " + fnfe.getMessage());
        }
        catch (IOException ioe) {
            logger.error("IO Exception! " + ioe.getMessage());
        }
        catch (Exception e) {
            logger.error("Exception in Main: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
