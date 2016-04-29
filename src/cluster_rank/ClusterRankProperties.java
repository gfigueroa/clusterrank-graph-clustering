package cluster_rank;

import cluster_rank.ClusterRankGraph.SetDivisionApproach;
import java.util.Properties;

/**
 * Properties for the ClusterRank algorithm
 *
 * @author Gerardo Figueroa
 * Institute of Information Systems and Applications
 * National Tsing Hua University
 * Hsinchu, Taiwan
 * June 2013
 */
public class ClusterRankProperties {

    // PageRank Properties
    public final String graphFileSeparator;
    public final double pageRankStandardErrorThreshold;
    public final double dampingFactor;

    // ClusterRank Properties
    public final int clusters;
    public final SetDivisionApproach setDivisionApproach;
    public final double edgeWeightLowerBound;
    public final double edgeWeightUpperBound;

    public ClusterRankProperties(Properties props) throws Exception {

        try {
            this.graphFileSeparator = props.getProperty("graph_file_separator");
            this.pageRankStandardErrorThreshold =
                    Double.parseDouble(props.getProperty("pagerank_standard_error_threshold"));
            this.dampingFactor =
                    Double.parseDouble(props.getProperty("damping_factor"));

            this.clusters =
                    Integer.parseInt(props.getProperty("clusters"));
            this.setDivisionApproach =
                    ClusterRankGraph.getFeatureSetApproachFromString(
                    props.getProperty("set_division_approach"));
            this.edgeWeightLowerBound =
                    Double.parseDouble(props.getProperty("edge_weight_lower_bound"));
            this.edgeWeightUpperBound =
                    Double.parseDouble(props.getProperty("edge_weight_upper_bound"));
        }
        catch (Exception e) {
            throw new Exception("Error parsing properties file!");
        }
    }

    public ClusterRankProperties(String graphFileSeparator,
            double pageRankStandardErrorThreshold,
            double dampingFactor,
            int clusters,
            SetDivisionApproach setDivisionApproach,
            double edgeWeightLowerBound,
            double edgeWeightUpperBound,
            double learningRate,
            double clusterRankStandardErrorThreshold) {

            this.graphFileSeparator = graphFileSeparator;
            this.pageRankStandardErrorThreshold = pageRankStandardErrorThreshold;
            this.dampingFactor = dampingFactor;
            this.clusters = clusters;
            this.setDivisionApproach = setDivisionApproach;
            this.edgeWeightLowerBound = edgeWeightLowerBound;
            this.edgeWeightUpperBound = edgeWeightUpperBound;
    }

    @Override
    public String toString() {
        String string = "";

        string += "GRAPH_FILE_SEPARATOR = " + graphFileSeparator + "\n";
        string += "PAGE_RANK_STANDARD_ERROR_THRESHOLD = " + pageRankStandardErrorThreshold + "\n";
        string += "DAMPING_FACTOR = " + dampingFactor + "\n";
        string += "CLUSTERS = " + clusters + "\n";
        string += "SET_DIVISION_APPROACH = " + setDivisionApproach + "\n";
        string += "EDGE_WEIGHT_LOWER_BOUND = " + edgeWeightLowerBound + "\n";
        string += "EDGE_WEIGHT_UPPER_BOUND = " + edgeWeightUpperBound + "\n";

        return string;
    }

}