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
import java.util.List;

import jenkins.model.Jenkins;
import hudson.model.AbstractProject;
import hudson.model.Node;

public class NodeData implements Comparable<NodeData> {

    private Node node;
    // list of jobs functionally tied to this node
    private ArrayList<AbstractProject<?, ?>> jobs;
    // list of triggered jobs functionally tied to this node
    private ArrayList<TriggeredJob> triggeredJobs;

    public NodeData(Node n) {
        jobs = new ArrayList<AbstractProject<?, ?>>();
        triggeredJobs = new ArrayList<TriggeredJob>();
        node = n;
    }

    public void addJob(AbstractProject<?, ?> job) {
        jobs.add(job);
    }
    
    public void addTriggeredJobs(Collection<TriggeredJob> jobs) {
        triggeredJobs.addAll(jobs);
    }
    
    /************************************
     * functions used to render display in index.jelly
     ************************************/
    
    public String getName() {
        return node.getDisplayName();
    }
    
    public String getLabelURL() {
        return node.getSelfLabel().getUrl();
    }
    
    public String getNodeURL() {
        return Jenkins.getInstance().getComputer(node.getNodeName()).getUrl();
    }
    
    public int getJobsCount() {
        return jobs.size();
    }
    
    public int getTriggeredJobsCount() {
        return triggeredJobs.size();
    }
    
    public List<AbstractProject<?, ?>> getJobs() {
        return jobs;
    }
    
    public List<TriggeredJob> getTriggeredJobs() {
        return triggeredJobs;
    }

    /************************************
     * Comparable interface implementation
     ************************************/
    
    public int compareTo(NodeData o) {
        return this.getName().compareTo(o.getName());
    }
}
