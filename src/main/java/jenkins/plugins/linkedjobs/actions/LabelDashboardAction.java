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

import java.util.Collection;
import java.util.HashMap;

import jenkins.model.Jenkins;
import jenkins.plugins.linkedjobs.model.LabelAtomData;
import hudson.Extension;
import hudson.model.AbstractProject;
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
     * and all nodes associated to it
     */
    public Collection<LabelAtomData> getLabelsData() {
        HashMap<LabelAtom, LabelAtomData> result = new HashMap<LabelAtom, LabelAtomData>();
        
        // This loop is directly inspired from hudson.model.Label.getTiedJobs()
        // List all LabelAtom used by all jobs
        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            if (job instanceof TopLevelItem) {
                for (LabelAtom label : job.getAssignedLabel().listAtoms()) {
                    if (!result.containsKey(label)) {
                        result.put(label, new LabelAtomData(label));
                    }
                    result.get(label).add(job);
                }
            }
        }
        
        // list all LabelAtom defined by all nodes, including Jenkins master node
        String strNodeName = "master"; // hard-coded self label for Jenkins instance, see Jenkins.getSelfLabel()
        for (LabelAtom label : Jenkins.getInstance().getLabelAtoms()) {
            if (label.getName().equals(strNodeName)) {
                continue; // ignore node's self label in this function
            }
            if (!result.containsKey(label)) {
                result.put(label, new LabelAtomData(label));
            }
            result.get(label).add(Jenkins.getInstance());
        }
        for (Node node : Jenkins.getInstance().getNodes()) {
            strNodeName = node.getNodeName();
            for (LabelAtom label : node.getAssignedLabels()) {
                if (label.getName().equals(strNodeName)) {
                    continue; // ignore node's self label in this function
                }
                if (!result.containsKey(label)) {
                    result.put(label, new LabelAtomData(label));
                }
                result.get(label).add(node);
            }
        }
        
        return result.values();
    }
}