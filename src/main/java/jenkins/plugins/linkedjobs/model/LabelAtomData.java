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

import hudson.model.AbstractProject;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;

public class LabelAtomData implements Comparable<LabelAtomData> {

    private final LabelAtom label;
    // list of jobs sharing this label
    private ArrayList<AbstractProject<?, ?>> jobs;
    // list all nodes defining this label
    private ArrayList<Node> nodes;
    // list of triggered jobs sharing this label
    private ArrayList<TriggeredJob> triggeredJobs;

    public LabelAtomData(LabelAtom l) {
        label = l;
        jobs = new ArrayList<AbstractProject<?, ?>>();
        triggeredJobs = new ArrayList<TriggeredJob>();
        nodes = new ArrayList<Node>();
    }

    public void add(AbstractProject<?, ?> job) {
        jobs.add(job);
    }
    
    public void addTriggeredJobs(Collection<TriggeredJob> jobs) {
        triggeredJobs.addAll(jobs);
    }

    public void add(Node n) {
        nodes.add(n);
    }
    
    public LabelAtom getLabelAtom() {
        return label;
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

    public int getTriggeredJobsCount() {
        return triggeredJobs.size();
    }
    
    public int getNodesCount() {
        return nodes.size();
    }
    
    public boolean getPluginActiveForLabel() {
        for (hudson.model.Action a : label.getActions()) {
            if (a instanceof jenkins.plugins.linkedjobs.actions.LabelLinkedJobsAction) {
                return true;
            }
        }
        return false;
    }

    /************************************
     * Comparable interface implementation
     ************************************/
    
    public int compareTo(LabelAtomData o) {
        return this.label.compareTo(o.label);
    }
}
