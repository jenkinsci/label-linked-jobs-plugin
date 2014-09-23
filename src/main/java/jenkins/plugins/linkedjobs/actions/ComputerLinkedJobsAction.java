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

import java.util.Iterator;

import jenkins.model.Jenkins;
import jenkins.plugins.linkedjobs.model.NodeData;
import jenkins.plugins.linkedjobs.settings.GlobalSettings;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.ModelObject;
import hudson.model.Computer;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.TopLevelItem;

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
    
    // this function finds all jobs that can run only on this node because
    // of labels (mis-)configuration, thus with a potential non-redundancy
    // issue
    public NodeData getExclusiveJobs() {
        Node node = computer.getNode();
        NodeData result = new NodeData(node);

        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            if (!(job instanceof TopLevelItem)) {
                // consider only TopLevelItem - not 100% sure why, though...
                continue;
            }

            if (canRunExclusivelyOnThisNode(job.getAssignedLabel())) {
                result.addJob(job);
            }
        }

        return result;
    }
    
    private boolean canRunExclusivelyOnThisNode(Label l) {
        if (l == null) {
            // no label so no exclusivity
            return false;
        }
        
        Node exclusiveNode = null;
        
        Jenkins jenkins = Jenkins.getInstance();
        if (l.matches(jenkins)) {
            // this job could run on master, keep track of it
            exclusiveNode = jenkins;
        }
        
        
        Iterator<Node> i = jenkins.getNodes().iterator();
        while (i.hasNext()) {
            Node nodeToTest = i.next();
            if (l.matches(nodeToTest)) {
                // this label could run on this node!
                if (exclusiveNode == null) {
                    // we haven't found a node where this label can run,
                    // so let's keep track of this one. Maybe it's the only one?
                    exclusiveNode = nodeToTest;
                }
                else {
                    // wait, this label could already run on exclusiveNode, and now
                    // it can run on this other node. There's no exclusivity here
                    exclusiveNode = null;
                    break;
                }
            }
        }
        
        return (exclusiveNode != null && exclusiveNode.equals(computer.getNode()));
    }
    
    // this function finds all jobs that could run on this node,
    // based on labels configuration
    public NodeData getLinkedJobs() {
        Node node = computer.getNode();
        NodeData result = new NodeData(node);

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
                result.addJob(job);
            }
        }

        return result;
    }
}
