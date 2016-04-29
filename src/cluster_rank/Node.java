package cluster_rank;

import java.util.Comparator;
import java.util.HashMap;
import org.apache.commons.math.util.MathUtils;

/**
 * Node class for graph nodes.
 *
 * @author Gerardo Figueroa
 * Institute of Information Systems and Applications
 * National Tsing Hua University
 * Hsinchu, Taiwan
 * October 2013
 */
public class Node implements Comparable<Node> {

    public static enum Feature {
        COOCCURRENCE, SIMILARITY
    }

    /**
     * Public members.
     */
    public static Double DEFAULT_EDGE_WEIGHT = 0.0;

    /**
     * Private members.
     */
    private HashMap<Node, Double> outgoingEdges;
    private HashMap<Node, Double> incomingEdges;
    private final String key;
    private double rank;
    private final String label;
    // RankUp attributes
    private double cooccurrence;

    /**
     * Private constructor
     * @param key: the node key
     * @param data : the node label
     */
    private Node(final String key, final String data) {
        this.outgoingEdges = new HashMap<Node, Double>();
        this.incomingEdges = new HashMap<Node, Double>();
        this.key = key;
        this.rank = 1.0D;
        this.label = data;
        this.cooccurrence = -1.0D;
    }
    
    /**
     * Factory method constructor
     * @param graph: the graph to which the Node will be added
     * @param key: the node key
     * @param data: the node label
     * @return 
     */
    public static Node buildNode(final Graph graph, final String key,
            final String data) {
        Node n = graph.get(key);

        if (n == null) {
            n = new Node(key, data);
            graph.put(key, n);
        }

        return n;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Node) {
            Node node = (Node) o;
            if (node.key.equals(this.key)) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    /**
     * Compare method for sort ordering.
     * @param that
     * @return 
     */
    public int compareTo(final Node that) {
        if (this.rank > that.rank) {
            return -1;
        }
        else {
            return 1;
        }
    }

    /**
     * Ascending comparator for cooccurrence
     */
    public static Comparator<Node> CooccurrenceComparatorAscending =
            new Comparator<Node>() {

        public int compare(Node node1, Node node2) {
            Double nodeCooccurrence1 = node1.getCooccurrence();
            Double nodeCooccurrence2 = node2.getCooccurrence();

            // Ascending order
            return nodeCooccurrence1.compareTo(nodeCooccurrence2);
        }
    };

    /**
     * Descending comparator for cooccurrence
     */
    public static Comparator<Node> CooccurrenceComparatorDescending =
            new Comparator<Node>() {

        public int compare(Node node1, Node node2) {
            Double nodeCooccurrence1 = node1.getCooccurrence();
            Double nodeCooccurrence2 = node2.getCooccurrence();

            // Ascending order
            return nodeCooccurrence2.compareTo(nodeCooccurrence1);
        }
    };

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.key != null ? this.key.hashCode() : 0);
        return hash;
    }

    /**
     * Connect two nodes with a directional (outgoing) edge in the graph.
     * @param that
     * @param weight 
     */
    public void connect(final Node that, Double weight) {
        this.outgoingEdges.put(that, weight);
        that.incomingEdges.put(this, weight);
    }

    /**
     * Disconnect two nodes removing the outgoing edge in the
     * graph.
     * @param that 
     */
    public void disconnect(final Node that) {
        this.outgoingEdges.remove(that);
        that.incomingEdges.remove(this);
    }

    /**
     * Create a unique identifier for this node, returned as a hex
     * string.
     * @return 
     */
    public String getId() {
        return Integer.toString(hashCode(), 16);
    }

    /**
     * Get the Node's outgoing edges
     * @return 
     */
    public HashMap<Node, Double> getOutgoingEdges() {
        return outgoingEdges;
    }

    /**
     * Get the Node's incoming edges
     * @return 
     */
    public HashMap<Node, Double> getIncomingEdges() {
        return incomingEdges;
    }

    /**
     * Get the edge weight of a given outgoing edge
     * @param node
     * @return 
     */
    public double getOutgoingEdgeWeight(Node node) {
        Double weight = this.outgoingEdges.get(node);
        if (weight != null) {
            return weight;
        }
        else {
            return 0;
        }
    }

    /**
     * Get the edge weight of a given incoming edge
     * @param node
     * @return 
     */
    public double getIncomingEdgeWeight(Node node) {
        Double weight = this.incomingEdges.get(node);
        if (weight != null) {
            return weight;
        }
        else {
            return 0;
        }
    }

    public String getKey() {
        return key;
    }

    public double getRank() {
        return rank;
    }

    public String getLabel() {
        return label;
    }

    public double getCooccurrence() {
        return cooccurrence;
    }

    public double getFeatureValue(Feature feature) {
        switch (feature) {
            case COOCCURRENCE:
                return cooccurrence;
            default:
                return -1;
        }
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public void setCooccurrence(double cooccurrence) {
        this.cooccurrence = cooccurrence;
    }

    public String toStringSeparator(String separator) {
        String string = "";
        string += "Key: " + this.key;
        string += separator + "Label: " + this.label;
        string += separator + "Rank: " + MathUtils.round(this.rank, 2);
        string += separator + "Outgoing Edges: " + this.outgoingEdges.size();
        string += separator + "Incoming Edges: " + this.incomingEdges.size();
        string += separator + "Cooccurrence: " + MathUtils.round(this.cooccurrence, 2);
        
        return string;
    }

    @Override
    public String toString() {
                String string = "";
        string += "Key: " + this.key;
        string += " | Label: " + this.label;
        string += " | Rank: " + MathUtils.round(this.rank, 2);
        string += " | Outgoing Edges: " + this.outgoingEdges.size();
        string += " | Incoming Edges: " + this.incomingEdges.size();
        string += " | Cooccurrence: " + MathUtils.round(this.cooccurrence, 2);

        return string;
    }
}
