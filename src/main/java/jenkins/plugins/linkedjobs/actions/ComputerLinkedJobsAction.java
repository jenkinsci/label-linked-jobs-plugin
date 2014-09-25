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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jenkins.model.Jenkins;
import jenkins.plugins.linkedjobs.model.JobsGroup;
import jenkins.plugins.linkedjobs.model.LabelAtomData;
import jenkins.plugins.linkedjobs.model.NodeData;
import jenkins.plugins.linkedjobs.settings.GlobalSettings;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.ModelObject;
import hudson.model.Computer;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.TopLevelItem;
import hudson.model.labels.LabelAtom;

/**
 * For each Computer, Jenkins associates an object of this class to a Linked Jobs page
 * thanks to {@link jenkins.plugins.linkedjobs.extensions.ComputerExtension}.<br/><br/>
 * This class is responsible for building & collecting the data necessary to build
 * this additional page.
 * @author dominiquebrice
 *
 */
public class ComputerLinkedJobsAction implements Action {
    
    /**
     * The computer/node associated to this action
     */
    private final Computer computer;

    public ComputerLinkedJobsAction(Computer c) {
        // for now only store the computer reference
        // calculation is done when requested by display
        this.computer = c;
    }

    public Node getNode() {
        return computer.getNode();
    }
    
    public ModelObject getOwner() {
        return computer;
    }
    
    public String getIconFileName() {
        return "search.png";
    }

    // name of the additional link in the left-side menu
    public String getDisplayName() {
        return "Linked Jobs";
    }

    // relative URL used for the additional link in the left-side menu
    public String getUrlName() {
        return "linkedjobs";
    }
    
    public boolean getDetailedView() {
        return GlobalSettings.get().getDetailedView();
    }
    
    public boolean getShowSingleNodeJobs() {
        return GlobalSettings.get().getShowSingleNodeJobs();
    }
    
    public List<LabelAtom> getNodeLabels() {
        ArrayList<LabelAtom> result = new ArrayList<LabelAtom>();
        // list only static labels, not dynamic labels nor the node's self-label
        // so do not call Node.getAssignedLabels(), instead replicate here
        // only the interesting part of this function, which is the Label.parse(getLabelString()) call
        Node node = computer.getNode();
        if (node == null) {
            return Collections.emptyList();
        }

        for (LabelAtom label : Label.parse(node.getLabelString())) {
            if (node.getSelfLabel().equals(label)) {
                // skip label that corresponds to a node name
                // see getNodesData()
                continue;
            }
            result.add(label);
        }
        return result;
    }
    
    // this function finds all jobs that can run only on this node because
    // of labels (mis-)configuration, thus with a potential non-redundancy
    // issue
    public List<JobsGroup> getExclusiveJobs(List<JobsGroup> groups) {
        Iterator<JobsGroup> i = groups.iterator();
        while (i.hasNext()) {
            JobsGroup group = i.next();
            if (!group.isSingleNode()) {
                i.remove();
            }
        }

        return groups;
    }
    
    // this function finds all jobs that could run on this node,
    // based on labels configuration, and groups them by label
    public List<JobsGroup> getLinkedJobs() {
        HashMap<Label, JobsGroup> tmpResult = new HashMap<Label, JobsGroup>();
        Node node = computer.getNode();

        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            if (!(job instanceof TopLevelItem)) {
                // consider only TopLevelItem - not 100% sure why, though...
                continue;
            }

            Label jobLabel = job.getAssignedLabel();
            if (jobLabel == null) {
                // jobs with no label are not considered for this function
                continue;
            }

            if (jobLabel.matches(node)) {
                JobsGroup matchingJobGroup = tmpResult.get(jobLabel);
                if (matchingJobGroup == null) {
                    matchingJobGroup = new JobsGroup(jobLabel);
                    tmpResult.put(jobLabel, matchingJobGroup);
                }
                matchingJobGroup.addJob(job);
            }
        }

        ArrayList<JobsGroup> result = new ArrayList<JobsGroup>(tmpResult.size());
        result.addAll(tmpResult.values());
        Collections.sort(result);

        return result;
    }
}
