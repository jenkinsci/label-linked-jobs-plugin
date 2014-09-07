/*
 * The MIT License
 * 
 * Copyright (C) 2014 Dominique Brice
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.plugins.linkedjobs.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import jenkins.model.Jenkins;
import jenkins.plugins.linkedjobs.model.LabelAtomData;
import jenkins.plugins.linkedjobs.model.NodeData;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;

/**
 * Action (and ExtensionPoint!) responsible for the display of the Labels Dashboard plugin page.
 * Scans all jobs and nodes of this jenkins instance to extract all used (and unused) labels
 * @author dominiquebrice
 */
@Extension
public class LabelDashboardAction implements RootAction {

    public String getIconFileName() {
        return "search.png";
    }

    public String getDisplayName() {
        return "Labels Dashboard";
    }

    public String getUrlName() {
        return "labelsdashboard";
    }
    
    /**
     * This function scans all jobs and all nodes of this Jenkins instance
     * to extract all LabelAtom defined. Goal is to list, per LabelAtom, all jobs
     * and all nodes associated to it.
     * It ignores nodes' self labels, that are managed by getNodesData()
     */
    public Collection<LabelAtomData> getLabelsData() {
        HashMap<LabelAtom, LabelAtomData> result = new HashMap<LabelAtom, LabelAtomData>();
        
        // build a list of all the nodes self labels
        HashSet<LabelAtom> nodesSelfLabels = new HashSet<LabelAtom>();
        nodesSelfLabels.add(Jenkins.getInstance().getSelfLabel());
        for (Node node : Jenkins.getInstance().getNodes()) {
            nodesSelfLabels.add(node.getSelfLabel());
        }
        
        // This loop is directly inspired from hudson.model.Label.getTiedJobs()
        // List all LabelAtom used by all jobs, except nodes self labels that are
        // processed in getNodesData()
        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            if (job instanceof TopLevelItem) {
                for (LabelAtom label : job.getAssignedLabel().listAtoms()) {
                    if (nodesSelfLabels.contains(label)) {
                        // skip label that corresponds to a node name
                        // see getNodesData()
                        continue;
                    }
                    if (!result.containsKey(label)) {
                        result.put(label, new LabelAtomData(label));
                    }
                    result.get(label).add(job);
                }
            }
        }
        
        // list all LabelAtom defined by all nodes, including Jenkins master node
        listNodeLabels(nodesSelfLabels, result, Jenkins.getInstance());
        for (Node node : Jenkins.getInstance().getNodes()) {
            listNodeLabels(nodesSelfLabels, result, node);
        }
        
        return result.values();
    }
    
    // this function finds all jobs that can't run on any nodes
    // because of labels configuration
    public ArrayList<AbstractProject<?, ?>> getOrphanedJobs() {
        ArrayList<AbstractProject<?, ?>> orphanedJobs = new ArrayList<AbstractProject<?,?>>();

        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            if (!(job instanceof TopLevelItem)) {
                // consider only TopLevelItem - not 100% sure why, though...
                continue;
            }

            Label jobLabel = job.getAssignedLabel();
            if (jobLabel == null) {
                // if job.getAssignedLabel is null then the job can run
                // anywhere, so we're fine
                continue;
            }

            Jenkins jenkins = Jenkins.getInstance();
            if (jobLabel.matches(jenkins)) {
                // this job can run on the master, skip it
                continue;
            }
            
            boolean hasMatchingNode = false;
            Iterator<Node> i = jenkins.getNodes().iterator();
            while (i.hasNext() && !hasMatchingNode) {
                if (jobLabel.matches(i.next())) {
                    hasMatchingNode = true;
                }
            }
            if (!hasMatchingNode) {
                orphanedJobs.add(job);
            }
        }

        return orphanedJobs;
    }
    
    /**
     * This function scans all jobs to find those that are
     * using nodes' self labels
     */
    public Collection<NodeData> getNodesData() {
        HashMap<LabelAtom, NodeData> result = new HashMap<LabelAtom, NodeData>();
        
        // prefill the results with all nodes
        result.put(Jenkins.getInstance().getSelfLabel(),
                new NodeData(Jenkins.getInstance()));
        for (Node node : Jenkins.getInstance().getNodes()) {
            result.put(node.getSelfLabel(), new NodeData(node));
        }
        
        // This loop is directly inspired from hudson.model.Label.getTiedJobs()
        // Find all jobs that are using directly some nodes self labels
        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            if (job instanceof TopLevelItem) {
                for (LabelAtom label : job.getAssignedLabel().listAtoms()) {
                    if (result.containsKey(label)) {
                        // ok, this corresponds to a node self label. Let's use it
                        result.get(label).add(job);
                    }
                }
            }
        }
        
        return result.values();
    }
    
    private void listNodeLabels(HashSet<LabelAtom> nodesSelfLabels,
            HashMap<LabelAtom, LabelAtomData> result, Node node) {
        // list only static labels, not dynamic labels nor the self-label
        // so do not call Node.getAssignedLabels(), instead replicate here
        // only the interesting part of this function, which is the Label.parse(getLabelString()) call
        for (LabelAtom label : Label.parse(node.getLabelString())) {
            if (nodesSelfLabels.contains(label)) {
                // skip label that corresponds to a node name
                // see getNodesData()
                continue;
            }
            if (!result.containsKey(label)) {
                result.put(label, new LabelAtomData(label));
            }
            result.get(label).add(node);
        }
    }
}