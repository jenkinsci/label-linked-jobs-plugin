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

package jenkins.plugins.linkedjobs.model;

import java.util.ArrayList;
import java.util.Collection;

import jenkins.model.Jenkins;
import jenkins.plugins.linkedjobs.actions.LabelLinkedJobsAction;
import hudson.model.AbstractProject;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import hudson.util.VersionNumber;

public class LabelAtomData implements Comparable<LabelAtomData> {

    private final LabelAtom labelAtom;
    // list of jobs sharing this label
    private ArrayList<AbstractProject<?, ?>> jobs;
    // list all nodes defining this label
    private ArrayList<Node> nodes;
    // list of triggered jobs sharing this label
    private ArrayList<TriggeredJob> triggeredJobs;
    // list of jobs using this label as a default value for their Label parameter
    private ArrayList<AbstractProject<?, ?>> jobsWithLabelDefaultValue;

    public LabelAtomData(LabelAtom l) {
        labelAtom = l;
        jobs = new ArrayList<AbstractProject<?, ?>>();
        triggeredJobs = new ArrayList<TriggeredJob>();
        jobsWithLabelDefaultValue = new ArrayList<AbstractProject<?,?>>();
        nodes = new ArrayList<Node>();
    }

    public void add(AbstractProject<?, ?> job) {
        jobs.add(job);
    }
    
    public void addTriggeredJobs(Collection<TriggeredJob> jobs) {
        triggeredJobs.addAll(jobs);
    }
    
    public void addJobsWithDefaultValue(Collection<AbstractProject<?, ?>> jobs) {
        jobsWithLabelDefaultValue.addAll(jobs);
    }

    public void add(Node n) {
        nodes.add(n);
    }
    
    public LabelAtom getLabelAtom() {
        return labelAtom;
    }
    
    /************************************
     * functions used to render display in index.jelly
     ************************************/
    
    public String getDescription() {
        // configurable description for LabelAtom was implemented in Jenkins core v1.580
        if (Jenkins.getVersion() != null && !Jenkins.getVersion().isOlderThan(new VersionNumber("1.580"))) {
            return labelAtom.getDescription() != null && labelAtom.getDescription().trim().length() > 0 ? labelAtom.getDescription() : null;
        }
        else {
            return null;
        }
    }
    
    public String getLabel() {
        return labelAtom.getDisplayName();
    }
    
    public String getLabelURL() {
        return labelAtom.getUrl();
    }
    
    public int getJobsCount() {
        return jobs.size();
    }

    public int getTriggeredJobsCount() {
        return triggeredJobs.size();
    }
    
    // return the number of jobs that uses this LabelAtom
    // as (part of) their default value for a Label parameter
    public int getJobsWithLabelDefaultValueCount() {
        return jobsWithLabelDefaultValue.size();
    }
    
    public int getNodesCount() {
        return nodes.size();
    }
    
    public boolean getPluginActiveForLabel() {
        for (hudson.model.Action a : labelAtom.getActions()) {
            if (a instanceof LabelLinkedJobsAction) {
                return true;
            }
        }
        return false;
    }

    /************************************
     * Comparable interface implementation
     ************************************/
    
    public int compareTo(LabelAtomData o) {
        return this.labelAtom.compareTo(o.labelAtom);
    }
}
