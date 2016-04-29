package cluster_rank;

import cluster_rank.Node.Feature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;

/**
 * The ClusterRankGraph contains the graph extracted for ClusterRank. It contains
 * several important statistics and features of the set.
 *
 * @author Gerardo Figueroa
 * Institute of Information Systems and Applications
 * National Tsing Hua University
 * Hsinchu, Taiwan
 * January 2013
 */
public class ClusterRankGraph {

    private static final Logger logger = Logger.getLogger(Graph.class);
    
    public static enum SetDivisionApproach {
        MEAN, IQR
    }
    
    private Graph graph;

    // TFIDF Solution Expector
    private final double edgeWeightLowerBound;
    private final double edgeWeightUpperBound;

    public final static double PAGERANK_SCORE_LOWER_BOUND = 0.5;
    public final static double PAGERANK_SCORE_UPPER_BOUND = 0.5;

    // Sets
    private ArrayList<Node> lowPageRankScoreSet;
    private ArrayList<Node> midPageRankScoreSet;
    private ArrayList<Node> highPageRankScoreSet;
    private ArrayList<Node> lowEdgeWeightSet;
    private ArrayList<Node> midEdgeWeightSet;
    private ArrayList<Node> highEdgeWeightSet;

    public static enum SetLevel {
        LOW, MID, HIGH
    }

    public ClusterRankGraph(Graph graph,
            SetDivisionApproach featureSetApproach,
            double edgeWeightLowerBound, 
            double edgeWeightUpperBound) {
        this.graph = graph;
        this.edgeWeightLowerBound = edgeWeightLowerBound;
        this.edgeWeightUpperBound = edgeWeightUpperBound;

        this.lowPageRankScoreSet = new ArrayList<Node>();
        this.midPageRankScoreSet = new ArrayList<Node>();
        this.highPageRankScoreSet = new ArrayList<Node>();

        this.lowEdgeWeightSet = new ArrayList<Node>();
        this.midEdgeWeightSet = new ArrayList<Node>();
        this.highEdgeWeightSet = new ArrayList<Node>();

        assignPageRankScoreSets();

        assignFeatures();

        if (featureSetApproach == SetDivisionApproach.MEAN) {
            assignFeatureSetsMean(Feature.COOCCURRENCE);
        }
        else if (featureSetApproach == SetDivisionApproach.IQR) {
            assignFeatureSetsIQR(Feature.COOCCURRENCE);
        }
    }

    public Graph getGraph() {
        return graph;
    }

    /**
     * Assign features to the nodes in the graph
     */
    private void assignFeatures() {
        Node topNode = graph.node_list[0];

        double highestEdgeWeight = Double.NEGATIVE_INFINITY;
        for (int i = 1; i < graph.node_list.length; i++) {
            Node node = graph.node_list[i];
            double outgoingEdgeWeight = node.getOutgoingEdgeWeight(topNode);
            double incomingEdgeWeight = node.getIncomingEdgeWeight(topNode);

            double minEdgeWeight =
                    Math.min(outgoingEdgeWeight, incomingEdgeWeight);
            double maxEdgeWeight =
                    Math.max(outgoingEdgeWeight, incomingEdgeWeight);
            double averageEdgeWeight =
                    (outgoingEdgeWeight + incomingEdgeWeight) / 2.0;
            double normalizedEdgeWeight;
            
            
            node.setCooccurrence(maxEdgeWeight);

            if (averageEdgeWeight >= highestEdgeWeight) {
                highestEdgeWeight = averageEdgeWeight;
            }
        }

        // Assign highest cooccurrence to top node
        topNode.setCooccurrence(highestEdgeWeight);
    }

    /**
     * Allocate Nodes in the graph to each PageRank Score Set.
     */
    private void assignPageRankScoreSets() {

        double pageRankScoreMean = getPageRankScoreMean();
        double pageRankScoreStandardDeviation = getPageRankScoreStandardDeviation();

        for (Node Node : graph.values()) {
            double pageRankScore = Node.getRank();
            // Assign to Low TFIDF set
            if (pageRankScore
                    < pageRankScoreMean - (PAGERANK_SCORE_LOWER_BOUND *
                    pageRankScoreStandardDeviation)) {
                lowPageRankScoreSet.add(Node);
            }
            // Assign to High TFIDF set
            else if (pageRankScore
                    > pageRankScoreMean + (PAGERANK_SCORE_UPPER_BOUND *
                    pageRankScoreStandardDeviation)) {
                highPageRankScoreSet.add(Node);
            }
            // Assign to Mid TFIDF set
            else {
                midPageRankScoreSet.add(Node);
            }
        }
    }

    /**
     * Allocate Nodes in the graph to each Feature Set according to the given Feature
     * using the Mean method.
     * @param feature 
     */
    private void assignFeatureSetsMean(Feature feature) {

        double featureMean = getFeatureMean(feature);
        double featureStandardDeviation = getFeatureStandardDeviation(feature);
        ArrayList<Node> lowSet;
        ArrayList<Node> midSet;
        ArrayList<Node> highSet;
        double lower_bound;
        double upper_bound;

        switch (feature) {
            case COOCCURRENCE:
                lowSet = lowEdgeWeightSet;
                midSet = midEdgeWeightSet;
                highSet = highEdgeWeightSet;
                lower_bound = edgeWeightLowerBound;
                upper_bound = edgeWeightUpperBound;
                break;
            default:
                return;
        }

        for (Node Node : graph.values()) {

            double featureValue = Node.getFeatureValue(feature);

            // Assign to Low  set
            if (featureValue
                    < featureMean - (lower_bound * featureStandardDeviation)) {
                lowSet.add(Node);
            }
            // Assign to High set
            else if (featureValue
                    > featureMean + (upper_bound * featureStandardDeviation)) {
                highSet.add(Node);
            }
            // Assign to Mid set
            else {
                midSet.add(Node);
            }
        }
    }

    /**
     * Allocate Nodes in the graph to each Feature Set according to the given Feature
     * using the IQR method.
     * @param feature 
     */
    private void assignFeatureSetsIQR(Feature feature) {

        double featureQ3 = getFeatureQ3(feature);
        double featureQ1 = getFeatureQ1(feature);
        double featureIQR = featureQ3 - featureQ1;

        ArrayList<Node> lowSet;
        ArrayList<Node> midSet;
        ArrayList<Node> highSet;

        switch (feature) {
            case COOCCURRENCE:
                lowSet = lowEdgeWeightSet;
                midSet = midEdgeWeightSet;
                highSet = highEdgeWeightSet;
                break;
            default:
                return;
        }

        for (Node Node : graph.values()) {

            double featureValue = Node.getFeatureValue(feature);

            // Assign to Low  set
            if (featureValue < (featureQ1 - (featureIQR * 1.5))) {
                lowSet.add(Node);
            }
            // Assign to High set
            else if (featureValue > (featureQ3 + (featureIQR * 1.5)) ) {
                highSet.add(Node);
            }
            // Assign to Mid set
            else {
                midSet.add(Node);
            }
        }
    }

    /**
     * Get a PageRank Score Set given its level
     * @param setLevel
     * @return 
     */
    public ArrayList<Node> getPageRankScoreSet(SetLevel setLevel) {
        switch (setLevel) {
            case LOW:
                return lowPageRankScoreSet;
            case MID:
                return midPageRankScoreSet;
            case HIGH:
                return highPageRankScoreSet;
            default:
                return null;
        }
    }

    /**
     * Get a Feature Set given its level and the Feature
     * @param setLevel
     * @param feature
     * @return 
     */
    public ArrayList<Node> getFeatureSet(SetLevel setLevel,
            Feature feature) {

        switch (setLevel) {
            case LOW:
                switch (feature) {
                    case COOCCURRENCE:
                        return lowEdgeWeightSet;
                }
            case MID:
                switch (feature) {
                    case COOCCURRENCE:
                        return midEdgeWeightSet;
                }
            case HIGH:
                switch (feature) {
                    case COOCCURRENCE:
                        return highEdgeWeightSet;
                }
            default:
                return null;
        }
    }

    /**
     * Get the minimum Feature value of a Feature Set given its level
     * @param setLevel
     * @param feature
     * @return 
     */
    public double getFeatureSetMin(SetLevel setLevel,
            Feature feature) {

        ArrayList<Node> set = getFeatureSet(setLevel, feature);
        double min = Double.POSITIVE_INFINITY;

        for (Node Node : set) {
            double featureValue = Node.getFeatureValue(feature);
            if (featureValue <= min) {
                min = featureValue;
            }
        }

        return min;
    }

    /**
     * Get the maximum Feature value of a Feature Set given its level
     * @param setLevel
     * @param feature
     * @return 
     */
    public double getFeatureSetMax(SetLevel setLevel,
            Feature feature) {

        ArrayList<Node> set = getFeatureSet(setLevel, feature);
        double max = Double.NEGATIVE_INFINITY;

        for (Node Node : set) {
            double featureValue = Node.getFeatureValue(feature);
            if (featureValue >= max) {
                max = featureValue;
            }
        }

        return max;
    }
    
    /**
     * Get the mean Feature value of a Feature Set given its level.
     * @param setLevel
     * @param feature
     * @return 
     */
    public double getFeatureSetMean(SetLevel setLevel,
            Feature feature) {

        ArrayList<Node> set = getFeatureSet(setLevel, feature);
        double mean = 0;

        for (Node Node : set) {
            mean += Node.getFeatureValue(feature);
        }

        mean /= set.size();
        return mean;
    }

    /**
     * Get the minimum PageRank Score of a Feature Set given its level.
     * @param setLevel
     * @param feature
     * @return 
     */
    public double getFeatureSetMinPageRankScore(SetLevel setLevel,
            Feature feature) {

        ArrayList<Node> set = getFeatureSet(setLevel, feature);
        double min = Double.POSITIVE_INFINITY;

        for (Node Node : set) {
            double score = Node.getRank();
            if (score<= min) {
                min = score;
            }
        }

        return min;
    }

    /**
     * Get the maximum PageRank Score of a Feature Set given its level.
     * @param setLevel
     * @param feature
     * @return 
     */
    public double getFeatureSetMaxPageRankScore(SetLevel setLevel,
            Feature feature) {

        ArrayList<Node> set = getFeatureSet(setLevel, feature);
        double max = Double.NEGATIVE_INFINITY;

        for (Node Node : set) {
            double score = Node.getRank();
            if (score >= max) {
                max = score;
            }
        }

        return max;
    }

    /**
     * Get the mean PageRank Score of a Feature Set given its level.
     * @param setLevel
     * @param feature
     * @return 
     */
    public double getFeatureSetMeanPageRankScore(SetLevel setLevel,
            Feature feature) {

        ArrayList<Node> set = getFeatureSet(setLevel, feature);
        double mean = 0;

        for (Node Node : set) {
            mean += Node.getRank();
        }

        mean /= set.size();
        return mean;
    }

    /**
     * Get the minimum PageRank Score of a PageRank Score Set given its level.
     * @param setLevel
     * @return 
     */
    public double getPageRankScoreSetMin(SetLevel setLevel) {
        ArrayList<Node> set = getPageRankScoreSet(setLevel);
        double min = Double.POSITIVE_INFINITY;

        for (Node Node : set) {
            double score = Node.getRank();
            if (score <= min) {
                min = score;
            }
        }

        return min;
    }

    /**
     * Get the maximum PageRank Score of a PageRank Score Set given its level.
     * @param setLevel
     * @return 
     */
    public double getPageRankScoreSetMax(SetLevel setLevel) {
        ArrayList<Node> set = getPageRankScoreSet(setLevel);
        double max = Double.NEGATIVE_INFINITY;

        for (Node Node : set) {
            double score = Node.getRank();
            if (score >= max) {
                max = score;
            }
        }

        return max;
    }

    /**
     * Get the mean PageRank Score of a PageRank Score Set given its level.
     * @param setLevel
     * @return 
     */
    public double getPageRankScoreSetMean(SetLevel setLevel) {
        ArrayList<Node> set = getPageRankScoreSet(setLevel);
        double mean = 0.0;

        for (Node Node : set) {
            mean += Node.getRank();
        }

        mean /= set.size();
        return mean;
    }

    /**
     * Get the mean PageRank Score of the whole graph.
     * @return 
     */
    public double getPageRankScoreMean() {

        double mean = 0;

        for (Node Node : graph.values()) {
            mean += Node.getRank();
        }

        mean /= graph.size();
        return mean;
    }

    /**
     * Get the variance of the PageRank Score of the whole graph.
     * @return 
     */
    public double getPageRankScoreVariance() {

        double variance = 0;
        double mean = getPageRankScoreMean();

        for (Node Node : graph.values()) {
            variance += Math.pow(Node.getRank() - mean, 2);
        }

        variance /= (graph.size() - 1);
        return variance;
    }

    /**
     * Get the standard deviation of the PageRank Score of the whole graph.
     * @return 
     */
    public double getPageRankScoreStandardDeviation() {

        double variance = getPageRankScoreVariance();

        double standardDeviation = Math.sqrt(variance);
        return standardDeviation;
    }

    /**
     * Get the mean value of the given Feature of the whole graph.
     * @param feature
     * @return 
     */
    public double getFeatureMean(Feature feature) {

        double mean = 0;

        for (Node Node : graph.values()) {
            double featureValue = Node.getFeatureValue(feature);
            mean += featureValue;
        }

        mean /= graph.size();
        return mean;
    }

    /**
     * Get the variance of the given Feature of the whole graph.
     * @param feature
     * @return 
     */
    public double getFeatureVariance(Feature feature) {

        double variance = 0;
        double mean = getFeatureMean(feature);

        for (Node Node : graph.values()) {

            double featureValue = Node.getFeatureValue(feature);
            variance += Math.pow(featureValue - mean, 2);
        }

        variance /= (graph.size() - 1);
        return variance;
    }

    /**
     * Get the standard deviation of the given Feature of the whole graph.
     * @param feature
     * @return 
     */
    public double getFeatureStandardDeviation(Feature feature) {

        double variance = getFeatureVariance(feature);

        double standardDeviation = Math.sqrt(variance);
        return standardDeviation;
    }

    /**
     * Get the 1st quartile of the given Feature of the whole graph.
     * @param feature
     * @return 
     */
    public double getFeatureQ1(Feature feature) {
        List<Node> nodes = getFeatureSortedNodes(feature, true);
        int size = nodes.size();
        int q1Position = (int) MathUtils.round((size / 4.0), 0);

        return nodes.get(q1Position).getFeatureValue(feature);
    }

    /**
     * Get the 3rd quartile of the given Feature of the whole graph.
     * @param feature
     * @return 
     */
    public double getFeatureQ3(Feature feature) {
        List<Node> nodes = getFeatureSortedNodes(feature, true);
        int size = nodes.size();
        int q3Position = (int) MathUtils.round((size / 4.0), 0) * 3;

        return nodes.get(q3Position).getFeatureValue(feature);
    }

    /**
     * Get a list of Nodes sorted by the given Feature
     * @param feature
     * @param ascending
     * @return 
     */
    public List<Node> getFeatureSortedNodes(Feature feature, boolean ascending) {

        List<Node> sortedNodes = new ArrayList<Node>(graph.values());

        switch (feature) {
            case COOCCURRENCE:
                if (ascending) {
                    Collections.sort(sortedNodes, Node.CooccurrenceComparatorAscending);
                }
                else {
                    Collections.sort(sortedNodes, Node.CooccurrenceComparatorAscending);
                }
                break;
            default:
                break;
        }

        return sortedNodes;
    }

    /**
     * Get an array of Nodes sorted by their PageRank score.
     * @return 
     */
    public Node[] getSortedNodes() {

        Collection<Node> nodes = graph.values();
        Node[] sortedNodes = new Node[nodes.size()];
        int i = 0;
        for (Node node : nodes) {
            sortedNodes[i] = node;
            i++;
        }

        Arrays.sort(sortedNodes,
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

        return sortedNodes;
    }

    /**
     * Get the SetDivisionApproach of a given String representation
     * @param featureSetApproachString
     * @return 
     */
    public static SetDivisionApproach getFeatureSetApproachFromString(
            String featureSetApproachString) {

        if (featureSetApproachString == null) {
            return null;
        }
        else if (featureSetApproachString.equalsIgnoreCase("mean")) {
            return SetDivisionApproach.MEAN;
        }
        else if (featureSetApproachString.equalsIgnoreCase("iqr")) {
            return SetDivisionApproach.IQR;
        }
        else {
            return null;
        }
    }

    /**
     * Print the statistics of this graph, including PageRank Score mean, SDV,
     * and Feature means, variances and standard deviations.
     */
    public void printStatistics() {
        logger.debug("**** STATISTICS ****");
        logger.debug("Score Mean: " + MathUtils.round(getPageRankScoreMean(), 2));
        logger.debug("Score Standard Deviation: " + 
                MathUtils.round(getPageRankScoreStandardDeviation(), 2));
        logger.debug("EdgeWeight Mean: "
                + MathUtils.round(getFeatureMean(Feature.COOCCURRENCE), 2));
        logger.debug("EdgeWeight Variance: "
                + MathUtils.round(getFeatureVariance(Feature.COOCCURRENCE), 2));
        logger.debug("EdgeWeight Standard Deviation: "
                + MathUtils.round(getFeatureStandardDeviation(Feature.COOCCURRENCE), 2));
        logger.debug("");
    }

    /**
     * Print the Nodes in a Feature set given its Level.
     * @param setLevel
     * @param feature 
     */
    public void printFeatureSet(SetLevel setLevel, Feature feature) {
        System.out.println(setLevel + " " + feature +
                " (" +
                "MIN " + feature + ": " +
                MathUtils.round(getFeatureSetMin(setLevel, feature), 2) +
                ", MAX " + feature + ": " +
                MathUtils.round(getFeatureSetMax(setLevel, feature), 2) +
                ", MEAN " + feature + ": " +
                MathUtils.round(getFeatureSetMean(setLevel, feature), 2) +
                ", MIN PRS: " +
                MathUtils.round(getFeatureSetMinPageRankScore(setLevel, feature), 2) +
                ", MAX PRS: " +
                MathUtils.round(getFeatureSetMaxPageRankScore(setLevel, feature), 2) +
                ", MEAN PRS: " +
                MathUtils.round(getFeatureSetMeanPageRankScore(setLevel, feature), 2) +
                ")");
        for (Node Node : getFeatureSet(setLevel, feature)) {
            System.out.println(Node.toString());
        }
        System.out.println();
    }
}
