package cluster_rank;

import java.util.TreeSet;

/**
 * The NodeCluster contains nodes that have been assigned to this cluster.
 *
 * @author Gerardo Figueroa
 * Institute of Information Systems and Applications
 * National Tsing Hua University
 * Hsinchu, Taiwan
 * November 2013
 */
public class NodeCluster<Node> extends TreeSet<Node> {

    private final String id;

    public NodeCluster(String id) {
        super();
        this.id = id;
    }

    /**
     * Print all the nodes in this cluster using a custom format.
     * @param separator: the separator to use between each output field
     */
    public void printNodeCluster(String separator) {
        System.out.println("Cluster " + id);
        cluster_rank.Node[] nodes = this.toArray(new cluster_rank.Node[0]);
        for (cluster_rank.Node node : nodes) {
            System.out.println(node.toStringSeparator(separator));
        }
    }
    
    /**
     * Print all the nodes in this cluster using the default format.
     */
    public void printNodeCluster() {
        System.out.println("Cluster " + id);
        cluster_rank.Node[] nodes = this.toArray(new cluster_rank.Node[0]);
        for (cluster_rank.Node node : nodes) {
            System.out.println(node.toString());
        }
    }

    public String getId() {
        return id;
    }

}
