/*
 * The MIT License
 * 
 * Copyright (C) 2014, 2015 Dominique Brice
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

import hudson.model.AbstractProject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility=2)
public abstract class AbstractJobsGroup {

    // list of jobs functionally tied to this label/node
    protected ArrayList<AbstractProject<?, ?>> jobs;
    // list of jobs using this label/node and triggered by another job - JENKINS-27588
    protected ArrayList<TriggeredJob> triggeredJobs;
    // list of jobs using this label/node for the default value of their Label parameter - JENKINS-27588
    protected ArrayList<AbstractProject<?, ?>> jobsWithLabelDefaultValue;
    
    protected AbstractJobsGroup() {
        jobs = new ArrayList<AbstractProject<?, ?>>();
        triggeredJobs = new ArrayList<TriggeredJob>();
        jobsWithLabelDefaultValue = new ArrayList<AbstractProject<?,?>>();
    }
    
    public void addJob(AbstractProject<?, ?> job) {
        jobs.add(job);
    }
    
    public void addTriggeredJobs(Collection<TriggeredJob> jobs) {
        triggeredJobs.addAll(jobs);
    }
    
    public void addJobsWithDefaultValue(Collection<AbstractProject<?, ?>> jobs) {
        jobsWithLabelDefaultValue.addAll(jobs);
    }

    //************************************************
    // functions used to render display in index.jelly
    //************************************************

    @Exported
    public int getJobsCount() {
        return jobs.size();
    }

    @Exported
    public int getTriggeredJobsCount() {
        return triggeredJobs.size();
    }
    
    // return the number of jobs that uses this labelAtom or node's labelAtom
    // as (part of) their default value for a Label parameter
    @Exported
    public int getJobsWithLabelDefaultValueCount() {
        return jobsWithLabelDefaultValue.size();
    }
    
    @Exported
    public List<AbstractProject<?, ?>> getJobs() {
        return jobs;
    }
    
    @Exported
    public List<TriggeredJob> getTriggeredJobs() {
        return triggeredJobs;
    }
    
    @Exported
    public List<AbstractProject<?, ?>> getJobsWithLabelDefaultValue() {
        return jobsWithLabelDefaultValue;
    }
    
    @Exported
    public boolean getHasMoreThanOneJob() {
        return (jobs.size() + triggeredJobs.size() + jobsWithLabelDefaultValue.size()) > 1;
    }
}
