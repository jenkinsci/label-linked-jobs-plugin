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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jenkins.model.Jenkins;
import jenkins.plugins.linkedjobs.helpers.TriggeredJobsHelper;
import jenkins.plugins.linkedjobs.model.LabelAtomData;
import jenkins.plugins.linkedjobs.model.NodeData;
import jenkins.plugins.linkedjobs.model.TriggeredJob;
import jenkins.plugins.linkedjobs.settings.GlobalSettings;
import jenkins.security.stapler.StaplerDispatchable;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.model.RootAction;
import hudson.model.TopLevelItem;
import hudson.util.HttpResponses;
import hudson.slaves.Cloud;

import net.sf.json.JSONArray;
import org.kohsuke.stapler.HttpResponse;

/**
 * Action (and ExtensionPoint!) responsible for the display of the Labels Dashboard plugin page.
 * Scans all jobs and nodes of this jenkins instance to extract all used (and unused) labels
 * @author dominiquebrice
 */
@Extension
public class LabelDashboardAction implements RootAction {
    
    // see getRefresh
    private boolean m_onlyExclusiveNodes;
    private HashMap<Label, HashMap<AbstractProject<?, ?>, TriggeredJob>> m_triggeredJobsByLabel;
    private HashMap<Label, List<AbstractProject<?,?>>> m_jobsByDefaultLabel;

    public String getIconFileName() {
        return "attribute.png";
    }

    public String getDisplayName() {
        return "Labels Dashboard";
    }

    public String getUrlName() {
        return "labelsdashboard";
    }
    
    public boolean getDashboardOrphanedJobsDetailedView() {
        return GlobalSettings.get().getDashboardOrphanedJobsDetailedView();
    }
    
    public boolean getShowSingleNodeJobs() {
        return GlobalSettings.get().getShowSingleNodeJobs();
    }
    
    public boolean getShowLabellessJobs() {
        return GlobalSettings.get().getShowLabellessJobs();
    }
    
    // while the display of LabelDashboardAction.index.jelly is calculated, we will
    // need several time to have the list of triggered jobs, organized by their triggering label.
    // to avoid doing it several times, a 'fake' initialize function is called at the beginning of
    // index.jelly. Its result is not directly used, but it allows to 'refresh' the value
    // of several LabelDashboardAction instance variables, used in other get* functions below
    public boolean getRefresh() {
        
        m_onlyExclusiveNodes = getOnlyExclusiveNodes();

        m_triggeredJobsByLabel = new HashMap<Label, HashMap<AbstractProject<?,?>, TriggeredJob>>();
        TriggeredJobsHelper.populateTriggeredJobs(m_triggeredJobsByLabel);
        m_jobsByDefaultLabel = new HashMap<Label, List<AbstractProject<?,?>>>();
        TriggeredJobsHelper.populateJobsWithLabelDefaultValue(m_jobsByDefaultLabel);
        
        return true;
    }
    
    // boolean indicator used on the dashboard page to determine whether
    // clouds information should be displayed at all
    public boolean getHasAtLeastOneCloud() {
        return Jenkins.getInstance().clouds.size() > 0;
    }

    @StaplerDispatchable
    public HttpResponse doLabelsData() {
        getRefresh();
        List<LabelAtomData> labels = getLabelsData();
        if (labels.size() == 0) {
            // sometimes it's empty, in order to have the correct list, try it again
            getRefresh();
            labels = getLabelsData();
        }
        return HttpResponses.okJSON(JSONArray.fromObject(labels));
    }

    /**
     * This function scans all jobs and all nodes of this Jenkins instance
     * to extract all LabelAtom defined. Goal is to list, per LabelAtom, all jobs
     * and all nodes associated to it.
     * It ignores nodes' self labels, that are managed by getNodesData()
     * @return a list of LabelAtomData
     */
    public List<LabelAtomData> getLabelsData() {
        HashMap<LabelAtom, LabelAtomData> tmpResult = new HashMap<LabelAtom, LabelAtomData>();
        
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
            if (!(job instanceof TopLevelItem)) {
                continue;
            }
            Label assignedLabel = job.getAssignedLabel();
            if (assignedLabel == null) {
                // should we do something particular for jobs with no labels?
                continue;
            }

            for (LabelAtom labelAtom : assignedLabel.listAtoms()) {
                if (nodesSelfLabels.contains(labelAtom)) {
                    // skip label that corresponds to a node name
                    // see getNodesData()
                    continue;
                }
                if (!tmpResult.containsKey(labelAtom)) {
                    tmpResult.put(labelAtom, new LabelAtomData(labelAtom));
                }
                tmpResult.get(labelAtom).addJob(job);
            }
        }
        
        // then browse list of triggered jobs
        for (Map.Entry<Label, HashMap<AbstractProject<?, ?>, TriggeredJob>> entry: m_triggeredJobsByLabel.entrySet()) {
            for (LabelAtom labelAtom : entry.getKey().listAtoms()) {
                if (nodesSelfLabels.contains(labelAtom)) {
                    // skip label that corresponds to a node name
                    // see getNodesData()
                    continue;
                }
                if (!tmpResult.containsKey(labelAtom)) {
                    tmpResult.put(labelAtom, new LabelAtomData(labelAtom));
                }
                tmpResult.get(labelAtom).addTriggeredJobs(entry.getValue().values());
            }
        }
        
        // then browse list of jobs with default values for a Label parameter
        for (Map.Entry<Label, List<AbstractProject<?,?>>> entry : m_jobsByDefaultLabel.entrySet()) {
            for (LabelAtom labelAtom : entry.getKey().listAtoms()) {
                if (nodesSelfLabels.contains(labelAtom)) {
                    // skip label that corresponds to a node name
                    // see getNodesData()
                    continue;
                }
                if (!tmpResult.containsKey(labelAtom)) {
                    tmpResult.put(labelAtom, new LabelAtomData(labelAtom));
                }
                tmpResult.get(labelAtom).addJobsWithDefaultValue(entry.getValue());
            }
        }
        
        // list all LabelAtom defined by all nodes, including Jenkins master node,
        // but ignore nodes' self labels. See listNodeLabels()
        listNodeLabels(nodesSelfLabels, tmpResult, Jenkins.getInstance());
        for (Node node : Jenkins.getInstance().getNodes()) {
            listNodeLabels(nodesSelfLabels, tmpResult, node);
        }

        //list all available labels defined by all clouds' templates.
        listCloudTemplateLabels(tmpResult);

        ArrayList<LabelAtomData> result = new ArrayList<LabelAtomData>();
        result.addAll(tmpResult.values());
        // sort labels alphabetically by name
        Collections.sort(result);
        return result;
    }
    
    /**
     * JENKINS-25188 - Orphaned jobs do not show jobs without label when all nodes set to Label restrictions
     * This function scans all nodes to determine if at least one is in non-exclusive mode,
     * meaning it can be used to run jobs with no labels.
     * If there is no such node, jobs without labels can't be run at all
     * @return true only if all nodes are set to Mode.EXCLUSIVE
     */
    public static boolean getOnlyExclusiveNodes() {
        Jenkins jenkins = Jenkins.getInstance();
        if (!Node.Mode.EXCLUSIVE.equals(jenkins.getMode())) {
            return false;
        }
        Iterator<Node> i = jenkins.getNodes().iterator();
        while (i.hasNext()) {
            if (!Node.Mode.EXCLUSIVE.equals(i.next().getMode())) {
                return false;
            }
        }
        return true;
    }
    
    // JENKINS-25163 - Add list of jobs that do not have a label
    // this function returns all jobs that have no associated label(s)
    public List<AbstractProject<?, ?>> getJobsWithNoLabels() {
        ArrayList<AbstractProject<?, ?>> noLabelsJobs = new ArrayList<AbstractProject<?, ?>>();
        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            if (!(job instanceof TopLevelItem)) {
                // consider only TopLevelItem - not 100% sure why, though...
                continue;
            }
            Label jobLabel = job.getAssignedLabel();
            if (jobLabel == null) {
                noLabelsJobs.add(job);
            }
        }
        // sort list by jobs names
        Collections.sort(noLabelsJobs, new Comparator<AbstractProject<?, ?>>() {
            public int compare(AbstractProject<?, ?> o1, AbstractProject<?, ?> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return noLabelsJobs;
    }
    
    /**
     * @param label the label to test
     * @return true if no node can accept this label. This means that if a job has this label
     * it will remain stuck in the queue as no node can run it
     */
    private boolean isOrphanedLabel(Label label) {
        if (label == null) {
            // if job.getAssignedLabel is null then the job can run
            // anywhere, so we're fine...
            if (m_onlyExclusiveNodes) {
                // ... except if all nodes are in exclusive mode!
                // JENKINS-25188 - Orphaned jobs do not show jobs without label when all nodes set to Label restrictions
                return true;
            }
            return false;
        }
        Jenkins jenkins = Jenkins.getInstance();
        if (label.matches(jenkins)) {
            // this job can run on the master, skip it
            return false;
        }

        for (Node node : jenkins.getNodes()) {
            if (label.matches(node)) {
                // this label can run on this node... label is not orphaned!
                return false;
            }
        }
        
        // JENKINS-32445, also look for clouds that could support this label
        for (Cloud c : Jenkins.getInstance().clouds) {
            if (c.canProvision(label)) {
                return false;
            }
        }
        return true;
    }
    
    // this function finds all triggered jobs that can't run on any nodes
    // because their triggering jobs trigger them with a label that is compatible
    // with no nodes - JENKINS-27588 - nor clouds - JENKINS-32445
    public List<TriggeredJob> getOrphanedTriggeredJobs() {
        ArrayList<TriggeredJob> orphanedJobs = new ArrayList<TriggeredJob>();
        for (Map.Entry<Label, HashMap<AbstractProject<?, ?>, TriggeredJob>> entry : m_triggeredJobsByLabel.entrySet()) {
            if (isOrphanedLabel(entry.getKey())) {
                // all these triggered jobs are in trouble!
                orphanedJobs.addAll(entry.getValue().values());
            }
        }
        return orphanedJobs;
    }
    
    public Set<AbstractProject<?, ?>> getOrphanedDefaultValueJobs() {
        HashSet<AbstractProject<?, ?>> orphanedJobs = new HashSet<AbstractProject<?,?>>();
        for (Map.Entry<Label, List<AbstractProject<?,?>>> entry : m_jobsByDefaultLabel.entrySet()) {
            if (isOrphanedLabel(entry.getKey())) {
                // all these triggered jobs are in trouble!
                orphanedJobs.addAll(entry.getValue());
            }
        }
        return orphanedJobs;
    }
    
    // this function finds all jobs that can't run on any nodes
    // because of labels (mis-)configuration
    public List<AbstractProject<?, ?>> getOrphanedJobs() {
        ArrayList<AbstractProject<?, ?>> orphanedJobs = new ArrayList<AbstractProject<?, ?>>();
        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            if (!(job instanceof TopLevelItem)) {
                // consider only TopLevelItem - not 100% sure why, though...
                continue;
            }

            if (isOrphanedLabel(job.getAssignedLabel())) {
                orphanedJobs.add(job);
            }
        }
        // sort list by jobs names
        Collections.sort(orphanedJobs, new Comparator<AbstractProject<?, ?>>() {
            public int compare(AbstractProject<?, ?> o1, AbstractProject<?, ?> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return orphanedJobs;
    }
    
    /**
     * @param label
     * @return a non-null Node if this and only this node can run jobs configured with label
     */
    private Node isSingleNode(Label label) {
        Node node = null;
        Jenkins jenkins = Jenkins.getInstance();
        if (label.matches(jenkins)) {
            node  = jenkins;
        }

        Iterator<Node> i = jenkins.getNodes().iterator();
        while (i.hasNext()) {
            Node iNode = i.next();
            if (label.matches(iNode)) {
                if (node == null) {
                    // that's the first node this job can run on
                    // let's continue the loop
                    node = iNode;
                }
                else {
                    // this job can already run on one node
                    // that's the second node, so we won't keep
                    // track of it. Indicate it by setting node to null
                    node = null;
                    // and get out of the loop
                    break;
                }
            }
        }
        return node;
    }
    
    // this function finds all jobs that can run on only one node
    // because of labels (mis-)configuration, thus with a non-redundancy
    // issue
    public List<NodeData> getSingleNodeJobs() {
        HashMap<Node, NodeData> tmpResult = new HashMap<Node, NodeData>();

        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            if (!(job instanceof TopLevelItem)) {
                // consider only TopLevelItem - not 100% sure why, though...
                continue;
            }

            // switch to a loop on job.getRelevantLabels() ?
            // that would make it applicable for hudson.matrix.MatrixProject.
            // On the other hand, maybe we could ignore MatrixProject and
            // try to process hudson.matrix.MatrixConfiguration, which are NOT TopLevelItem
            // but are apparently part of the getAllItems loop
            Label jobLabel = job.getAssignedLabel();
            if (jobLabel == null) {
                // if job.getAssignedLabel is null then the job can run
                // anywhere, so we're fine - that is, assuming there's more
                // than one node/slave in the infra
                continue;
            }

            Node node = isSingleNode(jobLabel);
            // that fact that the job can run on a single-node
            // is indicated by node being not null
            if (node != null) {
                if (!tmpResult.containsKey(node)) {
                    tmpResult.put(node, new NodeData(node));
                }
                tmpResult.get(node).addJob(job);
            }
        }
        
        // then scan the list of triggered jobs
        for (Map.Entry<Label, HashMap<AbstractProject<?, ?>, TriggeredJob>> entry : m_triggeredJobsByLabel.entrySet()) {
            Node node = isSingleNode(entry.getKey());
            if (node != null) {
                if (!tmpResult.containsKey(node)) {
                    tmpResult.put(node, new NodeData(node));
                }
                tmpResult.get(node).addTriggeredJobs(entry.getValue().values());
            }
        }

        // then scan the list of jobs using default value for their Label parameter
        for (Map.Entry<Label, List<AbstractProject<?,?>>> entry : m_jobsByDefaultLabel.entrySet()) {
            Node node = isSingleNode(entry.getKey());
            if (node != null) {
                if (!tmpResult.containsKey(node)) {
                    tmpResult.put(node, new NodeData(node));
                }
                tmpResult.get(node).addJobsWithDefaultValue(entry.getValue());
            }
        }

        ArrayList<NodeData> result = new ArrayList<NodeData>(tmpResult.size());
        result.addAll(tmpResult.values());
        // sort list by node names
        Collections.sort(result);
        return result;
    }

    @StaplerDispatchable
    public HttpResponse doNodesData() {
        getRefresh();
        Collection<NodeData> nodes = getNodesData();
        if (nodes.size() == 0) {
            // sometimes it's empty, in order to have the correct list, try it again
            getRefresh();
            nodes = getNodesData();
        }
        return HttpResponses.okJSON(JSONArray.fromObject(nodes));
    }
    
    /**
     * This function scans all jobs to find those that are
     * using nodes' self labels
     * @return a collection of NodeData
     */
    public Collection<NodeData> getNodesData() {
        HashMap<LabelAtom, NodeData> tmpResult = new HashMap<LabelAtom, NodeData>();
        
        // prefill the results with all nodes
        tmpResult.put(Jenkins.getInstance().getSelfLabel(),
                new NodeData(Jenkins.getInstance()));
        for (Node node : Jenkins.getInstance().getNodes()) {
            tmpResult.put(node.getSelfLabel(), new NodeData(node));
        }
        
        // This loop is directly inspired from hudson.model.Label.getTiedJobs()
        // Find all jobs that are using directly some nodes' self labels
        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            if (!(job instanceof TopLevelItem)) {
                continue;
            }
            
            Label assignedLabel = job.getAssignedLabel();
            if (assignedLabel == null) {
                continue;
            }
            
            for (LabelAtom labelAtom : assignedLabel.listAtoms()) {
                if (tmpResult.containsKey(labelAtom)) {
                    // ok, this corresponds to a node's self label. Let's use it
                    tmpResult.get(labelAtom).addJob(job);
                }
            }
        }
        
        // then scan the list of triggered jobs
        for (Map.Entry<Label, HashMap<AbstractProject<?, ?>, TriggeredJob>> entry : m_triggeredJobsByLabel.entrySet()) {
            for (LabelAtom labelAtom : entry.getKey().listAtoms()) {
                if (tmpResult.containsKey(labelAtom)) {
                    tmpResult.get(labelAtom).addTriggeredJobs(entry.getValue().values());
                }
            }
        }
        
        // then browse list of jobs with default values for a Label parameter
        for (Map.Entry<Label, List<AbstractProject<?,?>>> entry : m_jobsByDefaultLabel.entrySet()) {
            for (LabelAtom labelAtom : entry.getKey().listAtoms()) {
                if (tmpResult.containsKey(labelAtom)) {
                    tmpResult.get(labelAtom).addJobsWithDefaultValue(entry.getValue());
                }
            }
        }
        
        ArrayList<NodeData> result = new ArrayList<NodeData>();
        result.addAll(tmpResult.values());
        // sort nodes alphabetically by display name
        Collections.sort(result);
        return result;
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

    private void listCloudTemplateLabels(HashMap<LabelAtom, LabelAtomData> result) {
        //Listing all available labels so that later the cloud template related lables can be picked from them
        for (Label label : Jenkins.getInstance().getLabels()) {
            if (label.getClouds().size() > 0) {
                for (LabelAtom labelAtom : label.listAtoms()) {
                    if (!result.containsKey(labelAtom)) {
                        result.put(labelAtom, new LabelAtomData(labelAtom));
                    }
                }
            }
        }
    }
}