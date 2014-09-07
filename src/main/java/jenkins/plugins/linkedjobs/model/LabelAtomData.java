package jenkins.plugins.linkedjobs.model;

import java.util.ArrayList;

import hudson.model.AbstractProject;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;

public class LabelAtomData {

    private final LabelAtom label;
    // list of jobs sharing this label
    private ArrayList<AbstractProject<?, ?>> jobs;
    // list all nodes defining this label
    private ArrayList<Node> nodes;

    public LabelAtomData(LabelAtom l) {
        label = l;
        jobs = new ArrayList<AbstractProject<?, ?>>();
        nodes = new ArrayList<Node>();
    }

    public void add(AbstractProject<?, ?> job) {
        jobs.add(job);
    }

    public void add(Node n) {
        nodes.add(n);
    }
    
    
    /************************************
     * functions used to render display in index.jelly
     ************************************/
    
    public String getLabel() {
        return label.getDisplayName();
    }
    
    public String getLabelURL() {
        return label.getUrl();
    }
    
    public int getJobsCount() {
        return jobs.size();
    }
    
    public int getNodesCount() {
        return nodes.size();
    }
}
