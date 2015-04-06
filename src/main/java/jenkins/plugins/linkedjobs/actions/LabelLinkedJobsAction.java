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
import java.util.List;

import org.jvnet.jenkins.plugins.nodelabelparameter.parameterizedtrigger.AllNodesForLabelBuildParameterFactory;

import jenkins.plugins.linkedjobs.model.JobsGroup;
import jenkins.plugins.linkedjobs.settings.GlobalSettings;
import jenkins.model.Jenkins;
import hudson.model.Action;
import hudson.model.TopLevelItem;
import hudson.model.AbstractProject;
import hudson.model.Label;
import hudson.model.Project;
import hudson.model.labels.LabelAtom;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameterFactory;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.tasks.Builder;

/**
 * For each Label, Jenkins associates an object of this class to a Linked Jobs page.<br/><br/>
 * This class is responsible for building & collecting the data necessary to build
 * this additional page. Besides boiler-plate code for pure UI purpose, the interesting
 * function is getJobsGroups, which scans all Jobs/Projects of this Jenkins instance
 * @author dominiquebrice
 *
 */
public class LabelLinkedJobsAction implements Action {
    
    /**
     * The label associated to this action
     */
    private final LabelAtom label;
    private final boolean parameterizedTriggerAndNodeLabelParameterPluginsInstalled;

    public LabelLinkedJobsAction(LabelAtom labelAtom) {
        // for now only store the label
        // calculation is done when requested by display
        this.label = labelAtom;
        this.parameterizedTriggerAndNodeLabelParameterPluginsInstalled =
            Jenkins.getInstance().getPlugin("parameterized-trigger") != null
         && Jenkins.getInstance().getPlugin("nodelabelparameter") != null;
    }

    public String getIconFileName() {
        return "search.png";
    }

    // name of the additional link in the left-side menu for labels
    public String getDisplayName() {
        return "Linked Jobs";
    }

    public String getTitle() {
        return label.getDisplayName() + " " + getDisplayName();
    }

    // relative URL used for the additional link in the left-side menu for
    // labels
    public String getUrlName() {
        return "linkedjobs";
    }

    // will be called by my friend jelly via ${it.label}. Of course!
    public LabelAtom getLabel() {
        return this.label;
    }

    public boolean getDetailedView() {
        return GlobalSettings.get().getDetailedView();
    }

    /**
     * This function retrieves all jobs existing on this jenkins instance and
     * group them by "assigned label", keeping only such labels that contains
     * the particular label of this LabelLinkedJobsAction instance
     * 
     * @return a list of jobs grouped by assigned label
     */
    public List<JobsGroup> getJobsGroups() {
        HashMap<Label, JobsGroup> tmpResult = new HashMap<Label, JobsGroup>();

        // this loop is directly inspired from hudson.model.Label.getTiedJobs()
        for (AbstractProject<?, ?> job : Jenkins.getInstance().getAllItems(
                AbstractProject.class)) {
            if (job instanceof TopLevelItem) {
                Label jobLabel = job.getAssignedLabel();
                // condition for a job to be listed is simply to use the label,
                // regardless of the actual meaning of the expression
                // In other words, jobs with assigned label "windows&&!jdk7"
                // will show up on the jdk7 "linked jobs" page
                if (isAssignedLabelLinked(jobLabel)) {
                    JobsGroup matchingJobGroup = tmpResult.get(jobLabel);
                    if (matchingJobGroup == null) {
                        matchingJobGroup = new JobsGroup(jobLabel);
                        tmpResult.put(jobLabel, matchingJobGroup);
                    }
                    matchingJobGroup.addJob(job);
                }
            }
            
            // JENKINS-27588
            // need to explore more in depth jobs configuration to look for jobs triggered by this label
            listTriggeredJobs(job, tmpResult);
        }

        ArrayList<JobsGroup> result = new ArrayList<JobsGroup>(tmpResult.size());
        // keep track from where we must finally sort the result
        int fromIndex = 0;
        // if the label itself is present, put it first in the list
        if (tmpResult.containsKey(label)) {
            result.add(tmpResult.remove(label));
            fromIndex = 1;
        }
        // add all remaining groups
        result.addAll(tmpResult.values());
        // and order them
        Collections.sort(result.subList(fromIndex, result.size()));

        return result;
    }

    // JENKINS-27588
    // support configuration possible with parameterized-trigger & nodelabelparameter plugins
    private void listTriggeredJobs(AbstractProject triggeringJob, HashMap<Label, JobsGroup> tmpResult) {
        if (parameterizedTriggerAndNodeLabelParameterPluginsInstalled && triggeringJob instanceof Project) {
            List<Builder> builders = ((Project) triggeringJob).getBuilders();
            for (Builder builder : builders) {
                if (!(builder instanceof TriggerBuilder)) {
                    continue;
                }
                // this job is triggering other jobs...
                List<BlockableBuildTriggerConfig> configs = ((TriggerBuilder) builder).getConfigs();
                if (configs != null) {
                    for (BlockableBuildTriggerConfig config : configs) {
                        List<AbstractBuildParameterFactory> factories = config.getConfigFactories();
                        if (factories == null) {
                            continue;
                        }
                        for (AbstractBuildParameterFactory factory : factories) {
                            if (!(factory instanceof AllNodesForLabelBuildParameterFactory)) {
                                continue;
                            }

                            // and it's triggering jobs based on specific label
                            // using the nodelabelparameter plugin
                            Label jobLabel = Jenkins.getInstance().getLabel(
                                    ((AllNodesForLabelBuildParameterFactory) factory).nodeLabel);
                            // TODO: expand nodeLabel?
                            if (isAssignedLabelLinked(jobLabel)) {
                                JobsGroup matchingJobGroup = tmpResult.get(jobLabel);
                                if (matchingJobGroup == null) {
                                    matchingJobGroup = new JobsGroup(jobLabel);
                                    tmpResult.put(jobLabel, matchingJobGroup);
                                }
                                // get the list of all triggered jobs
                                List<AbstractProject> triggeredJobs = config.getProjectList(null);
                                if (triggeredJobs != null) {
                                    for (AbstractProject triggeredJob : triggeredJobs) {
                                        // and store them, associated to the
                                        // triggering job
                                        matchingJobGroup.addTriggeredJob(triggeredJob, triggeringJob);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected boolean isAssignedLabelLinked(Label jobLabel) {
        return jobLabel != null && jobLabel.listAtoms().contains(this.label);
    }
}