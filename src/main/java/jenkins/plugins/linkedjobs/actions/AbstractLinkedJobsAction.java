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

package jenkins.plugins.linkedjobs.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jenkins.model.Jenkins;
import jenkins.plugins.linkedjobs.helpers.TriggeredJobsHelper;
import jenkins.plugins.linkedjobs.model.JobsGroup;
import jenkins.plugins.linkedjobs.model.TriggeredJob;
import jenkins.plugins.linkedjobs.settings.GlobalSettings;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Label;
import hudson.model.TopLevelItem;

public abstract class AbstractLinkedJobsAction implements Action {
    
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
    
    protected List<JobsGroup> buildJobsGroups() {
        HashMap<Label, JobsGroup> tmpResult = new HashMap<Label, JobsGroup>();

        // this loop is directly inspired from hudson.model.Label.getTiedJobs()
        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
            if (!(job instanceof TopLevelItem)) {
                // consider only TopLevelItem - not 100% sure why, though...
                continue;
            }

            Label jobLabel = job.getAssignedLabel();
            if (isLabelRelevant(jobLabel)) {
                JobsGroup matchingJobGroup = tmpResult.get(jobLabel);
                if (matchingJobGroup == null) {
                    matchingJobGroup = new JobsGroup(jobLabel);
                    tmpResult.put(jobLabel, matchingJobGroup);
                }
                matchingJobGroup.addJob(job);
            }
        }
        
        // then browse list of triggered jobs
        HashMap<Label, HashMap<AbstractProject<?,?>, TriggeredJob>> triggeredJobsByLabel =
                new HashMap<Label, HashMap<AbstractProject<?,?>, TriggeredJob>>();
        TriggeredJobsHelper.populateTriggeredJobs(triggeredJobsByLabel);
        for (Map.Entry<Label, HashMap<AbstractProject<?, ?>, TriggeredJob>> entry : triggeredJobsByLabel.entrySet()) {
            Label label = entry.getKey();
            if (isLabelRelevant(label)) {
                JobsGroup matchingJobGroup = tmpResult.get(label);
                if (matchingJobGroup == null) {
                    matchingJobGroup = new JobsGroup(label);
                    tmpResult.put(label, matchingJobGroup);
                }
                // get the list of all triggered jobs
                matchingJobGroup.addTriggeredJobs(entry.getValue().values());
            }
        }
        
        // then browse list of jobs with a Label parameter with a default value
        HashMap<Label, List<AbstractProject<?,?>>> jobsByDefaultLabel = new HashMap<Label, List<AbstractProject<?,?>>>();
        TriggeredJobsHelper.populateJobsWithLabelDefaultValue(jobsByDefaultLabel);
        for (Map.Entry<Label, List<AbstractProject<?, ?>>> entry : jobsByDefaultLabel.entrySet()) {
            Label label = entry.getKey();
            if (isLabelRelevant(label)) {
                JobsGroup matchJobsGroup = tmpResult.get(label);
                if (matchJobsGroup == null) {
                    matchJobsGroup = new JobsGroup(label);
                    tmpResult.put(label, matchJobsGroup);
                }
                matchJobsGroup.addJobsWithDefaultValue(entry.getValue());
            }
        }

        return buildResult(tmpResult);
    }
    
    protected abstract List<JobsGroup> buildResult(HashMap<Label, JobsGroup> tmpResult);
    protected abstract boolean isLabelRelevant(Label jobLabel);
}
